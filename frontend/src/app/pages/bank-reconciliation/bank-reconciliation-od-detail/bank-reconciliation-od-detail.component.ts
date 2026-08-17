import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { BankReconciliationService } from '../bank-reconciliation.service';

@Component({
    selector: 'app-bank-reconciliation-od-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './bank-reconciliation-od-detail.component.html'
})
export class BankReconciliationOdDetailComponent {
    module    = 'Bank Reconciliation';
    subModule = 'Other Deposit Detail';
    menuLink  = 'bank-reconciliation';

    id: any   = null;
    data: any = {};
    isLoading = signal(false);

    private service      = inject(BankReconciliationService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            if (idParam && /^\d+$/.test(idParam)) {
                this.id = Number(idParam);
                this.loadData();
            }
        });
    }

    loadData(): void {
        this.isLoading.set(true);
        this.service.getOd(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.data = data;
                } else {
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Error', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    get accountLabel(): string {
        const acc = this.data?.account;
        if (!acc) return '—';
        const code  = acc.accountCode  || acc.code  || '';
        const title = acc.accountTitle || acc.title || '';
        return code && title ? `${code} - ${title}` : code || title || '—';
    }
}
