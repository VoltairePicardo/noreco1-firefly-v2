import { Component, computed, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { AccountingReportsService } from '../accounting-reports.service';

@Component({
    selector: 'app-mir-register',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './mir-register.component.html'
})
export class MirRegisterComponent {
    module   = 'Material Issue Register';
    menuLink = 'accounting-reports';
    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    fromDate         = '';
    toDate           = '';
    selectedStatusId = 0;
    invDocType       = '';
    documentStatuses = signal<any[]>([]);
    rows             = signal<any[]>([]);
    recapRows        = signal<any[]>([]);
    isLoading        = signal(false);

    totalDebit  = computed(() => this.rows().reduce((s, r) => s + (r.debit  || 0), 0));
    totalCredit = computed(() => this.rows().reduce((s, r) => s + (r.credit || 0), 0));
    totalGlDebit   = computed(() => this.recapRows().reduce((s, r) => s + (r.glDebit  || 0), 0));
    totalGlCredit  = computed(() => this.recapRows().reduce((s, r) => s + (r.glCredit || 0), 0));
    totalSlDebit   = computed(() => this.recapRows().reduce((s, r) => s + (r.slDebit  || 0), 0));
    totalSlCredit  = computed(() => this.recapRows().reduce((s, r) => s + (r.slCredit || 0), 0));

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
        this.service.getMirRegister(this.fromDate, this.toDate, this.selectedStatusId, this.invDocType).subscribe({
            next: (data) => { this.rows.set(data || []); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); }
        });
        this.service.getMirRegisterRecap(this.fromDate, this.toDate, this.selectedStatusId, this.invDocType).subscribe({
            next: (data) => this.recapRows.set(data || [])
        });
    }

    export(type: 'pdf' | 'xls'): void {
        if (!this.fromDate || !this.toDate) {
            this.alertService.error(this.module, 'Validation', 'Please select a date range.');
            return;
        }
        const url = this.invDocType
            ? `/reports/export/mir/${this.invDocType}/${this.fromDate}/${this.toDate}/${this.selectedStatusId}`
            : `/reports/export/mir/${this.fromDate}/${this.toDate}/${this.selectedStatusId}`;
        this.downloadSvc.print(url, { type });
    }
}
