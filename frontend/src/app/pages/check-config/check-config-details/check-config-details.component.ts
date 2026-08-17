import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { SharedModule } from '@/app/shared/shared.module';
import { CheckConfigService } from '../check-config.service';

@Component({
    selector: 'app-check-config-details',
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule],
    templateUrl: './check-config-details.component.html'
})
export class CheckConfigDetailsComponent {
    module    = 'Check Config';
    subModule = 'Details';
    menuLink  = 'check-config';
    id: any   = 0;
    data: any = {};
    isLoading = signal(false);

    private service      = inject(CheckConfigService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    testPrint(): void {
        this.service.getTestPrint(this.id).subscribe({
            next: (html: string) => {
                const blob = new Blob([html], { type: 'text/html' });
                const url  = URL.createObjectURL(blob);
                window.open(url, '_blank');
            },
            error: () => this.alertService.error(this.module, 'Test Print', 'Failed to generate test print.')
        });
    }

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
