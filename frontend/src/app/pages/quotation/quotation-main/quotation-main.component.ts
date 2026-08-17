import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { QuotationService } from '../quotation.service';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerRefresh, tablerPlus, tablerEye, tablerEdit } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-quotation-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS, provideIcons({ tablerSearch, tablerRefresh, tablerPlus, tablerEye, tablerEdit })],
    templateUrl: './quotation-main.component.html'
})
export class QuotationMainComponent {
    module   = 'Quotation';
    menuLink = 'quotation';

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    items             = signal<any[]>([]);

    page     = 1;
    pageSize = 10;

    searchText = '';

    get pagedRecords(): any[] {
        const q = this.searchText.trim().toLowerCase();
        const filtered = q ? this.items().filter((r: any) => (r.code || '').toLowerCase().includes(q)) : this.items();
        const start = (this.page - 1) * this.pageSize;
        return filtered.slice(start, start + this.pageSize);
    }

    statuses          = signal<any[]>([]);
    isLoading         = signal(false);
    selectedStatusId: number | null = null;

    dateFrom: string;
    dateTo:   string;

    private service      = inject(QuotationService);
    private alertService = inject(AlertService);
    private router       = inject(Router);

    private toLocalDateStr(d: Date): string { return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`; }

    constructor() {
        const now     = new Date();
        this.dateFrom = this.toLocalDateStr(new Date(now.getFullYear(), now.getMonth(), 1));
        this.dateTo   = this.toLocalDateStr(new Date(now.getFullYear(), now.getMonth() + 1, 0));
    }

    ngOnInit(): void {
        this.loadStatuses();
        this.load();
    }

    loadStatuses(): void {
        this.service.getDocumentStatuses().subscribe({
            next:  (data) => this.statuses.set(data || []),
            error: () => {}
        });
    }

    load(): void {
        if (!this.dateFrom || !this.dateTo) return;
        this.isLoading.set(true);
        const req = this.selectedStatusId != null
            ? this.service.listByStatus(this.dateFrom, this.dateTo, this.selectedStatusId)
            : this.service.list(this.dateFrom, this.dateTo);

        req.subscribe({
            next:  (data) => { this.items.set(data || []);
            this.page = 1; this.isLoading.set(false); },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    reset(): void {
        const now        = new Date();
        this.dateFrom    = this.toLocalDateStr(new Date(now.getFullYear(), now.getMonth(), 1));
        this.dateTo      = this.toLocalDateStr(new Date(now.getFullYear(), now.getMonth() + 1, 0));
        this.selectedStatusId = null;
        this.searchText = '';
        this.load();
    }
}
