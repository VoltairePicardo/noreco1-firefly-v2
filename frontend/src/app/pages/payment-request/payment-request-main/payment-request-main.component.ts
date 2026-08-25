import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { PaymentRequestService } from '../payment-request.service';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { FormsModule } from '@angular/forms';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerRefresh, tablerPlus, tablerEye, tablerEdit } from '@ng-icons/tabler-icons';
import { monthStart, monthEnd } from '@/app/shared/utils/date.utils';

@Component({
    selector: 'app-payment-request-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective, FormsModule, RouterLink],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS,
        provideIcons({ tablerSearch, tablerRefresh, tablerPlus, tablerEye, tablerEdit })],
    templateUrl: './payment-request-main.component.html'
})
export class PaymentRequestMainComponent {
    module   = 'Payment Request';
    menuLink = 'payment-request';

    isLoading = signal(false);
    records   = signal<any[]>([]);

    page     = 1;
    pageSize = 10;

    statuses         = signal<any[]>([]);
    dateFrom         = '';
    dateTo           = '';
    selectedStatusId: number | null = null;

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    private service      = inject(PaymentRequestService);
    private alertService = inject(AlertService);

    searchText = '';

    get filteredRecords(): any[] {
        if (!this.searchText.trim()) return this.records();
        const q = this.searchText.toLowerCase();
        return this.records().filter((r: any) => (r.localCode || r.code || '').toLowerCase().includes(q));
    }

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.filteredRecords.slice(start, start + this.pageSize);
    }

    ngOnInit(): void {
        this.service.getDocumentStatuses().subscribe({
            next: (data) => this.statuses.set(data || []),
            error: () => {}
        });

        this.dateFrom = monthStart();
        this.dateTo   = monthEnd();
        this.load();
    }

    load(): void {
        if (!this.dateFrom || !this.dateTo) return;
        this.isLoading.set(true);
        this.page = 1;

        const req$ = this.selectedStatusId
            ? this.service.listByStatus(this.dateFrom, this.dateTo, this.selectedStatusId)
            : this.service.list(this.dateFrom, this.dateTo);

        req$.subscribe({
            next: (data) => {
                this.records.set(data || []);
                this.isLoading.set(false);
            },
            error: () => {
                this.alertService.error(this.module, 'Load', '');
                this.isLoading.set(false);
            }
        });
    }

    reset(): void {
        this.dateFrom        = monthStart();
        this.dateTo          = monthEnd();
        this.selectedStatusId = null;
        this.searchText = '';
        this.records.set([]);
        this.page = 1;
        this.load();
    }

    isEditable(rec: any): boolean {
        const s = rec?.status || '';
        return s === 'Document Created' || s === 'For Revision';
    }
}
