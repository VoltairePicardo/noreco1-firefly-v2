import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { AccountingReportsService } from '../accounting-reports.service';

@Component({
    selector: 'app-asset-monitoring-sheet',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './asset-monitoring-sheet.component.html'
})
export class AssetMonitoringSheetComponent {
    module   = 'Asset Monitoring Sheet';
    menuLink = 'accounting-reports';

    searchText           = '';
    selectedRecord: any  = null;
    searchResults        = signal<any[]>([]);
    isSearching          = signal(false);

    private downloadSvc  = inject(DownloadService);
    private alertService = inject(AlertService);
    private service      = inject(AccountingReportsService);

    search(): void {
        this.isSearching.set(true);
        this.searchResults.set([]);
        this.service.getMaintenanceRecords(this.searchText).subscribe({
            next: (data) => {
                this.searchResults.set(data?.content ?? data ?? []);
                this.isSearching.set(false);
            },
            error: () => { this.isSearching.set(false); }
        });
    }

    select(record: any): void {
        this.selectedRecord = record;
        this.searchResults.set([]);
    }

    export(): void {
        if (!this.selectedRecord) {
            this.alertService.error(this.module, 'Validation', 'Please select a maintenance record.');
            return;
        }
        this.downloadSvc.print(
            `/reports/export/asset-monitoring-sheet/${this.selectedRecord.id}`,
            { type: 'pdf' }
        );
    }
}
