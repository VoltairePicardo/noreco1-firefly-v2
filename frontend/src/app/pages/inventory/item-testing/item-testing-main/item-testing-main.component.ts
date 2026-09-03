import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { ItemTestingService } from '../item-testing.service';
import { monthStart, monthEnd } from '@/app/shared/utils/date.utils';
import { ItemTestingListRow } from '@/app/models/inventory-modules/item-testing.model';

@Component({
    selector: 'app-item-testing-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './item-testing-main.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ItemTestingMainComponent implements OnInit {
    module    = 'Item Testing';
    subModule = '';
    menuLink  = 'item-testing';

    records       = signal<ItemTestingListRow[]>([]);
    isLoading     = signal(false);

    page          = signal(1);
    pageSize      = 10;
    filteredTotal = signal(0);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };
    fromDate = '';
    toDate   = '';

    private service      = inject(ItemTestingService);
    private alertService = inject(AlertService);

    ngOnInit(): void { this.setDefaultDates(); this.load(); }

    setDefaultDates(): void {
        this.fromDate = monthStart();
        this.toDate   = monthEnd();
    }

    load(): void {
        this.isLoading.set(true);
        this.service.listPaged(this.fromDate, this.toDate, this.page() - 1, this.pageSize).subscribe({
            next: (data) => {
                this.records.set(data?.content ?? []);
                this.filteredTotal.set(data?.totalElements ?? data?.page?.totalElements ?? 0);
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

    onPageChange(page: number): void {
        this.page.set(page);
        this.load();
    }

    reset(): void {
        this.setDefaultDates();
        this.page.set(1);
        this.load();
    }

    isEditable(rec: ItemTestingListRow): boolean {
        const s = rec?.documentStatus?.status || '';
        return s === 'Document Created' || s === 'For Revision';
    }
}
