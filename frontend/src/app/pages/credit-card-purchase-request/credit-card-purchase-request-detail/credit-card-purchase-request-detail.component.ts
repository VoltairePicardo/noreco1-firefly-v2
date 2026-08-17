import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CreditCardPurchaseRequestService } from '../credit-card-purchase-request.service';
import { provideIcons } from '@ng-icons/core';
import { tablerPrinter, tablerEdit, tablerArrowLeft, tablerCheck, tablerEye, tablerEyeOff } from '@ng-icons/tabler-icons';

const TERMINAL_STATUSES = ['Approved', 'Cancelled', 'Rejected'];

@Component({
    selector: 'app-credit-card-purchase-request-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FormsModule, RouterLink],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerPrinter, tablerEdit, tablerArrowLeft, tablerCheck, tablerEye, tablerEyeOff })],
    templateUrl: './credit-card-purchase-request-detail.component.html'
})
export class CreditCardPurchaseRequestDetailComponent {
    module    = 'Credit Card Purchase Request';
    subModule = 'Details';
    menuLink  = 'credit-card-purchase-request';

    id: any      = null;
    data: any    = {};
    itemDetails: any[]  = [];
    isLoading    = signal(false);
    isProcessing = signal(false);

    // Workflow
    workflowActions: any[] = [];
    selectedAction: any    = null;
    processRemarks         = '';

    // Logs
    logs: any[] = [];
    showLogs    = false;
    logsLoading = false;

    private service      = inject(CreditCardPurchaseRequestService);
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
                    this.data = data;
                    this.loadWorkflowActions();
                    if (data.purchaseOrder?.id) {
                        this.loadPoItems(data.purchaseOrder.id);
                    } else if (data.jobOrder?.id) {
                        this.loadJoItems(data.jobOrder.id);
                    }
                } else {
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Error', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    private loadPoItems(poId: number): void {
        this.service.getPurchaseOrderItems(poId).subscribe({
            next: (items) => { this.itemDetails = items || []; },
            error: () => {}
        });
    }

    private loadJoItems(joId: number): void {
        this.service.getJobOrderItems(joId).subscribe({
            next: (items) => { this.itemDetails = items || []; },
            error: () => {}
        });
    }

    loadWorkflowActions(): void {
        const transId = this.data?.transaction?.id || this.data?.transId;
        if (!transId) return;
        const status = this.data?.documentStatus?.status || '';
        if (TERMINAL_STATUSES.some(s => status.includes(s))) return;
        this.service.getWorkflowActions(transId).subscribe({
            next: (actions) => {
                this.workflowActions = actions || [];
                this.selectedAction  = null;
                this.processRemarks  = '';
            },
            error: () => { this.workflowActions = []; }
        });
    }

    toggleLogs(): void {
        this.showLogs = !this.showLogs;
        if (this.showLogs && this.logs.length === 0) {
            this.loadLogs();
        }
    }

    loadLogs(): void {
        const transId = this.data?.transaction?.id || this.data?.transId;
        if (!transId) return;
        this.logsLoading = true;
        this.service.getDocumentLogs(transId).subscribe({
            next: (data) => { this.logs = data || []; this.logsLoading = false; },
            error: () => { this.logsLoading = false; }
        });
    }

    processDocument(): void {
        if (!this.selectedAction) {
            this.alertService.warning(this.module, 'Process', 'Please select an action.');
            return;
        }

        this.isProcessing.set(true);
        const payload = {
            documentId: this.data.id,
            transId:    this.data.transaction?.id || this.data.transId,
            remarks:    this.processRemarks,
            workflowActionsDto: {
                actionMapId: this.selectedAction.actionMapId,
                actionId:    this.selectedAction.actionId,
                action:      this.selectedAction.action,
                sequence:    this.selectedAction.sequence
            }
        };

        this.service.process(payload).subscribe({
            next: (res) => {
                this.isProcessing.set(false);
                if (res.success) {
                    this.alertService.success(this.module, 'Processed', '');
                    this.processRemarks = '';
                    this.loadData();
                } else {
                    this.alertService.error(this.module, 'Processing', res.failureMessage || '');
                }
            },
            error: () => {
                this.isProcessing.set(false);
                this.alertService.error(this.module, 'Processing', '');
            }
        });
    }

    isEditable(): boolean {
        const s = this.data?.documentStatus?.status || '';
        return s === 'Document Created' || s === 'For Revision';
    }

    getLogField(value: string, key: string): string {
        try { return JSON.parse(value)?.[key] || '—'; } catch { return '—'; }
    }

    print(): void {
        this.service.print(this.data.id);
    }
}
