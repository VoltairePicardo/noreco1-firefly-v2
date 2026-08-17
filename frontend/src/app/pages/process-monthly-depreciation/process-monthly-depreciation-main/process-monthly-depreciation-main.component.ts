import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { ProcessMonthlyDepreciationService } from '../process-monthly-depreciation.service';

const MONTH_NAMES = [
    '', 'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
];

@Component({
    selector: 'app-process-monthly-depreciation-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './process-monthly-depreciation-main.component.html'
})
export class ProcessMonthlyDepreciationMainComponent {
    module    = 'Process Monthly Depreciation';
    subModule = '';
    menuLink  = 'process-monthly-depreciation';

    records       = signal<any[]>([]);
    isLoading     = signal(false);
    isProcessing  = signal(false);
    pageNumber    = signal(0);
    totalElements = signal(0);
    pageSize = 10;

    get pagedRecords(): any[] {
        const start = this.pageNumber() * this.pageSize;
        return this.records().slice(start, start + this.pageSize);
    }

    years: number[] = [];
    months = [
        { id: 1,  name: 'January'   },
        { id: 2,  name: 'February'  },
        { id: 3,  name: 'March'     },
        { id: 4,  name: 'April'     },
        { id: 5,  name: 'May'       },
        { id: 6,  name: 'June'      },
        { id: 7,  name: 'July'      },
        { id: 8,  name: 'August'    },
        { id: 9,  name: 'September' },
        { id: 10, name: 'October'   },
        { id: 11, name: 'November'  },
        { id: 12, name: 'December'  },
    ];

    selectedYear  = signal<number | null>(null);
    selectedMonth = signal<number | null>(null);

    private service      = inject(ProcessMonthlyDepreciationService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        const currentYear = new Date().getFullYear();
        for (let y = currentYear - 5; y <= currentYear + 2; y++) {
            this.years.push(y);
        }
        this.selectedYear.set(currentYear);
        this.selectedMonth.set(new Date().getMonth() + 1);
        this.load();
    }

    getMonthName(id: number): string {
        return MONTH_NAMES[id] || '';
    }

    load(): void {
        const year  = this.selectedYear();
        const month = this.selectedMonth();
        if (!year || !month) return;

        this.isLoading.set(true);
        this.service.list(year, month).subscribe({
            next: (data) => {
                this.records.set(data || []);
                this.pageNumber.set(0);
                this.totalElements.set((data || []).length);
                this.isLoading.set(false);
            },
            error: () => {
                this.alertService.error(this.module, 'Load', '');
                this.isLoading.set(false);
            }
        });
    }

    onFilterChange(): void { this.load(); }

    onPageChange(p: number): void { this.pageNumber.set(p - 1); }

    processDepreciation(): void {
        const year  = this.selectedYear();
        const month = this.selectedMonth();

        if (!year || !month) {
            this.alertService.warning(this.module, 'Validation', 'Please select a year and month before processing.');
            return;
        }

        const monthName = this.getMonthName(month);
        this.alertService.confirm(
            `Process depreciation for <strong>${monthName} ${year}</strong>?`
        ).then(result => {
            if (!result.isConfirmed) return;

            this.isProcessing.set(true);
            this.service.process({ year, month }).subscribe({
                next: (res) => {
                    this.isProcessing.set(false);
                    if (res?.success === false) {
                        this.alertService.error(this.module, 'Process', res.failureMessage || '');
                    } else {
                        this.alertService.success(this.module, 'Processed', '');
                        this.load();
                    }
                },
                error: () => {
                    this.isProcessing.set(false);
                    this.alertService.error(this.module, 'Process', '');
                }
            });
        });
    }
}
