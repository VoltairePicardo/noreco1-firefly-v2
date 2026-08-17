import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { FormsModule } from '@angular/forms';
import { provideIcons } from '@ng-icons/core';
import { tablerPrinter, tablerEdit, tablerArrowLeft, tablerCheck, tablerEye, tablerEyeOff } from '@ng-icons/tabler-icons';
import { AlertService } from '@/app/shared/services/alert.service';
import { forkJoin } from 'rxjs';
import { CanvassService } from '../canvass.service';
import { SharedModule } from '@/app/shared/shared.module';

const TERMINAL_STATUSES = ['Approved', 'Denied', 'Cancelled'];

@Component({
    selector: 'app-canvass-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule, FormsModule, RouterLink],
    providers: [provideIcons({ tablerPrinter, tablerEdit, tablerArrowLeft, tablerCheck, tablerEye, tablerEyeOff })],
    templateUrl: './canvass-detail.component.html'
})
export class CanvassDetailComponent {
    module    = 'Canvass';
    subModule = 'Details';
    menuLink  = 'canvass';

    id: any   = 0;
    data: any = {};
    lineItems: any[] = [];
    isLoading = signal(false);

    workflowActions:   any[] = [];
    selectedAction:    any   = null;
    remarks            = '';
    processingWorkflow = false;

    logs        : any[] = [];
    showLogs    = false;
    logsLoading = false;

    private service      = inject(CanvassService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

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
        forkJoin({
            header:  this.service.getData(this.id),
            details: this.service.getDetails(this.id)
        }).subscribe({
            next: ({ header, details }) => {
                this.isLoading.set(false);
                if (header?.id) {
                    this.data      = header;
                    this.lineItems = details || [];
                    this.logs      = [];
                    this.showLogs  = false;
                    this.loadWorkflowActions();
                } else {
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Error loading canvass.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
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

    processWorkflow(): void {
        if (!this.selectedAction) return;
        this.processingWorkflow = true;

        const payload = {
            documentId:         this.data.id,
            remarks:            this.remarks,
            workflowActionsDto: { actionMapId: this.selectedAction.actionMapId }
        };

        this.service.process(payload).subscribe({
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

    getLogField(value: string, key: string): string {
        try { return JSON.parse(value)?.[key] || ''; } catch { return ''; }
    }

    print(): void {
        this.service.print(this.data.id);
    }

    get supplierList(): any[] {
        return this.data?.suppliers || [];
    }
}
