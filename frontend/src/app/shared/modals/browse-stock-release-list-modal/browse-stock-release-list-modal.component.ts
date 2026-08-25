import { ChangeDetectionStrategy, Component, computed, inject, Input, OnInit, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerChevronLeft, tablerChevronRight, tablerSearch } from '@ng-icons/tabler-icons';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { StockReleaseService } from '@/app/pages/inventory/stock-release/stock-release.service';
import { StockRelease } from '@/app/models/inventory-modules/stock-release.model';
import { DateHelper } from '@/app/helpers/date-helper';

@Component({
    selector: 'app-browse-stock-release-list-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective, ReactiveFormsModule],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerChevronLeft, tablerChevronRight, tablerSearch }), provideFlatpickrDefaults()],
    templateUrl: './browse-stock-release-list-modal.component.html'
})
export class BrowseStockReleaseListModalComponent implements OnInit {
    @Input() title = 'Browse Stock Release';
    @Input() from?: string;
    @Input() to?: string;
    @Input() type?: number;

    activeModal = inject(NgbActiveModal);
    private service = inject(StockReleaseService);

    readonly pageSize = 10;
    readonly flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y', static: true };

    items   = signal<StockRelease[]>([]);
    total   = signal(0);
    page    = signal(1);
    loading = signal(false);

    searchControl = new FormControl('', { nonNullable: true });

    hasResults = computed(() => this.items().length > 0);

    ngOnInit(): void {
        this.applyDefaultDateRange();
        this.loadData();
    }

    private applyDefaultDateRange(): void {
        this.from ??= DateHelper.dateToSql(DateHelper.startOfMonth());
        this.to ??= DateHelper.dateToSql(DateHelper.endOfMonth());
    }

    loadData(): void {
        if (this.loading()) return;
        this.loading.set(true);
        this.service.getStockReleasePaged(this.from!, this.to!, this.type ?? null, this.searchControl.value.trim(), this.page() - 1, this.pageSize)
            .subscribe({
                next: (res) => {
                    this.items.set(res?.content ?? []);
                    this.total.set(res?.page?.totalElements ?? 0);
                    this.loading.set(false);
                },
                error: () => {
                    this.items.set([]);
                    this.total.set(0);
                    this.loading.set(false);
                }
            });
    }

    onSearch(): void {
        this.page.set(1);
        this.loadData();
    }

    onPageChange(page: number): void {
        this.page.set(page);
        this.loadData();
    }

    select(item: StockRelease): void {
        this.activeModal.close({ action: 'select', data: item });
    }
}
