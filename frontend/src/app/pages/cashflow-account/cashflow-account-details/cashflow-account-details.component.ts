import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { SharedModule } from '@/app/shared/shared.module';
import { CashflowAccountService } from '../cashflow-account.service';

@Component({
    selector: 'app-cashflow-account-details',
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule],
    templateUrl: './cashflow-account-details.component.html'
})
export class CashflowAccountDetailsComponent {
    module    = 'Cash Flow Account';
    subModule = 'Details';
    menuLink  = 'cashflow-account';
    id: any   = 0;
    data: any = {};
    logs      = signal<any[]>([]);
    isLoading = signal(false);

    private service      = inject(CashflowAccountService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.id = params.get('id');
            if (this.id && /^\d+$/.test(String(this.id))) {
                this.loadData();
            }
        });
    }

    loadData(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.data = data;
                    this.loadLogs();
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

    loadLogs(): void {
        this.service.getLogs(this.id).subscribe({
            next: (logs) => this.logs.set(Array.isArray(logs) ? logs : []),
            error: () => {}
        });
    }
}
