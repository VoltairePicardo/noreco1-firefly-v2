import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { CprService } from '../cpr.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseVoucherModalComponent } from '@/app/shared/modals/browse-voucher-modal/browse-voucher-modal.component';
import { BrowseCOAModalComponent } from '@/app/shared/modals/browse-coa-modal/browse-coa-modal.component';

const MONTHS = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
];

// Link type IDs (matching old Firefly constants)
const LINK_TYPE_ACQUISITION  = 1;
const LINK_TYPE_MAJOR_REPAIR = 2;
const LINK_TYPE_MINOR_REPAIR = 3;
const LINK_TYPE_RETIREMENT   = 4;
const LINK_TYPE_ADJUSTMENT   = 5;

const ADJUSTMENT_TYPES = [
    { id: 'ADD_TO_ASSET',         description: 'Add to Asset' },
    { id: 'DEDUCT_FROM_ASSET',    description: 'Deduct from Asset' },
    { id: 'ADD_DEPRECIATION',     description: 'Add depreciation' },
    { id: 'DEDUCT_DEPRECIATION',  description: 'Deduct depreciation' },
];

function roundUp(value: number, decimals: number): number {
    return Number(Math.round(Number(value + 'e' + decimals)) + 'e-' + decimals);
}

function toMonthName(m: any): string {
    if (typeof m === 'string' && MONTHS.includes(m)) return m;
    const idx = (Number(m) || 1) - 1;
    return MONTHS[idx] || MONTHS[0];
}

@Component({
    selector: 'app-cpr-link-voucher',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
    ],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './cpr-link-voucher.component.html'
})
export class CprLinkVoucherComponent {
    module    = 'Asset Records (CPR)';
    subModule = 'Link Voucher';
    menuLink  = 'cpr';

    id: any   = null;
    data: any = {};
    isLoading  = signal(false);
    formSubmit = false;

    // Dropdowns
    linkTypes       = signal<any[]>([]);
    adjustmentTypes = ADJUSTMENT_TYPES;
    selectedLinkType:       any = null;
    selectedAdjustmentType: any = null;

    // Selected voucher
    selectedVoucher: any = null;

    // Extended section state (copied from CPR data when link type changes)
    depYears  = 0;
    depMonths = 0;
    endYear   = 0;
    endMonth  = '';

    schedule:     any[] = [];
    assetDetails: any[] = [];

    // Totals
    get totalValue():            number { return this.assetDetails.reduce((s, r) => s + (Number(r.value) || 0), 0); }
    get totalDepreciatedValue(): number { return this.assetDetails.reduce((s, r) => s + (Number(r.depreciatedValue) || 0), 0); }
    get totalMonthlyDep():       number { return this.assetDetails.reduce((s, r) => s + (Number(r.monthlyDepreciation) || 0), 0); }
    get totalRemainingValue():   number { return this.assetDetails.reduce((s, r) => s + (Number(r.remainingValue) || 0), 0); }
    get totalDebit():            number { return this.assetDetails.reduce((s, r) => s + (Number(r.debit) || 0), 0); }

    // Link type flags
    get isMajorRepair(): boolean { return this.selectedLinkType?.id === LINK_TYPE_MAJOR_REPAIR; }
    get isAdjustment():  boolean { return this.selectedLinkType?.id === LINK_TYPE_ADJUSTMENT; }
    get isAcquisition(): boolean { return this.selectedLinkType?.id === LINK_TYPE_ACQUISITION; }
    get isRetirement():  boolean { return this.selectedLinkType?.id === LINK_TYPE_RETIREMENT; }
    get showExtended():  boolean { return this.isMajorRepair || this.isAdjustment || this.isAcquisition || this.isRetirement; }
    /** Editable accounts + value/dep inputs: adjustment or retirement */
    get isEditable():    boolean { return this.isAdjustment || this.isRetirement; }

    private service      = inject(CprService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private modalService = inject(ModalService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.id = params.get('id');
            if (this.id && /^\d+$/.test(String(this.id))) {
                this.loadData();
                this.loadLinkTypes();
            }
        });
    }

    loadData(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (!data?.id) {
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                    return;
                }
                this.data = data;
                // Prime dep fields from CPR
                this.depYears  = data.depreciationYears  || 0;
                this.depMonths = data.depreciationMonths || 0;
                this.endYear   = data.endYear  || 0;
                this.endMonth  = toMonthName(data.endMonth);
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Error loading CPR', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    loadLinkTypes(): void {
        this.service.getLinkTypes().subscribe({
            next: (types) => this.linkTypes.set(types || []),
            error: () => {}
        });
    }

    // ── Link type change ─────────────────────────────────────────────────────────

    onLinkTypeChange(): void {
        // Reset adjustment type when link type changes
        this.selectedAdjustmentType = this.isAdjustment ? this.adjustmentTypes[0] : null;

        // Reset dep fields to CPR values
        this.depYears  = this.data.depreciationYears  || 0;
        this.depMonths = this.data.depreciationMonths || 0;
        this.endYear   = this.data.endYear  || 0;
        this.endMonth  = toMonthName(this.data.endMonth);

        this.schedule     = [];
        this.assetDetails = [];

        if (!this.showExtended) return;

        // Load details and schedule in parallel
        this.isLoading.set(true);
        forkJoin({
            details:  this.service.getDetails(this.id),
            schedule: this.service.getDepreciationSchedule(this.id),
        }).subscribe({
            next: ({ details, schedule }) => {
                this.assetDetails = (details || []).map((d: any) => ({
                    id:                  d.id || null,
                    assetAccount:        d.assetAccount        || null,
                    expenseAccount:      d.expenseAccount      || null,
                    accumDepAccount:     d.accumDepAccount     || null,
                    value:               Number(d.value)               || 0,
                    monthlyDepreciation: Number(d.monthlyDepreciation) || 0,
                    depreciatedValue:    Number(d.depreciatedValue)    || 0,
                    remainingValue:      Number(d.remainingValue)      || 0,
                    debit:               Number(d.debit)               || 0,
                    newRow:              false,
                }));
                this.schedule = schedule || [];
                this.isLoading.set(false);
            },
            error: () => { this.isLoading.set(false); }
        });
    }

    // ── Depreciation recalc ───────────────────────────────────────────────────────

    onDepYearsChange(): void {
        this.depMonths = Math.round(this.depYears * 12);
        this.recalcEndDate();
        this.recalcAllRows();
    }

    onDepMonthsChange(): void {
        this.depYears = Math.round((this.depMonths / 12) * 100) / 100;
        this.recalcEndDate();
        this.recalcAllRows();
    }

    recalcEndDate(): void {
        const startMonthName  = toMonthName(this.data.startMonth);
        const startMonthIndex = MONTHS.indexOf(startMonthName);
        if (startMonthIndex < 0 || !this.data.startYear || !this.depMonths) return;
        this.endYear = parseInt(String(this.data.startYear)) +
            Math.floor((startMonthIndex + parseInt(String(this.depMonths))) / 12);
        const endMonthIdx = (startMonthIndex + parseInt(String(this.depMonths))) % 12;
        this.endMonth = MONTHS[endMonthIdx];
    }

    recalcAllRows(): void {
        this.assetDetails.forEach(row => this.recalcRow(row));
    }

    recalcRow(row: any): void {
        row.remainingValue = roundUp((Number(row.value) || 0) - (Number(row.depreciatedValue) || 0), 2);
        if (this.depMonths > 0 && row.remainingValue > 0) {
            row.monthlyDepreciation = roundUp(row.remainingValue / this.depMonths, 2);
        } else {
            row.monthlyDepreciation = 0;
        }
    }

    onRowValueChange(row: any): void { this.recalcRow(row); }
    onRowDepChange(row: any):   void { this.recalcRow(row); }

    // ── Account management (editable rows only) ────────────────────────────────────

    addRow(): void {
        this.assetDetails.push({
            id: null, newRow: true,
            assetAccount: null, expenseAccount: null, accumDepAccount: null,
            value: 0, monthlyDepreciation: 0, depreciatedValue: 0, remainingValue: 0, debit: 0
        });
    }

    removeRow(i: number): void { this.assetDetails.splice(i, 1); }

    async browseAccount(field: 'asset' | 'expense' | 'accumDep', rowIndex: number): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseCOAModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                const account = result.data;
                const row     = this.assetDetails[rowIndex];
                if (field === 'asset')    row.assetAccount    = account;
                if (field === 'expense')  row.expenseAccount  = account;
                if (field === 'accumDep') row.accumDepAccount = account;
            }
        } catch { }
    }

    clearAccount(field: 'asset' | 'expense' | 'accumDep', rowIndex: number): void {
        const row = this.assetDetails[rowIndex];
        if (field === 'asset')    row.assetAccount    = null;
        if (field === 'expense')  row.expenseAccount  = null;
        if (field === 'accumDep') row.accumDepAccount = null;
    }

    accountLabel(account: any): string {
        if (!account) return '';
        return (account.code || account.accountCode || '') + ' — ' + (account.title || account.accountTitle || account.accountDescription || '');
    }

    // ── Voucher browse ─────────────────────────────────────────────────────────────

    async browseVoucher(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseVoucherModalComponent,
                { accountNo: this.data.accountNo || '' },
                { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.selectedVoucher = result.data;
            }
        } catch { }
    }

    clearVoucher(): void { this.selectedVoucher = null; }

    // ── Month name helper ──────────────────────────────────────────────────────────

    monthName(m: any): string { return toMonthName(m); }

    // ── Save ──────────────────────────────────────────────────────────────────────

    save(): void {
        const errors: string[] = [];

        if (!this.selectedVoucher)             errors.push('Please select a voucher.');
        if (!this.selectedLinkType?.id)        errors.push('Transaction Type is required.');
        if (this.isAdjustment && !this.selectedAdjustmentType) errors.push('Adjustment Type is required.');

        if (this.showExtended) {
            if (!this.depMonths || this.depMonths <= 0) errors.push('Depreciation Months must be greater than 0.');
            this.assetDetails.forEach((row, i) => {
                const n = i + 1;
                if (!row.assetAccount?.id)    errors.push(`Row ${n}: Asset Account is required.`);
                if (!row.expenseAccount?.id)  errors.push(`Row ${n}: Expense Account is required.`);
                if (!row.accumDepAccount?.id) errors.push(`Row ${n}: Accum. Dep. Account is required.`);
                if (!row.value || row.value <= 0) errors.push(`Row ${n}: Value must be greater than 0.`);
            });
        }

        if (errors.length > 0) {
            this.alertService.fieldWarning(this.module, 'Validation', errors);
            return;
        }

        this.formSubmit = true;

        const asset: any = { id: this.id };
        if (this.isMajorRepair || this.isAdjustment || this.isAcquisition) {
            asset.totalValue               = this.totalValue;
            asset.totalMonthlyDepreciation = this.totalMonthlyDep;
            asset.totalDepreciatedValue    = this.totalDepreciatedValue;
            asset.totalRemainingValue      = this.totalRemainingValue;
            asset.depreciationYears        = this.depYears;
            asset.depreciationMonths       = this.depMonths;
            asset.endYear                  = this.endYear;
            asset.endMonth                 = MONTHS.indexOf(this.endMonth) + 1;
        }

        const payload: any = {
            asset,
            voucherTransaction: { id: this.selectedVoucher.transactionId },
            documentType:       { id: this.selectedVoucher.documentTypeId },
            linkType:           this.selectedLinkType,
        };

        if (this.isAdjustment) {
            payload.adjustmentType = this.selectedAdjustmentType.id;
        }

        if (this.showExtended) {
            payload.assetDetails = this.assetDetails;
        }

        this.service.linkAssetVoucher(payload).subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Asset & Voucher linked successfully.', '');
                    this.router.navigate(['/' + this.menuLink, this.id, 'detail']);
                } else {
                    this.alertService.error(this.module, 'Link failed.', res?.failureMessage || '');
                }
            },
            error: () => {
                this.formSubmit = false;
                this.alertService.error(this.module, 'An error occurred while saving.', '');
            }
        });
    }

    goBack(): void {
        this.router.navigate(['/' + this.menuLink, this.id, 'detail']);
    }
}
