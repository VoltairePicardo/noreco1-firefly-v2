import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { FlatpickrDefaults, FlatpickrModule } from 'angularx-flatpickr';
import { EnergySalesService } from '../energy-sales.service';
import { monthStart, monthEnd } from '@/app/shared/utils/date.utils';

@Component({
    selector: 'app-energy-sales-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrModule],
    providers: [...SHARED_PROVIDERS, FlatpickrDefaults],
    templateUrl: './energy-sales-main.component.html'
})
export class EnergySalesMainComponent {
    module    = 'Energy Sales';
    subModule = '';
    menuLink  = 'energy-sales';

    records          = signal<any[]>([]);
    isLoading        = signal(false);
    documentStatuses = signal<any[]>([]);
    processingAll    = false;

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

    get selectedIds(): number[] {
        return this.records().filter(r => r.selected).map(r => r.id);
    }

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    fromDate       = '';
    toDate         = '';
    selectedStatus = signal<number>(0);

    private service      = inject(EnergySalesService);
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
        this.service.listByDateRange(this.fromDate, this.toDate, this.selectedStatus()).subscribe({
            next: (data) => { this.records.set((data || []).map((d: any) => ({ ...d, selected: false }))); this.page = 1; this.isLoading.set(false); },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    reset(): void {
        this.setDefaultDates();
        this.selectedStatus.set(0);
        this.searchText = '';
        this.load();
    }

    approveAll(): void {
        const ids = this.selectedIds;
        if (ids.length === 0) {
            this.alertService.warning(this.module, 'Approve All', 'Please select at least one record.');
            return;
        }
        this.processingAll = true;
        this.service.approveAll(ids).subscribe({
            next: (res) => {
                this.processingAll = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Approved', '');
                    this.load();
                } else {
                    this.alertService.error(this.module, 'Approve All', res?.failureMessage || '');
                }
            },
            error: () => {
                this.processingAll = false;
                this.alertService.error(this.module, 'Approve All', '');
            }
        });
    }

    isEditable(rec: any): boolean {
        const s = rec?.status || '';
        return s === 'Document Created' || s === 'Returned to Creator';
    }
}
