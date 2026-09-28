import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { InitialReadingEntryService } from '../initial-reading-entry.service';

@Component({
    selector: 'app-initial-reading-entry-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './initial-reading-entry-main.component.html'
})
export class InitialReadingEntryMainComponent {
    module    = 'Initial Reading Entry';
    subModule = '';
    menuLink  = 'initial-reading-entry';

    records       = signal<any[]>([]);
    isLoading     = signal(false);
    pageNumber    = signal(0);
    totalPages    = signal(0);
    totalElements = signal(0);
    pageSize      = 10;
    searchText    = '';

    private service      = inject(InitialReadingEntryService);
    private alertService = inject(AlertService);

    ngOnInit(): void { this.load(); }

    load(page = 0): void {
        this.isLoading.set(true);
        this.service.list(this.searchText, page, this.pageSize).subscribe({
            next: (data) => {
                this.records.set(data.content);
                this.pageNumber.set(data.page.number);
                this.totalPages.set(data.page.totalPages);
                this.totalElements.set(data.page.totalElements);
                this.isLoading.set(false);
            },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    search():      void { this.load(0); }
    clearSearch(): void { this.searchText = ''; this.load(0); }
    onPageChange(p: number): void { this.load(p - 1); }
}
