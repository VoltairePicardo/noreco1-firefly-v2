# Document Inquiry (DI) — Migration Design Spec
**Date:** 2026-08-10
**Source:** Old Firefly `views/di/main.jsp` + `resources/js/app/module/di.js` + `factories/document-inquiry-factory.js`
**Target:** Firefly v2 `frontend/src/app/pages/di/`
**Approach:** Option C — Single flat component, Angular 19 signals, full-stack (frontend + backend controller)

---

## 1. Goals

- Faithfully reproduce old Firefly Document Inquiry functionality in Angular 19 v2 standards.
- Implement the empty `DocumentInquiryController.java` by wiring it to the existing `DocumentInquiryServiceImpl`.
- Verify and supplement all document-detail endpoints in their respective controllers.
- No business logic invented. Old Firefly is the functional source of truth.

---

## 2. File Structure

### Frontend (new)
```
frontend/src/app/pages/di/
  di.route.ts
  di.service.ts
  di-main/
    di-main.component.ts
    di-main.component.html
```

### Frontend (modified)
```
frontend/src/app/pages/pages.route.ts   — add { path: 'di', ... }
```

### Backend (modified)
```
src/main/java/com/noreco1/fireflyv2/controller/DocumentInquiryController.java
```

### Backend (verify / supplement per controller during impl)
Each detail endpoint below is checked against existing v2 controllers. If missing, a GET method is added to the appropriate controller following the old endpoint path and logic.

---

## 3. Document Type Config

The `document-types` endpoint returns a list built from the `DocumentType` enum with these fields per entry:

| Field | Meaning |
|---|---|
| `id` | DocumentType.getId() |
| `code` | DocumentType.getDescription() (short code, e.g. "RV") |
| `desc` | DocumentType display name |
| `tableName` | DB table name for the native query |
| `pt` | "particulars" column alias (e.g. `purpose`, `particulars`, `FK_vendorAccountNo`) |
| `type` | 1=Purchasing, 2=Accounting, 3=Inventory, 4=WorkOrder |
| `module` | Group label (e.g. "PURCHASING") |
| `order` | Sort order for optgroup rendering |

### Full config (matches old DocumentDtoerImpl exactly):

| DocumentType | tableName | pt | type |
|---|---|---|---|
| RV | PurchaseRequest | purpose | 1 |
| CF (Canvass) | Canvass | FK_vendorAccountNo | 1 |
| QUOTATION_SUMMARY | Quotation | particulars | 1 |
| PO | PurchaseOrder | FK_vendorAccountNo | 1 |
| JO | JobOrder | FK_vendorAccountNo | 1 |
| JOA | JoAcceptance | FK_vendorAccountNo | 1 |
| PR | PaymentRequest | FK_vendorAccountNo | 1 |
| APV | AccountsPayableVoucher | particulars | 2 |
| CV | CheckVoucher | particulars | 2 |
| JV | JournalVoucher | explanation | 2 |
| CRV | CashReceipts | particulars | 2 |
| SV | SalesVoucher | particulars | 2 |
| RR | ReceivingReport | purpose | 3 |
| SW | StockWithdrawal | description | 3 |
| SRL | StockRelease | description | 3 |
| ST | StockTransfer | description | 3 |
| SRC | StockReceive | description | 3 |
| SA | StockAdjustment | description | 3 |
| MCT | MaterialCreditTicket | description | 3 |
| MST | MaterialSalvageTicket | description | 3 |
| CE | CostEstimate | description | 4 |
| SITE_INSPECTION_REPORT | SiteInspectionReport | description | 4 |

---

## 4. Backend API Endpoints

### DocumentInquiryController — implement all 7:

```
GET /document-inquiry/document-types
  → static config list (private helper method in controller)

GET /document-inquiry/list/{docTypeId}/tn/{tableName}/sd/{startDate}/ed/{endDate}/pt/{particulars}
  ?suppId=&dDate=&c=&eAmt=&tAmt=&d=
  → service.findDocumentsByTypeIdStartDateEndDate(...)

GET /document-inquiry/list/{docTypeId}/tn/{tableName}/sd/{startDate}/ed/{endDate}/pt/{particulars}/userId/{userId}
  → service.findDocumentsByUserId(...)

GET /document-inquiry/search/{query}
  → service.findDocumentsByQuery(query)

GET /document-inquiry/purchase-cycle/{rvdId}
  → service.findDocumentsByRvdId(rvdId)

GET /document-inquiry/accounting-cycle/{transId}
  → service.findDocumentsByTransId(transId)

GET /document-inquiry/inventory-cycle/{transId}
  → service.findInventoryDocumentsByTransId(transId)
```

### Existing endpoints (already in v2):
```
GET /json/departments                  — AnyJsonController
GET /json/departments-by-user          — AnyJsonController
GET /json/document-logs/{transId}      — AnyJsonController
```

### Detail endpoints (verify each; add if missing):
```
GET /rv-detail/rvd/{rvId}
GET /canvass-detail/cnvsd/{canvassId}
GET /po-detail/pod/{poId}
GET /job-order/jod/{joId}
GET /jo-acceptance/joa-detail/joad/{joaId}
GET /quotation-detail/{quotationId}
GET /ledger/gl/{transId}
GET /receiving-report/rr-detail/{rrId}
GET /inventory/withdrawal/items/{swId}
GET /inventory/releasing/withdrawal-details/{swId}
GET /inventory/stock-transfer/items/{transId}
GET /inventory/stock-adjustment/items/{transId}
GET /inventory/mct/items/{transId}
GET /inventory/mst/items/{transId}
GET /inventory/receiving/items/{transId}
GET /cost-estimate/details/{transId}
GET /site-inspection-report/details/{transId}
GET /json/users                        — verify
```

---

## 5. Angular Frontend Design

### 5.1 Component: `DiMainComponent`

**Signals:**
```typescript
docTypes        = signal<any[]>([])
departments     = signal<any[]>([])
users           = signal<any[]>([])
docList         = signal<any[]>([])
selectedDoc     = signal<any>(null)
docInqDetails   = signal<any[]>([])
cycles          = signal<any[]>([])
quotationDetails        = signal<any[]>([])
costEstimateDetails     = signal<any[]>([])
siteInspectionDetails   = signal<any[]>([])
quotationSuppliers      = signal<any[]>([])
logs            = signal<any[]>([])
isLoading       = signal(false)
showFilters     = signal(false)    // shown after first search
showPrint       = signal(false)    // shown after row selection
showLogs        = signal(false)
```

**Plain state (no signal needed — rarely changes or drives template directly):**
```typescript
selectedDocType: any = null
selectedDepartment: any = null
selectedSupplier: any = null
selectedUser: any = null
fromDate = ''        // current month start
toDate   = ''        // current month end
dueDate  = ''
codeFilter = ''
amountEntryFilter: number | null = null
totalsFilter: number | null = null
voucherQuery = ''

// column filter toggles
filterStatus = false
filterDate   = false
filterDocNo  = false
filterParticulars = false
showUserFilter = false

// doc type flags (set on selectDocument)
forPurchasing = true
forInventory  = false
rv = true   // show Amount column in purchasing table (false for RV and Canvass)
isReceivingReport = false
isWithdrawal  = false
isReleasing   = false
isStockTransfer = false
isReceiving   = false
isStockAdjustment = false
isMCT = false
isMST = false
showQuotationDetails      = false
showCostEstimateDetails   = false
showSiteInspectionDetails = false
showInventoryDetailAmount = true
```

**Computed:**
```typescript
get showAccountFilters(): boolean {
  const ids = [APV_ID, CV_ID, JV_ID, CRV_ID, SV_ID]
  return ids.includes(this.selectedDocType?.id)
}
get showSupplierFilter(): boolean {
  return [APV_ID, CV_ID].includes(this.selectedDocType?.id)
}
get showDueDateFilter(): boolean {
  return this.selectedDocType?.id === APV_ID
}
get filteredDocList(): any[] {
  // client-side column filter applied per checkbox state
}
```

**Key methods:**
```typescript
ngOnInit()           — load docTypes, departments, defaultDepartment, users; set date defaults
onDocTypeChange()    — reset grid + detail panels, reset filters
search()             — call list API, set showFilters(true)
searchByQuery()      — call search API
filterByUser()       — call list-by-userId API
selectDocument(row)  — set selectedDoc, load details + cycles, auto-load logs
browseSupplier()     — open BrowseSupplierModalComponent
removeSupplier()     — clear selectedSupplier
toggleLogs()         — call /json/document-logs/{transId}, toggle showLogs
print()              — call DownloadService.print() with correct export URL per doc type
setDetails(data)     — map raw API response to docInqDetails signal
setInventoryDetails(data) — map inventory API response
```

### 5.2 Template Structure

```html
<app-page-title>

<div class="container-fluid">
  <div class="card">
    <div class="card-body">

      <!-- Row 1: Primary Filters -->
      Department select | DocType select | From date | To date | [Go!]

      <!-- Row 2: Supplier + Due Date (conditional) -->
      @if (showSupplierFilter) { ... }

      <!-- Row 3: Account/Amount Filters (conditional) -->
      @if (showAccountFilters) { ... }

      <!-- Row 4: Toolbar (shown after first search) -->
      @if (showFilters()) {
        Add Filters dropdown | Print btn | Transacted By select | Search by voucher code
      }

      <!-- Row 5: Main Table -->
      table with docList() | loading/empty states | clickable Code column

      <hr/>

      <!-- Row 6: Detail Panels (shown after row selection) -->
      @if (selectedDoc()) {

        <!-- Standard details + Cycles (side by side, 50/50) -->
        @if (!showQuotationDetails && !showCostEstimateDetails && !showSiteInspectionDetails) {
          <div class="row">
            <div class="col-lg-6"> <!-- details table --> </div>
            <div class="col-lg-6"> <!-- cycles table --> </div>
          </div>
        }

        <!-- Quotation details (full width) -->
        @if (showQuotationDetails) { ... }

        <!-- Cost Estimate details (full width) -->
        @if (showCostEstimateDetails) { ... }

        <!-- Site Inspection Report details (full width) -->
        @if (showSiteInspectionDetails) { ... }

        <!-- View Logs toggle -->
        <a (click)="toggleLogs()">View Logs</a>
        @if (showLogs()) {
          <!-- Logs table with type-specific log cell -->
        }
      }

    </div>
  </div>
</div>
```

### 5.3 Detail table column rules (preserves old logic exactly)

| docType | forPurchasing | forInventory | rv | Detail columns |
|---|---|---|---|---|
| RV | true | false | false | Desc / Qty / Unit |
| Canvass | true | false | false | Desc / Qty / Unit |
| PO, JO, JOA, PR | true | false | true | Desc / Qty / Unit / Amount |
| Quotation | true | false | — | (special quotation table) |
| CE | — | — | — | (special CE table) |
| SIR | — | — | — | (special SIR table) |
| APV, CV, JV, CRV, SV, AJ | false | false | — | Account / Debit / Credit |
| RR | false | true | — | Desc / Qty / Unit / Amount |
| SW, SRL | false | true | — | Desc / Qty / Unit (no Amount) |
| ST, SA, MCT, MST, SRC | false | true | — | Desc / Qty / Unit / Amount |

### 5.4 `setDetails()` mapping (from old JS — preserved exactly)

```typescript
setDetails(data: any[]): void {
  this.docInqDetails.set(data.map(v => ({
    id: this.selectedDocType?.id === JOA_ID
      ? v.rvDetailId
      : v.rvDetailId ?? v.id ?? v.accountId,
    unit: v.unitCode,
    particulars: v.itemDescription ?? v.joDescription ?? v.description,
    quantity: v.quantity,
    amount: v.itemAmount,
    debit: v.debit,
    credit: v.credit
  })))
}
```

### 5.5 Print URL resolution (preserves old logic exactly)

RV type constants (from old `firefly.js`):
- `RV_FOR_IT = 2`
- `RV_FOR_REP = 3`
- `RV_FOR_LABOR = 4`

Base URL map (from old `util-service.js setBaseUrlForPrint`):

| typeDesc | base path |
|---|---|
| Accounts Payable | `accounts-payable` |
| Journal Voucher | `journal-voucher` |
| Check Voucher | `check-voucher` |
| Purchase or Work Request | `requisition-voucher` |
| Canvass | `canvass-rv` |
| Purchase Order | `purchase-order` |
| Job Order | `job-order` |
| JO Acceptance | `jo-acceptance` |
| Payment Request | `payment-request` |
| Summary of Quotation | `quotation` |
| Sales Voucher | `sales-voucher-mgt` |
| Cash Receipts | `cash-receipts-mgt` |
| Receiving Report | `receiving-report-mgt` |
| Stock Withdrawal | `inventory/withdrawal` |
| Stock Release | `inventory/releasing` |
| Stock Adjustment | `inventory/stock-adjustment-mgt` |
| Stock Transfer | `inventory/stock-transfer-mgt` |
| Stock Receive | `inventory/receiving-mgt` |
| Material Credit Ticket | `inventory/mct-mgt` |
| Material Salvage Ticket | `inventory/mst-mgt` |
| Site Inspection Report | `site-inspection-report` |
| Cost Estimate | `cost-estimate-mgt` |

```typescript
// Defined as a const map in di.service.ts
export const PRINT_BASE_URL_MAP: Record<string, string> = { ... }

print(): void {
  const base = PRINT_BASE_URL_MAP[this.selectedDocType.desc]
  const id = this.selectedDoc().id
  let path: string

  if (this.selectedDocType.desc !== 'Purchase or Work Request') {
    path = `/${base}/export/${id}`
  } else {
    const rvTypeId = this.selectedDoc().rvTypeId
    if (rvTypeId === 3)      path = `/${base}/export2/${id}`   // RV_FOR_REP
    else if (rvTypeId === 4) path = `/${base}/export3/${id}`   // RV_FOR_LABOR
    else if (rvTypeId === 2) path = `/${base}/export1/${id}`   // RV_FOR_IT
    else                     path = `/${base}/export/${id}`
  }
  this.downloadSvc.print(path, { type: 'pdf' })
}
```

> Note: The `isMCT` check inside the `isReleasing` log template in the old JSP is preserved as-is (dead-code path — the two flags are mutually exclusive at runtime, but the template guard is kept for fidelity).

---

## 6. Routing

```typescript
// di.route.ts
export const DI_ROUTES: Routes = [{
  path: '',
  loadComponent: () => import('./di-main/di-main.component').then(m => m.DiMainComponent)
}]

// pages.route.ts — add:
{ path: 'di', loadChildren: () => import('./di/di.route').then(m => m.DI_ROUTES), data: { title: 'Document Inquiry' } }
```

---

## 7. Parity Checklist

- [ ] Page layout — single card, all sections inline
- [ ] Header/title — "Document Inquiry" via `app-page-title`
- [ ] Department filter — select with default to user's department
- [ ] Document Type filter — grouped select (optgroup per module)
- [ ] From/To date — flatpickr, defaults to current month
- [ ] Go! search button
- [ ] Supplier browse + clear (APV/CV only)
- [ ] Due Date filter (APV only)
- [ ] Account Code / Entry Amount / Totals filters (APV, CV, JV, CRV, SV)
- [ ] Add Filters dropdown (Status / Date / Doc Number / Particulars)
- [ ] Transacted By user filter
- [ ] Search by voucher code
- [ ] Print button (post-selection)
- [ ] Main table — Code/Date/Particulars/Status/Amount, clickable Code
- [ ] Row click → load details
- [ ] Standard details table — purchasing columns
- [ ] Standard details table — accounting columns
- [ ] Standard details table — inventory columns (with/without amount)
- [ ] Cycles table (unique by code, ordered by createdAt)
- [ ] Quotation details (3-supplier layout)
- [ ] Cost Estimate details
- [ ] Site Inspection Report details
- [ ] View Logs toggle
- [ ] Logs table — 9 type-specific log templates
- [ ] Loading state
- [ ] Empty state
- [ ] AlertService for all errors/warnings
- [ ] Backend — 7 DI endpoints wired to existing service impl
- [ ] Backend — all detail endpoints verified / supplemented
- [ ] Routing registered in pages.route.ts
- [ ] Angular 19 standalone, signals, inject(), flatpickr, v2 table classes
