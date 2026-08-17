import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { AccountingReportsService } from '../accounting-reports.service';

@Component({
    selector: 'app-depreciation-schedule',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './depreciation-schedule.component.html'
})
export class DepreciationScheduleComponent {
    module   = 'Depreciation Schedule';
    menuLink = 'accounting-reports';

    selectedYear = signal<number>(new Date().getFullYear());
    years: number[] = [];

    rows      = signal<any[]>([]);
    isLoading = signal(false);

    private downloadSvc  = inject(DownloadService);
    private alertService = inject(AlertService);
    private service      = inject(AccountingReportsService);

    ngOnInit(): void {
        const currentYear = new Date().getFullYear();
        for (let y = currentYear - 5; y <= currentYear + 2; y++) this.years.push(y);
    }

    search(): void {
        const year = this.selectedYear();
        if (!year) {
            this.alertService.error(this.module, 'Validation', 'Please select a year.');
            return;
        }
        this.isLoading.set(true);
        this.rows.set([]);
        this.service.getDepreciationSchedule(year).subscribe({
            next: (data) => { this.rows.set(data); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); }
        });
    }

    export(type: 'pdf' | 'xls'): void {
        const year = this.selectedYear();
        if (!year) {
            this.alertService.error(this.module, 'Validation', 'Please select a year.');
            return;
        }
        this.downloadSvc.print(`/reports/export/depreciation-schedule/${year}`, { type });
    }
}
