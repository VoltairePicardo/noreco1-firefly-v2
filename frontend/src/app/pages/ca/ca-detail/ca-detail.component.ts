import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { CaService } from '../ca.service';
import Swal from 'sweetalert2';

@Component({
    selector: 'app-ca-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './ca-detail.component.html'
})
export class CaDetailComponent {
    module    = 'Cash Advance';
    subModule = 'Detail';
    menuLink  = 'ca';

    id: any = null;
    data: any = {};
    isLoading  = signal(false);
    formSubmit = false;

    workflowActions: any[] = [];
    selectedAction: any    = null;
    remarks = '';

    logs        = signal<any[]>([]);
    logsLoaded  = false;
    logsLoading = false;

    private service      = inject(CaService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            if (idParam) {
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
                    this.data = data;
                    this.loadWorkflowActions();
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
        const transId = this.data?.transId || this.data?.transaction?.id;
        if (!transId) return;
        this.service.getWorkflowActions(transId).subscribe({
            next: (actions) => { this.workflowActions = actions || []; this.selectedAction = null; this.remarks = ''; },
            error: () => { this.workflowActions = []; }
        });
    }

    processWorkflow(): void {
        if (!this.selectedAction) return;
        this.formSubmit = true;
        const transId = this.data?.transId || this.data?.transaction?.id;
        const payload = {
            documentId:         this.data.id,
            transId:            transId,
            workflowActionsDto: this.selectedAction,
            remarks:            this.remarks || ''
        };
        this.service.process(payload).subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Processed', '');
                    this.load();
                } else {
                    this.alertService.error(this.module, 'Process', res?.failureMessage || '');
                }
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'Process Error', ''); }
        });
    }

    print(): void {
        this.service.print(this.id);
    }

    isApproved(): boolean {
        const s = (this.data?.documentStatus?.status || this.data?.status || '').toLowerCase();
        return s.includes('approved') && !s.includes('not') && !s.includes('returned');
    }

    isLiquidated(): boolean {
        return !!this.data?.isLiquidated;
    }

    canBeSetAsLiquidated(): boolean {
        return this.isApproved() && !this.isLiquidated();
    }

    async setAsLiquidated(): Promise<void> {
        const result = await Swal.fire({
            title: 'Confirm',
            text: `Are you sure you want to set ${this.data?.code} as Liquidated? This action cannot be undone.`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Yes',
            cancelButtonText: 'Cancel'
        });
        if (!result.isConfirmed) return;

        this.formSubmit = true;
        this.service.setAsLiquidated(this.id).subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Set as Liquidated.', '');
                    this.load();
                } else {
                    this.alertService.error(this.module, 'Failed', res?.failureMessage || '');
                }
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'Error', ''); }
        });
    }

    loadLogs(): void {
        const transId = this.data?.transId || this.data?.transaction?.id;
        if (!transId) return;
        this.logsLoaded  = true;
        this.logsLoading = true;
        this.service.getDocumentLogs(transId).subscribe({
            next:  (data) => { this.logs.set(data || []); this.logsLoading = false; },
            error: ()     => { this.logsLoading = false; }
        });
    }

    get particulars(): any[] {
        return this.data?.cashAdvanceParticulars || [];
    }

    rowTotal(p: any): number {
        return (Number(p.quantity) || 0) * (Number(p.amount) || 0);
    }

    get grandTotal(): number {
        return this.particulars.reduce((sum, p) => sum + this.rowTotal(p), 0);
    }

    isEditable(): boolean {
        const s = (this.data?.documentStatus?.status || this.data?.status || '').toString();
        return s === 'Document Created' || s === 'Returned to Creator' || s === '1';
    }
}
