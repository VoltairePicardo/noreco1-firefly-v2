import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { SharedModule } from '@/app/shared/shared.module';
import { ItemService } from '../item.service';
import Swal from 'sweetalert2';

@Component({
    selector: 'app-item-details',
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule],
    templateUrl: './item-details.component.html'
})
export class ItemDetailsComponent {
    module    = 'Item';
    subModule = 'Details';
    menuLink  = 'item';
    id: any   = 0;
    data: any = {};
    isLoading = signal(false);

    private service      = inject(ItemService);
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
            title: 'Delete Item?',
            text: `Are you sure you want to delete "${this.data.description}"?`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Yes, delete',
            confirmButtonColor: '#d33',
            cancelButtonText: 'Cancel'
        }).then(result => {
            if (result.isConfirmed) {
                this.service.remove(this.id).subscribe({
                    next: (res) => {
                        if (res.success) {
                            Swal.fire({ title: 'Deleted!', text: res.successMessage, icon: 'success', timer: 2000, showConfirmButton: false });
                            this.router.navigate(['/' + this.menuLink]);
                        } else {
                            Swal.fire({ title: 'Error', text: res.failureMessage, icon: 'error' });
                        }
                    },
                    error: () => Swal.fire({ title: 'Error', text: 'Failed to delete item.', icon: 'error' })
                });
            }
        });
    }
}
