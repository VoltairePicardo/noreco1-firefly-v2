import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { ApproveVouchersService } from '../approve-vouchers.service';
import { ApproveVoucherModalComponent } from '@/app/shared/modals/approve-voucher-modal/approve-voucher-modal.component';

@Component({
    selector: 'app-approve-vouchers-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './approve-vouchers-main.component.html'
})
export class ApproveVouchersMainComponent {
    module    = 'Approve Vouchers';
    subModule = '';
    menuLink  = 'approve-vouchers';

    records     = signal<any[]>([]);
    isLoading   = signal(false);
    searchText  = '';


    page     = 1;
    pageSize = 10;

    get filteredRecords(): any[] {
        if (!this.searchText.trim()) return this.records();
        const q = this.searchText.toLowerCase();
        return this.records().filter(r =>
            (r.code         || '').toLowerCase().includes(q) ||
            (r.particulars  || '').toLowerCase().includes(q) ||
            (r.documentType || '').toLowerCase().includes(q) ||
            (r.status       || '').toLowerCase().includes(q)
        );
    }

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.filteredRecords.slice(start, start + this.pageSize);
    }

    private service      = inject(ApproveVouchersService);
    private alertService = inject(AlertService);
    private modalService = inject(ModalService);

    ngOnInit(): void {
        this.load();
    }

    load(): void {
        this.isLoading.set(true);
        this.service.getVouchers().subscribe({
            next: (data) => { this.records.set(data || []); this.page = 1; this.isLoading.set(false); },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    async openApproveModal(rec: any): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                ApproveVoucherModalComponent,
                { voucher: rec },
                { size: 'lg', centered: true }
            );
            if (result?.action === 'approved') {
                this.alertService.success(this.module, 'Voucher approved successfully.', '');
                this.load();
            }
        } catch { }
    }

    private readonly docCodeRouteMap: Record<string, string> = {
        APV:  'accounts-payable-voucher',
        CV:   'check-voucher',
        JV:   'general-journal',
        CRV:  'cash-receipts',
        SV:   'energy-sales',
        AJ:   'adjustment-journal',
        MR:   'material-issuance',
        RV:   'requisition-voucher',
        PO:   'purchase-order',
        JO:   'job-order',
        JOA:  'jo-acceptance',
        PR:   'payment-request',
        CF:   'canvass',
        OAR:  'other-account-receivable',
        CCPR: 'credit-card-purchase-request',
    };

    detailLink(rec: any): string {
        const code = rec?.documentCode || '';
        const route = this.docCodeRouteMap[code] || code.toLowerCase().replace(/_/g, '-');
        return `/${route}/${rec.id}/detail`;
    }
}
