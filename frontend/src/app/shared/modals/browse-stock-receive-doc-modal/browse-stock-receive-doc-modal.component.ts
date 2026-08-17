import { Component, ChangeDetectionStrategy, ChangeDetectorRef, inject } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { StockReceiveService } from '@/app/pages/stock-receive/stock-receive.service';

@Component({
    selector: 'app-browse-stock-receive-doc-modal',
    templateUrl: './browse-stock-receive-doc-modal.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS]
})
export class BrowseStockReceiveDocModalComponent {
    locationId: number = 0;

    records:  any[] = [];
    isLoading       = false;
    searchText      = '';

    page     = 1;
    pageSize = 10;

    get filtered(): any[] {
        const q = this.searchText.toLowerCase();
        return q
            ? this.records.filter(r =>
                (r.code                               || '').toLowerCase().includes(q) ||
                (r.fromInventoryLocation?.description || '').toLowerCase().includes(q) ||
                (r.toInventoryLocation?.description   || '').toLowerCase().includes(q))
            : this.records;
    }

    get filteredTotal(): number { return this.filtered.length; }

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.filtered.slice(start, start + this.pageSize);
    }

    private activeModal = inject(NgbActiveModal);
    private service     = inject(StockReceiveService);
    private cdr         = inject(ChangeDetectorRef);

    ngOnInit(): void {
        if (this.locationId) this.load();
    }

    load(): void {
        this.isLoading = true;
        this.service.getReceivingDocuments(this.locationId).subscribe({
            next: (data) => { this.records = data || []; this.isLoading = false; this.cdr.markForCheck(); },
            error: () => { this.isLoading = false; this.cdr.markForCheck(); }
        });
    }

    select(doc: any): void {
        this.activeModal.close({ action: 'select', data: doc });
    }

    dismiss(): void { this.activeModal.dismiss(); }
}
