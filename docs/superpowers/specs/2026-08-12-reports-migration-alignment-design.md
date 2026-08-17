# Reports Migration Alignment — Design Spec
**Date:** 2026-08-12
**Approach:** Parallel subagent execution (Approach B)

---

## 1. Scope

### Paths modified
- `frontend/src/app/pages/accounting-reports/**`
- `frontend/src/app/pages/inventory-reports/**`

### Source of truth
- OLD Firefly: `noreco1-firefly/src/main/webapp/WEB-INF/views/reports/`
- OLD JS: `noreco1-firefly/src/main/webapp/resources/js/app/module/rep-accounting.js`
- OLD JS: `noreco1-firefly/src/main/webapp/resources/js/app/module/rep-inventory.js`

---

## 1b. Expanded Scope (User-Requested)

In addition to the known gaps above, each implementation track must perform a **full report-by-report audit** of ALL existing NEW components against their OLD counterparts before fixing. Do not assume any existing NEW component is correct. For every report:

1. Read the OLD JSP partial + relevant OLD JS controller logic
2. Read the NEW component (`.ts` + `.html`) + service method
3. Compare: inputs, filters, defaults, validations, parameters, API URLs, export URLs, table columns, totals, print/export behavior
4. Fix any misalignment found

---

## 2. Issues Found (Forensic Audit)

### 2A — Accounting Reports

#### Issue 1 — `pe-summary` completely missing
- **Prepayment Expense Summary** has no component, no route, not in the main menu.
- OLD inputs: **Month** (month-name picker), **Year** (year picker)
- OLD API (view): `prepaymentFactory.getListForSummary(intMonth, year)`
- OLD export URL: `/reports/export/pe-summary/${intMonth}/${year}/${month}?token=&type=`
- OLD columns: Reference, Description, No Of Months, Amount, Balance
- OLD totals footer: Total No. of Vouchers, Total Amount, Total Balance
- NEW must add: component, service method, route, and entry in main menu under **Summaries**

#### Issue 2 — Category misclassification in `accounting-reports-main`
OLD Support Module Reports includes:
- Summary of Construction Work In Progress (`work-in-progress`)
- Aging of Work Order (`work-order`)
- Work Order Transaction Summary (`work-order-transaction`)
- Petty Cash Fund Ledger (`pcf-ledger`)
- Unliquidated Cash Advance (`unliquidated-ca`)

NEW incorrectly places `work-in-progress` and `work-order` under **Summaries** instead of **Support Module Reports**.

Fix: move both to the Support Module Reports category in `accounting-reports-main.component.ts`.

Also: add `pe-summary` to Summaries.

#### Issue 3 — `cashflow-statement-nea` and `cashflow-statement-bsup` missing validations
**Missing: same-year date range validation**
OLD: `if (yearFrom != yearTo) { warn "Date range must be of the same year"; return; }`
Must validate before both search() and export().

**Missing: required checkedBy / notedBy validation on export**
OLD: if checkedBy or notedBy is undefined → show field error + `toastr.warning('Error Found!')` → return.
NEW sends `0` as default, which is wrong — must block export if not selected.

---

### 2B — Inventory Reports

#### Issue 4 — 7 inventory summary reports are export-only (missing search/view/table)

Reports affected:
| Component | Missing Location filter | Missing DocType filter | Missing search/table | Missing expandable items |
|---|---|---|---|---|
| `adjustment-summary` | YES | no | YES | YES |
| `withdrawal-summary` | YES | YES | YES | YES |
| `release-summary` | YES | YES | YES | YES |
| `mcrt-summary` | YES | no | YES | YES |
| `mst-summary` | YES | no | YES | YES |
| `transfer-summary` | YES | no | YES | YES |
| `receive-summary` | YES | no | YES | YES |

**What each needs (behavioral parity with OLD):**

**Filters to add (all 7):**
- From / To date range (already exists)
- Document Status dropdown (already exists)
- Inventory Location dropdown (ALL must have this — currently missing)
- Document Type dropdown (withdrawal-summary and release-summary only)

**Search / View:**
- "View" button that calls `search()` to load data into a table
- Table columns per report type (see OLD JSP for exact columns)
- Loading state
- Empty state

**Expandable items (all 7 — modernized):**
- Each transaction row has a toggle button (chevron or `tablerChevronDown` icon)
- Click toggles an inline sub-table with that transaction's items
- Items are lazy-loaded on first expand (same as OLD), cached on repeat expand
- Angular signal-based expand state per row
- Service call per-row to fetch items when expanded for the first time

**Totals:**
- `withdrawal-summary`: Number of Stock Withdrawals count
- `release-summary`: as per OLD JSP
- Others: Total Cost summation across all visible items

**Export URL patterns (from OLD):**
- `withdrawal-summary`: `/reports/export/withdrawal-summary/{from}/{to}/{statusId}?docType=...&location=...`
- `release-summary`: `/reports/export/release-summary/{from}/{to}/{statusId}?docType=...&location=...`
- `mcrt-summary`: `/reports/export/mcrt-summary/{from}/{to}/{statusId}?location=...`
- `mst-summary`: `/reports/export/mst-summary/{from}/{to}/{statusId}?location=...`
- `adjustment-summary`: `/reports/export/adjustment-summary/{from}/{to}/{statusId}?location=...`
- `transfer-summary`: `/reports/export/transfer-summary/{from}/{to}/{statusId}?location=...`
- `receive-summary`: `/reports/export/receive-summary/{from}/{to}/{statusId}?location=...`

#### Issue 5 — `inventory-reports.service.ts` missing data-fetch methods
Must add service methods for each summary (view/search):
- `getMcrtSummary(from, to, locationId, statusId)`
- `getMstSummary(from, to, locationId, statusId)`
- `getWithdrawalSummary(from, to, docTypeId, locationId, statusId)`
- `getReleaseSummary(from, to, docTypeId, locationId, statusId)`
- `getAdjustmentSummary(from, to, locationId, statusId)`
- `getTransferSummary(from, to, locationId, statusId)`
- `getReceiveSummary(from, to, locationId, statusId)`
- Per-row item fetch methods for each (for expandable detail):
  - `getMcrtItems(transactionId)`
  - `getMstItems(transactionId)`
  - `getWithdrawalItems(id)`
  - `getReleaseItems(documentTransactionId)`
  - (adjustment/transfer/receive: items are inline in main response per OLD — no lazy load needed)

---

## 3. Implementation Plan — Two Parallel Tracks

### Track 1: Accounting Agent

**Files to modify:**
- `accounting-reports-main/accounting-reports-main.component.ts` — fix categories (move work-in-progress + work-order to Support; add pe-summary to Summaries)
- `accounting-reports.route.ts` — add `pe-summary` route
- `accounting-reports.service.ts` — add `getPeSummary(month: number, year: string): Observable<any[]>`
- `reports/cashflow-statement-nea.component.ts` — add same-year validation + checkedBy/notedBy required on export
- `reports/cashflow-statement-bsup.component.ts` — same as nea
- `reports/cashflow-statement-nea.component.html` — show field errors for checker/notedBy
- `reports/cashflow-statement-bsup.component.html` — same

**Files to create:**
- `reports/pe-summary.component.ts`
- `reports/pe-summary.component.html`

**pe-summary component design:**
- Inputs: Month (flatpickr month/year or separate select dropdowns per OLD), Year
- Per OLD: month is displayed as name (e.g., "August"), intMonth is integer
- Use two flatpickr or two native selects — simpler: month select (Jan–Dec) + year input
- `search()`: calls `service.getPeSummary(intMonth, year)` → shows table
- `export(type)`: `/reports/export/pe-summary/{intMonth}/{year}/{monthName}?type=...`
- Columns: Reference | Description | No Of Months | Amount | Balance
- Totals: Total No. of Vouchers | Total Amount | Total Balance
- V2 button standards: View = `btn-primary`, Export PDF/Excel = `btn-success`

**cashflow validations:**
- Add `sameYearCheck()`: extract year from fromDate and toDate strings; if different, `alertService.error()` + return false
- Call before `search()` and `export()`
- Add `checkedByRequired` and `notedByRequired` errors shown under browse inputs
- Validate on `export()` before proceeding

---

### Track 2: Inventory Agent

**Files to modify:**
- `inventory-reports.service.ts` — add all missing summary + item-fetch methods

**Files to modify (7 summary components — .ts and .html):**
- `adjustment-summary/adjustment-summary.component.ts`
- `adjustment-summary/adjustment-summary.component.html`
- `withdrawal-summary/withdrawal-summary.component.ts`
- `withdrawal-summary/withdrawal-summary.component.html`
- `release-summary/release-summary.component.ts`
- `release-summary/release-summary.component.html`
- `mcrt-summary/mcrt-summary.component.ts`
- `mcrt-summary/mcrt-summary.component.html`
- `mst-summary/mst-summary.component.ts`
- `mst-summary/mst-summary.component.html`
- `transfer-summary/transfer-summary.component.ts`
- `transfer-summary/transfer-summary.component.html`
- `receive-summary/receive-summary.component.ts`
- `receive-summary/receive-summary.component.html`

**Each summary component pattern (modernized):**

```typescript
// State
rows = signal<any[]>([]);
isLoading = signal(false);
locations = signal<any[]>([]);
statuses = signal<any[]>([]);
locationId = 0;
statusId = 0;
// withdrawal/release only:
docTypeId = 0;
docTypes: { id: number; desc: string }[] = RELEASING_DOC_TYPES;

// Expand state — per-row signal map
expandedRows = new Map<number, boolean>();
rowItems = new Map<number, any[]>();
loadingRowItems = new Set<number>();

// Methods
search(): void — load rows via service
toggleRow(index: number, row: any): void — expand/collapse; lazy-load items on first expand
export(type): void
```

```html
<!-- Filter bar -->
<div class="row g-2 align-items-end mb-3">
  <!-- From, To, Status, [DocType], Location, View button, Export buttons -->
</div>

<!-- Table -->
<table class="table table-custom table-centered table-hover w-100 mb-0">
  <thead>...</thead>
  <tbody>
    @for (row of rows(); track $index) {
      <tr (click)="toggleRow($index, row)" style="cursor:pointer">
        <!-- columns + expand chevron icon -->
      </tr>
      @if (expandedRows.get($index)) {
        <tr>
          <td [attr.colspan]="N" class="p-0 bg-light">
            <!-- nested table: items sub-table -->
          </td>
        </tr>
      }
    }
    <!-- totals row -->
  </tbody>
</table>
```

**Expandable items — modernized pattern:**
- Row toggle: click anywhere on transaction row → expand/collapse
- Indicator: `tablerChevronRight` rotates to `tablerChevronDown` when expanded
- Items load lazily on first expand, cached in `rowItems` Map
- Loading spinner inside the expanded cell while items load
- Items display as a nested table (same columns as OLD)

**Document Type values (withdrawal-summary, release-summary):**
Per OLD `RELEASING_INV_CAT_TYPE` constant:
- All (id: 0)
- And whatever enum values the OLD defines (need to check the constants file for exact values)

**API endpoints for service methods (per OLD controller):**
- `getMcrtSummary`: `GET /mct/summary?from=&to=&locationId=&statusId=`
- `getMstSummary`: `GET /mst/summary?from=&to=&locationId=&statusId=`
- `getWithdrawalSummary`: `GET /withdrawal/summary?from=&to=&docTypeId=&locationId=&statusId=`
- `getReleaseSummary`: `GET /stock-release/summary?from=&to=&docTypeId=&locationId=&statusId=`
- `getAdjustmentSummary`: `GET /stock-adjustment/summary?from=&to=&locationId=&statusId=`
- `getTransferSummary`: `GET /stock-transfer/summary?from=&to=&locationId=&statusId=`
- `getReceiveSummary`: `GET /stock-receive/summary?from=&to=&locationId=&statusId=`
- Item fetches: follow OLD factory patterns (e.g., `mctFactory.getItems(id)` → `GET /mct/{id}/items`)

> Note: The exact backend URL paths must be verified against the v2 Spring Boot API. The agent must read existing service files and related v2 backend patterns to confirm correct endpoints before writing service calls.

---

## 4. Frontend Standards (from CLAUDE.md)

- Buttons: View → `btn-primary fw-bold`, Export → `btn-success fw-bold`
- Tables: `table table-custom table-centered table-hover w-100 mb-0`
- thead: `bg-light align-middle bg-opacity-25 thead-sm`, tr: `text-uppercase fs-xxs`
- Date inputs: `mwlFlatpickr` with `[options]="flatpickrOptions"`
- Alerts: `AlertService` (SweetAlert2 wrapper) — no native alert()
- Icons: `ng-icon` with tabler icons
- Empty state: single `<td colspan="N">` centered muted text
- Loading state: spinner-border-sm in empty colspan row
- Dropdowns: `form-select`

---

## 5. Execution Strategy

### Parallel execution
Both tracks run simultaneously as separate subagents. They touch separate files with no overlap.

### Track 1 (Accounting) task order:
1. Fix `accounting-reports-main.component.ts` — categories
2. Add `pe-summary` route to `accounting-reports.route.ts`
3. Add `getPeSummary()` to `accounting-reports.service.ts`
4. Create `pe-summary.component.ts` + `.html`
5. Fix `cashflow-statement-nea` validations (ts + html)
6. Fix `cashflow-statement-bsup` validations (ts + html)

### Track 2 (Inventory) task order:
1. Verify backend endpoint patterns by checking existing inventory service + backend routes
2. Add all missing service methods to `inventory-reports.service.ts`
3. Check OLD constants file for RELEASING_INV_CAT_TYPE values
4. Fix `withdrawal-summary` (ts + html) — full pattern with docType + location + search + expandable
5. Fix `release-summary` (ts + html) — same pattern
6. Fix `mcrt-summary` (ts + html) — location + search + expandable (lazy items)
7. Fix `mst-summary` (ts + html) — same
8. Fix `adjustment-summary` (ts + html) — location + search (items inline, no lazy)
9. Fix `transfer-summary` (ts + html) — location + search (items inline, no lazy)
10. Fix `receive-summary` (ts + html) — location + search (items inline, no lazy)

### After both tracks complete:
- Final parity review: re-read OLD JSPs vs NEW implementations
- TypeScript/lint check

---

## 6. Out of Scope
- Backend changes (assume backend already supports all required endpoints)
- Changes to non-report modules
- Any OLD report not listed above (already aligned)
- Changes to OLD Firefly
