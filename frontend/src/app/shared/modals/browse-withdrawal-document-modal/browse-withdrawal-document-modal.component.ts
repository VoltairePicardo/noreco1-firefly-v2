import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch } from '@ng-icons/tabler-icons';
import { StockReleaseService } from '@/app/pages/inventory/stock-release/stock-release.service';

@Component({
    selector: 'app-browse-withdrawal-document-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerSearch })],
    templateUrl: './browse-withdrawal-document-modal.component.html'
})
export class BrowseWithdrawalDocumentModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private service = inject(StockReleaseService);
    private cdr = inject(ChangeDetectorRef);

    items: any[] = [];
    loading = false;
    searchText = '';

    ngOnInit(): void {
        this.loading = true;
        this.service.getWithdrawalDocuments().subscribe({
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
        return this.items.filter(w =>
            w.code?.toLowerCase().includes(q) ||
            w.inventoryLocation?.description?.toLowerCase().includes(q) ||
            w.inventoryLocation?.name?.toLowerCase().includes(q) ||
            w.inventoryCategory?.description?.toLowerCase().includes(q) ||
            w.createdBy?.fullName?.toLowerCase().includes(q)
        );
    }

    select(withdrawal: any): void {
        this.activeModal.close({ action: 'select', data: withdrawal });
    }
}
