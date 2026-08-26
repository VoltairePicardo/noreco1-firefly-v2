import {ChangeDetectionStrategy, Component, inject, Input, OnInit, signal} from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerChevronLeft, tablerChevronRight, tablerSearch } from '@ng-icons/tabler-icons';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { WithdrawalService } from '@/app/pages/inventory/withdrawal/withdrawal.service';
import { StockWithdrawal } from '@/app/models/inventory-modules/stock-withdrawal.model';

@Component({
    selector: 'app-browse-mr-stock-withdrawal-modal',
    templateUrl: './browse-mr-stock-withdrawal-modal.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerChevronLeft, tablerChevronRight, tablerSearch })]
})
export class BrowseMrStockWithdrawalModalComponent implements OnInit {

    @Input()multipleEmployee: boolean = false;

    activeModal = inject(NgbActiveModal);
    private service = inject(WithdrawalService);

    items      = signal<StockWithdrawal[]>([]);
    total      = signal(0);
    page       = signal(1);
    pageSize   = signal(10);
    searchText = signal('');
    loading    = signal(false);

    ngOnInit(): void {
        this.loadData();
    }

    loadData(): void {
        if (this.loading()) return;
        this.loading.set(true);
        this.service.getMemorandumReceiptVouchers(this.searchText(), this.page() - 1, this.pageSize(), this.multipleEmployee).subscribe({
            next: (res) => {
                this.items.set(res.content ?? []);
                this.total.set(res.totalElements ?? res.page?.totalElements ?? this.items().length);
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    onPageChange(page: number): void {
        this.page.set(page);
        this.loadData();
    }

    onSearchChange(): void {
        this.page.set(1);
        this.loadData();
    }

    select(doc: StockWithdrawal): void {
        this.activeModal.close({ action: 'select', data: doc });
    }
}
