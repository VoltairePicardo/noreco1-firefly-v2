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
    applyAllocation?: boolean;
    allocationPct?: number | null;
    applyWht?: boolean;
    wht?: { atc: any; nature: string; rate: number | null; amount: number | null } | null;
    slentries?: SlEntry[];
    generated?: boolean;
    wTaxEntry?: WTaxEntry | null;
    vatEntry?: any | null;
    _id?: number;
    _parentId?: number;
    _grossDebit?: number;
    _grossCredit?: number;
    _netDebit?: number | null;
    _netCredit?: number | null;
}

export interface WTaxEntry {
    percentage: number | null;
    amount: number | null;
    baseAmount: number | null;
    nature?: string;
    atc: TaxCode;
}

export interface TaxCode {
    id: number;
    code: string;
    description: string;
    rate: number;
    payeeType: string;
    account?: any;
}

@Component({
    selector: 'app-journal-entries-form',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerSearch, tablerPlus, tablerTrash, tablerSettings, tablerUsers })],
    templateUrl: './journal-entries-form.component.html'
})
export class JournalEntriesFormComponent implements OnChanges {
    @Input() entries: JournalEntry[] = [];
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
        const a = entry.account;
        return `${a.accountCode || a.code || ''} — ${a.accountTitle || a.accountDescription || a.title || ''}`;
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

    get debitCreditDiff(): string {
        const diff = Math.round((this.debitTotal - this.creditTotal) * 100) / 100;
        if (diff === 0) return '';

        const formatted = Math.abs(diff).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        return diff > 0
            ? `Debit is greater than Credit by ${formatted}`
            : `Credit is greater than Debit by ${formatted}`;
    }
}
