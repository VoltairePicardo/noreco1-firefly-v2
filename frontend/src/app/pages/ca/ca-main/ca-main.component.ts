import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { FlatpickrDefaults, FlatpickrModule } from 'angularx-flatpickr';
import { CaService } from '../ca.service';

@Component({
    selector: 'app-ca-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrModule],
    providers: [FlatpickrDefaults],
    templateUrl: './ca-main.component.html'
})
export class CaMainComponent {
    module    = 'Cash Advance';
    subModule = '';
    menuLink  = 'ca';

    records          = signal<any[]>([]);
    documentStatuses = signal<any[]>([]);
    offices          = signal<any[]>([]);
    isLoading        = signal(false);

    searchQuery      = '';
    fromDate         = '';
    toDate           = '';
    selectedStatusId = 0;
    selectedOfficeId = 0;
    page             = 1;
    pageSize         = 20;

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    private service      = inject(CaService);
    private alertService = inject(AlertService);

    get filteredRecords(): any[] {
        const q = this.searchQuery.toLowerCase();
        return this.records().filter(r =>
            !q ||
            (r.code || '').toLowerCase().includes(q) ||
            (r.employee?.fullName || r.employee || r.employeeName || '').toLowerCase().includes(q) ||
            (r.createdBy?.fullName || '').toLowerCase().includes(q)
        );
    }

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.filteredRecords.slice(start, start + this.pageSize);
    }

    ngOnInit(): void {
        this.setDefaultDates();
        this.loadDocumentStatuses();
        this.loadOffices();
    }

    setDefaultDates(): void {
        const now   = new Date();
        const first = new Date(now.getFullYear(), now.getMonth(), 1);
        const last  = new Date(now.getFullYear(), now.getMonth() + 1, 0);
        this.fromDate = this.toDateString(first);
        this.toDate   = this.toDateString(last);
    }

    toDateString(d: Date): string {
        return d.toISOString().substring(0, 10);
    }

    loadDocumentStatuses(): void {
        this.service.getDocumentStatuses().subscribe({
            next: (data) => this.documentStatuses.set(data || []),
            error: () => {}
        });
    }

    loadOffices(): void {
        this.service.getOffices().subscribe({
            next: (data) => {
                this.offices.set(data || []);
                this.loadUserOffice();
            },
            error: () => this.load()
        });
    }

    loadUserOffice(): void {
        this.service.getUserOffice().subscribe({
            next: (data) => { this.selectedOfficeId = data?.id || 0; this.load(); },
            error: () => this.load()
        });
    }

    load(): void {
        this.isLoading.set(true);
        this.service.list(this.fromDate, this.toDate, this.selectedStatusId || undefined, this.selectedOfficeId || undefined).subscribe({
            next: (data) => { this.records.set(data || []); this.page = 1; this.isLoading.set(false); },
            error: () => { this.alertService.error(this.module, 'Failed to load records.', ''); this.isLoading.set(false); }
        });
    }

    reset(): void {
        this.searchQuery      = '';
        this.selectedStatusId = 0;
        this.selectedOfficeId = 0;
        this.setDefaultDates();
        this.page             = 1;
        this.load();
    }

    isEditable(rec: any): boolean {
        const s = (rec?.documentStatus?.status || rec?.status || '').toString();
        return s === 'Document Created' || s === 'Returned to Creator' || s === '1';
    }
}
