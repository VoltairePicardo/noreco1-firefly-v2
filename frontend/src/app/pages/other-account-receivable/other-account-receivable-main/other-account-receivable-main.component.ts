import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { OtherAccountReceivableService } from '../other-account-receivable.service';

@Component({
    selector: 'app-other-account-receivable-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './other-account-receivable-main.component.html'
})
export class OtherAccountReceivableMainComponent {
    module    = 'Other Account Receivable';
    subModule = '';
    menuLink  = 'other-account-receivable';

    records          = signal<any[]>([]);
    isLoading        = signal(false);
    documentStatuses = signal<any[]>([]);

    page     = 1;
    pageSize = 10;

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.records().slice(start, start + this.pageSize);
    }

    selectedStatus: number | null = null;

    private service      = inject(OtherAccountReceivableService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.loadStatuses();
        this.load();
    }

    loadStatuses(): void {
        this.service.getDocumentStatuses().subscribe({
            next: (s) => this.documentStatuses.set(s || []),
            error: () => {}
        });
    }

    load(): void {
        this.isLoading.set(true);
        const obs = this.selectedStatus != null
            ? this.service.listByStatus(this.selectedStatus)
            : this.service.list();

        obs.subscribe({
            next: (data) => { this.records.set(data || []); this.page = 1; this.isLoading.set(false); },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    reset(): void {
        this.selectedStatus = null;
        this.load();
    }

    isEditable(rec: any): boolean {
        const s = rec?.status || '';
        return s === 'Document Created' || s === 'Returned to Creator';
    }
}
