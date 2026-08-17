import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { VoucherCashflowService } from '../voucher-cashflow.service';

interface CashflowAssignmentItem {
    cashflowItemId:   number;
    cashflowItemName: string;
    amount:           number;
}

@Component({
    selector: 'app-voucher-cashflow-setup',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './voucher-cashflow-setup.component.html'
})
export class VoucherCashflowSetupComponent {
    module    = 'Cash Flow Account Settings';
    subModule = 'Setup';
    menuLink  = 'voucher-cashflow';

    documentTypeCode = '';
    voucherId        = 0;

    voucher         = signal<any>(null);
    generalLedger   = signal<any[]>([]);
    cashflowDetails = signal<any[]>([]);
    cashflowItems   = signal<any[]>([]);

    isLoading = signal(false);
    isSaving  = signal(false);

    /** Set via monthly-cycle API — mirrors legacy `hide` flag */
    isClosed = false;

    /** Tracks which ledger row has the inline panel open */
    activeLedgerId: number | null = null;

    /** Inline form fields */
    selectedCashflowItemId: number | null = null;
    inlineAmount: number | null = null;

    /** Per-ledger cashflow assignments (built up before saving) */
    cashflowAssignments: { [ledgerId: number]: CashflowAssignmentItem[] } = {};

    private service      = inject(VoucherCashflowService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.documentTypeCode = params.get('documentTypeCode') || '';
            this.voucherId        = Number(params.get('voucherId')) || 0;
            if (this.documentTypeCode && this.voucherId) {
                this.loadCashflowItems();
                this.loadVoucher();
            }
        });
    }

    loadCashflowItems(): void {
        this.service.getCashflowItems().subscribe({
            next: (items) => this.cashflowItems.set(items || []),
            error: () => {}
        });
    }

    loadVoucher(): void {
        this.isLoading.set(true);
        this.service.getVoucher(this.documentTypeCode, this.voucherId).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data) {
                    this.voucher.set(data);
                    this.generalLedger.set(data.generalLedger || []);
                    this.cashflowDetails.set(data.cashflowDetails || []);
                    this.loadSavedDetails(data);
                    this.checkMonthlyCycle(data.voucherDate || data.date);
                } else {
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Load Error', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    /** Mirrors legacy: GET /monthly-cycle/{year}/{month} → hide = (status === 'CLOSE') */
    checkMonthlyCycle(voucherDate: string): void {
        if (!voucherDate) return;
        const d     = new Date(voucherDate);
        const year  = d.getFullYear();
        const month = d.getMonth() + 1;
        this.service.getMonthlyCycle(year, month).subscribe({
            next: (data) => {
                this.isClosed = data?.status === 'CLOSE';
            },
            error: () => {
                this.isClosed = false;
            }
        });
    }

    loadSavedDetails(data: any): void {
        const transId = data?.transId;
        if (!transId) return;
        this.service.getDetails(transId).subscribe({
            next: (details) => {
                this.cashflowDetails.set(details || []);
                this.buildAssignmentsFromDetails(details || []);
            },
            error: () => {}
        });
    }

    buildAssignmentsFromDetails(details: any[]): void {
        const map: { [ledgerId: number]: CashflowAssignmentItem[] } = {};
        for (const d of details) {
            const ledgerId = d.generalLedgerId ?? d.ledgerId;
            if (ledgerId == null) continue;
            if (!map[ledgerId]) map[ledgerId] = [];
            map[ledgerId].push({
                cashflowItemId:   d.cashflowItemId ?? d.id,
                cashflowItemName: d.cashflowItemName ?? d.cashflowItem?.name ?? '',
                amount:           d.amount ?? 0
            });
        }
        this.cashflowAssignments = map;
    }

    openInlinePanel(ledgerId: number): void {
        if (this.activeLedgerId === ledgerId) {
            this.activeLedgerId = null;
        } else {
            this.activeLedgerId         = ledgerId;
            this.selectedCashflowItemId = null;
            this.inlineAmount           = null;
        }
    }

    /** Returns the effective amount for a ledger entry (mirrors legacy isCredit logic) */
    entryAmount(entry: any): number {
        if (entry.isCredit) return Number(entry.credit) || 0;
        return Number(entry.debit) || 0;
    }

    /** Returns current total assigned for a ledger entry */
    assignedTotal(ledgerId: number): number {
        return (this.cashflowAssignments[ledgerId] || [])
            .reduce((sum, a) => sum + (Number(a.amount) || 0), 0);
    }

    addAssignment(ledgerId: number): void {
        if (!this.selectedCashflowItemId || !this.inlineAmount) {
            this.alertService.error(this.module, 'Validation', 'Please select a cashflow item and enter an amount.');
            return;
        }

        // Gap #1: total CF amount must not exceed entry amount (mirrors legacy addCf validation)
        const entry      = this.generalLedger().find(e => e.id === ledgerId);
        const entryAmt   = entry ? this.entryAmount(entry) : 0;
        const newTotal   = this.assignedTotal(ledgerId) + Number(this.inlineAmount);

        if (entryAmt > 0 && newTotal > entryAmt) {
            this.alertService.error(this.module, 'Validation', 'Total cashflow amount must not exceed the entry amount.');
            return;
        }

        const item = this.cashflowItems().find(i => i.id === this.selectedCashflowItemId);
        if (!this.cashflowAssignments[ledgerId]) {
            this.cashflowAssignments[ledgerId] = [];
        }
        this.cashflowAssignments[ledgerId].push({
            cashflowItemId:   this.selectedCashflowItemId,
            cashflowItemName: item?.name ?? '',
            amount:           this.inlineAmount
        });
        this.selectedCashflowItemId = null;
        this.inlineAmount           = null;
    }

    removeAssignment(ledgerId: number, index: number): void {
        if (this.cashflowAssignments[ledgerId]) {
            this.cashflowAssignments[ledgerId].splice(index, 1);
        }
    }

    get allAssignmentRows(): { ledgerId: number; entry: any; item: CashflowAssignmentItem }[] {
        const rows: { ledgerId: number; entry: any; item: CashflowAssignmentItem }[] = [];
        const ledger = this.generalLedger();
        for (const [ledgerIdStr, items] of Object.entries(this.cashflowAssignments)) {
            const ledgerId = Number(ledgerIdStr);
            const entry    = ledger.find(l => l.id === ledgerId) ?? null;
            for (const item of items) {
                rows.push({ ledgerId, entry, item });
            }
        }
        return rows;
    }

    save(): void {
        const cashflowData: any[] = [];

        for (const [ledgerIdStr, items] of Object.entries(this.cashflowAssignments)) {
            const ledgerId = Number(ledgerIdStr);

            // Gap #2: total CF amount per entry must equal entry amount (mirrors legacy okSetupFromModal validation)
            const entry    = this.generalLedger().find(e => e.id === ledgerId);
            const entryAmt = entry ? this.entryAmount(entry) : 0;
            const total    = items.reduce((sum, a) => sum + (Number(a.amount) || 0), 0);

            if (entryAmt > 0 && Math.abs(total - entryAmt) > 0.001) {
                this.alertService.error(
                    this.module,
                    'Validation',
                    `Total cashflow amount for entry "${entry?.code}" must equal the entry amount (${entryAmt.toFixed(2)}).`
                );
                return;
            }

            for (const item of items) {
                cashflowData.push({
                    generalLedgerId: ledgerId,
                    cashflowItemId:  item.cashflowItemId,
                    amount:          item.amount
                });
            }
        }

        if (cashflowData.length === 0) {
            this.alertService.error(this.module, 'Validation', 'No cashflow assignments to save.');
            return;
        }

        const payload = { voucherId: this.voucherId, cashflowData };
        this.isSaving.set(true);
        this.service.save(this.documentTypeCode, payload).subscribe({
            next: (res) => {
                this.isSaving.set(false);
                if (res?.success) {
                    this.alertService.success(this.module, 'Saved', 'Cashflow settings saved successfully.');
                    this.loadVoucher();
                    this.activeLedgerId = null;
                } else {
                    this.alertService.error(this.module, 'Save Failed', res?.failureMessage || '');
                }
            },
            error: () => {
                this.isSaving.set(false);
                this.alertService.error(this.module, 'Save Error', '');
            }
        });
    }

    clearAssignments(): void {
        this.cashflowAssignments = {};
        this.activeLedgerId      = null;
    }

    goBack(): void {
        this.router.navigate(['/' + this.menuLink]);
    }
}
