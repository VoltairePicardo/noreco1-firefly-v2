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
import { BrowseAccountSettingDocModalComponent } from '@/app/shared/modals/browse-account-setting-doc-modal/browse-account-setting-doc-modal.component';
import { JournalEntriesFormComponent, JournalEntry } from '@/app/shared/forms/journal-entries-form/journal-entries-form.component';
import { provideIcons } from '@ng-icons/core';
import {
    tablerSearch, tablerX, tablerArrowLeft, tablerCheck,
    tablerTrash, tablerPaperclip, tablerPhoto, tablerFile,
    tablerDatabase, tablerLink
} from '@ng-icons/tabler-icons';

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

    id: any    = null;
    editMode   = false;
    formSubmit = false;
    isLoading  = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // Form fields
    voucherDate = '';
    explanation = '';
    payable     = false;

    // Journal entries (uses shared JournalEntry type)
    journalEntries: JournalEntry[] = [];

    // Temp GL/SL
    tempBatch: any              = null;
    tempBatches: any[]          = [];
    selectedTempId: number | null = null;
    loadingTempBatches          = false;
    showTempList                = false;

    // Credit Card Purchase Request batch
    ccprBatch: any               = null;
    ccprBatches: any[]           = [];
    loadingCcprBatches           = false;
    showCcprList                 = false;

    // Document browse
    selectedDocument: any       = null;
    selectedIsCal: boolean      = false;
    loadingDocEntries           = false;

    // Attachments (staged for upload)
    stagedFiles  : File[] = [];
    uploadingFiles = false;

    // Signatories
    signatories: { [key: string]: any } = {
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
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
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
                        recommendingApproval: data.recommendingApproval || data.recommendingOfficer || data.recommendedBy || null,
                        auditingOfficer:      data.auditingOfficer      || data.auditedBy         || null,
                        approvingOfficer:     data.approvingOfficer     || data.approvedBy        || null
                    };
                }
            },
            error: () => {}
        });
    }

    // Totals (read from entries array for validation)
    get totalDebit(): number {
        return this.journalEntries.reduce((sum, e) => sum + (Number(e.debit) || 0), 0);
    }

    get totalCredit(): number {
        return this.journalEntries.reduce((sum, e) => sum + (Number(e.credit) || 0), 0);
    }

    get isBalanced(): boolean {
        return this.journalEntries.length > 0 && Math.abs(this.totalDebit - this.totalCredit) < 0.001;
    }

    // Temp GL/SL
    openTempBrowse(): void {
        this.showTempList = !this.showTempList;
        if (this.showTempList && this.tempBatches.length === 0 && !this.loadingTempBatches) {
            this.loadingTempBatches = true;
            this.service.getTempBatches().subscribe({
                next: (data) => { this.tempBatches = data || []; this.loadingTempBatches = false; },
                error: () => { this.loadingTempBatches = false; }
            });
        }
    }

    onTempBatchSelect(event: Event): void {
        const id = Number((event.target as HTMLSelectElement).value);
        if (!id) return;
        const batch = this.tempBatches.find(b => Number(b.tempBatchId) === id);
        if (!batch) return;
        this.tempBatch    = batch;
        this.showTempList = false;
        this.selectedTempId = null;
        this.service.getTempGLEntries(batch.tempBatchId).subscribe({
            next: (entries) => {
                this.journalEntries = (entries || []).map((e: any): JournalEntry => ({
                    account: { accountCode: e.code, accountTitle: e.description, id: e.accountId },
                    debit:   Number(e.debit) || null,
                    credit:  Number(e.credit) || null
                }));
            },
            error: () => {}
        });
    }

    clearTempBatch(): void {
        this.tempBatch      = null;
        this.selectedTempId = null;
        this.showTempList   = false;
    }

    // Credit Card Purchase Request batch — selecting a batch groups its requests by
    // expense account and sums PO+JO amounts into debit lines, mirroring legacy
    // jv.js's onBatchChange().
    openCcprBrowse(): void {
        this.showCcprList = !this.showCcprList;
        if (this.showCcprList && this.ccprBatches.length === 0 && !this.loadingCcprBatches) {
            this.loadingCcprBatches = true;
            this.service.getCcprBatchesForJv().subscribe({
                next: (data) => { this.ccprBatches = data || []; this.loadingCcprBatches = false; },
                error: () => { this.loadingCcprBatches = false; }
            });
        }
    }

    onCcprBatchSelect(event: Event): void {
        const id = Number((event.target as HTMLSelectElement).value);
        if (!id) return;
        const batch = this.ccprBatches.find(b => Number(b.id) === id);
        if (!batch) return;
        this.ccprBatch    = batch;
        this.showCcprList = false;
        this.service.getCcprRequestsByBatch(batch.id).subscribe({
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
        this.ccprBatch     = null;
        this.showCcprList  = false;
    }

    // Document browse
    async openDocumentBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseAccountSettingDocModalComponent,
                { docType: '' },
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.selectedDocument = result.data;
                this.selectedIsCal    = result.docType === 'cal';

                if (this.selectedIsCal) {
                    // Legacy links the CA Liquidation directly and copies its remarks
                    // into the explanation — it does not pre-fill journal entries.
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

    // Entity browse (signatories)
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

    // Save
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

        const sigRef = (key: string) => this.signatories[key]
            ? { accountNo: this.signatories[key].accountNo } : null;

        const payload: any = {
            id:             this.editMode ? this.id : null,
            voucherDate:    this.voucherDate,
            explanation:    this.explanation    || null,
            payable:        this.payable,
            amount:         this.totalDebit,
            tempBatchId:    this.tempBatch?.tempBatchId || null,
            batch:          this.ccprBatch?.id ? { id: this.ccprBatch.id } : null,
            // Mutually exclusive, matching legacy: a CA Liquidation link replaces the
            // generic invDocTransactionId source-document link.
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
            // Top-level, matched back to its GL line by accountId — this is what the
            // backend (LedgerFacadeImpl.postGeneralLedger) actually reads; the per-line
            // `slentries` on GeneralLedgerLineDto2 is populated only on read, never on save.
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
