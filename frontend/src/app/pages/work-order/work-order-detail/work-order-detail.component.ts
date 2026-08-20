import { Component, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { SharedModule } from '@/app/shared/shared.module';
import { WorkOrderService } from '../work-order.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { WorkOrderCloseOutModalComponent } from '@/app/shared/modals/work-order-close-out-modal/work-order-close-out-modal.component';

@Component({
    selector: 'app-work-order-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule, FormsModule, RouterLink],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './work-order-detail.component.html'
})
export class WorkOrderDetailComponent {
    module    = 'Work Order';
    subModule = 'Details';
    menuLink  = 'work-order';

    id: any   = 0;
    data: any = {};
    isLoading = signal(false);

    postedVouchers: any[] = [];

    logs        : any[] = [];
    showLogs    = false;
    logsLoading = false;

    private service      = inject(WorkOrderService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);
    private modalService = inject(ModalService);

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
                    this.data           = data;
                    this.postedVouchers = [];
                    if (!reloadLogs) {
                        this.logs     = [];
                        this.showLogs = false;
                    }
                    this.loadPostedVouchers();
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

    loadPostedVouchers(): void {
        this.service.getPostedVouchers(this.data.id).subscribe({
            next: (data) => { this.postedVouchers = data || []; },
            error: () => { this.postedVouchers = []; }
        });
    }

    isEditable(): boolean {
        return !this.data?.isClosed;
    }

    isCloseable(): boolean {
        return !this.data?.isClosed && this.data?.project?.documentStatus?.id === 47;
    }

    async openCloseOut(): Promise<void> {
        try {
            let workOrderDetail: any = {};
            try {
                workOrderDetail = await firstValueFrom(this.service.getWorkOrderDetail(this.data.id));
            } catch (_) {}

            const result = await this.modalService.openModal(
                WorkOrderCloseOutModalComponent,
                { workOrder: this.data, workOrderDetail },
                { size: 'xl', centered: true }
            );
            if (result?.action === 'closed') {
                this.alertService.success(this.module, 'Work order closed out successfully.', '');
                this.loadData(this.showLogs);
            }
        } catch (_) {}
    }

    toggleLogs(): void {
        this.showLogs = !this.showLogs;
        if (this.showLogs && this.logs.length === 0 && !this.logsLoading) {
            this.loadLogs();
        }
    }

    loadLogs(): void {
        if (!this.data?.id || this.logsLoading) return;
        this.logsLoading = true;
        this.service.getLogs(this.data.id).subscribe({
            next: (logs) => { this.logs = logs || []; this.logsLoading = false; },
            error: () => { this.logsLoading = false; }
        });
    }

}
