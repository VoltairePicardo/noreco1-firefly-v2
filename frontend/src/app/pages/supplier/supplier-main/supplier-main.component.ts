import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { SupplierService } from '../supplier.service';
import Swal from 'sweetalert2';

@Component({
    selector: 'app-supplier-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './supplier-main.component.html'
})
export class SupplierMainComponent {
    module    = 'Supplier';
    subModule = '';
    menuLink  = 'supplier';

    suppliers     = signal<any[]>([]);
    isLoading     = signal(false);
    pageNumber    = signal(0);
    totalPages    = signal(0);
    totalElements = signal(0);
    pageSize = 10;
    searchText    = '';

    private service      = inject(SupplierService);
    private alertService = inject(AlertService);

    ngOnInit(): void { this.load(); }

    load(page = 0): void {
        this.isLoading.set(true);
        this.service.list(this.searchText, page).subscribe({
            next: (data) => {
                this.suppliers.set(data.content);
                this.pageNumber.set(data.page.number);
                this.totalPages.set(data.page.totalPages);
                this.totalElements.set(data.page.totalElements);
                this.isLoading.set(false);
            },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    search():      void { this.load(0); }
    clearSearch(): void { this.searchText = ''; this.load(0); }
    onPageChange(p: number): void { this.load(p - 1); }

    delete(id: number, name: string): void {
        Swal.fire({
            title: 'Delete Supplier',
            html: `<p>Are you sure you want to delete <strong>${name}</strong>?</p>`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Yes, delete it',
            cancelButtonText: 'Cancel',
            confirmButtonColor: '#d33',
        }).then((result) => {
            if (result.isConfirmed) {
                this.service.remove(id).subscribe({
                    next: (res) => {
                        if (res?.success) {
                            this.alertService.success(this.module, 'Deleted', '');
                            this.load(this.pageNumber());
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
