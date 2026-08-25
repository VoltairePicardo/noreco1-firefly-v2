import { ChangeDetectionStrategy, Component, inject, Input, OnInit, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerChevronLeft, tablerChevronRight, tablerSearch } from '@ng-icons/tabler-icons';
import { Observable } from 'rxjs';
import { ItemStockService } from '@/app/shared/services/item-stock.service';
import { ItemStock } from '@/app/models/inventory-modules/item-stock.model';

export type ItemStockBrowseType = 'WITHDRAWAL' | 'ADJUSTMENT';

@Component({
    selector: 'app-browse-item-stock-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerChevronLeft, tablerChevronRight, tablerSearch })],
    templateUrl: './browse-item-stock-modal.component.html'
})
export class BrowseItemStockModalComponent implements OnInit {
    @Input() locationId!: number;
    @Input() categoryId!: number;
    // Plain @Input(), not a signal input() — ModalService.openModal wires modal inputs via a raw
    // `componentInstance[key] = value` assignment, which would clobber a signal input's function value.
    @Input() type: ItemStockBrowseType = 'WITHDRAWAL';

    activeModal                = inject(NgbActiveModal);
    private itemStockService   = inject(ItemStockService);

    readonly pageSize = 10;

    items   = signal<ItemStock[]>([]);
    total   = signal(0);
    page    = signal(1);
    loading = signal(false);

    searchText = '';

    ngOnInit(): void {
        this.loadData();
    }

    private fetchItemStocks(): Observable<any> {
        switch (this.type) {
            case 'ADJUSTMENT':
                return this.itemStockService.getItemStocksInvLocWithZeroQuantity(
                    this.locationId, this.searchText, this.page() - 1, this.pageSize
                );
            case 'WITHDRAWAL':
            default:
                return this.itemStockService.getItemStocksWithZeroQuantityInvLocInvCat(
                    this.locationId, this.categoryId, this.searchText, this.page() - 1, this.pageSize
                );
        }
    }

    loadData(): void {
        this.loading.set(true);
        this.fetchItemStocks().subscribe({
            next: (data) => {
                this.items.set(data?.content ?? []);
                this.total.set(data?.page?.totalElements ?? 0);
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

    select(stock: ItemStock): void {
        this.activeModal.close({ action: 'select', data: stock });
    }
}
