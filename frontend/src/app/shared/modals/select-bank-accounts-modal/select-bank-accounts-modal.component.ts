import { ChangeDetectionStrategy, Component, Input, OnInit, inject } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'app-select-bank-accounts-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './select-bank-accounts-modal.component.html'
})
export class SelectBankAccountsModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);

    @Input() bank: any = null;
    @Input() bankAccounts: any[] = [];
    @Input() alreadySelectedIds: number[] = [];

    protected accounts: any[] = [];
    protected selectedIds = new Set<number>();
    protected selectAll = false;

    ngOnInit(): void {
        this.accounts = this.bankAccounts || [];
        (this.alreadySelectedIds || []).forEach(id => this.selectedIds.add(id));
        this.syncSelectAll();
    }

    protected isSelected(account: any): boolean {
        return this.selectedIds.has(account.id);
    }

    protected toggle(account: any): void {
        if (this.selectedIds.has(account.id)) {
            this.selectedIds.delete(account.id);
        } else {
            this.selectedIds.add(account.id);
        }
        this.syncSelectAll();
    }

    protected toggleAll(): void {
        this.selectAll = !this.selectAll;
        this.selectedIds = new Set(this.selectAll ? this.accounts.map(a => a.id) : []);
    }

    protected confirm(): void {
        const selected = this.accounts.filter(a => this.selectedIds.has(a.id));
        this.activeModal.close({ action: 'select', data: selected });
    }

    private syncSelectAll(): void {
        this.selectAll = this.accounts.length > 0 && this.accounts.every(a => this.selectedIds.has(a.id));
    }
}
