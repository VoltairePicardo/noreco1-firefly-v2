import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { AccountingReportsService } from '../accounting-reports.service';

@Component({
    selector: 'app-balance-sheet',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './balance-sheet.component.html'
})
export class BalanceSheetComponent {
    module   = 'Balance Sheet (BSUP)';
    menuLink = 'accounting-reports';
    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    asOfDate = '';

    rows      = signal<any[]>([]);
    isLoading = signal(false);

    private downloadSvc  = inject(DownloadService);
    private alertService = inject(AlertService);
    private service      = inject(AccountingReportsService);

    ngOnInit(): void {
        this.asOfDate = new Date().toISOString().substring(0, 10);
    }

    search(): void {
        if (!this.asOfDate) {
            this.alertService.error(this.module, 'Validation', 'Please select an as-of date.');
            return;
        }
        this.isLoading.set(true);
        this.rows.set([]);
        this.service.getBalanceSheetData(this.asOfDate).subscribe({
            next: (data) => { this.rows.set(data ?? []); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); }
        });
    }

    export(type: 'pdf' | 'xls'): void {
        if (!this.asOfDate) {
            this.alertService.error(this.module, 'Validation', 'Please select an as-of date.');
            return;
        }
        this.downloadSvc.print(`/reports/export/balance-sheet/${this.asOfDate}`, { type });
    }
}
