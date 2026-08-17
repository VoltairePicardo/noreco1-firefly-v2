import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { AllocationFactorService } from '../allocation-factor.service';

@Component({
    selector: 'app-allocation-factor-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './allocation-factor-main.component.html'
})
export class AllocationFactorMainComponent {
    module    = 'Allocation Factor';
    subModule = '';
    menuLink  = 'allocation-factor';

    factors       = signal<any[]>([]);
    isLoading     = signal(false);
    pageNumber    = signal(0);
    totalPages    = signal(0);
    totalElements = signal(0);
    pageSize      = 10;
    searchText    = '';

    private service      = inject(AllocationFactorService);
    private alertService = inject(AlertService);

    ngOnInit(): void { this.load(); }

    load(page = 0): void {
        this.isLoading.set(true);
        this.service.list(this.searchText, page).subscribe({
            next: (data) => {
                this.factors.set(data.content);
                this.pageNumber.set(data.page.number);
                this.totalPages.set(data.page.totalPages);
                this.totalElements.set(data.page.totalElements);
                this.isLoading.set(false);
            },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    search():              void { this.load(0); }
    clearSearch():         void { this.searchText = ''; this.load(0); }
    onPageChange(p: number): void { this.load(p - 1); }

    async delete(factor: any): Promise<void> {
        const result = await this.alertService.confirm(`Delete factor <strong>${factor.code}</strong>?`);
        if (!result.isConfirmed) return;
        this.service.delete(factor.id).subscribe({
            next: (res: any) => {
                if (res?.success) { this.alertService.success(this.module, 'Deleted', ''); this.load(this.pageNumber()); }
                else { this.alertService.error(this.module, 'Delete', res?.failureMessage || ''); }
            },
            error: () => { this.alertService.error(this.module, 'Delete', ''); }
        });
    }
}
