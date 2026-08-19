import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { SharedModule } from '@/app/shared/shared.module';
import { FormsModule } from '@angular/forms';
import { provideIcons } from '@ng-icons/core';
import {
    tablerArrowLeft, tablerEdit, tablerCheck,
    tablerEye, tablerEyeOff
} from '@ng-icons/tabler-icons';
import { ProjectService } from '../project.service';

const TERMINAL_STATUSES = ['Approved', 'Denied', 'Cancelled'];

@Component({
    selector: 'app-project-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule, FormsModule],
    providers: [provideIcons({ tablerArrowLeft, tablerEdit, tablerCheck, tablerEye, tablerEyeOff })],
    templateUrl: './project-detail.component.html'
})
export class ProjectDetailComponent {
    module    = 'Project';
    subModule = 'Details';
    menuLink  = 'project';

    id: any   = 0;
    data: any = {};
    isLoading = signal(false);

    attachments: any[] = [];

    workflowActions:   any[] = [];
    selectedAction:    any   = null;
    remarks            = '';
    processingWorkflow = false;

    logs        : any[] = [];
    showLogs    = false;
    logsLoading = false;

    private service      = inject(ProjectService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.id = params.get('id');
            if (this.id && /^\d+$/.test(String(this.id))) this.loadData();
        });
    }

    loadData(reloadLogs = false): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.data = data;
                    if (!reloadLogs) {
                        this.logs     = [];
                        this.showLogs = false;
                    }
                    this.loadAttachments();
                    this.loadWorkflowActions();
                    if (reloadLogs) {
                        this.logs = [];
                        this.loadLogs();
                    }
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

    loadAttachments(): void {
        this.service.getFiles(this.id).subscribe({
            next: (files) => { this.attachments = files || []; },
            error: () => { this.attachments = []; }
        });
    }

    fileUrl(fileId: number): string {
        return this.service.fileUrl(fileId);
    }

    loadWorkflowActions(): void {
        if (!this.data?.transaction?.id || this.isTerminal()) return;
        this.service.getWorkflowActions(this.data.transaction.id).subscribe({
            next: (actions) => {
                this.workflowActions = actions || [];
                this.selectedAction  = null;
                this.remarks         = '';
            },
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

    processWorkflow(): void {
        if (!this.selectedAction) return;
        this.processingWorkflow = true;
        const payload = {
            documentId:         this.data.id,
            transId:            this.data.transaction?.id,
            workflowActionsDto: this.selectedAction,
            remarks:            this.remarks || ''
        };
        this.service.process(payload).subscribe({
            next: (res) => {
                this.processingWorkflow = false;
                if (res?.success) {
                    this.alertService.success(this.module, res.successMessage || 'Processed.', '');
                    this.loadData(this.showLogs);
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

    toggleLogs(): void {
        this.showLogs = !this.showLogs;
        if (this.showLogs && this.logs.length === 0 && !this.logsLoading) {
            this.loadLogs();
        }
    }

    loadLogs(): void {
        if (!this.data?.transaction?.id || this.logsLoading) return;
        this.logsLoading = true;
        this.service.getDocumentLogs(this.data.transaction.id).subscribe({
            next: (logs) => { this.logs = logs || []; this.logsLoading = false; },
            error: () => { this.logsLoading = false; }
        });
    }

    print(): void { this.service.print(this.data.id); }
}
