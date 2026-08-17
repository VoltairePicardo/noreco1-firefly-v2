import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { SharedModule } from '@/app/shared/shared.module';
import { AllocationFactorService } from '../allocation-factor.service';

@Component({
    selector: 'app-allocation-factor-details',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, SharedModule],
    templateUrl: './allocation-factor-details.component.html'
})
export class AllocationFactorDetailsComponent {
    module    = 'Allocation Factor';
    subModule = 'Details';
    menuLink  = 'allocation-factor';

    factorId: any = null;
    data: any     = null;
    isLoading     = signal(false);

    private service      = inject(AllocationFactorService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.factorId = params.get('id') ? Number(params.get('id')) : null;
            if (this.factorId) {
                this.isLoading.set(true);
                this.service.getById(this.factorId).subscribe({
                    next: (data) => {
                        this.isLoading.set(false);
                        if (data) { this.data = data; }
                        else { this.alertService.error(this.module, 'Not Found', ''); this.router.navigate(['/' + this.menuLink]); }
                    },
                    error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Error', ''); this.router.navigate(['/' + this.menuLink]); }
                });
            }
        });
    }

    validityEntries(): [string, any[]][] {
        if (!this.data?.factorPercentageDistroSetByValidity) return [];
        return Object.entries(this.data.factorPercentageDistroSetByValidity);
    }

    segmentTotal(rows: any[]): number {
        return rows.reduce((s, r) => s + Number(r.percentage || 0) * 100, 0);
    }
}
