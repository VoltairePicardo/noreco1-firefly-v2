import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { SharedModule } from '@/app/shared/shared.module';
import { InventoryCategory } from '@/app/models/coop-accounting/inventory-category.model';
import { InventoryCategoryService } from '../inventory-category.service';

@Component({
    selector: 'app-inventory-category-details',
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule],
    changeDetection: ChangeDetectionStrategy.OnPush,
    templateUrl: './inventory-category-details.component.html'
})
export class InventoryCategoryDetailsComponent {
    module    = 'Inventory Category';
    subModule = 'Details';
    menuLink  = 'inventory-category';
    data      = signal<InventoryCategory>({ description: '' });
    isLoading = signal(false);

    private service      = inject(InventoryCategoryService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            const id = params.get('id');
            if (id && /^\d+$/.test(id)) {
                this.isLoading.set(true);
                this.service.getData(+id).subscribe({
                    next: (data) => {
                        this.isLoading.set(false);
                        if (data?.id) { this.data.set(data); }
                        else { this.alertService.error(this.module, 'Not Found', ''); this.router.navigate(['/' + this.menuLink]); }
                    },
                    error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Error', ''); this.router.navigate(['/' + this.menuLink]); }
                });
            }
        });
    }
}
