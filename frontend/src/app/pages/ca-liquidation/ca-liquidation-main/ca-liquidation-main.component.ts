import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { CaLiquidationService } from '../ca-liquidation.service';
import { provideIcons } from '@ng-icons/core';
import { tablerChevronLeft, tablerChevronRight } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-ca-liquidation-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerChevronLeft, tablerChevronRight })],
    templateUrl: './ca-liquidation-main.component.html'
})
export class CaLiquidationMainComponent {
    module    = 'CA Liquidation';
    subModule = '';
    menuLink  = 'ca-liquidation';

    records          = signal<any[]>([]);
    offices          = signal<any[]>([]);
    documentStatuses = signal<any[]>([]);
    isLoading        = signal(false);
    isLoadingFilters = signal(false);

    searchQuery      = '';
    page             = 1;
    pageSize         = 20;

    fromDate         = this.firstOfMonth();
    toDate           = this.lastOfMonth();
    selectedOffice:  any = null;
    selectedStatus:  any = null;

    private service      = inject(CaLiquidationService);
    private alertService = inject(AlertService);

    private firstOfMonth(): string {
        const d = new Date();
        return new Date(d.getFullYear(), d.getMonth(), 1).toISOString().substring(0, 10);
    }

    private lastOfMonth(): string {
        const d = new Date();
        return new Date(d.getFullYear(), d.getMonth() + 1, 0).toISOString().substring(0, 10);
    }

    get filteredRecords(): any[] {
        const q = this.searchQuery.toLowerCase();
        return this.records().filter(r => {
            const matchText = !q ||
                (r.code || '').toLowerCase().includes(q) ||
                (r.employee || '').toLowerCase().includes(q) ||
                (r.preparedBy?.fullName || '').toLowerCase().includes(q);

            const recDate = r.date ? r.date.substring(0, 10) : '';
            const matchFrom   = !this.fromDate || recDate >= this.fromDate;
            const matchTo     = !this.toDate   || recDate <= this.toDate;
            const matchOffice = !this.selectedOffice || r.office?.id === this.selectedOffice.id;
            const matchStatus = !this.selectedStatus || r.documentStatus?.id === this.selectedStatus.id;

            return matchText && matchFrom && matchTo && matchOffice && matchStatus;
        });
    }

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.filteredRecords.slice(start, start + this.pageSize);
    }

    ngOnInit(): void {
        this.isLoadingFilters.set(true);
        this.service.getOffices().subscribe({
            next: (data) => this.offices.set(data || []),
            error: () => {}
        });
        this.service.getDocumentStatuses().subscribe({
            next: (data) => this.documentStatuses.set(data || []),
            error: () => {}
        });
        this.service.getUserOffice().subscribe({
            next: (data) => {
                this.selectedOffice = data || null;
                this.isLoadingFilters.set(false);
                this.load();
            },
            error: () => { this.isLoadingFilters.set(false); this.load(); }
        });
    }

    load(): void {
        this.isLoading.set(true);
        this.service.list().subscribe({
            next: (data) => { this.records.set(data || []); this.page = 1; this.isLoading.set(false); },
            error: () => { this.alertService.error(this.module, 'Failed to load records.', ''); this.isLoading.set(false); }
        });
    }

    reset(): void {
        this.fromDate       = this.firstOfMonth();
        this.toDate         = this.lastOfMonth();
        this.selectedOffice = null;
        this.selectedStatus = null;
        this.searchQuery    = '';
        this.page           = 1;
        this.load();
    }

    compareById(a: any, b: any): boolean {
        return a && b ? a.id === b.id : a === b;
    }

    isEditable(rec: any): boolean {
        const s = (rec?.documentStatus?.status || '').toString();
        return s === 'Document Created' || s === 'Returned to Creator';
    }
}
