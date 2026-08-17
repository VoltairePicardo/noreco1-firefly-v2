import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { FormsModule } from '@angular/forms';
import { FlatpickrDefaults, FlatpickrModule } from 'angularx-flatpickr';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerRefresh, tablerPlus, tablerEye, tablerEdit } from '@ng-icons/tabler-icons';
import { JobOrderService } from '../job-order.service';

@Component({
    selector: 'app-job-order-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FormsModule, FlatpickrModule, RouterLink],
    providers: [FlatpickrDefaults, provideIcons({ tablerSearch, tablerRefresh, tablerPlus, tablerEye, tablerEdit })],
    templateUrl: './job-order-main.component.html'
})
export class JobOrderMainComponent {
    module    = 'Job Order';
    subModule = '';
    menuLink  = 'job-order';

    fromDate = '';
    toDate   = '';

    private toLocalDateStr(d: Date): string { return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`; }
    private monthStart(): string { const d = new Date(); return this.toLocalDateStr(new Date(d.getFullYear(), d.getMonth(), 1)); }
    private monthEnd():   string { const d = new Date(); return this.toLocalDateStr(new Date(d.getFullYear(), d.getMonth() + 1, 0)); }
    selectedStatus = signal<number | null>(null);
    documentStatuses = signal<any[]>([]);
    records   = signal<any[]>([]);

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

    isLoading = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    private service = inject(JobOrderService);

    ngOnInit(): void {
        this.fromDate = this.monthStart();
        this.toDate   = this.monthEnd();
        this.service.getDocumentStatuses().subscribe({
            next: (statuses) => this.documentStatuses.set(statuses || []),
            error: () => {}
        });
        this.load();
    }

    load(): void {
        if (!this.fromDate || !this.toDate) return;
        this.isLoading.set(true);
        this.service.listByDateRange(this.fromDate, this.toDate, this.selectedStatus()).subscribe({
            next: (data) => { this.records.set(data || []);
            this.page = 1; this.isLoading.set(false); },
            error: () => { this.records.set([]);
            this.page = 1; this.isLoading.set(false); }
        });
    }

    reset(): void {
        this.fromDate = this.monthStart();
        this.toDate   = this.monthEnd();
        this.selectedStatus.set(null);
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
