import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { MemorandumReceiptService } from '../memorandum-receipt.service';
import { monthStart, monthEnd } from '@/app/shared/utils/date.utils';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerRefresh, tablerPlus, tablerEye, tablerEdit, tablerUsers, tablerRepeat } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-memorandum-receipt-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults(), provideIcons({ tablerSearch, tablerRefresh, tablerPlus, tablerEye, tablerEdit, tablerUsers, tablerRepeat })],
    templateUrl: './memorandum-receipt-main.component.html'
})
export class MemorandumReceiptMainComponent {
    module    = 'Memorandum Receipt';
    subModule = '';
    menuLink  = 'memorandum-receipt';

    records   = signal<any[]>([]);
    isLoading = signal(false);

    page     = 1;
    pageSize = 10;

    get filteredRecords(): any[] {
        const q = this.searchText.toLowerCase();
        return q
            ? this.records().filter(r =>
                (r.code        || '').toLowerCase().includes(q) ||
                (r.employee?.name     || '').toLowerCase().includes(q) ||
                (r.employee?.fullName || '').toLowerCase().includes(q))
            : this.records();
    }

    get filteredTotal(): number { return this.filteredRecords.length; }

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.filteredRecords.slice(start, start + this.pageSize);
    }

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };
    fromDate   = '';
    toDate     = '';
    searchText = '';
    employee: any = null;

    private service      = inject(MemorandumReceiptService);
    private modalService = inject(ModalService);
    private alertService = inject(AlertService);

    ngOnInit(): void { this.setDefaultDates(); this.load(); }

    setDefaultDates(): void {
        this.fromDate = monthStart();
        this.toDate   = monthEnd();
    }

    load(): void {
        this.isLoading.set(true);
        let obs;
        if (this.fromDate && this.toDate && this.employee?.accountNo) {
            obs = this.service.listByEmployee(this.fromDate, this.toDate, this.employee.accountNo);
        } else if (this.fromDate && this.toDate) {
            obs = this.service.listByDateRange(this.fromDate, this.toDate);
        } else {
            obs = this.service.list();
        }
        obs.subscribe({
            next: (data) => { this.records.set(data || []); this.page = 1; this.isLoading.set(false); },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    reset(): void { this.setDefaultDates(); this.searchText = ''; this.employee = null; this.load(); }

    async openEmployeeBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.employee = result.data;
            }
        } catch { }
    }

    isEditable(rec: any): boolean {
        const s = rec?.status || '';
        return s === 'Document Created' || s === 'Returned to Creator';
    }
}
