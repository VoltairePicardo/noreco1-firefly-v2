import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { EffectivityDateService } from '../effectivity-date.service';
import Swal from 'sweetalert2';

@Component({
    selector: 'app-effectivity-date-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './effectivity-date-main.component.html'
})
export class EffectivityDateMainComponent {
    module    = 'Effectivity Date';
    subModule = '';
    menuLink  = 'effectivity-date';

    effectivityDates = signal<any[]>([]);

    page     = 1;
    pageSize = 10;

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.effectivityDates().slice(start, start + this.pageSize);
    }

    isLoading        = signal(false);

    private service      = inject(EffectivityDateService);
    private alertService = inject(AlertService);

    ngOnInit(): void { this.load(); }

    load(): void {
        this.isLoading.set(true);
        this.service.list().subscribe({
            next: (data) => {
                this.effectivityDates.set(Array.isArray(data) ? data : []);
                this.page = 1;
                this.isLoading.set(false);
            },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    delete(id: number, description: string): void {
        Swal.fire({
            title: 'Delete Effectivity Date',
            html: `<p>Are you sure you want to delete <strong>${description}</strong>?</p>`,
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
                            this.load();
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
