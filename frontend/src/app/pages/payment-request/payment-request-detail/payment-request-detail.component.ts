import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { DocumentLogsComponent } from '@/app/shared/components/document-logs/document-logs.component';
import { AttachmentsComponent } from '@/app/shared/components/attachments/attachments.component';
import { PaymentRequestService } from '../payment-request.service';
import { provideIcons } from '@ng-icons/core';
import { tablerPrinter, tablerEdit, tablerEye, tablerEyeOff, tablerArrowLeft, tablerCheck } from '@ng-icons/tabler-icons';

const TERMINAL_STATUSES = ['Approved', 'Cancelled', 'Rejected'];

@Component({
    selector: 'app-payment-request-detail',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        DocumentLogsComponent,
        AttachmentsComponent,
    ],
    providers: [...SHARED_PROVIDERS,
        provideIcons({ tablerPrinter, tablerEdit, tablerEye, tablerEyeOff, tablerArrowLeft, tablerCheck })],
    templateUrl: './payment-request-detail.component.html'
})
export class PaymentRequestDetailComponent {
    module   = 'Payment Request';
    menuLink = 'payment-request';

    id: any   = null;
    data: any = {};
    lineItems: any[]          = [];
    budgetLineItems: any[]    = [];
    budgetSubItems: any[]     = [];
    attachments: any[]        = [];
    isLoading    = signal(false);
    showLogs       = false;

    // Workflow
    workflowActions: any[] = [];
    selectedAction: any    = null;
    remarks                = '';
    processingWorkflow     = false;

    private service      = inject(PaymentRequestService);
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
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.data           = data;
                    this.lineItems      = data.paymentRequestDetails || [];
                    this.budgetLineItems = data.budgetLineItemDetails || [];
                    this.budgetSubItems  = data.budgetDetails         || [];
                    this.loadWorkflowActions();
                    this.loadAttachments();
                } else {
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Error loading data', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    loadWorkflowActions(): void {
        if (!this.data?.transId) return;
        const status = this.data?.documentStatus?.status || '';
        if (TERMINAL_STATUSES.some(s => status.includes(s))) return;
        this.service.getWorkflowActions(this.data.transId).subscribe({
            next: (actions) => {
                this.workflowActions = actions || [];
                this.selectedAction  = null;
                this.remarks         = '';
            },
            error: () => { this.workflowActions = []; }
        });
    }

    loadAttachments(): void {
        this.service.getFiles(this.id).subscribe({
            next: (files) => { this.attachments = files || []; },
            error: () => {}
        });
    }

    get totalAmount(): number {
        return this.lineItems.reduce((sum: number, item: any) => sum + (Number(item.amount) || 0), 0);
    }

    isEditable(): boolean {
        const s = this.data?.documentStatus?.status || '';
        return s === 'Document Created' || s === 'For Revision';
    }

    processWorkflow(): void {
        if (!this.selectedAction) {
            this.alertService.warning(this.module, 'Workflow', 'Please select an action.');
            return;
        }

        this.processingWorkflow = true;

        const payload = {
            transId:            this.data.transId,
            documentId:         this.data.id,
            remarks:            this.remarks,
            workflowActionsDto: {
                actionMapId: this.selectedAction.actionMapId,
                actionId:    this.selectedAction.actionId,
                action:      this.selectedAction.action,
                sequence:    this.selectedAction.sequence
            }
        };

        this.service.process(payload).subscribe({
            next: (res) => {
                this.processingWorkflow = false;
                if (res.success) {
                    this.alertService.success(this.module, 'Processed', res.successMessage || '');
                    this.remarks = '';
                    this.loadData();
                } else {
                    this.alertService.error(this.module, 'Processing', res.failureMessage || '');
                }
            },
            error: () => {
                this.processingWorkflow = false;
                this.alertService.error(this.module, 'Processing', '');
            }
        });
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
