import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch } from '@ng-icons/tabler-icons';
import { StockAdjustmentService } from '@/app/pages/stock-adjustment/stock-adjustment.service';

@Component({
    selector: 'app-browse-sa-item-stock-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerSearch })],
    templateUrl: './browse-sa-item-stock-modal.component.html'
})
export class BrowseSaItemStockModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private service = inject(StockAdjustmentService);
    private cdr = inject(ChangeDetectorRef);

    // Set by ModalService from openModal(Component, { locationId: X }, ...)
    locationId: number = 0;

    items: any[] = [];
    loading = false;
    searchText = '';

    ngOnInit(): void {
        this.loading = true;
        this.service.getItemStocks(this.locationId).subscribe({
            next: (data) => {
                this.items = data || [];
                this.loading = false;
                this.cdr.markForCheck();
            },
            error: () => {
                this.loading = false;
                this.cdr.markForCheck();
            }
        });
    }

    get filtered(): any[] {
        const q = this.searchText.toLowerCase();
        if (!q) return this.items;
        return this.items.filter(s =>
            s.item?.code?.toLowerCase().includes(q) ||
            s.code?.toLowerCase().includes(q) ||
            s.item?.description?.toLowerCase().includes(q) ||
            s.description?.toLowerCase().includes(q)
        );
    }

    select(itemStock: any): void {
        this.activeModal.close({ action: 'select', data: itemStock });
    }
}
