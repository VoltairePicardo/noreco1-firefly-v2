import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { CaLiquidationService } from '../ca-liquidation.service';

@Component({
    selector: 'app-ca-liquidation-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './ca-liquidation-main.component.html'
})
export class CaLiquidationMainComponent {
    module    = 'CA Liquidation';
    subModule = '';
    menuLink  = 'ca-liquidation';

    records   = signal<any[]>([]);
    isLoading = signal(false);

    searchQuery = '';
    page        = 1;
    pageSize    = 20;

    private service      = inject(CaLiquidationService);
    private alertService = inject(AlertService);

    get filteredRecords(): any[] {
        const q = this.searchQuery.toLowerCase();
        return this.records().filter(r =>
            !q ||
            (r.code || '').toLowerCase().includes(q) ||
            (r.cashAdvanceCode || r.cashAdvance?.code || '').toLowerCase().includes(q) ||
            (r.employee || r.employeeName || '').toLowerCase().includes(q)
        );
    }

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.filteredRecords.slice(start, start + this.pageSize);
    }

    ngOnInit(): void {
        this.load();
    }

    load(): void {
        this.isLoading.set(true);
        this.service.list().subscribe({
            next: (data) => { this.records.set(data || []); this.page = 1; this.isLoading.set(false); },
            error: () => { this.alertService.error(this.module, 'Failed to load records.', ''); this.isLoading.set(false); }
        });
    }

    onSearch(): void {
        this.page = 1;
    }

    reset(): void {
        this.searchQuery = '';
        this.page        = 1;
        this.load();
    }

    isEditable(rec: any): boolean {
        const s = (rec?.status || rec?.documentStatus || '').toString();
        return s === 'Document Created' || s === 'Returned to Creator' || s === '1';
    }
}
