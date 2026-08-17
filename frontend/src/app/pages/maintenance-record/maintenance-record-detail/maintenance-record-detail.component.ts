import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { MaintenanceRecordService } from '../maintenance-record.service';

@Component({
    selector: 'app-maintenance-record-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './maintenance-record-detail.component.html'
})
export class MaintenanceRecordDetailComponent {
    module    = 'Maintenance Record';
    subModule = 'Detail';
    menuLink  = 'maintenance-record';
    id: any   = 0;
    data: any = {};
    isLoading = signal(false);

    get workItems(): any[] {
        return this.data?.maintenanceRecordWorks || [];
    }

    get otherItems(): any[] {
        return this.data?.maintenanceRecordOtherItems || [];
    }

    get materialReleases(): any[] {
        return this.data?.maintenanceRecordMaterialReleases || [];
    }

    get totalWorkAmount(): number {
        return this.workItems.reduce((sum: number, w: any) => sum + (Number(w.amount) || 0), 0);
    }

    get totalOtherAmount(): number {
        return this.otherItems.reduce((sum: number, o: any) => sum + (Number(o.amount) || 0), 0);
    }

    private service      = inject(MaintenanceRecordService);
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

    print(): void {
        this.service.print(this.id);
    }
}
