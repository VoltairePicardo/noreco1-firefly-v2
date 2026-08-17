import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { CheckConfigService } from '../check-config.service';

@Component({
    selector: 'app-check-config-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './check-config-main.component.html'
})
export class CheckConfigMainComponent {
    module    = 'Check Config';
    subModule = '';
    menuLink  = 'check-config';

    checkConfigs = signal<any[]>([]);
    searchText   = '';

    page     = 1;
    pageSize = 10;

    get filteredRecords(): any[] {
        const q = this.searchText.toLowerCase().trim();
        return q
            ? this.checkConfigs().filter(c =>
                (c.code || '').toLowerCase().includes(q) ||
                (c.dateFormat || '').toLowerCase().includes(q) ||
                (c.checkNoPrefix || '').toLowerCase().includes(q))
            : this.checkConfigs();
    }

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.filteredRecords.slice(start, start + this.pageSize);
    }

    isLoading    = signal(false);

    private service      = inject(CheckConfigService);
    private alertService = inject(AlertService);

    ngOnInit(): void { this.load(); }

    search():      void { this.page = 1; }
    clearSearch(): void { this.searchText = ''; this.page = 1; }

    load(): void {
        this.isLoading.set(true);
        this.service.list().subscribe({
            next: (data) => {
                this.checkConfigs.set(data);
            this.page = 1;
                this.isLoading.set(false);
            },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }
}
