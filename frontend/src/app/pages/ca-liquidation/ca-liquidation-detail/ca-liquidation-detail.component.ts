import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { SharedModule } from '@/app/shared/shared.module';
import { FormsModule } from '@angular/forms';
import { AlertService } from '@/app/shared/services/alert.service';
import { CaLiquidationService } from '../ca-liquidation.service';

@Component({
    selector: 'app-ca-liquidation-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule, FormsModule],
    providers: [],
    templateUrl: './ca-liquidation-detail.component.html'
})
export class CaLiquidationDetailComponent {
    module    = 'CA Liquidation';
    subModule = 'Details';
    menuLink  = 'ca-liquidation';

    id: any = null;
    data: any = {};
    isLoading          = signal(false);
    processingWorkflow = false;

    workflowActions: any[] = [];
    selectedAction: any    = null;
    remarks = '';

    logs: any[]  = [];
    showLogs     = false;
    logsLoading  = false;

    private service      = inject(CaLiquidationService);
    private route         = inject(ActivatedRoute);
    private router        = inject(Router);
    private alertService  = inject(AlertService);

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
                    this.logs = [];
                    this.showLogs = false;
                    this.loadWorkflowActions();
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

    loadWorkflowActions(): void {
        const transId = this.data?.transaction?.id;
        if (!transId) return;
        this.service.getWorkflowActions(transId).subscribe({
            next: (actions) => { this.workflowActions = actions || []; this.selectedAction = null; this.remarks = ''; },
            error: () => { this.workflowActions = []; }
        });
    }

    processWorkflow(): void {
        if (!this.selectedAction) return;
        this.processingWorkflow = true;
        const transId = this.data?.transaction?.id;
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

    print(): void {
        this.service.print(this.id);
    }

    isEditable(): boolean {
        const s = (this.data?.documentStatus?.status || '').toString();
        return s === 'Document Created' || s === 'Returned to Creator';
    }

    get items(): any[] {
        return this.data?.cashAdvanceLiquidationItems || [];
    }

    get totalOriginalAmount(): number {
        return this.items.reduce((sum, item) => sum + (Number(item.cashAdvanceParticular?.amount) || 0), 0);
    }

    get totalReturnedAmount(): number {
        return this.items.reduce((sum, item) => sum + (Number(item.amount) || 0), 0);
    }

    toggleLogs(): void {
        this.showLogs = !this.showLogs;
        if (this.showLogs && this.logs.length === 0 && !this.logsLoading) {
            this.loadLogs();
        }
    }

    loadLogs(): void {
        const transId = this.data?.transaction?.id;
        if (!transId || this.logsLoading) return;
        this.logsLoading = true;
        this.service.getDocumentLogs(transId).subscribe({
            next: (data) => { this.logs = data || []; this.logsLoading = false; },
            error: ()     => { this.logsLoading = false; }
        });
    }
}
