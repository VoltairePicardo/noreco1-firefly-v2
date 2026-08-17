import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { PrepaymentService } from '../prepayment.service';

@Component({
    selector: 'app-prepayment-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './prepayment-main.component.html'
})
export class PrepaymentMainComponent {
    module    = 'Prepayments and Other Amortizations';
    subModule = '';
    menuLink  = 'prepayment';

    records   = signal<any[]>([]);
    isLoading = signal(false);

    page     = 1;
    pageSize = 10;

    searchText  = '';
    statusFilter = '';   // '' = All, 'OPEN', 'CLOSED'
    fromDate     = '';
    toDate       = '';

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    get filteredRecords(): any[] {
        let data = this.records();
        const q  = this.searchText.trim().toLowerCase();
        if (q) {
            data = data.filter(r =>
                (r.code        || '').toLowerCase().includes(q) ||
                (r.description || '').toLowerCase().includes(q)
            );
        }
        if (this.statusFilter) {
            data = data.filter(r => this.getStatus(r) === this.statusFilter);
        }
        return data;
    }

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.filteredRecords.slice(start, start + this.pageSize);
    }

    private service      = inject(PrepaymentService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.load();
    }

    load(): void {
        this.isLoading.set(true);
        this.page = 1;

        const obs = (this.fromDate && this.toDate)
            ? this.service.listByDateRange(this.fromDate, this.toDate)
            : this.service.list();

        obs.subscribe({
            next: (data) => { this.records.set(data || []); this.isLoading.set(false); },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    search(): void {
        this.page = 1;
        this.load();
    }

    reset(): void {
        this.searchText   = '';
        this.statusFilter = '';
        this.fromDate     = '';
        this.toDate       = '';
        this.load();
    }

    getStatus(rec: any): string {
        return (Number(rec.balance) || 0) > 0 ? 'OPEN' : 'CLOSED';
    }

    isEditable(rec: any): boolean {
        return !rec.hasPpd;
    }
}
