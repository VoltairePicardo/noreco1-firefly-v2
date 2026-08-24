import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { FlatpickrDefaults, FlatpickrModule } from 'angularx-flatpickr';
import { MaintenanceRecordService } from '../maintenance-record.service';
import { fmtDate, monthStart, monthEnd } from '@/app/shared/utils/date.utils';

@Component({
    selector: 'app-maintenance-record-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrModule],
    providers: [...SHARED_PROVIDERS, FlatpickrDefaults],
    templateUrl: './maintenance-record-main.component.html'
})
export class MaintenanceRecordMainComponent {
    module    = 'Maintenance Record';
    subModule = '';
    menuLink  = 'maintenance-record';

    records       = signal<any[]>([]);
    isLoading     = signal(false);
    pageNumber    = signal(0);
    totalElements = signal(0);
    pageSize = 10;

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    fromDate = '';
    toDate   = '';
    query    = '';

    private service      = inject(MaintenanceRecordService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.setDefaultDates();
        this.load();
    }

    setDefaultDates(): void {
        this.fromDate = monthStart();
        this.toDate   = monthEnd();
    }

    toDateString(d: Date): string {
        return fmtDate(d);
    }

    load(page = 0): void {
        this.isLoading.set(true);
        this.service.list(this.query, this.fromDate, this.toDate, page, this.pageSize).subscribe({
            next: (data) => {
                this.records.set(data?.content ?? []);
                this.pageNumber.set(data?.page?.number ?? 0);
                this.totalElements.set(data?.page?.totalElements ?? 0);
                this.isLoading.set(false);
            },
            error: () => {
                this.alertService.error(this.module, 'Load', '');
                this.isLoading.set(false);
            }
        });
    }

    search(): void { this.load(0); }

    reset(): void {
        this.query = '';
        this.setDefaultDates();
        this.load(0);
    }

    onPageChange(p: number): void { this.load(p - 1); }
}
