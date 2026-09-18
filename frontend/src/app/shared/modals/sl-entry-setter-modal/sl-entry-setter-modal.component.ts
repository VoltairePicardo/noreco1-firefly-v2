import { Component, inject, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { provideIcons } from '@ng-icons/core';
import { tablerCheck, tablerX, tablerPlus, tablerTrash } from '@ng-icons/tabler-icons';
import { JournalEntry, SlEntry } from '@/app/shared/forms/journal-entries-form/journal-entries-form.component';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { AlertService } from '@/app/shared/services/alert.service';

/**
 * Standalone SL (sub-ledger) entity split modal — a straight port of the legacy
 * `slEntrySetter` directive (sl-setter-modal.js / sl-setter-modal.jsp), triggered
 * directly from the "SL" button on a journal entry row (journal-entries.js), not
 * from the account-settings modal.
 */
@Component({
    selector: 'app-sl-entry-setter-modal',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerCheck, tablerX, tablerPlus, tablerTrash })],
    templateUrl: './sl-entry-setter-modal.component.html'
})
export class SlEntrySetterModalComponent implements OnInit {
    @Input() entry: JournalEntry = { account: null, debit: null, credit: null };
    @Input() defaultEntity?: any;

    activeModal = inject(NgbActiveModal);
    private modalService = inject(ModalService);
    private alertService = inject(AlertService);

    slEntries: SlEntry[] = [];
    // The row's debit/credit at the moment this modal was opened — the target the
    // SL split must reconcile to (mirrors legacy's entryAmountDebit/entryAmountCredit).
    entryDebit = 0;
    entryCredit = 0;

    ngOnInit(): void {
        this.entryDebit  = Number(this.entry.debit)  || 0;
        this.entryCredit = Number(this.entry.credit) || 0;
        this.slEntries = (this.entry.slentries || []).map(e => ({ ...e }));

        // initSLEntry(): seed a single default row against the voucher's default
        // entity (e.g. the payee) when there's nothing set up yet.
        if (this.slEntries.length === 0 && this.defaultEntity) {
            this.slEntries.push({
                entity: this.defaultEntity,
                accountNo: this.defaultEntity.accountNo,
                name: this.defaultEntity.name,
                debit: this.entryDebit  || null,
                credit: this.entryCredit || null
            });
        }
    }

    accountLabel(): string {
        const a = this.entry.account;
        if (!a) return '—';
        return `${a.accountCode || ''} — ${a.accountTitle || a.accountDescription || ''}`.trim();
    }

    get slTotalDebit(): number {
        return this.slEntries.reduce((s, e) => s + (Number(e.debit) || 0), 0);
    }

    get slTotalCredit(): number {
        return this.slEntries.reduce((s, e) => s + (Number(e.credit) || 0), 0);
    }

    debitBalanced(): boolean {
        return Math.round(this.slTotalDebit * 100) === Math.round(this.entryDebit * 100);
    }

    creditBalanced(): boolean {
        return Math.round(this.slTotalCredit * 100) === Math.round(this.entryCredit * 100);
    }

    /** newBlankRow(): pre-fill the new row with whatever's left of the entry's amount. */
    addRow(): void {
        const varDebit  = this.slTotalDebit  < this.entryDebit  ? +(this.entryDebit  - this.slTotalDebit).toFixed(2)  : 0;
        const varCredit = this.slTotalCredit < this.entryCredit ? +(this.entryCredit - this.slTotalCredit).toFixed(2) : 0;

        this.slEntries.push({ entity: null, accountNo: null, name: '', debit: varDebit || null, credit: varCredit || null });
    }

    removeRow(index: number): void {
        this.slEntries.splice(index, 1);
    }

    async browseEntity(index: number): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseEntityModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                const entity = result.data;
                this.slEntries[index] = {
                    ...this.slEntries[index],
                    entity,
                    accountNo: entity.accountNo,
                    name: entity.name
                };
            }
        } catch { }
    }

    /** saveEntries(): same validation order as legacy sl-setter-modal.js. */
    save(): void {
        if (this.slEntries.length === 0) {
            this.alertService.warning('SL Entries', 'Validation', 'Set SL entries.');
            return;
        }
        if (!this.debitBalanced()) {
            this.alertService.warning('SL Entries', 'Validation', "Total debit amount of all SL entities is not equal to account's debit amount.");
            return;
        }
        if (!this.creditBalanced()) {
            this.alertService.warning('SL Entries', 'Validation', "Total credit amount of all SL entities is not equal to account's credit amount.");
            return;
        }
        if (this.slEntries.some(e => !e.accountNo)) {
            this.alertService.warning('SL Entries', 'Validation', 'Missing SL entity!');
            return;
        }
        if (this.slEntries.some(e => !(e.debit || e.credit))) {
            this.alertService.warning('SL Entries', 'Validation', 'Input SL amount.');
            return;
        }

        this.activeModal.close({ action: 'save', data: { slentries: this.slEntries } });
    }

    dismiss(): void {
        this.activeModal.dismiss();
    }
}
