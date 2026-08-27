import { Component, ChangeDetectionStrategy, ChangeDetectorRef, inject } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { StockTransferService } from '@/app/pages/inventory/stock-transfer/stock-transfer.service';

@Component({
    selector: 'app-browse-st-item-stock-modal',
    templateUrl: './browse-st-item-stock-modal.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS]
})
export class BrowseStItemStockModalComponent {
    locationId: number = 0;

    records:   any[] = [];
    isLoading        = false;
    searchText       = '';

    page     = 1;
    pageSize = 10;

    get filtered(): any[] {
        const q = this.searchText.toLowerCase();
        return q
            ? this.records.filter(r =>
                (r.item?.code        || '').toLowerCase().includes(q) ||
                (r.item?.description || '').toLowerCase().includes(q))
            : this.records;
    }

    get filteredTotal(): number { return this.filtered.length; }

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.filtered.slice(start, start + this.pageSize);
    }

    private activeModal = inject(NgbActiveModal);
    private service     = inject(StockTransferService);
    private cdr         = inject(ChangeDetectorRef);

    ngOnInit(): void {
        if (this.locationId) this.load();
    }

    load(): void {
        this.isLoading = true;
        this.service.getItemStocks(this.locationId).subscribe({
            next: (data) => { this.records = data || []; this.isLoading = false; this.cdr.markForCheck(); },
            error: () => { this.isLoading = false; this.cdr.markForCheck(); }
        });
    }

    select(item: any): void {
        this.activeModal.close({ action: 'select', data: item });
    }

    dismiss(): void { this.activeModal.dismiss(); }
}
