import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { SharedModule } from '@/app/shared/shared.module';
import { ReceivingReportService } from '../receiving-report.service';
import { provideIcons } from '@ng-icons/core';
import { tablerPrinter, tablerEdit, tablerPaperclip, tablerArrowLeft, tablerEye, tablerEyeOff, tablerCheck } from '@ng-icons/tabler-icons';

const TERMINAL_STATUSES = ['Approved', 'Denied', 'Cancelled'];

@Component({
    selector: 'app-receiving-report-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule, FormsModule, RouterLink],
    templateUrl: './receiving-report-detail.component.html',
    providers: [provideIcons({ tablerPrinter, tablerEdit, tablerPaperclip, tablerArrowLeft, tablerEye, tablerEyeOff, tablerCheck })],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReceivingReportDetailComponent implements OnInit {
    module    = 'Receiving Report';
    subModule = 'Details';
    menuLink  = 'receiving-report';

    id: any = 0;
    data = signal<any>({});
    isLoading = signal(false);

    workflowActions    = signal<any[]>([]);
    selectedAction: any = null;
    remarks             = '';
    processingWorkflow  = signal(false);

    logs        = signal<any[]>([]);
    showLogs    = false;
    logsLoading = signal(false);

    private service      = inject(ReceivingReportService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    private transactionId = computed<number | undefined>(() => this.data()?.transId ?? this.data()?.transaction?.id);

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
                if (data?.id) { this.data.set(data); this.loadWorkflowActions(); }
                else { this.alertService.error(this.module, 'Not Found', ''); this.router.navigate(['/' + this.menuLink]); }
            },
            error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Error loading record.', ''); this.router.navigate(['/' + this.menuLink]); }
        });
    }

    loadWorkflowActions(): void {
        const transactionId = this.transactionId();
        if (!transactionId || this.isTerminal()) {
            this.workflowActions.set([]);
            this.selectedAction  = null;
            return;
        }
        this.service.getWorkflowActions(transactionId).subscribe({
            next: (actions) => { this.workflowActions.set(actions || []); this.selectedAction = null; this.remarks = ''; },
            error: () => { this.workflowActions.set([]); }
        });
    }

    isTerminal(): boolean { return TERMINAL_STATUSES.includes(this.data()?.documentStatus?.status || ''); }
    isEditable(): boolean {
        const s = this.data()?.documentStatus?.status || '';
        return s === 'Document Created' || s === 'Returned to Creator';
    }

    processWorkflow(): void {
        if (!this.selectedAction) return;
        this.processingWorkflow.set(true);
        this.service.process({ documentId: this.data().id, remarks: this.remarks, workflowActionsDto: { actionMapId: this.selectedAction.actionMapId } }).subscribe({
            next: (res) => {
                this.processingWorkflow.set(false);
                if (res?.success) { this.workflowActions.set([]); this.selectedAction = null; this.remarks = ''; this.alertService.success(this.module, res.successMessage || 'Processed.', ''); this.loadData(); }
                else { this.alertService.error(this.module, 'Process', res?.failureMessage || 'Processing failed.'); }
            },
            error: () => { this.processingWorkflow.set(false); this.alertService.error(this.module, 'An error occurred.', ''); }
        });
    }

    toggleLogs(): void {
        this.showLogs = !this.showLogs;
        if (this.showLogs && this.logs().length === 0) {
            this.logsLoading.set(true);
            this.service.getDocumentLogs(this.transactionId()!).subscribe({
                next: (logs) => { this.logs.set(logs || []); this.logsLoading.set(false); },
                error: () => { this.logsLoading.set(false); }
            });
        }
    }

    print(): void { this.service.print(this.data().id); }
}
