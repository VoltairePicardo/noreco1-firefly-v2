import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { FlatpickrDefaults, FlatpickrModule } from 'angularx-flatpickr';
import { BankDepositService } from '../bank-deposit.service';
import { monthStart, monthEnd } from '@/app/shared/utils/date.utils';

@Component({
    selector: 'app-bank-deposit-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrModule],
    providers: [...SHARED_PROVIDERS, FlatpickrDefaults],
    templateUrl: './bank-deposit-main.component.html'
})
export class BankDepositMainComponent {
    module    = 'Bank Deposits';
    subModule = '';
    menuLink  = 'bank-deposit';

    records   = signal<any[]>([]);
    isLoading = signal(false);

    page     = 1;
    pageSize = 10;

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    fromDate = '';
    toDate   = '';
    search   = '';

    get filteredRecords(): any[] {
        let data = this.records();
        if (this.fromDate) {
            data = data.filter(r => r.depositDate >= this.fromDate);
        }
        if (this.toDate) {
            data = data.filter(r => r.depositDate <= this.toDate);
        }
        if (this.search.trim()) {
            const q = this.search.trim().toLowerCase();
            data = data.filter(r =>
                (r.code || '').toLowerCase().includes(q) ||
                (r.referenceNumber || '').toLowerCase().includes(q) ||
                (r.bankAccountName || '').toLowerCase().includes(q)
            );
        }
        return data;
    }

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.filteredRecords.slice(start, start + this.pageSize);
    }

    private service      = inject(BankDepositService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.setDefaultDates();
        this.load();
    }

    setDefaultDates(): void {
        this.fromDate = monthStart();
        this.toDate   = monthEnd();
    }

    load(): void {
        this.isLoading.set(true);
        this.service.list().subscribe({
            next: (data) => { this.records.set(data || []); this.page = 1; this.isLoading.set(false); },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    reset(): void {
        this.setDefaultDates();
        this.search = '';
        this.page   = 1;
        this.load();
    }
}
