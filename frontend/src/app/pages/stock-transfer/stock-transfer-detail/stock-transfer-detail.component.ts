import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { StockTransferService } from '../stock-transfer.service';
import { provideIcons } from '@ng-icons/core';
import { tablerArrowLeft, tablerPrinter, tablerEdit, tablerEye, tablerEyeOff, tablerCheck } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-stock-transfer-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, RouterLink],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerArrowLeft, tablerPrinter, tablerEdit, tablerEye, tablerEyeOff, tablerCheck })],
    templateUrl: './stock-transfer-detail.component.html'
})
export class StockTransferDetailComponent {
    module    = 'Stock Transfer';
    subModule = 'Detail';
    menuLink  = 'stock-transfer';

    id: any   = 0;
    data: any = {};
    isLoading = signal(false);

    // Workflow
    workflowActions: any[] = [];
    selectedAction: any    = null;
    remarks                = '';
    processingWorkflow     = false;

    // Logs
    logs        : any[] = [];
    showLogs    = false;
    logsLoading = false;

    private service      = inject(StockTransferService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.id = params.get('id');
            if (this.id && /^\d+$/.test(String(this.id))) { this.loadData(); }
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

    private get transId(): number | undefined {
        return this.data?.transId || this.data?.transaction?.id;
    }

    loadWorkflowActions(): void {
        if (!this.transId) return;
        this.service.getWorkflowActions(this.transId).subscribe({
            next: (actions) => { this.workflowActions = actions || []; this.selectedAction = null; this.remarks = ''; },
            error: () => { this.workflowActions = []; }
        });
    }

    processWorkflow(): void {
        if (!this.selectedAction) return;
        this.processingWorkflow = true;
        const payload = {
            documentId:         this.data.id,
            transId:            this.transId,
            workflowActionsDto: this.selectedAction,
            remarks:            this.remarks || ''
        };
        this.service.process(payload).subscribe({
            next: (res) => {
                this.processingWorkflow = false;
                if (res?.success) {
                    this.alertService.success(this.module, res.successMessage || 'Processed.', '');
                    this.loadData();
                } else {
                    this.alertService.error(this.module, 'Process', res?.failureMessage || 'Failed.');
                }
            },
            error: () => { this.processingWorkflow = false; this.alertService.error(this.module, 'Error.', ''); }
        });
    }

    isEditable(): boolean {
        const s = this.data?.documentStatus?.status || '';
        return s === 'Document Created' || s === 'Returned to Creator';
    }

    toggleLogs(): void {
        this.showLogs = !this.showLogs;
        if (this.showLogs && this.logs.length === 0 && !this.logsLoading) { this.loadLogs(); }
    }

    loadLogs(): void {
        if (!this.transId || this.logsLoading) return;
        this.logsLoading = true;
        this.service.getDocumentLogs(this.transId).subscribe({
            next: (logs) => { this.logs = logs || []; this.logsLoading = false; },
            error: () => { this.logsLoading = false; }
        });
    }

    print(): void { this.service.print(this.data.id); }
}
