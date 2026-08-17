import { Component, inject, OnInit, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { DownloadService } from '@/app/services/download.service';
import { InventoryReportsService } from '../../inventory-reports.service';
import { AlertService } from '@/app/shared/services/alert.service';

@Component({
    selector: 'app-material-issuance-summary',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './material-issuance-summary.component.html'
})
export class MaterialIssuanceSummaryComponent implements OnInit {
    module   = 'Summary of Material Periodic Issuance';
    menuLink = 'inventory-reports';

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    fromDate   = '';
    toDate     = '';
    locationId = 0;
    categoryId = 0;
    locations: any[] = [];
    categories: any[] = [];

    rows          = signal<any[]>([]);
    isLoading     = signal(false);
    currentPage   = signal(0);
    totalElements = signal(0);
    pageSize      = 10;

    private downloadSvc  = inject(DownloadService);
    private service      = inject(InventoryReportsService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.setDefaultDates();
        this.loadLocations();
        this.loadCategories();
    }

    setDefaultDates(): void {
        const now   = new Date();
        const first = new Date(now.getFullYear(), now.getMonth(), 1);
        this.fromDate = first.toISOString().substring(0, 10);
        this.toDate   = now.toISOString().substring(0, 10);
    }

    loadLocations(): void {
        this.service.getInventoryLocations().subscribe({
            next: (data) => { this.locations = [{ id: 0, description: 'All Locations' }, ...(data || [])]; },
            error: () => { this.alertService.error(this.module, 'Load', 'Failed to load inventory locations.'); }
        });
    }

    loadCategories(): void {
        this.service.getInventoryCategories().subscribe({
            next: (data) => { this.categories = [{ id: 0, description: 'All Categories' }, ...(data || [])]; },
            error: () => { this.alertService.error(this.module, 'Load', 'Failed to load inventory categories.'); }
        });
    }

    search(): void {
        this.currentPage.set(0);
        this.loadPage();
    }

    goToPage(p: number): void {
        this.currentPage.set(p);
        this.loadPage();
    }

    private loadPage(): void {
        this.isLoading.set(true);
        this.rows.set([]);
        const locId = this.locationId || null;
        const catId = this.categoryId || null;
        this.service.getMaterialIssuanceSummaryPaged(this.fromDate, this.toDate, catId, locId, this.currentPage(), this.pageSize).subscribe({
            next: (resp) => {
                this.rows.set(resp.content ?? []);
                this.totalElements.set(resp.page?.totalElements ?? 0);
                this.isLoading.set(false);
            },
            error: () => { this.isLoading.set(false); }
        });
    }

    get totalPages(): number {
        return Math.ceil(this.totalElements() / this.pageSize);
    }

    export(type: 'pdf' | 'xls'): void {
        const params: any = { type, s: this.fromDate, e: this.toDate };
        if (this.categoryId) params['c'] = this.categoryId;
        if (this.locationId) params['l'] = this.locationId;
        this.downloadSvc.print(`/reports/export/material-issuance-summary`, params);
    }
}
