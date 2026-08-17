import { Component, inject, signal } from '@angular/core';
import { FormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { LaddaModule } from 'angular2-ladda';
import { AssemblyUnitService } from '../assembly-unit.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseCOAModalComponent } from '@/app/shared/modals/browse-coa-modal/browse-coa-modal.component';

@Component({
    selector: 'app-assembly-unit-types-add-edit',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, LaddaModule],
    templateUrl: './assembly-unit-types-add-edit.component.html'
})
export class AssemblyUnitTypesAddEditComponent {
    module     = 'Assembly Unit';
    subModule  = 'Create Assembly Type';
    menuLink   = 'assembly-unit';
    id: any    = 0;
    editMode   = false;
    submit     = false;
    formSubmit = false;
    isLoading  = signal(false);

    selectedAssetAccount:   any = null;
    selectedExpenseAccount: any = null;
    selectedAccuDepAccount: any = null;

    validationForm!: UntypedFormGroup;

    private service      = inject(AssemblyUnitService);
    private modalService = inject(ModalService);
    public  fb           = inject(FormBuilder);
    public  route        = inject(ActivatedRoute);
    public  router       = inject(Router);
    public  alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.editMode = params.get('id') != null && /^\d+$/.test(params.get('id') ?? '');
            if (this.editMode) {
                this.id = params.get('id');
                this.subModule = 'Edit Assembly Type';
                this.getData();
            } else {
                this.subModule = 'Create Assembly Type';
                this.initForm();
            }
        });
    }

    initForm(data?: any): void {
        this.validationForm = this.fb.group({
            description: [data?.description || '', Validators.required],
        });
        this.selectedAssetAccount   = data?.assetAccount                   || null;
        this.selectedExpenseAccount = data?.expenseAccount                 || null;
        this.selectedAccuDepAccount = data?.accumulatedDepreciationAccount || null;
    }

    getData(): void {
        this.isLoading.set(true);
        this.service.getType(this.id).subscribe({
            next:  (data) => {
                this.isLoading.set(false);
                if (data?.id) { this.initForm(data); }
                else { this.alertService.error(this.module, 'Not Found', ''); this.router.navigate(['/' + this.menuLink + '/types']); }
            },
            error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Error', ''); this.router.navigate(['/' + this.menuLink + '/types']); }
        });
    }

    get form(): UntypedFormGroup { return this.validationForm; }

    async openAccountBrowse(field: 'asset' | 'expense' | 'accuDep'): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseCOAModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                if (field === 'asset')   this.selectedAssetAccount   = result.data;
                if (field === 'expense') this.selectedExpenseAccount = result.data;
                if (field === 'accuDep') this.selectedAccuDepAccount = result.data;
            }
        } catch { }
    }

    getAccountLabel(account: any): string {
        if (!account) return '';
        return `${account.accountCode || ''} - ${account.accountTitle || account.accountDescription || ''}`.trim().replace(/^- /, '');
    }

    validSubmit(): void {
        this.submit = true;
        this.formSubmit = true;
        if (this.validationForm.invalid) { this.formSubmit = false; return; }

        const v = this.form.value;
        const frm: any = {
            id:          this.id || 0,
            description: v.description,
        };
        if (this.selectedAssetAccount)   frm.assetAccount                   = { id: this.selectedAssetAccount.id };
        if (this.selectedExpenseAccount) frm.expenseAccount                 = { id: this.selectedExpenseAccount.id };
        if (this.selectedAccuDepAccount) frm.accumulatedDepreciationAccount = { id: this.selectedAccuDepAccount.id };

        const req = this.editMode ? this.service.updateType(frm) : this.service.createType(frm);

        req.subscribe({
            next: (res) => {
                if (res.success) {
                    this.alertService.success(this.module, 'Assembly Type Saved', '');
                    this.router.navigate(['/' + this.menuLink + '/types']);
                } else {
                    this.formSubmit = false;
                    this.alertService.error(this.module, 'Saving', res.failureMessage);
                }
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'Saving', ''); }
        });
    }
}
