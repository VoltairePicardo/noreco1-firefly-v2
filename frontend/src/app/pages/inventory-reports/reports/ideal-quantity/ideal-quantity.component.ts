import { Component, inject, OnInit, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { DownloadService } from '@/app/services/download.service';
import { InventoryReportsService } from '../../inventory-reports.service';
import { AlertService } from '@/app/shared/services/alert.service';

@Component({
    selector: 'app-ideal-quantity',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './ideal-quantity.component.html'
})
export class IdealQuantityComponent implements OnInit {
    module   = 'Ideal Quantity or Reorder Point';
    menuLink = 'inventory-reports';

    locationId   = 0;
    reportTypeId = 0;
    categoryId   = 0;
    locations: any[]  = [];
    categories: any[] = [];

    reportTypes = [
        { id: 0, label: 'For Reorder' },
        { id: 1, label: 'Ideal Quantity' },
    ];

    rows      = signal<any[]>([]);
    isLoading = signal(false);

    private downloadSvc  = inject(DownloadService);
    private service      = inject(InventoryReportsService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.loadLocations();
        this.loadCategories();
    }

    loadLocations(): void {
        this.service.getInventoryLocations().subscribe({
            next: (data) => { this.locations = data || []; },
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
        if (!this.locationId) {
            this.alertService.error(this.module, 'Validation', 'Please select an inventory location.');
            return;
        }
        this.isLoading.set(true);
        this.rows.set([]);
        this.service.getIdealQuantityData(this.locationId, this.reportTypeId, this.categoryId).subscribe({
            next: (data) => { this.rows.set(data); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); }
        });
    }

    export(type: 'pdf' | 'xls'): void {
        if (!this.locationId) {
            this.alertService.error(this.module, 'Validation', 'Please select an inventory location.');
            return;
        }
        this.downloadSvc.print(
            `/reports/export/ideal-quantity-reorder-point/${this.locationId}/${this.reportTypeId}/${this.categoryId}`,
            { type }
        );
    }
}
