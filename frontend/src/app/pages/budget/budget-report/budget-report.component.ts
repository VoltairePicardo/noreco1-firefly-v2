import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { provideIcons } from '@ng-icons/core';
import { tablerArrowLeft, tablerPrinter, tablerSearch } from '@ng-icons/tabler-icons';
import { BudgetService } from '../budget.service';

@Component({
    selector: 'app-budget-report',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerArrowLeft, tablerPrinter, tablerSearch })],
    templateUrl: './budget-report.component.html'
})
export class BudgetReportComponent {
    module    = 'Cash Flow Budget';
    subModule = 'Report';
    menuLink  = 'budget';

    budgets          = signal<any[]>([]);
    details          = signal<any[]>([]);
    isLoading        = signal(false);
    isLoadingDetails = signal(false);

    selectedYear: number | null = null;
    searchQuery = '';

    private service      = inject(BudgetService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.isLoading.set(true);
        this.service.list().subscribe({
            next: (data) => { this.budgets.set(data || []); this.isLoading.set(false); },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    get years(): number[] {
        return [...new Set(this.budgets().map((b: any) => b.year))].sort((a: number, b: number) => b - a);
    }

    onYearChange(): void {
        this.searchQuery = '';
        this.details.set([]);
        if (!this.selectedYear) return;
        const budget = this.budgets().find((b: any) => b.year === this.selectedYear);
        if (!budget) return;
        this.isLoadingDetails.set(true);
        this.service.getDetails(budget.id).subscribe({
            next: (data) => { this.details.set(data || []); this.isLoadingDetails.set(false); },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoadingDetails.set(false); }
        });
    }

    get filteredDetails(): any[] {
        const q = this.searchQuery.trim().toLowerCase();
        if (!q) return this.details();
        return this.details().filter((d: any) =>
            (d.cashflowItem?.name || '').toLowerCase().includes(q) ||
            (d.cashflowItem?.cashflowItemType?.name || '').toLowerCase().includes(q)
        );
    }

    get totalAmount(): number {
        return this.filteredDetails.reduce((sum, d) => sum + (Number(d.amount) || 0), 0);
    }

    print(): void {
        const budget = this.budgets().find((b: any) => b.year === this.selectedYear);
        if (!budget) return;
        this.service.print(budget.id, this.selectedYear!);
    }
}
