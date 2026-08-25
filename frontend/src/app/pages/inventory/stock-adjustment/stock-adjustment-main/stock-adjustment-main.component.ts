import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { StockAdjustmentService } from '../stock-adjustment.service';

@Component({
    selector: 'app-stock-adjustment-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './stock-adjustment-main.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class StockAdjustmentMainComponent implements OnInit {
    module    = 'Stock Adjustment';
    subModule = '';
    menuLink  = 'stock-adjustment';

    records          = signal<any[]>([]);
    isLoading        = signal(false);
    documentStatuses = signal<any[]>([]);

    page          = signal(1);
    pageSize      = 10;
    filteredTotal = signal(0);
    searchText    = signal('');

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    fromDate       = '';
    toDate         = '';
    selectedStatus = signal<number | null>(null);

    private service      = inject(StockAdjustmentService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.setDefaultDates();
        this.loadStatuses();
        this.load();
    }

    setDefaultDates(): void {
        const now   = new Date();
        const first = new Date(now.getFullYear(), now.getMonth(), 1);
        this.fromDate = first.toISOString().substring(0, 10);
        this.toDate   = now.toISOString().substring(0, 10);
    }

    loadStatuses(): void {
        this.service.getDocumentStatuses().subscribe({
            next: (s) => this.documentStatuses.set(s || []),
            error: () => {}
        });
    }

    load(): void {
        this.isLoading.set(true);
        this.service.listPaged(this.fromDate, this.toDate, this.selectedStatus(), this.searchText().trim(), this.page() - 1, this.pageSize)
            .subscribe({
                next: (data) => {
                    this.records.set(data?.content ?? []);
                    this.filteredTotal.set(data?.page?.totalElements ?? 0);
                    this.isLoading.set(false);
                },
                error: () => {
                    this.records.set([]);
                    this.filteredTotal.set(0);
                    this.alertService.error(this.module, 'Load', '');
                    this.isLoading.set(false);
                }
            });
    }

    search(): void {
        this.page.set(1);
        this.load();
    }

    onPageChange(page: number): void {
        this.page.set(page);
        this.load();
    }

    reset(): void {
        this.setDefaultDates();
        this.selectedStatus.set(null);
        this.searchText.set('');
        this.page.set(1);
        this.load();
    }

    isEditable(rec: any): boolean {
        const s = rec?.status || '';
        return s === 'Document Created' || s === 'Returned to Creator';
    }
}
