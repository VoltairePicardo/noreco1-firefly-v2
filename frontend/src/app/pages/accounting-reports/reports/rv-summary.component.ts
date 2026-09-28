import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { AccountingReportsService } from '../accounting-reports.service';

@Component({
    selector: 'app-rv-summary',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './rv-summary.component.html'
})
export class RvSummaryComponent {
    module   = 'RV Summary';
    menuLink = 'accounting-reports';
    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    fromDate         = '';
    toDate           = '';
    selectedStatusId = 0;
    documentStatuses = signal<any[]>([]);
    rows             = signal<any[]>([]);
    isLoading        = signal(false);

    private downloadSvc  = inject(DownloadService);
    private alertService = inject(AlertService);
    private service      = inject(AccountingReportsService);

    ngOnInit(): void {
        this.setDefaultDates();
        this.service.getDocumentStatuses().subscribe({
            next: (data) => this.documentStatuses.set([{ id: 0, status: 'All' }, ...(data || [])])
        });
    }

    setDefaultDates(): void {
        const now   = new Date();
        const first = new Date(now.getFullYear(), now.getMonth(), 1);
        const last  = new Date(now.getFullYear(), now.getMonth() + 1, 0);
        const fmt   = (d: Date) => `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
        this.fromDate = fmt(first);
        this.toDate   = fmt(last);
    }

    search(): void {
        if (!this.fromDate || !this.toDate) {
            this.alertService.error(this.module, 'Validation', 'Please select a date range.');
            return;
        }
        this.isLoading.set(true);
        this.service.getRvSummary(this.fromDate, this.toDate, this.selectedStatusId).subscribe({
            next: (data) => { this.rows.set(data || []); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); }
        });
    }

    export(type: 'pdf' | 'xls'): void {
        if (!this.fromDate || !this.toDate) {
            this.alertService.error(this.module, 'Validation', 'Please select a date range.');
            return;
        }
        this.downloadSvc.print(`/reports/export/rv-summary/${this.fromDate}/${this.toDate}/${this.selectedStatusId}`, { type });
    }
}
