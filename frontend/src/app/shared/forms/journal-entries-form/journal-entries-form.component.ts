import { Component, inject, Input, OnChanges, SimpleChanges } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerPlus, tablerTrash, tablerSettings, tablerUsers } from '@ng-icons/tabler-icons';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseCOAModalComponent } from '@/app/shared/modals/browse-coa-modal/browse-coa-modal.component';
import { JournalEntrySettingModalComponent } from '@/app/shared/modals/journal-entry-setting-modal/journal-entry-setting-modal.component';
import { SlEntrySetterModalComponent } from '@/app/shared/modals/sl-entry-setter-modal/sl-entry-setter-modal.component';
import { AlertService } from '@/app/shared/services/alert.service';

export interface SlEntry {
    entity: any;
    accountNo: number | null;
    name: string;
    debit: number | null;
    credit: number | null;
}

export interface JournalEntry {
    account: any;
    debit: number | null;
    credit: number | null;
    // Per-row settings
    applyAllocation?: boolean;
    allocationPct?: number | null;
    applyWht?: boolean;
    wht?: { atc: any; nature: string; rate: number | null; amount: number | null } | null;
    // Sub-ledger split (only applicable when account.hasSL)
    slentries?: SlEntry[];
    // Generated rows (auto WHT/VAT rows produced by the settings modal — read-only in the grid)
    generated?: boolean;
    wTaxEntry?: any | null;
    vatEntry?: any | null;
    // Internal linkage — not persisted; lets a reopened settings modal replace (not duplicate)
    // the WHT/VAT rows it previously generated for this row.
    _id?: number;
    _parentId?: number;
    // Internal — the row's gross amount before any WHT/VAT deduction, so re-opening the
    // settings modal recomputes deductions idempotently instead of compounding them.
    _grossDebit?: number;
    _grossCredit?: number;
}

@Component({
    selector: 'app-journal-entries-form',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerSearch, tablerPlus, tablerTrash, tablerSettings, tablerUsers })],
    templateUrl: './journal-entries-form.component.html'
})
export class JournalEntriesFormComponent implements OnChanges {
    @Input() entries: JournalEntry[] = [];
    // Optional context passed down to the settings modal for VAT (needs the payee's
    // `vatable` flag) and allocation-factor lookups (needs the voucher date). Parents
    // that don't wire these simply keep the current allocation/WHT-only behavior.
    @Input() voucherDate?: string;
    @Input() defaultEntity?: any;

    private modalService = inject(ModalService);
    private alertService = inject(AlertService);
    private idSeq = 1;

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['entries'] && !this.entries) {
            this.entries = [];
        }
    }

    addRow(): void {
        this.entries.push({
            account: null, debit: null, credit: null,
            applyAllocation: false, allocationPct: null,
            applyWht: false, wht: null,
            _id: this.idSeq++
        });
    }

    removeRow(index: number): void {
        this.entries.splice(index, 1);
    }

    /**
     * Mirrors the legacy journal-entries directive's `newBlankRow` behavior:
     * once the last row in the grid gets a debit or credit amount, a fresh
     * blank row is appended automatically so the user can keep tabbing down
     * without clicking "Add Row" for every line.
     */
    onAmountChanged(index: number): void {
        const entry = this.entries[index];
        const isLastRow = index === this.entries.length - 1;
        const hasValue = !!(entry.debit || entry.credit);

        if (isLastRow && hasValue) {
            this.addRow();
        }
    }

    async openAccountBrowse(index: number): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseCOAModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this.entries[index] = { ...this.entries[index], account: result.data };
            }
        } catch { }
    }

    async openSettings(index: number): Promise<void> {
        try {
            const entry = this.entries[index];
            if (entry._id == null) entry._id = this.idSeq++;

            const result = await this.modalService.openModal(
                JournalEntrySettingModalComponent,
                { entry, voucherDate: this.voucherDate, defaultEntity: this.defaultEntity },
                { size: 'lg', centered: true }
            );
            if (result?.action === 'save' && result?.data) {
                // Drop any WHT/VAT rows this row previously generated — they get
                // replaced below with whatever the modal produced this time. Mutate
                // `entries` in place (splice), never reassign it: it's the same array
                // object the parent passed in via [entries], so reassigning here would
                // desync the child's copy from the parent's — e.g. rows added afterward
                // via "Add Row" would render locally but never reach the parent's model
                // (and so never make it into the save payload).
                for (let i = this.entries.length - 1; i >= 0; i--) {
                    if (this.entries[i]._parentId === entry._id) {
                        this.entries.splice(i, 1);
                    }
                }
                const idx = this.entries.indexOf(entry);

                this.entries[idx] = { ...entry, ...result.data };

                const siblings: JournalEntry[] = (result.siblings || []).map((s: JournalEntry) => ({
                    ...s,
                    generated: true,
                    _id: this.idSeq++,
                    _parentId: entry._id
                }));
                this.entries.splice(idx + 1, 0, ...siblings);
            }
        } catch { }
    }

    /**
     * Standalone SL entries modal — a direct port of the legacy `slEntrySetter`
     * directive (sl-setter-modal.js/jsp), triggered from its own button on the row,
     * independent of the account-settings modal.
     */
    async openSlEntries(index: number): Promise<void> {
        const entry = this.entries[index];
        const debit = Number(entry.debit) || 0;
        const credit = Number(entry.credit) || 0;

        if (debit === 0 && credit === 0) {
            this.alertService.warning('Journal Entry', 'Validation', 'Enter debit or credit amount.');
            return;
        }

        try {
            const result = await this.modalService.openModal(
                SlEntrySetterModalComponent,
                { entry, defaultEntity: this.defaultEntity },
                { size: 'lg', centered: true }
            );
            if (result?.action === 'save' && result?.data) {
                this.entries[index] = { ...entry, slentries: result.data.slentries };
            }
        } catch { }
    }

    hasSettings(entry: JournalEntry): boolean {
        return !!(entry.applyAllocation || entry.applyWht);
    }

    hasSL(entry: JournalEntry): boolean {
        return !!entry.account?.hasSL;
    }

    getAccountLabel(entry: JournalEntry): string {
        if (!entry?.account) return '';
        return `${entry.account.accountCode || ''} — ${entry.account.accountTitle || entry.account.accountDescription || ''}`;
    }

    /** Short badge for an auto-generated WHT/VAT row. */
    getGeneratedLabel(entry: JournalEntry): string {
        if (entry.wTaxEntry) return 'Auto — WHT';
        if (entry.vatEntry) return 'Auto — VAT';
        return 'Auto';
    }

    get debitTotal(): number {
        return this.entries.reduce((s, e) => s + (Number(e.debit) || 0), 0);
    }

    get creditTotal(): number {
        return this.entries.reduce((s, e) => s + (Number(e.credit) || 0), 0);
    }

    get isBalanced(): boolean {
        return Math.round(this.debitTotal * 100) === Math.round(this.creditTotal * 100);
    }

    /**
     * Mirrors the legacy journal-entries directive's `debitCreditDiff()` —
     * flags the running debit/credit imbalance so it can be surfaced next
     * to the totals row.
     */
    get debitCreditDiff(): string {
        const diff = Math.round((this.debitTotal - this.creditTotal) * 100) / 100;
        if (diff === 0) return '';

        const formatted = Math.abs(diff).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        return diff > 0
            ? `Debit is greater than Credit by ${formatted}`
            : `Credit is greater than Debit by ${formatted}`;
    }
}
