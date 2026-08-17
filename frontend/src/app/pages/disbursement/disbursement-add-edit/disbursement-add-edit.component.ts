import { Component, inject, signal, ViewChild, ElementRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { DisbursementService } from '../disbursement.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseIemopBillingModalComponent } from '@/app/shared/modals/browse-iemop-billing-modal/browse-iemop-billing-modal.component';
import { BrowseTempGLModalComponent } from '@/app/shared/modals/browse-temp-gl-modal/browse-temp-gl-modal.component';
import { JournalEntriesFormComponent, JournalEntry } from '@/app/shared/forms/journal-entries-form/journal-entries-form.component';
import { provideIcons } from '@ng-icons/core';
import {
    tablerSearch, tablerX, tablerArrowLeft, tablerCheck,
    tablerPlus, tablerTrash, tablerPaperclip, tablerPhoto, tablerFile
} from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-disbursement-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective,
        JournalEntriesFormComponent
    ],
    providers: [
        provideFlatpickrDefaults(),
        ...SHARED_PROVIDERS,
        provideIcons({ tablerSearch, tablerX, tablerArrowLeft, tablerCheck, tablerPlus, tablerTrash, tablerPaperclip, tablerPhoto, tablerFile })
    ],
    templateUrl: './disbursement-add-edit.component.html'
})
export class DisbursementAddEditComponent {
    @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

    module    = 'Disbursement';
    subModule = 'Create';
    menuLink  = 'disbursement';

    id: any    = null;
    editMode   = false;
    formSubmit = false;
    isLoading  = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    voucherDate          = '';
    checkDate            = '';
    checkNumber          = '';
    particulars          = '';
    additionalPayeeInfo  = '';
    checkAmount: number | null = null;

    payee: any = null;

    bankAccounts      : any[] = [];
    selectedBankAccount: any  = null;

    journalEntries: JournalEntry[] = [];
    iemopBillings : any[] = [];
    tempBatch: any = null;

    stagedFiles  : File[] = [];
    uploadingFiles = false;

    signatories: { [key: string]: any } = {
        checker:              null,
        budgetOfficer:        null,
        recommendingApproval: null,
        auditingOfficer:      null,
        checkPrinter:         null,
        approvingOfficer:     null,
        secondCheckSign:      null
    };

    signatoryLabels: { [key: string]: string } = {
        checker:              'Checked By',
        budgetOfficer:        'Budget Officer',
        recommendingApproval: 'Recommended By',
        auditingOfficer:      'Audited By',
        checkPrinter:         'Check Printing Officer',
        approvingOfficer:     'Approved By',
        secondCheckSign:      '2nd Signatory'
    };

    signatoryKeys = [
        'checker', 'budgetOfficer', 'recommendingApproval',
        'auditingOfficer', 'checkPrinter', 'approvingOfficer', 'secondCheckSign'
    ];

    private service      = inject(DisbursementService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private modalService = inject(ModalService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.editMode = idParam != null && /^\d+$/.test(idParam);
            if (this.editMode) {
                this.id        = Number(idParam);
                this.subModule = 'Edit';
                this.loadForEdit();
            } else {
                this.subModule = 'Create';
                this.addEntry();
                this.loadDefaultSignatories();
            }
            this.loadBankAccounts();
        });
    }

    loadBankAccounts(): void {
        this.service.getBankAccounts().subscribe({
            next: (data) => { this.bankAccounts = data || []; },
            error: () => { this.bankAccounts = []; }
        });
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.voucherDate         = data.voucherDate         ? new Date(data.voucherDate).toISOString().substring(0, 10) : '';
                    this.checkDate           = data.checkDate           ? new Date(data.checkDate).toISOString().substring(0, 10)   : '';
                    this.checkNumber         = data.checkNumber         || '';
                    this.particulars         = data.particulars         || '';
                    this.additionalPayeeInfo = data.additionalPayeeInfo || '';
                    this.checkAmount         = data.checkAmount         ?? null;
                    this.payee               = data.payee               || null;
                    this.selectedBankAccount = data.bankAccount         || null;

                    this.journalEntries = (data.journalEntries || data.details || []).map((e: any) => ({
                        account: e.account || null,
                        debit:   Number(e.debitAmount  || e.debit)  || null,
                        credit:  Number(e.creditAmount || e.credit) || null,
                    }));
                    if (this.journalEntries.length === 0) this.addEntry();

                    this.iemopBillings = data.iemopBillings || [];

                    this.signatories = {
                        checker:              data.checker              || data.checkedBy         || null,
                        budgetOfficer:        data.budgetOfficer                                  || null,
                        recommendingApproval: data.recommendingApproval || data.recommendedBy     || null,
                        auditingOfficer:      data.auditingOfficer      || data.auditedBy         || null,
                        checkPrinter:         data.checkPrinter                                   || null,
                        approvingOfficer:     data.approvingOfficer     || data.approvedBy        || null,
                        secondCheckSign:      data.secondCheckSign                                || null
                    };
                } else {
                    this.alertService.error(this.module, 'Record not found.', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load record.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (data) => {
                if (data) {
                    this.signatories = {
                        checker:              data.checker              || data.checkedBy         || null,
                        budgetOfficer:        data.budgetOfficer                                  || null,
                        recommendingApproval: data.recommendingApproval || data.recommendedBy     || null,
                        auditingOfficer:      data.auditingOfficer      || data.auditedBy         || null,
                        checkPrinter:         data.checkPrinter                                   || null,
                        approvingOfficer:     data.approvingOfficer     || data.approvedBy        || null,
                        secondCheckSign:      data.secondCheckSign                                || null
                    };
                }
            },
            error: () => {}
        });
    }

    async openTempGLBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseTempGLModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                const { batch, entries } = result.data;
                this.tempBatch    = batch;
                this.particulars  = batch.remarks || '';
                this.journalEntries = (entries as any[]).map((e: any) => ({
                    account: e.account || { accountCode: e.code, accountTitle: e.description, id: e.accountId ?? null },
                    debit:   Number(e.debit)  || null,
                    credit:  Number(e.credit) || null,
                }));
            }
        } catch { }
    }

    clearTempGL(): void {
        this.tempBatch      = null;
        this.journalEntries = [];
        this.particulars    = '';
    }

    async openPayeeBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseEntityModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this.payee = result.data;
            }
        } catch { }
    }

    clearPayee(): void { this.payee = null; }

    // Journal Entries
    addEntry(): void {
        this.journalEntries.push({ account: null, debit: null, credit: null });
    }

    get totalDebit(): number {
        return this.journalEntries.reduce((sum, e) => sum + (Number(e.debit) || 0), 0);
    }

    get totalCredit(): number {
        return this.journalEntries.reduce((sum, e) => sum + (Number(e.credit) || 0), 0);
    }

    get isBalanced(): boolean {
        return Math.abs(this.totalDebit - this.totalCredit) < 0.001;
    }

    // IEMOP Billings
    async openIemopBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseIemopBillingModalComponent, {}, { size: 'xl', centered: true });
            if (result?.action === 'select' && result?.data) {
                const exists = this.iemopBillings.some(b => b.id === result.data.id);
                if (!exists) this.iemopBillings.push(result.data);
            }
        } catch { }
    }

    removeIemop(index: number): void { this.iemopBillings.splice(index, 1); }

    get iemopBaseTotal(): number {
        return this.iemopBillings.reduce((s, b) => s + ((b.vatablePurchases || 0) + (b.zeroRatedEcoPurchases || 0)), 0);
    }

    get iemopWtaxTotal(): number {
        return this.iemopBillings.reduce((s, b) => s + (b.ewtPurchases || 0), 0);
    }

    // Signatories
    async openEntityBrowse(key: string): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseEntityModalComponent, { entityTypes: [1] }, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this.signatories[key] = result.data;
            }
        } catch { }
    }

    clearSignatory(key: string): void { this.signatories[key] = null; }

    // Attachments
    openFilePicker(): void {
        this.fileInput.nativeElement.click();
    }

    onFileSelect(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files) {
            const allowed = ['image/jpeg', 'image/png', 'application/pdf'];
            Array.from(input.files).forEach(f => {
                if (allowed.includes(f.type)) this.stagedFiles.push(f);
            });
        }
        input.value = '';
    }

    removeStagedFile(index: number): void {
        this.stagedFiles.splice(index, 1);
    }

    isImage(file: File): boolean {
        return file.type.startsWith('image/');
    }

    private uploadAndNavigate(savedId: number): void {
        const formData = new FormData();
        this.stagedFiles.forEach(f => formData.append('files', f, f.name));
        this.service.uploadFiles(savedId, formData).subscribe({
            next: () => {
                this.formSubmit = false;
                this.alertService.success(this.module, 'Saved successfully.', '');
                this.router.navigate(['/' + this.menuLink, savedId, 'detail']);
            },
            error: () => {
                this.formSubmit = false;
                this.alertService.success(this.module, 'Saved successfully.', 'Record saved but file upload failed.');
                this.router.navigate(['/' + this.menuLink, savedId, 'detail']);
            }
        });
    }

    save(): void {
        if (!this.voucherDate) {
            this.alertService.warning(this.module, 'Validation', 'Please enter a voucher date.');
            return;
        }
        if (!this.payee) {
            this.alertService.warning(this.module, 'Validation', 'Please select a payee.');
            return;
        }
        if (this.journalEntries.length === 0) {
            this.alertService.warning(this.module, 'Validation', 'Please add at least one journal entry.');
            return;
        }
        if (!this.isBalanced) {
            this.alertService.warning(this.module, 'Validation', 'Debit and credit totals must be equal.');
            return;
        }

        this.formSubmit = true;

        const sigRef = (key: string) => this.signatories[key]
            ? { accountNo: this.signatories[key].accountNo } : null;

        const payload: any = {
            id:                  this.editMode ? this.id : null,
            voucherDate:         this.voucherDate,
            checkDate:           this.checkDate           || null,
            checkNumber:         this.checkNumber         || null,
            checkAmount:         this.checkAmount         ?? null,
            particulars:         this.particulars         || null,
            additionalPayeeInfo: this.additionalPayeeInfo || null,
            payee:               { accountNo: this.payee.accountNo },
            bankAccount:         this.selectedBankAccount ? { id: this.selectedBankAccount.id } : null,
            generalLedgerLines:  this.journalEntries.map(e => ({
                code:        e.account?.accountCode  || '',
                description: e.account?.accountTitle || '',
                accountId:   e.account?.id           || null,
                debit:       Number(e.debit)         || 0,
                credit:      Number(e.credit)        || 0
            })),
            iemopBillings:       this.iemopBillings.map(b => ({ id: b.id })),
            checker:              sigRef('checker'),
            budgetOfficer:        sigRef('budgetOfficer'),
            recommendingOfficer:  sigRef('recommendingApproval'),
            auditingOfficer:      sigRef('auditingOfficer'),
            checkPrinter:         sigRef('checkPrinter'),
            approvingOfficer:     sigRef('approvingOfficer'),
            secondCheckSign:      sigRef('secondCheckSign')
        };

        const req$ = this.editMode ? this.service.update(payload) : this.service.create(payload);
        req$.subscribe({
            next: (res) => {
                if (res?.success) {
                    if (this.stagedFiles.length > 0) {
                        this.uploadAndNavigate(res.modelId);
                    } else {
                        this.formSubmit = false;
                        this.alertService.success(this.module, 'Saved successfully.', '');
                        this.router.navigate(['/' + this.menuLink, res.modelId, 'detail']);
                    }
                } else {
                    this.formSubmit = false;
                    this.alertService.error(this.module, 'Save failed.', res?.failureMessage || '');
                }
            },
            error: () => {
                this.formSubmit = false;
                this.alertService.error(this.module, 'An error occurred while saving.', '');
            }
        });
    }
}
