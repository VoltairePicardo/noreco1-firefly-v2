import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { IemopBillingService } from '../iemop-billing.service';

@Component({
    selector: 'app-iemop-billing-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './iemop-billing-main.component.html'
})
export class IemopBillingMainComponent {
    module    = 'IEMOP Billing';
    subModule = '';
    menuLink  = 'iemop-billing';

    records       = signal<any[]>([]);
    isLoading     = signal(false);
    pageNumber    = signal(0);
    totalPages    = signal(0);
    totalElements = signal(0);
    pageSize      = 20;
    searchQuery   = '';

    private service      = inject(IemopBillingService);
    private alertService = inject(AlertService);

    ngOnInit(): void { this.load(); }

    load(page = 0): void {
        this.isLoading.set(true);
        this.service.list(page, this.pageSize, this.searchQuery).subscribe({
            next: (data) => {
                this.records.set(data.content || []);
                this.pageNumber.set(data.page.number);
                this.totalPages.set(data.page.totalPages);
                this.totalElements.set(data.page.totalElements);
                this.isLoading.set(false);
            },
            error: () => {
                this.alertService.error(this.module, 'Failed to load records.', '');
                this.isLoading.set(false);
            }
        });
    }

    search():                     void { this.load(0); }
    reset():                      void { this.searchQuery = ''; this.load(0); }
    onPageChange(p: number):      void { this.load(p - 1); }
}
