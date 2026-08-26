import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { SharedModule } from '@/app/shared/shared.module';
import { ItemTestingService } from '../item-testing.service';
import { ItemTestingDto } from '@/app/models/inventory-modules/item-testing.model';
import { WorkflowAction } from '@/app/models/workflow-action.model';
import { DocumentLog } from '@/app/models/shared/document.model';

const TERMINAL_STATUSES = ['Approved', 'Denied', 'Cancelled'];

@Component({
    selector: 'app-item-testing-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule, FormsModule, RouterLink],
    templateUrl: './item-testing-detail.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ItemTestingDetailComponent implements OnInit {
    module    = 'Item Testing';
    subModule = 'Details';
    menuLink  = 'item-testing';

    id: string | null = null;
    data      = signal<ItemTestingDto>({} as ItemTestingDto);
    isLoading = signal(false);

    workflowActions    = signal<WorkflowAction[]>([]);
    selectedAction: WorkflowAction | null = null;
    remarks            = '';
    processingWorkflow = signal(false);
    logs               = signal<DocumentLog[]>([]);
    showLogs           = false;
    logsLoading        = signal(false);

    private service       = inject(ItemTestingService);
    private route         = inject(ActivatedRoute);
    private router        = inject(Router);
    private alertService  = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.id = params.get('id');
            if (this.id && /^\d+$/.test(this.id)) this.loadData();
        });
    }

    loadData(): void {
        if (!this.id) return;
        this.isLoading.set(true);
        this.service.getData(Number(this.id)).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) { this.data.set(data); this.loadWorkflowActions(); }
                else { this.alertService.error(this.module, 'Not Found', ''); this.router.navigate(['/' + this.menuLink]); }
            },
            error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Error.', ''); this.router.navigate(['/' + this.menuLink]); }
        });
    }

    loadWorkflowActions(): void {
        const transId = this.data()?.transId;
        if (!transId || this.isTerminal()) return;
        this.service.getWorkflowActions(transId).subscribe({
            next: (a) => { this.workflowActions.set(a || []); },
            error: () => { this.workflowActions.set([]); }
        });
    }

    isTerminal(): boolean { return TERMINAL_STATUSES.includes(this.data()?.documentStatus?.status || ''); }
    isEditable(): boolean { const s = this.data()?.documentStatus?.status || ''; return s === 'Document Created' || s === 'For Revision'; }

    processWorkflow(): void {
        if (!this.selectedAction) return;
        this.processingWorkflow.set(true);
        this.service.process({ documentId: this.data().id, remarks: this.remarks, workflowActionsDto: { actionMapId: this.selectedAction.actionMapId } }).subscribe({
            next: (res) => {
                this.processingWorkflow.set(false);
                if (res?.success) { this.alertService.success(this.module, res.successMessage || 'Processed.', ''); this.loadData(); }
                else { this.alertService.error(this.module, res?.failureMessage || 'Failed.', ''); }
            },
            error: () => { this.processingWorkflow.set(false); this.alertService.error(this.module, 'Error.', ''); }
        });
    }

    toggleLogs(): void {
        this.showLogs = !this.showLogs;
        const transId = this.data()?.transId;
        if (this.showLogs && this.logs().length === 0 && transId) {
            this.logsLoading.set(true);
            this.service.getDocumentLogs(transId).subscribe({
                next: (l) => { this.logs.set(l || []); this.logsLoading.set(false); },
                error: () => { this.logsLoading.set(false); }
            });
        }
    }

    print(): void { this.service.print(this.data().id); }

    getTotalUnitsReceived(): number {
        return (this.data().itemTestingDetails || []).reduce((sum, d) => sum + (Number(d.unitsReceivedQuantity) || 0), 0);
    }

    getTotalAccepted(): number {
        return (this.data().itemTestingDetails || []).reduce((sum, d) => sum + (Number(d.quantityReceived) || 0), 0);
    }

    getTotalRejected(): number {
        return (this.data().itemTestingDetails || []).reduce((sum, d) => sum + (Number(d.unitsRejectedQuantity) || 0), 0);
    }
}
