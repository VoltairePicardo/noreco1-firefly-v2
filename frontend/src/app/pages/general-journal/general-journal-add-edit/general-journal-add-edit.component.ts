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
import { GeneralJournalService } from '../general-journal.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseDocumentModalComponent } from '@/app/shared/modals/browse-document-modal/browse-document-modal.component';
import { BrowseTempGLModalComponent } from '@/app/shared/modals/browse-temp-gl-modal/browse-temp-gl-modal.component';
import { JournalEntriesFormComponent, JournalEntry } from '@/app/shared/forms/journal-entries-form/journal-entries-form.component';
import { provideIcons } from '@ng-icons/core';
import {
    tablerSearch, tablerX, tablerArrowLeft, tablerCheck,
    tablerTrash, tablerPaperclip, tablerPhoto, tablerFile,
    tablerDatabase, tablerLink
} from '@ng-icons/tabler-icons';

interface Signatory {
    name?: string;
    position?: string;
    accountNo?: string;
}

interface TempBatch {
    tempBatchId?: number;
    docTypeDesc?: string;
    remarks?: string;
    amount?: number;
    tempBatchDate?: string;
}

interface CcprBatch {
    id?: number;
    status?: boolean;
    createdAt?: string;
}

interface SourceDocument {
    id?: number;
    transactionId?: number;
    localCode?: string;
    code?: string;
    netAmount?: number;
    totalReturnedAmount?: number;
    amount?: number;
    voucherDate?: string;
    particulars?: string;
    remarks?: string;
}

interface SignatoryRef {
    accountNo?: string;
}

interface GeneralJournalPayload {
    id: number | null;
    voucherDate: string;
    explanation: string | null;
    payable: boolean;
    amount: number;
    tempBatchId: number | null;
    batch: { id: number } | null;
    invDocTransactionId: number | null;
    cashAdvanceLiquidation: { id: number } | null;
    generalLedgerLines: {
        code: string;
        description: string;
        accountId: number | null;
        debit: number;
        credit: number;
        hasSL: boolean;
        wTaxEntry: any;
        vatEntry: any;
    }[];
    subLedgerLines: {
        accountId: number | null;
        accountNo: number | null;
        name: string;
        debit: number;
        credit: number;
    }[];
    checker: SignatoryRef | null;
    budgetOfficer: SignatoryRef | null;
    recommendingOfficer: SignatoryRef | null;
    auditingOfficer: SignatoryRef | null;
    approvingOfficer: SignatoryRef | null;
}

@Component({
    selector: 'app-general-journal-add-edit',
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
            tablerSearch, tablerX, tablerArrowLeft, tablerCheck,
            tablerTrash, tablerPaperclip, tablerPhoto, tablerFile,
            tablerDatabase, tablerLink
        })
    ],
    templateUrl: './general-journal-add-edit.component.html'
})
export class GeneralJournalAddEditComponent {
    @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

    module    = 'General Journal';
    subModule = 'Create';
    menuLink  = 'general-journal';

    id: number | null = null;
    transId: number | null = null;
    editMode   = false;
    formSubmit = false;
    isLoading  = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    voucherDate = '';
    explanation = '';
    payable     = false;

    journalEntries: JournalEntry[] = [];

    tempBatch: TempBatch | null = null;

    ccprBatch: CcprBatch | null  = null;
    ccprBatches: CcprBatch[]     = [];
    loadingCcprBatches           = false;

    selectedDocument: SourceDocument | null = null;
    selectedIsCal: boolean      = false;
    loadingDocEntries           = false;

    stagedFiles  : File[] = [];
    existingFiles: any[]  = [];
    filesToRemove: any[]  = [];
    uploadingFiles = false;

    signatories: Record<string, Signatory | null> = {
        checker:              null,
        budgetOfficer:        null,
        recommendingApproval: null,
        auditingOfficer:      null,
        approvingOfficer:     null
    };

    signatoryLabels: { [key: string]: string } = {
        checker:              'Checked By',
        budgetOfficer:        'Budget Officer',
        recommendingApproval: 'Recommended By',
        auditingOfficer:      'Audited By',
        approvingOfficer:     'Approving Officer'
    };

    signatoryKeys = ['checker', 'budgetOfficer', 'recommendingApproval', 'auditingOfficer', 'approvingOfficer'];

    private service      = inject(GeneralJournalService);
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
                this.journalEntries = [{ account: null, debit: null, credit: null }];
                this.loadDefaultSignatories();
            }
        });
        this.loadCcprBatches();
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id!).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.transId     = data.transId ?? data.transaction?.id ?? null;
                    this.voucherDate = data.voucherDate ? new Date(data.voucherDate).toISOString().substring(0, 10) : '';
                    this.explanation = data.explanation || data.remarks || '';
                    this.payable     = data.payable     || false;
                    this.journalEntries = (data.generalLedgerLines || []).map((e: any): JournalEntry => ({
                        account: {
                            id:           e.accountId,
                            accountCode:  e.code,
                            accountTitle: e.description,
                            hasSL:        e.hasSL || false
                        },
                        debit:           Number(e.debit)  || null,
                        credit:          Number(e.credit) || null,
                        applyAllocation: e.applyAllocation || false,
                        allocationPct:   e.allocationPct   ?? null,
                        applyWht:        e.applyWht        || false,
                        wht:             e.wht             || null,
                        wTaxEntry:       e.wTaxEntry        || null,
                        vatEntry:        e.vatEntry         || null,
                        slentries:       (e.slentries || []).map((sl: any) => ({
                            entity:    null,
                            accountNo: sl.accountNo,
                            name:      sl.name,
                            debit:     Number(sl.debit)  || null,
                            credit:    Number(sl.credit) || null
                        }))
                    }));
                    if (this.journalEntries.length === 0) {
                        this.journalEntries = [{ account: null, debit: null, credit: null }];
                    }
                    this.ccprBatch = data.batch || null;
                    if (data.cashAdvanceLiquidation) {
                        this.selectedDocument = data.cashAdvanceLiquidation;
                        this.selectedIsCal    = true;
                    }
                    this.signatories = {
                        checker:              data.checker              || data.checkedBy         || null,
                        budgetOfficer:        data.budgetOfficer                                  || null,
                        recommendingApproval: data.recommendingApproval || data.recommendingOfficer || data.recommendedBy || null,
                        auditingOfficer:      data.auditingOfficer      || data.auditedBy         || null,
                        approvingOfficer:     data.approvingOfficer     || data.approvedBy        || null
                    };
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

    loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (data) => {
                if (data) {
                    this.signatories = {
                        checker:              data.checker              || data.checkedBy         || null,
                        budgetOfficer:        data.budgetOfficer                                  || null,
                        recommendingApproval: data.recommendingApproval || data.recommendingOfficer || data.recommendedBy || null,
                        auditingOfficer:      data.auditingOfficer      || data.auditedBy         || null,
                        approvingOfficer:     data.approvingOfficer     || data.approvedBy        || null
                    };
                }
            },
            error: () => {}
        });
    }

    get totalDebit(): number {
        return this.journalEntries.reduce((sum, e) => sum + (Number(e.debit) || 0), 0);
    }

    get totalCredit(): number {
        return this.journalEntries.reduce((sum, e) => sum + (Number(e.credit) || 0), 0);
    }

    get isBalanced(): boolean {
        return this.journalEntries.length > 0 && Math.abs(this.totalDebit - this.totalCredit) < 0.001;
    }

    async openTempBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseTempGLModalComponent,
                {},
                { size: 'xl', centered: true, }
            );
            if (result?.action === 'select' && result?.data) {
                this.tempBatch = result.data;
                this.journalEntries = (result.entries || []).map((e: any): JournalEntry => ({
                    account: { accountCode: e.code, accountTitle: e.description, id: e.accountId },
                    debit:   Number(e.debit) || null,
                    credit:  Number(e.credit) || null
                }));
            }
        } catch { }
    }

    clearTempBatch(): void {
        this.tempBatch = null;
    }

    loadCcprBatches(): void {
        this.loadingCcprBatches = true;
        this.service.getCcprBatchesForJv().subscribe({
            next: (data) => { this.ccprBatches = data || []; this.loadingCcprBatches = false; },
            error: () => { this.loadingCcprBatches = false; }
        });
    }

    onCcprBatchSelect(event: Event): void {
        const id = Number((event.target as HTMLSelectElement).value);
        if (!id) {
            this.clearCcprBatch();
            return;
        }
        const batch = this.ccprBatches.find(b => Number(b.id) === id);
        if (!batch) return;
        this.ccprBatch = batch;
        this.service.getCcprRequestsByBatch(batch.id!).subscribe({
            next: (requests) => {
                const accountMap = new Map<number, JournalEntry>();
                (requests || []).forEach((r: any) => {
                    const account = r.expenseAccount;
                    if (!account?.id) return;
                    const amount = (Number(r.purchaseOrder?.amount) || 0) + (Number(r.jobOrder?.amount) || 0);
                    const existing = accountMap.get(account.id);
                    if (existing) {
                        existing.debit = (Number(existing.debit) || 0) + amount;
                    } else {
                        accountMap.set(account.id, {
                            account: { id: account.id, accountCode: account.code, accountTitle: account.title, hasSL: account.hasSL || false },
                            debit:   amount || null,
                            credit:  null
                        });
                    }
                });
                this.journalEntries = Array.from(accountMap.values());
            },
            error: () => {}
        });
    }

    clearCcprBatch(): void {
        this.ccprBatch = null;
    }

    async openDocumentBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseDocumentModalComponent,
                {},
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.selectedDocument = result.data;
                this.selectedIsCal    = result.documentType === 'CAL';

                if (this.selectedIsCal) {
                    this.explanation = result.data.remarks || this.explanation;
                } else if (result.data.transactionId) {
                    this.loadingDocEntries = true;
                    this.service.getAccountSettingEntries(result.data.transactionId).subscribe({
                        next: (entries) => {
                            this.loadingDocEntries = false;
                            this.journalEntries = (entries || []).map((e: any): JournalEntry => ({
                                account: { accountCode: e.code, accountTitle: e.description, id: e.accountId },
                                debit:   Number(e.debit) || null,
                                credit:  Number(e.credit) || null
                            }));
                        },
                        error: () => { this.loadingDocEntries = false; }
                    });
                }
            }
        } catch { }
    }

    clearSelectedDocument(): void {
        this.selectedDocument = null;
        this.selectedIsCal    = false;
    }

    async openEntityBrowse(key: string): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseEntityModalComponent, { entityTypes: [1] }, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this.signatories[key] = result.data;
            }
        } catch { }
    }

    clearSignatory(key: string): void {
        this.signatories[key] = null;
    }

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
        if (this.journalEntries.length === 0) {
            this.alertService.warning(this.module, 'Validation', 'Please add at least one journal entry.');
            return;
        }
        if (!this.isBalanced) {
            this.alertService.warning(this.module, 'Validation', 'Debit and credit totals must be equal.');
            return;
        }

        this.formSubmit = true;

        const sigRef = (key: string): SignatoryRef | null => this.signatories[key]?.accountNo
            ? { accountNo: this.signatories[key]!.accountNo } : null;

        const payload: GeneralJournalPayload = {
            id:             this.editMode ? this.id : null,
            voucherDate:    this.voucherDate,
            explanation:    this.explanation    || null,
            payable:        this.payable,
            amount:         this.totalDebit,
            tempBatchId:    this.tempBatch?.tempBatchId || null,
            batch:          this.ccprBatch?.id ? { id: this.ccprBatch.id } : null,
            invDocTransactionId:    this.selectedIsCal ? null : (this.selectedDocument?.transactionId || null),
            cashAdvanceLiquidation: this.selectedIsCal && this.selectedDocument?.id
                ? { id: this.selectedDocument.id } : null,
            generalLedgerLines: this.journalEntries.map(e => ({
                code:        e.account?.accountCode  || '',
                description: e.account?.accountTitle || e.account?.accountDescription || '',
                accountId:   e.account?.id           || null,
                debit:       Number(e.debit)         || 0,
                credit:      Number(e.credit)        || 0,
                hasSL:       !!e.account?.hasSL,
                wTaxEntry:   e.wTaxEntry || null,
                vatEntry:    e.vatEntry  || null
            })),
            subLedgerLines: this.journalEntries
                .filter(e => (e.slentries || []).length > 0)
                .flatMap(e => (e.slentries || []).map(sl => ({
                    accountId: e.account?.id || null,
                    accountNo: sl.accountNo,
                    name:      sl.name,
                    debit:     Number(sl.debit)  || 0,
                    credit:    Number(sl.credit) || 0
                }))),
            checker:             sigRef('checker'),
            budgetOfficer:       sigRef('budgetOfficer'),
            recommendingOfficer: sigRef('recommendingApproval'),
            auditingOfficer:     sigRef('auditingOfficer'),
            approvingOfficer:    sigRef('approvingOfficer')
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
