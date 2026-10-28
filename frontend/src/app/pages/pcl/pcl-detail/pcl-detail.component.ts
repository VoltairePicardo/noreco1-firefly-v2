import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { DocumentLogsComponent } from '@/app/shared/components/document-logs/document-logs.component';
import { AttachmentsComponent } from '@/app/shared/components/attachments/attachments.component';
import { PclService } from '../pcl.service';

@Component({
    selector: 'app-pcl-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, DocumentLogsComponent, AttachmentsComponent],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './pcl-detail.component.html'
})
export class PclDetailComponent {
    module    = 'Petty Cash Liquidation';
    subModule = 'Details';
    menuLink  = 'pcl';

    id: any = null;
    data: any = {};
    isLoading          = signal(false);
    processingWorkflow = false;

    workflowActions: any[] = [];
    selectedAction: any    = null;
    remarks = '';

    showLogs    = false;
    attachments : any[] = [];

    private service      = inject(PclService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

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
                    this.data     = data;
                    this.showLogs = false;
                    this.loadWorkflowActions();
                    const transId = data.transaction?.id;
                    if (transId) this.loadAttachments(transId);
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
        if (!transId || this.isTerminal()) {
            this.workflowActions = [];
            this.selectedAction  = null;
            return;
        }
        this.service.getWorkflowActions(transId).subscribe({
            next: (actions) => { this.workflowActions = actions || []; this.selectedAction = null; this.remarks = ''; },
            error: () => { this.workflowActions = []; }
        });
    }

    loadAttachments(transId: number): void {
        this.service.getFiles(transId).subscribe({
            next: (files) => { this.attachments = files || []; },
            error: () => {}
        });
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
                    this.workflowActions = [];
                    this.selectedAction  = null;
                    this.remarks         = '';
                    this.alertService.success(this.module, res.successMessage || 'Processed.', '');
                    this.load();
                } else {
                    this.alertService.error(this.module, res?.failureMessage || 'Process failed.', '');
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

    isTerminal(): boolean {
        const s = this.data?.documentStatus?.status || '';
        return s === 'Approved' || s === 'Cancelled';
    }

    toggleLogs(): void {
        this.showLogs = !this.showLogs;
    }

    fileUrl = (fileId: number): string => this.service.fileUrl(fileId);

    get items(): any[] {
        return this.data?.pettyCashLiquidationDetails || [];
    }

    get totalAmount(): number {
        return this.items.reduce((sum, item) => sum + (Number(item.amount) || 0), 0);
    }
}
