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
import {
    BrowseCvSourceVoucherModalComponent,
    CvSourceVoucherType,
    CvVoucherDto,
    CvVoucherInstallmentDetail
} from '@/app/shared/modals/browse-cv-source-voucher-modal/browse-cv-source-voucher-modal.component';
import { BrowseJobOrderModalComponent } from '@/app/shared/modals/browse-job-order-modal/browse-job-order-modal.component';
import { BrowsePurchaseOrderModalComponent } from '@/app/shared/modals/browse-purchase-order-modal/browse-purchase-order-modal.component';
import { SelectBankAccountsModalComponent } from '@/app/shared/modals/select-bank-accounts-modal/select-bank-accounts-modal.component';
import { JournalEntriesFormComponent, JournalEntry } from '@/app/shared/forms/journal-entries-form/journal-entries-form.component';
import { provideIcons } from '@ng-icons/core';
import {
    tablerSearch, tablerX, tablerArrowLeft, tablerCheck,
    tablerPlus, tablerTrash, tablerPaperclip, tablerPhoto, tablerFile, tablerLink
} from '@ng-icons/tabler-icons';

interface CheckNumberRow {
    bankAccount: any;
    checkNumber: string;
    loadingNextNumber?: boolean;
}

const VOUCHER_TYPE_LABELS: Record<CvSourceVoucherType, string> = {
    APV: 'Account Payable Voucher',
    CA:  'Cash Advance',
    JV:  'Journal Voucher',
    RR:  'Receiving Report',
    JOA: 'JO Acceptance'
};

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
        provideIcons({
            tablerSearch, tablerX, tablerArrowLeft, tablerCheck, tablerPlus,
            tablerTrash, tablerPaperclip, tablerPhoto, tablerFile, tablerLink
        })
    ],
    templateUrl: './disbursement-add-edit.component.html'
})
export class DisbursementAddEditComponent {
    @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

    module    = 'Disbursement';
    subModule = 'Create';
    menuLink  = 'disbursement';

    id: any       = null;
    transId: number | null = null;
    editMode      = false;
    formSubmit    = false;
    isLoading     = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    voucherDate          = '';
    particulars          = '';
    additionalPayeeInfo  = '';
    checkAmount: number | null = null;

    payee: any = null;

    banks        : any[] = [];
    selectedBank : any   = null;
    loadingBankAccountsForCv = false;
    checkNumberRows     : CheckNumberRow[] = [];

    purchaseOrder: any = null;
    jobOrder: any = null;

    readonly voucherTypeLabels = VOUCHER_TYPE_LABELS;
    selectedVoucherType: CvSourceVoucherType | null = null;
    selectedSourceVoucher: CvVoucherDto | null = null;
    cashAdvances: CvVoucherDto[] = [];
    selectedInstallmentIds: number[] = [];
    loadingSourceVoucherEntries = false;

    journalEntries: JournalEntry[] = [];
    iemopBillings : any[] = [];
    tempBatch: any = null;

    stagedFiles  : File[] = [];
    existingFiles: any[]  = [];
    filesToRemove: any[]  = [];
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
            this.loadBanks();
        });
    }

    loadBanks(): void {
        this.service.getBanks().subscribe({
            next: (data) => { this.banks = data || []; },
            error: () => { this.banks = []; }
        });
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.transId             = data.transId ?? data.transaction?.id ?? null;
                    this.voucherDate         = data.voucherDate         ? new Date(data.voucherDate).toISOString().substring(0, 10) : '';
                    this.particulars         = data.particulars         || '';
                    this.additionalPayeeInfo = data.additionalPayeeInfo || '';
                    this.checkAmount         = data.checkAmount         ?? null;
                    this.payee               = data.payee               || null;
                    this.selectedBank        = data.bank                || null;
                    this.purchaseOrder       = data.purchaseOrder       || null;
                    this.jobOrder            = data.jobOrder            || null;
                    this.cashAdvances        = data.cashAdvances        || [];

                    if (data.accountsPayableVoucher?.id) {
                        this.selectedVoucherType   = 'APV';
                        this.selectedSourceVoucher = data.accountsPayableVoucher;
                    } else if (data.journalVoucher?.id) {
                        this.selectedVoucherType   = 'JV';
                        this.selectedSourceVoucher = data.journalVoucher;
                    } else if (data.receivingReport?.id) {
                        this.selectedVoucherType   = 'RR';
                        this.selectedSourceVoucher = data.receivingReport;
                    } else if (data.joAcceptance?.id) {
                        this.selectedVoucherType   = 'JOA';
                        this.selectedSourceVoucher = data.joAcceptance;
                    }

                    this.journalEntries = (data.generalLedgerLines || []).map((line: any) => {
                        // The backend currently attaches the transaction's wTaxEntry to every
                        // GL line (a pre-existing quirk), so only the line whose account actually
                        // matches the ATC's WHT account is flagged as the auto-generated WHT row.
                        const whtAccountId = line.wTaxEntry?.atc?.account?.id;
                        const isWhtRow = whtAccountId != null && whtAccountId === line.accountId;

                        return {
                            account: {
                                id: line.accountId,
                                accountCode: line.code,
                                accountTitle: line.description,
                                hasSL: line.hasSL
                            },
                            debit:  Number(line.debit)  || null,
                            credit: Number(line.credit) || null,
                            slentries: (line.slentries || []).map((sl: any) => ({
                                entity: null,
                                accountNo: sl.accountNo,
                                name: sl.name,
                                debit:  Number(sl.debit)  || null,
                                credit: Number(sl.credit) || null
                            })),
                            generated: isWhtRow,
                            wTaxEntry: isWhtRow ? line.wTaxEntry : null
                        };
                    });
                    if (this.journalEntries.length === 0) this.addEntry();

                    this.iemopBillings = data.iemopBillings || [];

                    this.signatories = {
                        checker:              data.checker              || data.checkedBy         || null,
                        budgetOfficer:        data.budgetOfficer                                  || null,
                        recommendingApproval: data.recommendingOfficer  || data.recommendedBy     || null,
                        auditingOfficer:      data.auditor              || data.auditedBy         || null,
                        checkPrinter:         data.checkPrinter                                   || null,
                        approvingOfficer:     data.approvingOfficer     || data.approvedBy        || null,
                        secondCheckSign:      data.secondCheckSign                                || null
                    };

                    if (this.transId) this.loadExistingCheckNumbers(this.transId);
                    this.loadAttachments();
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

    loadAttachments(): void {
        if (!this.transId) return;
        this.service.getFiles(this.transId).subscribe({
            next: (files) => { this.existingFiles = files || []; },
            error: () => { this.existingFiles = []; }
        });
    }

    fileUrl(fileId: number): string {
        return this.service.fileUrl(fileId);
    }

    removeExistingFile(index: number): void {
        const [removed] = this.existingFiles.splice(index, 1);
        if (removed) this.filesToRemove.push(removed);
    }

    private loadExistingCheckNumbers(transId: number): void {
        this.service.getCvChecks(transId).subscribe({
            next: (checks) => {
                this.checkNumberRows = (checks || []).map((c: any) => ({
                    bankAccount: c.bankAccount,
                    checkNumber: c.checkNumber || ''
                }));
            },
            error: () => {}
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

    // Source document linking (APV / CA / JV / RR / JOA) — mirrors legacy cv2.js setSelectedVoucher()
    async openSourceVoucherBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseCvSourceVoucherModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action !== 'select' || !result?.data) return;

            const voucherType: CvSourceVoucherType = result.voucherType;
            const voucher: CvVoucherDto = result.data;

            if (voucherType === 'CA') {
                this.addCashAdvance(voucher);
                return;
            }

            this.selectedVoucherType   = voucherType;
            this.selectedSourceVoucher = voucher;
            this.selectedInstallmentIds = [];

            if (voucherType === 'APV' || voucherType === 'RR' || voucherType === 'JOA') {
                this.payee = { accountNo: voucher.slentityAccountNo, name: voucher.slentityName };
            }
            this.checkAmount = voucher.amount ?? null;
            this.particulars = voucher.particulars || this.particulars;

            if (voucherType === 'APV') {
                if (voucher.forInstallment) {
                    // Amount is derived from selected installments, not the full APV amount.
                    this.checkAmount = null;
                } else {
                    this.fetchSourceGLEntries(this.service.getGLEntriesWithoutWithholdingTax(voucher.transId!));
                }
            } else if (voucherType === 'JV') {
                this.fetchSourceGLEntries(this.service.getGLEntries(voucher.transId!));
            }
            // RR / JOA: legacy does not auto-fetch GL entries for these — payee/amount/particulars only.
        } catch { }
    }

    private fetchSourceGLEntries(request: ReturnType<DisbursementService['getGLEntries']>): void {
        this.loadingSourceVoucherEntries = true;
        request.subscribe({
            next: (entries) => {
                this.loadingSourceVoucherEntries = false;
                this.journalEntries = (entries || []).map((e: any): JournalEntry => ({
                    account: { accountCode: e.code, accountTitle: e.description, id: e.accountId, hasSL: e.hasSL },
                    debit:   Number(e.debit)  || null,
                    credit:  Number(e.credit) || null
                }));
            },
            error: () => { this.loadingSourceVoucherEntries = false; }
        });
    }

    clearSourceVoucher(): void {
        this.selectedVoucherType    = null;
        this.selectedSourceVoucher  = null;
        this.selectedInstallmentIds = [];
        this.payee        = null;
        this.checkAmount  = null;
        this.journalEntries = [];
    }

    get installmentDetails(): CvVoucherInstallmentDetail[] {
        return this.selectedSourceVoucher?.installmentDetails || [];
    }

    isInstallmentSelected(detail: CvVoucherInstallmentDetail): boolean {
        return this.selectedInstallmentIds.includes(detail.id);
    }

    toggleInstallmentDetail(detail: CvVoucherInstallmentDetail): void {
        const idx = this.selectedInstallmentIds.indexOf(detail.id);
        if (idx >= 0) {
            this.selectedInstallmentIds.splice(idx, 1);
        } else {
            this.selectedInstallmentIds.push(detail.id);
        }
        this.checkAmount = this.installmentDetails
            .filter(d => this.selectedInstallmentIds.includes(d.id))
            .reduce((sum, d) => sum + (Number(d.amount) || 0), 0);
    }

    // Cash Advance linking — additive, matching legacy handleCashAdvance()
    private addCashAdvance(voucher: CvVoucherDto): void {
        if (this.cashAdvances.some(ca => ca.id === voucher.id)) return;

        if (this.cashAdvances.length === 0) this.journalEntries = [];

        this.cashAdvances.push(voucher);
        this.checkAmount = (this.checkAmount || 0) + (voucher.amount || 0);
        this.particulars = this.particulars ? `${this.particulars}, ${voucher.particulars || ''}` : (voucher.particulars || '');

        this.service.getGLEntriesForCashAdvance(voucher.id!).subscribe({
            next: (entries) => this.mergeCashAdvanceEntries(entries || []),
            error: () => {}
        });
    }

    private mergeCashAdvanceEntries(entries: any[]): void {
        const byAccountId = new Map<number, JournalEntry>();
        this.journalEntries.forEach(e => { if (e.account?.id != null) byAccountId.set(e.account.id, e); });

        entries.forEach((e: any) => {
            const accountId = e.accountId;
            if (accountId == null) return;
            const existing = byAccountId.get(accountId);
            if (existing) {
                existing.debit  = (Number(existing.debit)  || 0) + (Number(e.debit)  || 0);
                existing.credit = (Number(existing.credit) || 0) + (Number(e.credit) || 0);
            } else {
                const entry: JournalEntry = {
                    account: { accountCode: e.code, accountTitle: e.description, id: accountId, hasSL: e.hasSL },
                    debit:   Number(e.debit)  || null,
                    credit:  Number(e.credit) || null
                };
                byAccountId.set(accountId, entry);
                this.journalEntries.push(entry);
            }
        });
    }

    removeCashAdvance(index: number): void {
        const [removed] = this.cashAdvances.splice(index, 1);
        if (removed) this.checkAmount = Math.max(0, (this.checkAmount || 0) - (removed.amount || 0));
        if (this.cashAdvances.length === 0) this.journalEntries = [];
    }

    // Job Order / Purchase Order — mutually exclusive, matching legacy jo_selection_handler/po_selection_handler
    async openJobOrderBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseJobOrderModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this.jobOrder      = result.data;
                this.purchaseOrder = null;
            }
        } catch { }
    }

    clearJobOrder(): void { this.jobOrder = null; }

    async openPurchaseOrderBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowsePurchaseOrderModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this.purchaseOrder = result.data;
                this.jobOrder       = null;
            }
        } catch { }
    }

    clearPurchaseOrder(): void { this.purchaseOrder = null; }

    // Bank + bank accounts + check numbers
    compareById(a: any, b: any): boolean {
        return a === b || a?.id === b?.id;
    }

    onBankChange(): void {
        if (!this.selectedBank) return;
        this.loadingBankAccountsForCv = true;
        this.service.getBankAccountsForCv(this.selectedBank.id).subscribe({
            next: (accounts) => {
                this.loadingBankAccountsForCv = false;
                this.openSelectBankAccountsModal(accounts || []);
            },
            error: () => { this.loadingBankAccountsForCv = false; }
        });
    }

    private async openSelectBankAccountsModal(accounts: any[]): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                SelectBankAccountsModalComponent,
                {
                    bank: this.selectedBank,
                    bankAccounts: accounts,
                    alreadySelectedIds: this.checkNumberRows.map(r => r.bankAccount.id)
                },
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                (result.data as any[]).forEach(bankAccount => {
                    if (this.checkNumberRows.some(r => r.bankAccount.id === bankAccount.id)) return;
                    const row: CheckNumberRow = { bankAccount, checkNumber: '' };
                    this.checkNumberRows.push(row);
                    this.getNextCheckNumberFor(row);
                    this.addJournalEntryForBankAccount(bankAccount);
                });
            }
        } catch { }
    }

    private addJournalEntryForBankAccount(bankAccount: any): void {
        const account = bankAccount.account;
        if (!account?.id) return;
        if (this.journalEntries.some(e => e.account?.id === account.id)) return;

        const blankEntry = this.journalEntries.find(e => !e.account);
        const entry: JournalEntry = {
            account: { accountCode: account.code, accountTitle: account.title, id: account.id, hasSL: account.hasSL || false },
            debit:   null,
            credit:  null
        };
        if (blankEntry) {
            Object.assign(blankEntry, entry);
        } else {
            this.journalEntries.push(entry);
        }
    }

    removeBankAccountRow(index: number): void {
        const [removed] = this.checkNumberRows.splice(index, 1);
        const accountId = removed?.bankAccount?.account?.id;
        if (accountId == null) return;
        const entryIdx = this.journalEntries.findIndex(e => e.account?.id === accountId);
        if (entryIdx >= 0) this.journalEntries.splice(entryIdx, 1);
    }

    getNextCheckNumberFor(row: CheckNumberRow): void {
        row.loadingNextNumber = true;
        this.service.getNextCheckNumber(row.bankAccount.id).subscribe({
            next: (res) => {
                row.loadingNextNumber = false;
                if (res?.checkNumber) row.checkNumber = res.checkNumber;
            },
            error: () => { row.loadingNextNumber = false; }
        });
    }

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

    save(): void {
        if (!this.voucherDate) {
            this.alertService.warning(this.module, 'Validation', 'Please enter a voucher date.');
            return;
        }
        if (!this.payee) {
            this.alertService.warning(this.module, 'Validation', 'Please select a payee.');
            return;
        }
        if (this.checkAmount == null || isNaN(Number(this.checkAmount))) {
            this.alertService.warning(this.module, 'Validation', 'Please enter check amount.');
            return;
        }
        if (!this.particulars || this.particulars.trim() === '') {
            this.alertService.warning(this.module, 'Validation', 'Please enter particulars.');
            return;
        }

        const requiredSignatoryKeys = ['checker', 'budgetOfficer', 'recommendingApproval', 'auditingOfficer', 'checkPrinter', 'approvingOfficer'];
        const missingSignatory = requiredSignatoryKeys.find(key => !this.signatories[key]);
        if (missingSignatory) {
            this.alertService.warning(this.module, 'Validation', `Please select ${this.signatoryLabels[missingSignatory]}.`);
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
        if (this.totalDebit <= 0 || this.totalCredit <= 0) {
            this.alertService.warning(this.module, 'Validation', 'Journal entries totals must be greater than zero.');
            return;
        }

        const missingSl = this.journalEntries.find(e =>
            !e.generated && e.account?.hasSL && !(e.slentries && e.slentries.length > 0)
        );
        if (missingSl) {
            this.alertService.warning(this.module, 'Validation', `SL entry is required for ${missingSl.account?.accountTitle || 'the selected account'}.`);
            return;
        }

        if (this.checkNumberRows.some(r => !r.checkNumber || r.checkNumber.trim() === '')) {
            this.alertService.warning(this.module, 'Validation', 'Please enter a check number for every selected bank account.');
            return;
        }

        this.formSubmit = true;

        const sigRef = (key: string) => this.signatories[key]
            ? { accountNo: this.signatories[key].accountNo } : null;

        const payload: any = {
            id:                  this.editMode ? this.id : null,
            transaction:         this.transId ? { id: this.transId } : null,
            voucherDate:         this.voucherDate,
            checkAmount:         this.checkAmount        ?? null,
            amount:              this.totalDebit,
            particulars:         this.particulars         || null,
            additionalPayeeInfo: this.additionalPayeeInfo || null,
            payee:               { accountNo: this.payee.accountNo },
            bank:                this.selectedBank ? { id: this.selectedBank.id } : null,
            checker:              sigRef('checker'),
            budgetOfficer:        sigRef('budgetOfficer'),
            recommendingOfficer:  sigRef('recommendingApproval'),
            auditingOfficer:      sigRef('auditingOfficer'),
            checkPrinter:         sigRef('checkPrinter'),
            approvingOfficer:     sigRef('approvingOfficer'),
            secondCheckSign:      sigRef('secondCheckSign'),
            generalLedgerLines:  this.journalEntries.map(e => ({
                code:        e.account?.accountCode  || '',
                description: e.account?.accountTitle || '',
                accountId:   e.account?.id           || null,
                debit:       Number(e.debit)         || 0,
                credit:      Number(e.credit)        || 0,
                hasSL:       !!e.account?.hasSL,
                wTaxEntry:   e.wTaxEntry || null,
                vatEntry:    e.vatEntry  || null
            })),
            subLedgerLines: this.journalEntries.flatMap(e =>
                (e.slentries || []).map(sl => ({
                    accountId: e.account?.id ?? null,
                    accountNo: sl.accountNo,
                    debit:     Number(sl.debit)  || 0,
                    credit:    Number(sl.credit) || 0
                }))
            ),
            checkNumbers: this.checkNumberRows.map(r => ({
                checkNumber: r.checkNumber,
                bankAccount: { id: r.bankAccount.id },
                account:     { id: r.bankAccount.account?.id }
            })),
            purchaseOrder:  this.purchaseOrder ? { id: this.purchaseOrder.id } : null,
            jobOrder:       this.jobOrder      ? { id: this.jobOrder.id }      : null,
            accountsPayableVoucher: this.selectedVoucherType === 'APV' && this.selectedSourceVoucher
                ? { id: this.selectedSourceVoucher.id } : null,
            journalVoucher: this.selectedVoucherType === 'JV' && this.selectedSourceVoucher
                ? { id: this.selectedSourceVoucher.id } : null,
            receivingReport: this.selectedVoucherType === 'RR' && this.selectedSourceVoucher
                ? { id: this.selectedSourceVoucher.id } : null,
            joAcceptance: this.selectedVoucherType === 'JOA' && this.selectedSourceVoucher
                ? { id: this.selectedSourceVoucher.id } : null,
            cashAdvances: this.cashAdvances.map(ca => ({ id: ca.id })),
            selectedInstallmentDetails: this.selectedInstallmentIds.map(id => ({ id })),
            iemopBillings: this.iemopBillings.map(b => ({ id: b.id }))
        };

        const req$ = this.editMode
            ? this.service.update(payload, this.stagedFiles, this.filesToRemove)
            : this.service.create(payload, this.stagedFiles);
        req$.subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Saved successfully.', '');
                    this.router.navigate(['/' + this.menuLink, res.modelId, 'detail']);
                } else {
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
