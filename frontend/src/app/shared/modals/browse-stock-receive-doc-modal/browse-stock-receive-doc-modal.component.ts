import { Component, ChangeDetectionStrategy, ChangeDetectorRef, inject } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { StockReceiveService } from '@/app/pages/inventory/stock-receive/stock-receive.service';

@Component({
    selector: 'app-browse-stock-receive-doc-modal',
    templateUrl: './browse-stock-receive-doc-modal.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS]
})
export class BrowseStockReceiveDocModalComponent {
    locationId: number = 0;

    records:  any[] = [];
    total     = 0;
    isLoading = false;
    searchText = '';

    page     = 1;
    pageSize = 10;

    private activeModal = inject(NgbActiveModal);
    private service     = inject(StockReceiveService);
    private cdr         = inject(ChangeDetectorRef);

    ngOnInit(): void {
        if (this.locationId) this.load();
    }

    load(): void {
        if (this.isLoading) return;
        this.isLoading = true;
        this.service.getReceivingDocuments(this.locationId, this.searchText, this.page - 1, this.pageSize).subscribe({
            next: (res) => {
                this.records   = res.content ?? res ?? [];
                this.total     = res.totalElements ?? res.page?.totalElements ?? this.records.length;
                this.isLoading = false;
                this.cdr.markForCheck();
            },
            error: () => { this.isLoading = false; this.cdr.markForCheck(); }
        });
    }

    onSearchChange(): void {
        this.page = 1;
        this.load();
    }

    onPageChange(): void {
        this.load();
    }

    select(doc: any): void {
        this.activeModal.close({ action: 'select', data: doc });
    }

    dismiss(): void { this.activeModal.dismiss(); }
}
