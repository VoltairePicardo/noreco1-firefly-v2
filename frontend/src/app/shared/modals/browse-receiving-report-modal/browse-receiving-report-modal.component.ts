import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, Input, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerChevronLeft, tablerChevronRight, tablerSearch } from '@ng-icons/tabler-icons';
import { AccountsPayableVoucherService } from '@/app/pages/accounts-payable-voucher/accounts-payable-voucher.service';

@Component({
    selector: 'app-browse-receiving-report-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerChevronLeft, tablerChevronRight, tablerSearch })],
    templateUrl: './browse-receiving-report-modal.component.html'
})
export class BrowseReceivingReportModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private service = inject(AccountsPayableVoucherService);
    private cdr = inject(ChangeDetectorRef);

    @Input() docType: 'rr' | 'pr' | 'joa' = 'rr';

    items: any[] = [];
    total = 0;
    page = 1;
    pageSize = 10;
    searchText = '';
    loading = false;

    get modalTitle(): string {
        return this.docType === 'pr' ? 'Browse Payment Requests'
             : this.docType === 'joa' ? 'Browse JO Acceptances'
             : 'Browse Receiving Reports';
    }

    ngOnInit(): void { this.loadData(); }

    loadData(): void {
        if (this.loading) return;
        this.loading = true;
        const loader$ = this.docType === 'pr'  ? this.service.searchPrs(this.searchText, this.page - 1, this.pageSize)
                      : this.docType === 'joa' ? this.service.searchJoas(this.searchText, this.page - 1, this.pageSize)
                      : this.service.searchRvs(this.searchText, this.page - 1, this.pageSize);
        loader$.subscribe({
            next: (res) => {
                this.items = res.content ?? res ?? [];
                this.total = res.totalElements ?? res.page?.totalElements ?? this.items.length;
                this.loading = false;
                this.cdr.markForCheck();
            },
            error: () => { this.loading = false; this.cdr.markForCheck(); }
        });
    }

    onSearchChange(): void { this.page = 1; this.loadData(); }

    select(item: any): void {
        this.activeModal.close({ action: 'select', data: item });
    }
}
