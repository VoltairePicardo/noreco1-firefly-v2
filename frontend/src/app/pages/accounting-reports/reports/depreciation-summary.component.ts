import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { AccountingReportsService } from '../accounting-reports.service';

@Component({
    selector: 'app-depreciation-summary',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './depreciation-summary.component.html'
})
export class DepreciationSummaryComponent {
    module   = 'Depreciation Summary';
    menuLink = 'accounting-reports';

    selectedYear  = signal<number>(new Date().getFullYear());
    selectedMonth = signal<number>(new Date().getMonth() + 1);

    years: number[] = [];
    months = [
        { id: 1,  name: 'January'   }, { id: 2,  name: 'February'  }, { id: 3,  name: 'March'     },
        { id: 4,  name: 'April'     }, { id: 5,  name: 'May'       }, { id: 6,  name: 'June'      },
        { id: 7,  name: 'July'      }, { id: 8,  name: 'August'    }, { id: 9,  name: 'September' },
        { id: 10, name: 'October'   }, { id: 11, name: 'November'  }, { id: 12, name: 'December'  },
    ];

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
        const year  = this.selectedYear();
        const month = this.selectedMonth();
        if (!year || !month) {
            this.alertService.error(this.module, 'Validation', 'Please select year and month.');
            return;
        }
        this.isLoading.set(true);
        this.rows.set([]);
        this.service.getDepreciationSummary(year, month).subscribe({
            next: (data) => { this.rows.set(data); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); }
        });
    }

    export(type: 'pdf' | 'xls'): void {
        const year  = this.selectedYear();
        const month = this.selectedMonth();
        if (!year || !month) {
            this.alertService.error(this.module, 'Validation', 'Please select year and month.');
            return;
        }
        this.downloadSvc.print(`/reports/export/depreciation-summary/${year}/${month}`, { type });
    }
}
