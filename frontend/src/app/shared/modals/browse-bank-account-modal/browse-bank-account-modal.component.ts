import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerChevronLeft, tablerChevronRight, tablerSearch } from '@ng-icons/tabler-icons';
import { BankDepositService } from '@/app/pages/bank-deposit/bank-deposit.service';

@Component({
    selector: 'app-browse-bank-account-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerChevronLeft, tablerChevronRight, tablerSearch })],
    templateUrl: './browse-bank-account-modal.component.html'
})
export class BrowseBankAccountModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private service = inject(BankDepositService);
    private cdr = inject(ChangeDetectorRef);

    items: any[] = [];
    loading = false;
    searchQuery = '';
    page = 1;
    pageSize = 10;
    total = 0;

    ngOnInit(): void {
        this.load();
    }

    load(): void {
        this.loading = true;
        this.service.getBankAccounts(this.searchQuery, this.page - 1, this.pageSize).subscribe({
            next: (data) => {
                this.items = data?.content || [];
                this.total = data?.page?.totalElements ?? data?.totalElements ?? 0;
                this.loading = false;
                this.cdr.markForCheck();
            },
            error: () => { this.loading = false; this.cdr.markForCheck(); }
        });
    }

    search(): void {
        this.page = 1;
        this.load();
    }

    select(item: any): void {
        this.activeModal.close({ action: 'select', data: item });
    }
}
