import { Component, inject, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { DownloadService } from '@/app/services/download.service';
import { InventoryReportsService } from '../../inventory-reports.service';
import { AlertService } from '@/app/shared/services/alert.service';

@Component({
    selector: 'app-inventory-balance',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './inventory-balance.component.html'
})
export class InventoryBalanceComponent implements OnInit {
    module   = 'Inventory Balance';
    menuLink = 'inventory-reports';

    locationId  = 0;
    categoryId  = 0;
    locations: any[] = [];
    categories: any[] = [];

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

    export(type: 'pdf' | 'xls'): void {
        if (!this.locationId) {
            this.alertService.error(this.module, 'Validation', 'Please select an inventory location.');
            return;
        }
        this.downloadSvc.print(
            `/reports/export/inventory-balance/${this.locationId}/${this.categoryId}`,
            { type }
        );
    }
}
