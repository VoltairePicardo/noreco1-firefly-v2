import { ChangeDetectionStrategy, Component, computed, effect, inject, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { WithdrawalService } from '../withdrawal.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseWorkOrderModalComponent } from '@/app/shared/modals/browse-work-order-modal/browse-work-order-modal.component';
import { BrowseItemStockModalComponent } from '@/app/shared/modals/browse-item-stock-modal/browse-item-stock-modal.component';
import { BrowseRvCostEstimateModalComponent } from '@/app/shared/modals/browse-rv-cost-estimate-modal/browse-rv-cost-estimate-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerTrash, tablerPlus, tablerArrowLeft, tablerCheck } from '@ng-icons/tabler-icons';
import { ItemStock } from '@/app/models/inventory-modules/item-stock.model';
import {Purpose} from '@/app/models/dropdown.model';
import {InventoryCategory, InventoryLocation} from '@/app/models/shared/reference.model';

interface SlEntity {
    accountNo: string;
    name?: string;
    fullName?: string;
}

interface WorkOrder {
    id: number;
    code?: string;
    project?: { name?: string };
}

interface RvCostEstimateRef {
    type: 'rv' | 'ce';
    id: number;
    detailKey: number;
    code: string;
}

interface EmployeeRef {
    name: string;
    accountNo: string;
}

interface WithdrawalDetailRow {
    itemStockId: number | null;
    itemId: number | null;
    itemCode: string;
    itemDescription: string;
    unitCode: string;
    unitId: number | null;
    unitCost: number;
    quantity: number;
    quantityReleased: number;
    inventoryBalance: number;
    inventoryLocationId: number | null;
    specialEquipment: boolean;
}

interface WithdrawalRecord {
    id: number;
    voucherDate?: string;
    description?: string;
    approvingOfficer?: SlEntity;
    employees?: EmployeeRef[];
    details?: WithdrawalDetailRow[];
    inventoryLocation?: InventoryLocation;
    inventoryCategory?: InventoryCategory;
    purpose?: Purpose;
    workOrder?: WorkOrder;
    purchaseRequest?: { id: number; code?: string };
    costEstimate?: { id: number; code?: string; transaction?: { id: number } };
}

interface WithdrawalPayload {
    id?: number;
    voucherDate: string;
    description: string | null;
    approvingOfficer: { accountNo: string; fullName?: string };
    inventoryLocation: { id: number };
    inventoryCategory: { id: number };
    purpose: { id: number };
    type: number | null;
    employees: EmployeeRef[];
    details: WithdrawalDetailRow[];
    workOrder: { id: number } | null;
    purchaseRequest: { id: number } | null;
    costEstimate: { id: number } | null;
}

const OFFICE_EQUIPMENT_FIXTURES_AND_FURNITURE_CATEGORY_ID = 5;
const EMPLOYEE_CLASSIFICATION_ID = 1;
const OTHER_PURPOSE = 4;

type EmployeeForm = FormGroup<{
    name: FormControl<string>;
    accountNo: FormControl<string>;
}>;

type DetailForm = FormGroup<{
    itemStockId: FormControl<number | null>;
    itemId: FormControl<number | null>;
    itemCode: FormControl<string>;
    itemDescription: FormControl<string>;
    unitCode: FormControl<string>;
    unitId: FormControl<number | null>;
    unitCost: FormControl<number>;
    quantity: FormControl<number>;
    quantityReleased: FormControl<number>;
    inventoryBalance: FormControl<number>;
    inventoryLocationId: FormControl<number | null>;
    specialEquipment: FormControl<boolean>;
}>;

@Component({
    selector: 'app-withdrawal-add-edit',
    changeDetection: ChangeDetectionStrategy.OnPush,
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
export class WithdrawalAddEditComponent implements OnInit {
    readonly menuLink = 'withdrawal';
    readonly flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    module    = signal('Stock Withdrawal');
    subModule = signal('Create');

    id  = signal<number | null>(null);
    editMode  = signal(false);
    isLoading = signal(false);

    inventoryLocations  = signal<InventoryLocation[]>([]);
    inventoryCategories = signal<InventoryCategory[]>([]);
    purposes            = signal<Purpose[]>([]);

    form = new FormGroup({
        voucherDate:        new FormControl('', { nonNullable: true, validators: [Validators.required] }),
        description:        new FormControl('', { nonNullable: true }),
        inventoryLocation:  new FormControl<InventoryLocation | null>(null, Validators.required),
        inventoryCategory:  new FormControl<InventoryCategory | null>(null, Validators.required),
        purpose:            new FormControl<Purpose | null>(null, Validators.required),
        approvingOfficer:   new FormControl<SlEntity | null>(null, Validators.required),
        workOrder:          new FormControl<WorkOrder | null>(null),
        rvCostEstimate:     new FormControl<RvCostEstimateRef | null>(null),
        employees:          new FormArray<EmployeeForm>([]),
        details:            new FormArray<DetailForm>([]),
    });

    get employeesArray(): FormArray<EmployeeForm> {
        return this.form.controls.employees;
    }
    get detailsArray(): FormArray<DetailForm> {
        return this.form.controls.details;
    }

    // Use getRawValue() (not the raw valueChanges payload) so computed state below still sees
    // inventoryLocation/inventoryCategory after the effect() disables them once items are added —
    // FormGroup.value silently drops disabled controls, which otherwise flips showEmployeeSection()
    // to false (hiding the whole employee section) the next time any control emits.
    private formValue = toSignal(
        this.form.valueChanges.pipe(map(() => this.form.getRawValue())),
        { initialValue: this.form.getRawValue() }
    );

    showPurposeInput  = computed(() => this.formValue().purpose?.id === OTHER_PURPOSE);
    showEmployeeSection = computed(() => this.formValue().inventoryCategory?.id === OFFICE_EQUIPMENT_FIXTURES_AND_FURNITURE_CATEGORY_ID);
    employeesCount    = computed(() => this.formValue().employees?.length ?? 0);
    detailsCount      = computed(() => this.formValue().details?.length ?? 0);
    totalQuantity     = computed(() => (this.formValue().details ?? []).reduce((s, r) => s + (Number(r.quantity) || 0), 0));
    hasWorkOrder      = computed(() => !!this.formValue().workOrder);
    workOrderDesc     = computed(() => {
        const wo = this.formValue().workOrder;
        return wo ? (wo.code || '') + (wo.project?.name ? ' — ' + wo.project.name : '') : '';
    });
    hasRvCostEstimate = computed(() => !!this.formValue().rvCostEstimate);
    rvCostEstimateDesc = computed(() => {
        const ref = this.formValue().rvCostEstimate;
        if (!ref) return '';
        return (ref.type === 'rv' ? 'RV: ' : 'CE: ') + (ref.code || '');
    });
    approvingOfficerLabel = computed(() => {
        const officer = this.formValue().approvingOfficer;
        return officer ? (officer.fullName || officer.name || '') : '';
    });

    private service      = inject(WithdrawalService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    constructor() {
        effect(() => {
            const locked = this.detailsCount() > 0;
            const locationCtrl = this.form.controls.inventoryLocation;
            const categoryCtrl = this.form.controls.inventoryCategory;
            if (locked) {
                locationCtrl.disable({ emitEvent: false });
                categoryCtrl.disable({ emitEvent: false });
            } else {
                locationCtrl.enable({ emitEvent: false });
                categoryCtrl.enable({ emitEvent: false });
            }
        });
    }

    ngOnInit(): void {
        this.loadDropdowns();

        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            const isEdit = idParam != null && /^\d+$/.test(idParam);
            this.editMode.set(isEdit);
            if (isEdit) {
                const numericId = Number(idParam);
                this.id.set(numericId);
                this.subModule.set('Edit');
                this.loadForEdit(numericId);
            } else {
                this.subModule.set('Create');
                const today = new Date().toISOString().substring(0, 10);
                this.form.controls.voucherDate.setValue(today);
                this.loadDefaultSignatories();
            }
        });
    }

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
            next: (data: { approvedBy?: SlEntity; approvingOfficer?: SlEntity } | null) => {
                if (data) this.form.controls.approvingOfficer.setValue(data.approvedBy || data.approvingOfficer || null);
            },
            error: () => {}
        });
    }

    private loadForEdit(id: number): void {
        this.isLoading.set(true);
        this.service.getData(id).subscribe({
            next: (data: WithdrawalRecord) => {
                this.isLoading.set(false);
                if (!data?.id) {
                    this.alertService.error(this.module(), 'Record not found.', '');
                    this.router.navigate(['/' + this.menuLink]);
                    return;
                }
                const toYmd = (v: unknown) => v ? new Date(v as string).toISOString().substring(0, 10) : '';

                this.detailsArray.clear();
                (data.details || []).forEach(d => this.detailsArray.push(this.createDetailGroup(d)));

                this.employeesArray.clear();
                (data.employees || []).forEach(e => this.employeesArray.push(this.createEmployeeGroup(e)));

                const rvCostEstimate: RvCostEstimateRef | null = data.purchaseRequest?.id
                    ? { type: 'rv', id: data.purchaseRequest.id, detailKey: data.purchaseRequest.id, code: data.purchaseRequest.code || '' }
                    : data.costEstimate?.id
                        ? { type: 'ce', id: data.costEstimate.id, detailKey: data.costEstimate.transaction?.id ?? data.costEstimate.id, code: data.costEstimate.code || '' }
                        : null;

                this.form.patchValue({
                    voucherDate:      toYmd(data.voucherDate),
                    description:      data.description || '',
                    approvingOfficer: data.approvingOfficer || null,
                    workOrder:        data.workOrder?.id ? data.workOrder : null,
                    rvCostEstimate,
                });

                const tryMatch = () => {
                    if (data.inventoryLocation?.id) {
                        const match = this.inventoryLocations().find(l => l.id === data.inventoryLocation!.id) ?? data.inventoryLocation;
                        this.form.controls.inventoryLocation.setValue(match);
                    }
                    if (data.inventoryCategory?.id) {
                        const match = this.inventoryCategories().find(c => c.id === data.inventoryCategory!.id) ?? data.inventoryCategory;
                        this.form.controls.inventoryCategory.setValue(match);
                    }
                    if (data.purpose?.id) {
                        const match = this.purposes().find(p => p.id === data.purpose!.id) ?? data.purpose;
                        this.form.controls.purpose.setValue(match);
                    }
                };
                tryMatch();
                setTimeout(tryMatch, 400);
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module(), 'Failed to load record.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    private createEmployeeGroup(row: Partial<EmployeeRef> = {}): EmployeeForm {
        return new FormGroup({
            name:      new FormControl(row.name ?? '', { nonNullable: true }),
            accountNo: new FormControl(row.accountNo ?? '', { nonNullable: true }),
        });
    }

    private createDetailGroup(row: Partial<WithdrawalDetailRow> = {}): DetailForm {
        return new FormGroup({
            itemStockId:         new FormControl(row.itemStockId ?? null),
            itemId:              new FormControl(row.itemId ?? null),
            itemCode:            new FormControl(row.itemCode ?? '', { nonNullable: true }),
            itemDescription:     new FormControl(row.itemDescription ?? '', { nonNullable: true }),
            unitCode:            new FormControl(row.unitCode ?? '', { nonNullable: true }),
            unitId:              new FormControl(row.unitId ?? null),
            unitCost:            new FormControl(row.unitCost ?? 0, { nonNullable: true }),
            quantity:            new FormControl(row.quantity ?? 0, { nonNullable: true }),
            quantityReleased:    new FormControl(row.quantityReleased ?? 0, { nonNullable: true }),
            inventoryBalance:    new FormControl(row.inventoryBalance ?? 0, { nonNullable: true }),
            inventoryLocationId: new FormControl(row.inventoryLocationId ?? null),
            specialEquipment:    new FormControl(row.specialEquipment ?? false, { nonNullable: true }),
        });
    }

    onCategoryChange(): void {
        if (!this.showEmployeeSection()) {
            this.employeesArray.clear();
        }
    }

    async openApprovingOfficerBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, { defaultClassificationId: EMPLOYEE_CLASSIFICATION_ID }, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.form.controls.approvingOfficer.setValue(result.data as SlEntity);
            }
        } catch { }
    }

    async openWorkOrderBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseWorkOrderModalComponent, {}, { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const wo = result.data as WorkOrder;
                this.form.controls.workOrder.setValue(wo);
                const location = this.form.controls.inventoryLocation.value;
                const category = this.form.controls.inventoryCategory.value;
                if (location?.id && category?.id) {
                    this.service.getWorkOrderDetails(wo.id, location.id, category.id).subscribe({
                        next: (items: Partial<WithdrawalDetailRow>[]) => {
                            this.detailsArray.clear();
                            (items || []).forEach(item => this.detailsArray.push(this.createDetailGroup(item)));
                        },
                        error: () => this.alertService.error(this.module(), 'Failed to load Work Order items.', '')
                    });
                }
            }
        } catch { }
    }

    clearWorkOrder(): void {
        this.form.controls.workOrder.setValue(null);
        this.detailsArray.clear();
    }

    async openRvCostEstimateBrowse(): Promise<void> {
        const location = this.form.controls.inventoryLocation.value;
        const category = this.form.controls.inventoryCategory.value;
        if (!location?.id || !category?.id) {
            this.alertService.warning(this.module(), 'Validation', 'Please select Inventory Location and Category first.');
            return;
        }
        try {
            const result = await this.modalService.openModal(
                BrowseRvCostEstimateModalComponent, { locationId: location.id }, { size: 'lg', centered: true }
            );
            if (result?.action !== 'select' || !result?.data) return;

            const { type, item } = result.data as { type: 'rv' | 'ce'; item: any };
            const ref: RvCostEstimateRef = type === 'rv'
                ? { type, id: item.id, detailKey: item.id, code: item.code }
                : { type, id: item.id, detailKey: item.transaction?.id, code: item.code };

            if (!ref.detailKey) {
                this.alertService.error(this.module(), 'Selected document has no linked items.', '');
                return;
            }

            const details$ = type === 'rv'
                ? this.service.getRVDetailsForWithdrawal(ref.detailKey, location.id, category.id)
                : this.service.getCostEstimateDetails(ref.detailKey);

            details$.subscribe({
                next: (rows: any[]) => {
                    this.form.controls.rvCostEstimate.setValue(ref);
                    this.detailsArray.clear();
                    (rows || []).forEach(row => this.detailsArray.push(this.createDetailGroup(
                        type === 'rv' ? row : {
                            itemId:              row.itemId,
                            itemCode:            row.itemCode,
                            itemDescription:     row.itemDescription,
                            unitCode:            row.unitCode,
                            unitId:              row.unitId,
                            unitCost:            Number(row.unitCost) || 0,
                            quantity:            Number(row.quantity) || 0,
                            quantityReleased:    0,
                            inventoryBalance:    Number(row.quantity) || 0,
                            inventoryLocationId: location.id,
                            specialEquipment:    false
                        }
                    )));
                },
                error: () => this.alertService.error(this.module(), `Failed to load ${type === 'rv' ? 'RV' : 'Cost Estimate'} items.`, '')
            });
        } catch { }
    }

    clearRvCostEstimate(): void {
        this.form.controls.rvCostEstimate.setValue(null);
        this.detailsArray.clear();
    }

    async openItemStockBrowse(): Promise<void> {
        const location = this.form.controls.inventoryLocation.value;
        const category = this.form.controls.inventoryCategory.value;
        if (!location?.id || !category?.id) {
            this.alertService.warning(this.module(), 'Validation', 'Please select Inventory Location and Category first.');
            return;
        }
        try {
            const result = await this.modalService.openModal(
                BrowseItemStockModalComponent,
                { locationId: location.id, categoryId: category.id },
                { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const stock = result.data as ItemStock;
                const isDuplicate = this.detailsArray.controls.some(c => c.controls.itemStockId.value === stock.id);
                if (isDuplicate) {
                    this.alertService.warning(this.module(), 'Validation', 'Duplicate item.');
                    return;
                }
                this.detailsArray.push(this.createDetailGroup({
                    itemStockId:         stock.id,
                    itemId:              stock.item?.id ?? null,
                    itemCode:            stock.item?.code ?? '',
                    itemDescription:     stock.item?.description ?? '',
                    unitCode:            stock.item?.unit?.code ?? '',
                    unitId:              stock.item?.unit?.id ?? null,
                    unitCost:            stock.unitCost ?? 0,
                    quantity:            0,
                    quantityReleased:    0,
                    inventoryBalance:    stock.balance ?? stock.totalQuantity ?? 0,
                    inventoryLocationId: stock.inventoryLocation?.id ?? location.id,
                    specialEquipment:    false
                }));
            }
        } catch { }
    }

    async openEmployeeBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, { defaultClassificationId: EMPLOYEE_CLASSIFICATION_ID }, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const emp = result.data as SlEntity;
                const isDuplicate = this.employeesArray.controls.some(c => c.controls.accountNo.value === emp.accountNo);
                if (isDuplicate) {
                    this.alertService.warning(this.module(), 'Validation', 'Duplicate employee.');
                    return;
                }
                this.employeesArray.push(this.createEmployeeGroup({ name: emp.name || emp.fullName || '', accountNo: emp.accountNo }));
            }
        } catch { }
    }

    removeEmployee(index: number): void {
        this.employeesArray.removeAt(index);
    }

    onQuantityChange(index: number): void {
        const row = this.detailsArray.at(index);
        if (!row) return;
        const qty = Number(row.controls.quantity.value) || 0;
        const bal = Number(row.controls.inventoryBalance.value) || 0;
        if (qty > bal) {
            row.controls.quantity.setValue(bal);
            this.alertService.warning(this.module(), 'Validation',
                `Quantity cannot exceed inventory balance (${bal}).`);
        }
    }

    removeRow(index: number): void {
        this.detailsArray.removeAt(index);
    }

    compareById(a: { id?: unknown } | null, b: { id?: unknown } | null): boolean {
        return a && b ? a.id === b.id : a === b;
    }

    save(): void {
        const value = this.form.getRawValue();

        if (!value.voucherDate) {
            this.alertService.warning(this.module(), 'Validation', 'Voucher Date is required.');
            return;
        }
        if (!value.inventoryLocation?.id) {
            this.alertService.warning(this.module(), 'Validation', 'Inventory Location is required.');
            return;
        }
        if (!value.inventoryCategory?.id) {
            this.alertService.warning(this.module(), 'Validation', 'Inventory Category is required.');
            return;
        }
        if (!value.purpose?.id) {
            this.alertService.warning(this.module(), 'Validation', 'Purpose is required.');
            return;
        }
        if (this.showPurposeInput() && !value.description.trim()) {
            this.alertService.warning(this.module(), 'Validation', 'Please describe the purpose (Others).');
            return;
        }
        if (!value.approvingOfficer?.accountNo) {
            this.alertService.warning(this.module(), 'Validation', 'Approving Officer is required.');
            return;
        }
        if (value.details.length === 0) {
            this.alertService.warning(this.module(), 'Validation', 'Please add at least one item.');
            return;
        }
        const hasQty = value.details.some(d => (Number(d.quantity) || 0) > 0);
        if (!hasQty) {
            this.alertService.warning(this.module(), 'Validation', 'Total quantity is zero — enter quantities for items.');
            return;
        }
        const missingUnit = value.details.find(d => (Number(d.quantity) || 0) > 0 && !d.unitId);
        if (missingUnit) {
            this.alertService.warning(this.module(), 'Validation',
                `"${missingUnit.itemDescription || missingUnit.itemCode || 'Item'}" has no unit of measure. Please remove it or select a valid item.`);
            return;
        }

        this.isLoading.set(true);

        const payload: WithdrawalPayload = {
            voucherDate:      value.voucherDate,
            description:      value.description.trim() || null,
            approvingOfficer: { accountNo: value.approvingOfficer.accountNo, fullName: value.approvingOfficer.fullName || value.approvingOfficer.name },
            inventoryLocation: { id: value.inventoryLocation.id },
            inventoryCategory: { id: value.inventoryCategory.id },
            purpose:           { id: value.purpose.id },
            type:              value.inventoryCategory.type || null,
            employees:         value.employees,
            details:           value.details.map(d => ({
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
                inventoryLocationId: d.inventoryLocationId ?? value.inventoryLocation!.id,
                specialEquipment:    d.specialEquipment   || false
            })),
            workOrder:      value.workOrder ? { id: value.workOrder.id } : null,
            purchaseRequest: value.rvCostEstimate?.type === 'rv' ? { id: value.rvCostEstimate.id } : null,
            costEstimate:    value.rvCostEstimate?.type === 'ce' ? { id: value.rvCostEstimate.id } : null,
        };

        if (this.editMode()) payload.id = this.id() ?? undefined;

        const request$ = this.editMode() ? this.service.update(payload) : this.service.create(payload);

        request$.subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.success === false) {
                    this.alertService.error(this.module(), 'Save', data.failureMessage || '');
                } else {
                    this.alertService.success(
                        this.module(),
                        this.editMode() ? 'Updated successfully.' : 'Created successfully.',
                        ''
                    );
                    const id = data?.modelId ?? data?.id ?? this.id();
                    this.router.navigate(['/' + this.menuLink, id, 'detail']);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module(), 'Save', 'An error occurred.');
            }
        });
    }
}
