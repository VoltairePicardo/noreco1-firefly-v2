import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { FormsModule } from '@angular/forms';
import { FlatpickrDefaults, FlatpickrModule } from 'angularx-flatpickr';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerRefresh, tablerPlus, tablerEye, tablerEdit } from '@ng-icons/tabler-icons';
import { JoAcceptanceService } from '../jo-acceptance.service';

@Component({
    selector: 'app-jo-acceptance-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FormsModule, FlatpickrModule, RouterLink],
    providers: [FlatpickrDefaults, provideIcons({ tablerSearch, tablerRefresh, tablerPlus, tablerEye, tablerEdit })],
    templateUrl: './jo-acceptance-main.component.html'
})
export class JoAcceptanceMainComponent {
    module    = 'JO Certification / Acceptance';
    subModule = '';
    menuLink  = 'jo-acceptance';

    fromDate     = '';
    toDate       = '';
    selectedStatus   = signal<number | null>(null);
    documentStatuses = signal<any[]>([]);
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

    isLoading = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    private service = inject(JoAcceptanceService);

    ngOnInit(): void {
        this.service.getDocumentStatuses().subscribe({
            next: (statuses) => this.documentStatuses.set(statuses || []),
            error: () => {}
        });

        // Auto-load with current month range (mirrors old Firefly behavior)
        const now   = new Date();
        const fmt   = (d: Date) =>
            `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
        this.fromDate = fmt(new Date(now.getFullYear(), now.getMonth(), 1));
        this.toDate   = fmt(new Date(now.getFullYear(), now.getMonth() + 1, 0));
        this.load();
    }

    load(): void {
        if (!this.fromDate || !this.toDate) return;
        this.isLoading.set(true);
        this.page = 1;
        this.service.listByDateRange(this.fromDate, this.toDate, this.selectedStatus()).subscribe({
            next:  (data) => { this.records.set(data || []); this.isLoading.set(false); },
            error: ()     => { this.records.set([]);         this.isLoading.set(false); }
        });
    }

    reset(): void {
        const now   = new Date();
        const fmt   = (d: Date) =>
            `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
        this.fromDate = fmt(new Date(now.getFullYear(), now.getMonth(), 1));
        this.toDate   = fmt(new Date(now.getFullYear(), now.getMonth() + 1, 0));
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
