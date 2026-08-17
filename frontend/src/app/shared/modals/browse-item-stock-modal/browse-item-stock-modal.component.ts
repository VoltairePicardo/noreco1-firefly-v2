import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, Input, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch } from '@ng-icons/tabler-icons';
import { WithdrawalService } from '@/app/pages/withdrawal/withdrawal.service';

@Component({
    selector: 'app-browse-item-stock-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerSearch })],
    templateUrl: './browse-item-stock-modal.component.html'
})
export class BrowseItemStockModalComponent implements OnInit {
    @Input() locationId!: number;
    @Input() categoryId!: number;

    activeModal      = inject(NgbActiveModal);
    private service  = inject(WithdrawalService);
    private cdr      = inject(ChangeDetectorRef);

    items:      any[] = [];
    loading           = false;
    searchText        = '';

    get filtered(): any[] {
        const q = this.searchText.trim().toLowerCase();
        if (!q) return this.items;
        return this.items.filter(s =>
            (s.item?.code        || '').toLowerCase().includes(q) ||
            (s.item?.description || '').toLowerCase().includes(q) ||
            (s.item?.unit?.code  || '').toLowerCase().includes(q)
        );
    }

    ngOnInit(): void {
        this.loading = true;
        this.service.getItemStocksForWithdrawal(this.locationId, this.categoryId).subscribe({
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

    select(stock: any): void {
        this.activeModal.close({ action: 'select', data: stock });
    }
}
