import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, Input, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerChevronLeft, tablerChevronRight, tablerSearch } from '@ng-icons/tabler-icons';
import { AccountSettingService } from '@/app/pages/account-setting/account-setting.service';

@Component({
    selector: 'app-browse-account-setting-doc-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerChevronLeft, tablerChevronRight, tablerSearch })],
    templateUrl: './browse-account-setting-doc-modal.component.html'
})
export class BrowseAccountSettingDocModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private service = inject(AccountSettingService);
    private cdr = inject(ChangeDetectorRef);

    @Input() docType = '';

    readonly docTypes = [
        { key: 'rr',            label: 'Receiving Report' },
        { key: 'stockReceive',  label: 'Stock Receiving' },
        { key: 'mct',           label: 'MCT (Material Transfer)' },
        { key: 'stockRelease',  label: 'Stock Release' },
        { key: 'mst',           label: 'MST (Material/Store Transfer)' },
        { key: 'stockAdjust',   label: 'Stock Adjustment' },
        { key: 'rv',            label: 'Requisition Voucher' },
        { key: 'canvass',       label: 'Canvass' },
        { key: 'quotation',     label: 'Quotation' },
        { key: 'po',            label: 'Purchase Order' },
        { key: 'jo',            label: 'Job Order' },
        { key: 'joa',           label: 'JO Acceptance' },
        { key: 'paymentReq',    label: 'Payment Request' },
        { key: 'ccpr',          label: 'Credit Card Purchase Request' },
        { key: 'apv',           label: 'Accounts Payable Voucher' },
        { key: 'disbursement',  label: 'Disbursement (Check Voucher)' },
        { key: 'generalJournal', label: 'General Journal' },
    ];

    selectedDocType = '';
    items: any[] = [];
    total = 0;
    page = 1;
    pageSize = 10;
    searchText = '';
    loading = false;

    ngOnInit(): void {
        this.selectedDocType = this.docType || '';
        if (this.selectedDocType) this.loadData();
    }

    loadData(): void {
        if (!this.selectedDocType || this.loading) return;
        this.loading = true;
        this.service.browseDocuments(this.selectedDocType, this.searchText, this.page - 1, this.pageSize).subscribe({
            next: (res) => {
                this.items = res.content ?? res ?? [];
                this.total = res.totalElements ?? res.page?.totalElements ?? this.items.length;
                this.loading = false;
                this.cdr.markForCheck();
            },
            error: () => { this.loading = false; this.cdr.markForCheck(); }
        });
    }

    onDocTypeChange(): void {
        this.page = 1;
        this.items = [];
        this.total = 0;
        this.searchText = '';
        this.loadData();
    }

    onSearchChange(): void { this.page = 1; this.loadData(); }

    select(item: any): void {
        this.activeModal.close({ action: 'select', data: item });
    }
}
