import { Component, ElementRef, inject, signal, ViewChild } from '@angular/core';
import { FormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { LaddaModule } from 'angular2-ladda';
import { ItemService } from '../item.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseCOAModalComponent } from '@/app/shared/modals/browse-coa-modal/browse-coa-modal.component';
import { BrowseItemModalComponent } from '@/app/shared/modals/browse-item-modal/browse-item-modal.component';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { WorkflowService } from '@/app/shared/workflow/workflow.service';
import { WorkflowActionOption, buildProcessPayload } from '@/app/models/shared/workflow.model';
import Swal from 'sweetalert2';
@Component({
    selector: 'app-item-add-edit',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, LaddaModule],
    templateUrl: './item-add-edit.component.html'
})
export class ItemAddEditComponent {
    module     = 'Item';
    subModule  = 'Create';
    menuLink   = 'item';
    id: any    = 0;
    editMode   = false;
    submit     = false;
    formSubmit = false;
    isLoading  = signal(false);
    validationForm!: UntypedFormGroup;

    units      = signal<any[]>([]);
    categories = signal<any[]>([]);
    subCategories = signal<any[]>([]);
    brands     = signal<any[]>([]);

    isInventoryOfficer  = signal(false);
    duplicates          = signal<any[]>([]);
    currentItem         = signal<any>(null);
    approveAction       = signal<WorkflowActionOption | null>(null);
    descriptionDisplay  = signal('');

    @ViewChild('descInput') private descInput?: ElementRef<HTMLInputElement>;

    private dupSearch$ = new Subject<string>();

    selectedParentItem:     any = null;
    selectedAssetAccount:   any = null;
    selectedExpenseAccount: any = null;
    selectedImageFile:      File | null = null;
    imagePreview:           string | null = null;

    private service          = inject(ItemService);
    private modalService     = inject(ModalService);
    private workflowService  = inject(WorkflowService);
    public  fb           = inject(FormBuilder);
    public  route        = inject(ActivatedRoute);
    public  router       = inject(Router);
    public  alertService = inject(AlertService);

    ngOnInit(): void {
        this.service.isInventoryOfficer().subscribe({ next: v => this.isInventoryOfficer.set(v), error: () => {} });
        this.service.listUnits().subscribe({ next: d => this.units.set(d), error: () => {} });
        this.service.getCategories().subscribe({ next: d => this.categories.set(d), error: () => {} });
        this.service.getBrands().subscribe({ next: d => { this.brands.set(d); this.refreshDescription(); }, error: () => {} });

        this.dupSearch$.pipe(
            debounceTime(400),
            distinctUntilChanged(),
            switchMap(q => q.trim().length > 1
                ? this.service.list(q, null, 0, 5, null, this.editMode ? +this.id : undefined)
                : [{ content: [] }])
        ).subscribe({ next: res => this.duplicates.set(res.content ?? []), error: () => {} });

        this.route.paramMap.subscribe(params => {
            this.editMode = params.get('id') != null && /^\d+$/.test(params.get('id') ?? '');
            if (this.editMode) {
                this.id = params.get('id');
                this.subModule = 'Edit';
                this.getData();
            } else {
                this.subModule = 'Create';
                this.initForm();
            }
        });
    }

    initForm(data?: any): void {
        if (data?.parentItem) {
            this.selectedParentItem = data.parentItem;
        }
        if (data?.assetAccount) {
            this.selectedAssetAccount = {
                id: data.assetAccount.id,
                accountCode:  data.assetAccount.code,
                accountTitle: data.assetAccount.title
            };
        }
        if (data?.expenseAccount) {
            this.selectedExpenseAccount = {
                id: data.expenseAccount.id,
                accountCode:  data.expenseAccount.code,
                accountTitle: data.expenseAccount.title
            };
        }

        this.descriptionDisplay.set(data?.description || '');

        this.validationForm = this.fb.group({
            code:                [data?.code                  || ''],
            description:         [data?.description           || '', Validators.required],
            unitId:              [data?.unit?.id              || null, Validators.required],
            reorderPoint:        [data?.reorderPoint          ?? null],
            idealQty:            [data?.idealQty              ?? null],
            location:            [data?.location              || ''],
            isActive:            [data?.isActive              ?? true],
            inventoryCategoryId: [data?.inventoryCategory?.id || null],
            hasSerialNumbers:    [data?.hasSerialNumbers      ?? false],
            barcode:             [data?.barcode               || ''],
            subCategoryId:       [data?.subCategory?.id       || null],
            genericName:         [data?.genericName           || ''],
            size:                [data?.size                  || ''],
            rating:              [data?.rating                || ''],
            specification:       [data?.specification         || ''],
            brandId:             [data?.brand?.id             || null],
            manufacturer:        [data?.manufacturer          || ''],
            partNumber:          [data?.partNumber            || ''],
            remarks:             [data?.remarks               || ''],
        });

        this.loadSubCategories(this.validationForm.get('inventoryCategoryId')?.value);
        this.validationForm.get('inventoryCategoryId')?.valueChanges.subscribe(catId => {
            this.validationForm.get('subCategoryId')?.setValue(null);
            this.loadSubCategories(catId);
        });
        ['genericName', 'size', 'rating', 'specification', 'brandId'].forEach(name =>
            this.validationForm.get(name)?.valueChanges.subscribe(val =>
                this.refreshDescription({ [name]: val })));

        if (data?.description) {
            this.dupSearch$.next(data.description);
        }
    }

    private loadSubCategories(categoryId: number | null): void {
        if (!categoryId) { this.subCategories.set([]); return; }
        this.service.getCategoryWithSubCategories(categoryId).subscribe({
            next: c => this.subCategories.set(c?.subCategories ?? []),
            error: () => this.subCategories.set([])
        });
    }

    // Mirrors the backend rule: parent name (or generic name), size, rating, specification, brand name.
    // The backend rebuilds it on save; this is only a live preview. Left as-is when no part is filled in.
    // override: pass the just-emitted valueChanges value so we don't read a stale f.value snapshot.
    private refreshDescription(override: Record<string, any> = {}): void {
        const f = this.validationForm;
        if (!f) return;
        const v = { ...f.value, ...override };
        const brand = this.brands().find(b => b.id === v.brandId);
        const text = [
            this.selectedParentItem ? this.selectedParentItem.description : v.genericName,
            v.size, v.rating, v.specification, brand?.name
        ].map(p => (p ?? '').toString().trim()).filter(p => p).join(', ');
        if (text) {
            this.descriptionDisplay.set(text);
            if (this.descInput?.nativeElement) {
                this.descInput.nativeElement.value = text; // direct DOM — no CD scheduling wait
            }
            f.get('description')?.setValue(text, { emitEvent: false });
            this.dupSearch$.next(text);
        }
    }

    getData(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next:  (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.currentItem.set(data);
                    this.initForm(data);
                    if (data.transaction?.id) {
                        this.workflowService.getAvailableActionsForTransaction(data.transaction.id).subscribe({
                            next: actions => {
                                const approve = (actions ?? []).find(a => (a.action ?? '').toLowerCase().includes('approve'));
                                this.approveAction.set(approve ?? null);
                            }
                        });
                    }
                } else {
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Error', ''); this.router.navigate(['/' + this.menuLink]); }
        });
    }

    get form(): UntypedFormGroup { return this.validationForm; }

    get showFullForm(): boolean {
        if (!this.editMode) return false;
        const statusId = this.currentItem()?.documentStatus?.id;
        return statusId === 5 || statusId === 7;
    }

    onDuplicateSearch(value: string): void {
        this.dupSearch$.next(value);
    }

    async openParentItemBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseItemModalComponent,
                { excludeId: this.editMode ? +this.id : undefined },
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.selectedParentItem = result.data;
                this.refreshDescription();
            }
        } catch { }
    }

    clearParentItem(): void { this.selectedParentItem = null; this.refreshDescription(); }

    async openAssetAccountBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseCOAModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this.selectedAssetAccount = result.data;
            }
        } catch { }
    }

    clearAssetAccount(): void { this.selectedAssetAccount = null; }

    async openExpenseAccountBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseCOAModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this.selectedExpenseAccount = result.data;
            }
        } catch { }
    }

    clearExpenseAccount(): void { this.selectedExpenseAccount = null; }

    onImageChange(event: Event): void {
        const input = event.target as HTMLInputElement;
        const file = input.files?.[0] ?? null;
        this.selectedImageFile = file;
        if (file) {
            const reader = new FileReader();
            reader.onload = () => { this.imagePreview = reader.result as string; };
            reader.readAsDataURL(file);
        } else {
            this.imagePreview = null;
        }
    }

    private buildPayload(): any {
        const v = this.form.value;
        return {
            id:                this.id || 0,
            code:              v.code,
            description:       v.description,
            unit:              v.unitId              ? { id: v.unitId }              : null,
            reorderPoint:      v.reorderPoint,
            idealQty:          v.idealQty,
            location:          v.location,
            isActive:          v.isActive,
            parentItem:        this.selectedParentItem     ? { id: this.selectedParentItem.id }     : null,
            assetAccount:      this.selectedAssetAccount   ? { id: this.selectedAssetAccount.id }   : null,
            expenseAccount:    this.selectedExpenseAccount ? { id: this.selectedExpenseAccount.id } : null,
            inventoryCategory: v.inventoryCategoryId ? { id: v.inventoryCategoryId } : null,
            hasSerialNumbers:  v.hasSerialNumbers,
            barcode:           v.barcode,
            subCategory:       v.subCategoryId ? { id: v.subCategoryId } : null,
            genericName:       v.genericName,
            size:              v.size,
            rating:            v.rating,
            specification:     v.specification,
            brand:             v.brandId ? { id: v.brandId } : null,
            manufacturer:      v.manufacturer,
            partNumber:        v.partNumber,
            remarks:           v.remarks,
        };
    }

    validSubmit(): void {
        this.submit = true;
        this.formSubmit = true;
        if (this.validationForm.invalid) { this.formSubmit = false; return; }

        const frm = this.buildPayload();
        const req = this.editMode ? this.service.update(frm) : this.service.create(frm);

        req.subscribe({
            next: (res) => {
                if (res.success) {
                    const navigateToDetail = () => {
                        this.alertService.success(this.module, 'Saved', '');
                        this.router.navigate(['/' + this.menuLink, res.modelId, 'detail']);
                    };
                    if (this.selectedImageFile) {
                        this.service.uploadImage(this.selectedImageFile, res.modelId).subscribe({
                            next: () => navigateToDetail(),
                            error: () => navigateToDetail()
                        });
                    } else {
                        navigateToDetail();
                    }
                } else {
                    this.formSubmit = false;
                    Swal.fire({ title: 'Error', text: res.failureMessage || 'Failed to save item.', icon: 'error' });
                }
            },
            error: () => { this.formSubmit = false; Swal.fire({ title: 'Error', text: 'Failed to save item.', icon: 'error' }); }
        });
    }

    saveAndApprove(): void {
        this.submit = true;
        if (this.validationForm.invalid) return;
        if (!this.validationForm.get('unitId')?.value) {
            Swal.fire({ title: 'Required', text: 'Unit is required for approval.', icon: 'warning' });
            return;
        }
        if (!this.selectedAssetAccount) {
            Swal.fire({ title: 'Required', text: 'Asset Account is required for approval.', icon: 'warning' });
            return;
        }
        if (!this.selectedExpenseAccount) {
            Swal.fire({ title: 'Required', text: 'Expense Account is required for approval.', icon: 'warning' });
            return;
        }
        const action = this.approveAction();
        if (!action) return;
        this.formSubmit = true;
        this.service.update(this.buildPayload()).subscribe({
            next: (res) => {
                if (!res.success) {
                    this.formSubmit = false;
                    Swal.fire({ title: 'Error', text: res.failureMessage || 'Failed to save item.', icon: 'error' });
                    return;
                }
                this.service.process(buildProcessPayload(res.modelId, action.actionMapId, '')).subscribe({
                    next: (pRes) => {
                        this.formSubmit = false;
                        if (pRes.success) {
                            Swal.fire({ title: 'Approved', text: 'Item saved and approved.', icon: 'success', timer: 2000, showConfirmButton: false });
                            this.router.navigate(['/' + this.menuLink, res.modelId, 'detail']);
                        } else {
                            Swal.fire({ title: 'Error', text: pRes.failureMessage || 'Failed to approve.', icon: 'error' });
                        }
                    },
                    error: () => { this.formSubmit = false; Swal.fire({ title: 'Error', text: 'Failed to approve.', icon: 'error' }); }
                });
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'Saving', ''); }
        });
    }
}
