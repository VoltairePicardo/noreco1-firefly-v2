import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseCashflowItemModalComponent } from '@/app/shared/modals/browse-cashflow-item-modal/browse-cashflow-item-modal.component';
import { PcvService } from '../pcv.service';
import { DocumentLogsComponent } from '@/app/shared/components/document-logs/document-logs.component';
import { AttachmentsComponent } from '@/app/shared/components/attachments/attachments.component';

@Component({
    selector: 'app-pcv-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, DocumentLogsComponent, AttachmentsComponent],
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

    // Logs & Attachments
    showLogs    = false;
    attachments : any[] = [];

    // Cash flow
    cashFlowDetails     : any[]         = [];
    cashFlowItems       : any[]         = [];
    cashFlowBalancePOJO : number | null = null;
    cashFlowBalanceCV   : number | null = null;

    private service      = inject(PcvService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);
    private modalService = inject(ModalService);

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
                if (data?.id) {
                    this.data = data;
                    this.items.set(data.pettyCashTransDetails || []);
                    this.loadWorkflowActions();
                    this.loadCashFlowData();
                    if (data.transId) { this.loadAttachments(data.transId); }
                } else {
                    this.isLoading.set(false);
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
        return status === 'Document Created' || status === 'Returned to Creator' || status === 'For Checking';
    }

    // ─── Workflow ─────────────────────────────────────────────────────

    loadWorkflowActions(): void {
        const transId = this.data?.transId || this.data?.transaction?.id;
        if (!transId) { this.isLoading.set(false); return; }
        this.service.getWorkflowActions(transId).subscribe({
            next: (actions) => { this.isLoading.set(false); this.workflowActions = actions || []; this.selectedAction = null; this.remarks = ''; },
            error: () => { this.isLoading.set(false); this.workflowActions = []; }
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
    }

    // ─── Attachments ──────────────────────────────────────────────────

    fileUrl = (fileId: number): string => this.service.fileUrl(fileId);

    loadAttachments(transId: number): void {
        this.service.getFiles(transId).subscribe({
            next: (files) => { this.attachments = files || []; },
            error: () => {}
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
            transId:                     this.data?.transaction?.id,
            cashFlowItemBalancePOJORFP:  this.cashFlowBalancePOJO,
            cashFlowItemBalanceCV:       this.cashFlowBalanceCV,
            cashFlowItemTotal:           this.cashFlowTotal,
            pettyCashTransBudgetDetails: this.cashFlowDetails.map((d: any) => ({
                cashflowItem: { id: d.cashflowItem?.id },
                amount:       d.amount
            }))
        };
        this.service.saveCashFlowItems(payload).subscribe({
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
