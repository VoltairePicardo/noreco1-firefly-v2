import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, Input, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerChevronLeft, tablerChevronRight, tablerSearch } from '@ng-icons/tabler-icons';
import { ItemService } from '@/app/pages/item/item.service';

@Component({
    selector: 'app-browse-item-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [
        ...SHARED_PROVIDERS,
        provideIcons({ tablerChevronLeft, tablerChevronRight, tablerSearch })
    ],
    templateUrl: './browse-item-modal.component.html'
})
export class BrowseItemModalComponent implements OnInit {
    @Input() inventoryLocationId?: number;

    activeModal = inject(NgbActiveModal);
    private service = inject(ItemService);
    private cdr = inject(ChangeDetectorRef);

    items: any[] = [];
    categories: any[] = [];
    selectedCategoryId: number | null = null;
    total = 0;
    page = 1;
    pageSize = 10;
    searchText = '';
    loading = false;

    ngOnInit(): void {
        if (!this.inventoryLocationId) {
            this.service.getCategories().subscribe({
                next: (res) => { this.categories = res; this.cdr.markForCheck(); },
                error: () => {}
            });
        }
        this.loadData();
    }

    onCategoryChange(): void {
        this.page = 1;
        this.loadData();
    }

    loadData(): void {
        if (this.loading) return;
        this.loading = true;
        const obs = this.inventoryLocationId
            ? this.service.listByLocation(this.inventoryLocationId, this.searchText, this.page - 1, this.pageSize)
            : this.service.list(this.searchText, null, this.page - 1, this.pageSize, this.selectedCategoryId);
        obs.subscribe({
            next: (res) => {
                const raw = res.content || [];
                // normalize ItemStock entries so template stays unchanged
                this.items = this.inventoryLocationId
                    ? raw.map((is: any) => ({
                        id:          is.id,
                        code:        is.item?.code,
                        description: is.item?.description,
                        unit:        is.item?.unit
                      }))
                    : raw;
                this.total = res.page?.totalElements || res.totalElements || 0;
                this.loading = false;
                this.cdr.markForCheck();
            },
            error: () => {
                this.loading = false;
                this.cdr.markForCheck();
            }
        });
    }

    onSearchChange(): void {
        this.page = 1;
        this.loadData();
    }

    select(item: any): void {
        this.activeModal.close({ action: 'select', data: item });
    }

    close(): void {
        this.activeModal.dismiss('closed');
    }
}
