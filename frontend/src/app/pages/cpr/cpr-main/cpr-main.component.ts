import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { CprService } from '../cpr.service';

@Component({
    selector: 'app-cpr-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './cpr-main.component.html'
})
export class CprMainComponent {
    module   = 'Asset Records (CPR)';
    subModule = '';
    menuLink = 'cpr';

    records       = signal<any[]>([]);
    isLoading     = signal(false);
    assetTypes    = signal<any[]>([]);
    pageNumber    = signal(0);
    totalPages    = signal(0);
    totalElements = signal(0);
    pageSize = 10;

    // Filters
    searchQ           = '';
    selectedAssetType = '';
    fullyDepreciated  = '';
    assetStatus       = '';

    private service      = inject(CprService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.loadAssetTypes();
        this.load();
    }

    loadAssetTypes(): void {
        this.service.getAssetTypes().subscribe({
            next: (res) => {
                const items = res?.content ?? res ?? [];
                this.assetTypes.set(items);
            },
            error: () => {}
        });
    }

    load(page = 0): void {
        this.isLoading.set(true);
        this.service.listPaged(
            this.searchQ,
            this.selectedAssetType,
            this.fullyDepreciated,
            this.assetStatus,
            page
        ).subscribe({
            next: (res) => {
                this.records.set(res?.content ?? []);
                this.pageNumber.set(res?.page?.number ?? 0);
                this.totalPages.set(res?.page?.totalPages ?? 0);
                this.totalElements.set(res?.page?.totalElements ?? 0);
                this.isLoading.set(false);
            },
            error: () => {
                this.alertService.error(this.module, 'Load', '');
                this.isLoading.set(false);
            }
        });
    }

    search(): void { this.load(0); }

    reset(): void {
        this.searchQ           = '';
        this.selectedAssetType = '';
        this.fullyDepreciated  = '';
        this.assetStatus       = '';
        this.load(0);
    }

    onPageChange(p: number): void { this.load(p - 1); }
}
