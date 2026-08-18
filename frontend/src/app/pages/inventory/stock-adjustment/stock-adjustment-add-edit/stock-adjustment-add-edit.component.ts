import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { StockAdjustmentService } from '../stock-adjustment.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseSaItemStockModalComponent } from '@/app/shared/modals/browse-sa-item-stock-modal/browse-sa-item-stock-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerTrash, tablerArrowLeft, tablerCheck, tablerPlus } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-stock-adjustment-add-edit',
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
    templateUrl: './stock-adjustment-add-edit.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class StockAdjustmentAddEditComponent implements OnInit {
    module    = 'Stock Adjustment';
    subModule = 'Create';
    menuLink  = 'stock-adjustment';

    id: any  = null;
    editMode = false;
    isLoading = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    voucherDate = '';
    type        = '';
    remarks     = '';

    inventoryLocations = signal<any[]>([]);
    selectedLocation   = signal<any>(null);

    checker    = signal<any>(null);
    approvedBy = signal<any>(null);

    details = signal<any[]>([]);

    private service      = inject(StockAdjustmentService);
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
                if (data) {
                    this.checker.set(data.checker || null);
                    this.approvedBy.set(data.approvedBy || null);
                }
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
                this.type        = data.type    || '';
                this.remarks     = data.remarks || '';
                this.checker.set(data.checker          || null);
                this.approvedBy.set(data.approvingOfficer || null);
                this.details.set((data.details || []).map((d: any) => ({ ...d })));

                const tryMatch = () => {
                    if (data.inventoryLocation?.id) {
                        this.selectedLocation.set(this.inventoryLocations().find(l => l.id === data.inventoryLocation.id) ?? data.inventoryLocation);
                    }
                };
                tryMatch();
                setTimeout(tryMatch, 400);
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load record.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    async openItemStockBrowse(): Promise<void> {
        if (!this.selectedLocation()?.id) {
            this.alertService.warning(this.module, 'Validation', 'Please select an Inventory Location first.');
            return;
        }
        try {
            const result = await this.modalService.openModal(
                BrowseSaItemStockModalComponent,
                { locationId: this.selectedLocation().id },
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
        const location = this.selectedLocation();
        this.details.update(list => [...list, {
            itemStockId:         itemStockId,
            itemId:              itemStock.item?.id            ?? null,
            itemCode:            itemStock.item?.code          ?? itemStock.code ?? '',
            unitCode:            itemStock.item?.unit?.code    ?? itemStock.unitCode ?? '',
            unitCost:            Number(itemStock.unitCost)    || 0,
            itemDescription:     itemStock.item?.description   ?? itemStock.description ?? '',
            quantity:            Number(itemStock.totalQuantity ?? itemStock.quantity) || 0,
            inventoryLocationId: itemStock.inventoryLocation?.id ?? location?.id ?? null,
            adjustment:          0
        }]);
    }

    onAdjustmentChange(index: number): void {
        const details = this.details();
        const row = details[index];
        if (!row) return;
        const adj = Number(row.adjustment) || 0;
        const qty = Number(row.quantity)   || 0;
        if (adj < 0 && Math.abs(adj) > qty) {
            row.adjustment = -qty;
            this.alertService.warning(this.module, 'Validation',
                `Adjustment for ${row.itemCode} cannot reduce stock below zero (max decrease: ${qty}).`);
        }
        this.details.update(list => [...list]);
    }

    removeRow(index: number): void {
        this.details.update(list => list.filter((_, i) => i !== index));
    }

    async openCheckerBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const e = result.data;
                this.checker.set({ accountNo: e.accountNo, fullName: e.fullName || e.name });
            }
        } catch { }
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
        if (!this.selectedLocation()?.id) {
            this.alertService.warning(this.module, 'Validation', 'Inventory Location is required.');
            return;
        }
        if (this.details().length === 0) {
            this.alertService.warning(this.module, 'Validation', 'No items selected. Please add at least one item.');
            return;
        }
        if (!this.checker()?.accountNo) {
            this.alertService.warning(this.module, 'Validation', 'Checked By is required.');
            return;
        }
        if (!this.approvedBy()?.accountNo) {
            this.alertService.warning(this.module, 'Validation', 'Approved By is required.');
            return;
        }

        this.isLoading.set(true);

        const payload: any = {
            voucherDate:       this.voucherDate,
            type:              this.type || null,
            remarks:           this.remarks.trim(),
            inventoryLocation: { id: this.selectedLocation().id },
            checker:           { accountNo: this.checker().accountNo,    fullName: this.checker().fullName },
            approvingOfficer:  { accountNo: this.approvedBy().accountNo, fullName: this.approvedBy().fullName },
            details:           this.details().map(d => ({
                itemStockId:         d.itemStockId         || null,
                itemId:              d.itemId              || null,
                itemCode:            d.itemCode            || '',
                unitCode:            d.unitCode            || '',
                unitCost:            Number(d.unitCost)    || 0,
                itemDescription:     d.itemDescription     || '',
                quantity:            Number(d.quantity)    || 0,
                inventoryLocationId: d.inventoryLocationId || null,
                adjustment:          Number(d.adjustment)  || 0
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
