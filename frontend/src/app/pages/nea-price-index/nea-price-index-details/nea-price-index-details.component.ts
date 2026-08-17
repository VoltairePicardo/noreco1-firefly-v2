import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { SharedModule } from '@/app/shared/shared.module';
import { NeaPriceIndexService } from '../nea-price-index.service';

@Component({
    selector: 'app-nea-price-index-details',
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule],
    templateUrl: './nea-price-index-details.component.html'
})
export class NeaPriceIndexDetailsComponent {
    module    = 'NEA Price Index';
    subModule = 'Details';
    menuLink  = 'nea-price-index';
    id: any   = 0;
    data: any = {};
    isLoading = signal(false);

    private service      = inject(NeaPriceIndexService);
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
}
