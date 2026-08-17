import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { SharedModule } from '@/app/shared/shared.module';
import { MstService } from '../mst.service';
import { provideIcons } from '@ng-icons/core';
import { tablerPrinter, tablerEdit, tablerArrowLeft } from '@ng-icons/tabler-icons';

const TERMINAL_STATUSES = ['Approved', 'Denied', 'Cancelled'];

@Component({
    selector: 'app-mst-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule, FormsModule, RouterLink],
    templateUrl: './mst-detail.component.html',
    providers: [provideIcons({ tablerPrinter, tablerEdit, tablerArrowLeft })]
})
export class MstDetailComponent {
    module    = 'Material Salvage Ticket';
    subModule = 'Details';
    menuLink  = 'mst';

    id: any   = 0;
    data: any = {};
    isLoading = signal(false);

    workflowActions:   any[] = [];
    selectedAction:    any   = null;
    remarks            = '';
    processingWorkflow = false;
    logs: any[] = []; showLogs = false; logsLoading = false;

    private service      = inject(MstService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

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
                if (data?.id) { this.data = data; this.loadWorkflowActions(); }
                else { this.alertService.error(this.module, 'Not Found', ''); this.router.navigate(['/' + this.menuLink]); }
            },
            error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Error.', ''); this.router.navigate(['/' + this.menuLink]); }
        });
    }

    loadWorkflowActions(): void {
        if (!this.data?.transId || this.isTerminal()) return;
        this.service.getWorkflowActions(this.data.transId).subscribe({
            next: (a) => { this.workflowActions = a || []; }, error: () => { this.workflowActions = []; }
        });
    }

    isTerminal(): boolean { return TERMINAL_STATUSES.includes(this.data?.documentStatus?.status || ''); }
    isEditable(): boolean {
        const s = this.data?.documentStatus?.status || '';
        return s === 'Document Created' || s === 'Returned to Creator';
    }

    processWorkflow(): void {
        if (!this.selectedAction) return;
        this.processingWorkflow = true;
        this.service.process({ documentId: this.data.id, remarks: this.remarks, workflowActionsDto: { actionMapId: this.selectedAction.actionMapId } }).subscribe({
            next: (res) => {
                this.processingWorkflow = false;
                if (res?.success) { this.alertService.success(this.module, res.successMessage || 'Processed.', ''); this.loadData(); }
                else { this.alertService.error(this.module, res?.failureMessage || 'Failed.', ''); }
            },
            error: () => { this.processingWorkflow = false; this.alertService.error(this.module, 'Error.', ''); }
        });
    }

    toggleLogs(): void {
        this.showLogs = !this.showLogs;
        if (this.showLogs && this.logs.length === 0) {
            this.logsLoading = true;
            this.service.getDocumentLogs(this.data.transId).subscribe({
                next: (l) => { this.logs = l || []; this.logsLoading = false; },
                error: () => { this.logsLoading = false; }
            });
        }
    }

    print(): void { this.service.print(this.data.id); }
}
