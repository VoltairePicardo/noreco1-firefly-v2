import { Component, inject, signal } from '@angular/core';
import { FormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { LaddaModule } from 'angular2-ladda';
import { NgbTooltip } from '@ng-bootstrap/ng-bootstrap';
import { CashflowAccountService } from '../cashflow-account.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseCOAModalComponent } from '@/app/shared/modals/browse-coa-modal/browse-coa-modal.component';

@Component({
    selector: 'app-cashflow-account-add-edit',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, LaddaModule],
    templateUrl: './cashflow-account-add-edit.component.html'
})
export class CashflowAccountAddEditComponent {
    module     = 'Cash Flow Account';
    subModule  = 'Create';
    menuLink   = 'cashflow-account';
    id: any    = 0;
    editMode   = false;
    submit     = false;
    formSubmit = false;
    isLoading  = signal(false);
    types       = signal<any[]>([]);
    parentItems = signal<any[]>([]);
    selectedAccount: any = null;
    validationForm!: UntypedFormGroup;

    private service      = inject(CashflowAccountService);
    private modalService = inject(ModalService);
    public  fb           = inject(FormBuilder);
    public  route        = inject(ActivatedRoute);
    public  router       = inject(Router);
    public  alertService = inject(AlertService);

    ngOnInit(): void {
        this.loadLookups();
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

    loadLookups(): void {
        this.service.getTypes().subscribe({
            next: (types) => this.types.set(Array.isArray(types) ? types : []),
            error: () => {}
        });
        this.service.list().subscribe({
            next: (items) => {
                const all = Array.isArray(items) ? items : [];
                this.parentItems.set(this.editMode ? all.filter((i: any) => i.id !== +this.id) : all);
            },
            error: () => {}
        });
    }

    initForm(data?: any): void {
        this.selectedAccount = data?.account || null;
        this.validationForm = this.fb.group({
            name:                   [data?.name                   || '', Validators.required],
            ordinalNumber:          [data?.ordinalNumber          ?? null, Validators.required],
            cashflowItemTypeId:     [data?.cashflowItemType?.id   || null, Validators.required],
            parentCashflowItemId:   [data?.parentCashflowItem?.id || null],
            accountId:              [data?.account?.id            || null],
            showInCashFlowStatement:[data?.showInCashFlowStatement ?? true],
        });
    }

    getData(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) { this.initForm(data); }
                else { this.alertService.error(this.module, 'Not Found', ''); this.router.navigate(['/' + this.menuLink]); }
            },
            error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Error', ''); this.router.navigate(['/' + this.menuLink]); }
        });
    }

    get form(): UntypedFormGroup { return this.validationForm; }

    async openAccountBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseCOAModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this.selectedAccount = result.data;
                this.form.get('accountId')?.setValue(result.data.id);
            }
        } catch { }
    }

    getAccountLabel(): string {
        if (!this.selectedAccount) return '';
        const code  = this.selectedAccount.code  || this.selectedAccount.accountCode  || '';
        const title = this.selectedAccount.title || this.selectedAccount.accountTitle || '';
        return [code, title].filter(Boolean).join(' — ');
    }

    validSubmit(): void {
        this.submit = true;
        this.formSubmit = true;
        if (this.validationForm.invalid) { this.formSubmit = false; return; }

        const { name, ordinalNumber, cashflowItemTypeId, parentCashflowItemId, accountId, showInCashFlowStatement } = this.form.value;

        if (this.editMode && parentCashflowItemId && +parentCashflowItemId === +this.id) {
            this.formSubmit = false;
            this.alertService.error(this.module, 'Validation', 'A cash flow account cannot be its own parent.');
            return;
        }
        const frm = {
            id: this.editMode ? this.id : null,
            name,
            ordinalNumber,
            cashflowItemType:   { id: cashflowItemTypeId },
            parentCashflowItem: parentCashflowItemId ? { id: parentCashflowItemId } : null,
            account:            accountId ? { id: accountId } : null,
            showInCashFlowStatement
        };
        const req = this.editMode ? this.service.update(frm) : this.service.create(frm);

        req.subscribe({
            next: (res) => {
                if (res.success) {
                    this.alertService.success(this.module, 'Saved', '');
                    this.router.navigate(['/' + this.menuLink, res.modelId, 'detail']);
                } else {
                    this.formSubmit = false;
                    this.alertService.error(this.module, 'Saving', res.failureMessage);
                }
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'Saving', ''); }
        });
    }
}
