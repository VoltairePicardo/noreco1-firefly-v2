import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, Input, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';
import { JobOrderService } from '@/app/pages/job-order/job-order.service';
import { PurchaseOrderService } from '@/app/pages/purchase-order/purchase-order.service';

@Component({
    selector: 'app-browse-purchase-request-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, FormsModule],
    templateUrl: './browse-purchase-request-modal.component.html'
})
export class BrowsePurchaseRequestModalComponent implements OnInit {
    activeModal  = inject(NgbActiveModal);
    private joSvc = inject(JobOrderService);
    private poSvc = inject(PurchaseOrderService);
    private cdr   = inject(ChangeDetectorRef);

    /** 'jo' loads FOR_REP/FOR_LAB PRs; 'po' loads FOR_PO/FOR_IT PRs */
    @Input() type: 'jo' | 'po' = 'jo';
    @Input() title = 'Browse Purchase Request';

    items: any[]   = [];
    loading        = false;
    searchQuery    = '';

    get filtered(): any[] {
        if (!this.searchQuery.trim()) return this.items;
        const q = this.searchQuery.toLowerCase();
        return this.items.filter(pr =>
            (pr.code        || '').toLowerCase().includes(q) ||
            (pr.purpose     || '').toLowerCase().includes(q) ||
            (pr.preparedBy  || '').toLowerCase().includes(q)
        );
    }

    ngOnInit(): void {
        this.loading = true;
        const loader$ = this.type === 'po'
            ? this.poSvc.getPurchaseRequestsForPo()
            : this.joSvc.getPurchaseRequestsForJo();

        loader$.subscribe({
            next: (data) => {
                this.items   = data || [];
                this.loading = false;
                this.cdr.markForCheck();
            },
            error: () => {
                this.loading = false;
                this.cdr.markForCheck();
            }
        });
    }

    select(pr: any): void {
        this.activeModal.close({ action: 'select', data: pr });
    }
}
