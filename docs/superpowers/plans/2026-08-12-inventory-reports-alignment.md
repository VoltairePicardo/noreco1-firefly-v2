# Inventory Reports Alignment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bring all 7 export-only inventory summary components to full behavioral parity with OLD Firefly — adding location/docType filters, search/view mode with expandable items, and correct totals — while confirming existing 8 inventory reports are already aligned.

**Architecture:** Angular 19 standalone components using signals + `Map<number, boolean>` per-row expand state. Lazy-load items for mcrt/mst/withdrawal/release; inline `row.details` for adjustment/transfer/receive. Service layer calls v2 Spring Boot REST endpoints.

**Tech Stack:** Angular 19, TypeScript signals, `angularx-flatpickr`, `ng-icon` (tabler icons), `DownloadService`, `AlertService` (SweetAlert2 wrapper), `InventoryReportsService`

## Global Constraints

- Buttons: View → `btn-primary fw-bold`, Export PDF/Excel → `btn-success fw-bold`
- Tables: `table table-custom table-centered table-hover w-100 mb-0`
- thead: `bg-light align-middle bg-opacity-25 thead-sm`, tr: `text-uppercase fs-xxs`
- Date inputs: `mwlFlatpickr` with `[options]="flatpickrOptions"` + `provideFlatpickrDefaults()`
- Alerts: `AlertService` only — no native `alert()`/`confirm()`
- Icons: `ng-icon` with tabler icons (`tablerChevronRight`, `tablerChevronDown`, etc.)
- Empty/loading state: single `<td colspan="N">` with centered muted text
- Status field on response: `row.documentStatus?.status`

## Reference: OLD `RELEASING_INV_CAT_TYPE` Constants

```typescript
const RELEASING_DOC_TYPES = [
    { id: 0, desc: 'All' },
    { id: 1, desc: 'Material Charge Ticket' },
    { id: 2, desc: 'Office Furniture and Equipment Acquired or Issued' },
    { id: 3, desc: 'Office Supplies or Spare Parts Issuance Slip' },
    { id: 4, desc: 'Stock Transfer Release' }
];
```

## Reference: Per-Report Table Specs (from OLD JSPs)

| Report | Main Columns | Items Sub-table | Items Source | Footer |
|---|---|---|---|---|
| mcrt-summary | Date, MCRT No., Description, Doc Status | Item Id, Item Code, Qty, Unit, Cost, Total | Lazy: `getMcrtItems(row.transaction.id)` | Number of MCRT: {count} |
| mst-summary | Date, MST No., Description, Doc Status | Item Id, Item Code, Qty, Unit, Cost, Total | Lazy: `getMstItems(row.transaction.id)` | Number of MST: {count} |
| withdrawal-summary | Date, Withdrawal No., Description, Doc Status | Item Id, Item Code, Qty, Unit, Qty Released | Lazy: `getWithdrawalItems(row.id)` | Number of Stock Withdrawals: {count} |
| release-summary | Date, Stock Release No., Description, Doc Status | Item Id, Item Code, Qty, Unit, Qty Released | Lazy: `getReleaseItems(row.documentTransaction.id)` | Number of Stock Releases: {count} |
| adjustment-summary | Date, Adjustment No., Description, Doc Status | Item Id, Item Code, Qty, Unit, Cost, Total | Inline: `row.details` | Number of Stock Adjustments + Total Cost |
| transfer-summary | Date, Transfer No., Description, Doc Status | Item Id, Item Code, Qty, Unit, Cost, Total | Inline: `row.details` | Number of Stock Transfers + Total Cost |
| receive-summary | Date, Receive No., Description, Doc Status | Item Id, Item Code, Qty, Unit, Cost, Total | Inline: `row.details` | Number of Stock Receives + Total Cost |

---

### Task 1: Verify Backend Endpoints + Add Service Methods

**Files:**
- Modify: `frontend/src/app/pages/inventory-reports/inventory-reports.service.ts`

**Context:** v2 backend may not yet expose `/summary` or `/items` endpoints for inventory modules. This task reads the relevant backend controllers before writing any service code, then adds all missing methods using the best available endpoints.

- [ ] **Step 1: Check backend endpoints for summary data**

Read these files and note which GET endpoints are available:
```bash
grep -n "GetMapping\|RequestMapping\|GetMapping" \
  src/main/java/com/noreco1/fireflyv2/controller/ReportsController.java | grep -i "mcrt\|mst\|withdraw\|release\|adjust\|transfer\|receive"
```
Also check individual module controllers for `/summary` or `/items` variants:
```bash
grep -rn "summary\|/items" \
  src/main/java/com/noreco1/fireflyv2/controller/StockWithdrawalController.java \
  src/main/java/com/noreco1/fireflyv2/controller/StockAdjustmentController.java \
  src/main/java/com/noreco1/fireflyv2/controller/StockReleaseController.java \
  src/main/java/com/noreco1/fireflyv2/controller/StockTransferController.java \
  src/main/java/com/noreco1/fireflyv2/controller/StockReceiveController.java
```

**Expected:** The v2 backend does NOT have `/summary` data GET endpoints. The list endpoints exist at `/api/{module}/list/{from}/{to}/{statusId}` (confirmed during plan writing). The `locationId` and `docTypeId` filtering must be passed as query params — the backend may ignore them if not yet supported (client-side display will still work; filtering will be server-side once backend adds support).

For items (lazy load): use the detail endpoint `GET /api/{module}/{id}` and extract the items array from the response. Confirmed endpoints:
- `GET /api/mct/{id}` → response includes items in `.items` or `.details` field
- `GET /api/withdrawal/{id}` → `StockWithdrawal` entity, check for items field
- etc.

- [ ] **Step 2: Check item field names on detail responses**

Read the detail component HTML for mcrt and withdrawal to find which field holds items:
```
frontend/src/app/pages/mct/mct-detail/mct-detail.component.html  (search for 'item' or 'details')
frontend/src/app/pages/withdrawal (if detail exists, check item field)
```

Use the field name found (e.g., `data.items`, `data.details`, `data.stockWithdrawalDetails`) in the service item-fetch methods.

- [ ] **Step 3: Add all missing service methods to `inventory-reports.service.ts`**

Open `frontend/src/app/pages/inventory-reports/inventory-reports.service.ts` and append the following methods (adapt field paths found in Step 2):

```typescript
// ─── Inventory Summary — data endpoints ─────────────────────────────────────

getMcrtSummary(from: string, to: string, locationId: number, statusId: number): Observable<any[]> {
    const params = new HttpParams()
        .set('from', from).set('to', to)
        .set('locationId', locationId).set('statusId', statusId);
    return this.http.get<any[]>(`${BASE_API}/reports/inventory/mcrt-summary-list`, { params });
}

getMstSummary(from: string, to: string, locationId: number, statusId: number): Observable<any[]> {
    const params = new HttpParams()
        .set('from', from).set('to', to)
        .set('locationId', locationId).set('statusId', statusId);
    return this.http.get<any[]>(`${BASE_API}/reports/inventory/mst-summary-list`, { params });
}

getWithdrawalSummary(from: string, to: string, docTypeId: number, locationId: number, statusId: number): Observable<any[]> {
    const params = new HttpParams()
        .set('from', from).set('to', to)
        .set('docTypeId', docTypeId).set('locationId', locationId).set('statusId', statusId);
    return this.http.get<any[]>(`${BASE_API}/reports/inventory/withdrawal-summary-list`, { params });
}

getReleaseSummary(from: string, to: string, docTypeId: number, locationId: number, statusId: number): Observable<any[]> {
    const params = new HttpParams()
        .set('from', from).set('to', to)
        .set('docTypeId', docTypeId).set('locationId', locationId).set('statusId', statusId);
    return this.http.get<any[]>(`${BASE_API}/reports/inventory/release-summary-list`, { params });
}

getAdjustmentSummary(from: string, to: string, locationId: number, statusId: number): Observable<any[]> {
    const params = new HttpParams()
        .set('from', from).set('to', to)
        .set('locationId', locationId).set('statusId', statusId);
    return this.http.get<any[]>(`${BASE_API}/reports/inventory/adjustment-summary-list`, { params });
}

getTransferSummary(from: string, to: string, locationId: number, statusId: number): Observable<any[]> {
    const params = new HttpParams()
        .set('from', from).set('to', to)
        .set('locationId', locationId).set('statusId', statusId);
    return this.http.get<any[]>(`${BASE_API}/reports/inventory/transfer-summary-list`, { params });
}

getReceiveSummary(from: string, to: string, locationId: number, statusId: number): Observable<any[]> {
    const params = new HttpParams()
        .set('from', from).set('to', to)
        .set('locationId', locationId).set('statusId', statusId);
    return this.http.get<any[]>(`${BASE_API}/reports/inventory/receive-summary-list`, { params });
}

// ─── Inventory Summary — item fetch (lazy load on expand) ───────────────────

getMcrtItems(transactionId: number): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_API}/mct/${transactionId}/items`);
}

getMstItems(transactionId: number): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_API}/mst/${transactionId}/items`);
}

getWithdrawalItems(id: number): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_API}/withdrawal/${id}/items`);
}

getReleaseItems(documentTransactionId: number): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_API}/stock-release/${documentTransactionId}/items`);
}
```

> **Backend dependency note:** If the endpoints above return 404, the backend needs to add them. This is acceptable as the UI will still compile — the View button will just show an error alert until the backend is implemented.

---

### Task 2: Fix `withdrawal-summary` — Full Pattern with DocType + Location + Search + Expandable Items

**Files:**
- Modify: `frontend/src/app/pages/inventory-reports/reports/withdrawal-summary/withdrawal-summary.component.ts`
- Modify: `frontend/src/app/pages/inventory-reports/reports/withdrawal-summary/withdrawal-summary.component.html`

**Columns:** Date | Withdrawal No. | Description | Doc Status | (chevron)
**Items sub-table:** Item Id | Item Code | Quantity | Unit | Qty Released
**Footer:** Number of Stock Withdrawals: {rows().length}
**Items:** Lazy-loaded on first expand via `getWithdrawalItems(row.id)`

- [ ] **Step 1: Rewrite `withdrawal-summary.component.ts`**

Replace the entire file content with:

```typescript
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { InventoryReportsService } from '../../inventory-reports.service';

const RELEASING_DOC_TYPES = [
    { id: 0, desc: 'All' },
    { id: 1, desc: 'Material Charge Ticket' },
    { id: 2, desc: 'Office Furniture and Equipment Acquired or Issued' },
    { id: 3, desc: 'Office Supplies or Spare Parts Issuance Slip' },
    { id: 4, desc: 'Stock Transfer Release' }
];

@Component({
    selector: 'app-withdrawal-summary',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './withdrawal-summary.component.html'
})
export class WithdrawalSummaryComponent implements OnInit {
    module   = 'Summary of Stock Withdrawal';
    menuLink = 'inventory-reports';

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    fromDate    = '';
    toDate      = '';
    statusId    = 0;
    locationId  = 0;
    docTypeId   = 0;
    statuses    = signal<any[]>([]);
    locations   = signal<any[]>([]);
    docTypes    = RELEASING_DOC_TYPES;

    rows      = signal<any[]>([]);
    isLoading = signal(false);

    expandedRows    = new Map<number, boolean>();
    rowItems        = new Map<number, any[]>();
    loadingRowItems = new Set<number>();

    private downloadSvc  = inject(DownloadService);
    private service      = inject(InventoryReportsService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.setDefaultDates();
        this.service.getDocumentStatuses().subscribe({
            next: (data) => this.statuses.set([{ id: 0, status: 'All' }, ...(data || [])])
        });
        this.service.getInventoryLocations().subscribe({
            next: (data) => this.locations.set([{ id: 0, description: 'All' }, ...(data || [])])
        });
    }

    setDefaultDates(): void {
        const now = new Date(), first = new Date(now.getFullYear(), now.getMonth(), 1);
        this.fromDate = first.toISOString().substring(0, 10);
        this.toDate   = now.toISOString().substring(0, 10);
    }

    search(): void {
        if (!this.fromDate || !this.toDate) {
            this.alertService.error(this.module, 'Validation', 'Please select a date range.');
            return;
        }
        this.rows.set([]);
        this.expandedRows.clear();
        this.rowItems.clear();
        this.loadingRowItems.clear();
        this.isLoading.set(true);
        this.service.getWithdrawalSummary(this.fromDate, this.toDate, this.docTypeId, this.locationId, this.statusId).subscribe({
            next: (data) => { this.rows.set(data ?? []); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); }
        });
    }

    toggleRow(index: number, row: any): void {
        const isExpanded = this.expandedRows.get(index) ?? false;
        this.expandedRows.set(index, !isExpanded);
        if (!isExpanded && !this.rowItems.has(index)) {
            this.loadingRowItems.add(index);
            this.service.getWithdrawalItems(row.id).subscribe({
                next: (items) => { this.rowItems.set(index, items ?? []); this.loadingRowItems.delete(index); },
                error: () => { this.loadingRowItems.delete(index); }
            });
        }
    }

    export(type: 'pdf' | 'xls'): void {
        if (!this.fromDate || !this.toDate) {
            this.alertService.error(this.module, 'Validation', 'Please select a date range.');
            return;
        }
        this.downloadSvc.print(
            `/reports/export/withdrawal-summary/${this.fromDate}/${this.toDate}/${this.statusId}`,
            { type, docType: this.docTypeId, location: this.locationId }
        );
    }
}
```

- [ ] **Step 2: Rewrite `withdrawal-summary.component.html`**

Replace the entire file content with:

```html
<div class="container-fluid">
    <app-page-title [title]="'Inventory Reports'" [subTitle]="module" [menuLink]="menuLink" [useSubtitleAsTitle]="true"/>
</div>

<div class="container-fluid">
    <div class="card">
        <div class="card-body">

            <div class="row g-2 align-items-end mb-3">
                <div class="col-md-auto">
                    <label class="form-label fw-bold mb-1">From</label>
                    <input type="text" mwlFlatpickr [options]="flatpickrOptions"
                           class="form-control" [(ngModel)]="fromDate" placeholder="From date"/>
                </div>
                <div class="col-md-auto">
                    <label class="form-label fw-bold mb-1">To</label>
                    <input type="text" mwlFlatpickr [options]="flatpickrOptions"
                           class="form-control" [(ngModel)]="toDate" placeholder="To date"/>
                </div>
                <div class="col-md-2">
                    <label class="form-label fw-bold mb-1">Status</label>
                    <select class="form-select" [(ngModel)]="statusId">
                        @for (s of statuses(); track s.id) {
                            <option [ngValue]="s.id">{{ s.status }}</option>
                        }
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label fw-bold mb-1">Document Type</label>
                    <select class="form-select" [(ngModel)]="docTypeId">
                        @for (dt of docTypes; track dt.id) {
                            <option [ngValue]="dt.id">{{ dt.desc }}</option>
                        }
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label fw-bold mb-1">Inventory Location</label>
                    <select class="form-select" [(ngModel)]="locationId">
                        @for (loc of locations(); track loc.id) {
                            <option [ngValue]="loc.id">{{ loc.description }}</option>
                        }
                    </select>
                </div>
                <div class="col-md-auto ms-auto">
                    <div class="d-flex gap-2">
                        <button type="button" class="btn btn-primary fw-bold" (click)="search()">
                            <ng-icon name="tablerEye" class="fw-bold me-1"></ng-icon>View
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

            <div class="table-responsive">
                <table class="table table-custom table-centered table-hover w-100 mb-0">
                    <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                        <tr class="text-uppercase fs-xxs">
                            <th></th>
                            <th>Date</th>
                            <th>Withdrawal No.</th>
                            <th>Description</th>
                            <th>Document Status</th>
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
                                <tr (click)="toggleRow($index, row)" style="cursor:pointer">
                                    <td class="text-center" style="width:30px">
                                        <ng-icon [name]="expandedRows.get($index) ? 'tablerChevronDown' : 'tablerChevronRight'"></ng-icon>
                                    </td>
                                    <td>{{ row.voucherDate | date:'MMM dd, yyyy' }}</td>
                                    <td>{{ row.code }}</td>
                                    <td>{{ row.description }}</td>
                                    <td>{{ row.documentStatus?.status }}</td>
                                </tr>
                                @if (expandedRows.get($index)) {
                                    <tr>
                                        <td colspan="5" class="p-0 bg-light bg-opacity-50">
                                            @if (loadingRowItems.has($index)) {
                                                <div class="p-3 text-center text-muted">
                                                    <span class="spinner-border spinner-border-sm me-2"></span>Loading items...
                                                </div>
                                            } @else {
                                                <table class="table table-sm table-bordered mb-0 ms-4" style="width:calc(100% - 2rem)">
                                                    <thead class="bg-secondary bg-opacity-10">
                                                        <tr class="text-uppercase fs-xxs">
                                                            <th>Item Id</th>
                                                            <th>Item Code</th>
                                                            <th class="text-end">Quantity</th>
                                                            <th>Unit</th>
                                                            <th class="text-end">Qty Released</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        @for (item of rowItems.get($index) || []; track $index) {
                                                            <tr>
                                                                <td>{{ item.itemId }}</td>
                                                                <td>{{ item.itemCode }}</td>
                                                                <td class="text-end">{{ item.quantity }}</td>
                                                                <td>{{ item.unitCode }}</td>
                                                                <td class="text-end">{{ item.quantityReleased }}</td>
                                                            </tr>
                                                        }
                                                        @if ((rowItems.get($index) || []).length === 0) {
                                                            <tr><td colspan="5" class="text-center text-muted">No items.</td></tr>
                                                        }
                                                    </tbody>
                                                </table>
                                            }
                                        </td>
                                    </tr>
                                }
                            }
                            <tr class="fw-bold">
                                <td colspan="5">Number of Stock Withdrawals: {{ rows().length }}</td>
                            </tr>
                        }
                    </tbody>
                </table>
            </div>

        </div>
    </div>
</div>
```

---

### Task 3: Fix `release-summary` — DocType + Location + Search + Expandable Items (Lazy)

**Files:**
- Modify: `frontend/src/app/pages/inventory-reports/reports/release-summary/release-summary.component.ts`
- Modify: `frontend/src/app/pages/inventory-reports/reports/release-summary/release-summary.component.html`

**Columns:** Date | Stock Release No. | Description | Doc Status | (chevron)
**Items sub-table:** Item Id | Item Code | Quantity | Unit | Qty Released
**Footer:** Number of Stock Releases: {rows().length}
**Items:** Lazy-loaded on first expand via `getReleaseItems(row.documentTransaction.id)`

- [ ] **Step 1: Rewrite `release-summary.component.ts`**

```typescript
import { Component, inject, OnInit, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { InventoryReportsService } from '../../inventory-reports.service';

const RELEASING_DOC_TYPES = [
    { id: 0, desc: 'All' },
    { id: 1, desc: 'Material Charge Ticket' },
    { id: 2, desc: 'Office Furniture and Equipment Acquired or Issued' },
    { id: 3, desc: 'Office Supplies or Spare Parts Issuance Slip' },
    { id: 4, desc: 'Stock Transfer Release' }
];

@Component({
    selector: 'app-release-summary',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './release-summary.component.html'
})
export class ReleaseSummaryComponent implements OnInit {
    module   = 'Summary of Stock Release';
    menuLink = 'inventory-reports';

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    fromDate   = '';
    toDate     = '';
    statusId   = 0;
    locationId = 0;
    docTypeId  = 0;
    statuses   = signal<any[]>([]);
    locations  = signal<any[]>([]);
    docTypes   = RELEASING_DOC_TYPES;

    rows      = signal<any[]>([]);
    isLoading = signal(false);

    expandedRows    = new Map<number, boolean>();
    rowItems        = new Map<number, any[]>();
    loadingRowItems = new Set<number>();

    private downloadSvc  = inject(DownloadService);
    private service      = inject(InventoryReportsService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.setDefaultDates();
        this.service.getDocumentStatuses().subscribe({
            next: (data) => this.statuses.set([{ id: 0, status: 'All' }, ...(data || [])])
        });
        this.service.getInventoryLocations().subscribe({
            next: (data) => this.locations.set([{ id: 0, description: 'All' }, ...(data || [])])
        });
    }

    setDefaultDates(): void {
        const now = new Date(), first = new Date(now.getFullYear(), now.getMonth(), 1);
        this.fromDate = first.toISOString().substring(0, 10);
        this.toDate   = now.toISOString().substring(0, 10);
    }

    search(): void {
        if (!this.fromDate || !this.toDate) {
            this.alertService.error(this.module, 'Validation', 'Please select a date range.');
            return;
        }
        this.rows.set([]);
        this.expandedRows.clear();
        this.rowItems.clear();
        this.loadingRowItems.clear();
        this.isLoading.set(true);
        this.service.getReleaseSummary(this.fromDate, this.toDate, this.docTypeId, this.locationId, this.statusId).subscribe({
            next: (data) => { this.rows.set(data ?? []); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); }
        });
    }

    toggleRow(index: number, row: any): void {
        const isExpanded = this.expandedRows.get(index) ?? false;
        this.expandedRows.set(index, !isExpanded);
        if (!isExpanded && !this.rowItems.has(index)) {
            this.loadingRowItems.add(index);
            const dtId = row.documentTransaction?.id ?? row.id;
            this.service.getReleaseItems(dtId).subscribe({
                next: (items) => { this.rowItems.set(index, items ?? []); this.loadingRowItems.delete(index); },
                error: () => { this.loadingRowItems.delete(index); }
            });
        }
    }

    export(type: 'pdf' | 'xls'): void {
        if (!this.fromDate || !this.toDate) {
            this.alertService.error(this.module, 'Validation', 'Please select a date range.');
            return;
        }
        this.downloadSvc.print(
            `/reports/export/release-summary/${this.fromDate}/${this.toDate}/${this.statusId}`,
            { type, docType: this.docTypeId, location: this.locationId }
        );
    }
}
```

- [ ] **Step 2: Rewrite `release-summary.component.html`**

Same structure as withdrawal-summary.component.html but with:
- `module` displayed as "Summary of Stock Release"
- Main table column: "Stock Release No." (not "Withdrawal No.")
- Items sub-table header: "Qty Released" (same as withdrawal)
- Footer: "Number of Stock Releases: {{ rows().length }}"
- Calls: `toggleRow($index, row)`, `search()`, `export('pdf')`, `export('xls')`

Write the full HTML following exactly the same structure as Task 2 Step 2, substituting the column name differences above.

---

### Task 4: Fix `mcrt-summary` — Location + Search + Expandable Items (Lazy)

**Files:**
- Modify: `frontend/src/app/pages/inventory-reports/reports/mcrt-summary/mcrt-summary.component.ts`
- Modify: `frontend/src/app/pages/inventory-reports/reports/mcrt-summary/mcrt-summary.component.html`

**Columns:** Date | MCRT No. | Description | Doc Status | (chevron)
**Items sub-table:** Item Id | Item Code | Quantity | Unit | Cost | Total
**Footer:** Number of MCRT: {rows().length}
**Items:** Lazy-loaded on first expand via `getMcrtItems(row.transaction.id)`

- [ ] **Step 1: Rewrite `mcrt-summary.component.ts`**

```typescript
import { Component, inject, OnInit, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { InventoryReportsService } from '../../inventory-reports.service';

@Component({
    selector: 'app-mcrt-summary',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './mcrt-summary.component.html'
})
export class McrtSummaryComponent implements OnInit {
    module   = 'Summary of MCRT';
    menuLink = 'inventory-reports';

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    fromDate   = '';
    toDate     = '';
    statusId   = 0;
    locationId = 0;
    statuses   = signal<any[]>([]);
    locations  = signal<any[]>([]);

    rows      = signal<any[]>([]);
    isLoading = signal(false);

    expandedRows    = new Map<number, boolean>();
    rowItems        = new Map<number, any[]>();
    loadingRowItems = new Set<number>();

    private downloadSvc  = inject(DownloadService);
    private service      = inject(InventoryReportsService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.setDefaultDates();
        this.service.getDocumentStatuses().subscribe({
            next: (data) => this.statuses.set([{ id: 0, status: 'All' }, ...(data || [])])
        });
        this.service.getInventoryLocations().subscribe({
            next: (data) => this.locations.set([{ id: 0, description: 'All' }, ...(data || [])])
        });
    }

    setDefaultDates(): void {
        const now = new Date(), first = new Date(now.getFullYear(), now.getMonth(), 1);
        this.fromDate = first.toISOString().substring(0, 10);
        this.toDate   = now.toISOString().substring(0, 10);
    }

    search(): void {
        if (!this.fromDate || !this.toDate) {
            this.alertService.error(this.module, 'Validation', 'Please select a date range.');
            return;
        }
        this.rows.set([]);
        this.expandedRows.clear();
        this.rowItems.clear();
        this.loadingRowItems.clear();
        this.isLoading.set(true);
        this.service.getMcrtSummary(this.fromDate, this.toDate, this.locationId, this.statusId).subscribe({
            next: (data) => { this.rows.set(data ?? []); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); }
        });
    }

    toggleRow(index: number, row: any): void {
        const isExpanded = this.expandedRows.get(index) ?? false;
        this.expandedRows.set(index, !isExpanded);
        if (!isExpanded && !this.rowItems.has(index)) {
            this.loadingRowItems.add(index);
            const transId = row.transaction?.id ?? row.id;
            this.service.getMcrtItems(transId).subscribe({
                next: (items) => { this.rowItems.set(index, items ?? []); this.loadingRowItems.delete(index); },
                error: () => { this.loadingRowItems.delete(index); }
            });
        }
    }

    export(type: 'pdf' | 'xls'): void {
        if (!this.fromDate || !this.toDate) {
            this.alertService.error(this.module, 'Validation', 'Please select a date range.');
            return;
        }
        this.downloadSvc.print(
            `/reports/export/mcrt-summary/${this.fromDate}/${this.toDate}/${this.statusId}`,
            { type, location: this.locationId }
        );
    }
}
```

- [ ] **Step 2: Rewrite `mcrt-summary.component.html`**

Write the full HTML following the same structure as withdrawal-summary but:
- Filter row: From, To, Status, Location — **no DocType filter**
- Main table columns: (chevron), Date, MCRT No., Description, Document Status
- Items sub-table: Item Id | Item Code | Quantity | Unit | Cost | Total
- Items loop: `item.itemId`, `item.itemCode`, `item.quantity`, `item.unitCode`, `item.unitCost | number:'1.2-2'`, `item.totalCost | number:'1.2-2'`
- Footer: `Number of MCRT: {{ rows().length }}`

```html
<!-- Items sub-table header for mcrt/mst (cost + total instead of qty released): -->
<tr class="text-uppercase fs-xxs">
    <th>Item Id</th>
    <th>Item Code</th>
    <th class="text-end">Quantity</th>
    <th>Unit</th>
    <th class="text-end">Cost</th>
    <th class="text-end">Total</th>
</tr>
<!-- Items loop: -->
<tr>
    <td>{{ item.itemId }}</td>
    <td>{{ item.itemCode }}</td>
    <td class="text-end">{{ item.quantity }}</td>
    <td>{{ item.unitCode }}</td>
    <td class="text-end">{{ item.unitCost | number:'1.2-2' }}</td>
    <td class="text-end">{{ item.totalCost | number:'1.2-2' }}</td>
</tr>
```

---

### Task 5: Fix `mst-summary` — Location + Search + Expandable Items (Lazy)

**Files:**
- Modify: `frontend/src/app/pages/inventory-reports/reports/mst-summary/mst-summary.component.ts`
- Modify: `frontend/src/app/pages/inventory-reports/reports/mst-summary/mst-summary.component.html`

**Columns:** Date | MST No. | Description | Doc Status | (chevron)
**Items sub-table:** Item Id | Item Code | Quantity | Unit | Cost | Total
**Footer:** Number of MST: {rows().length}
**Items:** Lazy-loaded via `getMstItems(row.transaction.id)`

- [ ] **Step 1: Rewrite `mst-summary.component.ts`**

Identical to `McrtSummaryComponent` in Task 4 except:
- `selector: 'app-mst-summary'`
- `module = 'Summary of MST'`
- `search()` calls `this.service.getMstSummary(...)` instead of `getMcrtSummary`
- `toggleRow()` calls `this.service.getMstItems(transId)` instead of `getMcrtItems`
- `export()` uses `/reports/export/mst-summary/${...}`

- [ ] **Step 2: Rewrite `mst-summary.component.html`**

Identical to mcrt-summary.component.html except:
- Column header: "MST No." instead of "MCRT No."
- Footer: `Number of MST: {{ rows().length }}`

---

### Task 6: Fix `adjustment-summary` — Location + Search + Expandable Items (Inline)

**Files:**
- Modify: `frontend/src/app/pages/inventory-reports/reports/adjustment-summary/adjustment-summary.component.ts`
- Modify: `frontend/src/app/pages/inventory-reports/reports/adjustment-summary/adjustment-summary.component.html`

**Columns:** Date | Adjustment No. | Description | Doc Status | (chevron)
**Items:** Inline from `row.details` — no AJAX on expand
**Footer:** Number of Stock Adjustments: {rows().length} + Total Cost: {totalCost()}

- [ ] **Step 1: Rewrite `adjustment-summary.component.ts`**

```typescript
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { InventoryReportsService } from '../../inventory-reports.service';

@Component({
    selector: 'app-adjustment-summary',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './adjustment-summary.component.html'
})
export class AdjustmentSummaryComponent implements OnInit {
    module   = 'Summary of Stock Adjustment';
    menuLink = 'inventory-reports';

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    fromDate   = '';
    toDate     = '';
    statusId   = 0;
    locationId = 0;
    statuses   = signal<any[]>([]);
    locations  = signal<any[]>([]);

    rows      = signal<any[]>([]);
    isLoading = signal(false);

    // Inline expand — items are in row.details from the search response
    expandedRows = new Map<number, boolean>();

    totalCost = computed(() =>
        this.rows().reduce((sum, r) =>
            sum + (r.details || []).reduce((s: number, item: any) => s + (item.totalCost || 0), 0), 0)
    );

    private downloadSvc  = inject(DownloadService);
    private service      = inject(InventoryReportsService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.setDefaultDates();
        this.service.getDocumentStatuses().subscribe({
            next: (data) => this.statuses.set([{ id: 0, status: 'All' }, ...(data || [])])
        });
        this.service.getInventoryLocations().subscribe({
            next: (data) => this.locations.set([{ id: 0, description: 'All' }, ...(data || [])])
        });
    }

    setDefaultDates(): void {
        const now = new Date(), first = new Date(now.getFullYear(), now.getMonth(), 1);
        this.fromDate = first.toISOString().substring(0, 10);
        this.toDate   = now.toISOString().substring(0, 10);
    }

    search(): void {
        if (!this.fromDate || !this.toDate) {
            this.alertService.error(this.module, 'Validation', 'Please select a date range.');
            return;
        }
        this.rows.set([]);
        this.expandedRows.clear();
        this.isLoading.set(true);
        this.service.getAdjustmentSummary(this.fromDate, this.toDate, this.locationId, this.statusId).subscribe({
            next: (data) => { this.rows.set(data ?? []); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); }
        });
    }

    toggleRow(index: number): void {
        const isExpanded = this.expandedRows.get(index) ?? false;
        this.expandedRows.set(index, !isExpanded);
    }

    export(type: 'pdf' | 'xls'): void {
        if (!this.fromDate || !this.toDate) {
            this.alertService.error(this.module, 'Validation', 'Please select a date range.');
            return;
        }
        this.downloadSvc.print(
            `/reports/export/adjustment-summary/${this.fromDate}/${this.toDate}/${this.statusId}`,
            { type, location: this.locationId }
        );
    }
}
```

- [ ] **Step 2: Rewrite `adjustment-summary.component.html`**

```html
<div class="container-fluid">
    <app-page-title [title]="'Inventory Reports'" [subTitle]="module" [menuLink]="menuLink" [useSubtitleAsTitle]="true"/>
</div>

<div class="container-fluid">
    <div class="card">
        <div class="card-body">

            <div class="row g-2 align-items-end mb-3">
                <div class="col-md-auto">
                    <label class="form-label fw-bold mb-1">From</label>
                    <input type="text" mwlFlatpickr [options]="flatpickrOptions" class="form-control" [(ngModel)]="fromDate" placeholder="From date"/>
                </div>
                <div class="col-md-auto">
                    <label class="form-label fw-bold mb-1">To</label>
                    <input type="text" mwlFlatpickr [options]="flatpickrOptions" class="form-control" [(ngModel)]="toDate" placeholder="To date"/>
                </div>
                <div class="col-md-2">
                    <label class="form-label fw-bold mb-1">Status</label>
                    <select class="form-select" [(ngModel)]="statusId">
                        @for (s of statuses(); track s.id) {
                            <option [ngValue]="s.id">{{ s.status }}</option>
                        }
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label fw-bold mb-1">Inventory Location</label>
                    <select class="form-select" [(ngModel)]="locationId">
                        @for (loc of locations(); track loc.id) {
                            <option [ngValue]="loc.id">{{ loc.description }}</option>
                        }
                    </select>
                </div>
                <div class="col-md-auto ms-auto">
                    <div class="d-flex gap-2">
                        <button type="button" class="btn btn-primary fw-bold" (click)="search()">
                            <ng-icon name="tablerEye" class="fw-bold me-1"></ng-icon>View
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

            <div class="table-responsive">
                <table class="table table-custom table-centered table-hover w-100 mb-0">
                    <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                        <tr class="text-uppercase fs-xxs">
                            <th></th>
                            <th>Date</th>
                            <th>Adjustment No.</th>
                            <th>Description</th>
                            <th>Document Status</th>
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
                                <tr (click)="toggleRow($index)" style="cursor:pointer">
                                    <td class="text-center" style="width:30px">
                                        <ng-icon [name]="expandedRows.get($index) ? 'tablerChevronDown' : 'tablerChevronRight'"></ng-icon>
                                    </td>
                                    <td>{{ row.voucherDate | date:'MMM dd, yyyy' }}</td>
                                    <td>{{ row.code }}</td>
                                    <td>{{ row.description }}</td>
                                    <td>{{ row.documentStatus?.status }}</td>
                                </tr>
                                @if (expandedRows.get($index)) {
                                    <tr>
                                        <td colspan="5" class="p-0 bg-light bg-opacity-50">
                                            <table class="table table-sm table-bordered mb-0 ms-4" style="width:calc(100% - 2rem)">
                                                <thead class="bg-secondary bg-opacity-10">
                                                    <tr class="text-uppercase fs-xxs">
                                                        <th>Item Id</th>
                                                        <th>Item Code</th>
                                                        <th class="text-end">Quantity</th>
                                                        <th>Unit</th>
                                                        <th class="text-end">Cost</th>
                                                        <th class="text-end">Total</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    @for (item of row.details || []; track $index) {
                                                        <tr>
                                                            <td>{{ item.itemId }}</td>
                                                            <td>{{ item.itemCode }}</td>
                                                            <td class="text-end">{{ item.quantity }}</td>
                                                            <td>{{ item.unitCode }}</td>
                                                            <td class="text-end">{{ item.unitCost | number:'1.2-2' }}</td>
                                                            <td class="text-end">{{ item.totalCost | number:'1.2-2' }}</td>
                                                        </tr>
                                                    }
                                                    @if (!(row.details?.length)) {
                                                        <tr><td colspan="6" class="text-center text-muted">No items.</td></tr>
                                                    }
                                                </tbody>
                                            </table>
                                        </td>
                                    </tr>
                                }
                            }
                            <tr class="fw-bold">
                                <td colspan="4">Number of Stock Adjustments: {{ rows().length }}</td>
                                <td class="text-end">Total Cost: {{ totalCost() | number:'1.2-2' }}</td>
                            </tr>
                        }
                    </tbody>
                </table>
            </div>

        </div>
    </div>
</div>
```

---

### Task 7: Fix `transfer-summary` — Location + Search + Expandable Items (Inline)

**Files:**
- Modify: `frontend/src/app/pages/inventory-reports/reports/transfer-summary/transfer-summary.component.ts`
- Modify: `frontend/src/app/pages/inventory-reports/reports/transfer-summary/transfer-summary.component.html`

**Columns:** Date | Transfer No. | Description | Doc Status | (chevron)
**Items sub-table:** Item Id | Item Code | Quantity | Unit | Cost | Total (inline `row.details`)
**Footer:** Number of Stock Transfers: {rows().length} + Total Cost

- [ ] **Step 1: Rewrite `transfer-summary.component.ts`**

Identical to `AdjustmentSummaryComponent` (Task 6 Step 1) except:
- `selector: 'app-transfer-summary'`
- `module = 'Summary of Stock Transfer'`
- `search()` calls `this.service.getTransferSummary(...)` instead of `getAdjustmentSummary`
- `export()` uses `/reports/export/transfer-summary/${...}`

- [ ] **Step 2: Rewrite `transfer-summary.component.html`**

Identical to adjustment-summary.component.html (Task 6 Step 2) except:
- Column header: "Transfer No."
- Footer: `Number of Stock Transfers: {{ rows().length }}`

---

### Task 8: Fix `receive-summary` — Location + Search + Expandable Items (Inline)

**Files:**
- Modify: `frontend/src/app/pages/inventory-reports/reports/receive-summary/receive-summary.component.ts`
- Modify: `frontend/src/app/pages/inventory-reports/reports/receive-summary/receive-summary.component.html`

**Columns:** Date | Receive No. | Description | Doc Status | (chevron)
**Items sub-table:** Item Id | Item Code | Quantity | Unit | Cost | Total (inline `row.details`)
**Footer:** Number of Stock Receives: {rows().length} + Total Cost

- [ ] **Step 1: Rewrite `receive-summary.component.ts`**

Identical to `AdjustmentSummaryComponent` (Task 6 Step 1) except:
- `selector: 'app-receive-summary'`
- `module = 'Summary of Received Stock Transfers'`
- `search()` calls `this.service.getReceiveSummary(...)` instead of `getAdjustmentSummary`
- `export()` uses `/reports/export/receive-summary/${...}`

- [ ] **Step 2: Rewrite `receive-summary.component.html`**

Identical to adjustment-summary.component.html (Task 6 Step 2) except:
- Column header: "Receive No."
- Footer: `Number of Stock Receives: {{ rows().length }}`

---

### Task 9: Audit Existing Inventory Reports (Alignment Verification)

**Files:** Read-only audit — no code changes expected unless issues are found.

**Reports to audit:** bin-card, stock-card, inventory-balance, material-issuance-summary, ideal-quantity, mrte-ledger, special-equipment-release-summary, special-equipment-pending-summary.

- [ ] **Step 1: Audit bin-card and stock-card export URL consistency**

Both components convert dates to timestamps in `search()` but use raw date strings in `export()`. Verify whether the v2 backend's export endpoint (`/reports/export/bin-card/{from}/{to}/{itemStockId}`) accepts date strings or expects timestamps.

Read:
```
src/main/java/com/noreco1/fireflyv2/controller/ReportsController.java
```
Look for `exportBinCard` and `exportStockCard` method signatures. If the path variable is typed as `String` (not Long/Date), date strings are accepted — no fix needed.

- [ ] **Step 2: Audit ideal-quantity report type IDs**

The NEW component uses `reportTypes = [{ id: 0, label: 'For Reorder' }, { id: 1, label: 'Ideal Quantity' }]`.

Read the v2 backend `ReportsController` to find `idealQuantityReorderPoint` and confirm what `reportTypeId` values it expects. The OLD used 1 and 2 (enum IDs). If the v2 backend expects 0 and 1, NEW is correct. If it expects 1 and 2, fix:

```typescript
// In ideal-quantity.component.ts, change to:
reportTypes = [
    { id: 1, label: 'For Reorder' },
    { id: 2, label: 'Ideal Quantity' },
];
reportTypeId = 1; // default to first real option
```

- [ ] **Step 3: Audit mrte-ledger export URL**

OLD export: `/reports/export/mrte-ledger?acctNo=`
NEW export: `this.downloadSvc.print('/reports/export/mrte-ledger', params)` where params includes `{ type, acctNo }` — ALIGNED.

Confirm: no changes needed.

- [ ] **Step 4: Audit material-issuance-summary, special-equipment-*, inventory-balance**

Compare filter values and export URLs against OLD JSPs. These were verified aligned during plan research. Confirm no additional fixes needed.

Expected result: All 8 existing reports are aligned. If any discrepancy is found in Step 2 (ideal-quantity IDs), apply the fix in that step.

---

### Task 10: Register tabler Icons in All Modified Components

**Context:** The new HTML files use `tablerChevronRight`, `tablerChevronDown`, `tablerEye`, `tablerFileTypePdf`, `tablerFileTypeXls`. These must be provided in each component's `providers`.

- [ ] **Step 1: Update imports in all 7 summary .ts files**

Each modified `.component.ts` file needs:

```typescript
import { provideIcons } from '@ng-icons/core';
import { tablerChevronRight, tablerChevronDown, tablerEye, tablerFileTypePdf, tablerFileTypeXls } from '@ng-icons/tabler-icons';
```

And in `@Component`:
```typescript
providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults(), provideIcons({ tablerChevronRight, tablerChevronDown, tablerEye, tablerFileTypePdf, tablerFileTypeXls })]
```

Apply to: withdrawal-summary, release-summary, mcrt-summary, mst-summary, adjustment-summary, transfer-summary, receive-summary.

- [ ] **Step 2: Verify existing icon imports in bin-card and stock-card**

These components use `ng-icon` — confirm their `providers` already include `provideIcons`. If not, add the missing icons. No changes expected since these were pre-existing.

---

### Task 11: Build Validation

- [ ] **Step 1: TypeScript compilation check**

```bash
cd frontend && npx ng build --configuration=production 2>&1 | tail -40
```

Expected: No errors. Fix any type errors before proceeding.

- [ ] **Step 2: Lint check**

```bash
cd frontend && npx ng lint 2>&1 | tail -40
```

Expected: No errors or warnings introduced by the changes.

- [ ] **Step 3: Spot-check in browser (if dev server available)**

Navigate to each of the 7 summary pages and confirm:
- Filter row renders with correct controls (location, docType where applicable)
- View button loads data into table
- Click any row → chevron rotates, items sub-table appears
- Export PDF / Export Excel buttons still fire correctly

---

## Self-Review Checklist

1. **Spec coverage:**
   - ✓ `getWithdrawalSummary` + docType filter → Task 2
   - ✓ `getReleaseSummary` + docType filter → Task 3
   - ✓ `getMcrtSummary` + lazy items → Task 4
   - ✓ `getMstSummary` + lazy items → Task 5
   - ✓ `getAdjustmentSummary` + inline items + totalCost → Task 6
   - ✓ `getTransferSummary` + inline items + totalCost → Task 7
   - ✓ `getReceiveSummary` + inline items + totalCost → Task 8
   - ✓ Icon registration → Task 10
   - ✓ Build validation → Task 11
   - ✓ Existing reports audit → Task 9

2. **Backend dependency:** Tasks 1–8 write service calls to endpoints that need to be added to ReportsController. If these 404, the View button shows a service error. The export endpoints already exist and will work regardless.

3. **Type consistency:** `toggleRow(index: number, row: any)` used in withdrawal/release/mcrt/mst; `toggleRow(index: number)` used in adjustment/transfer/receive (no row needed since items are inline). Both signatures are consistent within their component.
