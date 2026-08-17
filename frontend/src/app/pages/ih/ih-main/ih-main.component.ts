import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { IhService } from '../ih.service';

@Component({
    selector: 'app-ih-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './ih-main.component.html'
})
export class IhMainComponent {
    module    = 'Item History';
    subModule = '';
    menuLink  = 'ih';

    serialNo   = '';
    item       = signal<any>(null);
    histories  = signal<any[]>([]);
    isLoading  = signal(false);
    searched   = false;

    page     = 1;
    pageSize = 10;

    get pagedHistories(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.histories().slice(start, start + this.pageSize);
    }

    private service      = inject(IhService);
    private alertService = inject(AlertService);

    search(): void {
        if (!this.serialNo?.trim()) {
            this.alertService.warning(this.module, 'Validation', 'Please enter a serial number.');
            return;
        }
        this.isLoading.set(true);
        this.searched = true;
        this.service.searchBySerial(this.serialNo.trim()).subscribe({
            next: (data) => { this.histories.set(data || []); this.page = 1; this.isLoading.set(false); },
            error: () => { this.alertService.error(this.module, 'Search', ''); this.isLoading.set(false); }
        });
    }

    clear(): void {
        this.serialNo = '';
        this.item.set(null);
        this.histories.set([]);
        this.searched = false;
    }
}
