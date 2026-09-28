import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { SharedModule } from '@/app/shared/shared.module';
import { ReturnMemorandumReceiptService } from '../return-memorandum-receipt.service';
import { provideIcons } from '@ng-icons/core';
import { tablerPrinter, tablerEdit, tablerArrowLeft } from '@ng-icons/tabler-icons';
import { ReturnMemorandumReceiptDto } from '@/app/models/inventory-modules/memorandum-receipt.model';
import { WorkflowAction } from '@/app/models/workflow-action.model';
import { AnyJSONService } from '@/app/shared/services/any-json.service';
import { DocumentLogsComponent } from '@/app/shared/components/document-logs/document-logs.component';

const TERMINAL_STATUSES = ['Approved', 'Denied', 'Cancelled'];

@Component({
    selector: 'app-return-memorandum-receipt-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule, FormsModule, RouterLink, DocumentLogsComponent],
    templateUrl: './return-memorandum-receipt-detail.component.html',
    providers: [provideIcons({ tablerPrinter, tablerEdit, tablerArrowLeft })],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReturnMemorandumReceiptDetailComponent implements OnInit {
    module    = 'Return Memorandum Receipt';
    subModule = 'Details';
    menuLink  = 'return-memorandum-receipt';

    id: number | null = null;
    data      = signal<Partial<ReturnMemorandumReceiptDto>>({});
    isLoading = signal(false);

    workflowActions    = signal<WorkflowAction[]>([]);
    selectedAction: WorkflowAction | null = null;
    remarks            = '';
    processingWorkflow = signal(false);

    showLogs = false;

    private service       = inject(ReturnMemorandumReceiptService);
    private route          = inject(ActivatedRoute);
    private router         = inject(Router);
    private alertService   = inject(AlertService);
    private anyJSONService = inject(AnyJSONService);

    transactionId = computed<number | undefined>(() => this.data()?.transaction?.id);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            if (idParam != null && /^\d+$/.test(idParam)) {
                this.id = Number(idParam);
                this.loadData();
            }
        });
    }

    loadData(): void {
        if (this.id == null) return;
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) { this.data.set(data); this.loadWorkflowActions(); }
                else { this.alertService.error(this.module, 'Not Found', ''); this.router.navigate(['/' + this.menuLink]); }
            },
            error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Error.', ''); this.router.navigate(['/' + this.menuLink]); }
        });
    }

    loadWorkflowActions(): void {
        const transactionId = this.transactionId();
        if (!transactionId || this.isTerminal()) {
            this.workflowActions.set([]);
            this.selectedAction  = null;
            return;
        }
        this.anyJSONService.getWorkflowActions(transactionId).subscribe({
            next: (a) => { this.workflowActions.set(a || []); this.selectedAction = null; this.remarks = ''; },
            error: () => { this.workflowActions.set([]); }
        });
    }

    isTerminal(): boolean { return TERMINAL_STATUSES.includes(this.data()?.documentStatus?.status || ''); }
    isEditable(): boolean { const s = this.data()?.documentStatus?.status || ''; return s === 'Document Created' || s === 'For Revision'; }

    processWorkflow(): void {
        if (!this.selectedAction || this.data().id == null) return;
        this.processingWorkflow.set(true);
        this.service.process({
            documentId: this.data().id,
            remarks: this.remarks,
            workflowActionsDto: {
                    actionMapId: this.selectedAction.actionMapId
                }
        }).subscribe({
            next: (res) => {
                this.processingWorkflow.set(false);
                if (res?.success) {
                    this.workflowActions.set([]);
                    this.selectedAction = null;
                    this.remarks = '';
                    this.alertService.success(this.module, res.successMessage || 'Processed.', ''); this.loadData();
                } else {
                    this.alertService.error(this.module, res?.failureMessage || 'Failed.', '');
                }},
            error: () => {
                this.processingWorkflow.set(false);
                this.alertService.error(this.module, 'Error.', '');
            }
        });
    }

    toggleLogs(): void {
        this.showLogs = !this.showLogs;
    }

    print(): void { if (this.data().id != null) this.service.print(this.data().id!); }
}
