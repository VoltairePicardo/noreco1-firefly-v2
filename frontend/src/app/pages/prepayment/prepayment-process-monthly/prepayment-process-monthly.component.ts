import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { PrepaymentService } from '../prepayment.service';

@Component({
    selector: 'app-prepayment-process-monthly',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './prepayment-process-monthly.component.html'
})
export class PrepaymentProcessMonthlyComponent {
    module    = 'Prepayments and Other Amortizations';
    subModule = 'Process Monthly Expense';
    menuLink  = 'prepayment';

    selectedMonth = new Date().getMonth() + 1;
    selectedYear  = new Date().getFullYear();

    isLoading  = signal(false);
    hasLoaded  = signal(false);
    items      = signal<any[]>([]);
    processing = signal<Set<number>>(new Set());
    processed  = signal<Set<number>>(new Set());

    readonly months = [
        { value: 1,  label: 'January'   },
        { value: 2,  label: 'February'  },
        { value: 3,  label: 'March'     },
        { value: 4,  label: 'April'     },
        { value: 5,  label: 'May'       },
        { value: 6,  label: 'June'      },
        { value: 7,  label: 'July'      },
        { value: 8,  label: 'August'    },
        { value: 9,  label: 'September' },
        { value: 10, label: 'October'   },
        { value: 11, label: 'November'  },
        { value: 12, label: 'December'  },
    ];

    private service      = inject(PrepaymentService);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    load(): void {
        this.isLoading.set(true);
        this.hasLoaded.set(false);
        this.items.set([]);
        this.processed.set(new Set());
        this.service.getProcessMonthly(this.selectedMonth, this.selectedYear).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                this.hasLoaded.set(true);
                this.items.set(Array.isArray(data) ? data : []);
            },
            error: () => {
                this.isLoading.set(false);
                this.hasLoaded.set(true);
                this.alertService.error(this.module, 'Load Failed', '');
            }
        });
    }

    clampYear(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.value.length > 4) {
            this.selectedYear = parseInt(input.value.slice(0, 4), 10);
            input.value = String(this.selectedYear);
        }
    }

    process(item: any): void {
        const id: number = item.id;
        const set = new Set(this.processing());
        set.add(id);
        this.processing.set(set);

        const processDate = `${this.selectedYear}-${String(this.selectedMonth).padStart(2, '0')}-01`;
        const newBalance  = (Number(item.balance) || 0) - (Number(item.monthlyCost) || 0);

        const payload = {
            id,
            hasPpd: true,
            ppDetails: [{
                accountNo: item.accountNo,
                month:     this.selectedMonth,
                year:      this.selectedYear,
                amount:    item.monthlyCost,
                balance:   newBalance
            }],
            processDate,
            tempBatch: { id: item.temporaryBatch?.id }
        };

        this.service.create(payload).subscribe({
            next: (res) => {
                const proc = new Set(this.processing());
                proc.delete(id);
                this.processing.set(proc);
                if (res?.success) {
                    const done = new Set(this.processed());
                    done.add(id);
                    this.processed.set(done);
                    this.alertService.success(this.module, 'Processed', item.description || '');
                } else {
                    this.alertService.error(this.module, 'Process Failed', res?.failureMessage || '');
                }
            },
            error: () => {
                const proc = new Set(this.processing());
                proc.delete(id);
                this.processing.set(proc);
                this.alertService.error(this.module, 'Process Failed', '');
            }
        });
    }

    isProcessing(id: number): boolean { return this.processing().has(id); }
    isProcessed(id: number): boolean  { return this.processed().has(id);  }

    cancel(): void {
        this.router.navigate(['/' + this.menuLink]);
    }
}
