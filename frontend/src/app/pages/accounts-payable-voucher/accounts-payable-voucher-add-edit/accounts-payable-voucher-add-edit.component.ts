import { Component, inject, signal, ViewChild, ElementRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { CdkTextareaAutosize } from '@angular/cdk/text-field';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { AccountsPayableVoucherService } from '../accounts-payable-voucher.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseIemopBillingModalComponent } from '@/app/shared/modals/browse-iemop-billing-modal/browse-iemop-billing-modal.component';
import { BrowseReceivingReportModalComponent } from '@/app/shared/modals/browse-receiving-report-modal/browse-receiving-report-modal.component';
import { JournalEntriesFormComponent, JournalEntry } from '@/app/shared/forms/journal-entries-form/journal-entries-form.component';
import { AttachmentFile, AttachmentsComponent } from '@/app/shared/components/attachments/attachments.component';
import { provideIcons } from '@ng-icons/core';
import { tablerCheck, tablerArrowLeft, tablerSearch, tablerPlus, tablerTrash, tablerX, tablerPaperclip } from '@ng-icons/tabler-icons';
import {SL_ENTITY_CLASSIFICATION} from '@/app/shared/utils/sl_entity-classification';

@Component({
    selector: 'app-accounts-payable-voucher-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective,
        CdkTextareaAutosize,
        JournalEntriesFormComponent,
        AttachmentsComponent
    ],
    providers: [
        provideFlatpickrDefaults(),
        ...SHARED_PROVIDERS,
        provideIcons({ tablerCheck, tablerArrowLeft, tablerSearch, tablerPlus, tablerTrash, tablerX, tablerPaperclip })
    ],
    templateUrl: './accounts-payable-voucher-add-edit.component.html'
})
export class AccountsPayableVoucherAddEditComponent {
    @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

    module    = 'Accounts Payable Voucher';
    subModule = 'Create';
    menuLink  = 'accounts-payable-voucher';

    id: any    = null;
    editMode   = false;
    formSubmit = false;
    isLoading  = signal(false);

    flatpickrOptions = {
        dateFormat: 'Y-m-d',
        altInput: true,
        altFormat: 'F j, Y'
    };

    // Form fields
    voucherDate       = '';
    invoiceDate       = '';
    dueDate           = '';
    particulars       = '';
    paymentTerm: number | null = null;
    forInstallment    = false;
    numberOfPayments: number | null = null;
    vendor: any       = null;

    // Signatories
    checker: any          = null;
    approvingOfficer: any = null;

    // Linked purchasing document
    docType: 'rr' | 'pr' | 'joa' | null = null;
    linkedRrs: any[] = [];
    linkedDocument: any = null;
    private autoFilled = false;

    get docTypeLabel(): string {
        return this.docType === 'pr'  ? 'Payment Request'
             : this.docType === 'rr'  ? 'Receiving Report'
             : this.docType === 'joa' ? 'JO Acceptance'
             : '';
    }

    // IEMOP Billings
    iemopBillings: any[] = [];

    // Journal Entries
    journalEntries: JournalEntry[] = [];

    // Installment details (legacy: apv.installmentDetails)
    installmentDetails: { dueDate: string | null; amount: number | null }[] = [];

    // Linked transaction (edit mode)
    transId: number | null = null;

    // Attachments
    newFiles: File[] = [];
    existingFiles: any[]    = [];
    filesToRemove: any[]    = [];
    attachmentItems: AttachmentFile[] = [];   // existing + staged, shown by <app-attachments>
    private tempIds    = new Map<File, number>();
    private nextTempId = -1;

    private service      = inject(AccountsPayableVoucherService);
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
                const today      = new Date().toISOString().substring(0, 10);
                this.subModule   = 'Create';
                this.voucherDate = today;
                this.invoiceDate = today;
                this.dueDate     = today;
                this.loadDefaultSignatories();
            }
        });
    }

    loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (data) => {
                if (data) {
                    this.checker          = data.checker          || data.checkedBy  || null;
                    this.approvingOfficer = data.approvingOfficer || data.approvedBy || null;
                }
            },
            error: () => {}
        });
    }

    get defaultEntity(): any {
        return this.vendor
            ? { name: this.vendor.name, accountNo: this.vendor.accountNo, vatable: this.vendor.vatable, amount: 0 }
            : undefined;
    }

    get totalDebit(): number {
        return this.journalEntries.reduce((s, e) => s + (Number(e.debit) || 0), 0);
    }

    get totalCredit(): number {
        return this.journalEntries.reduce((s, e) => s + (Number(e.credit) || 0), 0);
    }

    private toIso(d: Date): string {
        const mm = ('0' + (d.getMonth() + 1)).slice(-2);
        const dd = ('0' + d.getDate()).slice(-2);
        return `${d.getFullYear()}-${mm}-${dd}`;
    }

    /** Legacy calculateDueDate: voucher date + payment term (days). */
    calculateDueDate(): void {
        if (this.paymentTerm == null || !this.voucherDate) return;
        const d = new Date(this.voucherDate + 'T00:00:00');
        if (isNaN(d.getTime())) return;
        d.setDate(d.getDate() + Number(this.paymentTerm));
        this.dueDate = this.toIso(d);
    }

    /** Legacy resetPaymentDetail: toggling installment clears the schedule. */
    onInstallmentToggle(): void {
        this.numberOfPayments   = null;
        this.installmentDetails = [];
    }

    /** Legacy calculateNumberOfPaymentDetail + distributeInstallmentAmounts. */
    onNumberOfPaymentsChange(): void {
        const count = Math.max(0, Math.floor(Number(this.numberOfPayments) || 0));
        while (this.installmentDetails.length < count) {
            this.installmentDetails.push({ dueDate: null, amount: null });
        }
        this.installmentDetails.length = count;
        this.distributeInstallmentAmounts();
    }

    distributeInstallmentAmounts(): void {
        const count = this.installmentDetails.length;
        if (count === 0) return;
        const total  = this.totalDebit;
        const perRow = Math.round((total / count) * 100) / 100;
        let running  = 0;
        this.installmentDetails.forEach((row, i) => {
            if (i === count - 1) {
                row.amount = Math.round((total - running) * 100) / 100;
            } else {
                row.amount = perRow;
                running   += perRow;
            }
        });
    }

    /** Legacy calculateSucceedingDueDates: following rows are +1 month each from the first. */
    onFirstInstallmentDueDateChange(): void {
        const first = this.installmentDetails[0]?.dueDate;
        if (!first) return;
        const base = new Date(first + 'T00:00:00');
        if (isNaN(base.getTime())) return;
        for (let i = 1; i < this.installmentDetails.length; i++) {
            const next = new Date(base);
            next.setMonth(base.getMonth() + i);
            this.installmentDetails[i].dueDate = this.toIso(next);
        }
    }

    loadAttachments(): void {
        if (!this.transId) return;
        this.service.getFiles(this.transId).subscribe({
            next: (files) => { this.existingFiles = files || []; this.refreshAttachments(); },
            error: () => { this.existingFiles = []; this.refreshAttachments(); }
        });
    }

    fileUrl = (fileId: number): string => this.service.fileUrl(fileId);

    removeExistingFile(index: number): void {
        const [removed] = this.existingFiles.splice(index, 1);
        if (removed) this.filesToRemove.push(removed);
        this.refreshAttachments();
    }

    onRemoveAttachment(item: AttachmentFile): void {
        if (item.local) {
            this.removeFile(this.newFiles.indexOf(item.local));
        } else {
            this.removeExistingFile(this.existingFiles.findIndex(f => f.id === item.id));
        }
    }

    // staged files get a negative temp id so they never clash with saved file ids
    private refreshAttachments(): void {
        this.attachmentItems = [
            ...this.existingFiles.map(f => ({
                id: f.id,
                originalFilename: f.originalFilename || f.name || f.fileName || f.filename,
                mimeType: f.mimeType
            })),
            ...this.newFiles.map(f => {
                if (!this.tempIds.has(f)) this.tempIds.set(f, this.nextTempId--);
                return { id: this.tempIds.get(f)!, originalFilename: f.name, mimeType: f.type, local: f };
            })
        ];
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getById(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.voucherDate    = data.voucherDate   ? new Date(data.voucherDate).toISOString().substring(0, 10)   : '';
                    this.invoiceDate    = data.invoiceDate   ? new Date(data.invoiceDate).toISOString().substring(0, 10)   : '';
                    this.dueDate        = data.dueDate       ? new Date(data.dueDate).toISOString().substring(0, 10)       : '';
                    this.particulars    = data.particulars   || '';
                    this.paymentTerm    = data.paymentTerm   ?? null;
                    this.forInstallment = data.forInstallment ?? false;
                    this.numberOfPayments = data.numberOfPayments ?? null;
                    this.vendor         = data.vendor        || null;
                    this.checker        = data.checker       || null;
                    this.approvingOfficer = data.approvingOfficer || null;
                    this.linkedRrs     = data.receivingReports || [];
                    if (this.linkedRrs.length > 0) {
                        this.docType = 'rr';
                    } else if (data.linkedPr) {
                        this.docType        = 'pr';
                        this.linkedDocument = data.linkedPr;
                    } else if (data.linkedJoa) {
                        this.docType        = 'joa';
                        this.linkedDocument = data.linkedJoa;
                    }
                    this.iemopBillings = data.iemopBillings || [];
                    this.transId       = data.transId ?? null;
                    this.installmentDetails = (data.installmentDetails || []).map((d: any) => ({
                        dueDate: d.dueDate ? new Date(d.dueDate).toISOString().substring(0, 10) : null,
                        amount:  d.amount ?? null
                    }));
                    if (this.forInstallment && this.numberOfPayments == null) {
                        this.numberOfPayments = this.installmentDetails.length;
                    }
                    if (data.transId) {
                        this.loadJournalEntries(data.transId);
                        this.loadAttachments();
                    }
                } else {
                    this.alertService.error(this.module, 'Record not found.', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: (err) => {
                this.isLoading.set(false);
                this.alertService.httpError(this.module, 'Failed to load record.', err);
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    loadJournalEntries(transId: number): void {
        this.service.getJournalEntries(transId).subscribe({
            next: (data) => {
                this.journalEntries = (data || []).map((e: any): JournalEntry => ({
                    account: {
                        accountCode:  e.code || e.accountCode,
                        accountTitle: e.description || e.accountTitle,
                        id:           e.accountId,
                        hasSL:        e.hasSL || false
                    },
                    debit:     Number(e.debit)  || null,
                    credit:    Number(e.credit) || null,
                    wTaxEntry: e.wTaxEntry || null,
                    vatEntry:  e.vatEntry  || null,
                    slentries: (e.slentries || []).map((sl: any) => ({
                        entity:    null,
                        accountNo: sl.accountNo,
                        name:      sl.name,
                        debit:     Number(sl.debit)  || null,
                        credit:    Number(sl.credit) || null
                    }))
                }));
            },
            error: () => { this.journalEntries = []; }
        });
    }

    async openVendorBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent,
                { defaultClassificationId: SL_ENTITY_CLASSIFICATION.SUPPLIER },
                { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this.vendor = result.data;
            }
        } catch { }
    }

    clearVendor(): void {
        this.vendor = null;
        this.clearAutoFilled();
    }

    onDocTypeChange(): void {
        this.linkedRrs = [];
        this.linkedDocument = null;
        this.clearAutoFilled();
    }

    private clearAutoFilled(): void {
        if (this.autoFilled) {
            this.vendor      = null;
            this.invoiceDate = '';
            this.particulars = '';
            this.autoFilled  = false;
        }
    }

    private autoFillFromDoc(doc: any): void {
        if (!this.vendor && doc.slentityAccountNo) {
            this.vendor     = { accountNo: doc.slentityAccountNo, name: doc.slentityName };
            this.autoFilled = true;
        }
        if (!this.invoiceDate && doc.invoiceDate) {
            this.invoiceDate = new Date(doc.invoiceDate).toISOString().substring(0, 10);
            this.autoFilled  = true;
        }
        if (!this.particulars && doc.particulars) {
            this.particulars = doc.particulars;
            this.autoFilled  = true;
        }
        if (!this.paymentTerm && doc.paymentTerm) {
            this.paymentTerm = doc.paymentTerm;
            this.calculateDueDate();
        }
    }

    async openDocumentBrowse(): Promise<void> {
        if (!this.docType) return;
        try {
            const result = await this.modalService.openModal(
                BrowseReceivingReportModalComponent,
                { docType: this.docType },
                { size: 'xl', centered: true });
            if (result?.action === 'select' && result?.data) {
                const doc = result.data;
                if (this.docType === 'rr') {
                    if (this.linkedRrs.some(r => r.id === doc.id)) {
                        this.alertService.warning(this.module, 'Already Added', 'This RR is already linked.');
                        return;
                    }
                    this.linkedRrs.push(doc);
                    if (this.linkedRrs.length === 1) this.autoFillFromDoc(doc);
                } else {
                    this.linkedDocument = doc;
                    this.autoFillFromDoc(doc);
                }
            }
        } catch { }
    }

    removeRr(index: number): void {
        this.linkedRrs.splice(index, 1);
        if (this.linkedRrs.length === 0) this.clearAutoFilled();
    }

    clearLinkedDocument(): void {
        this.linkedDocument = null;
        this.clearAutoFilled();
    }

    async openIemopBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseIemopBillingModalComponent, {}, { size: 'xl', centered: true });
            if (result?.action === 'select' && result?.data) {
                const bill = result.data;
                const alreadyLinked = this.iemopBillings.some((b: any) => b.id === bill.id);
                if (alreadyLinked) {
                    this.alertService.warning(this.module, 'Already Added', 'This IEMOP billing is already linked.');
                    return;
                }
                this.iemopBillings.push(bill);
            }
        } catch { }
    }

    removeIemop(index: number): void {
        this.iemopBillings.splice(index, 1);
    }

    async openSignatoryBrowse(field: 'checker' | 'approvingOfficer'): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseEntityModalComponent, { entityTypes: [1] }, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this[field] = result.data;
            }
        } catch { }
    }

    clearSignatory(field: 'checker' | 'approvingOfficer'): void {
        this[field] = null;
    }

    openFilePicker(): void {
        this.fileInput.nativeElement.click();
    }

    onFileSelect(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files) {
            const allowed = ['image/jpeg', 'image/png', 'application/pdf'];
            const duplicates: string[] = [];
            Array.from(input.files).forEach(f => {
                if (!allowed.includes(f.type)) return;
                if (this.isDuplicateFile(f)) {
                    duplicates.push(f.name);
                    return;
                }
                this.newFiles.push(f);
            });
            this.refreshAttachments();
            if (duplicates.length > 0) {
                this.alertService.warning(this.module, 'Duplicate file skipped', duplicates.join(', '));
            }
        }
        input.value = '';
    }

    // same file already picked (name + size + last modified), or a saved attachment with the same name
    private isDuplicateFile(file: File): boolean {
        return this.newFiles.some(s => s.name === file.name && s.size === file.size && s.lastModified === file.lastModified)
            || this.existingFiles.some(e => (e.originalFilename || e.name || e.fileName || e.filename) === file.name);
    }

    removeFile(index: number): void {
        const [removed] = this.newFiles.splice(index, 1);
        if (removed) this.tempIds.delete(removed);
        this.refreshAttachments();
    }

    save(): void {
        if (!this.voucherDate) {
            this.alertService.warning(this.module, 'Validation', 'Please enter a voucher date.');
            return;
        }
        if (!this.vendor) {
            this.alertService.warning(this.module, 'Validation', 'Please select a vendor/supplier.');
            return;
        }
        if (!this.particulars || !this.particulars.trim()) {
            this.alertService.warning(this.module, 'Validation', 'Please enter particulars.');
            return;
        }
        if (!this.dueDate) {
            this.alertService.warning(this.module, 'Validation', 'Please enter due date.');
            return;
        }
        if (!this.checker) {
            this.alertService.warning(this.module, 'Validation', 'Please select checker.');
            return;
        }
        if (!this.approvingOfficer) {
            this.alertService.warning(this.module, 'Validation', 'Please select approving officer.');
            return;
        }

        if (this.forInstallment) {
            if (!this.numberOfPayments || this.numberOfPayments <= 0) {
                this.alertService.warning(this.module, 'Validation', 'Number of payments is required and must be at least 1 when Installment is enabled.');
                return;
            }
            for (let i = 0; i < this.installmentDetails.length; i++) {
                const row = this.installmentDetails[i];
                if (!row.dueDate) {
                    this.alertService.warning(this.module, 'Validation', `Payment #${i + 1}: Due date is required.`);
                    return;
                }
                if (!row.amount || row.amount <= 0) {
                    this.alertService.warning(this.module, 'Validation', `Payment #${i + 1}: Amount must be greater than 0.`);
                    return;
                }
            }
        }

        const lines = this.journalEntries.filter(e => e.account);
        if (lines.length === 0) {
            this.alertService.warning(this.module, 'Validation', 'Please complete journal entries.');
            return;
        }
        if (Math.round(this.totalDebit * 100) !== Math.round(this.totalCredit * 100)) {
            this.alertService.warning(this.module, 'Validation', 'Journal entries totals are not balanced.');
            return;
        }
        if (this.totalDebit <= 0) {
            this.alertService.warning(this.module, 'Validation', 'Journal entries totals must be greater than zero.');
            return;
        }
        const missingSl = lines.find(e => !e.generated && e.account?.hasSL && !(e.slentries && e.slentries.length > 0));
        if (missingSl) {
            this.alertService.warning(this.module, 'Validation', `SL entry is required for ${missingSl.account?.accountTitle || 'the selected account'}.`);
            return;
        }

        this.formSubmit = true;

        const payload: any = {
            id:               this.editMode ? this.id : null,
            transaction:      this.transId ? { id: this.transId } : null,
            voucherDate:      this.voucherDate,
            invoiceDate:      this.invoiceDate       || null,
            dueDate:          this.dueDate           || null,
            particulars:      this.particulars       || null,
            amount:           this.totalDebit,
            paymentTerm:      this.paymentTerm       ?? null,
            forInstallment:   this.forInstallment,
            numberOfPayments: this.forInstallment ? (this.numberOfPayments ?? null) : 0,
            installmentDetails: this.forInstallment
                ? this.installmentDetails.map(d => ({ dueDate: d.dueDate, amount: d.amount }))
                : [],
            vendor:           { accountNo: this.vendor.accountNo },
            checker:          { accountNo: this.checker.accountNo },
            approvingOfficer: { accountNo: this.approvingOfficer.accountNo },
            paymentRequestId: this.docType === 'pr'  ? (this.linkedDocument?.id ?? 0) : 0,
            receivingReportId: 0,
            joAcceptanceId:   this.docType === 'joa' ? (this.linkedDocument?.id ?? 0) : 0,
            receivingReports: this.docType === 'rr'  ? this.linkedRrs.map(r => ({ id: r.id })) : [],
            iemopBillings:    this.iemopBillings.map(b => ({ id: b.id })),
            generalLedgerLines: lines.map(e => ({
                code:        e.account?.accountCode  || '',
                description: e.account?.accountTitle || '',
                accountId:   e.account?.id           || null,
                debit:       Number(e.debit)         || 0,
                credit:      Number(e.credit)        || 0,
                hasSL:       !!e.account?.hasSL,
                wTaxEntry:   e.wTaxEntry || null,
                vatEntry:    e.vatEntry  || null
            })),
            subLedgerLines: lines.flatMap(e =>
                (e.slentries || []).map(sl => ({
                    accountId: e.account?.id ?? null,
                    accountNo: sl.accountNo,
                    name:      sl.name,
                    debit:     Number(sl.debit)  || 0,
                    credit:    Number(sl.credit) || 0
                }))
            )
        };

        const req$ = this.editMode
            ? this.service.update(payload, this.newFiles, this.filesToRemove)
            : this.service.create(payload, this.newFiles);

        req$.subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Saved successfully.', '');
                    this.router.navigate(['/' + this.menuLink, res.modelId, 'detail']);
                } else {
                    this.alertService.postError(this.module, 'Save failed.', res);
                }
            },
            error: (err) => {
                this.formSubmit = false;
                this.alertService.httpError(this.module, 'Save failed.', err);
            }
        });
    }
}
