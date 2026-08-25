import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { ReturnMemorandumReceiptService } from '../return-memorandum-receipt.service';
import { monthStart, monthEnd } from '@/app/shared/utils/date.utils';

@Component({
    selector: 'app-return-memorandum-receipt-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './return-memorandum-receipt-main.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReturnMemorandumReceiptMainComponent implements OnInit {
    module    = 'Return Memorandum Receipt';
    subModule = '';
    menuLink  = 'return-memorandum-receipt';

    records   = signal<any[]>([]);
    isLoading = signal(false);
    page      = signal(1);
    pageSize  = 10;

    pagedRecords = computed(() => {
        const start = (this.page() - 1) * this.pageSize;
        return this.records().slice(start, start + this.pageSize);
    });

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };
    fromDate = ''; toDate = '';

    private service      = inject(ReturnMemorandumReceiptService);
    private alertService = inject(AlertService);

    ngOnInit(): void { this.setDefaultDates(); this.load(); }

    setDefaultDates(): void {
        this.fromDate = monthStart();
        this.toDate   = monthEnd();
    }

    load(): void {
        this.isLoading.set(true);
        const obs = (this.fromDate && this.toDate)
            ? this.service.listByDateRange(this.fromDate, this.toDate)
            : this.service.list();
        obs.subscribe({
            next: (data) => { this.records.set(data || []); this.page.set(1); this.isLoading.set(false); },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    reset(): void { this.setDefaultDates(); this.load(); }
}
