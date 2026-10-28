import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseCashflowItemModalComponent } from '@/app/shared/modals/browse-cashflow-item-modal/browse-cashflow-item-modal.component';
import { DocumentLogsComponent } from '@/app/shared/components/document-logs/document-logs.component';
import { CaService } from '../ca.service';
import Swal from 'sweetalert2';

@Component({
    selector: 'app-ca-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, DocumentLogsComponent],
    providers: [],
    templateUrl: './ca-detail.component.html'
})
export class CaDetailComponent {
    module    = 'Cash Advance';
    subModule = 'Details';
    menuLink  = 'ca';

    id: any = null;
    data: any = {};
    isLoading          = signal(false);
    processingWorkflow = false;
    settingLiquidated  = false;

    // Workflow
    workflowActions: any[] = [];
    selectedAction: any    = null;
    remarks = '';

    // Logs
    showLogs = false;

    // Cash flow
    cashFlowDetails     : any[]         = [];
    cashFlowItems       : any[]         = [];
    cashFlowBalancePOJO : number | null = null;
    cashFlowBalanceCV   : number | null = null;
    formSubmit = false;

    private service      = inject(CaService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);
    private modalService = inject(ModalService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            if (idParam && /^\d+$/.test(idParam)) {
                this.id = Number(idParam);
                this.load();
            }
        });
    }

    load(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.data = data;
                    this.loadWorkflowActions();
                    this.loadCashFlowData();
                } else {
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Load Error', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
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
                    this.alertService.success(this.module, 'Processed', '');
                    this.load();
                } else {
                    this.alertService.error(this.module, 'Process', res?.failureMessage || '');
                }
            },
            error: () => { this.processingWorkflow = false; this.alertService.error(this.module, 'Process Error', ''); }
        });
    }

    // ─── Print ────────────────────────────────────────────────────────

    print(): void {
        this.service.print(this.id);
    }

    // ─── Status checks ────────────────────────────────────────────────

    isEditable(): boolean {
        const s = (this.data?.documentStatus?.status || this.data?.status || '').toString();
        return s === 'Document Created' || s === 'Returned to Creator';
    }

    isApproved(): boolean {
        const s = (this.data?.documentStatus?.status || this.data?.status || '').toLowerCase();
        return s.includes('approved') && !s.includes('not') && !s.includes('returned');
    }

    isLiquidated(): boolean {
        return !!this.data?.isLiquidated;
    }

    canBeSetAsLiquidated(): boolean {
        return this.isApproved() && !this.isLiquidated();
    }

    // ─── Set as liquidated ────────────────────────────────────────────

    async setAsLiquidated(): Promise<void> {
        const result = await Swal.fire({
            title: 'Confirm',
            text: `Are you sure you want to set ${this.data?.code} as Liquidated? This action cannot be undone.`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Yes',
            cancelButtonText: 'Cancel'
        });
        if (!result.isConfirmed) return;

        this.settingLiquidated = true;
        this.service.setAsLiquidated(this.id).subscribe({
            next: (res) => {
                this.settingLiquidated = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Set as Liquidated.', '');
                    this.load();
                } else {
                    this.alertService.error(this.module, 'Failed', res?.failureMessage || '');
                }
            },
            error: () => { this.settingLiquidated = false; this.alertService.error(this.module, 'Error', ''); }
        });
    }

    // ─── Logs ─────────────────────────────────────────────────────────

    toggleLogs(): void {
        this.showLogs = !this.showLogs;
    }

    // ─── Particulars ──────────────────────────────────────────────────

    get particulars(): any[] {
        return this.data?.cashAdvanceParticulars || [];
    }

    rowTotal(p: any): number {
        return (Number(p.quantity) || 0) * (Number(p.amount) || 0);
    }

    get grandTotal(): number {
        return this.particulars.reduce((sum, p) => sum + this.rowTotal(p), 0);
    }

    // ─── Cash flow ────────────────────────────────────────────────────

    loadCashFlowData(): void {
        const transactionId = this.data?.transId || this.data?.transaction?.id;
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

        this.service.getCashFlowDetails(this.data.id).subscribe({
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

    async openCashFlowBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseCashflowItemModalComponent,
                { items: this.cashFlowItems },
                { size: 'lg', centered: true, scrollable: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.cashFlowDetails.push({ cashflowItem: result.data, amount: 0 });
            }
        } catch { }
    }

    removeCashFlowRow(index: number): void {
        this.cashFlowDetails.splice(index, 1);
    }

    saveCashFlowItems(): void {
        this.formSubmit = true;
        const payload = {
            transId:                    this.data?.transId || this.data?.transaction?.id,
            cashFlowItemBalancePOJORFP: this.cashFlowBalancePOJO,
            cashFlowItemBalanceCV:      this.cashFlowBalanceCV,
            cashFlowItemTotal:          this.cashFlowTotal,
            cashAdvanceBudgetDetails:   this.cashFlowDetails.map((d: any) => ({
                cashflowItem: { id: d.cashflowItem?.id },
                amount:       d.amount
            }))
        };
        this.service.saveCashFlowItems(payload).subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Cash flow items saved.', '');
                    this.load();
                } else {
                    this.alertService.error(this.module, 'Save failed.', res?.failureMessage || '');
                }
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'Error.', ''); }
        });
    }
}
