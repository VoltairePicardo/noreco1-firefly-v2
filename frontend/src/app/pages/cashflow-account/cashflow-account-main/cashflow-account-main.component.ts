import { Component, inject, signal, computed } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { CashflowAccountService } from '../cashflow-account.service';

@Component({
    selector: 'app-cashflow-account-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './cashflow-account-main.component.html'
})
export class CashflowAccountMainComponent {
    module    = 'Cash Flow Account';
    subModule = '';
    menuLink  = 'cashflow-account';

    allItems  = signal<any[]>([]);

    page     = 1;
    pageSize = 10;

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.items().slice(start, start + this.pageSize);
    }

    isLoading = signal(false);
    searchText = '';

    items = computed(() => {
        const q = this.searchText.toLowerCase().trim();
        if (!q) return this.allItems();
        return this.allItems().filter(d =>
            (d.name || '').toLowerCase().includes(q) ||
            (d.cashflowItemType?.name || '').toLowerCase().includes(q)
        );
    });

    private service      = inject(CashflowAccountService);
    private alertService = inject(AlertService);

    ngOnInit(): void { this.load(); }

    load(): void {
        this.isLoading.set(true);
        this.service.list().subscribe({
            next: (data) => {
                this.allItems.set(Array.isArray(data) ? data : []);
            this.page = 1;
                this.isLoading.set(false);
            },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    search():      void { this.page = 1; this.allItems.update(v => [...v]); }
    clearSearch(): void { this.searchText = ''; this.page = 1; this.allItems.update(v => [...v]); }
}
