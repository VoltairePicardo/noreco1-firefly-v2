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
import { AccountsPayableVoucherService } from '../accounts-payable-voucher.service';
import { provideIcons } from '@ng-icons/core';
import { tablerCheck, tablerEdit, tablerEye, tablerEyeOff, tablerPrinter, tablerArrowLeft, tablerX, tablerChevronDown, tablerChevronRight } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-accounts-payable-voucher-detail',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        DocumentLogsComponent,
        AttachmentsComponent
    ],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerCheck, tablerEdit, tablerEye, tablerEyeOff, tablerPrinter, tablerArrowLeft, tablerX, tablerChevronDown, tablerChevronRight })],
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
                this.isLoading.set(false);
                if (data?.id) {
                    this.data               = data;
                    this.receivingReports   = data.receivingReports   || [];
                    this.linkedPr           = data.linkedPr            || null;
                    this.linkedJoa          = data.linkedJoa           || null;
                    this.iemopBillings      = data.iemopBillings      || [];
                    this.installmentDetails = data.installmentDetails  || [];
                    this.unpaidRemark       = data.unpaidRemark        || '';
                    this.loadWorkflowActions();
                    if (data.transId) {
                        this.loadJournalEntries(data.transId);
                        this.loadAttachments(data.transId);
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
            next: (data) => { this.journalEntries = data || []; },
            error: ()    => { this.journalEntries = []; }
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
        if (!transId) return;
        this.service.getWorkflowActions(transId).subscribe({
            next: (actions) => {
                this.workflowActions = actions || [];
                this.selectedAction  = null;
            },
            error: () => {
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
        return status === 'Document Created' || status === 'For Revision';
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
