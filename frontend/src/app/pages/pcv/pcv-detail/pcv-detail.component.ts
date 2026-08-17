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
    workflowActions : any[]  = [];
    selectedAction  : any    = null;
    remarks                  = '';
    formSubmit               = false;

    // Logs
    showLogs    = false;
    logs        = signal<any[]>([]);
    logsLoading = signal(false);

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

    // ─── Workflow actions ─────────────────────────────────────────────

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
        this.formSubmit = true;
        const transId = this.data?.transId || this.data?.transaction?.id;
        const payload = {
            documentId:         this.data.id,
            transId:            transId,
            workflowActionsDto: this.selectedAction,
            remarks:            this.remarks || ''
        };
        this.service.process(payload).subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Processed.', '');
                    this.loadData();
                } else {
                    this.alertService.error(this.module, 'Process failed.', res?.failureMessage || '');
                }
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'Process Error', ''); }
        });
    }

    // ─── Print ────────────────────────────────────────────────────────

    print(): void {
        this.service.print(this.id);
    }

    // ─── Logs ─────────────────────────────────────────────────────────

    toggleLogs(): void {
        this.showLogs = !this.showLogs;
        if (this.showLogs && this.logs().length === 0) {
            this.loadLogs();
        }
    }

    loadLogs(): void {
        const transId = this.data?.transaction?.id;
        if (!transId) return;
        this.logsLoading.set(true);
        this.service.getLogs(transId).subscribe({
            next: (logs) => { this.logs.set(Array.isArray(logs) ? logs : []); this.logsLoading.set(false); },
            error: () => this.logsLoading.set(false)
        });
    }

    parseLogValue(newValue: string): any {
        try { return JSON.parse(newValue); } catch { return {}; }
    }

    // ─── Cash flow ────────────────────────────────────────────────────

    loadCashFlowData(): void {
        this.cashFlowDetails = this.data?.cashFlowDetails || this.data?.budgetDetails || [];
        const budgetLineItemDetailId = this.data?.budgetLineItemDetail?.id;
        if (this.data?.isDocumentForCashFlowItemAssignment && budgetLineItemDetailId) {
            this.loadCashFlowBalances(budgetLineItemDetailId);
            this.loadCashFlowItems();
        }
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
