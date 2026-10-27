import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerChevronLeft, tablerChevronRight, tablerSearch } from '@ng-icons/tabler-icons';
import { AccountsPayableVoucherService, ApvDocumentType } from '@/app/pages/accounts-payable-voucher/accounts-payable-voucher.service';

@Component({
    selector: 'app-browse-purchasing-document-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerChevronLeft, tablerChevronRight, tablerSearch })],
    templateUrl: './browse-purchasing-document-modal.component.html'
})
export class BrowsePurchasingDocumentModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private service = inject(AccountsPayableVoucherService);
    private cdr = inject(ChangeDetectorRef);

    supplierAccountNo: number | null = null;
    docType: ApvDocumentType = 'PR';
    docTypes: { value: ApvDocumentType; label: string }[] = [
        { value: 'PR',  label: 'Payment Request' },
        { value: 'RR',  label: 'Receiving Report' },
        { value: 'JOA', label: 'JO Acceptance' }
    ];

    items: any[] = [];
    total = 0;
    page = 1;
    pageSize = 10;
    searchText = '';
    loading = false;

    get needsSupplier(): boolean {
        return this.docType === 'RR' && this.supplierAccountNo == null;
    }

    ngOnInit(): void { this.loadData(); }

    loadData(): void {
        if (this.needsSupplier) {
            this.items = [];
            this.total = 0;
            this.cdr.markForCheck();
            return;
        }
        this.loading = true;
        this.cdr.markForCheck();
        this.service.searchPurchasingDocuments(this.docType, this.searchText, this.page - 1, this.pageSize, this.supplierAccountNo).subscribe({
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
        this.activeModal.close({ action: 'select', docType: this.docType, data: item });
    }
}
