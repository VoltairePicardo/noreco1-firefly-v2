import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { SharedModule } from '@/app/shared/shared.module';
import { CoaService } from '../coa.service';

@Component({
    selector: 'app-coa-details',
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule],
    templateUrl: './coa-details.component.html'
})
export class CoaDetailsComponent {
    module    = 'Chart of Accounts';
    subModule = 'Details';
    menuLink  = 'coa';
    id: any   = 0;
    data: any = {};
    isLoading = signal(false);

    private service      = inject(CoaService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.id = params.get('id');
            if (this.id && /^\d+$/.test(String(this.id))) {
                this.isLoading.set(true);
                this.service.getData(this.id).subscribe({
                    next: (data) => {
                        this.isLoading.set(false);
                        if (data?.id) { this.data = data; }
                        else { this.alertService.error(this.module, 'Not Found', ''); this.router.navigate(['/' + this.menuLink]); }
                    },
                    error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Error', ''); this.router.navigate(['/' + this.menuLink]); }
                });
            }
        });
    }

    normalBalanceLabel(val: number): string {
        return val === 1 ? 'Debit' : val === 2 ? 'Credit' : '—';
    }

    validityEntries(): [string, any[]][] {
        const map = this.data?.factor?.factorPercentageDistroSetByValidity;
        if (!map) return [];
        return Object.entries(map) as [string, any[]][];
    }

    segmentTotal(rows: any[]): number {
        return rows.reduce((sum, r) => sum + (r.percentage ?? 0) * 100, 0);
    }
}
