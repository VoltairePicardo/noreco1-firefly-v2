import { Component, inject, signal } from '@angular/core';
import { FormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { LaddaModule } from 'angular2-ladda';
import { NgbTooltip } from '@ng-bootstrap/ng-bootstrap';
import { CheckConfigService } from '../check-config.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseBankAccountModalComponent } from '@/app/shared/modals/browse-bank-account-modal/browse-bank-account-modal.component';

@Component({
    selector: 'app-check-config-add-edit',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, LaddaModule],
    templateUrl: './check-config-add-edit.component.html'
})
export class CheckConfigAddEditComponent {
    module     = 'Check Config';
    subModule  = 'Create';
    menuLink   = 'check-config';
    id: any    = 0;
    editMode   = false;
    submit     = false;
    formSubmit = false;
    isLoading  = signal(false);
    selectedBankAccount: any = null;
    validationForm!: UntypedFormGroup;

    private service      = inject(CheckConfigService);
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
                this.subModule = 'Edit';
                this.getData();
            } else {
                this.subModule = 'Create';
                this.initForm();
            }
        });
    }

    initForm(data?: any): void {
        this.selectedBankAccount = data?.bankAccount || null;
        this.validationForm = this.fb.group({
            code:                   [data?.code                   || '', Validators.required],
            bankAccountId:          [data?.bankAccount?.id        ?? null, Validators.required],
            dateFormat:             [data?.dateFormat             || ''],
            checkNoPrefix:          [data?.checkNoPrefix          || ''],
            dateX:                  [data?.dateX                  ?? null],
            dateY:                  [data?.dateY                  ?? null],
            payeeX:                 [data?.payeeX                 ?? null],
            payeeY:                 [data?.payeeY                 ?? null],
            payeeW:                 [data?.payeeW                 ?? null],
            numericAmountX:         [data?.numericAmountX         ?? null],
            numericAmountY:         [data?.numericAmountY         ?? null],
            alphaAmountX:           [data?.alphaAmountX           ?? null],
            alphaAmountY:           [data?.alphaAmountY           ?? null],
            alphaAmountW:           [data?.alphaAmountW           ?? null],
            sig1X:                  [data?.sig1X                  ?? null],
            sig1Y:                  [data?.sig1Y                  ?? null],
            sig2X:                  [data?.sig2X                  ?? null],
            sig2Y:                  [data?.sig2Y                  ?? null],
            checkNoX:               [data?.checkNoX               ?? null],
            checkNoY:               [data?.checkNoY               ?? null],
            dateLineSpacing:        [data?.dateLineSpacing        ?? null],
            amountNoOfCharToAdjust: [data?.amountNoOfCharToAdjust ?? null],
            nameNoOfCharToAdjust:   [data?.nameNoOfCharToAdjust   ?? null],
            withSigner:             [data?.withSigner             ?? false],
            showDesignation:        [data?.showDesignation        ?? false],
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

    async openBankAccountBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseBankAccountModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this.selectedBankAccount = result.data;
                this.form.get('bankAccountId')?.setValue(result.data.id);
            }
        } catch { }
    }

    getBankAccountLabel(): string {
        if (!this.selectedBankAccount) return '';
        const num  = this.selectedBankAccount.accountNumber || '';
        const name = this.selectedBankAccount.bank?.name || this.selectedBankAccount.accountName || '';
        return [num, name].filter(Boolean).join(' - ');
    }

    validSubmit(): void {
        this.submit = true;
        this.formSubmit = true;
        if (this.validationForm.invalid) { this.formSubmit = false; return; }

        const v = this.form.value;
        const { bankAccountId, ...rest } = v;
        const frm = {
            id: this.editMode ? this.id : null,
            ...rest,
            bankAccount: { id: bankAccountId },
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
