import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { MonthlyClosingService } from '../monthly-closing.service';

@Component({
    selector: 'app-monthly-closing-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './monthly-closing-main.component.html'
})
export class MonthlyClosingMainComponent {
    module    = 'Monthly Closing';
    subModule = '';
    menuLink  = 'monthly-closing';

    records   = signal<any[]>([]);
    isLoading = signal(false);

    page     = 1;
    pageSize = 10;

    searchText = '';

    get filteredRecords(): any[] {
        if (!this.searchText.trim()) return this.records();
        const q = this.searchText.toLowerCase();
        return this.records().filter(r =>
            this.formatMonth(r.year, r.month).toLowerCase().includes(q) ||
            (r.status || '').toLowerCase().includes(q)
        );
    }

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.filteredRecords.slice(start, start + this.pageSize);
    }

    private service      = inject(MonthlyClosingService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.load();
    }

    load(): void {
        this.isLoading.set(true);
        this.service.list().subscribe({
            next: (data) => { this.records.set(data || []); this.page = 1; this.isLoading.set(false); },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    formatMonth(year: number, month: number): string {
        if (!year || !month) return '—';
        const d = new Date(year, month - 1, 1);
        return d.toLocaleString('default', { month: 'long', year: 'numeric' });
    }
}
