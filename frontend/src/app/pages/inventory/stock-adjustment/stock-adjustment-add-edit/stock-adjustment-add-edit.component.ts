import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { StockAdjustmentService } from '../stock-adjustment.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseItemStockModalComponent } from '@/app/shared/modals/browse-item-stock-modal/browse-item-stock-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerTrash, tablerArrowLeft, tablerCheck, tablerPlus } from '@ng-icons/tabler-icons';
import { InventoryLocationService } from '@/app/pages/inventory-location/inventory-location.service';
import { InventoryLocation } from '@/app/models/shared/reference.model';
import { ItemTransactionDetailDto } from '@/app/models/inventory-modules/stock-release.model';
import { ItemStock } from '@/app/models/inventory-modules/item-stock.model';
import { StockAdjustment, StockAdjustmentDefaultSignatories, SignatoryRef } from '@/app/models/inventory-modules/stock-adjustment.model';
import { SelectOnFocusDirective } from '@/app/core/directive/select-on-focus.directive';

const EMPLOYEE_CLASSIFICATION_ID = 1;

@Component({
    selector: 'app-stock-adjustment-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective,
        RouterLink,
        SelectOnFocusDirective
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

    id: number | null = null;
    editMode   = false;
    submit     = false;
    formSubmit = false;
    isLoading  = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    inventoryLocations = signal<InventoryLocation[]>([]);
    details            = signal<ItemTransactionDetailDto[]>([]);
    signatoryNames: { [key: string]: string } = {};

    validationForm!: UntypedFormGroup;

    private service      = inject(StockAdjustmentService);
    private locationService      = inject(InventoryLocationService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);
    private fb           = inject(FormBuilder);

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
                this.initForm();
                this.loadDefaultSignatories();
            }
        });
    }

    private today(): string {
        return new Date().toISOString().substring(0, 10);
    }

    toDateInput(val: any): string {
        if (!val) return '';
        return new Date(val).toISOString().substring(0, 10);
    }

    get form(): UntypedFormGroup { return this.validationForm; }

    initForm(data?: StockAdjustment): void {
        this.validationForm = this.fb.group({
            voucherDate:         [data?.voucherDate ? this.toDateInput(data.voucherDate) : this.today(), Validators.required],
            inventoryLocationId: [data?.inventoryLocation?.id || null, Validators.required],
            remarks:             [data?.remarks || '', Validators.required],
            checkerAccountNo:    [data?.checker?.accountNo          || null, Validators.required],
            approvedByAccountNo: [data?.approvingOfficer?.accountNo || null, Validators.required],
        });

        this.signatoryNames = {};
        if (data?.checker?.fullName)          this.signatoryNames['checkerAccountNo']    = data.checker.fullName;
        if (data?.approvingOfficer?.fullName)  this.signatoryNames['approvedByAccountNo'] = data.approvingOfficer.fullName;
    }

    private loadInventoryLocations(): void {
        this.locationService.getAllLocations().subscribe({
            next: (d) => this.inventoryLocations.set(d || []),
            error: () => {}
        });
    }

    private loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (data: StockAdjustmentDefaultSignatories) => {
                if (!data) return;
                const patch: { checkerAccountNo?: number; approvedByAccountNo?: number } = {};
                if (data.checker?.accountNo)    { patch.checkerAccountNo    = data.checker.accountNo;    this.signatoryNames['checkerAccountNo']    = data.checker.fullName    || data.checker.name || ''; }
                if (data.approvedBy?.accountNo) { patch.approvedByAccountNo = data.approvedBy.accountNo; this.signatoryNames['approvedByAccountNo'] = data.approvedBy.fullName || data.approvedBy.name || ''; }
                this.form.patchValue(patch);
            },
            error: () => {}
        });
    }

    loadForEdit(): void {
        this.isLoading.set(true);

        this.service.getData(this.id!).subscribe({
            next: (data: StockAdjustment) => {
                this.isLoading.set(false);
                if (!data?.id) {
                    this.alertService.error(this.module, 'Record not found.', '');
                    this.router.navigate(['/' + this.menuLink]);
                    return;
                }
                this.initForm(data);
                this.details.set((data.details || []).map(d => ({ ...d })));
                this.syncLocationLock();
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load record.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    private syncLocationLock(): void {
        const ctrl = this.form.get('inventoryLocationId');
        if (!ctrl) return;
        if (this.details().length > 0) ctrl.disable({ emitEvent: false });
        else ctrl.enable({ emitEvent: false });
    }

    async openItemStockBrowse(): Promise<void> {
        const locationId = this.form.get('inventoryLocationId')?.value;
        if (!locationId) {
            this.alertService.warning(this.module, 'Validation', 'Please select an Inventory Location first.');
            return;
        }
        try {
            const result = await this.modalService.openModal(
                BrowseItemStockModalComponent,
                { locationId, type: 'INV_LOCATION' },
                { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.onItemStockSelected(result.data as ItemStock);
            }
        } catch { }
    }

    private onItemStockSelected(itemStock: ItemStock): void {
        const itemStockId = itemStock.id;
        const isDuplicate = this.details().some(d => d.itemStockId === itemStockId);
        if (isDuplicate) {
            this.alertService.error(this.module, 'Duplicate Item', 'This item is already in the list.');
            return;
        }
        const locationId = this.form.get('inventoryLocationId')?.value;
        this.details.update(list => [...list, {
            itemStockId:         itemStockId,
            itemId:              itemStock.item?.id            ?? undefined,
            itemCode:            itemStock.item?.code          ?? '',
            unitCode:            itemStock.item?.unit?.code    ?? '',
            unitCost:            Number(itemStock.unitCost)    || 0,
            itemDescription:     itemStock.item?.description   ?? '',
            quantity:            Number(itemStock.totalQuantity ?? itemStock.quantity) || 0,
            inventoryLocationId: itemStock.inventoryLocation?.id ?? locationId ?? undefined,
            adjustment:          0
        }]);
        this.syncLocationLock();
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
        this.syncLocationLock();
    }

    async openCheckerBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, { defaultClassificationId: EMPLOYEE_CLASSIFICATION_ID }, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const e = result.data as SignatoryRef;
                this.form.get('checkerAccountNo')?.setValue(e.accountNo);
                this.signatoryNames['checkerAccountNo'] = e.fullName || e.name || '';
            }
        } catch { }
    }

    async openApprovedByBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, { defaultClassificationId: EMPLOYEE_CLASSIFICATION_ID }, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const e = result.data as SignatoryRef;
                this.form.get('approvedByAccountNo')?.setValue(e.accountNo);
                this.signatoryNames['approvedByAccountNo'] = e.fullName || e.name || '';
            }
        } catch { }
    }

    save(): void {
        this.submit = true;
        if (this.form.invalid) return;
        if (this.details().length === 0) {
            this.alertService.warning(this.module, 'Validation', 'No items selected. Please add at least one item.');
            return;
        }

        this.formSubmit = true;

        const v = this.form.getRawValue();
        const payload: any = {
            voucherDate:       v.voucherDate,
            remarks:           v.remarks.trim(),
            inventoryLocation: { id: v.inventoryLocationId },
            checker:           { accountNo: v.checkerAccountNo,    fullName: this.signatoryNames['checkerAccountNo'] },
            approvingOfficer:  { accountNo: v.approvedByAccountNo, fullName: this.signatoryNames['approvedByAccountNo'] },
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
                this.formSubmit = false;
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
                this.formSubmit = false;
                this.alertService.error(this.module, 'Save', 'An error occurred.');
            }
        });
    }
}
