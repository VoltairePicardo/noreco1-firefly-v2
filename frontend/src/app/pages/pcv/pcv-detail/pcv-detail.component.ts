import { Component, TemplateRef, ViewChild, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { PcvService } from '../pcv.service';

@Component({
    selector: 'app-pcv-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './pcv-detail.component.html'
})
export class PcvDetailComponent {
    module    = 'Petty Cash Voucher';
    subModule = 'Detail';
    menuLink  = 'pcv';

    id: any   = 0;
    data: any = {};
    items     = signal<any[]>([]);
    isLoading = signal(false);

    // Workflow
    workflowActions    : any[] = [];
    selectedAction     : any   = null;
    remarks                    = '';
    processingWorkflow         = false;

    // Cash flow save
    formSubmit = false;

    // Logs
    logs        : any[] = [];
    showLogs            = false;
    logsLoading         = false;

    // Cash flow
    @ViewChild('cashFlowBrowseModal') cashFlowBrowseModalRef!: TemplateRef<any>;
    cashFlowDetails     : any[]         = [];
    cashFlowItems       : any[]         = [];
    cashFlowBalancePOJO : number | null = null;
    cashFlowBalanceCV   : number | null = null;

    private service      = inject(PcvService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);
    private ngbModal     = inject(NgbModal);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.id = params.get('id');
            if (this.id && /^\d+$/.test(String(this.id))) {
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
                    this.items.set(data.pettyCashTransDetails || []);
                    this.loadWorkflowActions();
                    this.loadCashFlowData();
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

    get totalAmount(): number {
        return this.items().reduce((s, i) => s + (Number(i.amount) || 0), 0);
    }

    isEditable(): boolean {
        const status = this.data?.documentStatus?.status || '';
        return status === 'Document Created' || status === 'Returned to Creator';
    }

    // ─── Workflow ─────────────────────────────────────────────────────

    loadWorkflowActions(): void {
        const transId = this.data?.transId || this.data?.transaction?.id;
        if (!transId) return;
        this.service.getWorkflowActions(transId).subscribe({
            next: (actions) => { this.workflowActions = actions || []; this.selectedAction = null; this.remarks = ''; },
            error: () => { this.workflowActions = []; }
        });
    }

    processWorkflow(): void {
        if (!this.selectedAction) return;
        this.processingWorkflow = true;
        const transId = this.data?.transId || this.data?.transaction?.id;
        const payload = {
            documentId:         this.data.id,
            transId:            transId,
            workflowActionsDto: this.selectedAction,
            remarks:            this.remarks || ''
        };
        this.service.process(payload).subscribe({
            next: (res) => {
                this.processingWorkflow = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Processed.', '');
                    this.loadData();
                } else {
                    this.alertService.error(this.module, 'Process failed.', res?.failureMessage || '');
                }
            },
            error: () => { this.processingWorkflow = false; this.alertService.error(this.module, 'Process Error', ''); }
        });
    }

    // ─── Print ────────────────────────────────────────────────────────

    print(): void {
        this.service.print(this.id);
    }

    // ─── Logs ─────────────────────────────────────────────────────────

    toggleLogs(): void {
        this.showLogs = !this.showLogs;
        if (this.showLogs && this.logs.length === 0 && !this.logsLoading) {
            this.loadLogs();
        }
    }

    loadLogs(): void {
        const transId = this.data?.transId || this.data?.transaction?.id;
        if (!transId || this.logsLoading) return;
        this.logsLoading = true;
        this.service.getLogs(transId).subscribe({
            next: (logs) => { this.logs = Array.isArray(logs) ? logs : []; this.logsLoading = false; },
            error: () => { this.logsLoading = false; }
        });
    }

    // ─── Cash flow ────────────────────────────────────────────────────

    loadCashFlowData(): void {
        const transactionId = this.data?.transaction?.id;
        if (!transactionId) return;

        this.service.isDocumentForCashFlowAssignment(transactionId).subscribe({
            next: (flag) => {
                this.data.isDocumentForCashFlowItemAssignment = flag;
                if (flag) {
                    const budgetLineItemDetailId = this.data?.budgetLineItemDetail?.id;
                    if (budgetLineItemDetailId) {
                        this.loadCashFlowBalances(budgetLineItemDetailId);
                    }
                    this.loadCashFlowItems();
                }
            },
            error: () => { this.data.isDocumentForCashFlowItemAssignment = false; }
        });

        this.service.getPcvCashFlowDetails(this.data.id).subscribe({
            next: (details) => { this.cashFlowDetails = details || []; },
            error: () => { this.cashFlowDetails = []; }
        });
    }

    loadCashFlowItems(): void {
        this.service.getCashFlowItems().subscribe({
            next: (data) => { this.cashFlowItems = data || []; },
            error: () => {}
        });
    }

    loadCashFlowBalances(budgetLineItemDetailId: number): void {
        this.service.getCashFlowBalances(budgetLineItemDetailId).subscribe({
            next: (data) => {
                this.cashFlowBalancePOJO = data?.cashFlowAmountBalancePOJO ?? null;
                this.cashFlowBalanceCV   = data?.cashFlowAmountBalanceCV   ?? null;
            },
            error: () => {}
        });
    }

    get cashFlowTotal(): number {
        return this.cashFlowDetails.reduce((s, d) => s + (Number(d.amount) || 0), 0);
    }

    openCashFlowBrowse(): void {
        this.ngbModal.open(this.cashFlowBrowseModalRef, { size: 'lg', centered: true });
    }

    addCashFlowItem(item: any, modal: any): void {
        this.cashFlowDetails.push({ cashflowItem: item, amount: 0 });
        modal.close();
    }

    removeCashFlowRow(index: number): void {
        this.cashFlowDetails.splice(index, 1);
    }

    saveCashFlowItems(): void {
        this.formSubmit = true;
        this.service.saveCashFlowItems({ pcvId: this.data.id, cashFlowDetails: this.cashFlowDetails }).subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Cash flow items saved.', '');
                    this.loadData();
                } else {
                    this.alertService.error(this.module, 'Save failed.', res?.failureMessage || '');
                }
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'Error.', ''); }
        });
    }
}
