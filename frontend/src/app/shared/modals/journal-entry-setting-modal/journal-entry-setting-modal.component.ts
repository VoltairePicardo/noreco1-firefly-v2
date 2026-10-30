import { Component, inject, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { HttpClient } from '@angular/common/http';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { provideIcons } from '@ng-icons/core';
import { tablerCheck, tablerX } from '@ng-icons/tabler-icons';
import { environment } from '@/environments/environment';
import { JournalEntry } from '@/app/shared/forms/journal-entries-form/journal-entries-form.component';
import { AlertService } from '@/app/shared/services/alert.service';

const BASE_API = environment.get('baseApiUrl');

@Component({
    selector: 'app-journal-entry-setting-modal',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerCheck, tablerX })],
    templateUrl: './journal-entry-setting-modal.component.html'
})
export class JournalEntrySettingModalComponent implements OnInit {
    @Input() entry: JournalEntry = { account: null, debit: null, credit: null };
    @Input() voucherDate?: string;
    @Input() defaultEntity?: any;

    activeModal = inject(NgbActiveModal);
    private http = inject(HttpClient);
    private alertService = inject(AlertService);

    baseDebit = 0;
    baseCredit = 0;

    applyAllocation  = false;
    allocationPct: number | null = null;

    applyWht          = false;
    atcList: any[]    = [];
    selectedAtc: any  = null;
    whtNature         = '';
    whtRate: number | null  = null;
    whtAmount: number | null = null;

    isVatable         = false;
    applyVat          = false;
    vatAccount: any   = null;
    vatPercentage: number | null = null;
    vatAmount: number | null = null;

    ngOnInit(): void {
        const debit  = Number(this.entry.debit)  || 0;
        const credit = Number(this.entry.credit) || 0;
        const untouched = this.entry._grossDebit != null && this.entry._grossCredit != null
            && debit  === (Number(this.entry._netDebit)  || 0)
            && credit === (Number(this.entry._netCredit) || 0);

        this.baseDebit  = untouched ? this.entry._grossDebit!  : debit;
        this.baseCredit = untouched ? this.entry._grossCredit! : credit;

        this.applyAllocation = this.entry.applyAllocation || false;
        this.allocationPct   = this.entry.allocationPct   ?? null;
        this.applyWht        = this.entry.applyWht         || false;

        if (this.entry.wht) {
            this.selectedAtc = this.entry.wht.atc   || null;
            this.whtNature   = this.entry.wht.nature || '';
            this.whtRate     = this.entry.wht.rate   ?? null;
            this.whtAmount   = this.entry.wht.amount ?? null;
        }

        this.http.get<any[]>(`${BASE_API}/json/tax-codes`).subscribe({
            next:  (data) => { this.atcList = data || []; },
            error: ()     => { this.atcList = []; }
        });

        this.isVatable = !!this.defaultEntity?.vatable;
        this.applyVat  = this.entry.vatEntry ? true : this.isVatable;
        if (this.isVatable) {
            this.http.get<any>(`${BASE_API}/accounting/accounts/vat`).subscribe({
                next: (data) => {
                    this.vatAccount    = data || null;
                    this.vatPercentage = data?.value != null ? Number(data.value) : null;
                    this.computeVatAmount(!!this.entry.wht);
                },
                error: () => { this.vatAccount = null; this.vatPercentage = null; }
            });
        }
    }

    accountLabel(): string {
        const a = this.entry.account;
        if (!a) return '—';
        return `${a.accountCode || ''} — ${a.accountTitle || a.accountDescription || ''}`.trim();
    }

    get taxBase(): number {
        return this.baseDebit || this.baseCredit || 0;
    }

    get whtBase(): number {
        const vat = this.applyVat && this.isVatable ? (this.vatAmount || 0) : 0;
        return Math.max(+(this.taxBase - vat).toFixed(2), 0);
    }

    onAtcChange(): void {
        if (!this.selectedAtc) {
            this.whtNature = '';
            this.whtRate   = null;
            this.whtAmount = null;
            return;
        }
        this.whtNature = this.selectedAtc.description || '';
        this.whtRate   = this.selectedAtc.rate != null ? Number(this.selectedAtc.rate) : null;
        this.computeWhtAmount();

    }

    /** ATC accounts come from the API as { id, code, title }; the journal form and payload use accountCode/accountTitle. */
    private normalizeAccount(a: any): any {
        if (!a) return a;
        return { ...a, accountCode: a.accountCode ?? a.code, accountTitle: a.accountTitle ?? a.title, hasSL: a.hasSL ?? false };
    }

    onWhtToggle(): void {
        if (this.applyWht && this.whtAmount == null) this.computeWhtAmount();
    }

    computeWhtAmount(): void {
        const rate = Number(this.whtRate);
        this.whtAmount = this.whtRate != null && !isNaN(rate)
            ? +(this.whtBase * rate / 100).toFixed(2)
            : null;
    }

    computeVatAmount(keepSavedWht = false): void {
        if (this.vatPercentage == null) {
            this.vatAmount = null;
        } else {
            const net = this.taxBase / ((100 + this.vatPercentage) / 100);
            this.vatAmount = +(this.taxBase - net).toFixed(2);
        }
        if (!keepSavedWht) this.computeWhtAmount();
    }

    save(): void {
        if (this.applyWht && (this.selectedAtc == null)) {
            this.alertService.warning('Journal Entry', 'Validation', 'Select an ATC / income payment for withholding tax.');
            return;
        }
        if (this.applyWht && !(this.whtRate! > 0)) {
            this.alertService.warning('Journal Entry', 'Validation', 'Enter a withholding tax percentage.');
            return;
        }

        let netDebit  = this.baseDebit;
        let netCredit = this.baseCredit;
        const siblings: JournalEntry[] = [];

        if (this.applyWht && this.whtAmount) {
            if (netDebit  > 0) netDebit  = +(netDebit  - this.whtAmount).toFixed(2);
            if (netCredit > 0) netCredit = +(netCredit - this.whtAmount).toFixed(2);

            siblings.push({
                account: this.normalizeAccount(this.selectedAtc.account),
                debit: 0,
                credit: this.whtAmount,
                wTaxEntry: {
                    atc: this.selectedAtc,
                    nature: this.whtNature,
                    percentage: this.whtRate,
                    baseAmount: this.whtBase,
                    amount: this.whtAmount
                }
            });
        }

        if (this.applyVat && this.isVatable && this.vatAmount) {
            if (netDebit  > 0) netDebit  = +(netDebit  - this.vatAmount).toFixed(2);
            if (netCredit > 0) netCredit = +(netCredit - this.vatAmount).toFixed(2);

            siblings.push({
                account: this.vatAccount,
                debit:  this.baseDebit  > 0 ? this.vatAmount : 0,
                credit: this.baseCredit > 0 ? this.vatAmount : 0,
                vatEntry: {
                    percentage: this.vatPercentage,
                    amount: this.vatAmount,
                    account: this.vatAccount
                }
            });
        }

        this.activeModal.close({
            action: 'save',
            data: {
                debit:  netDebit  || null,
                credit: netCredit || null,
                _grossDebit:  this.baseDebit,
                _grossCredit: this.baseCredit,
                _netDebit:    netDebit  || null,
                _netCredit:   netCredit || null,
                applyAllocation: this.applyAllocation,
                allocationPct:   this.applyAllocation ? this.allocationPct : null,
                applyWht:        this.applyWht,
                wht: this.applyWht ? {
                    atc:    this.selectedAtc,
                    nature: this.whtNature,
                    rate:   this.whtRate,
                    amount: this.whtAmount
                } : null
            },
            siblings
        });
    }

    dismiss(): void {
        this.activeModal.dismiss();
    }
}
