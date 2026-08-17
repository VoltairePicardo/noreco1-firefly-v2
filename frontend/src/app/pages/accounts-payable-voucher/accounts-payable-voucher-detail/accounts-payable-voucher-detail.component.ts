import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { AccountsPayableVoucherService } from '../accounts-payable-voucher.service';
import { provideIcons } from '@ng-icons/core';
import { tablerCheck, tablerHistory, tablerEdit, tablerPrinter, tablerArrowLeft } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-accounts-payable-voucher-detail',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS
    ],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerCheck, tablerHistory, tablerEdit, tablerPrinter, tablerArrowLeft })],
    templateUrl: './accounts-payable-voucher-detail.component.html'
})
export class AccountsPayableVoucherDetailComponent {
    module   = 'Accounts Payable Voucher';
    menuLink = 'accounts-payable-voucher';

    id: any   = null;
    data: any = {};
    receivingReports: any[] = [];
    journalEntries: any[]   = [];
    iemopBillings: any[]    = [];
    isLoading = signal(false);

    // Workflow
    workflowActions: any[] = [];
    selectedAction: any    = null;
    remarks                = '';
    processingWorkflow     = false;

    // Document logs
    logs: any[]  = [];
    showLogs     = false;
    logsLoading  = false;

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
                    this.data             = data;
                    this.receivingReports = data.receivingReports || [];
                    this.iemopBillings    = data.iemopBillings    || [];
                    this.loadWorkflowActions();
                    if (data.transId) { this.loadJournalEntries(data.transId); }
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

    loadJournalEntries(transId: number): void {
        this.service.getJournalEntries(transId).subscribe({
            next: (data) => { this.journalEntries = data || []; },
            error: ()    => { this.journalEntries = []; }
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
            (sum: number, rr: any) => sum + (Number(rr.netAmount) || Number(rr['netAmount']) || 0),
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
                    this.alertService.error(this.module, 'Processing failed.', res?.failureMessage || '');
                }
            },
            error: () => {
                this.processingWorkflow = false;
                this.alertService.error(this.module, 'An error occurred while processing.', '');
            }
        });
    }

    toggleLogs(): void {
        this.showLogs = !this.showLogs;
        if (this.showLogs && this.logs.length === 0) {
            this.loadLogs();
        }
    }

    loadLogs(): void {
        if (!this.data?.transId) return;
        this.logsLoading = true;
        this.service.getDocumentLogs(this.data.transId).subscribe({
            next: (data) => {
                this.logs        = data || [];
                this.logsLoading = false;
            },
            error: () => {
                this.logsLoading = false;
            }
        });
    }

    print(): void {
        this.service.print(this.id);
    }

}
