import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { WithdrawalService } from '../withdrawal.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseWorkOrderModalComponent } from '@/app/shared/modals/browse-work-order-modal/browse-work-order-modal.component';
import { BrowseItemStockModalComponent } from '@/app/shared/modals/browse-item-stock-modal/browse-item-stock-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerTrash, tablerPlus, tablerArrowLeft, tablerCheck } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-withdrawal-add-edit',
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
        provideIcons({ tablerSearch, tablerTrash, tablerPlus, tablerArrowLeft, tablerCheck })
    ],
    templateUrl: './withdrawal-add-edit.component.html'
})
export class WithdrawalAddEditComponent {
    module    = 'Stock Withdrawal';
    subModule = 'Create';
    menuLink  = 'withdrawal';

    id: any   = null;
    editMode  = false;
    isLoading = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // Header fields
    voucherDate = '';
    description = '';

    // Dropdowns
    inventoryLocations  = signal<any[]>([]);
    inventoryCategories = signal<any[]>([]);
    purposes            = signal<any[]>([]);

    selectedLocation: any = null;
    selectedCategory: any = null;
    selectedPurpose:  any = null;

    // Reference documents
    selectedWorkOrder: any = null;
    workOrderDesc          = '';

    // Approving officer
    approvingOfficer: any = null;

    // Employees (shown when category type = OFFICE_EQUIPMENT_FURNITURE_FIXTURES)
    employees: any[] = [];

    // Items
    details: any[] = [];

    // Computed visibility
    get showPurposeInput(): boolean {
        return this.selectedPurpose?.type === 'OTHERS';
    }
    get showEmployeeSection(): boolean {
        return this.selectedCategory?.type === 'OFFICE_EQUIPMENT_FURNITURE_FIXTURES';
    }
    get totalQuantity(): number {
        return this.details.reduce((s, r) => s + (Number(r.quantity) || 0), 0);
    }

    private service      = inject(WithdrawalService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    ngOnInit(): void {
        this.loadDropdowns();

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

    private loadDropdowns(): void {
        this.service.getInventoryLocations().subscribe({
            next: (d) => this.inventoryLocations.set(d || []),
            error: () => {}
        });
        this.service.getInventoryCategories().subscribe({
            next: (d) => this.inventoryCategories.set(d || []),
            error: () => {}
        });
        this.service.getPurposes().subscribe({
            next: (d) => this.purposes.set(d || []),
            error: () => {}
        });
    }

    private loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (data) => {
                if (data) this.approvingOfficer = data.approvedBy || data.approvingOfficer || null;
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

                this.voucherDate      = toYmd(data.voucherDate);
                this.description      = data.description || '';
                this.approvingOfficer = data.approvingOfficer || null;
                this.employees        = data.employees || [];
                this.details          = data.details   || [];

                // Match dropdowns by id after they've loaded
                const tryMatch = () => {
                    if (data.inventoryLocation?.id) {
                        this.selectedLocation = this.inventoryLocations().find(l => l.id === data.inventoryLocation.id) ?? data.inventoryLocation;
                    }
                    if (data.inventoryCategory?.id) {
                        this.selectedCategory = this.inventoryCategories().find(c => c.id === data.inventoryCategory.id) ?? data.inventoryCategory;
                    }
                    if (data.purpose?.id) {
                        this.selectedPurpose = this.purposes().find(p => p.id === data.purpose.id) ?? data.purpose;
                    }
                };
                // Retry once after a tick in case dropdowns haven't loaded yet
                tryMatch();
                setTimeout(tryMatch, 400);

                if (data.workOrder?.id) {
                    this.selectedWorkOrder = data.workOrder;
                    this.workOrderDesc = data.workOrder.code || '';
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load record.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    // ─── Category Change Handler ──────────────────────────────────────────────

    onCategoryChange(): void {
        if (!this.showEmployeeSection) {
            this.employees = [];
        }
    }

    // ─── Browse: Approving Officer ────────────────────────────────────────────

    async openApprovingOfficerBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.approvingOfficer = result.data;
            }
        } catch { }
    }

    // ─── Browse: Work Order ───────────────────────────────────────────────────

    async openWorkOrderBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseWorkOrderModalComponent, {}, { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const wo = result.data;
                this.selectedWorkOrder = wo;
                this.workOrderDesc = (wo.code || '') + (wo.project?.name ? ' — ' + wo.project.name : '');
                // Load WO items if location and category selected
                if (this.selectedLocation?.id && this.selectedCategory?.id) {
                    this.service.getWorkOrderDetails(wo.id, this.selectedLocation.id, this.selectedCategory.id).subscribe({
                        next: (items) => { this.details = items || []; },
                        error: () => this.alertService.error(this.module, 'Failed to load Work Order items.', '')
                    });
                }
            }
        } catch { }
    }

    clearWorkOrder(): void {
        this.selectedWorkOrder = null;
        this.workOrderDesc     = '';
        this.details           = [];
    }

    // ─── Browse: Item Stock ───────────────────────────────────────────────────

    async openItemStockBrowse(): Promise<void> {
        if (!this.selectedLocation?.id || !this.selectedCategory?.id) {
            this.alertService.warning(this.module, 'Validation', 'Please select Inventory Location and Category first.');
            return;
        }
        try {
            const result = await this.modalService.openModal(
                BrowseItemStockModalComponent,
                { locationId: this.selectedLocation.id, categoryId: this.selectedCategory.id },
                { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const stock = result.data;
                const isDuplicate = this.details.some(d => d.itemStockId === stock.id);
                if (isDuplicate) {
                    this.alertService.warning(this.module, 'Validation', 'Duplicate item.');
                    return;
                }
                this.details.push({
                    itemStockId:         stock.id,
                    itemId:              stock.item?.id,
                    itemCode:            stock.item?.code,
                    itemDescription:     stock.item?.description,
                    unitCode:            stock.item?.unit?.code,
                    unitId:              stock.item?.unit?.id,
                    unitCost:            stock.unitCost,
                    quantity:            0,
                    quantityReleased:    0,
                    inventoryBalance:    stock.totalQuantity,
                    inventoryLocationId: stock.inventoryLocation?.id ?? this.selectedLocation.id,
                    specialEquipment:    false
                });
            }
        } catch { }
    }

    // ─── Browse: Employee ─────────────────────────────────────────────────────

    async openEmployeeBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const emp = result.data;
                const isDuplicate = this.employees.some(e => e.accountNo === emp.accountNo);
                if (isDuplicate) {
                    this.alertService.warning(this.module, 'Validation', 'Duplicate employee.');
                    return;
                }
                this.employees.push({ name: emp.name || emp.fullName, accountNo: emp.accountNo });
            }
        } catch { }
    }

    removeEmployee(index: number): void {
        this.employees.splice(index, 1);
    }

    // ─── Items Table Helpers ──────────────────────────────────────────────────

    /**
     * Cap quantity to inventoryBalance on change.
     */
    onQuantityChange(index: number): void {
        const row = this.details[index];
        if (!row) return;
        const qty = Number(row.quantity) || 0;
        const bal = Number(row.inventoryBalance) || 0;
        if (qty > bal) {
            row.quantity = bal;
            this.alertService.warning(this.module, 'Validation',
                `Quantity cannot exceed inventory balance (${bal}).`);
        }
    }

    removeRow(index: number): void {
        this.details.splice(index, 1);
    }

    compareById(a: any, b: any): boolean {
        return a && b ? a.id === b.id : a === b;
    }

    // ─── Save ─────────────────────────────────────────────────────────────────

    save(): void {
        if (!this.voucherDate) {
            this.alertService.warning(this.module, 'Validation', 'Voucher Date is required.');
            return;
        }
        if (!this.selectedLocation?.id) {
            this.alertService.warning(this.module, 'Validation', 'Inventory Location is required.');
            return;
        }
        if (!this.selectedCategory?.id) {
            this.alertService.warning(this.module, 'Validation', 'Inventory Category is required.');
            return;
        }
        if (!this.selectedPurpose?.id) {
            this.alertService.warning(this.module, 'Validation', 'Purpose is required.');
            return;
        }
        if (this.showPurposeInput && !this.description.trim()) {
            this.alertService.warning(this.module, 'Validation', 'Please describe the purpose (Others).');
            return;
        }
        if (!this.approvingOfficer?.accountNo) {
            this.alertService.warning(this.module, 'Validation', 'Approving Officer is required.');
            return;
        }
        if (this.details.length === 0) {
            this.alertService.warning(this.module, 'Validation', 'Please add at least one item.');
            return;
        }
        const hasQty = this.details.some(d => (Number(d.quantity) || 0) > 0);
        if (!hasQty) {
            this.alertService.warning(this.module, 'Validation', 'Total quantity is zero — enter quantities for items.');
            return;
        }

        this.isLoading.set(true);

        const payload: any = {
            voucherDate:      this.voucherDate,
            description:      this.description.trim() || null,
            approvingOfficer: { accountNo: this.approvingOfficer.accountNo, fullName: this.approvingOfficer.fullName || this.approvingOfficer.name },
            inventoryLocation: { id: this.selectedLocation.id },
            inventoryCategory: { id: this.selectedCategory.id },
            purpose:           { id: this.selectedPurpose.id },
            type:              this.selectedCategory.type || null,
            employees:         this.employees,
            details:           this.details.map(d => ({
                itemStockId:         d.itemStockId        || null,
                itemId:              d.itemId             || null,
                itemCode:            d.itemCode           || '',
                itemDescription:     d.itemDescription    || '',
                unitCode:            d.unitCode           || '',
                unitId:              d.unitId             || null,
                unitCost:            Number(d.unitCost)   || 0,
                quantity:            Number(d.quantity)   || 0,
                quantityReleased:    Number(d.quantityReleased) || 0,
                inventoryBalance:    Number(d.inventoryBalance) || 0,
                inventoryLocationId: d.inventoryLocationId ?? this.selectedLocation.id,
                specialEquipment:    d.specialEquipment   || false
            })),
            workOrder:      this.selectedWorkOrder ? { id: this.selectedWorkOrder.id } : null,
            purchaseRequest: null,
            costEstimate:    null
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
}
