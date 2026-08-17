import { Component, inject, signal } from '@angular/core';
import { FormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { LaddaModule } from 'angular2-ladda';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { CoaService } from '../coa.service';
import { BrowseCOAModalComponent } from '@/app/shared/modals/browse-coa-modal/browse-coa-modal.component';

@Component({
    selector: 'app-coa-add-edit',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, LaddaModule],
    templateUrl: './coa-add-edit.component.html'
})
export class CoaAddEditComponent {
    module     = 'Chart of Accounts';
    subModule  = 'Create';
    menuLink   = 'coa';
    id: any    = 0;
    editMode   = false;
    submit     = false;
    formSubmit = false;
    isLoading  = signal(false);
    validationForm!: UntypedFormGroup;

    accountTypes  = signal<any[]>([]);
    accountGroups = signal<any[]>([]);
    factors       = signal<any[]>([]);
    selectedParentAccount: any = null;

    private service      = inject(CoaService);
    public  fb           = inject(FormBuilder);
    public  route        = inject(ActivatedRoute);
    public  router       = inject(Router);
    public  alertService = inject(AlertService);
    private modalService = inject(NgbModal);

    ngOnInit(): void {
        this.service.getAccountTypes().subscribe({ next: (d) => this.accountTypes.set(Array.isArray(d) ? d : []) });
        this.service.getAccountGroups().subscribe({ next: (d) => this.accountGroups.set(Array.isArray(d) ? d : []) });
        this.service.getFactors().subscribe({ next: (d) => this.factors.set(Array.isArray(d) ? d : []) });

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
        this.validationForm = this.fb.group({
            code:          [data?.code          || '',  Validators.required],
            title:         [data?.title         || '',  Validators.required],
            classification:[data?.classification || ''],
            normalBalance: [data?.normalBalance  ?? 1],
            isActive:      [data != null ? (data.isActive === 1 || data.isActive === true) : true],
            isHeader:      [data != null ? (data.isHeader === 1 || data.isHeader === true) : false],
            hasSL:         [data != null ? (data.hasSL   === 1 || data.hasSL   === true) : false],
            glaccount:     [data?.GLAccount      || ''],
            slaccount:     [data?.SLAccount      || ''],
            auxAccount:    [data?.auxAccount     || ''],
            accountTypeId: [data?.accountType?.id  ?? null, Validators.required],
            accountGroupId:[data?.accountGroup?.id ?? null, Validators.required],
            factorId:      [data?.factor?.id       ?? null],
        });

        if (data?.parentAccount) {
            this.selectedParentAccount = data.parentAccount;
        } else {
            this.selectedParentAccount = null;
        }
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

    openParentAccountBrowse(): void {
        const ref = this.modalService.open(BrowseCOAModalComponent, { size: 'lg', centered: true });
        ref.result.then((result) => {
            if (result?.action === 'select') {
                const d = result.data;
                // normalize modal field names (accountCode/accountTitle) to (code/title)
                this.selectedParentAccount = {
                    id:    d.id,
                    code:  d.code  || d.accountCode  || '',
                    title: d.title || d.accountTitle || '',
                };
            }
        }).catch(() => {});
    }

    clearParentAccount(): void {
        this.selectedParentAccount = null;
    }

    get form(): UntypedFormGroup { return this.validationForm; }

    validSubmit(): void {
        this.submit = true;
        this.formSubmit = true;
        if (this.validationForm.invalid) { this.formSubmit = false; return; }

        const v = this.form.value;
        const frm: any = {
            id:            this.id || 0,
            title:         v.title,
            classification:v.classification,
            normalBalance: v.normalBalance,
            glaccount:     v.glaccount,
            slaccount:     v.slaccount,
            auxAccount:    v.auxAccount,
            isActive:      v.isActive ? 1 : 0,
            isHeader:      v.isHeader ? 1 : 0,
            hasSL:         v.hasSL    ? 1 : 0,
            parentAccountId: this.selectedParentAccount?.id ?? 0,
            accountType:   v.accountTypeId  ? { id: v.accountTypeId }  : null,
            accountGroup:  v.accountGroupId ? { id: v.accountGroupId } : null,
        };

        if (v.factorId) {
            frm.allocationFactor = { id: v.factorId };
        }

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
