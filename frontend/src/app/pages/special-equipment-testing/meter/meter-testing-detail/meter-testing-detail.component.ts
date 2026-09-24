import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { SharedModule } from '@/app/shared/shared.module';
import { AlertService } from '@/app/shared/services/alert.service';
import { MeterTestingService } from '../meter-testing.service';

@Component({
    selector: 'app-meter-testing-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule],
    templateUrl: './meter-testing-detail.component.html'
})
export class MeterTestingDetailComponent {
    module    = 'Meter Testing';
    subModule = 'Details';
    menuLink  = 'meter-testing';

    id: any = null;
    data: any = {};
    isLoading = signal(false);

    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private service      = inject(MeterTestingService);
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
