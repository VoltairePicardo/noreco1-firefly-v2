import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { BudgetService } from '../budget.service';

@Component({
    selector: 'app-budget-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './budget-detail.component.html'
})
export class BudgetDetailComponent {
    module    = 'Cash Flow Budget';
    subModule = 'Detail';
    menuLink  = 'budget';

    id: any        = 0;
    data: any      = {};
    details: any[] = [];
    isLoading      = signal(false);

    private service      = inject(BudgetService);
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
                if (data?.id) {
                    this.data = data;
                    this.loadDetails();
                } else {
                    this.isLoading.set(false);
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

    loadDetails(): void {
        this.service.getDetails(this.id).subscribe({
            next: (details) => {
                this.details = details || [];
                this.isLoading.set(false);
            },
            error: () => {
                this.details = [];
                this.isLoading.set(false);
            }
        });
    }

    get totalAmount(): number {
        return this.details.reduce((sum, d) => sum + (Number(d.amount) || 0), 0);
    }
}
