import { Component, inject, signal } from '@angular/core';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { provideIcons } from '@ng-icons/core';
import { tablerArrowLeft, tablerSearch, tablerPlayerPlay } from '@ng-icons/tabler-icons';
import { PrepaymentService } from '../prepayment.service';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

@Component({
    selector: 'app-prepayment-process-monthly',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerArrowLeft, tablerSearch, tablerPlayerPlay })],
    templateUrl: './prepayment-process-monthly.component.html'
})
export class PrepaymentProcessMonthlyComponent {
    module    = 'Prepayments and Other Amortizations';
    subModule = 'Process Monthly Expense';
    menuLink  = 'prepayment';

    selectedMonth = new Date().getMonth() + 1;
    selectedYear  = new Date().getFullYear();

    isLoading       = signal(false);
    hasLoaded       = signal(false);
    items           = signal<any[]>([]);
    isProcessingAll = signal(false);
    processed       = signal<Set<number>>(new Set());

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

    processAll(): void {
        const pending = this.items().filter(item => !this.isProcessed(item.id));
        if (pending.length === 0) {
            this.alertService.warning(this.module, 'Nothing to Process', 'All items have already been processed.');
            return;
        }

        this.isProcessingAll.set(true);
        const processDate = `${this.selectedYear}-${String(this.selectedMonth).padStart(2, '0')}-01`;

        const requests = pending.map(item => {
            const newBalance = (Number(item.balance) || 0) - (Number(item.monthlyCost) || 0);
            const payload = {
                id:        item.id,
                hasPpd:    true,
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
            return this.service.create(payload).pipe(
                map(res => ({ id: item.id, success: !!res?.success })),
                catchError(() => of({ id: item.id, success: false }))
            );
        });

        forkJoin(requests).subscribe({
            next: (results) => {
                this.isProcessingAll.set(false);
                const succeeded = results.filter(r => r.success);
                const failed    = results.length - succeeded.length;

                const done = new Set(this.processed());
                succeeded.forEach(r => done.add(r.id));
                this.processed.set(done);

                const monthLabel = this.months[this.selectedMonth - 1]?.label ?? this.selectedMonth;
                if (failed === 0) {
                    this.alertService.success(this.module, 'Processed',
                        `${succeeded.length} prepayment(s) processed for ${monthLabel} ${this.selectedYear}.`);
                } else {
                    this.alertService.warning(this.module, 'Partial Success',
                        `${succeeded.length} processed, ${failed} failed for ${monthLabel} ${this.selectedYear}.`);
                }
            },
            error: () => {
                this.isProcessingAll.set(false);
                this.alertService.error(this.module, 'Process Failed', '');
            }
        });
    }

    isProcessed(id: number): boolean { return this.processed().has(id); }
}
