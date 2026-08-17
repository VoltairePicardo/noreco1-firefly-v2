# Accounting Reports Migration Alignment — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Forensically align every NEW Firefly v2 accounting report component against its OLD Firefly counterpart, fixing all functional, input, validation, parameter, and output gaps, then create the missing `pe-summary` report.

**Architecture:** Each report is a standalone Angular 19 standalone component under `frontend/src/app/pages/accounting-reports/reports/`. Shared state goes through `AccountingReportsService`. The main landing page (`accounting-reports-main`) routes to each report. All audits follow the same pattern: read OLD JSP + OLD JS → read NEW .ts + .html → compare every dimension → fix in place.

**Tech Stack:** Angular 19, TypeScript, `angularx-flatpickr`, `@ng-bootstrap/ng-bootstrap`, `NgbModal`, `BrowseEntityModalComponent`, `AccountingReportsService`, `DownloadService`, `AlertService`, tabler icons via `ng-icon`

## Global Constraints

- Button classes: View/Save = `btn-primary fw-bold`, Export/Print = `btn-success fw-bold`, Cancel/Back = `btn-light fw-bold`
- Tables: `table table-custom table-centered table-hover w-100 mb-0`, thead: `bg-light align-middle bg-opacity-25 thead-sm`, tr: `text-uppercase fs-xxs`
- Dates: always `mwlFlatpickr` with `flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' }`
- Alerts: always `AlertService` (SweetAlert2 wrapper) — never native `alert()`/`confirm()`
- Icons: `ng-icon` with tabler icon names
- Empty state: `<tr><td [attr.colspan]="N" class="text-center text-muted py-3">No records found.</td></tr>`
- Loading state: `<tr><td [attr.colspan]="N" class="text-center text-muted py-3"><span class="spinner-border spinner-border-sm me-2"></span>Loading...</td></tr>`
- OLD source: `noreco1-firefly/src/main/webapp/WEB-INF/views/reports/partials/`
- OLD JS: `noreco1-firefly/src/main/webapp/resources/js/app/module/rep-accounting.js`
- NEW reports: `noreco1-firefly-v2/frontend/src/app/pages/accounting-reports/reports/`
- Do NOT modify OLD files

---

## Audit Checklist (apply to every report)

For each report, verify:
- [ ] All OLD input fields present (date ranges, as-of dates, status dropdowns, year/month, lookups)
- [ ] Default values match OLD (first/last of current month, current date, 'Unaudited', 'All')
- [ ] Required field validation before search AND before export matches OLD
- [ ] Date range validation (same-year check where OLD enforces it)
- [ ] API call parameters match OLD (field names, types, order, null handling)
- [ ] Export URL pattern matches OLD exactly
- [ ] Table columns match OLD (name, order, alignment)
- [ ] Totals/subtotals match OLD
- [ ] Signatory browse fields present where OLD has them
- [ ] Print actions match OLD (separate "form" vs "schedule" actions where applicable)

---

## Task 1: Fix `accounting-reports-main` — Category Misclassification + Add pe-summary

**Files:**
- Modify: `accounting-reports-main/accounting-reports-main.component.ts`

**Known issues:**
- `work-in-progress` and `work-order` are under Summaries → must move to Support Module Reports
- `pe-summary` is missing from Summaries

- [ ] **Step 1: Read the current file**

```
Read: frontend/src/app/pages/accounting-reports/accounting-reports-main/accounting-reports-main.component.ts
```

- [ ] **Step 2: Fix the `categories` array**

In `accounting-reports-main.component.ts`, apply these exact changes:

**Remove from Summaries:**
```typescript
// Remove these two entries from the Summaries options array:
{ label: 'Work In Progress',               route: 'work-in-progress' },
{ label: 'Work Order Aging',               route: 'work-order' },
```

**Add to Summaries** (after `'pr-summary'` entry, before `'depreciation-summary'`):
```typescript
{ label: 'Prepayment Expense Summary',     route: 'pe-summary' },
```

**In Support Module Reports options**, add the two moved reports at the TOP:
```typescript
{ label: 'Summary of Construction Work In Progress', route: 'work-in-progress' },
{ label: 'Aging of Work Order',                      route: 'work-order' },
// existing entries follow:
{ label: 'Work Order Transaction',         route: 'work-order-transaction' },
{ label: 'PCF Ledger',                     route: 'pcf-ledger' },
{ label: 'Unliquidated Cash Advance',      route: 'unliquidated-ca' },
```

Final Summaries options order (OLD order from `rep-accounting.js`):
```typescript
options: [
    { label: 'RV Summary',                     route: 'rv-summary' },
    { label: 'PO Summary',                     route: 'po-summary' },
    { label: 'JO Summary',                     route: 'jo-summary' },
    { label: 'JOA Summary',                    route: 'joa-summary' },
    { label: 'Canvass Summary',                route: 'canvass-summary' },
    { label: 'PR Summary',                     route: 'pr-summary' },
    { label: 'Prepayment Expense Summary',     route: 'pe-summary' },
    { label: 'Depreciation Summary',           route: 'depreciation-summary' },
    { label: 'Depreciation Schedule',          route: 'depreciation-schedule' },
    { label: 'Accounts Payable Aging',         route: 'accounts-payable-aging' },
    { label: 'Pending Voucher List',           route: 'pending-voucher-list' },
    { label: 'Quotation Summary',              route: 'quotation-summary' },
    { label: 'Summary of Petty Cash Vouchers', route: 'pcv-summary' },
    { label: 'Check Listing',                  route: 'check-list' },
    { label: 'Cash Flow Detail',               route: 'cash-flow-detail' },
    { label: 'Pending Purchase Requests',      route: 'pending-purchase-request' },
]
```

- [ ] **Step 3: Build check**

```bash
cd frontend && npx ng build --configuration=development 2>&1 | tail -20
```

Expected: no errors.

- [ ] **Step 4: Commit**

```bash
git add frontend/src/app/pages/accounting-reports/accounting-reports-main/accounting-reports-main.component.ts
git commit -m "fix(accounting-reports): fix category placement and add pe-summary to menu"
```

---

## Task 2: Add `pe-summary` service method + route

**Files:**
- Modify: `accounting-reports/accounting-reports.service.ts`
- Modify: `accounting-reports/accounting-reports.route.ts`

- [ ] **Step 1: Read service and route files**

```
Read: frontend/src/app/pages/accounting-reports/accounting-reports.service.ts
Read: frontend/src/app/pages/accounting-reports/accounting-reports.route.ts
```

- [ ] **Step 2: Add service method**

In `accounting-reports.service.ts`, add at the end (before the closing `}`):

```typescript
getPeSummary(month: number, year: number): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_API}/reports/accounting/pe-summary/${month}/${year}`);
}
```

- [ ] **Step 3: Add route**

In `accounting-reports.route.ts`, add inside `ACCOUNTING_REPORTS_ROUTES` after the `pr-summary` route entry:

```typescript
{
    path: 'pe-summary',
    loadComponent: () => import('./reports/pe-summary.component').then(m => m.PeSummaryComponent),
    data: { title: 'Prepayment Expense Summary' }
},
```

- [ ] **Step 4: Build check**

```bash
cd frontend && npx ng build --configuration=development 2>&1 | tail -20
```

Expected: may fail with "Cannot find module './reports/pe-summary.component'" — that is expected until Task 3 creates the file. If there are OTHER errors, fix them first.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/app/pages/accounting-reports/accounting-reports.service.ts \
        frontend/src/app/pages/accounting-reports/accounting-reports.route.ts
git commit -m "feat(accounting-reports): add pe-summary service method and route"
```

---

## Task 3: Create `pe-summary` component

**Files:**
- Create: `accounting-reports/reports/pe-summary.component.ts`
- Create: `accounting-reports/reports/pe-summary.component.html`

**OLD behavior (from `rep-accounting.js` — `summaryCtrl` case `"pe-summary"`):**
- Inputs: **Month** (name, e.g. "August") + **Year** (4-digit number)
- `intMonth` = integer derived from month name (1–12)
- API: `prepaymentFactory.getListForSummary(intMonth, year)` → internal path `/pe-summary/{intMonth}/{year}`
- Export URL: `/reports/export/pe-summary/{intMonth}/{year}/{monthName}?token=&type=`
- Columns: Reference | Description | No Of Months | Amount | Balance
- Totals: Total No. of Vouchers (count of rows) | Total Amount | Total Balance

- [ ] **Step 1: Create `pe-summary.component.ts`**

```typescript
import { Component, computed, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { AccountingReportsService } from '../accounting-reports.service';

@Component({
    selector: 'app-pe-summary',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './pe-summary.component.html'
})
export class PeSummaryComponent {
    module   = 'Prepayment Expense Summary';
    menuLink = 'accounting-reports';

    selectedMonth = new Date().getMonth() + 1;  // 1-based
    selectedYear  = new Date().getFullYear();

    months = [
        { id: 1,  name: 'January'   }, { id: 2,  name: 'February'  }, { id: 3,  name: 'March'     },
        { id: 4,  name: 'April'     }, { id: 5,  name: 'May'       }, { id: 6,  name: 'June'      },
        { id: 7,  name: 'July'      }, { id: 8,  name: 'August'    }, { id: 9,  name: 'September' },
        { id: 10, name: 'October'   }, { id: 11, name: 'November'  }, { id: 12, name: 'December'  },
    ];

    years: number[] = [];
    rows      = signal<any[]>([]);
    isLoading = signal(false);

    totalVouchers = computed(() => this.rows().length);
    totalAmount   = computed(() => this.rows().reduce((s, r) => s + (r.amount  || 0), 0));
    totalBalance  = computed(() => this.rows().reduce((s, r) => s + (r.balance || 0), 0));

    private downloadSvc  = inject(DownloadService);
    private alertService = inject(AlertService);
    private service      = inject(AccountingReportsService);

    ngOnInit(): void {
        const current = new Date().getFullYear();
        for (let y = current - 5; y <= current + 2; y++) this.years.push(y);
    }

    get selectedMonthName(): string {
        return this.months.find(m => m.id === this.selectedMonth)?.name ?? '';
    }

    search(): void {
        this.isLoading.set(true);
        this.rows.set([]);
        this.service.getPeSummary(this.selectedMonth, this.selectedYear).subscribe({
            next: (data) => { this.rows.set(data ?? []); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); }
        });
    }

    export(type: 'pdf' | 'xls'): void {
        this.downloadSvc.print(
            `/reports/export/pe-summary/${this.selectedMonth}/${this.selectedYear}/${this.selectedMonthName}`,
            { type }
        );
    }
}
```

- [ ] **Step 2: Create `pe-summary.component.html`**

```html
<div class="container-fluid">
    <app-page-title [title]="'Accounting Reports'" [subTitle]="module" [menuLink]="menuLink" [useSubtitleAsTitle]="true"/>
</div>
<div class="container-fluid">
    <div class="card">
        <div class="card-body">
            <div class="row g-2 align-items-end mb-3">
                <div class="col-md-auto">
                    <label class="form-label fw-bold mb-1">Month</label>
                    <select class="form-select" [(ngModel)]="selectedMonth">
                        @for (m of months; track m.id) {
                            <option [ngValue]="m.id">{{ m.name }}</option>
                        }
                    </select>
                </div>
                <div class="col-md-auto">
                    <label class="form-label fw-bold mb-1">Year</label>
                    <select class="form-select" [(ngModel)]="selectedYear">
                        @for (y of years; track y) {
                            <option [ngValue]="y">{{ y }}</option>
                        }
                    </select>
                </div>
                <div class="col-md-auto ms-auto">
                    <div class="d-flex gap-2">
                        <button type="button" class="btn btn-primary fw-bold" (click)="search()">
                            <ng-icon name="tablerEye" class="fw-bold me-1"></ng-icon>View Only
                        </button>
                        <button type="button" class="btn btn-success fw-bold" (click)="export('pdf')">
                            <ng-icon name="tablerFileTypePdf" class="fw-bold me-1"></ng-icon>Export PDF
                        </button>
                        <button type="button" class="btn btn-success fw-bold" (click)="export('xls')">
                            <ng-icon name="tablerFileTypeXls" class="fw-bold me-1"></ng-icon>Export Excel
                        </button>
                    </div>
                </div>
            </div>

            <div class="table-responsive mt-2">
                <table class="table table-custom table-centered table-hover w-100 mb-0">
                    <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                        <tr class="text-uppercase fs-xxs">
                            <th>Reference</th>
                            <th>Description</th>
                            <th class="text-center">No. of Months</th>
                            <th class="text-end">Amount</th>
                            <th class="text-end">Balance</th>
                        </tr>
                    </thead>
                    <tbody>
                        @if (isLoading()) {
                            <tr><td colspan="5" class="text-center text-muted py-3">
                                <span class="spinner-border spinner-border-sm me-2"></span>Loading...
                            </td></tr>
                        } @else if (rows().length === 0) {
                            <tr><td colspan="5" class="text-center text-muted py-3">No records found.</td></tr>
                        } @else {
                            @for (row of rows(); track $index) {
                                <tr>
                                    <td class="fw-bold">{{ row.reference }}</td>
                                    <td>{{ row.particulars }}</td>
                                    <td class="text-center">{{ row.noOfMonths }}</td>
                                    <td class="text-end">{{ row.amount | number:'1.2-2' }}</td>
                                    <td class="text-end">{{ row.balance | number:'1.2-2' }}</td>
                                </tr>
                            }
                            <tr class="fw-bold border-top">
                                <td colspan="3" class="text-end text-primary">Total No. of Vouchers:</td>
                                <td colspan="2" class="fw-bold">{{ totalVouchers() }}</td>
                            </tr>
                            <tr class="fw-bold">
                                <td colspan="4" class="text-end text-primary">Total Amount:</td>
                                <td class="text-end fw-bold">{{ totalAmount() | number:'1.2-2' }}</td>
                            </tr>
                            <tr class="fw-bold">
                                <td colspan="4" class="text-end text-primary">Total Balance:</td>
                                <td class="text-end fw-bold">{{ totalBalance() | number:'1.2-2' }}</td>
                            </tr>
                        }
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
```

- [ ] **Step 3: Build check**

```bash
cd frontend && npx ng build --configuration=development 2>&1 | tail -20
```

Expected: no errors.

- [ ] **Step 4: Commit**

```bash
git add frontend/src/app/pages/accounting-reports/reports/pe-summary.component.ts \
        frontend/src/app/pages/accounting-reports/reports/pe-summary.component.html
git commit -m "feat(accounting-reports): create pe-summary (Prepayment Expense Summary) component"
```

---

## Task 4: Fix `cashflow-statement-nea` and `cashflow-statement-bsup` — Missing Validations

**Files:**
- Modify: `reports/cashflow-statement-nea.component.ts`
- Modify: `reports/cashflow-statement-nea.component.html`
- Modify: `reports/cashflow-statement-bsup.component.ts`
- Modify: `reports/cashflow-statement-bsup.component.html`

**OLD behavior (from `supportCtrl`, cases `"cashflow-statement"` and `"cashflow-statement-bsup"`):**
1. Same-year validation: `yearFrom != yearTo` → warn "Date range must be of the same year", abort search AND export
2. Checker/NotedBy required on export: both must be selected or error displayed
3. BrowseEntityModalComponent pattern: `types = ['ENTITY_EMPLOYEE']`

- [ ] **Step 1: Read both existing components**

```
Read: frontend/src/app/pages/accounting-reports/reports/cashflow-statement-nea.component.ts
Read: frontend/src/app/pages/accounting-reports/reports/cashflow-statement-nea.component.html
Read: frontend/src/app/pages/accounting-reports/reports/cashflow-statement-bsup.component.ts
Read: frontend/src/app/pages/accounting-reports/reports/cashflow-statement-bsup.component.html
```

- [ ] **Step 2: Update `cashflow-statement-nea.component.ts`**

Add helper + update `search()` and `export()`:

```typescript
// Add field-level error state
checkedByError  = '';
notedByError    = '';

// Add same-year validation helper
private sameYearValid(): boolean {
    const y1 = this.fromDate?.substring(0, 4);
    const y2 = this.toDate?.substring(0, 4);
    if (y1 && y2 && y1 !== y2) {
        this.alertService.error(this.module, 'Validation', 'Date range must be within the same year.');
        return false;
    }
    return true;
}

// Update search() to include same-year check
search(): void {
    if (!this.fromDate || !this.toDate) {
        this.alertService.error(this.module, 'Validation', 'Please select a date range.');
        return;
    }
    if (!this.sameYearValid()) return;
    // existing subscribe logic unchanged ...
}

// Update export() to validate checkedBy, notedBy, same-year
export(type: 'pdf' | 'xls'): void {
    if (!this.fromDate || !this.toDate) {
        this.alertService.error(this.module, 'Validation', 'Please select a date range.');
        return;
    }
    if (!this.sameYearValid()) return;
    this.checkedByError = '';
    this.notedByError   = '';
    let valid = true;
    if (!this.checkedBy) { this.checkedByError = 'Please select an officer.'; valid = false; }
    if (!this.notedBy)   { this.notedByError   = 'Please select an officer.'; valid = false; }
    if (!valid) {
        this.alertService.error(this.module, 'Validation', 'Please select all required officers.');
        return;
    }
    const checkedByAcctNo = this.checkedBy.accountNo;
    const notedByAcctNo   = this.notedBy.accountNo;
    this.downloadSvc.print(
        `/reports/export/cashflow-statement/${this.fromDate}/${this.toDate}/${checkedByAcctNo}/${notedByAcctNo}`,
        { type }
    );
}
```

- [ ] **Step 3: Update `cashflow-statement-nea.component.html`**

Add error messages under each browse input (after the browse button):

```html
@if (checkedByError) {
    <div class="text-danger small mt-1">{{ checkedByError }}</div>
}
```

```html
@if (notedByError) {
    <div class="text-danger small mt-1">{{ notedByError }}</div>
}
```

- [ ] **Step 4: Apply identical changes to `cashflow-statement-bsup.component.ts` and `.html`**

The only difference is the export URL:
```typescript
// bsup uses:
`/reports/export/cashflow-statement-bsup/${this.fromDate}/${this.toDate}/${checkedByAcctNo}/${notedByAcctNo}`
```

Apply the same `sameYearValid()`, `checkedByError`, `notedByError` fields and the same update to `search()` and `export()`.

Add error markup to `cashflow-statement-bsup.component.html` identically.

- [ ] **Step 5: Build check**

```bash
cd frontend && npx ng build --configuration=development 2>&1 | tail -20
```

- [ ] **Step 6: Commit**

```bash
git add frontend/src/app/pages/accounting-reports/reports/cashflow-statement-nea.component.ts \
        frontend/src/app/pages/accounting-reports/reports/cashflow-statement-nea.component.html \
        frontend/src/app/pages/accounting-reports/reports/cashflow-statement-bsup.component.ts \
        frontend/src/app/pages/accounting-reports/reports/cashflow-statement-bsup.component.html
git commit -m "fix(accounting-reports): add same-year validation and signatory required checks to cashflow statements"
```

---

## Task 5: Fix `work-in-progress` — Wrong Status Options + Add `work-order` View Mode

**Files:**
- Modify: `reports/work-in-progress.component.ts`
- Modify: `reports/work-order.component.ts`
- Modify: `reports/work-order.component.html`

**OLD `work-in-progress` status values** (from `supportCtrl`):
```js
[{ id: 1, displayName: 'All' }, { id: 2, displayName: 'On Going' }, { id: 3, displayName: 'Closed Out' }]
```

**OLD `work-order`** (from `supportCtrl`): loads work order detail table via as-of date → shows table with aging data → also exports. Currently NEW is export-only.

- [ ] **Step 1: Read existing files**

```
Read: frontend/src/app/pages/accounting-reports/reports/work-in-progress.component.ts
Read: frontend/src/app/pages/accounting-reports/reports/work-order.component.ts
Read: frontend/src/app/pages/accounting-reports/reports/work-order.component.html
Read: noreco1-firefly/src/main/webapp/WEB-INF/views/reports/partials/work-order.jsp
```

- [ ] **Step 2: Fix `work-in-progress.component.ts` — correct status values**

Change:
```typescript
statusOptions = ['All', 'Open', 'Closed'];
```
To:
```typescript
statusOptions = ['All', 'On Going', 'Closed Out'];
```

- [ ] **Step 3: Add `AccountingReportsService.getWorkOrderAging()` to service**

In `accounting-reports.service.ts`, add:
```typescript
getWorkOrderAging(asOf: string): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_API}/reports/accounting/work-order/${asOf}`);
}
```

> If a different endpoint pattern exists in the v2 backend, use that. Check existing service patterns and backend routes before committing.

- [ ] **Step 4: Update `work-order.component.ts` — add search/view**

```typescript
import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { AccountingReportsService } from '../accounting-reports.service';

@Component({
    selector: 'app-work-order',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './work-order.component.html'
})
export class WorkOrderComponent {
    module   = 'Work Order Aging';
    menuLink = 'accounting-reports';
    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    asOfDate  = '';
    rows      = signal<any[]>([]);
    isLoading = signal(false);

    private downloadSvc  = inject(DownloadService);
    private alertService = inject(AlertService);
    private service      = inject(AccountingReportsService);

    ngOnInit(): void {
        this.asOfDate = new Date().toISOString().substring(0, 10);
    }

    search(): void {
        if (!this.asOfDate) {
            this.alertService.error(this.module, 'Validation', 'Please select an as-of date.');
            return;
        }
        this.isLoading.set(true);
        this.rows.set([]);
        this.service.getWorkOrderAging(this.asOfDate).subscribe({
            next: (data) => { this.rows.set(data ?? []); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); }
        });
    }

    export(type: 'pdf' | 'xls'): void {
        if (!this.asOfDate) {
            this.alertService.error(this.module, 'Validation', 'Please select an as-of date.');
            return;
        }
        this.downloadSvc.print(`/reports/export/work-order/${this.asOfDate}`, { type });
    }
}
```

- [ ] **Step 5: Update `work-order.component.html`**

Read `noreco1-firefly/src/main/webapp/WEB-INF/views/reports/partials/work-order.jsp` for exact OLD columns, then replace the template:

```html
<div class="container-fluid">
    <app-page-title [title]="'Accounting Reports'" [subTitle]="module" [menuLink]="menuLink" [useSubtitleAsTitle]="true"/>
</div>
<div class="container-fluid">
    <div class="card">
        <div class="card-body">
            <div class="row g-2 align-items-end mb-3">
                <div class="col-md-auto">
                    <label class="form-label fw-bold mb-1">As-of Date</label>
                    <input type="text" mwlFlatpickr [options]="flatpickrOptions"
                           class="form-control" [(ngModel)]="asOfDate" placeholder="As-of date"/>
                </div>
                <div class="col-md-auto ms-auto">
                    <div class="d-flex gap-2">
                        <button type="button" class="btn btn-primary fw-bold" (click)="search()">
                            <ng-icon name="tablerEye" class="fw-bold me-1"></ng-icon>View Only
                        </button>
                        <button type="button" class="btn btn-success fw-bold" (click)="export('pdf')">
                            <ng-icon name="tablerFileTypePdf" class="fw-bold me-1"></ng-icon>Export PDF
                        </button>
                        <button type="button" class="btn btn-success fw-bold" (click)="export('xls')">
                            <ng-icon name="tablerFileTypeXls" class="fw-bold me-1"></ng-icon>Export Excel
                        </button>
                    </div>
                </div>
            </div>

            <div class="table-responsive mt-2">
                <table class="table table-custom table-centered table-hover w-100 mb-0">
                    <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                        <tr class="text-uppercase fs-xxs">
                            <!-- Columns from OLD work-order.jsp — read the JSP and put exact column headers here -->
                        </tr>
                    </thead>
                    <tbody>
                        @if (isLoading()) {
                            <tr><td [attr.colspan]="6" class="text-center text-muted py-3">
                                <span class="spinner-border spinner-border-sm me-2"></span>Loading...
                            </td></tr>
                        } @else if (rows().length === 0) {
                            <tr><td [attr.colspan]="6" class="text-center text-muted py-3">No records found.</td></tr>
                        } @else {
                            @for (row of rows(); track $index) {
                                <tr>
                                    <!-- Map row fields from OLD work-order.jsp ng-repeat -->
                                </tr>
                            }
                        }
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
```

> **Important:** Before writing the final template, read `noreco1-firefly/src/main/webapp/WEB-INF/views/reports/partials/work-order.jsp` and use its exact `<th>` column names and row field mappings. Replace the placeholder comments above with real columns.

- [ ] **Step 6: Build check**

```bash
cd frontend && npx ng build --configuration=development 2>&1 | tail -20
```

- [ ] **Step 7: Commit**

```bash
git add frontend/src/app/pages/accounting-reports/reports/work-in-progress.component.ts \
        frontend/src/app/pages/accounting-reports/reports/work-order.component.ts \
        frontend/src/app/pages/accounting-reports/reports/work-order.component.html \
        frontend/src/app/pages/accounting-reports/accounting-reports.service.ts
git commit -m "fix(accounting-reports): correct work-in-progress status options; add work-order view mode"
```

---

## Task 6: Fix `quotation-summary` — Severe Misalignment

**Files:**
- Modify: `reports/quotation-summary.component.ts`
- Modify: `reports/quotation-summary.component.html`

**OLD behavior (from `quotationSummaryCtrl`):**
1. Browse RV using `sl-entity-browser` → sets `selectedRv`
2. On RV selected → load items for that RV (`summaryOfQuotationFactory.getItemsForQuotationSummary(rvId)`)
3. Browse **validator** (employee) + **approvedBy** (employee)
4. Both validator + approvedBy are required for export
5. Export URL: `/reports/export/quotation-summary/{rvId}/{validator.accountNo}/{approvedBy.accountNo}?type=`
6. Table shows items with columns: (read OLD JSP for exact columns)

**Current NEW:** only has a text field for RV ID, hardcodes 0/0 for signatories, no items table. SEVERELY misaligned.

- [ ] **Step 1: Read OLD JSP and current NEW files**

```
Read: noreco1-firefly/src/main/webapp/WEB-INF/views/reports/partials/quotation-summary.jsp
Read: frontend/src/app/pages/accounting-reports/reports/quotation-summary.component.ts
Read: frontend/src/app/pages/accounting-reports/reports/quotation-summary.component.html
```

Identify exact columns from the JSP before proceeding.

- [ ] **Step 2: Add `getQuotationSummaryItems()` to service**

In `accounting-reports.service.ts`:
```typescript
getQuotationSummaryItems(rvId: number): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_API}/reports/accounting/quotation-summary-items/${rvId}`);
}
```

> Verify the correct endpoint path against v2 backend. It may be `GET /api/canvass/items-for-quotation-summary/${rvId}` — check existing service patterns.

- [ ] **Step 3: Rewrite `quotation-summary.component.ts`**

```typescript
import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { AccountingReportsService } from '../accounting-reports.service';

@Component({
    selector: 'app-quotation-summary',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './quotation-summary.component.html'
})
export class QuotationSummaryComponent {
    module   = 'Summary of Quotations';
    menuLink = 'accounting-reports';

    selectedRv:  any = null;
    validator:   any = null;
    approvedBy:  any = null;

    items     = signal<any[]>([]);
    isLoading = signal(false);

    validatorError   = '';
    approvedByError  = '';

    private downloadSvc  = inject(DownloadService);
    private alertService = inject(AlertService);
    private modalService = inject(NgbModal);
    private service      = inject(AccountingReportsService);

    browseRv(): void {
        const ref = this.modalService.open(BrowseEntityModalComponent, { size: 'xl', centered: true });
        ref.componentInstance.types = ['ENTITY_RV'];
        ref.result.then((result) => {
            if (result?.action === 'select') {
                this.selectedRv = result.data;
                this.loadItems();
            }
        }, () => {});
    }

    browseValidator(): void {
        const ref = this.modalService.open(BrowseEntityModalComponent, { size: 'lg', centered: true });
        ref.componentInstance.types = ['ENTITY_EMPLOYEE'];
        ref.result.then((result) => {
            if (result?.action === 'select') { this.validator = result.data; this.validatorError = ''; }
        }, () => {});
    }

    browseApprovedBy(): void {
        const ref = this.modalService.open(BrowseEntityModalComponent, { size: 'lg', centered: true });
        ref.componentInstance.types = ['ENTITY_EMPLOYEE'];
        ref.result.then((result) => {
            if (result?.action === 'select') { this.approvedBy = result.data; this.approvedByError = ''; }
        }, () => {});
    }

    private loadItems(): void {
        if (!this.selectedRv) return;
        this.isLoading.set(true);
        this.items.set([]);
        this.service.getQuotationSummaryItems(this.selectedRv.id).subscribe({
            next: (data) => { this.items.set(data ?? []); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); }
        });
    }

    export(type: 'pdf' | 'xls'): void {
        this.validatorError  = '';
        this.approvedByError = '';
        let valid = true;
        if (!this.selectedRv) {
            this.alertService.error(this.module, 'Validation', 'Please select a Requisition Voucher.');
            return;
        }
        if (!this.validator)  { this.validatorError  = 'Please select a validating officer.';  valid = false; }
        if (!this.approvedBy) { this.approvedByError = 'Please select an approving officer.';  valid = false; }
        if (!valid) {
            this.alertService.error(this.module, 'Validation', 'Please select all required officers.');
            return;
        }
        this.downloadSvc.print(
            `/reports/export/quotation-summary/${this.selectedRv.id}/${this.validator.accountNo}/${this.approvedBy.accountNo}`,
            { type }
        );
    }
}
```

- [ ] **Step 4: Write `quotation-summary.component.html`**

Read `noreco1-firefly/src/main/webapp/WEB-INF/views/reports/partials/quotation-summary.jsp` for exact item column names before writing. Use Browse input pattern from CLAUDE.md:

```html
<div class="container-fluid">
    <app-page-title [title]="'Accounting Reports'" [subTitle]="module" [menuLink]="menuLink" [useSubtitleAsTitle]="true"/>
</div>
<div class="container-fluid">
    <div class="card">
        <div class="card-body">

            <!-- RV Browse -->
            <div class="row g-2 mb-3">
                <div class="col-md-6">
                    <label class="form-label fw-bold mb-1">Requisition Voucher</label>
                    <div class="input-group">
                        <input type="text" class="form-control" readonly
                               [value]="selectedRv ? selectedRv.code + ' — ' + (selectedRv.purpose ?? '') : ''"
                               placeholder="Browse RV..."/>
                        <button type="button" class="btn btn-success fw-bold" (click)="browseRv()">
                            <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                        </button>
                    </div>
                </div>

                <!-- Validator -->
                <div class="col-md-3">
                    <label class="form-label fw-bold mb-1">Validated By</label>
                    <div class="input-group">
                        <input type="text" class="form-control" readonly
                               [value]="validator?.name ?? ''" placeholder="Browse validator..."/>
                        <button type="button" class="btn btn-success fw-bold" (click)="browseValidator()">
                            <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                        </button>
                    </div>
                    @if (validatorError) { <div class="text-danger small mt-1">{{ validatorError }}</div> }
                </div>

                <!-- Approved By -->
                <div class="col-md-3">
                    <label class="form-label fw-bold mb-1">Approved By</label>
                    <div class="input-group">
                        <input type="text" class="form-control" readonly
                               [value]="approvedBy?.name ?? ''" placeholder="Browse officer..."/>
                        <button type="button" class="btn btn-success fw-bold" (click)="browseApprovedBy()">
                            <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                        </button>
                    </div>
                    @if (approvedByError) { <div class="text-danger small mt-1">{{ approvedByError }}</div> }
                </div>
            </div>

            <!-- Export buttons (only shown when RV selected) -->
            @if (selectedRv) {
                <div class="d-flex gap-2 justify-content-end mb-3">
                    <button type="button" class="btn btn-success fw-bold" (click)="export('pdf')">
                        <ng-icon name="tablerFileTypePdf" class="fw-bold me-1"></ng-icon>Export PDF
                    </button>
                    <button type="button" class="btn btn-success fw-bold" (click)="export('xls')">
                        <ng-icon name="tablerFileTypeXls" class="fw-bold me-1"></ng-icon>Export Excel
                    </button>
                </div>
            }

            <!-- Items table — columns from OLD quotation-summary.jsp -->
            <div class="table-responsive mt-2">
                <table class="table table-custom table-centered table-hover w-100 mb-0">
                    <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                        <tr class="text-uppercase fs-xxs">
                            <!-- Read OLD JSP for exact column headers and use them here -->
                        </tr>
                    </thead>
                    <tbody>
                        @if (isLoading()) {
                            <tr><td [attr.colspan]="6" class="text-center text-muted py-3">
                                <span class="spinner-border spinner-border-sm me-2"></span>Loading...
                            </td></tr>
                        } @else if (!selectedRv) {
                            <tr><td [attr.colspan]="6" class="text-center text-muted py-3">Select a Requisition Voucher to view items.</td></tr>
                        } @else if (items().length === 0) {
                            <tr><td [attr.colspan]="6" class="text-center text-muted py-3">No items found.</td></tr>
                        } @else {
                            @for (item of items(); track $index) {
                                <tr>
                                    <!-- Map fields from OLD JSP ng-repeat -->
                                </tr>
                            }
                        }
                    </tbody>
                </table>
            </div>

        </div>
    </div>
</div>
```

> Read OLD `quotation-summary.jsp` for exact column names and field paths before finalizing the template. Replace placeholder comments with real columns.

- [ ] **Step 5: Build check**

```bash
cd frontend && npx ng build --configuration=development 2>&1 | tail -20
```

- [ ] **Step 6: Commit**

```bash
git add frontend/src/app/pages/accounting-reports/reports/quotation-summary.component.ts \
        frontend/src/app/pages/accounting-reports/reports/quotation-summary.component.html \
        frontend/src/app/pages/accounting-reports/accounting-reports.service.ts
git commit -m "fix(accounting-reports): rewrite quotation-summary with RV browser, items table, and required signatories"
```

---

## Task 7: Fix `pcf-ledger` — Replace Number Input with PCF Dropdown

**Files:**
- Modify: `reports/pcf-ledger.component.ts`
- Modify: `reports/pcf-ledger.component.html`

**OLD behavior:** `pettyCashFundFactory.getListNotPaged()` → dropdown of PCFs; **required** before search/export.

**Current NEW issue:** PCF ID is a raw number input — no dropdown, no required validation.

- [ ] **Step 1: Read current component**

```
Read: frontend/src/app/pages/accounting-reports/reports/pcf-ledger.component.ts
Read: frontend/src/app/pages/accounting-reports/reports/pcf-ledger.component.html
```

- [ ] **Step 2: Update `pcf-ledger.component.ts`**

Add PCF fund list and required validation:

```typescript
// Replace: pcfId = 0;
// With:
pcfId    = 0;
pcfFunds = signal<any[]>([]);

// In ngOnInit, add:
this.service.getPcfList().subscribe({
    next: (data) => {
        const list = data?.content ?? data ?? [];
        this.pcfFunds.set([{ id: 0, description: 'Select Petty Cash Fund' }, ...list]);
    }
});

// Update search() — add PCF required check:
search(): void {
    if (!this.fromDate || !this.toDate) {
        this.alertService.error(this.module, 'Validation', 'Please select a date range.');
        return;
    }
    if (!this.pcfId) {
        this.alertService.error(this.module, 'Validation', 'Please select a Petty Cash Fund.');
        return;
    }
    // ... existing subscribe logic
}

// Update export() — add PCF required check:
export(type: 'pdf' | 'xls'): void {
    if (!this.fromDate || !this.toDate) {
        this.alertService.error(this.module, 'Validation', 'Please select a date range.');
        return;
    }
    if (!this.pcfId) {
        this.alertService.error(this.module, 'Validation', 'Please select a Petty Cash Fund.');
        return;
    }
    // ... existing downloadSvc.print
}
```

- [ ] **Step 3: Update `pcf-ledger.component.html`**

Replace the raw number input for PCF ID with a dropdown:

```html
<!-- Remove: -->
<!-- <input type="number" class="form-control" [(ngModel)]="pcfId" min="0"/> -->

<!-- Add: -->
<div class="col-md-3">
    <label class="form-label fw-bold mb-1">Petty Cash Fund</label>
    <select class="form-select" [(ngModel)]="pcfId">
        @for (f of pcfFunds(); track f.id) {
            <option [ngValue]="f.id">{{ f.description }}</option>
        }
    </select>
</div>
```

- [ ] **Step 4: Build check**

```bash
cd frontend && npx ng build --configuration=development 2>&1 | tail -20
```

- [ ] **Step 5: Commit**

```bash
git add frontend/src/app/pages/accounting-reports/reports/pcf-ledger.component.ts \
        frontend/src/app/pages/accounting-reports/reports/pcf-ledger.component.html
git commit -m "fix(accounting-reports): replace pcf-ledger raw ID input with PCF dropdown and add required validation"
```

---

## Task 8: Fix `bir-form-1601e` — Required `authorizedRep` Validation on Export

**Files:**
- Modify: `reports/bir-form-1601e.component.ts`

**OLD behavior (`form1601ECtrl`):** If `$scope.taxPayer` is undefined → `toastr.warning("Select authorized agent signatory")` → return. Never sends 0.

**Current NEW issue:** `exportForm()` sends `an = this.authorizedRep?.accountNo ?? 0` — silently sends 0 if not selected.

- [ ] **Step 1: Read current file**

```
Read: frontend/src/app/pages/accounting-reports/reports/bir-form-1601e.component.ts
```

- [ ] **Step 2: Update `exportForm()`**

```typescript
exportForm(): void {
    const year  = this.selectedYear();
    const month = this.selectedMonth();
    if (!year || !month) {
        this.alertService.error(this.module, 'Validation', 'Please select year and month.');
        return;
    }
    if (!this.authorizedRep) {
        this.alertService.error(this.module, 'Validation', 'Please select the authorized agent signatory.');
        return;
    }
    this.downloadSvc.print(`/reports/export/form-1601E/${year}/${month}`, { an: this.authorizedRep.accountNo });
}
```

- [ ] **Step 3: Build check + commit**

```bash
cd frontend && npx ng build --configuration=development 2>&1 | tail -20
git add frontend/src/app/pages/accounting-reports/reports/bir-form-1601e.component.ts
git commit -m "fix(accounting-reports): require authorizedRep selection before exporting bir-form-1601e"
```

---

## Task 9: Full Audit — Financial Statements (NEA + BSUP) + Registers

**Files to audit (read OLD + NEW, fix any gaps):**
- `trial-balance-nea.component.ts` + `.html`
- `income-statement-nea.component.ts` + `.html`
- `balance-sheet-nea.component.ts` + `.html`
- `trial-balance-nea-audited.component.ts` + `.html`
- `trial-balance.component.ts` + `.html`
- `transaction-summary.component.ts` + `.html`
- `balance-sheet.component.ts` + `.html`
- `income-statement.component.ts` + `.html`
- All 7 register components (apv, cv, jv, aj, mir, sales, cash)

**OLD JS sections to read:**
- `trialBalanceNEACtrl`, `trialBalanceNEAAuditedCtrl`, `trialBalanceCtrl`, `transactionSummaryCtrl`, `incomeStatementCtrl`, `incomeStatementNEACtrl`, `balanceSheetCtrl`, `balanceSheetNEACtrl`, `registerCtrl` (all cases)

- [ ] **Step 1: Read OLD JS for all these controllers**

```
Read: noreco1-firefly/src/main/webapp/resources/js/app/module/rep-accounting.js (lines 113–544)
```

- [ ] **Step 2: For each report below, read OLD JSP + NEW .ts + .html then apply the audit checklist**

**For each JSP, path is:** `noreco1-firefly/src/main/webapp/WEB-INF/views/reports/partials/<name>.jsp`

| Report | OLD JSP | Known pre-audit issue |
|---|---|---|
| trial-balance-nea | `trial-balance-nea.jsp` | `fsTypeOptions` order: OLD = Unaudited, Audited, Closed — NEW = Audited, Unaudited, Closed (reorder) |
| income-statement-nea | `income-statement-nea.jsp` | Verify fsType, date range, same format() helper |
| balance-sheet-nea | `balance-sheet-nea.jsp` | Verify fsType, as-of date, two-column groupR/groupL split |
| trial-balance-nea-audited | `trial-balance-nea.jsp` (audited variant) | Verify single cut-off date, not date range |
| trial-balance | `trial-balance.jsp` | Verify as-of date, totals extraction (pop last 4 rows) |
| transaction-summary | `transaction-summary.jsp` | Verify date range, meta + data response |
| balance-sheet | `balance-sheet.jsp` | Verify as-of date |
| income-statement | `income-statement.jsp` | Verify from/to dates |
| apv-register | `apv-register.jsp` | Looks aligned — verify columns |
| cv-register | `cv-register.jsp` | Verify — same register pattern |
| jv-register | `jv-register.jsp` | Verify — same register pattern |
| aj-register | `aj-register.jsp` | Verify — same register pattern |
| mir-register | `material-issue-register.jsp` | Verify inventory doc types dropdown |
| sales-register | `sales-register.jsp` | Verify — same register pattern |
| cash-register | `cash-register.jsp` | Verify — same register pattern |

- [ ] **Step 3: Fix `trial-balance-nea.component.ts` — reorder fsTypeOptions**

Change:
```typescript
fsTypeOptions = ['Audited', 'Unaudited', 'Closed'];
```
To (matching OLD `fsTypes()` order):
```typescript
fsTypeOptions = ['Unaudited', 'Audited', 'Closed'];
```

- [ ] **Step 4: Fix balance-sheet-nea if it doesn't split groupL/groupR columns**

Read `balance-sheet-nea.component.ts`. OLD (`balanceSheetNEACtrl`) splits data into `groupR` (column='R') and `groupL` (column='L'), extracts last item as totals, removes it. Verify NEW does this. If not, add:

```typescript
groupL = signal<any[]>([]);
groupR = signal<any[]>([]);
totalL = signal<number>(0);
totalR = signal<number>(0);

// In search() next callback, after setting data:
const left  = (data ?? []).filter(r => r.show && r.column === 'L');
const right = (data ?? []).filter(r => r.show && r.column === 'R');
this.totalL.set(left.length  ? left[left.length-1].amount   : 0);
this.totalR.set(right.length ? right[right.length-1].amount : 0);
left.pop(); right.pop();
this.groupL.set(left);
this.groupR.set(right);
```

- [ ] **Step 5: Fix trial-balance BSUP if it doesn't extract totals from response tail**

OLD (`trialBalanceCtrl`): pops last 4 items from data (grand total, total equity, total liability, total assets) and stores them in `$scope.totals`, reverses. Verify NEW does this.

- [ ] **Step 6: Fix any other gaps found during audit**

For each gap: update the `.ts` and/or `.html` file to match OLD behavior.

- [ ] **Step 7: Build check**

```bash
cd frontend && npx ng build --configuration=development 2>&1 | tail -20
```

- [ ] **Step 8: Commit all fixes from this task**

```bash
git add frontend/src/app/pages/accounting-reports/reports/
git commit -m "fix(accounting-reports): align financial statements and registers with OLD behavior"
```

---

## Task 10: Full Audit — Standard Summaries (RV, PO, JO, JOA, Canvass, PR) + Depreciation + AP Aging

**OLD JS section:** `summaryCtrl` (cases `rv-summary`, `po-summary`, `jo-summary`, `joa-summary`, `canvass-summary`, `pr-summary`, `depreciation-summary`, `depreciation-schedule`), `accountsPayableAgingCtrl`

**OLD pattern for standard summaries:**
- date range + status filter
- export URL: `/reports/export/{exportUrl}/{from}/{to}/{statusId}`
- table with standard columns

**Depreciation-summary:** month + year (integer month, integer year), no date range
**Depreciation-schedule:** year only, no month
**AP aging:** as-of date only

- [ ] **Step 1: Read OLD JSPs and NEW components**

Read the following OLD JSPs:
- `rv-summary.jsp`, `po-summary.jsp`, `jo-summary.jsp`, `joa-summary.jsp`, `canvass-summary.jsp`, `pr-summary.jsp`
- `depreciation-summary.jsp`, `depreciation-schedule.jsp`
- `accounts-payable-aging.jsp`

Read the corresponding NEW `.ts` and `.html` files.

- [ ] **Step 2: Apply audit checklist to each report**

Key things to verify per summary:
- Status filter uses `documentStatuses` from API (not hardcoded list)
- Export URL matches OLD exactly: `/reports/export/{name}/{from}/{to}/{statusId}`
- Table columns match OLD JSP thead exactly
- Totals at bottom match OLD (Total No. of Items, Total Amount, etc.)

For `depreciation-summary`: inputs are Month (select 1–12) + Year (select). API: `/depreciation-summary/{year}/{month}`. Export: `/reports/export/depreciation-summary/{year}/{month}`.

For `depreciation-schedule`: input is Year only. API: `/depreciation-schedule/{year}`. Export: `/reports/export/depreciation-schedule/{year}`.

For `accounts-payable-aging`: input is as-of date. Export URL: `/reports/export/accounts-payable-aging/{cutOffDate}`. Verify `cutOffDate` param matches backend (ISO date vs timestamp — check service).

- [ ] **Step 3: Fix any gaps found**

- [ ] **Step 4: Build check + commit**

```bash
cd frontend && npx ng build --configuration=development 2>&1 | tail -20
git add frontend/src/app/pages/accounting-reports/reports/
git commit -m "fix(accounting-reports): align summaries, depreciation, and AP aging with OLD behavior"
```

---

## Task 11: Full Audit — Pending Voucher List, Check List, Cash Flow Detail, Pending Purchase Requests

**OLD JS sections:** `pendingVoucherListCtrl`, `checkListingCtrl`, `cashFlowDetailCtrl`, `pendingPurchaseRequestCtrl`

- [ ] **Step 1: Read OLD JSPs and NEW components**

Read:
- `pending-voucher-list.jsp` + `pending-voucher-list.component.ts` + `.html`
- `check-list.jsp` + `check-list.component.ts` + `.html`
- `cash-flow-detail.jsp` + `cash-flow-detail.component.ts` + `.html`
- `pending-purchase-request.jsp` + `pending-purchase-request.component.ts` + `.html`

- [ ] **Step 2: Audit `pending-voucher-list`**

**OLD inputs:** docType dropdown (hardcoded list of 11 types), status dropdown, no date range.
**OLD API:** `pendingVoucherListFactory.getData(docTypeId, tableName, pt, statusId)`
**OLD export URL:** `/reports/export/pending-voucher-list/tn/{tableName}/pt/{pt}?docTypeId=&statusId=&docType=&status=&type=`

**docTypes list (from OLD — must be exact):**
```typescript
docTypes = [
    { desc: 'APV',    id: 4,  tableName: 'AccountsPayableVoucher',   pt: 'particulars' },
    { desc: 'Canvass',id: 20, tableName: 'Canvass',                  pt: 'FK_vendorAccountNo' },
    { desc: 'CRV',    id: 9,  tableName: 'CashReceipts',             pt: 'particulars' },
    { desc: 'CV',     id: 5,  tableName: 'CheckVoucher',             pt: 'particulars' },
    { desc: 'JOA',    id: 21, tableName: 'JoAcceptance',             pt: 'FK_vendorAccountNo' },
    { desc: 'JO',     id: 10, tableName: 'JobOrder',                 pt: 'FK_vendorAccountNo' },
    { desc: 'JV',     id: 6,  tableName: 'JournalVoucher',           pt: 'explanation' },
    { desc: 'MIR',    id: 19, tableName: 'MaterialIssueRegister',    pt: 'particulars' },
    { desc: 'PO',     id: 2,  tableName: 'PurchaseOrder',            pt: 'FK_vendorAccountNo' },
    { desc: 'RV',     id: 1,  tableName: 'RequisitionVoucher',       pt: 'purpose' },
    { desc: 'SV',     id: 13, tableName: 'SalesVoucher',             pt: 'particulars' },
];
```

Verify the NEW component has this exact list and passes `tableName` + `pt` in the API/export call. If not, fix it.

- [ ] **Step 3: Audit `check-list`**

OLD: date range → `/reports/export/check-list?from=&to=&type=`. Verify export URL uses query params (not path params).

- [ ] **Step 4: Audit `cash-flow-detail`**

OLD: date range → view table (account + amounts, skip sub-totals in totalAmount calc: `if(details[i].account.indexOf('Sub total') == -1)`).
Verify NEW replicates the sub-total exclusion logic in its total computation.

- [ ] **Step 5: Audit `pending-purchase-request`**

OLD: date range + status dropdown (from `requisitionVoucherFactory.documentStatuses()`). Export URL: `/reports/export/pending-purchase-requests?from=&to=&status=&type=`. Verify query param format.

- [ ] **Step 6: Fix all gaps, build check + commit**

```bash
cd frontend && npx ng build --configuration=development 2>&1 | tail -20
git add frontend/src/app/pages/accounting-reports/reports/
git commit -m "fix(accounting-reports): align pending-voucher-list, check-list, cash-flow-detail, pending-purchase-requests"
```

---

## Task 12: Full Audit — Support Reports + Unliquidated CA

**Reports:** `work-order-transaction`, `unliquidated-ca`

**OLD JS sections:** `supportCtrl` cases `"work-order-transaction"`, `"unliquidated-ca"`

**OLD `work-order-transaction`:** date range → table → export `/reports/export/work-order-transaction/{from}/{to}`
**OLD `unliquidated-ca`:** status dropdown (All / Overdue / Not Overdue) → table → export `/reports/export/unliquidated-ca/{statusDescription}`

- [ ] **Step 1: Read OLD JSPs and current NEW components**

```
Read: noreco1-firefly/src/main/webapp/WEB-INF/views/reports/partials/work-order-transaction.jsp
Read: noreco1-firefly/src/main/webapp/WEB-INF/views/reports/partials/unliquidated-ca.jsp
Read: frontend/src/app/pages/accounting-reports/reports/work-order-transaction.component.ts
Read: frontend/src/app/pages/accounting-reports/reports/work-order-transaction.component.html
Read: frontend/src/app/pages/accounting-reports/reports/unliquidated-ca.component.ts
Read: frontend/src/app/pages/accounting-reports/reports/unliquidated-ca.component.html
```

- [ ] **Step 2: Verify `unliquidated-ca` status options**

OLD status list:
```typescript
statusOptions = ['All', 'Overdue', 'Not Overdue'];
```

Verify NEW uses exactly these strings (they become the API parameter).

- [ ] **Step 3: Verify export URL formats**

`work-order-transaction`: `/reports/export/work-order-transaction/{from}/{to}?type=`
`unliquidated-ca`: `/reports/export/unliquidated-ca/{statusDescription}?type=` (no date in URL)

- [ ] **Step 4: Fix gaps, build check + commit**

```bash
cd frontend && npx ng build --configuration=development 2>&1 | tail -20
git add frontend/src/app/pages/accounting-reports/reports/work-order-transaction.component.ts \
        frontend/src/app/pages/accounting-reports/reports/work-order-transaction.component.html \
        frontend/src/app/pages/accounting-reports/reports/unliquidated-ca.component.ts \
        frontend/src/app/pages/accounting-reports/reports/unliquidated-ca.component.html
git commit -m "fix(accounting-reports): align work-order-transaction and unliquidated-ca with OLD"
```

---

## Task 13: Full Audit — BIR Alphalist + Asset Management

**Reports:** `bir-alphalist`, `maintenance-record-summary`, `asset-ledger`, `asset-monitoring-sheet`

**OLD JS sections:** `summaryCtrl` case `"bir-alphalist"`, `assetManagementCtrl` cases `"asset-ledger"` + `"maintenance-record-summary"`, `assetMonitoringSheetCtrl`

- [ ] **Step 1: Read OLD JSPs + NEW components**

```
Read: noreco1-firefly/src/main/webapp/WEB-INF/views/reports/partials/bir-alphalist.jsp
Read: noreco1-firefly/src/main/webapp/WEB-INF/views/reports/partials/asset-ledger.jsp
Read: noreco1-firefly/src/main/webapp/WEB-INF/views/reports/partials/asset-monitoring-sheet.jsp
Read: noreco1-firefly/src/main/webapp/WEB-INF/views/reports/partials/maintenance-record-summary.jsp
Read: frontend/src/app/pages/accounting-reports/reports/bir-alphalist.component.ts + .html
Read: frontend/src/app/pages/accounting-reports/reports/asset-ledger.component.ts + .html
Read: frontend/src/app/pages/accounting-reports/reports/asset-monitoring-sheet.component.ts + .html
Read: frontend/src/app/pages/accounting-reports/reports/maintenance-record-summary.component.ts + .html
```

- [ ] **Step 2: Audit `bir-alphalist`**

OLD: month (name) + year → `cvFactory.getCheckVoucherIncomePayment(year, intMonth)`. Export: `/reports/export/bir-alphalist/{year}/{intMonth}?type=`. Verify NEW uses integer month (not name string) in the URL.

- [ ] **Step 3: Audit `asset-ledger`**

OLD: date range + account browser + asset browser + `assetVoucherLinkType` dropdown (3 options: All/Major Repair/Minor Repair). Export: `/reports/export/asset-ledger/{from}/{to}/{accountId}/{assetAccountNo}/{linkTypeId}?type=&q=`. Verify all 3 filters are present.

**linkType options (exact strings from OLD):**
```typescript
linkTypeOptions = [
    { id: 0, displayName: 'All (Total Cost of Ownership)' },
    { id: 2, displayName: 'Major Repair (Capitalize)' },
    { id: 3, displayName: 'Minor Repair or Maintenance (Expense)' },
];
```

- [ ] **Step 4: Audit `asset-monitoring-sheet`**

OLD: browse maintenance record (entity browser) → load record detail → export. Verify maintenance record browser is present.

- [ ] **Step 5: Audit `maintenance-record-summary`**

OLD: date range + optional query string `q` → data table → export with `q=` param.

- [ ] **Step 6: Fix all gaps, build check + commit**

```bash
cd frontend && npx ng build --configuration=development 2>&1 | tail -20
git add frontend/src/app/pages/accounting-reports/reports/
git commit -m "fix(accounting-reports): align BIR reports and asset management reports with OLD"
```

---

## Task 14: Full Audit — PCV Summary

**Files:**
- Modify: `reports/pcv-summary.component.ts`
- Modify: `reports/pcv-summary.component.html`

**OLD behavior (`pcvSummaryCtrl`):**
- Date range defaults to **first of PREVIOUS month** to **last of current month** (not current month)
- Office dropdown → selecting office loads **batches** for that office + date range
- Batch is **required** before search and before export
- `documentStatuses` (All + standard statuses)
- Checker + ReplenishedBy browse (employees) — required for export
- Paged table of PCVs after search
- Export URL: `/petty-cash-voucher/print/{checkerAcctNo}/{replenishedByAcctNo}?batch=&from=&to=&documentStatusId=&officeId=&token=&type=`

- [ ] **Step 1: Read OLD JSP + current NEW**

```
Read: noreco1-firefly/src/main/webapp/WEB-INF/views/reports/partials/pcv-summary.jsp
Read: frontend/src/app/pages/accounting-reports/reports/pcv-summary.component.ts
Read: frontend/src/app/pages/accounting-reports/reports/pcv-summary.component.html
```

- [ ] **Step 2: Fix default dates** — `fromDate` should be first of **previous** month

```typescript
setDefaultDates(): void {
    const now  = new Date();
    const prev = new Date(now.getFullYear(), now.getMonth() - 1, 1); // first of previous month
    this.fromDate = prev.toISOString().substring(0, 10);
    this.toDate   = now.toISOString().substring(0, 10);
}
```

- [ ] **Step 3: Add batch loading on office change**

In `pcv-summary.component.ts`:

```typescript
batches      = signal<any[]>([]);
selectedBatchId = 0;

onOfficeChange(): void {
    this.selectedBatchId = 0;
    this.pcfId = 0;
    if (this.officeId) {
        this.service.getPcvBatches(this.fromDate, this.toDate, this.officeId).subscribe({
            next: (data) => {
                this.batches.set(data ?? []);
                if (data?.length) this.selectedBatchId = data[data.length - 1].id; // pre-select last batch
            }
        });
    } else {
        this.batches.set([]);
    }
}
```

Add service method in `accounting-reports.service.ts`:
```typescript
getPcvBatches(from: string, to: string, officeId: number): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_API}/pcv/batches`, {
        params: new HttpParams().set('from', from).set('to', to).set('officeId', officeId)
    });
}
```

> Verify correct batch endpoint path against v2 backend.

- [ ] **Step 4: Add batch required validation to `export()`**

```typescript
export(type: 'pdf' | 'xls'): void {
    if (!this.selectedBatchId) {
        this.alertService.error(this.module, 'Validation', 'Please select a batch.');
        return;
    }
    if (!this.checkedBy) {
        this.alertService.error(this.module, 'Validation', 'Please select the checked-by officer.');
        return;
    }
    if (!this.replenishedBy) {
        this.alertService.error(this.module, 'Validation', 'Please select the replenished-by officer.');
        return;
    }
    this.downloadSvc.print(
        `/petty-cash-voucher/print/${this.checkedBy.accountNo}/${this.replenishedBy.accountNo}`,
        {
            batch:            this.selectedBatchId,
            from:             this.fromDate,
            to:               this.toDate,
            documentStatusId: this.docStatId,
            officeId:         this.officeId,
            type
        }
    );
}
```

- [ ] **Step 5: Update template to show batch dropdown when office is selected**

Add after the office select in the HTML:
```html
@if (officeId && batches().length) {
    <div class="col-md-3">
        <label class="form-label fw-bold mb-1">Batch</label>
        <select class="form-select" [(ngModel)]="selectedBatchId">
            @for (b of batches(); track b.id) {
                <option [ngValue]="b.id">{{ b.description ?? b.id }}</option>
            }
        </select>
    </div>
}
```

- [ ] **Step 6: Build check + commit**

```bash
cd frontend && npx ng build --configuration=development 2>&1 | tail -20
git add frontend/src/app/pages/accounting-reports/reports/pcv-summary.component.ts \
        frontend/src/app/pages/accounting-reports/reports/pcv-summary.component.html \
        frontend/src/app/pages/accounting-reports/accounting-reports.service.ts
git commit -m "fix(accounting-reports): align pcv-summary with OLD batch/office/signatory requirements"
```

---

## Task 15: Final Accounting Reports Build Validation

- [ ] **Step 1: Full production build**

```bash
cd frontend && npx ng build --configuration=production 2>&1 | tail -30
```

Expected: no errors.

- [ ] **Step 2: Lint check**

```bash
cd frontend && npx ng lint 2>&1 | tail -30
```

Fix any lint errors reported.

- [ ] **Step 3: Final commit if any lint fixes**

```bash
git add frontend/src/app/pages/accounting-reports/
git commit -m "fix(accounting-reports): lint fixes post-alignment"
```
