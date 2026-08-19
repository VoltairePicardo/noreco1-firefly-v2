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

const BASE_API = environment.get('baseApiUrl');

@Component({
    selector: 'app-journal-entry-setting-modal',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerCheck, tablerX })],
    templateUrl: './journal-entry-setting-modal.component.html'
})
export class JournalEntrySettingModalComponent implements OnInit {
    @Input() entry: JournalEntry = { account: null, debit: null, credit: null };

    activeModal = inject(NgbActiveModal);
    private http = inject(HttpClient);

    // Allocation
    applyAllocation  = false;
    allocationPct: number | null = null;

    // Withholding Tax
    applyWht          = false;
    atcList: any[]    = [];
    selectedAtc: any  = null;
    whtNature         = '';
    whtRate: number | null  = null;
    whtBase: number | null  = null;
    whtAmount: number | null = null;

    ngOnInit(): void {
        this.applyAllocation = this.entry.applyAllocation  || false;
        this.allocationPct   = this.entry.allocationPct   ?? null;
        this.applyWht        = this.entry.applyWht         || false;
        this.whtBase         = this.entry.debit || this.entry.credit || null;

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
    }

    accountLabel(): string {
        const a = this.entry.account;
        if (!a) return '—';
        return `${a.accountCode || ''} — ${a.accountTitle || a.accountDescription || ''}`.trim();
    }

    get taxBase(): number {
        return this.entry.debit || this.entry.credit || 0;
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

    computeWhtAmount(): void {
        this.whtAmount = this.whtRate != null
            ? +(this.taxBase * this.whtRate / 100).toFixed(2)
            : null;
    }

    save(): void {
        this.activeModal.close({
            action: 'save',
            data: {
                applyAllocation: this.applyAllocation,
                allocationPct:   this.applyAllocation ? this.allocationPct : null,
                applyWht:        this.applyWht,
                wht: this.applyWht ? {
                    atc:    this.selectedAtc,
                    nature: this.whtNature,
                    rate:   this.whtRate,
                    amount: this.whtAmount
                } : null
            }
        });
    }

    dismiss(): void {
        this.activeModal.dismiss();
    }
}
