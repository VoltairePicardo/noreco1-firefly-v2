import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { DocumentLogsComponent } from '@/app/shared/components/document-logs/document-logs.component';
import { AttachmentsComponent } from '@/app/shared/components/attachments/attachments.component';
import { JournalEntriesFormComponent, JournalEntry } from '@/app/shared/forms/journal-entries-form/journal-entries-form.component';
import { AccountsPayableVoucherService } from '../accounts-payable-voucher.service';
import { provideIcons } from '@ng-icons/core';
import { tablerCheck, tablerEdit, tablerEye, tablerEyeOff, tablerPrinter, tablerArrowLeft, tablerX, tablerChevronDown, tablerChevronRight, tablerDeviceFloppy } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-accounts-payable-voucher-detail',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        DocumentLogsComponent,
        AttachmentsComponent,
        JournalEntriesFormComponent
    ],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerCheck, tablerEdit, tablerEye, tablerEyeOff, tablerPrinter, tablerArrowLeft, tablerX, tablerChevronDown, tablerChevronRight, tablerDeviceFloppy })],
    templateUrl: './accounts-payable-voucher-detail.component.html'
})
export class AccountsPayableVoucherDetailComponent {
    module   = 'Accounts Payable Voucher';
    menuLink = 'accounts-payable-voucher';

    id: any   = null;
    data: any = {};
    receivingReports: any[]   = [];
    linkedPr: any             = null;
    linkedJoa: any            = null;
    journalEntries: any[]     = [];
    editableEntries: JournalEntry[] = [];
    savingEntries             = false;
    iemopBillings: any[]      = [];
    attachments: any[]        = [];
    expandedSl = new Set<number>();
    installmentDetails: any[] = [];
    isLoading = signal(false);
    showLogs  = false;

    // Unpaid Remarks
    unpaidRemark        = '';
    editingUnpaidRemark = false;
    savingUnpaidRemark  = false;

    // Workflow
    workflowActions: any[] = [];
    selectedAction: any    = null;
    remarks                = '';
    processingWorkflow     = false;

    private service      = inject(AccountsPayableVoucherService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            if (idParam && /^\d+$/.test(idParam)) {
                this.id = Number(idParam);
                this.loadData();
            }
        });
    }

    loadData(): void {
        this.isLoading.set(true);
        this.service.getById(this.id).subscribe({
            next: (data) => {
                if (data?.id) {
                    this.data               = data;
                    this.receivingReports   = data.receivingReports   || [];
                    this.linkedPr           = data.linkedPr            || null;
                    this.linkedJoa          = data.linkedJoa           || null;
                    this.iemopBillings      = data.iemopBillings      || [];
                    this.installmentDetails = data.installmentDetails  || [];
                    this.unpaidRemark       = data.unpaidRemark        || '';
                    if (data.transId) {
                        this.loadJournalEntries(data.transId);
                        this.loadAttachments(data.transId);
                    }
                    this.loadWorkflowActions(); // sets isLoading(false) on completion
                } else {
                    this.isLoading.set(false);
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
                this.journalEntries = data || [];
                this.editableEntries = (data || []).map((e: any): JournalEntry => ({
                    account: { id: e.accountId, accountCode: e.code, accountTitle: e.description, hasSL: e.hasSL || false },
                    debit:   Number(e.debit)  || null,
                    credit:  Number(e.credit) || null,
                    slentries: (e.slentries || []).map((sl: any) => ({
                        entity: null, accountNo: sl.accountNo, name: sl.name,
                        debit: Number(sl.debit) || null, credit: Number(sl.credit) || null
                    }))
                }));
            },
            error: () => { this.journalEntries = []; }
        });
    }

    loadAttachments(transId: number): void {
        this.service.getFiles(transId).subscribe({
            next: (files) => { this.attachments = files || []; },
            error: ()     => { this.attachments = []; }
        });
    }

    loadWorkflowActions(): void {
        const transId = this.data?.transId || this.data?.transaction?.id;
        if (!transId) { this.isLoading.set(false); return; }
        this.service.getWorkflowActions(transId).subscribe({
            next: (actions) => {
                this.isLoading.set(false);
                this.workflowActions = actions || [];
                this.selectedAction  = null;
            },
            error: () => {
                this.isLoading.set(false);
                this.workflowActions = [];
            }
        });
    }

    get totalAmount(): number {
        return this.receivingReports.reduce(
            (sum: number, rr: any) => sum + (Number(rr.netAmount) || 0),
            0
        );
    }

    isEditable(): boolean {
        const status = this.data?.documentStatus?.status || '';
        return status === 'Document Created' || status === 'Returned to Creator';
    }

    isForCheckingOrReviewing(): boolean {
        const status = this.data?.documentStatus?.status || '';
        return status === 'For Checking' || status === 'For Reviewing';
    }

    saveEntries(): void {
        if (this.savingEntries) return;
        this.savingEntries = true;
        const payload = {
            id: this.data.id,
            generalLedgerLines: this.editableEntries.map(e => ({
                code:        e.account?.accountCode  || '',
                description: e.account?.accountTitle || '',
                accountId:   e.account?.id           || null,
                debit:       Number(e.debit)         || 0,
                credit:      Number(e.credit)        || 0
            })),
            subLedgerLines: this.editableEntries
                .filter(e => (e.slentries || []).length > 0)
                .flatMap(e => (e.slentries || []).map((sl: any) => ({
                    accountId: e.account?.id || null,
                    accountNo: sl.accountNo,
                    name:      sl.name,
                    debit:     Number(sl.debit)  || 0,
                    credit:    Number(sl.credit) || 0
                })))
        };
        this.service.updateEntries(payload).subscribe({
            next: (res) => {
                this.savingEntries = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Entries saved successfully.', '');
                    this.loadData();
                } else {
                    this.alertService.error(this.module, 'Save failed.', res?.failureMessage || '');
                }
            },
            error: () => {
                this.savingEntries = false;
                this.alertService.error(this.module, 'An error occurred while saving entries.', '');
            }
        });
    }

    get journalDebitTotal(): number {
        return this.journalEntries.reduce((s: number, e: any) => s + (Number(e.debit) || 0), 0);
    }

    get journalCreditTotal(): number {
        return this.journalEntries.reduce((s: number, e: any) => s + (Number(e.credit) || 0), 0);
    }

    processWorkflow(): void {
        if (!this.selectedAction) {
            this.alertService.warning(this.module, 'Workflow', 'Please select an action.');
            return;
        }

        this.processingWorkflow = true;

        const payload = {
            documentId:         this.data.id,
            transId:            this.data.transId,
            workflowActionsDto: this.selectedAction,
            remarks:            this.remarks || ''
        };

        this.service.process(payload).subscribe({
            next: (res) => {
                this.processingWorkflow = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Processed successfully.', res.successMessage || '');
                    this.remarks = '';
                    this.loadData();
                } else {
                    this.alertService.postError(this.module, 'Processing failed.', res);
                }
            },
            error: (err) => {
                this.processingWorkflow = false;
                this.alertService.httpError(this.module, 'Processing failed.', err);
            }
        });
    }

    editUnpaidRemark(): void {
        this.editingUnpaidRemark = true;
    }

    cancelUnpaidRemark(): void {
        this.unpaidRemark       = this.data.unpaidRemark || '';
        this.editingUnpaidRemark = false;
    }

    saveUnpaidRemark(): void {
        this.savingUnpaidRemark = true;
        this.service.saveUnpaidRemarks(this.id, this.unpaidRemark).subscribe({
            next: () => {
                this.savingUnpaidRemark  = false;
                this.editingUnpaidRemark = false;
                this.data.unpaidRemark   = this.unpaidRemark;
                this.alertService.success(this.module, 'Unpaid remarks saved.', '');
            },
            error: (err) => {
                this.savingUnpaidRemark = false;
                this.alertService.httpError(this.module, 'Failed to save remarks.', err);
            }
        });
    }

    isSlExpanded(index: number): boolean {
        return this.expandedSl.has(index);
    }

    toggleSl(index: number): void {
        if (this.expandedSl.has(index)) {
            this.expandedSl.delete(index);
        } else {
            this.expandedSl.add(index);
        }
    }

    toggleLogs(): void {
        this.showLogs = !this.showLogs;
    }

    // ── Attachments (display only; managed in add-edit) ──────────────────────
    fileUrl = (fileId: number): string => this.service.fileUrl(fileId);

    print(): void {
        this.service.print(this.id);
    }
}
