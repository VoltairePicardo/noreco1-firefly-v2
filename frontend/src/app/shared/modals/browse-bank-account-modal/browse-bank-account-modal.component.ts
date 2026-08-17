import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { BankDepositService } from '@/app/pages/bank-deposit/bank-deposit.service';

@Component({
    selector: 'app-browse-bank-account-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './browse-bank-account-modal.component.html'
})
export class BrowseBankAccountModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private service = inject(BankDepositService);
    private cdr = inject(ChangeDetectorRef);

    items: any[] = [];
    loading = false;

    ngOnInit(): void {
        this.loading = true;
        this.service.getBankAccounts().subscribe({
            next: (data) => { this.items = data || []; this.loading = false; this.cdr.markForCheck(); },
            error: () => { this.loading = false; this.cdr.markForCheck(); }
        });
    }

    select(item: any): void {
        this.activeModal.close({ action: 'select', data: item });
    }
}
