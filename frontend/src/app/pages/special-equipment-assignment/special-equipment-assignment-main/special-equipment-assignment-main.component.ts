import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { SpecialEquipmentAssignmentService } from '../special-equipment-assignment.service';
import { monthStart, monthEnd } from '@/app/shared/utils/date.utils';

@Component({
    selector: 'app-special-equipment-assignment-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './special-equipment-assignment-main.component.html'
})
export class SpecialEquipmentAssignmentMainComponent {
    module    = 'Special Equipment Assignment';
    subModule = '';
    menuLink  = 'special-equipment-assignment';

    records   = signal<any[]>([]);
    isLoading = signal(false);
    page      = 1;
    pageSize  = 10;

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.records().slice(start, start + this.pageSize);
    }

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };
    fromDate = ''; toDate = '';

    private service      = inject(SpecialEquipmentAssignmentService);
    private alertService = inject(AlertService);

    ngOnInit(): void { this.setDefaultDates(); this.load(); }

    setDefaultDates(): void {
        this.fromDate = monthStart();
        this.toDate   = monthEnd();
    }

    load(): void {
        this.isLoading.set(true);
        const obs = (this.fromDate && this.toDate)
            ? this.service.listByDateRange(this.fromDate, this.toDate)
            : this.service.list();
        obs.subscribe({
            next: (data) => { this.records.set(data || []); this.page = 1; this.isLoading.set(false); },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    reset(): void { this.setDefaultDates(); this.load(); }
}
