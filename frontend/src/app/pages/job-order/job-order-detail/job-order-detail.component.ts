import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FormsModule } from '@angular/forms';
import { provideIcons } from '@ng-icons/core';
import { tablerPrinter, tablerEdit, tablerArrowLeft, tablerCheck, tablerEye, tablerEyeOff, tablerPlus, tablerTrash } from '@ng-icons/tabler-icons';
import { AlertService } from '@/app/shared/services/alert.service';
import { forkJoin } from 'rxjs';
import { JobOrderService } from '../job-order.service';
import { SharedModule } from '@/app/shared/shared.module';

const TERMINAL_STATUSES = ['Approved', 'Denied', 'Cancelled'];

@Component({
    selector: 'app-job-order-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, SharedModule, FormsModule, RouterLink],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerPrinter, tablerEdit, tablerArrowLeft, tablerCheck, tablerEye, tablerEyeOff, tablerPlus, tablerTrash })],
    templateUrl: './job-order-detail.component.html'
})
export class JobOrderDetailComponent {
    module    = 'Job Order';
    subModule = 'Details';
    menuLink  = 'job-order';

    id: any   = 0;
    data: any = {};
    lineItems: any[] = [];
    isLoading = signal(false);

    workflowActions:   any[] = [];
    selectedAction:    any   = null;
    remarks            = '';
    processingWorkflow = false;

    logs        : any[] = [];
    showLogs    = false;
    logsLoading = false;

    // Budget sub-item assignment
    isForCashFlowAssignment = false;
    budgetDetails:    any[] = [];
    availableSubItems: any[] = [];
    selectedSubItemId: number | null = null;
    formSubmit = false;

    private service      = inject(JobOrderService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.id = params.get('id');
            if (this.id && /^\d+$/.test(String(this.id))) this.loadData();
        });
    }

    loadData(): void {
        this.isLoading.set(true);
        forkJoin({
            header:  this.service.getData(this.id),
            details: this.service.getDetails(this.id)
        }).subscribe({
            next: ({ header, details }) => {
                if (header?.id) {
                    this.data           = header;
                    this.lineItems      = details || [];
                    this.logs           = [];
                    this.showLogs       = false;
                    this.selectedAction = null;
                    this.remarks        = '';
                    this.loadWorkflowActions();
                    this.loadCashFlowData();
                } else {
                    this.isLoading.set(false);
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Error loading job order.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    loadWorkflowActions(): void {
        if (!this.data?.transId || this.isTerminal()) {
            this.isLoading.set(false);
            this.workflowActions = [];
            this.selectedAction  = null;
            return;
        }
        this.service.getWorkflowActions(this.data.transId).subscribe({
            next: (actions) => { this.isLoading.set(false); this.workflowActions = actions || []; this.selectedAction = null; this.remarks = ''; },
            error: () => { this.isLoading.set(false); this.workflowActions = []; }
        });
    }

    isTerminal(): boolean {
        return TERMINAL_STATUSES.includes(this.data?.documentStatus?.status || '');
    }

    isEditable(): boolean {
        const s = this.data?.documentStatus?.status || '';
        return s === 'Document Created' || s === 'For Revision';
    }

    processWorkflow(): void {
        if (!this.selectedAction) return;
        this.processingWorkflow = true;

        this.service.process({
            documentId:         this.data.id,
            remarks:            this.remarks,
            workflowActionsDto: { actionMapId: this.selectedAction.actionMapId }
        }).subscribe({
            next: (res) => {
                this.processingWorkflow = false;
                if (res?.success) {
                    this.workflowActions = [];
                    this.selectedAction  = null;
                    this.remarks         = '';
                    this.alertService.success(this.module, res.successMessage || 'Processed.', '');
                    this.loadData();
                } else {
                    this.alertService.error(this.module, res?.failureMessage || 'Processing failed.', '');
                }
            },
            error: () => {
                this.processingWorkflow = false;
                this.alertService.error(this.module, 'An error occurred.', '');
            }
        });
    }

    // ─── Budget Sub-Item Assignment ───────────────────────────────────────────

    private loadCashFlowData(): void {
        const transId = this.data?.transId;
        if (!transId) return;

        this.service.isDocumentForCashFlowItemAssignment(transId).subscribe({
            next: (flag) => {
                this.isForCashFlowAssignment = flag;
                if (flag) {
                    this.service.getBudgetDetails(this.data.id).subscribe({
                        next: (rows) => { this.budgetDetails = rows || []; },
                        error: () => {}
                    });
                    const budgetLineItemDetailId = this.data?.purchaseRequest?.budgetLineItemDetail?.id;
                    if (budgetLineItemDetailId) {
                        this.service.getBudgetSubItems(budgetLineItemDetailId).subscribe({
                            next: (items) => { this.availableSubItems = items || []; },
                            error: () => {}
                        });
                    }
                }
            },
            error: () => { this.isForCashFlowAssignment = false; }
        });
    }

    addSubItem(): void {
        if (!this.selectedSubItemId) return;
        const sub = this.availableSubItems.find(s => s.id === this.selectedSubItemId);
        if (!sub) return;
        this.budgetDetails.push({ budgetSubItem: sub, amount: 0 });
        this.selectedSubItemId = null;
    }

    removeSubItem(index: number): void {
        this.budgetDetails.splice(index, 1);
    }

    get budgetTotal(): number {
        return this.budgetDetails.reduce((s, d) => s + (Number(d.amount) || 0), 0);
    }

    saveCashFlowItems(): void {
        if (this.budgetDetails.length === 0) {
            this.alertService.warning(this.module, 'Please add at least one budget sub-item.', '');
            return;
        }
        this.formSubmit = true;
        const payload = {
            transId: this.data.transId,
            jobOrderBudgetDetails: this.budgetDetails.map(d => ({
                budgetSubItem: { id: d.budgetSubItem.id },
                amount: d.amount || 0
            }))
        };
        this.service.saveCashFlowItem(payload).subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Budget sub-items saved.', '');
                    this.loadData();
                } else {
                    this.alertService.error(this.module, res?.failureMessage || 'Save failed.', '');
                }
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'An error occurred.', ''); }
        });
    }

    toggleLogs(): void {
        this.showLogs = !this.showLogs;
        if (this.showLogs && this.logs.length === 0) {
            this.logsLoading = true;
            this.service.getDocumentLogs(this.data.transId).subscribe({
                next: (logs) => { this.logs = logs || []; this.logsLoading = false; },
                error: () => { this.logsLoading = false; }
            });
        }
    }

    print(): void { this.service.print(this.data.id); }
}
