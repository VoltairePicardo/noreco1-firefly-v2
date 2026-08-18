import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { StockTransferService } from '../stock-transfer.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseStItemStockModalComponent } from '@/app/shared/modals/browse-st-item-stock-modal/browse-st-item-stock-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerTrash, tablerArrowLeft, tablerCheck, tablerPlus } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-stock-transfer-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective,
        RouterLink
    ],
    providers: [
        provideFlatpickrDefaults(),
        ...SHARED_PROVIDERS,
        provideIcons({ tablerSearch, tablerTrash, tablerArrowLeft, tablerCheck, tablerPlus })
    ],
    templateUrl: './stock-transfer-add-edit.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class StockTransferAddEditComponent implements OnInit {
    module    = 'Stock Transfer';
    subModule = 'Create';
    menuLink  = 'stock-transfer';

    id: any  = null;
    editMode = false;
    isLoading = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    voucherDate = '';
    remarks     = '';

    inventoryLocations   = signal<any[]>([]);
    selectedFromLocation = signal<any>(null);
    selectedToLocation   = signal<any>(null);

    toInventoryLocations = computed(() => this.inventoryLocations().filter(l => l.id !== this.selectedFromLocation()?.id));

    approvedBy = signal<any>(null);

    details = signal<any[]>([]);

    private service      = inject(StockTransferService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.loadInventoryLocations();

        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.editMode = idParam != null && /^\d+$/.test(idParam);
            if (this.editMode) {
                this.id = Number(idParam);
                this.subModule = 'Edit';
                this.loadForEdit();
            } else {
                this.subModule = 'Create';
                const today = new Date().toISOString().substring(0, 10);
                this.voucherDate = today;
                this.loadDefaultSignatories();
            }
        });
    }

    private loadInventoryLocations(): void {
        this.service.getInventoryLocations().subscribe({
            next: (d) => this.inventoryLocations.set(d || []),
            error: () => {}
        });
    }

    private loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (data) => {
                if (data) { this.approvedBy.set(data.approvedBy || null); }
            },
            error: () => {}
        });
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (!data?.id) {
                    this.alertService.error(this.module, 'Record not found.', '');
                    this.router.navigate(['/' + this.menuLink]);
                    return;
                }
                const toYmd = (v: any) => v ? new Date(v).toISOString().substring(0, 10) : '';
                this.voucherDate = toYmd(data.voucherDate);
                this.remarks     = data.remarks || '';
                this.approvedBy.set(data.approvingOfficer || null);
                this.details.set((data.details || []).map((d: any) => ({
                    ...d,
                    stockQuantity: d.stockQuantity ?? d.quantity ?? 0
                })));

                const tryMatchFrom = () => {
                    if (data.fromInventoryLocation?.id) {
                        this.selectedFromLocation.set(this.inventoryLocations().find(l => l.id === data.fromInventoryLocation.id) ?? data.fromInventoryLocation);
                    }
                };
                tryMatchFrom();
                setTimeout(tryMatchFrom, 400);

                const tryMatchTo = () => {
                    if (data.toInventoryLocation?.id) {
                        this.selectedToLocation.set(this.inventoryLocations().find(l => l.id === data.toInventoryLocation.id) ?? data.toInventoryLocation);
                    }
                };
                tryMatchTo();
                setTimeout(tryMatchTo, 400);
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load record.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    async openItemStockBrowse(): Promise<void> {
        if (!this.selectedFromLocation()?.id) {
            this.alertService.warning(this.module, 'Validation', 'Please select a Source Location first.');
            return;
        }
        try {
            const result = await this.modalService.openModal(
                BrowseStItemStockModalComponent,
                { locationId: this.selectedFromLocation().id },
                { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.onItemStockSelected(result.data);
            }
        } catch { }
    }

    private onItemStockSelected(itemStock: any): void {
        const itemStockId = itemStock.id;
        const isDuplicate = this.details().some(d => d.itemStockId === itemStockId);
        if (isDuplicate) {
            this.alertService.error(this.module, 'Duplicate Item', 'This item is already in the list.');
            return;
        }
        this.details.update(list => [...list, {
            itemStockId:         itemStockId,
            itemId:              itemStock.item?.id          ?? null,
            itemCode:            itemStock.item?.code        ?? itemStock.code ?? '',
            unitCode:            itemStock.item?.unit?.code  ?? itemStock.unitCode ?? '',
            unitCost:            Number(itemStock.unitCost)  || 0,
            itemDescription:     itemStock.item?.description ?? itemStock.description ?? '',
            stockQuantity:       Number(itemStock.totalQuantity ?? itemStock.quantity) || 0,
            quantity:            0,
            inventoryLocationId: itemStock.inventoryLocation?.id ?? this.selectedFromLocation()?.id ?? null
        }]);
    }

    onQuantityChange(index: number): void {
        const row = this.details()[index];
        if (!row) return;
        const qty   = Number(row.quantity)      || 0;
        const stock = Number(row.stockQuantity) || 0;
        if (qty < 0) {
            row.quantity = 0;
            this.details.update(list => [...list]);
        } else if (stock > 0 && qty > stock) {
            row.quantity = stock;
            this.details.update(list => [...list]);
            this.alertService.warning(this.module, 'Validation',
                `Transfer quantity for ${row.itemCode} cannot exceed available stock (${stock}).`);
        }
    }

    removeRow(index: number): void {
        this.details.update(list => list.filter((_, i) => i !== index));
    }

    async openApprovedByBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const e = result.data;
                this.approvedBy.set({ accountNo: e.accountNo, fullName: e.fullName || e.name });
            }
        } catch { }
    }

    save(): void {
        if (!this.voucherDate) {
            this.alertService.warning(this.module, 'Validation', 'Date is required.');
            return;
        }
        if (!this.remarks?.trim()) {
            this.alertService.warning(this.module, 'Validation', 'Remarks is required.');
            return;
        }
        const fromLocation = this.selectedFromLocation();
        const toLocation   = this.selectedToLocation();
        const details      = this.details();
        const approvedBy   = this.approvedBy();

        if (!fromLocation?.id) {
            this.alertService.warning(this.module, 'Validation', 'Source Location is required.');
            return;
        }
        if (!toLocation?.id) {
            this.alertService.warning(this.module, 'Validation', 'Destination Location is required.');
            return;
        }
        if (fromLocation?.id === toLocation?.id) {
            this.alertService.warning(this.module, 'Validation', 'Source and Destination must be different.');
            return;
        }
        if (details.length === 0) {
            this.alertService.warning(this.module, 'Validation', 'No items selected. Please add at least one item.');
            return;
        }
        const invalidQty = details.find(d => !Number(d.quantity) || Number(d.quantity) <= 0);
        if (invalidQty) {
            this.alertService.warning(this.module, 'Validation',
                `Transfer quantity for "${invalidQty.itemCode}" must be greater than zero.`);
            return;
        }
        if (!approvedBy?.accountNo) {
            this.alertService.warning(this.module, 'Validation', 'Approved By is required.');
            return;
        }

        this.isLoading.set(true);

        const payload: any = {
            voucherDate:          this.voucherDate,
            remarks:              this.remarks.trim(),
            fromInventoryLocation: { id: fromLocation.id },
            toInventoryLocation:   { id: toLocation.id },
            approvingOfficer:      { accountNo: approvedBy.accountNo, fullName: approvedBy.fullName },
            details:              details.map(d => ({
                itemStockId:         d.itemStockId         || null,
                itemId:              d.itemId              || null,
                itemCode:            d.itemCode            || '',
                unitCode:            d.unitCode            || '',
                unitCost:            Number(d.unitCost)    || 0,
                itemDescription:     d.itemDescription     || '',
                quantity:            Number(d.quantity)    || 0,
                inventoryLocationId: d.inventoryLocationId || null
            }))
        };
        if (this.editMode) payload.id = this.id;

        const request$ = this.editMode ? this.service.update(payload) : this.service.create(payload);

        request$.subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.success === false) {
                    this.alertService.error(this.module, 'Save', data.failureMessage || '');
                } else {
                    this.alertService.success(
                        this.module,
                        this.editMode ? 'Updated successfully.' : 'Created successfully.',
                        ''
                    );
                    const id = data?.modelId ?? data?.id ?? this.id;
                    this.router.navigate(['/' + this.menuLink, id, 'detail']);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Save', 'An error occurred.');
            }
        });
    }

    compareById(a: any, b: any): boolean {
        return a && b ? a.id === b.id : a === b;
    }
}
