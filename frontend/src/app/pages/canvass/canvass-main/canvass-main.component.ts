import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { FormsModule } from '@angular/forms';
import { FlatpickrDefaults, FlatpickrModule } from 'angularx-flatpickr';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerRefresh, tablerPlus, tablerEye, tablerEdit } from '@ng-icons/tabler-icons';
import { CanvassService } from '../canvass.service';

@Component({
    selector: 'app-canvass-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FormsModule, FlatpickrModule, RouterLink],
    providers: [FlatpickrDefaults, provideIcons({ tablerSearch, tablerRefresh, tablerPlus, tablerEye, tablerEdit })],
    templateUrl: './canvass-main.component.html'
})
export class CanvassMainComponent {
    module    = 'Canvass';
    subModule = 'List';
    menuLink  = 'canvass';

    fromDate = '';
    toDate   = '';

    private toLocalDateStr(d: Date): string { return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`; }
    private monthStart(): string { const d = new Date(); return this.toLocalDateStr(new Date(d.getFullYear(), d.getMonth(), 1)); }
    private monthEnd():   string { const d = new Date(); return this.toLocalDateStr(new Date(d.getFullYear(), d.getMonth() + 1, 0)); }
    selectedStatus = signal<number | null>(null);
    documentStatuses = signal<any[]>([]);
    records      = signal<any[]>([]);
    canvassedRVs = signal<any[]>([]);

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

    private service = inject(CanvassService);

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
        this.canvassedRVs.set([]);
        this.service.listByDateRange(this.fromDate, this.toDate, this.selectedStatus()).subscribe({
            next: (data) => {
                this.records.set(data || []);
                this.page = 1;
                this.isLoading.set(false);
                const ids = (data || []).map((r: any) => r.id).filter(Boolean);
                if (ids.length > 0) {
                    this.service.getCanvassedRVs(ids).subscribe({
                        next: (rvs) => this.canvassedRVs.set(rvs || []),
                        error: () => {}
                    });
                }
            },
            error: () => {
                this.records.set([]);
                this.page = 1;
                this.isLoading.set(false);
            }
        });
    }

    reset(): void {
        this.fromDate = this.monthStart();
        this.toDate   = this.monthEnd();
        this.selectedStatus.set(null);
        this.searchText = '';
        this.records.set([]);
        this.canvassedRVs.set([]);
        this.page = 1;
        this.load();
    }

    isEditable(rec: any): boolean {
        const status = rec?.status || '';
        return status === 'Document Created' || status === 'For Revision';
    }
}
