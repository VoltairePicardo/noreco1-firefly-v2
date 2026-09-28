import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { SharedModule } from '@/app/shared/shared.module';
import { InitialReadingEntryService } from '../initial-reading-entry.service';

@Component({
    selector: 'app-initial-reading-entry-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule],
    templateUrl: './initial-reading-entry-detail.component.html'
})
export class InitialReadingEntryDetailComponent {
    module    = 'Initial Reading Entry';
    subModule = 'Details';
    menuLink  = 'initial-reading-entry';
    id: any   = 0;
    data: any = {};
    isLoading = signal(false);

    private service      = inject(InitialReadingEntryService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.id = params.get('id');
            if (this.id && /^\d+$/.test(String(this.id))) {
                this.isLoading.set(true);
                this.service.getById(Number(this.id)).subscribe({
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
