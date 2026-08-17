import { Component, inject, Input, OnChanges, SimpleChanges } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerPlus, tablerTrash, tablerSettings } from '@ng-icons/tabler-icons';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseCOAModalComponent } from '@/app/shared/modals/browse-coa-modal/browse-coa-modal.component';
import { JournalEntrySettingModalComponent } from '@/app/shared/modals/journal-entry-setting-modal/journal-entry-setting-modal.component';

export interface JournalEntry {
    account: any;
    debit: number | null;
    credit: number | null;
    // Per-row settings
    applyAllocation?: boolean;
    allocationPct?: number | null;
    applyWht?: boolean;
    wht?: { atc: any; nature: string; rate: number | null; amount: number | null } | null;
}

@Component({
    selector: 'app-journal-entries-form',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerSearch, tablerPlus, tablerTrash, tablerSettings })],
    templateUrl: './journal-entries-form.component.html'
})
export class JournalEntriesFormComponent implements OnChanges {
    @Input() entries: JournalEntry[] = [];

    private modalService = inject(ModalService);

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['entries'] && !this.entries) {
            this.entries = [];
        }
    }

    addRow(): void {
        this.entries.push({
            account: null, debit: null, credit: null,
            applyAllocation: false, allocationPct: null,
            applyWht: false, wht: null
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
            const result = await this.modalService.openModal(
                JournalEntrySettingModalComponent,
                { entry: this.entries[index] },
                { size: 'lg', centered: true }
            );
            if (result?.action === 'save' && result?.data) {
                this.entries[index] = { ...this.entries[index], ...result.data };
            }
        } catch { }
    }

    hasSettings(entry: JournalEntry): boolean {
        return !!(entry.applyAllocation || entry.applyWht);
    }

    getAccountLabel(entry: JournalEntry): string {
        if (!entry?.account) return '';
        return `${entry.account.accountCode || ''} — ${entry.account.accountTitle || entry.account.accountDescription || ''}`;
    }

    get debitTotal(): number {
        return this.entries.reduce((s, e) => s + (Number(e.debit) || 0), 0);
    }

    get creditTotal(): number {
        return this.entries.reduce((s, e) => s + (Number(e.credit) || 0), 0);
    }
}
