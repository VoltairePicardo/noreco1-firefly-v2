import { Component, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { SharedModule } from '@/app/shared/shared.module';
import { WorkOrderService } from '../work-order.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { WorkOrderCloseOutModalComponent } from '../work-order-close-out-modal/work-order-close-out-modal.component';

const TERMINAL_STATUSES = ['Approved', 'Denied', 'Cancelled'];

@Component({
    selector: 'app-work-order-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule, FormsModule, RouterLink],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './work-order-detail.component.html'
})
export class WorkOrderDetailComponent {
    module    = 'Work Order';
    subModule = 'Details';
    menuLink  = 'work-order';

    id: any   = 0;
    data: any = {};
    isLoading = signal(false);

    workflowActions:   any[] = [];
    selectedAction:    any   = null;
    remarks            = '';
    processingWorkflow = false;

    postedVouchers: any[] = [];

    logs        : any[] = [];
    showLogs    = false;
    logsLoading = false;

    private service      = inject(WorkOrderService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);
    private modalService = inject(ModalService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.id = params.get('id');
            if (this.id && /^\d+$/.test(String(this.id))) this.loadData();
        });
    }

    loadData(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.data           = data;
                    this.logs           = [];
                    this.showLogs       = false;
                    this.postedVouchers = [];
                    this.loadWorkflowActions();
                    this.loadPostedVouchers();
                } else {
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Error loading record.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    loadPostedVouchers(): void {
        this.service.getPostedVouchers(this.data.id).subscribe({
            next: (data) => { this.postedVouchers = data || []; },
            error: () => { this.postedVouchers = []; }
        });
    }

    loadWorkflowActions(): void {
        if (!this.data?.transId || this.isTerminal()) return;
        this.service.getWorkflowActions(this.data.transId).subscribe({
            next: (actions) => { this.workflowActions = actions || []; },
            error: () => { this.workflowActions = []; }
        });
    }

    isTerminal(): boolean {
        return TERMINAL_STATUSES.includes(this.data?.documentStatus?.status || '');
    }

    isEditable(): boolean {
        const s = this.data?.documentStatus?.status || '';
        return s === 'Document Created' || s === 'For Revision';
    }

    isCloseable(): boolean {
        return !this.data?.isClosed && this.data?.project?.documentStatus?.id === 47;
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

    async openCloseOut(): Promise<void> {
        try {
            let workOrderDetail: any = {};
            try {
                workOrderDetail = await firstValueFrom(this.service.getWorkOrderDetail(this.data.id));
            } catch (_) {}

            const result = await this.modalService.openModal(
                WorkOrderCloseOutModalComponent,
                { workOrder: this.data, workOrderDetail },
                { size: 'xl', centered: true }
            );
            if (result?.action === 'closed') {
                this.alertService.success(this.module, 'Work order closed out successfully.', '');
                this.loadData();
            }
        } catch (_) {}
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
