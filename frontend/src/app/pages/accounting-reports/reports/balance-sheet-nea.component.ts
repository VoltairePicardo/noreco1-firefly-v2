import { Component, inject, signal, computed } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { AccountingReportsService } from '../accounting-reports.service';

@Component({
    selector: 'app-balance-sheet-nea',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './balance-sheet-nea.component.html'
})
export class BalanceSheetNeaComponent {
    module   = 'Balance Sheet (NEA)';
    menuLink = 'accounting-reports';
    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    asOfDate      = '';
    fsType        = 'Unaudited';
    fsTypeOptions = ['Audited', 'Unaudited', 'Closed'];

    rows      = signal<any[]>([]);
    isLoading = signal(false);

    leftRows   = computed(() => this.rows().filter(r => r.column === 'L'));
    rightRows  = computed(() => this.rows().filter(r => r.column === 'R'));
    totalLeft  = computed(() => this.leftRows().filter(r => r.total).reduce((s: number, r: any) => s + (r.amount ?? 0), 0));
    totalRight = computed(() => this.rightRows().filter(r => r.total).reduce((s: number, r: any) => s + (r.amount ?? 0), 0));

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
        this.service.getBalanceSheetNeaData(this.asOfDate, this.fsType).subscribe({
            next: (data) => { this.rows.set(data ?? []); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); }
        });
    }

    export(type: 'pdf' | 'xls'): void {
        if (!this.asOfDate) {
            this.alertService.error(this.module, 'Validation', 'Please select an as-of date.');
            return;
        }
        this.downloadSvc.print(`/reports/export/balance-sheet-nea/${this.asOfDate}/${this.fsType}`, { type });
    }
}
