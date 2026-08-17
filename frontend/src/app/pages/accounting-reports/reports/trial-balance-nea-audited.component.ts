import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { AccountingReportsService } from '../accounting-reports.service';

@Component({
    selector: 'app-trial-balance-nea-audited',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './trial-balance-nea-audited.component.html'
})
export class TrialBalanceNeaAuditedComponent {
    module   = 'Trial Balance - Audited (NEA)';
    menuLink = 'accounting-reports';
    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    cutOffDate = '';

    rows      = signal<any[]>([]);
    isLoading = signal(false);

    private downloadSvc  = inject(DownloadService);
    private alertService = inject(AlertService);
    private service      = inject(AccountingReportsService);

    ngOnInit(): void {
        this.cutOffDate = new Date().toISOString().substring(0, 10);
    }

    search(): void {
        if (!this.cutOffDate) {
            this.alertService.error(this.module, 'Validation', 'Please select a cut-off date.');
            return;
        }
        this.isLoading.set(true);
        this.rows.set([]);
        this.service.getTrialBalanceNeaAuditedData(this.cutOffDate).subscribe({
            next: (data) => { this.rows.set(data ?? []); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); }
        });
    }

    export(type: 'pdf' | 'xls'): void {
        if (!this.cutOffDate) {
            this.alertService.error(this.module, 'Validation', 'Please select a cut-off date.');
            return;
        }
        this.downloadSvc.print(`/reports/export/trial-balance-nea-audited/${this.cutOffDate}`, { type });
    }
}
