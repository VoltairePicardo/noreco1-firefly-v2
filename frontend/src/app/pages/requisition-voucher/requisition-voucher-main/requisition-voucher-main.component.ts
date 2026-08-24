import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { FlatpickrDefaults, FlatpickrModule } from 'angularx-flatpickr';
import { RequisitionVoucherService } from '../requisition-voucher.service';
import { fmtDate, monthStart, monthEnd } from '@/app/shared/utils/date.utils';

@Component({
    selector: 'app-requisition-voucher-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrModule],
    providers: [...SHARED_PROVIDERS, FlatpickrDefaults],
    templateUrl: './requisition-voucher-main.component.html'
})
export class RequisitionVoucherMainComponent {
    module    = 'Purchase/Work Request';
    subModule = '';
    menuLink  = 'requisition-voucher';

    records          = signal<any[]>([]);

    page     = 1;
    pageSize = 10;

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

    isLoading        = signal(false);
    documentStatuses = signal<any[]>([]);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    fromDate       = '';
    toDate         = '';
    selectedStatus = signal<number | null>(null);

    private service      = inject(RequisitionVoucherService);
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

    toDateString(d: Date): string {
        return fmtDate(d);
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
            next: (data) => { this.records.set(data || []);
            this.page = 1; this.isLoading.set(false); },
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
        const s = rec?.status || '';
        return s === 'Document Created' || s === 'Returned to Creator';
    }
}
