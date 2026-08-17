import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { AccountingReportsService } from '../accounting-reports.service';

@Component({
    selector: 'app-transaction-summary',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './transaction-summary.component.html'
})
export class TransactionSummaryComponent {
    module   = 'Transaction Summary (BSUP)';
    menuLink = 'accounting-reports';
    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    fromDate = '';
    toDate   = '';

    rows      = signal<any[]>([]);
    meta      = signal<any>(null);
    isLoading = signal(false);

    private downloadSvc  = inject(DownloadService);
    private alertService = inject(AlertService);
    private service      = inject(AccountingReportsService);

    ngOnInit(): void { this.setDefaultDates(); }

    setDefaultDates(): void {
        const now = new Date(), first = new Date(now.getFullYear(), now.getMonth(), 1);
        this.fromDate = first.toISOString().substring(0, 10);
        this.toDate   = now.toISOString().substring(0, 10);
    }

    search(): void {
        if (!this.fromDate || !this.toDate) {
            this.alertService.error(this.module, 'Validation', 'Please select a date range.');
            return;
        }
        this.isLoading.set(true);
        this.rows.set([]);
        this.meta.set(null);
        this.service.getTransactionSummaryData(this.fromDate, this.toDate).subscribe({
            next: (res) => {
                this.rows.set(res?.data ?? []);
                this.meta.set(res?.meta ?? null);
                this.isLoading.set(false);
            },
            error: () => { this.isLoading.set(false); }
        });
    }

    export(type: 'pdf' | 'xls'): void {
        if (!this.fromDate || !this.toDate) {
            this.alertService.error(this.module, 'Validation', 'Please select a date range.');
            return;
        }
        this.downloadSvc.print(`/reports/export/transaction-summary-per-account/${this.fromDate}/${this.toDate}`, { type });
    }
}
