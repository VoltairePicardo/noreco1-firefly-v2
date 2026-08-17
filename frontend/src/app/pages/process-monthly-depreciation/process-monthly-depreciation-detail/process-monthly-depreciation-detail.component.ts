import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { ProcessMonthlyDepreciationService } from '../process-monthly-depreciation.service';
import { forkJoin } from 'rxjs';

const MONTH_NAMES = [
    '', 'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
];

@Component({
    selector: 'app-process-monthly-depreciation-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './process-monthly-depreciation-detail.component.html'
})
export class ProcessMonthlyDepreciationDetailComponent {
    module    = 'Process Monthly Depreciation';
    subModule = 'Detail';
    menuLink  = 'process-monthly-depreciation';
    id: any   = 0;
    data: any = {};
    details: any[] = [];
    footer: any = { count: 0, total: 0 };
    isLoading = signal(false);

    private service      = inject(ProcessMonthlyDepreciationService);
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

    getMonthName(id: number): string {
        return MONTH_NAMES[id] || '';
    }

    loadData(): void {
        this.isLoading.set(true);
        forkJoin({
            data:    this.service.getData(this.id),
            details: this.service.getDetails(this.id),
            footer:  this.service.getFooter(this.id),
        }).subscribe({
            next: ({ data, details, footer }) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.data    = data;
                    this.details = details || [];
                    this.footer  = footer  || { count: 0, total: 0 };
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
}
