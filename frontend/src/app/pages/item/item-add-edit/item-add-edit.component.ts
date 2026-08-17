import { Component, inject, signal } from '@angular/core';
import { FormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { LaddaModule } from 'angular2-ladda';
import { ItemService } from '../item.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseCOAModalComponent } from '@/app/shared/modals/browse-coa-modal/browse-coa-modal.component';

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

    selectedAssetAccount:   any = null;
    selectedExpenseAccount: any = null;
    selectedImageFile:      File | null = null;
    imagePreview:           string | null = null;

    private service      = inject(ItemService);
    private modalService = inject(ModalService);
    public  fb           = inject(FormBuilder);
    public  route        = inject(ActivatedRoute);
    public  router       = inject(Router);
    public  alertService = inject(AlertService);

    ngOnInit(): void {
        this.service.listUnits().subscribe({ next: d => this.units.set(d), error: () => {} });
        this.service.listCategories().subscribe({ next: d => this.categories.set(d), error: () => {} });

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

        this.validationForm = this.fb.group({
            code:                [data?.code                  || ''],
            description:         [data?.description           || '', Validators.required],
            unitId:              [data?.unit?.id              || null],
            reorderPoint:        [data?.reorderPoint          ?? null],
            idealQty:            [data?.idealQty              ?? null],
            location:            [data?.location              || ''],
            isActive:            [data?.isActive              ?? true],
            inventoryCategoryId: [data?.inventoryCategory?.id || null],
            hasSerialNumbers:    [data?.hasSerialNumbers      ?? false],
            barcode:             [data?.barcode               || ''],
        });
    }

    getData(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next:  (data) => {
                this.isLoading.set(false);
                if (data?.id) { this.initForm(data); }
                else { this.alertService.error(this.module, 'Not Found', ''); this.router.navigate(['/' + this.menuLink]); }
            },
            error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Error', ''); this.router.navigate(['/' + this.menuLink]); }
        });
    }

    get form(): UntypedFormGroup { return this.validationForm; }

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

    validSubmit(): void {
        this.submit = true;
        this.formSubmit = true;
        if (this.validationForm.invalid) { this.formSubmit = false; return; }

        const v = this.form.value;
        const frm: any = {
            id:                this.id || 0,
            code:              v.code,
            description:       v.description,
            unit:              v.unitId              ? { id: v.unitId }              : null,
            reorderPoint:      v.reorderPoint,
            idealQty:          v.idealQty,
            location:          v.location,
            isActive:          v.isActive,
            assetAccount:      this.selectedAssetAccount   ? { id: this.selectedAssetAccount.id }   : null,
            expenseAccount:    this.selectedExpenseAccount ? { id: this.selectedExpenseAccount.id } : null,
            inventoryCategory: v.inventoryCategoryId ? { id: v.inventoryCategoryId } : null,
            hasSerialNumbers:  v.hasSerialNumbers,
            barcode:           v.barcode,
        };

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
                    this.alertService.error(this.module, 'Saving', res.failureMessage);
                }
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'Saving', ''); }
        });
    }
}
