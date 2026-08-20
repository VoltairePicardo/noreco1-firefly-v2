import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { SharedModule } from '@/app/shared/shared.module';
import { provideIcons } from '@ng-icons/core';
import { tablerArrowLeft, tablerEdit, tablerCheck, tablerPrinter, tablerEye, tablerEyeOff } from '@ng-icons/tabler-icons';
import { CostEstimateService } from '../cost-estimate.service';

const TERMINAL_STATUSES = ['Approved', 'Denied', 'Cancelled'];

@Component({
    selector: 'app-cost-estimate-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule, FormsModule, RouterLink],
    providers: [provideIcons({ tablerArrowLeft, tablerEdit, tablerCheck, tablerPrinter, tablerEye, tablerEyeOff })],
    templateUrl: './cost-estimate-detail.component.html'
})
export class CostEstimateDetailComponent {
    module    = 'Cost Estimate';
    subModule = 'Details';
    menuLink  = 'cost-estimate';

    id: any   = 0;
    data: any = {};
    assemblies:    any[] = [];
    detailsAccess: any[] = [];
    detailsMeter:  any[] = [];
    miscCharges:   any[] = [];
    isLoading = signal(false);

    workflowActions:   any[] = [];
    selectedAction:    any   = null;
    remarks            = '';
    processingWorkflow = false;

    logs        : any[] = [];
    showLogs    = false;
    logsLoading = false;

    private service      = inject(CostEstimateService);
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
                    this.data          = data;
                    this.assemblies    = data.costEstimateAssemblyUnits || [];
                    const allDetails: any[] = data.details || [];
                    this.detailsAccess = allDetails.filter((d: any) => d.category === 1);
                    this.detailsMeter  = allDetails.filter((d: any) => d.category === 2);
                    this.miscCharges   = data.miscellaneousCharges || [];
                    if (!reloadLogs) {
                        this.logs     = [];
                        this.showLogs = false;
                    }
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

    loadWorkflowActions(): void {
        if (!this.data?.transaction?.id || this.isTerminal()) return;
        this.service.getWorkflowActions(this.data.transaction.id).subscribe({
            next: (actions) => { this.workflowActions = actions || []; this.selectedAction = null; this.remarks = ''; },
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

        this.service.process({
            documentId:         this.data.id,
            transId:            this.data.transaction?.id,
            workflowActionsDto: this.selectedAction,
            remarks:            this.remarks || ''
        }).subscribe({
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

    get grandTotal(): number {
        if (this.data?.grandTotal != null) return Number(this.data.grandTotal);
        const sumAccess   = this.detailsAccess.reduce((s: number, d: any) => s + ((d.quantity || 0) * (d.unitCost || 0)), 0);
        const sumMeter    = this.detailsMeter.reduce((s: number, d: any) => s + ((d.quantity || 0) * (d.unitCost || 0)), 0);
        const sumMisc     = this.miscCharges.reduce((s: number, m: any) => s + ((m.unitCost || 0) * (m.quantity || m.amount || 0)), 0);
        const sumAssembly = this.assemblies.reduce((s: number, a: any) => s + ((a.laborCost || a.unitCost || 0) * (a.quantity || 0)), 0);
        return sumAccess + sumMeter + sumMisc + sumAssembly
             + Number(this.data.laborCost      || 0)
             + Number(this.data.freightHandling || 0)
             + Number(this.data.contingency     || 0);
    }

    print(): void { this.service.print(this.data.id); }
}
