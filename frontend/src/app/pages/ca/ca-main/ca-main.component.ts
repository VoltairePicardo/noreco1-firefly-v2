import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { CaService } from '../ca.service';

@Component({
    selector: 'app-ca-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './ca-main.component.html'
})
export class CaMainComponent {
    module    = 'Cash Advance';
    subModule = 'List';
    menuLink  = 'ca';

    records          = signal<any[]>([]);
    documentStatuses = signal<any[]>([]);
    offices          = signal<any[]>([]);
    isLoading        = signal(false);

    searchQuery      = '';
    fromDate         = '';
    toDate           = '';
    selectedStatusId?: number;
    selectedOffice: any = null;
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
            next: (data) => { this.selectedOffice = data || null; this.load(); },
            error: () => this.load()
        });
    }

    load(): void {
        this.isLoading.set(true);
        this.service.list(this.fromDate, this.toDate, this.selectedStatusId, this.selectedOffice?.id).subscribe({
            next: (data) => { this.records.set(data || []); this.page = 1; this.isLoading.set(false); },
            error: () => { this.alertService.error(this.module, 'Failed to load records.', ''); this.isLoading.set(false); }
        });
    }

    reset(): void {
        this.searchQuery      = '';
        this.selectedStatusId = undefined;
        this.selectedOffice   = null;
        this.setDefaultDates();
        this.page             = 1;
        this.load();
    }

    compareById(a: any, b: any): boolean {
        return a && b ? a.id === b.id : a === b;
    }

    isEditable(rec: any): boolean {
        const s = (rec?.documentStatus?.status || rec?.status || '').toString();
        return s === 'Document Created' || s === 'Returned to Creator' || s === '1';
    }
}
