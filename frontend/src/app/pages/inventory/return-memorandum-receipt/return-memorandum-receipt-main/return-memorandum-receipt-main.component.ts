import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { ReturnMemorandumReceiptService } from '../return-memorandum-receipt.service';
import { monthStart, monthEnd } from '@/app/shared/utils/date.utils';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerRefresh, tablerPlus, tablerEye, tablerEdit, tablerUsers } from '@ng-icons/tabler-icons';
import { ReturnMemorandumReceiptListRow, SlEntity } from '@/app/models/inventory-modules/memorandum-receipt.model';

@Component({
    selector: 'app-return-memorandum-receipt-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults(), provideIcons({ tablerSearch, tablerRefresh, tablerPlus, tablerEye, tablerEdit, tablerUsers })],
    templateUrl: './return-memorandum-receipt-main.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReturnMemorandumReceiptMainComponent implements OnInit {
    module    = 'Return Memorandum Receipt';
    subModule = '';
    menuLink  = 'return-memorandum-receipt';

    records       = signal<ReturnMemorandumReceiptListRow[]>([]);
    isLoading     = signal(false);

    page          = signal(1);
    pageSize      = 10;
    filteredTotal = signal(0);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };
    fromDate   = '';
    toDate     = '';
    searchText = signal('');
    employee   = signal<SlEntity | null>(null);

    private service      = inject(ReturnMemorandumReceiptService);
    private modalService = inject(ModalService);
    private alertService = inject(AlertService);

    ngOnInit(): void { this.setDefaultDates(); this.load(); }

    setDefaultDates(): void {
        this.fromDate = monthStart();
        this.toDate   = monthEnd();
    }

    load(): void {
        this.isLoading.set(true);
        this.service.listPaged(this.fromDate, this.toDate, this.employee()?.accountNo ?? null, this.searchText().trim(), this.page() - 1, this.pageSize)
            .subscribe({
                next: (data) => {
                    this.records.set(data?.content ?? []);
                    this.filteredTotal.set(data?.totalElements ?? data?.page?.totalElements ?? 0);
                    this.isLoading.set(false);
                },
                error: () => {
                    this.records.set([]);
                    this.filteredTotal.set(0);
                    this.alertService.error(this.module, 'Load', '');
                    this.isLoading.set(false);
                }
            });
    }

    search(): void {
        this.page.set(1);
        this.load();
    }

    onPageChange(page: number): void {
        this.page.set(page);
        this.load();
    }

    reset(): void {
        this.setDefaultDates();
        this.searchText.set('');
        this.employee.set(null);
        this.page.set(1);
        this.load();
    }

    async openEmployeeBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.employee.set(result.data as SlEntity);
                this.search();
            }
        } catch { }
    }

    isEditable(rec: ReturnMemorandumReceiptListRow): boolean {
        const s = rec?.status || '';
        return s === 'Document Created' || s === 'For Revision';
    }
}
