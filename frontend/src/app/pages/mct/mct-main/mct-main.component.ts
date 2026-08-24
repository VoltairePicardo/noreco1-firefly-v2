import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { MctService } from '../mct.service';
import { monthStart, monthEnd } from '@/app/shared/utils/date.utils';

@Component({
    selector: 'app-mct-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './mct-main.component.html'
})
export class MctMainComponent {
    module    = 'Material Credit Ticket';
    subModule = '';
    menuLink  = 'mct';

    records          = signal<any[]>([]);
    isLoading        = signal(false);
    documentStatuses = signal<any[]>([]);

    page     = 1;
    pageSize = 10;
    searchText = '';

    get filteredRecords(): any[] {
        const q = this.searchText.toLowerCase();
        if (!q) return this.records();
        return this.records().filter(r =>
            r.code?.toLowerCase().includes(q) ||
            r.remarks?.toLowerCase().includes(q) ||
            r.description?.toLowerCase().includes(q) ||
            r.createdBy?.fullName?.toLowerCase().includes(q)
        );
    }

    get filteredTotal(): number { return this.filteredRecords.length; }

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.filteredRecords.slice(start, start + this.pageSize);
    }

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    fromDate       = '';
    toDate         = '';
    selectedStatus = signal<number | null>(null);

    private service      = inject(MctService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.setDefaultDates();
        this.loadStatuses();
        this.load();
    }

    setDefaultDates(): void {
        this.fromDate = monthStart();
        this.toDate   = monthEnd();
    }

    loadStatuses(): void {
        this.service.getDocumentStatuses().subscribe({
            next: (s) => this.documentStatuses.set(s || []),
            error: () => {}
        });
    }

    load(): void {
        this.isLoading.set(true);
        const obs = (this.fromDate && this.toDate)
            ? this.service.listByDateRange(this.fromDate, this.toDate, this.selectedStatus())
            : this.service.list();
        obs.subscribe({
            next: (data) => { this.records.set(data || []); this.page = 1; this.isLoading.set(false); },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    reset(): void {
        this.setDefaultDates();
        this.selectedStatus.set(null);
        this.searchText = '';
        this.load();
    }

    isEditable(rec: any): boolean {
        const s = rec?.documentStatus?.status || '';
        return s === 'Document Created' || s === 'Returned to Creator';
    }
}
