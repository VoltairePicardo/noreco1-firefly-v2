import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { AccountingReportsService } from '../accounting-reports.service';

@Component({
    selector: 'app-pcf-ledger',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './pcf-ledger.component.html'
})
export class PcfLedgerComponent {
    module   = 'PCF Ledger';
    menuLink = 'accounting-reports';
    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    fromDate         = '';
    toDate           = '';
    docStatId        = 0;
    pcfId            = 0;
    documentStatuses = signal<any[]>([]);
    pcfFunds         = signal<any[]>([]);
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
        this.service.getPcfList().subscribe({
            next: (res) => {
                const list = res?.content ?? res ?? [];
                this.pcfFunds.set([{ id: 0, description: 'Select Petty Cash Fund' }, ...list]);
            }
        });
    }

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
        if (!this.pcfId) {
            this.alertService.error(this.module, 'Validation', 'Please select a Petty Cash Fund.');
            return;
        }
        this.isLoading.set(true);
        this.service.getPcfLedger(this.fromDate, this.toDate, this.docStatId, this.pcfId).subscribe({
            next: (data) => { this.rows.set(data || []); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); }
        });
    }

    export(type: 'pdf' | 'xls'): void {
        if (!this.fromDate || !this.toDate) {
            this.alertService.error(this.module, 'Validation', 'Please select a date range.');
            return;
        }
        if (!this.pcfId) {
            this.alertService.error(this.module, 'Validation', 'Please select a Petty Cash Fund.');
            return;
        }
        this.downloadSvc.print(
            `/reports/export/petty-cash-fund-ledger/${this.fromDate}/${this.toDate}/${this.docStatId}/${this.pcfId}`,
            { type }
        );
    }
}
