import { Component, computed, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { AccountingReportsService } from '../accounting-reports.service';

@Component({
    selector: 'app-pe-summary',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './pe-summary.component.html'
})
export class PeSummaryComponent {
    module   = 'Prepayment Expense Summary';
    menuLink = 'accounting-reports';

    selectedMonth = new Date().getMonth() + 1;  // 1-based
    selectedYear  = new Date().getFullYear();

    months = [
        { id: 1,  name: 'January'   }, { id: 2,  name: 'February'  }, { id: 3,  name: 'March'     },
        { id: 4,  name: 'April'     }, { id: 5,  name: 'May'       }, { id: 6,  name: 'June'      },
        { id: 7,  name: 'July'      }, { id: 8,  name: 'August'    }, { id: 9,  name: 'September' },
        { id: 10, name: 'October'   }, { id: 11, name: 'November'  }, { id: 12, name: 'December'  },
    ];

    years: number[] = [];
    rows      = signal<any[]>([]);
    isLoading = signal(false);

    totalVouchers = computed(() => this.rows().length);
    totalAmount   = computed(() => this.rows().reduce((s, r) => s + (r.amount  || 0), 0));
    totalBalance  = computed(() => this.rows().reduce((s, r) => s + (r.balance || 0), 0));

    private downloadSvc  = inject(DownloadService);
    private alertService = inject(AlertService);
    private service      = inject(AccountingReportsService);

    ngOnInit(): void {
        const current = new Date().getFullYear();
        for (let y = current - 5; y <= current + 2; y++) this.years.push(y);
    }

    get selectedMonthName(): string {
        return this.months.find(m => m.id === this.selectedMonth)?.name ?? '';
    }

    search(): void {
        this.isLoading.set(true);
        this.rows.set([]);
        this.service.getPeSummary(this.selectedMonth, this.selectedYear).subscribe({
            next: (data) => { this.rows.set(data ?? []); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); }
        });
    }

    export(type: 'pdf' | 'xls'): void {
        this.downloadSvc.print(
            `/reports/export/pe-summary/${this.selectedMonth}/${this.selectedYear}/${this.selectedMonthName}`,
            { type }
        );
    }
}
