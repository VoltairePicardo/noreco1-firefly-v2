import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { SharedModule } from '@/app/shared/shared.module';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import { provideIcons } from '@ng-icons/core';
import { tablerPrinter, tablerEdit, tablerCheck } from '@ng-icons/tabler-icons';
import { RequisitionVoucherService } from '../requisition-voucher.service';
import { forkJoin } from 'rxjs';

const TERMINAL_STATUSES = ['Approved', 'Canvassed', 'Denied'];

@Component({
    selector: 'app-requisition-voucher-details',
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule, FormsModule, ReactiveFormsModule],
    providers: [provideIcons({ tablerPrinter, tablerEdit, tablerCheck })],
    templateUrl: './requisition-voucher-details.component.html'
})
export class RequisitionVoucherDetailsComponent {
    module    = 'Purchase/Work Request';
    subModule = 'Details';
    menuLink  = 'requisition-voucher';
    id: any   = 0;
    data: any = {};
    lineItems: any[] = [];
    isLoading = signal(false);

    workflowActions:  any[]   = [];
    selectedAction:   any     = null;
    remarks           = '';
    processingWorkflow = false;

    modesOfProcurement: any[] = [];
    selectedMop:        any   = null;
    settingMop          = false;

    logs        : any[] = [];
    showLogs    = false;
    logsLoading = false;

    private service      = inject(RequisitionVoucherService);
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

    loadData(reloadLogs = false): void {
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
                    if (!reloadLogs) {
                        this.logs     = [];
                        this.showLogs = false;
                    }
                    this.loadWorkflowActions();
                    this.loadModesOfProcurement();
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
                this.alertService.error(this.module, 'Error', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    loadModesOfProcurement(): void {
        if (!this.showMopSelection()) return;
        this.service.getModesOfProcurement(this.data.id).subscribe({
            next: (modes) => { this.modesOfProcurement = modes || []; },
            error: () => { this.modesOfProcurement = []; }
        });
    }

    showMopSelection(): boolean {
        const status = this.data?.documentStatus?.status || '';
        return status.includes('Reviewed') && !this.data?.modeOfProcurement;
    }

    confirmModeOfProcurement(): void {
        if (!this.selectedMop) return;
        this.settingMop = true;
        const payload = {
            documentId: this.data.id,
            transId:    this.data.transId,
            mode:       this.selectedMop,
            remarks:    ''
        };
        this.service.setModeOfProcurement(payload).subscribe({
            next: (res) => {
                this.settingMop = false;
                if (res.success) {
                    this.alertService.success(this.module, 'Mode of Procurement Set', '');
                    this.loadData(this.showLogs);
                } else {
                    this.alertService.error(this.module, 'Set MOP', res.failureMessage || '');
                }
            },
            error: () => {
                this.settingMop = false;
                this.alertService.error(this.module, 'Set MOP', '');
            }
        });
    }

    loadWorkflowActions(): void {
        if (!this.data?.transId) return;
        // Skip loading actions if document is in a terminal state
        const status = this.data?.documentStatus?.status || '';
        if (TERMINAL_STATUSES.some(s => status.includes(s))) return;

        this.service.getWorkflowActions(this.data.transId).subscribe({
            next: (actions) => {
                this.workflowActions = actions || [];
                this.selectedAction  = null;
                this.remarks         = '';
            },
            error: () => { this.workflowActions = []; }
        });
    }

    processWorkflow(): void {
        if (!this.selectedAction) return;
        this.processingWorkflow = true;
        const payload = {
            documentId:        this.data.id,
            transId:           this.data.transId,
            workflowActionsDto: this.selectedAction,
            remarks:           this.remarks || ''
        };
        this.service.process(payload).subscribe({
            next: (res) => {
                this.processingWorkflow = false;
                if (res.success) {
                    this.alertService.success(this.module, 'Processed', '');
                    this.loadData(this.showLogs);
                } else {
                    this.alertService.error(this.module, 'Process', res.failureMessage || (res.messages || []).join(', '));
                }
            },
            error: () => {
                this.processingWorkflow = false;
                this.alertService.error(this.module, 'Process', '');
            }
        });
    }

    isEditable(): boolean {
        const status = this.data?.documentStatus?.status || '';
        return status === 'Document Created' || status === 'Returned to Creator';
    }

    toggleLogs(): void {
        this.showLogs = !this.showLogs;
        if (this.showLogs && this.logs.length === 0 && !this.logsLoading) {
            this.loadLogs();
        }
    }

    loadLogs(): void {
        if (!this.data?.transId || this.logsLoading) return;
        this.logsLoading = true;
        this.service.getDocumentLogs(this.data.transId).subscribe({
            next: (logs) => { this.logs = logs || []; this.logsLoading = false; },
            error: () => { this.logsLoading = false; }
        });
    }

    getLogField(value: string, key: string): string {
        try { return JSON.parse(value)?.[key] || '—'; } catch { return '—'; }
    }

    print(): void {
        this.service.print(this.id, this.data?.rvType || '');
    }
}
