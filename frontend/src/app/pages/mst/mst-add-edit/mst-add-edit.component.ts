import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { MstService } from '../mst.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseMstItemStockModalComponent } from '@/app/shared/modals/browse-mst-item-stock-modal/browse-mst-item-stock-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerTrash, tablerArrowLeft, tablerCheck, tablerPlus } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-mst-add-edit',
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
    templateUrl: './mst-add-edit.component.html'
})
export class MstAddEditComponent {
    module    = 'Material Salvage Ticket';
    subModule = 'Create';
    menuLink  = 'mst';

    id: any  = null;
    editMode = false;
    isLoading = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // Header fields
    voucherDate = '';
    purpose     = '';

    // Dropdowns
    departments        = signal<any[]>([]);
    inventoryLocations = signal<any[]>([]);
    selectedDepartment: any = null;
    selectedLocation:   any = null;

    // Signatories
    returnedBy: any = null;
    receivedBy: any = null;

    // Items
    details: any[] = [];

    private service      = inject(MstService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    ngOnInit(): void {
        this.loadDepartments();
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

    // ─── Data Loading ─────────────────────────────────────────────────────────

    private loadDepartments(): void {
        this.service.getDepartments().subscribe({
            next: (d) => this.departments.set(d || []),
            error: () => {}
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
                    this.returnedBy = data.returnedBy || null;
                    this.receivedBy = data.receivedBy || null;
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
                this.purpose     = data.purpose || data.remarks || '';
                this.returnedBy  = data.returnedBy || null;
                this.receivedBy  = data.receivedBy || null;
                this.details     = (data.details || []).map((d: any) => ({ ...d }));

                // Match department after dropdown loads
                const tryMatchDept = () => {
                    if (data.department?.id) {
                        this.selectedDepartment = this.departments().find(d => d.id === data.department.id) ?? data.department;
                    }
                };
                tryMatchDept();
                setTimeout(tryMatchDept, 400);

                // Match inventory location after dropdown loads
                const tryMatchLoc = () => {
                    if (data.inventoryLocation?.id) {
                        this.selectedLocation = this.inventoryLocations().find(l => l.id === data.inventoryLocation.id) ?? data.inventoryLocation;
                    }
                };
                tryMatchLoc();
                setTimeout(tryMatchLoc, 400);
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load record.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    // ─── Items ────────────────────────────────────────────────────────────────

    async openItemStockBrowse(): Promise<void> {
        if (!this.selectedLocation?.id) {
            this.alertService.warning(this.module, 'Validation', 'Please select an Inventory Location first.');
            return;
        }
        try {
            const result = await this.modalService.openModal(
                BrowseMstItemStockModalComponent,
                { locationId: this.selectedLocation.id },
                { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.onItemStockSelected(result.data);
            }
        } catch { }
    }

    private onItemStockSelected(itemStock: any): void {
        const itemStockId = itemStock.id;
        const isDuplicate = this.details.some(d => d.itemStockId === itemStockId);
        if (isDuplicate) {
            this.alertService.error(this.module, 'Duplicate Item', 'This item is already in the list.');
            return;
        }
        this.details = [...this.details, {
            itemStockId:         itemStockId,
            itemId:              itemStock.item?.id          ?? null,
            itemCode:            itemStock.item?.code        ?? itemStock.code ?? '',
            unitCode:            itemStock.item?.unit?.code  ?? itemStock.unitCode ?? '',
            unitCost:            Number(itemStock.unitCost)  || 0,
            itemDescription:     itemStock.item?.description ?? itemStock.description ?? '',
            quantity:            0,
            inventoryLocationId: itemStock.inventoryLocation?.id ?? this.selectedLocation?.id ?? null,
            isUsable:            true
        }];
    }

    onQuantityChange(index: number): void {
        const row = this.details[index];
        if (!row) return;
        const qty = Number(row.quantity) || 0;
        if (qty < 0) {
            this.details[index] = { ...row, quantity: 0 };
        }
    }

    removeRow(index: number): void {
        this.details = this.details.filter((_, i) => i !== index);
    }

    // ─── Browse: Signatories ──────────────────────────────────────────────────

    async openReturnedByBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const e = result.data;
                this.returnedBy = { accountNo: e.accountNo, fullName: e.fullName || e.name };
            }
        } catch { }
    }

    async openReceivedByBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const e = result.data;
                this.receivedBy = { accountNo: e.accountNo, fullName: e.fullName || e.name };
            }
        } catch { }
    }

    // ─── Save ─────────────────────────────────────────────────────────────────

    save(): void {
        if (!this.voucherDate) {
            this.alertService.warning(this.module, 'Validation', 'Date is required.');
            return;
        }
        if (!this.purpose?.trim()) {
            this.alertService.warning(this.module, 'Validation', 'Purpose is required.');
            return;
        }
        if (!this.selectedDepartment?.id) {
            this.alertService.warning(this.module, 'Validation', 'Department is required.');
            return;
        }
        if (!this.selectedLocation?.id) {
            this.alertService.warning(this.module, 'Validation', 'Inventory Location is required.');
            return;
        }
        if (this.details.length === 0) {
            this.alertService.warning(this.module, 'Validation', 'No items selected. Please add at least one item.');
            return;
        }
        const invalidQty = this.details.find(d => !Number(d.quantity) || Number(d.quantity) <= 0);
        if (invalidQty) {
            this.alertService.warning(this.module, 'Validation',
                `Quantity for item "${invalidQty.itemCode}" must be greater than zero.`);
            return;
        }
        if (!this.returnedBy?.accountNo) {
            this.alertService.warning(this.module, 'Validation', 'Returned By is required.');
            return;
        }
        if (!this.receivedBy?.accountNo) {
            this.alertService.warning(this.module, 'Validation', 'Received By is required.');
            return;
        }

        this.isLoading.set(true);

        const payload: any = {
            voucherDate:       this.voucherDate,
            purpose:           this.purpose.trim(),
            inventoryLocation: { id: this.selectedLocation.id },
            department:        { id: this.selectedDepartment.id },
            returnedBy:        { accountNo: this.returnedBy.accountNo, fullName: this.returnedBy.fullName },
            receivedBy:        { accountNo: this.receivedBy.accountNo, fullName: this.receivedBy.fullName },
            details:           this.details.map(d => ({
                itemStockId:         d.itemStockId         || null,
                itemId:              d.itemId              || null,
                itemCode:            d.itemCode            || '',
                unitCode:            d.unitCode            || '',
                unitCost:            Number(d.unitCost)    || 0,
                itemDescription:     d.itemDescription     || '',
                quantity:            Number(d.quantity)    || 0,
                inventoryLocationId: d.inventoryLocationId || null,
                isUsable:            d.isUsable !== false
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
