import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { SharedModule } from '@/app/shared/shared.module';
import { SupplierService } from '../supplier.service';
import Swal from 'sweetalert2';

@Component({
    selector: 'app-supplier-details',
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule],
    templateUrl: './supplier-details.component.html'
})
export class SupplierDetailsComponent {
    module    = 'Supplier';
    subModule = 'Details';
    menuLink  = 'supplier';
    id: any   = 0;
    data: any = {};
    isLoading = signal(false);

    private service      = inject(SupplierService);
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

    delete(): void {
        Swal.fire({
            title: 'Delete Supplier',
            html: `<p>Are you sure you want to delete <strong>${this.data.name}</strong>?</p>`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Yes, delete it',
            cancelButtonText: 'Cancel',
            confirmButtonColor: '#d33',
        }).then((result) => {
            if (result.isConfirmed) {
                this.service.remove(this.id).subscribe({
                    next: (res) => {
                        if (res?.success) {
                            this.alertService.success(this.module, 'Deleted', '');
                            this.router.navigate(['/' + this.menuLink]);
                        } else {
                            this.alertService.error(this.module, 'Delete', res?.failureMessage ?? '');
                        }
                    },
                    error: () => this.alertService.error(this.module, 'Delete', '')
                });
            }
        });
    }
}
