import { Component, inject, signal } from '@angular/core';
import { FormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { LaddaModule } from 'angular2-ladda';
import { NgbTooltip } from '@ng-bootstrap/ng-bootstrap';
import { PettyCashFundService } from '../petty-cash-fund.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseCOAModalComponent } from '@/app/shared/modals/browse-coa-modal/browse-coa-modal.component';

@Component({
    selector: 'app-petty-cash-fund-add-edit',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, LaddaModule],
    templateUrl: './petty-cash-fund-add-edit.component.html'
})
export class PettyCashFundAddEditComponent {
    module     = 'Petty Cash Fund';
    subModule  = 'Create';
    menuLink   = 'petty-cash-fund';
    id: any    = 0;
    editMode   = false;
    submit     = false;
    formSubmit = false;
    isLoading  = signal(false);
    offices    = signal<any[]>([]);
    selectedAccount: any = null;
    validationForm!: UntypedFormGroup;

    private service      = inject(PettyCashFundService);
    private modalService = inject(ModalService);
    public  fb           = inject(FormBuilder);
    public  route        = inject(ActivatedRoute);
    public  router       = inject(Router);
    public  alertService = inject(AlertService);

    ngOnInit(): void {
        this.loadOffices();
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

    loadOffices(): void {
        this.service.getOffices().subscribe({
            next: (data) => this.offices.set(data),
            error: () => this.alertService.error(this.module, 'Load Offices', '')
        });
    }

    initForm(data?: any): void {
        this.selectedAccount = data?.account || null;
        this.validationForm = this.fb.group({
            description: [data?.description || '', Validators.required],
            officeId:    [data?.office?.id   || null, Validators.required],
            accountId:   [data?.account?.id  || null, Validators.required],
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

        const { description, officeId, accountId } = this.form.value;
        const frm = {
            id:          this.editMode ? this.id : null,
            description,
            office:      { id: officeId },
            account:     { id: accountId }
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
