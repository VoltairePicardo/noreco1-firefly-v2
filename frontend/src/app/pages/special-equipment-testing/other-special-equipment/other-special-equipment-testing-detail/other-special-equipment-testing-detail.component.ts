import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { OtherSpecialEquipmentTestingService } from '../other-special-equipment-testing.service';
import { OtherSpecialEquipmentTestingData } from '@/app/models/special-equipment-testing/other-special-equipment-testing.model';

@Component({
    selector: 'app-other-special-equipment-testing-detail',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS],
    templateUrl: './other-special-equipment-testing-detail.component.html'
})
export class OtherSpecialEquipmentTestingDetailComponent implements OnInit {
    module    = 'Other Special Equipment Testing';
    subModule = 'Details';
    menuLink  = 'other-special-equipment-testing';

    data = signal<OtherSpecialEquipmentTestingData | null>(null);
    isLoading = signal(false);

    private route   = inject(ActivatedRoute);
    private router  = inject(Router);
    private service = inject(OtherSpecialEquipmentTestingService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            const id = idParam != null && /^\d+$/.test(idParam) ? Number(idParam) : null;

            if (id !== null) {
                this.load(id);
            }
        });
    }

    private load(id: number): void {
        this.isLoading.set(true);
        this.service.getData(id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.data.set(data);
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
