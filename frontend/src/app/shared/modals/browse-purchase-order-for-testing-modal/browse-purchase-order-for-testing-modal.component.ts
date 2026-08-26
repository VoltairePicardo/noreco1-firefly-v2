import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerChevronLeft, tablerChevronRight, tablerSearch } from '@ng-icons/tabler-icons';
import { PurchaseOrderService } from '@/app/pages/purchase-order/purchase-order.service';
import { PurchaseOrder } from '@/app/models/inventory-modules/purchase-order.model';

@Component({
    selector: 'app-browse-purchase-order-for-testing-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerChevronLeft, tablerChevronRight, tablerSearch })],
    templateUrl: './browse-purchase-order-for-testing-modal.component.html'
})
export class BrowsePurchaseOrderForTestingModalComponent implements OnInit {
    activeModal      = inject(NgbActiveModal);
    private service   = inject(PurchaseOrderService);

    readonly pageSize = 10;

    items   = signal<PurchaseOrder[]>([]);
    total   = signal(0);
    page    = signal(1);
    loading = signal(false);

    searchText = '';

    ngOnInit(): void {
        this.loadData();
    }

    loadData(): void {
        this.loading.set(true);
        this.service.getForItemTestingPaged(this.searchText, this.page() - 1, this.pageSize).subscribe({
            next: (res) => {
                this.items.set(res?.content ?? []);
                this.total.set(res?.totalElements ?? 0);
                this.loading.set(false);
            },
            error: () => {
                this.items.set([]);
                this.total.set(0);
                this.loading.set(false);
            }
        });
    }

    onSearchChange(): void {
        this.page.set(1);
        this.loadData();
    }

    onPageChange(page: number): void {
        this.page.set(page);
        this.loadData();
    }

    select(po: PurchaseOrder): void {
        this.activeModal.close({ action: 'select', data: po });
    }
}
