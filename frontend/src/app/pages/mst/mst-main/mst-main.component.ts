import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { MstService } from '../mst.service';

@Component({
    selector: 'app-mst-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './mst-main.component.html'
})
export class MstMainComponent {
    module    = 'Material Salvage Ticket';
    subModule = '';
    menuLink  = 'mst';

    records          = signal<any[]>([]);
    isLoading        = signal(false);
    documentStatuses = signal<any[]>([]);

    page     = 1;
    pageSize = 10;

    get filteredRecords(): any[] {
        const q = this.searchText.toLowerCase();
        return q
            ? this.records().filter(r =>
                (r.code             || '').toLowerCase().includes(q) ||
                (r.department?.name || '').toLowerCase().includes(q) ||
                (r.purpose          || r.remarks || '').toLowerCase().includes(q) ||
                (r.createdBy?.fullName || r.preparedBy || '').toLowerCase().includes(q))
            : this.records();
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
    searchText     = '';

    private service      = inject(MstService);
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
