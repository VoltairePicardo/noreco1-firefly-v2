import { Component, inject, signal } from '@angular/core';
import { FormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { LaddaModule } from 'angular2-ladda';
import { FlatpickrDefaults, FlatpickrModule } from 'angularx-flatpickr';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerPlus, tablerTrash, tablerX, tablerCheck } from '@ng-icons/tabler-icons';
import { RequisitionVoucherService } from '../requisition-voucher.service';
import { SharedModalService } from '@/app/shared/modals/shared-modal-service/shared-modal.service';
import { BrowseItemModalComponent } from '@/app/shared/modals/browse-item-modal/browse-item-modal.component';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseUserModalComponent } from '@/app/shared/modals/browse-user-modal/browse-user-modal.component';
import { BrowseBudgetLineItemModalComponent } from '@/app/shared/modals/browse-budget-line-item-modal/browse-budget-line-item-modal.component';
import { forkJoin } from 'rxjs';

/*const RV_TYPES = [
    { id: 1, label: 'For PO (Purchase Request)' },
    { id: 2, label: 'For IT (Internal Transfer)' },
    { id: 3, label: 'For REP (Repair)' },
    { id: 4, label: 'For Labor' },
];*/

const RV_TYPES = [
    { id: 1, label: 'For PO (Purchase Request)' },
    { id: 3, label: 'For REP (Repair/JO)' },
    { id: 4, label: 'For Labor' },
];

const WAREHOUSE_CATEGORIES = ['LINE_MATERIALS', 'SPECIAL_EQUIPMENTS', 'ELECTRICAL_MATERIALS', 'HOUSE_WIRING_MATERIALS'];

@Component({
    selector: 'app-requisition-voucher-add-edit',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, LaddaModule, FlatpickrModule],
    providers: [FlatpickrDefaults, provideIcons({ tablerSearch, tablerPlus, tablerTrash, tablerX, tablerCheck })],
    templateUrl: './requisition-voucher-add-edit.component.html'
})
export class RequisitionVoucherAddEditComponent {
    module     = 'Purchase/Work Request';
    subModule  = 'Create';
    menuLink   = 'requisition-voucher';
    id: any    = null;
    editMode   = false;
    submit     = false;
    formSubmit = false;
    isLoading  = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };
    rvTypes   = RV_TYPES;

    entities           = signal<any[]>([]);
    units              = signal<any[]>([]);
    vehicles           = signal<any[]>([]);
    offices            = signal<any[]>([]);
    budgetLineItems    = signal<any[]>([]);
    budgetSubItems     = signal<any[]>([]);
    workOrders         = signal<any[]>([]);
    costEstimates      = signal<any[]>([]);
    showBudgetSubItem  = signal(false);

    showInventoryChecked = signal(false);
    budgetBalance        = signal<any>(null);
    budgetSubItemBalance = signal<any>(null);

    lineItems: any[] = [];
    signatoryNames: { [key: string]: string } = {};
    selectedBudgetLineItemText = '';

    validationForm!: UntypedFormGroup;

    private service      = inject(RequisitionVoucherService);
    private modalService = inject(SharedModalService);
    public  fb           = inject(FormBuilder);
    public  route        = inject(ActivatedRoute);
    public  router       = inject(Router);
    public  alertService = inject(AlertService);

    ngOnInit(): void {
        forkJoin({
            entities:        this.service.getEntities(),
            units:           this.service.getUnits(),
            vehicles:        this.service.getVehicles(),
            offices:         this.service.getOffices(),
            budgetLineItems: this.service.getBudgetLineItems(),
            workOrders:      this.service.getWorkOrders(),
            costEstimates:   this.service.getCostEstimates(),
        }).subscribe({
            next: (res) => {
                this.entities.set(res.entities);
                this.units.set(res.units);
                this.vehicles.set(res.vehicles);
                this.offices.set(res.offices);
                this.budgetLineItems.set(res.budgetLineItems);
                this.workOrders.set(res.workOrders);
                this.costEstimates.set(res.costEstimates);
            },
            error: () => {}
        });

        this.route.paramMap.subscribe(params => {
            this.editMode = params.get('id') != null && /^\d+$/.test(params.get('id') ?? '');
            if (this.editMode) {
                this.id = +params.get('id')!;
                this.subModule = 'Edit';
                this.getData();
            } else {
                this.subModule = 'Create';
                this.initForm();
                this.addLineItem();
                this.loadDefaultSignatories();
            }
        });
    }

    loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (defaults) => {
                if (!defaults) return;
                const patch: any = {};
                if (defaults.approvedBy?.accountNo)         { patch['approvingOfficer']            = defaults.approvedBy.accountNo;         this.signatoryNames['approvingOfficer']            = defaults.approvedBy.name; }
                if (defaults.reviewedAcceptedBy?.accountNo) { patch['reviewedAcceptedByAccountNo'] = defaults.reviewedAcceptedBy.accountNo; this.signatoryNames['reviewedAcceptedByAccountNo'] = defaults.reviewedAcceptedBy.name; }
                if (defaults.inventoryCheckedBy?.accountNo) { patch['inventoryCheckedByAccountNo'] = defaults.inventoryCheckedBy.accountNo; this.signatoryNames['inventoryCheckedByAccountNo'] = defaults.inventoryCheckedBy.name; }
                if (defaults.recAppBy?.accountNo)           { patch['recAppByAccountNo']           = defaults.recAppBy.accountNo;           this.signatoryNames['recAppByAccountNo']           = defaults.recAppBy.name; }
                if (defaults.bacRepresentative?.accountNo)  { patch['bacRepresentativeAccountNo']  = defaults.bacRepresentative.accountNo;  this.signatoryNames['bacRepresentativeAccountNo']  = defaults.bacRepresentative.name; }
                if (defaults.budgetOfficer?.accountNo)      { patch['budgetOfficerAccountNo']      = defaults.budgetOfficer.accountNo;      this.signatoryNames['budgetOfficerAccountNo']      = defaults.budgetOfficer.name; }
                this.form.patchValue(patch);
            },
            error: () => {}
        });
    }

    initForm(data?: any): void {
        this.validationForm = this.fb.group({
            rvType:                      [data?.rvTypeId             || null,  Validators.required],
            voucherDate:                 [data?.voucherDate  ? this.toDateInput(data.voucherDate)  : '', Validators.required],
            deliveryDate:                [data?.deliveryDate ? this.toDateInput(data.deliveryDate) : ''],
            purpose:                     [data?.purpose              || '',    Validators.required],
            approvingOfficer:            [data?.approvedBy?.accountNo || null, Validators.required],
            // shared optional/conditional
            estimatedAmount:             [data?.estimatedAmount      || null],
            emergencyPurchase:           [data?.emergencyPurchase    || false],
            costEstimateId:              [data?.costEstimate?.id     || null],
            workOrderId:                 [data?.workOrder?.id        || null],
            budgetLineItemDetailId:      [data?.budgetLineItemDetail?.id || null],
            budgetSubItemId:             [data?.budgetSubItem?.id    || null],
            vehicleId:                   [data?.vehicle?.id          || null],
            reviewedAcceptedByAccountNo: [data?.reviewedAcceptedBy?.accountNo || null],
            inventoryCheckedByAccountNo: [data?.inventoryCheckedBy?.accountNo || null],
            // FOR_IT specific
            officeId:                    [data?.office?.id           || null],
            rvItType:                    [data?.rvItType              || null],
            recAppByAccountNo:           [data?.recAppBy?.accountNo  || null],
            bacRepresentativeAccountNo:  [data?.bacRepresentative?.accountNo  || null],
            budgetOfficerAccountNo:      [data?.budgetOfficer?.accountNo      || null],
            // FOR_LAB specific
            employeeAccountNo:           [data?.employee?.accountNo  || null],
            durationStart:               [data?.durationStart ? this.toDateInput(data.durationStart) : ''],
            durationEnd:                 [data?.durationEnd   ? this.toDateInput(data.durationEnd)   : ''],
        });

        this.syncDynamicValidators();

        // Restore signatory display names when editing
        if (data) {
            if (data.approvedBy?.name)         this.signatoryNames['approvingOfficer']            = data.approvedBy.name;
            if (data.reviewedAcceptedBy?.name) this.signatoryNames['reviewedAcceptedByAccountNo'] = data.reviewedAcceptedBy.name;
            if (data.inventoryCheckedBy?.name) this.signatoryNames['inventoryCheckedByAccountNo'] = data.inventoryCheckedBy.name;
            if (data.recAppBy?.name)           this.signatoryNames['recAppByAccountNo']           = data.recAppBy.name;
            if (data.bacRepresentative?.name)  this.signatoryNames['bacRepresentativeAccountNo']  = data.bacRepresentative.name;
            if (data.budgetOfficer?.name)      this.signatoryNames['budgetOfficerAccountNo']      = data.budgetOfficer.name;
        }

        // Restore budget line item display text when editing
        if (data?.budgetLineItemDetail) {
            this.selectedBudgetLineItemText = data.budgetLineItemDetail.code + ' – ' + data.budgetLineItemDetail.title;
        }

        // Restore budget sub items if editing
        if (data?.budgetLineItemDetail?.id && data?.budgetLineItemDetail?.hasSubItems) {
            this.showBudgetSubItem.set(true);
            this.service.getBudgetSubItems(data.budgetLineItemDetail.id).subscribe({
                next: (items) => this.budgetSubItems.set(items),
                error: () => {}
            });
        }

        // Voucher date is locked once the document is created
        if (this.editMode) {
            this.form.get('voucherDate')?.disable();
        }
    }

    /**
     * Applies or removes Validators.required on fields whose requirement
     * depends on the selected RV type — mirrors the legacy per-type validation rules.
     */
    syncDynamicValidators(): void {
        const type = this.selectedRvType;

        const setRequired = (name: string, required: boolean) => {
            const ctrl = this.form.get(name);
            if (!ctrl) return;
            if (required) {
                ctrl.setValidators(Validators.required);
            } else {
                ctrl.clearValidators();
            }
            ctrl.updateValueAndValidity({ emitEvent: false });
        };

        // Delivery date: required for all types except FOR_LAB
        setRequired('deliveryDate', type !== 4);

        // Budget line item: required whenever a type is selected
        setRequired('budgetLineItemDetailId', type !== null);

        // Reviewed/Accepted By: required for PO (1), REP (3), LAB (4)
        setRequired('reviewedAcceptedByAccountNo', [1, 3, 4].includes(type!));

        // Estimated Amount: required for PO (1), REP (3), and LAB (4)
        setRequired('estimatedAmount', [1, 3, 4].includes(type!));

        // FOR_LAB specific required fields — employee was not enforced in the old system
        setRequired('employeeAccountNo', false);
        setRequired('durationStart',     type === 4);
        setRequired('durationEnd',       type === 4);

        // FOR_IT specific required signatories
        setRequired('recAppByAccountNo',          type === 2);
        setRequired('bacRepresentativeAccountNo', type === 2);
        setRequired('budgetOfficerAccountNo',     type === 2);
    }

    /**
     * When items are added or removed, check if any belong to warehousing
     * categories. If so, Inventory Checked By becomes visible and required.
     */
    checkInventoryRequired(): void {
        const requiresInventory = this.lineItems.some(li => WAREHOUSE_CATEGORIES.includes(li.itemGroup));
        this.showInventoryChecked.set(requiresInventory);
        const ctrl = this.form.get('inventoryCheckedByAccountNo');
        if (!ctrl) return;
        if (requiresInventory) {
            ctrl.setValidators(Validators.required);
        } else {
            ctrl.clearValidators();
            ctrl.setValue(null);
        }
        ctrl.updateValueAndValidity({ emitEvent: false });
    }

    async openBudgetLineItemBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseBudgetLineItemModalComponent,
                { items: this.budgetLineItems() },
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.applyBudgetLineItem(result.data);
            }
        } catch { }
    }

    clearBudgetLineItem(): void {
        this.form.get('budgetLineItemDetailId')?.setValue(null);
        this.form.get('budgetSubItemId')?.setValue(null);
        this.selectedBudgetLineItemText = '';
        this.budgetSubItems.set([]);
        this.showBudgetSubItem.set(false);
        this.budgetBalance.set(null);
        this.budgetSubItemBalance.set(null);
    }

    private applyBudgetLineItem(item: any): void {
        this.form.get('budgetLineItemDetailId')?.setValue(item.id);
        this.form.get('budgetSubItemId')?.setValue(null);
        this.selectedBudgetLineItemText = item.code + ' – ' + item.title;
        this.budgetSubItems.set([]);
        this.showBudgetSubItem.set(false);
        this.budgetBalance.set(null);
        this.budgetSubItemBalance.set(null);
        if (item.hasSubItems) {
            this.showBudgetSubItem.set(true);
            this.service.getBudgetSubItems(item.id).subscribe({
                next: (items) => this.budgetSubItems.set(items),
                error: () => {}
            });
        }
        this.service.getBudgetBalance(item.id).subscribe({
            next: (bal) => this.budgetBalance.set(bal),
            error: () => {}
        });
    }

    onBudgetSubItemChange(): void {
        const id = this.form.get('budgetSubItemId')?.value;
        this.budgetSubItemBalance.set(null);
        if (!id) return;
        this.service.getBudgetSubItemBalance(id).subscribe({
            next: (bal) => this.budgetSubItemBalance.set(bal),
            error: () => {}
        });
    }

    onRvTypeChange(): void {
        this.syncDynamicValidators();
        // Reset all type-specific fields when the type changes
        this.form.patchValue({
            estimatedAmount:             null,
            emergencyPurchase:           false,
            costEstimateId:              null,
            workOrderId:                 null,
            budgetLineItemDetailId:      null,
            budgetSubItemId:             null,
            vehicleId:                   null,
            reviewedAcceptedByAccountNo: null,
            inventoryCheckedByAccountNo: null,
            officeId:                    null,
            rvItType:                    null,
            recAppByAccountNo:           null,
            bacRepresentativeAccountNo:  null,
            budgetOfficerAccountNo:      null,
            employeeAccountNo:           null,
            durationStart:               '',
            durationEnd:                 '',
        });
        this.budgetSubItems.set([]);
        this.showBudgetSubItem.set(false);
        this.showInventoryChecked.set(false);
        this.budgetBalance.set(null);
        this.budgetSubItemBalance.set(null);
        this.selectedBudgetLineItemText = '';
    }

    toDateInput(val: any): string {
        if (!val) return '';
        const d = new Date(val);
        return d.toISOString().substring(0, 10);
    }

    getData(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.initForm(data);
                    this.service.getDetails(this.id).subscribe({
                        next: (details) => {
                            this.lineItems = (details || []).map((d: any) => ({
                                // itemId=0 means no item linked — description stored in joDescription
                                itemId:          d.itemId    || 0,
                                itemCode:        d.itemId    ? (d.itemCode    || '') : '',
                                itemDescription: d.itemId    ? (d.itemDescription || '') : (d.joDescription || ''),
                                quantity:        d.quantity  || 1,
                                unitId:          d.unitId    || null,
                                joDescription:   d.itemId    ? (d.joDescription || '') : '',
                                itemGroup:       d.itemGroup || null,
                            }));
                            if (this.lineItems.length === 0) this.addLineItem();
                            this.checkInventoryRequired();
                        },
                        error: () => { this.addLineItem(); }
                    });
                } else {
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Error', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    addLineItem(): void {
        // For LAB: block adding a new row if the last row has no description
        if (this.isForLab() && this.lineItems.length > 0) {
            const last = this.lineItems[this.lineItems.length - 1];
            if (!last.joDescription?.trim()) {
                this.alertService.error(this.module, 'Validation', 'Please fill in the description of the current row before adding another.');
                return;
            }
        }
        this.lineItems.push({ itemId: 0, itemCode: '', itemDescription: '', quantity: 1, unitId: null, joDescription: '', itemGroup: null });
        this.checkInventoryRequired();
    }

    /**
     * Opens the item browse modal for PO/IT line items.
     * Prevents duplicate item selection.
     */
    async openItemBrowse(index: number): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseItemModalComponent,
                {},
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const item = result.data;
                const isDuplicate = this.lineItems.some((li, i) => i !== index && li.itemId > 0 && li.itemId === item.id);
                if (isDuplicate) {
                    this.alertService.error(this.module, 'Duplicate Item', 'This item has already been added.');
                    return;
                }
                this.lineItems[index] = {
                    ...this.lineItems[index],
                    itemId:          item.id,
                    itemCode:        item.code        || '',
                    itemDescription: item.description || '',
                    unitId:          item.unit?.id    || this.lineItems[index].unitId,
                    itemGroup:       item.inventoryCategory?.code || null,
                };
                this.checkInventoryRequired();
            }
        } catch {
            // modal dismissed — no action needed
        }
    }

    async openSignatoryBrowse(field: string): Promise<void> {
        // approvingOfficer must be a system user — use user browse so only valid accounts are selectable
        const modalComponent = field === 'approvingOfficer' ? BrowseUserModalComponent : BrowseEntityModalComponent;
        try {
            const result = await this.modalService.openModal(
                modalComponent,
                {},
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.form.get(field)?.setValue(result.data.accountNo);
                this.signatoryNames[field] = result.data.name;
            }
        } catch { }
    }

    removeLineItem(index: number): void {
        this.lineItems.splice(index, 1);
        this.checkInventoryRequired();
    }

    get form(): UntypedFormGroup { return this.validationForm; }

    get selectedRvType(): number | null { return this.form?.get('rvType')?.value ?? null; }

    isForPo():  boolean { return this.selectedRvType === 1; }
    isForIt():  boolean { return this.selectedRvType === 2; }
    isForRep(): boolean { return this.selectedRvType === 3; }
    isForLab(): boolean { return this.selectedRvType === 4; }

    validSubmit(): void {
        this.submit = true;
        this.formSubmit = true;
        if (this.validationForm.invalid) { this.formSubmit = false; return; }
        if (this.lineItems.length === 0) {
            this.alertService.error(this.module, 'Validation', 'At least one line item is required.');
            this.formSubmit = false;
            return;
        }

        // getRawValue() includes disabled controls (e.g. voucherDate locked in edit mode)
        const v = this.form.getRawValue();
        const payload: any = {
            id:               this.editMode ? this.id : null,
            rvType:           v.rvType,
            voucherDate:      v.voucherDate,
            deliveryDate:     v.deliveryDate   || null,
            purpose:          v.purpose,
            approvingOfficer: { accountNo: v.approvingOfficer },
            estimatedAmount:  v.estimatedAmount || null,
            emergencyPurchase: v.emergencyPurchase || false,
            rvItType:         v.rvItType        || null,
            durationStart:    v.durationStart   || null,
            durationEnd:      v.durationEnd     || null,
            costEstimate:         v.costEstimateId         ? { id: v.costEstimateId }         : null,
            workOrder:            v.workOrderId            ? { id: v.workOrderId }            : null,
            budgetLineItemDetail: v.budgetLineItemDetailId ? { id: v.budgetLineItemDetailId } : null,
            budgetSubItem:        v.budgetSubItemId        ? { id: v.budgetSubItemId }        : null,
            vehicle:              v.vehicleId             ? { id: v.vehicleId }             : null,
            office:               v.officeId              ? { id: v.officeId }              : null,
            reviewedAcceptedBy:   v.reviewedAcceptedByAccountNo ? { accountNo: v.reviewedAcceptedByAccountNo } : null,
            inventoryCheckedBy:   v.inventoryCheckedByAccountNo ? { accountNo: v.inventoryCheckedByAccountNo } : null,
            recAppBy:             v.recAppByAccountNo          ? { accountNo: v.recAppByAccountNo }           : null,
            bacRepresentative:    v.bacRepresentativeAccountNo  ? { accountNo: v.bacRepresentativeAccountNo }  : null,
            budgetOfficer:        v.budgetOfficerAccountNo      ? { accountNo: v.budgetOfficerAccountNo }      : null,
            employee:             v.employeeAccountNo           ? { accountNo: v.employeeAccountNo }           : null,
            rvDetails: this.lineItems.map(li => ({
                itemId:          li.itemId  || 0,
                itemDescription: li.itemDescription,
                unitId:          li.unitId,
                quantity:        li.quantity,
                joDescription:   li.joDescription || '',
            })),
        };

        const req = this.editMode ? this.service.update(payload) : this.service.create(payload);

        req.subscribe({
            next: (res) => {
                if (res.success) {
                    this.alertService.success(this.module, 'Saved', '');
                    this.router.navigate(['/' + this.menuLink, res.modelId, 'detail']);
                } else {
                    this.formSubmit = false;
                    this.alertService.error(this.module, 'Saving', res.failureMessage || (res.messages || []).join(', '));
                }
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'Saving', ''); }
        });
    }

    trackByIndex(index: number): number { return index; }
}
