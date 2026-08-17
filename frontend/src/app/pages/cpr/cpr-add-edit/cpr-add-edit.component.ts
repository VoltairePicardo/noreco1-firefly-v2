import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { CprService } from '../cpr.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseCOAModalComponent } from '@/app/shared/modals/browse-coa-modal/browse-coa-modal.component';

const MONTHS = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
];

function roundUp(value: number, decimals: number): number {
    return Number(Math.round(Number(value + 'e' + decimals)) + 'e-' + decimals);
}

@Component({
    selector: 'app-cpr-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective
    ],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './cpr-add-edit.component.html'
})
export class CprAddEditComponent {
    module    = 'Asset Records (CPR)';
    subModule = 'Create';
    menuLink  = 'cpr';

    id: any    = null;
    editMode   = false;
    formSubmit = false;
    isLoading  = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    months = MONTHS;
    assetTypes = signal<any[]>([]);

    // Form fields
    description       = '';
    location          = '';
    selectedAssetType: any = null;
    acquisitionDate   = '';

    depreciationYears  = 0;
    depreciationMonths = 0;
    annualDepRate      = 0;
    monthlyDepRate     = 0;

    startYear  = new Date().getFullYear();
    startMonth = MONTHS[0];
    endYear    = 0;
    endMonth   = '';

    // Asset detail rows
    assetDetails: any[] = [];

    // Totals
    get totalValue(): number {
        return this.assetDetails.reduce((s, r) => s + (Number(r.value) || 0), 0);
    }
    get totalMonthlyDep(): number {
        return this.assetDetails.reduce((s, r) => s + (Number(r.monthlyDepreciation) || 0), 0);
    }
    get totalDepreciatedValue(): number {
        return this.assetDetails.reduce((s, r) => s + (Number(r.depreciatedValue) || 0), 0);
    }
    get totalRemainingValue(): number {
        return this.assetDetails.reduce((s, r) => s + (Number(r.remainingValue) || 0), 0);
    }

    private service      = inject(CprService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private modalService = inject(ModalService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.loadAssetTypes();
        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.editMode = idParam != null && /^\d+$/.test(idParam);
            if (this.editMode) {
                this.id        = Number(idParam);
                this.subModule = 'Edit';
                this.loadForEdit();
            } else {
                this.subModule = 'Create';
                this.addRow();
            }
        });
    }

    loadAssetTypes(): void {
        this.service.getAssetTypes().subscribe({
            next: (res) => {
                const items = res?.content ?? res ?? [];
                this.assetTypes.set(items);
            },
            error: () => {}
        });
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                if (!data?.id) {
                    this.isLoading.set(false);
                    this.alertService.error(this.module, 'Record not found.', '');
                    this.router.navigate(['/' + this.menuLink]);
                    return;
                }
                this.description       = data.description   || '';
                this.location          = data.location      || '';
                this.selectedAssetType = data.assetType     || null;
                this.acquisitionDate   = data.acquisitionDate ? new Date(data.acquisitionDate).toISOString().substring(0, 10) : '';
                this.depreciationYears  = data.depreciationYears  || 0;
                this.depreciationMonths = data.depreciationMonths || 0;
                this.annualDepRate      = data.annualDepreciationRate  || 0;
                this.monthlyDepRate     = data.monthlyDepreciationRate || 0;
                this.startYear  = data.startYear  || new Date().getFullYear();
                this.startMonth = typeof data.startMonth === 'string' ? data.startMonth : (MONTHS[(data.startMonth || 1) - 1] || MONTHS[0]);
                this.endYear    = data.endYear   || 0;
                this.endMonth   = typeof data.endMonth === 'string' ? data.endMonth : (MONTHS[(data.endMonth || 1) - 1] || '');

                // Load asset details
                this.service.getDetails(this.id).subscribe({
                    next: (details) => {
                        this.assetDetails = (details || []).map((d: any) => ({
                            id:                  d.id || null,
                            assetAccount:        d.assetAccount        || null,
                            expenseAccount:      d.expenseAccount      || null,
                            accumDepAccount:     d.accumDepAccount     || null,
                            value:               d.value               || 0,
                            monthlyDepreciation: d.monthlyDepreciation || 0,
                            depreciatedValue:    d.depreciatedValue    || 0,
                            depreciatedValueOrig:d.depreciatedValue    || 0,
                            remainingValue:      d.remainingValue      || 0,
                        }));
                        if (this.assetDetails.length === 0) this.addRow();
                        this.isLoading.set(false);
                    },
                    error: () => {
                        this.isLoading.set(false);
                        this.addRow();
                    }
                });
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load record.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    // ── Depreciation Calculations ──────────────────────────────────────────────

    onDepYearsChange(): void {
        this.depreciationMonths = Math.round(this.depreciationYears * 12);
        this.recalcRates();
        this.recalcEndDate();
        this.recalcAllRows();
    }

    onDepMonthsChange(): void {
        this.depreciationYears = Math.round((this.depreciationMonths / 12) * 100) / 100;
        this.recalcRates();
        this.recalcEndDate();
        this.recalcAllRows();
    }

    onStartYearChange(): void {
        this.recalcEndDate();
    }

    onStartMonthChange(): void {
        this.recalcEndDate();
    }

    recalcRates(): void {
        if (this.depreciationYears > 0) {
            this.annualDepRate = Math.round((100 / this.depreciationYears) * 10000) / 10000;
        } else {
            this.annualDepRate = 0;
        }
        if (this.depreciationMonths > 0) {
            this.monthlyDepRate = Math.round((100 / this.depreciationMonths) * 10000) / 10000;
        } else {
            this.monthlyDepRate = 0;
        }
    }

    recalcEndDate(): void {
        const startMonthIndex = MONTHS.indexOf(this.startMonth);
        if (startMonthIndex < 0 || !this.startYear || !this.depreciationMonths) return;
        this.endYear  = parseInt(String(this.startYear)) + Math.floor((startMonthIndex + parseInt(String(this.depreciationMonths))) / 12);
        const endMonthIndex = (startMonthIndex + parseInt(String(this.depreciationMonths))) % 12;
        this.endMonth = MONTHS[endMonthIndex];
    }

    recalcAllRows(): void {
        this.assetDetails.forEach(row => this.recalcRow(row));
    }

    recalcRow(row: any): void {
        if (this.depreciationMonths > 0 && row.value > 0) {
            row.monthlyDepreciation = roundUp(row.value / this.depreciationMonths, 2);
        } else {
            row.monthlyDepreciation = 0;
        }
        row.remainingValue = roundUp((row.value || 0) - (row.depreciatedValue || 0), 2);
    }

    onRowValueChange(row: any): void {
        this.recalcRow(row);
    }

    onRowDepreciatedValueChange(row: any): void {
        row.remainingValue = roundUp((row.value || 0) - (row.depreciatedValue || 0), 2);
    }

    // ── Asset Detail Row Management ────────────────────────────────────────────

    addRow(): void {
        this.assetDetails.push({
            id:                  null,
            assetAccount:        null,
            expenseAccount:      null,
            accumDepAccount:     null,
            value:               0,
            monthlyDepreciation: 0,
            depreciatedValue:    0,
            depreciatedValueOrig:0,
            remainingValue:      0,
        });
    }

    removeRow(index: number): void {
        this.assetDetails.splice(index, 1);
    }

    // ── Account Browse ─────────────────────────────────────────────────────────

    async browseAccount(field: 'asset' | 'expense' | 'accumDep', rowIndex: number): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseCOAModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                const account = result.data;
                const row     = this.assetDetails[rowIndex];

                // Duplicate validation within the same row
                const sameInRow = (field !== 'asset'    && row.assetAccount?.id    === account.id)
                               || (field !== 'expense'  && row.expenseAccount?.id  === account.id)
                               || (field !== 'accumDep' && row.accumDepAccount?.id === account.id);
                if (sameInRow) {
                    this.alertService.warning(this.module, 'Validation', 'The same account cannot be used for multiple fields in the same row.');
                    return;
                }

                // Asset Account uniqueness across rows
                if (field === 'asset') {
                    const usedElsewhere = this.assetDetails.some((r, idx) =>
                        idx !== rowIndex && (
                            r.assetAccount?.id    === account.id ||
                            r.expenseAccount?.id  === account.id ||
                            r.accumDepAccount?.id === account.id
                        )
                    );
                    if (usedElsewhere) {
                        this.alertService.warning(this.module, 'Validation', 'This account is already used in another row.');
                        return;
                    }
                }

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

    // ── Validation & Save ──────────────────────────────────────────────────────

    save(): void {
        const errors: string[] = [];

        if (!this.description?.trim()) errors.push('Description is required.');
        if (!this.selectedAssetType || !this.selectedAssetType.id) errors.push('Asset Type is required.');
        if (!this.depreciationMonths || this.depreciationMonths <= 0) errors.push('Depreciation Months must be greater than 0.');
        if (!this.startYear) errors.push('Start Year is required.');
        if (!this.startMonth) errors.push('Start Month is required.');
        if (!this.acquisitionDate) errors.push('Acquisition Date is required.');

        if (this.assetDetails.length === 0) {
            errors.push('At least one asset account detail is required.');
        } else {
            this.assetDetails.forEach((row, i) => {
                const n = i + 1;
                if (!row.assetAccount?.id)    errors.push(`Row ${n}: Asset Account is required.`);
                if (!row.expenseAccount?.id)  errors.push(`Row ${n}: Expense Account is required.`);
                if (!row.accumDepAccount?.id) errors.push(`Row ${n}: Accum. Dep. Account is required.`);
                if (!row.value || row.value <= 0) errors.push(`Row ${n}: Value must be greater than 0.`);
                if (row.value < row.depreciatedValue) errors.push(`Row ${n}: Asset value must be >= depreciated value.`);
                if (this.editMode && row.depreciatedValue < row.depreciatedValueOrig) errors.push(`Row ${n}: Depreciated value must be >= original depreciated value.`);
            });
        }

        if (errors.length > 0) {
            this.alertService.fieldWarning(this.module, 'Validation', errors);
            return;
        }

        this.formSubmit = true;

        const startMonthIndex  = MONTHS.indexOf(this.startMonth);
        const numericStartMonth = startMonthIndex + 1;
        const endMonthIndex    = MONTHS.indexOf(this.endMonth);
        const numericEndMonth  = endMonthIndex >= 0 ? endMonthIndex + 1 : 1;

        const payload: any = {
            id:                       this.editMode ? this.id : null,
            description:              this.description.trim(),
            location:                 this.location || null,
            assetType:                { id: this.selectedAssetType.id, description: this.selectedAssetType.description },
            acquisitionDate:          this.acquisitionDate,
            depreciationYears:        this.depreciationYears,
            depreciationMonths:       this.depreciationMonths,
            startYear:                this.startYear,
            startMonth:               numericStartMonth,
            endYear:                  this.endYear,
            endMonth:                 numericEndMonth,
            totalValue:               this.totalValue,
            totalMonthlyDepreciation: this.totalMonthlyDep,
            totalDepreciatedValue:    this.totalDepreciatedValue,
            totalRemainingValue:      this.totalRemainingValue,
            assetDetails:             this.assetDetails.map(r => ({
                id:                  r.id || null,
                assetAccount:        r.assetAccount        ? { id: r.assetAccount.id }        : null,
                expenseAccount:      r.expenseAccount      ? { id: r.expenseAccount.id }      : null,
                accumDepAccount:     r.accumDepAccount     ? { id: r.accumDepAccount.id }     : null,
                value:               r.value               || 0,
                monthlyDepreciation: r.monthlyDepreciation || 0,
                depreciatedValue:    r.depreciatedValue    || 0,
                remainingValue:      r.remainingValue      || 0,
            }))
        };

        const req$ = this.editMode ? this.service.update(payload) : this.service.create(payload);
        req$.subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Saved successfully.', '');
                    this.router.navigate(['/' + this.menuLink, res.modelId, 'detail']);
                } else {
                    this.alertService.error(this.module, 'Save failed.', res?.failureMessage || '');
                }
            },
            error: () => {
                this.formSubmit = false;
                this.alertService.error(this.module, 'An error occurred while saving.', '');
            }
        });
    }

    accountLabel(account: any): string {
        if (!account) return '';
        return (account.code || account.accountCode || '') + ' - ' + (account.title || account.accountTitle || account.accountDescription || '');
    }
}
