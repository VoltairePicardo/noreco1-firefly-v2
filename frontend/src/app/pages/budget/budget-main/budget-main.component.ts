import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { BudgetService } from '../budget.service';

@Component({
    selector: 'app-budget-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './budget-main.component.html'
})
export class BudgetMainComponent {
    module    = 'Cash Flow Budget';
    subModule = '';
    menuLink  = 'budget';

    records   = signal<any[]>([]);
    isLoading = signal(false);

    page     = 1;
    pageSize = 10;

    filterYear = '';

    get filteredRecords(): any[] {
        if (!this.filterYear) return this.records();
        return this.records().filter(r => String(r.year).includes(this.filterYear));
    }

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.filteredRecords.slice(start, start + this.pageSize);
    }

    private service      = inject(BudgetService);
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

    reset(): void {
        this.filterYear = '';
        this.load();
    }
}
