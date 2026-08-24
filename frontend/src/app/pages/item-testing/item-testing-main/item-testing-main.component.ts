import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { ItemTestingService } from '../item-testing.service';
import { monthStart, monthEnd } from '@/app/shared/utils/date.utils';

@Component({
    selector: 'app-item-testing-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './item-testing-main.component.html'
})
export class ItemTestingMainComponent {
    module    = 'Item Testing';
    subModule = '';
    menuLink  = 'item-testing';

    records   = signal<any[]>([]);
    isLoading = signal(false);
    page      = 1;
    pageSize  = 10;

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.records().slice(start, start + this.pageSize);
    }

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };
    fromDate = ''; toDate = '';

    private service      = inject(ItemTestingService);
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
            next: (data) => { this.records.set(data || []); this.page = 1; this.isLoading.set(false); },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    reset(): void { this.setDefaultDates(); this.load(); }

    isEditable(rec: any): boolean {
        const s = rec?.documentStatus?.status || '';
        return s === 'Document Created' || s === 'For Revision';
    }
}
