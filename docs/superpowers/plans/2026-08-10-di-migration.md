# Document Inquiry (DI) — Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate the old Firefly Document Inquiry module to Firefly v2 as a full-stack Angular 19 + Spring Boot implementation, preserving all business logic while applying v2 architecture standards.

**Architecture:** Single flat `DiMainComponent` (standalone, signals, inject()) following the `GlAccountInquiryMainComponent` pattern. Backend wires the existing `DocumentInquiryServiceImpl` (already fully implemented) into a new `DocumentInquiryController`. All missing document-detail API endpoints are added directly to `DocumentInquiryController` to keep the feature self-contained and avoid touching many controllers.

**Tech Stack:** Angular 19 (standalone, signals, inject(), mwlFlatpickr, NgbModal, NgbDropdown), Spring Boot (RestController, JPA repos via constructor injection with @RequiredArgsConstructor)

## Global Constraints

- **No git commits** — user handles all version control manually
- Old Firefly is source of truth for business logic and UI behavior
- v2 is source of truth for Angular 19 architecture and standards  
- All user-facing alerts/confirmations via `AlertService` — no `alert()` or `confirm()`
- Date inputs: `mwlFlatpickr` with `{ dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' }`
- Table classes: `table table-custom table-centered table-hover w-100 mb-0`
- thead: `bg-light align-middle bg-opacity-25 thead-sm` + `tr.text-uppercase.fs-xxs`
- Buttons: `btn-success fw-bold` for Search/Browse/Print/Export; `btn-danger fw-bold` for remove/clear
- Icons: `ng-icon` with tabler names (tablerSearch, tablerPrinter, tablerRefresh, etc.)
- Browse modals: `{ size: 'lg', centered: true }` via `NgbModal`
- v2 `DocumentType` enum field note: `getDescription()` = short code (e.g. "RV"), `getCode()` = full display name (e.g. "Purchase or Work Request") — the inverse of the old Firefly enum

---

## File Map

### Created
| File | Responsibility |
|---|---|
| `frontend/src/app/pages/di/di.route.ts` | Lazy route definition |
| `frontend/src/app/pages/di/di.service.ts` | All HTTP calls for the DI module |
| `frontend/src/app/pages/di/di-main/di-main.component.ts` | All component logic + signals |
| `frontend/src/app/pages/di/di-main/di-main.component.html` | Full template |

### Modified
| File | Change |
|---|---|
| `frontend/src/app/pages/pages.route.ts` | Add `{ path: 'di', ... }` entry |
| `src/main/java/com/noreco1/fireflyv2/controller/DocumentInquiryController.java` | Implement all endpoints (was empty stub) |

---

## Verified Endpoint Map

These are confirmed v2 paths the Angular service will call:

| Purpose | v2 Endpoint | Status |
|---|---|---|
| Document types list | `GET /document-inquiry/document-types` | add to controller |
| Documents list | `GET /document-inquiry/list/{typeId}/tn/{tableName}/sd/{sd}/ed/{ed}/pt/{pt}` | add |
| Documents by user | `GET /document-inquiry/list/{typeId}/tn/{tableName}/sd/{sd}/ed/{ed}/pt/{pt}/userId/{userId}` | add |
| Search by voucher code | `GET /document-inquiry/search/{query}` | add |
| Purchase cycle | `GET /document-inquiry/purchase-cycle/{rvdId}` | add |
| Accounting cycle | `GET /document-inquiry/accounting-cycle/{transId}` | add |
| Inventory cycle | `GET /document-inquiry/inventory-cycle/{transId}` | add |
| RV details | `GET /api/rv-detail/rvd/{rvId}` | ✅ exists (PurchaseRequestDetailController) |
| Canvass details | `GET /canvass-detail/cnvsd/{canvassId}` | ✅ exists (CanvassDetailController) |
| PO details | `GET /po-detail/pod/{poId}` | ✅ exists (PoDetailController) |
| JO details | `GET /api/job-order/detail/{joId}` | ✅ exists (JobOrderController) |
| JOA details | `GET /api/jo-acceptance/detail/{joaId}` | ✅ exists (JoAcceptanceController) |
| Quotation details | `GET /quotation-detail/{quotationId}` | ✅ exists (QuotationDetailController) |
| GL entries | `GET /ledger/gl/{transId}` | ✅ exists (LedgerController) |
| Departments | `GET /json/departments` | ✅ exists (AnyJsonController) |
| User default dept | `GET /json/departments-by-user` | ✅ exists (AnyJsonController) |
| Document logs | `GET /json/document-logs/{transId}` | ✅ exists (AnyJsonController) |
| Users list | `GET /api/user/list` | ✅ exists (UserController) |
| RR detail items | `GET /document-inquiry/rr-detail/{rrId}` | add to DI controller |
| SW items | `GET /document-inquiry/sw-items/{swId}` | add to DI controller |
| SRL items | `GET /document-inquiry/srl-items/{srlId}` | add to DI controller |
| ST/SA/MCT/MST/SRC items | `GET /document-inquiry/inv-items/{transId}` | add to DI controller (shared, uses ItemTransactionDetailRepo) |
| CE details | `GET /document-inquiry/ce-details/{ceId}` | add to DI controller |
| SIR details | `GET /document-inquiry/sir-details/{sirId}` | add to DI controller |

---

## Task 1: Backend — DocumentInquiryController (core DI endpoints + document types)

**Files:**
- Modify: `src/main/java/com/noreco1/fireflyv2/controller/DocumentInquiryController.java`

**Interfaces:**
- Produces: 7 REST endpoints consumed by `DiService`
- Consumes: existing `DocumentInquiryService` (already wired via `DocumentInquiryServiceImpl`)

---

- [ ] **Step 1: Replace the empty DocumentInquiryController body**

```java
package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.DocInqListDto;
import com.noreco1.fireflyv2.model.enums.DocumentType;
import com.noreco1.fireflyv2.service.DocumentInquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/document-inquiry")
@RequiredArgsConstructor
public class DocumentInquiryController {

    private final DocumentInquiryService docInqService;

    // ── Core list endpoints ──────────────────────────────────────────────────

    @GetMapping("/document-types")
    public List<Map<String, Object>> documentTypes() {
        return buildDocumentTypes();
    }

    @GetMapping("/list/{docTypeId}/tn/{tableName}/sd/{startDate}/ed/{endDate}/pt/{particulars}")
    public List<DocInqListDto> docInqList(
            @PathVariable Integer docTypeId,
            @PathVariable String tableName,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @PathVariable String particulars,
            @RequestParam(required = false, defaultValue = "0") Integer suppId,
            @RequestParam(required = false) String dDate,
            @RequestParam(required = false) String c,
            @RequestParam(required = false) String eAmt,
            @RequestParam(required = false) String tAmt,
            @RequestParam(required = false, defaultValue = "0") Integer d) {
        return docInqService.findDocumentsByTypeIdStartDateEndDate(
                docTypeId, tableName, startDate, endDate, particulars,
                suppId, dDate, c, eAmt, tAmt, d);
    }

    @GetMapping("/list/{docTypeId}/tn/{tableName}/sd/{startDate}/ed/{endDate}/pt/{particulars}/userId/{userId}")
    public List<DocInqListDto> docInqListByUser(
            @PathVariable Integer docTypeId,
            @PathVariable String tableName,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @PathVariable String particulars,
            @PathVariable Integer userId) {
        return docInqService.findDocumentsByUserId(userId, docTypeId, tableName, startDate, endDate, particulars);
    }

    @GetMapping("/search/{query}")
    public List<DocInqListDto> search(@PathVariable String query) {
        return docInqService.findDocumentsByQuery(query);
    }

    @GetMapping("/purchase-cycle/{rvdId}")
    public List<DocInqListDto> purchaseCycle(@PathVariable Integer rvdId) {
        return docInqService.findDocumentsByRvdId(rvdId);
    }

    @GetMapping("/accounting-cycle/{transId}")
    public List<DocInqListDto> accountingCycle(@PathVariable Integer transId) {
        return docInqService.findDocumentsByTransId(transId);
    }

    @GetMapping("/inventory-cycle/{transId}")
    public List<DocInqListDto> inventoryCycle(@PathVariable Integer transId) {
        return docInqService.findInventoryDocumentsByTransId(transId);
    }

    // ── Document-types config ────────────────────────────────────────────────

    private List<Map<String, Object>> buildDocumentTypes() {
        List<Map<String, Object>> types = new ArrayList<>();
        // type: 1=Purchasing, 2=Accounting, 3=Inventory, 4=WorkOrder
        addType(types, DocumentType.RV,                    "PurchaseRequest",        "purpose",            1);
        addType(types, DocumentType.CF,                    "Canvass",                "FK_vendorAccountNo", 1);
        addType(types, DocumentType.QUOTATION_SUMMARY,     "Quotation",              "particulars",        1);
        addType(types, DocumentType.PO,                    "PurchaseOrder",          "FK_vendorAccountNo", 1);
        addType(types, DocumentType.JO,                    "JobOrder",               "FK_vendorAccountNo", 1);
        addType(types, DocumentType.JOA,                   "JoAcceptance",           "FK_vendorAccountNo", 1);
        addType(types, DocumentType.PR,                    "PaymentRequest",         "FK_vendorAccountNo", 1);
        addType(types, DocumentType.APV,                   "AccountsPayableVoucher", "particulars",        2);
        addType(types, DocumentType.CV,                    "CheckVoucher",           "particulars",        2);
        addType(types, DocumentType.JV,                    "JournalVoucher",         "explanation",        2);
        addType(types, DocumentType.CRV,                   "CashReceipts",           "particulars",        2);
        addType(types, DocumentType.SV,                    "SalesVoucher",           "particulars",        2);
        addType(types, DocumentType.RR,                    "ReceivingReport",        "purpose",            3);
        addType(types, DocumentType.SW,                    "StockWithdrawal",        "description",        3);
        addType(types, DocumentType.SRL,                   "StockRelease",           "description",        3);
        addType(types, DocumentType.ST,                    "StockTransfer",          "description",        3);
        addType(types, DocumentType.SRC,                   "StockReceive",           "description",        3);
        addType(types, DocumentType.SA,                    "StockAdjustment",        "description",        3);
        addType(types, DocumentType.MCT,                   "MaterialCreditTicket",   "description",        3);
        addType(types, DocumentType.MST,                   "MaterialSalvageTicket",  "description",        3);
        addType(types, DocumentType.CE,                    "CostEstimate",           "description",        4);
        addType(types, DocumentType.SITE_INSPECTION_REPORT,"SiteInspectionReport",   "description",        4);
        return types;
    }

    private void addType(List<Map<String, Object>> list, DocumentType dt,
                         String tableName, String pt, int type) {
        Map<String, Object> m = new LinkedHashMap<>();
        // v2 enum: getDescription()=short code e.g. "RV"; getCode()=full name e.g. "Purchase or Work Request"
        m.put("id",        dt.getId());
        m.put("code",      dt.getDescription());   // short code e.g. "RV"
        m.put("desc",      dt.getCode());           // full display name e.g. "Purchase or Work Request"
        m.put("tableName", tableName);
        m.put("pt",        pt);
        m.put("type",      type);
        m.put("module",    dt.getModule());
        m.put("order",     moduleOrder(dt.getModule()));
        list.add(m);
    }

    private int moduleOrder(String module) {
        return switch (module) {
            case "PURCHASING"      -> 1;
            case "ACCOUNTING CORE" -> 2;
            case "INVENTORY"       -> 3;
            case "WORK ORDER"      -> 4;
            default                -> 5;
        };
    }
}
```

- [ ] **Step 2: Verify the app compiles**

Run: `mvn compile -q`
Expected: BUILD SUCCESS with no errors. If `DocumentInquiryService` injection fails, confirm the bean name in `DocumentInquiryServiceImpl` is `"docInqServiceImpl"` and add `@Qualifier("docInqServiceImpl")` to the field.

- [ ] **Step 3: Test the document-types endpoint manually**

Start the app and call:
```
GET http://localhost:8080/document-inquiry/document-types
```
Expected: JSON array of 22 objects, each with `id`, `code`, `desc`, `tableName`, `pt`, `type`, `module`, `order`.
Verify first entry is RV: `{ "id": 1, "code": "RV", "desc": "Purchase or Work Request", "tableName": "PurchaseRequest", "pt": "purpose", "type": 1, "module": "PURCHASING", "order": 1 }`.

- [ ] **Step 4: Test the list endpoint manually**

```
GET http://localhost:8080/document-inquiry/list/1/tn/PurchaseRequest/sd/2026-01-01/ed/2026-08-10/pt/purpose?d=0
```
Expected: JSON array of `DocInqListDto` objects (may be empty if no data, but no 500 error).

---

## Task 2: Backend — DocumentInquiryController (document detail endpoints)

All missing inventory/work-order detail endpoints are added directly to `DocumentInquiryController` to keep the feature self-contained. The controller injects only the repos needed.

**Files:**
- Modify: `src/main/java/com/noreco1/fireflyv2/controller/DocumentInquiryController.java`

**Interfaces:**
- Produces: 6 additional GET endpoints consumed by `DiService`
- Consumes: `ReceivingReportDetailRepo`, `StockWithdrawalDetailRepo`, `StockReleaseDetailRepo`, `ItemTransactionDetailRepo`, `CostEstimateDetailRepo`, `SiteInspectionReportDescriptionRepo`

---

- [ ] **Step 1: Add repo fields and detail endpoints to DocumentInquiryController**

Replace the class-level `@RequiredArgsConstructor` fields section and add the following imports + fields + endpoints. Preserve existing code — add to it:

**Imports to add:**
```java
import com.noreco1.fireflyv2.repo.CostEstimateDetailRepo;
import com.noreco1.fireflyv2.repo.ItemTransactionDetailRepo;
import com.noreco1.fireflyv2.repo.ReceivingReportDetailRepo;
import com.noreco1.fireflyv2.repo.SiteInspectionReportDescriptionRepo;
import com.noreco1.fireflyv2.repo.StockReleaseDetailRepo;
import com.noreco1.fireflyv2.repo.StockWithdrawalDetailRepo;
```

**Additional fields** (Lombok @RequiredArgsConstructor injects all `final` fields):
```java
private final ReceivingReportDetailRepo rrDetailRepo;
private final StockWithdrawalDetailRepo swDetailRepo;
private final StockReleaseDetailRepo srlDetailRepo;
private final ItemTransactionDetailRepo itemTransactionDetailRepo;
private final CostEstimateDetailRepo ceDetailRepo;
private final SiteInspectionReportDescriptionRepo sirDescRepo;
```

**New endpoints to add inside the class:**
```java
// ── Document detail endpoints ─────────────────────────────────────────────

@GetMapping("/rr-detail/{rrId}")
public List<?> rrDetail(@PathVariable Integer rrId) {
    return rrDetailRepo.findByReceivingReportId(rrId);
}

@GetMapping("/sw-items/{swId}")
public List<?> swItems(@PathVariable Integer swId) {
    return swDetailRepo.findByStockWithdrawalId(swId);
}

@GetMapping("/srl-items/{srlId}")
public List<?> srlItems(@PathVariable Integer srlId) {
    return srlDetailRepo.findByStockReleaseId(srlId);
}

// Used for ST, SA, MCT, MST, SRC — all share ItemTransactionDetail keyed by transId
@GetMapping("/inv-items/{transId}")
public List<?> invItems(@PathVariable Integer transId) {
    return itemTransactionDetailRepo.findByTransactionId(transId);
}

@GetMapping("/ce-details/{ceId}")
public List<?> ceDetails(@PathVariable Integer ceId) {
    return ceDetailRepo.findByCostEstimateId(ceId);
}

@GetMapping("/sir-details/{sirId}")
public List<?> sirDetails(@PathVariable Integer sirId) {
    return sirDescRepo.findBySiteInspectionReportId(sirId);
}
```

- [ ] **Step 2: Verify the app compiles**

Run: `mvn compile -q`
Expected: BUILD SUCCESS. If a repo is not found by name, check the import path. All repos are under `com.noreco1.fireflyv2.repo`.

- [ ] **Step 3: Check for lazy-loading issues**

If any detail endpoint returns a 500 with `LazyInitializationException`, add `@Transactional(readOnly = true)` to the endpoint method and add `import org.springframework.transaction.annotation.Transactional`.

Example fix for `rrDetail`:
```java
@Transactional(readOnly = true)
@GetMapping("/rr-detail/{rrId}")
public List<?> rrDetail(@PathVariable Integer rrId) {
    return rrDetailRepo.findByReceivingReportId(rrId);
}
```

Apply the same to any endpoint that triggers it.

---

## Task 3: Frontend — Route, Service, and Pages Route Registration

**Files:**
- Create: `frontend/src/app/pages/di/di.route.ts`
- Create: `frontend/src/app/pages/di/di.service.ts`
- Modify: `frontend/src/app/pages/pages.route.ts`

**Interfaces:**
- `DiService` produces observables consumed by `DiMainComponent`

---

- [ ] **Step 1: Create `di.route.ts`**

```typescript
// frontend/src/app/pages/di/di.route.ts
import { Routes } from '@angular/router';

export const DI_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./di-main/di-main.component').then(m => m.DiMainComponent)
    }
];
```

- [ ] **Step 2: Create `di.service.ts`**

```typescript
// frontend/src/app/pages/di/di.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE = environment.get('baseApiUrl');

// Print base URL map — from old Firefly voucherUtil.setBaseUrlForPrint()
export const PRINT_BASE_MAP: Record<string, string> = {
    'Accounts Payable':          'accounts-payable',
    'Journal Voucher':           'journal-voucher',
    'Check Voucher':             'check-voucher',
    'Purchase or Work Request':  'requisition-voucher',
    'Canvass':                   'canvass-rv',
    'Purchase Order':            'purchase-order',
    'Job Order':                 'job-order',
    'JO Acceptance':             'jo-acceptance',
    'Payment Request':           'payment-request',
    'Summary of Quotation':      'quotation',
    'Sales Voucher':             'sales-voucher-mgt',
    'Cash Receipts':             'cash-receipts-mgt',
    'Receiving Report':          'receiving-report-mgt',
    'Stock Withdrawal':          'inventory/withdrawal',
    'Stock Release':             'inventory/releasing',
    'Stock Adjustment':          'inventory/stock-adjustment-mgt',
    'Stock Transfer':            'inventory/stock-transfer-mgt',
    'Stock Receive':             'inventory/receiving-mgt',
    'Material Credit Ticket':    'inventory/mct-mgt',
    'Material Salvage Ticket':   'inventory/mst-mgt',
    'Site Inspection Report':    'site-inspection-report',
    'Cost Estimate':             'cost-estimate-mgt',
};

// RV print type constants — from old Firefly firefly.js
export const RV_FOR_IT    = 2;
export const RV_FOR_REP   = 3;
export const RV_FOR_LABOR = 4;

@Injectable({ providedIn: 'root' })
export class DiService {
    private http = inject(HttpClient);

    // ── Doc types + departments + users ──────────────────────────────────────
    getDocumentTypes(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/document-inquiry/document-types`);
    }
    getDepartments(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/json/departments`);
    }
    getUserDefaultDepartment(): Observable<any> {
        return this.http.get<any>(`${BASE}/json/departments-by-user`);
    }
    getUsers(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/api/user/list`);
    }

    // ── Document list ─────────────────────────────────────────────────────────
    getDocuments(
        typeId: number, tableName: string, startDate: string, endDate: string, pt: string,
        suppId = 0, dDate = '', c = '', eAmt = '', tAmt = '', d = 0
    ): Observable<any[]> {
        return this.http.get<any[]>(
            `${BASE}/document-inquiry/list/${typeId}/tn/${tableName}/sd/${startDate}/ed/${endDate}/pt/${pt}`,
            { params: { suppId, dDate, c, eAmt, tAmt, d } as any }
        );
    }
    getDocumentsByUser(
        typeId: number, tableName: string, startDate: string, endDate: string, pt: string, userId: number
    ): Observable<any[]> {
        return this.http.get<any[]>(
            `${BASE}/document-inquiry/list/${typeId}/tn/${tableName}/sd/${startDate}/ed/${endDate}/pt/${pt}/userId/${userId}`
        );
    }
    searchDocuments(query: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/document-inquiry/search/${query}`);
    }

    // ── Cycles ────────────────────────────────────────────────────────────────
    getPurchaseCycle(rvdId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/document-inquiry/purchase-cycle/${rvdId}`);
    }
    getAccountingCycle(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/document-inquiry/accounting-cycle/${transId}`);
    }
    getInventoryCycle(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/document-inquiry/inventory-cycle/${transId}`);
    }

    // ── Purchasing details ────────────────────────────────────────────────────
    getRvDetails(rvId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/api/rv-detail/rvd/${rvId}`);
    }
    getCanvassDetails(canvassId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/canvass-detail/cnvsd/${canvassId}`);
    }
    getPoDetails(poId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/po-detail/pod/${poId}`);
    }
    getJobOrderDetails(joId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/api/job-order/detail/${joId}`);
    }
    getJoaDetails(joaId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/api/jo-acceptance/detail/${joaId}`);
    }
    getQuotationDetails(quotationId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/quotation-detail/${quotationId}`);
    }

    // ── Accounting details ────────────────────────────────────────────────────
    getGlEntries(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/ledger/gl/${transId}`);
    }

    // ── Inventory details ─────────────────────────────────────────────────────
    getRrDetails(rrId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/document-inquiry/rr-detail/${rrId}`);
    }
    getSwItems(swId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/document-inquiry/sw-items/${swId}`);
    }
    getSrlItems(srlId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/document-inquiry/srl-items/${srlId}`);
    }
    // ST, SA, MCT, MST, SRC all use this — pass docInq.transId
    getInvItems(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/document-inquiry/inv-items/${transId}`);
    }

    // ── Work order details ────────────────────────────────────────────────────
    getCeDetails(ceId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/document-inquiry/ce-details/${ceId}`);
    }
    getSirDetails(sirId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/document-inquiry/sir-details/${sirId}`);
    }

    // ── Logs ──────────────────────────────────────────────────────────────────
    getLogs(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/json/document-logs/${transId}`);
    }
}
```

- [ ] **Step 3: Register the route in `pages.route.ts`**

Open `frontend/src/app/pages/pages.route.ts`. Find the `gl-account-inquiry` block and add the `di` entry near it (inquiry modules together):

```typescript
{
    path: 'di',
    loadChildren: () => import('./di/di.route').then(m => m.DI_ROUTES),
    data: { title: 'Document Inquiry' },
},
```

- [ ] **Step 4: Verify TypeScript compiles**

Run: `cd frontend && npx ng build --configuration=development 2>&1 | head -40`
Expected: No errors related to `DiService` or `DI_ROUTES`.

---

## Task 4: Frontend — DiMainComponent Shell + Init + Primary Filters

Creates the component file with all state declarations, `ngOnInit`, and the primary filter row (department, doc type, date range, Go button).

**Files:**
- Create: `frontend/src/app/pages/di/di-main/di-main.component.ts`
- Create: `frontend/src/app/pages/di/di-main/di-main.component.html` (stub for now)

---

- [ ] **Step 1: Create `di-main.component.ts`**

```typescript
// frontend/src/app/pages/di/di-main/di-main.component.ts
import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { NgbDropdownModule, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { DiService } from '../di.service';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { BrowseSupplierModalComponent } from '@/app/shared/modals/browse-supplier-modal/browse-supplier-modal.component';
import { PRINT_BASE_MAP, RV_FOR_IT, RV_FOR_REP, RV_FOR_LABOR } from '../di.service';

// DocumentType IDs (from v2 DocumentType enum)
const APV_ID  = 4;
const CV_ID   = 5;
const JV_ID   = 6;
const CRV_ID  = 9;
const SV_ID   = 13;
const RV_ID   = 1;
const CF_ID   = 20;   // Canvass
const PO_ID   = 2;
const JO_ID   = 10;
const JOA_ID  = 21;
const PR_ID   = 22;
const QS_ID   = 32;   // Quotation Summary
const RR_ID   = 3;
const SW_ID   = 8;
const SRL_ID  = 11;
const ST_ID   = 35;
const SA_ID   = 18;
const MCT_ID  = 33;
const MST_ID  = 34;
const SRC_ID  = 36;
const CE_ID   = 40;
const SIR_ID  = 39;

@Component({
    selector: 'app-di-main',
    standalone: true,
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective,
        NgbDropdownModule,
    ],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './di-main.component.html',
})
export class DiMainComponent {
    module   = 'Document Inquiry';
    menuLink = 'di';

    private svc           = inject(DiService);
    private modalService  = inject(NgbModal);
    private downloadSvc   = inject(DownloadService);
    private alertService  = inject(AlertService);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // ── Primary filters ────────────────────────────────────────────────────
    fromDate            = '';
    toDate              = '';
    selectedDepartment: any = null;
    selectedDocType:    any = null;

    // ── Supplier filter (APV / CV) ─────────────────────────────────────────
    selectedSupplier:   any = null;
    dueDate             = '';

    // ── Account/amount filters (APV, CV, JV, CRV, SV) ─────────────────────
    codeFilter          = '';
    amountEntryFilter   = '';
    totalsFilter        = '';

    // ── Toolbar state ──────────────────────────────────────────────────────
    voucherQuery        = '';
    selectedUser:       any = null;
    filterStatus        = false;
    filterDate          = false;
    filterDocNo         = false;
    filterParticulars   = false;
    showUserFilter      = false;

    // ── Doc type flags (set on selectDocument) ─────────────────────────────
    forPurchasing       = true;
    forInventory        = false;
    rv                  = true;     // show Amount col in purchasing table (false for RV, Canvass)
    isReceivingReport   = false;
    isWithdrawal        = false;
    isReleasing         = false;
    isStockTransfer     = false;
    isReceiving         = false;
    isStockAdjustment   = false;
    isMCT               = false;
    isMST               = false;
    showQuotationDetails        = false;
    showCostEstimateDetails     = false;
    showSiteInspectionDetails   = false;
    showInventoryDetailAmount   = true;

    // ── Signals ────────────────────────────────────────────────────────────
    docTypes            = signal<any[]>([]);
    departments         = signal<any[]>([]);
    users               = signal<any[]>([]);
    docList             = signal<any[]>([]);
    selectedDoc         = signal<any>(null);
    docInqDetails       = signal<any[]>([]);
    cycles              = signal<any[]>([]);
    quotationDetails    = signal<any[]>([]);
    quotationSuppliers  = signal<any[]>([]);
    costEstimateDetails = signal<any[]>([]);
    siteInspectionDetails = signal<any[]>([]);
    logs                = signal<any[]>([]);
    isLoading           = signal(false);
    showFilters         = signal(false);
    showPrint           = signal(false);
    showLogs            = signal(false);

    // ── Computed visibility ────────────────────────────────────────────────
    get showAccountFilters(): boolean {
        return [APV_ID, CV_ID, JV_ID, CRV_ID, SV_ID].includes(this.selectedDocType?.id);
    }
    get showSupplierFilter(): boolean {
        return [APV_ID, CV_ID].includes(this.selectedDocType?.id);
    }
    get showDueDateFilter(): boolean {
        return this.selectedDocType?.id === APV_ID;
    }

    // ── Grouped doc types for <optgroup> rendering ─────────────────────────
    get docTypeGroups(): { module: string; types: any[] }[] {
        const map = new Map<string, any[]>();
        for (const dt of this.docTypes()) {
            if (!map.has(dt.module)) map.set(dt.module, []);
            map.get(dt.module)!.push(dt);
        }
        // Sort groups by order field of first item in group
        return [...map.entries()]
            .sort((a, b) => (a[1][0]?.order ?? 0) - (b[1][0]?.order ?? 0))
            .map(([module, types]) => ({ module, types }));
    }

    // ── Filtered doc list (client-side column filters) ─────────────────────
    get filteredDocList(): any[] {
        let rows = this.docList();
        if (this.filterStatus && this._statusFilter)
            rows = rows.filter(r => r.status?.toLowerCase().includes(this._statusFilter.toLowerCase()));
        if (this.filterDate && this._dateFilter)
            rows = rows.filter(r => (r.voucherDate ?? '').toString().includes(this._dateFilter));
        if (this.filterDocNo && this._docNoFilter)
            rows = rows.filter(r => r.localCode?.toLowerCase().includes(this._docNoFilter.toLowerCase()));
        if (this.filterParticulars && this._particularsFilter)
            rows = rows.filter(r => r.particulars?.toLowerCase().includes(this._particularsFilter.toLowerCase()));
        return rows;
    }
    _statusFilter      = '';
    _dateFilter        = '';
    _docNoFilter       = '';
    _particularsFilter = '';

    // ── Lifecycle ──────────────────────────────────────────────────────────
    ngOnInit(): void {
        const now   = new Date();
        const first = new Date(now.getFullYear(), now.getMonth(), 1);
        this.fromDate = first.toISOString().substring(0, 10);
        this.toDate   = now.toISOString().substring(0, 10);

        this.svc.getDocumentTypes().subscribe({ next: data => this.docTypes.set(data) });
        this.svc.getDepartments().subscribe({ next: data => this.departments.set(data) });
        this.svc.getUsers().subscribe({ next: data => this.users.set(data) });
        this.svc.getUserDefaultDepartment().subscribe({
            next: data => {
                if (data && data.length > 0) this.selectedDepartment = data[0];
                else if (data && data.id) this.selectedDepartment = data;
            }
        });
    }

    // ── Doc type change ────────────────────────────────────────────────────
    onDocTypeChange(): void {
        this.docList.set([]);
        this.selectedDoc.set(null);
        this.docInqDetails.set([]);
        this.cycles.set([]);
        this.logs.set([]);
        this.showFilters.set(false);
        this.showPrint.set(false);
        this.showLogs.set(false);
        this.resetDetailFlags();
    }

    private resetDetailFlags(): void {
        this.forPurchasing = true;
        this.forInventory = false;
        this.rv = true;
        this.isReceivingReport = false;
        this.isWithdrawal = false;
        this.isReleasing = false;
        this.isStockTransfer = false;
        this.isReceiving = false;
        this.isStockAdjustment = false;
        this.isMCT = false;
        this.isMST = false;
        this.showQuotationDetails = false;
        this.showCostEstimateDetails = false;
        this.showSiteInspectionDetails = false;
        this.quotationDetails.set([]);
        this.quotationSuppliers.set([]);
        this.costEstimateDetails.set([]);
        this.siteInspectionDetails.set([]);
    }
}
```

- [ ] **Step 2: Create the stub template `di-main.component.html`**

```html
<!-- frontend/src/app/pages/di/di-main/di-main.component.html -->
<div class="container-fluid">
    <app-page-title [title]="module" [subTitle]="''" [menuLink]="menuLink"/>
</div>

<div class="container-fluid">
    <div class="card">
        <div class="card-body">
            <!-- Content added in later tasks -->
            <p class="text-muted">Loading...</p>
        </div>
    </div>
</div>
```

- [ ] **Step 3: Verify TypeScript compiles with no errors**

Run: `cd frontend && npx ng build --configuration=development 2>&1 | grep -i error | head -20`
Expected: no errors. If `DownloadService` import path is wrong, find it with:
`find frontend/src -name "download.service.ts" | head -3`
Then fix the import path.

---

## Task 5: Frontend — Primary Filters + Main Table + Search

Implements the filter row, main results table, and voucher code search in the template.

**Files:**
- Modify: `frontend/src/app/pages/di/di-main/di-main.component.ts` (add `search()` and `searchByQuery()`)
- Modify: `frontend/src/app/pages/di/di-main/di-main.component.html`

---

- [ ] **Step 1: Add `search()` and `searchByQuery()` to the component**

```typescript
// Add inside DiMainComponent class

search(): void {
    if (!this.selectedDocType) {
        this.alertService.error(this.module, 'Validation', 'Please select a document type.');
        return;
    }
    const deptId = this.selectedDepartment?.id ?? 0;
    const suppId = this.selectedSupplier?.id ?? 0;
    const dDate  = this.showDueDateFilter ? this.dueDate : '';
    const c      = this.showAccountFilters ? this.codeFilter : '';
    const eAmt   = this.showAccountFilters ? this.amountEntryFilter : '';
    const tAmt   = this.showAccountFilters ? this.totalsFilter : '';

    this.isLoading.set(true);
    this.docList.set([]);
    this.selectedDoc.set(null);
    this.docInqDetails.set([]);
    this.cycles.set([]);

    // Set forPurchasing / forInventory / rv flags from docType config
    const typeConfig = this.selectedDocType;
    if (typeConfig.type === 1) {
        this.forPurchasing = true;
        this.forInventory  = false;
        // rv=false hides Amount column for RV and Canvass (no line amounts at header level)
        this.rv = typeConfig.id !== RV_ID && typeConfig.id !== CF_ID;
    } else if (typeConfig.type === 2) {
        this.forPurchasing = false;
        this.forInventory  = false;
    } else {
        // type 3 (inventory) and type 4 (work order) → forInventory=true
        this.forPurchasing = false;
        this.forInventory  = true;
    }

    this.svc.getDocuments(
        typeConfig.id, typeConfig.tableName, this.fromDate, this.toDate, typeConfig.pt,
        suppId, dDate, c, eAmt, tAmt, deptId
    ).subscribe({
        next: data => { this.docList.set(data ?? []); this.isLoading.set(false); this.showFilters.set(true); },
        error: ()  => { this.alertService.error(this.module, 'Load', 'Failed to load documents.'); this.isLoading.set(false); }
    });
}

searchByQuery(): void {
    if (!this.selectedDocType) {
        this.alertService.error(this.module, 'Validation', 'Please select a document type first.');
        return;
    }
    if (!this.voucherQuery.trim()) {
        this.alertService.warning(this.module, 'Validation', 'Search field is empty.');
        return;
    }
    this.isLoading.set(true);
    this.svc.searchDocuments(this.voucherQuery.trim()).subscribe({
        next: data => { this.docList.set(data ?? []); this.isLoading.set(false); this.showFilters.set(true); },
        error: ()  => { this.alertService.error(this.module, 'Load', 'Failed to search documents.'); this.isLoading.set(false); }
    });
}

filterByUser(): void {
    if (!this.selectedUser || !this.selectedDocType) return;
    this.isLoading.set(true);
    this.svc.getDocumentsByUser(
        this.selectedDocType.id, this.selectedDocType.tableName,
        this.fromDate, this.toDate, this.selectedDocType.pt,
        this.selectedUser.id
    ).subscribe({
        next: data => { this.docList.set(data ?? []); this.isLoading.set(false); },
        error: ()  => { this.alertService.error(this.module, 'Load', 'Failed to load documents.'); this.isLoading.set(false); }
    });
}
```

- [ ] **Step 2: Replace `di-main.component.html` with the full filter + table template**

```html
<div class="container-fluid">
    <app-page-title [title]="module" [subTitle]="''" [menuLink]="menuLink"/>
</div>

<div class="container-fluid">
    <div class="card">
        <div class="card-body">

            <!-- Row 1: Primary Filters -->
            <div class="row g-2 align-items-end mb-2">
                <div class="col-md-auto">
                    <label class="form-label fw-bold mb-1">Department</label>
                    <select class="form-select" [(ngModel)]="selectedDepartment">
                        <option [ngValue]="null">-- All --</option>
                        @for (dept of departments(); track dept.id) {
                            <option [ngValue]="dept">{{ dept.name }}</option>
                        }
                    </select>
                </div>
                <div class="col-md-auto">
                    <label class="form-label fw-bold mb-1">Document <span class="text-danger">*</span></label>
                    <select class="form-select" [(ngModel)]="selectedDocType" (ngModelChange)="onDocTypeChange()">
                        <option [ngValue]="null">-- Select --</option>
                        @for (group of docTypeGroups; track group.module) {
                            <optgroup [label]="group.module">
                                @for (dt of group.types; track dt.id) {
                                    <option [ngValue]="dt">{{ dt.desc }}</option>
                                }
                            </optgroup>
                        }
                    </select>
                </div>
                <div class="col-md-auto">
                    <label class="form-label fw-bold mb-1">From</label>
                    <input type="text" class="form-control" mwlFlatpickr [options]="flatpickrOptions"
                           [(ngModel)]="fromDate" placeholder="From date"/>
                </div>
                <div class="col-md-auto">
                    <label class="form-label fw-bold mb-1">To</label>
                    <input type="text" class="form-control" mwlFlatpickr [options]="flatpickrOptions"
                           [(ngModel)]="toDate" placeholder="To date"/>
                </div>
                <div class="col-md-auto">
                    <button type="button" class="btn btn-success fw-bold" (click)="search()">
                        <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Go!
                    </button>
                </div>
            </div>

            <!-- Conditional filters added in Task 6 -->

            <!-- Row: Toolbar (shown after first search) -->
            @if (showFilters()) {
                <div class="row g-2 align-items-center mb-2 mt-1">
                    <!-- Add Filter dropdown — wired in Task 7 -->
                    <div class="col-md-auto">
                        <div class="dropdown" ngbDropdown>
                            <button class="btn btn-success fw-bold dropdown-toggle" ngbDropdownToggle type="button">
                                <ng-icon name="tablerFilter" class="ps-0 pe-2 fw-bold"></ng-icon>Add Filter
                            </button>
                            <div class="dropdown-menu p-2" ngbDropdownMenu style="min-width:200px;">
                                <div class="form-check">
                                    <input class="form-check-input" type="checkbox" id="fStatus"
                                           [(ngModel)]="filterStatus"/>
                                    <label class="form-check-label" for="fStatus">Status</label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input" type="checkbox" id="fDate"
                                           [(ngModel)]="filterDate"/>
                                    <label class="form-check-label" for="fDate">Date</label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input" type="checkbox" id="fDocNo"
                                           [(ngModel)]="filterDocNo"/>
                                    <label class="form-check-label" for="fDocNo">Document Number</label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input" type="checkbox" id="fPart"
                                           [(ngModel)]="filterParticulars"/>
                                    <label class="form-check-label" for="fPart">Particulars</label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input" type="checkbox" id="fUser"
                                           [(ngModel)]="showUserFilter"/>
                                    <label class="form-check-label" for="fUser">Transacted By</label>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Print button -->
                    @if (showPrint()) {
                        <div class="col-md-auto">
                            <button type="button" class="btn btn-success fw-bold" (click)="print()">
                                <ng-icon name="tablerPrinter" class="ps-0 pe-2 fw-bold"></ng-icon>Print
                            </button>
                        </div>
                    }

                    <!-- Transacted By select -->
                    @if (showUserFilter) {
                        <div class="col-md-auto">
                            <select class="form-select" [(ngModel)]="selectedUser" (ngModelChange)="filterByUser()">
                                <option [ngValue]="null">-- Transacted By --</option>
                                @for (u of users(); track u.id) {
                                    <option [ngValue]="u">{{ u.fullName }}</option>
                                }
                            </select>
                        </div>
                    }

                    <!-- Column filter inputs (shown per checkbox) -->
                    @if (filterStatus) {
                        <div class="col-md-auto">
                            <input type="text" class="form-control form-control-sm" placeholder="Filter Status"
                                   [(ngModel)]="_statusFilter"/>
                        </div>
                    }
                    @if (filterDate) {
                        <div class="col-md-auto">
                            <input type="text" class="form-control form-control-sm" placeholder="Filter Date"
                                   [(ngModel)]="_dateFilter"/>
                        </div>
                    }
                    @if (filterDocNo) {
                        <div class="col-md-auto">
                            <input type="text" class="form-control form-control-sm" placeholder="Filter Doc No."
                                   [(ngModel)]="_docNoFilter"/>
                        </div>
                    }
                    @if (filterParticulars) {
                        <div class="col-md-auto">
                            <input type="text" class="form-control form-control-sm" placeholder="Filter Particulars"
                                   [(ngModel)]="_particularsFilter"/>
                        </div>
                    }

                    <!-- Search by voucher code -->
                    <div class="col-md-auto ms-auto">
                        <div class="input-group">
                            <input type="text" class="form-control" placeholder="Search by voucher code"
                                   [(ngModel)]="voucherQuery" (keyup.enter)="searchByQuery()"/>
                            <button type="button" class="btn btn-success fw-bold" (click)="searchByQuery()">
                                <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Go!
                            </button>
                        </div>
                    </div>
                </div>
            }

            <!-- Main Table -->
            <div class="table-responsive">
                <table class="table table-custom table-centered table-hover w-100 mb-0">
                    <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                        <tr class="text-uppercase fs-xxs">
                            <th>Code</th>
                            <th>Voucher Date</th>
                            <th>Particulars</th>
                            <th>Status</th>
                            <th class="text-end">Amount</th>
                        </tr>
                    </thead>
                    <tbody>
                        @if (isLoading()) {
                            <tr><td colspan="5" class="text-center text-muted py-4">
                                <span class="spinner-border spinner-border-sm me-2" role="status"></span>Loading...
                            </td></tr>
                        } @else if (filteredDocList.length === 0) {
                            <tr><td colspan="5" class="text-center text-muted py-4">No records found.</td></tr>
                        } @else {
                            @for (row of filteredDocList; track row.id) {
                                <tr [class.table-active]="selectedDoc()?.id === row.id"
                                    style="cursor:pointer" (click)="selectDocument(row)">
                                    <td><a class="text-primary fw-semibold">{{ row.localCode }}</a></td>
                                    <td>{{ row.voucherDate | date:'MMM d, yyyy' }}</td>
                                    <td>{{ row.particulars }}</td>
                                    <td>{{ row.status }}</td>
                                    <td class="text-end">{{ row.amount ? (row.amount | number:'1.2-2') : '' }}</td>
                                </tr>
                            }
                        }
                    </tbody>
                </table>
            </div>

            <!-- Detail panels added in Tasks 6–9 -->

        </div>
    </div>
</div>
```

- [ ] **Step 3: Verify the page loads in the browser**

Navigate to `/di`. Expected: page renders with the filter form and empty table. Department and Document Type selects are populated from API. Console should have no errors.

---

## Task 6: Frontend — Conditional Filters (Supplier, Due Date, Account/Amount)

**Files:**
- Modify: `frontend/src/app/pages/di/di-main/di-main.component.ts` (add `browseSupplier`, `removeSupplier`)
- Modify: `frontend/src/app/pages/di/di-main/di-main.component.html`

---

- [ ] **Step 1: Add supplier browse methods to the component**

```typescript
// Add inside DiMainComponent class

browseSupplier(): void {
    const ref = this.modalService.open(BrowseSupplierModalComponent, { size: 'lg', centered: true });
    ref.result.then((supplier) => {
        if (supplier) this.selectedSupplier = supplier;
    }, () => {});
}

removeSupplier(): void {
    this.selectedSupplier = null;
}
```

- [ ] **Step 2: Insert conditional filter rows in the template after Row 1 (primary filters)**

Insert this block in `di-main.component.html` immediately after the closing `</div>` of Row 1:

```html
<!-- Row 2: Supplier + Due Date (APV / CV only) -->
@if (showSupplierFilter) {
    <div class="row g-2 align-items-end mb-2">
        <div class="col-md-4">
            <label class="form-label fw-bold mb-1">Supplier</label>
            <div class="input-group">
                <input type="text" class="form-control" readonly
                       [value]="selectedSupplier?.name ?? ''"
                       placeholder="Browse supplier..."/>
                @if (!selectedSupplier) {
                    <button type="button" class="btn btn-success fw-bold" (click)="browseSupplier()">
                        <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                    </button>
                } @else {
                    <button type="button" class="btn btn-danger fw-bold" (click)="removeSupplier()">
                        <ng-icon name="tablerRefresh" class="ps-0 pe-2 fw-bold"></ng-icon>Remove
                    </button>
                }
            </div>
        </div>
        @if (showDueDateFilter) {
            <div class="col-md-auto">
                <label class="form-label fw-bold mb-1">Due Date</label>
                <input type="text" class="form-control" mwlFlatpickr [options]="flatpickrOptions"
                       [(ngModel)]="dueDate" placeholder="Due date"/>
            </div>
        }
    </div>
}

<!-- Row 3: Account/Amount Filters (APV, CV, JV, CRV, SV) -->
@if (showAccountFilters) {
    <div class="row g-2 align-items-end mb-2">
        <div class="col-md-3">
            <label class="form-label fw-bold mb-1">Account Code</label>
            <input type="text" class="form-control" placeholder="Search account code"
                   [(ngModel)]="codeFilter"/>
        </div>
        <div class="col-md-3">
            <label class="form-label fw-bold mb-1">Entry Amount</label>
            <input type="number" class="form-control" placeholder="Search entry amount"
                   [(ngModel)]="amountEntryFilter"/>
        </div>
        <div class="col-md-3">
            <label class="form-label fw-bold mb-1">Totals</label>
            <input type="number" class="form-control" placeholder="Search totals"
                   [(ngModel)]="totalsFilter"/>
        </div>
    </div>
}
```

- [ ] **Step 3: Verify conditional rows appear correctly**

Select APV from the Document Type dropdown — both supplier row and account filters should appear. Select RV — neither should appear. Select CV — supplier row appears but no Due Date.

---

## Task 7: Frontend — Row Selection + Standard Detail Panels + Cycles

**Files:**
- Modify: `frontend/src/app/pages/di/di-main/di-main.component.ts`
- Modify: `frontend/src/app/pages/di/di-main/di-main.component.html`

---

- [ ] **Step 1: Add `selectDocument()`, `setDetails()`, `setInventoryDetails()`, and cycle loaders to the component**

```typescript
// Add inside DiMainComponent class

selectDocument(doc: any): void {
    this.selectedDoc.set(doc);
    this.showPrint.set(true);
    this.docInqDetails.set([]);
    this.cycles.set([]);
    this.logs.set([]);
    this.showLogs.set(false);

    // Reset special-panel flags
    this.isReceivingReport = false;
    this.isWithdrawal      = false;
    this.isReleasing       = false;
    this.isStockTransfer   = false;
    this.isReceiving       = false;
    this.isStockAdjustment = false;
    this.isMCT             = false;
    this.isMST             = false;
    this.showQuotationDetails      = false;
    this.showCostEstimateDetails   = false;
    this.showSiteInspectionDetails = false;
    this.quotationDetails.set([]);
    this.quotationSuppliers.set([]);
    this.costEstimateDetails.set([]);
    this.siteInspectionDetails.set([]);

    const id      = doc.id;
    const transId = doc.transId;
    const typeId  = this.selectedDocType?.id;

    // Auto-load logs
    if (transId) this.loadLogs(transId);

    // Route to appropriate detail loader
    if (typeId === RV_ID) {
        this.svc.getRvDetails(id).subscribe({ next: d => { this.setDetails(d); this.loadPurchaseCycles(this.docInqDetails()); } });
    } else if (typeId === CF_ID) {
        this.svc.getCanvassDetails(id).subscribe({ next: d => { this.setDetails(d); this.loadPurchaseCycles(this.docInqDetails()); } });
    } else if (typeId === PO_ID) {
        this.svc.getPoDetails(id).subscribe({ next: d => { this.setDetails(d); this.loadPurchaseCycles(this.docInqDetails()); } });
    } else if (typeId === JO_ID) {
        this.svc.getJobOrderDetails(id).subscribe({ next: d => { this.setDetails(d); this.loadPurchaseCycles(this.docInqDetails()); } });
    } else if (typeId === JOA_ID) {
        this.svc.getJoaDetails(id).subscribe({ next: d => { this.setDetails(d); this.loadPurchaseCycles(this.docInqDetails()); } });
    } else if (typeId === PR_ID) {
        // PR uses fkId (JOA id) for details
        this.svc.getJoaDetails(doc.fkId).subscribe({ next: d => { this.setDetails(d); this.loadPurchaseCycles(this.docInqDetails()); } });
    } else if (typeId === QS_ID) {
        this.svc.getQuotationDetails(id).subscribe({
            next: d => {
                this.setDetails(d);
                this.loadQuotation(id);
                this.loadPurchaseCycles(this.docInqDetails());
            }
        });
    } else if (typeId === RR_ID) {
        this.isReceivingReport = true;
        this.svc.getRrDetails(id).subscribe({ next: d => { this.setInventoryDetails(d); this.loadInventoryCycles(transId); } });
    } else if (typeId === SW_ID) {
        this.isWithdrawal = true;
        this.svc.getSwItems(id).subscribe({ next: d => { this.setInventoryDetails(d); this.loadInventoryCycles(transId); } });
    } else if (typeId === SRL_ID) {
        this.isReleasing = true;
        this.svc.getSrlItems(id).subscribe({ next: d => { this.setInventoryDetails(d); this.loadInventoryCycles(transId); } });
    } else if (typeId === ST_ID) {
        this.isStockTransfer = true;
        this.svc.getInvItems(transId).subscribe({ next: d => { this.setInventoryDetails(d); this.loadInventoryCycles(transId); } });
    } else if (typeId === SA_ID) {
        this.isStockAdjustment = true;
        this.svc.getInvItems(transId).subscribe({ next: d => { this.setInventoryDetails(d); this.loadInventoryCycles(transId); } });
    } else if (typeId === MCT_ID) {
        this.isMCT = true;
        this.svc.getInvItems(transId).subscribe({ next: d => { this.setInventoryDetails(d); this.loadInventoryCycles(transId); } });
    } else if (typeId === MST_ID) {
        this.isMST = true;
        this.svc.getInvItems(transId).subscribe({ next: d => { this.setInventoryDetails(d); this.loadInventoryCycles(transId); } });
    } else if (typeId === SRC_ID) {
        this.isReceiving = true;
        this.svc.getInvItems(transId).subscribe({ next: d => { this.setInventoryDetails(d); this.loadInventoryCycles(transId); } });
    } else if (typeId === CE_ID) {
        this.showCostEstimateDetails = true;
        this.svc.getCeDetails(id).subscribe({ next: d => this.costEstimateDetails.set(d ?? []) });
    } else if (typeId === SIR_ID) {
        this.showSiteInspectionDetails = true;
        this.svc.getSirDetails(id).subscribe({ next: d => this.siteInspectionDetails.set(d ?? []) });
    } else {
        // Accounting documents (APV, CV, JV, CRV, SV, AJ, etc.)
        this.loadAccountingCycles(transId);
        this.svc.getGlEntries(transId).subscribe({ next: d => this.setDetails(d) });
    }
}

// Exactly mirrors old JS setDetails() — field mapping preserved verbatim
private setDetails(data: any[]): void {
    const typeId = this.selectedDocType?.id;
    this.docInqDetails.set((data ?? []).map(v => ({
        id: typeId === JOA_ID
            ? v.rvDetailId
            : v.rvDetailId != null ? v.rvDetailId : (v.id != null ? v.id : v.accountId),
        unit:        v.unitCode,
        particulars: v.itemDescription ?? v.joDescription ?? v.description,
        quantity:    v.quantity,
        amount:      v.itemAmount,
        debit:       v.debit,
        credit:      v.credit,
    })));
}

// Exactly mirrors old JS setInventoryDetails()
private setInventoryDetails(data: any[]): void {
    const typeId = this.selectedDocType?.id;
    this.showInventoryDetailAmount = true;
    const details = (data ?? []).map(v => {
        let particulars: string;
        let amount: number | null = null;
        if (typeId === RR_ID) {
            particulars = v.description;
            amount      = v.netAmount;
        } else if (typeId === SW_ID) {
            particulars = v.itemDescription;
            this.showInventoryDetailAmount = false;
        } else if (typeId === SRL_ID) {
            particulars = v.description;
            this.showInventoryDetailAmount = false;
        } else {
            // ST, SA, MCT, MST, SRC
            particulars = v.itemDescription;
            amount      = v.totalCost;
            this.showInventoryDetailAmount = true;
        }
        return { id: v.id, unit: v.unitCode, particulars, quantity: v.quantity, amount };
    });
    this.docInqDetails.set(details);
}

private loadPurchaseCycles(details: any[]): void {
    details.forEach(d => {
        this.svc.getPurchaseCycle(d.id).subscribe({
            next: data => {
                const newCycles = (data ?? []).map(v => ({
                    id: v.id, code: v.localCode, voucherDate: v.voucherDate, createdAt: v.createdAt
                }));
                this.cycles.update(c => {
                    const existing = new Set(c.map(x => x.code));
                    return [...c, ...newCycles.filter(x => !existing.has(x.code))];
                });
            }
        });
    });
}

private loadAccountingCycles(transId: number): void {
    this.svc.getAccountingCycle(transId).subscribe({
        next: data => this.cycles.set((data ?? []).map(v => ({
            id: v.id, code: v.localCode, voucherDate: v.voucherDate
        })))
    });
}

private loadInventoryCycles(transId: number): void {
    this.svc.getInventoryCycle(transId).subscribe({
        next: data => this.cycles.set((data ?? []).map(v => ({
            id: v.id, code: v.localCode, voucherDate: v.voucherDate
        })))
    });
}

private loadQuotation(quotationId: number): void {
    // Quotation detail is the supplier comparison table
    this.svc.getQuotationDetails(quotationId).subscribe({
        next: data => {
            if (!data || !data.length) return;
            this.quotationDetails.set(data);
            // Extract suppliers from first item's details array
            const first = data[0];
            if (first?.details) this.quotationSuppliers.set(first.details.map((d: any) => d.supplier));
            this.showQuotationDetails = true;
        }
    });
}
```

- [ ] **Step 2: Add detail panels to the template**

Append after the main table `</div>` (end of `table-responsive`) and before the closing `</div>` of `card-body`:

```html
<!-- Detail panels (shown after row selection) -->
@if (selectedDoc()) {
    <hr class="mt-3"/>

    <!-- Standard details + Cycles (side by side) -->
    @if (!showQuotationDetails && !showCostEstimateDetails && !showSiteInspectionDetails) {
        <div class="row">
            <!-- Left: document details -->
            <div class="col-lg-6">
                <div class="table-responsive">
                    <table class="table table-custom table-centered table-hover w-100 mb-0">
                        <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                            <tr class="text-uppercase fs-xxs">
                                @if (forPurchasing) {
                                    <th>Description</th>
                                    <th>Qty</th>
                                    <th>Unit</th>
                                    @if (rv) { <th class="text-end">Amount</th> }
                                }
                                @if (!forPurchasing && !forInventory) {
                                    <th>Account</th>
                                    <th class="text-end">Debit</th>
                                    <th class="text-end">Credit</th>
                                }
                                @if (forInventory) {
                                    <th>Description</th>
                                    <th>Qty</th>
                                    <th>Unit</th>
                                    @if (showInventoryDetailAmount) { <th class="text-end">Amount</th> }
                                }
                            </tr>
                        </thead>
                        <tbody>
                            @if (docInqDetails().length === 0) {
                                <tr><td colspan="7" class="text-center text-muted py-3">No records found.</td></tr>
                            } @else {
                                @for (d of docInqDetails(); track d.id) {
                                    <tr>
                                        <td>{{ d.particulars }}</td>
                                        @if (forPurchasing) {
                                            <td class="text-end">{{ d.quantity }}</td>
                                            <td>{{ d.unit }}</td>
                                            @if (rv) { <td class="text-end">{{ d.amount | number:'1.2-2' }}</td> }
                                        }
                                        @if (!forPurchasing && !forInventory) {
                                            <td class="text-end">{{ d.debit ? (d.debit | number:'1.2-2') : '' }}</td>
                                            <td class="text-end">{{ d.credit ? (d.credit | number:'1.2-2') : '' }}</td>
                                        }
                                        @if (forInventory) {
                                            <td class="text-end">{{ d.quantity }}</td>
                                            <td>{{ d.unit }}</td>
                                            @if (showInventoryDetailAmount) {
                                                <td class="text-end">{{ d.amount ? (d.amount | number:'1.2-2') : '' }}</td>
                                            }
                                        }
                                    </tr>
                                }
                            }
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Right: cycles -->
            <div class="col-lg-6">
                <div class="table-responsive">
                    <table class="table table-custom table-centered table-hover w-100 mb-0">
                        <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                            <tr class="text-uppercase fs-xxs">
                                <th>Code</th>
                                <th>Date</th>
                            </tr>
                        </thead>
                        <tbody>
                            @if (cycles().length === 0) {
                                <tr><td colspan="2" class="text-center text-muted py-3">No records found.</td></tr>
                            } @else {
                                @for (cyc of cycles(); track cyc.code) {
                                    <tr>
                                        <td>{{ cyc.code }}</td>
                                        <td>{{ cyc.voucherDate | date:'MMM d, yyyy' }}</td>
                                    </tr>
                                }
                            }
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    }

    <!-- Special panels added in Task 8 -->
    <!-- Logs panel added in Task 9 -->
}
```

- [ ] **Step 3: Verify row selection loads details**

Click a document row. Expected: detail table and cycles table appear below the main table. Check browser console for errors. If detail API returns 404, verify the endpoint was added correctly in Task 2.

---

## Task 8: Frontend — Special Detail Panels (Quotation, Cost Estimate, SIR)

**Files:**
- Modify: `frontend/src/app/pages/di/di-main/di-main.component.html`

---

- [ ] **Step 1: Add special detail panel blocks after the standard panels in the template**

Insert inside the `@if (selectedDoc())` block, after the standard panels section:

```html
<!-- Quotation details (full width, 3-supplier comparison) -->
@if (showQuotationDetails) {
    <div class="table-responsive">
        <table class="table table-custom table-centered table-hover w-100 mb-0">
            <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                <tr class="text-uppercase fs-xxs">
                    <th>Description</th>
                    <th class="text-center" style="width:10%">Qty</th>
                    <th>Unit</th>
                    @for (sup of quotationSuppliers(); track $index) {
                        <th class="text-center">{{ sup?.name ?? ('Supplier ' + ($index + 1)) }}</th>
                    }
                </tr>
            </thead>
            <tbody>
                @if (quotationDetails().length === 0) {
                    <tr><td colspan="6" class="text-center text-muted py-3">No records found.</td></tr>
                } @else {
                    @for (q of quotationDetails(); track $index) {
                        <tr>
                            <td>{{ q.itemDescription }}</td>
                            <td class="text-center">{{ q.quantity }}</td>
                            <td>{{ q.unitCode }}</td>
                            @for (d of q.details; track $index) {
                                <td class="text-end">
                                    @if (d.price && d.price !== 0) {
                                        {{ d.price | number:'1.2-2' }}
                                    }
                                </td>
                            }
                        </tr>
                    }
                }
            </tbody>
        </table>
    </div>
}

<!-- Cost Estimate details (full width) -->
@if (showCostEstimateDetails) {
    <div class="table-responsive">
        <table class="table table-custom table-centered table-hover w-100 mb-0">
            <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                <tr class="text-uppercase fs-xxs">
                    <th>Item</th>
                    <th class="text-end">Quantity</th>
                    <th class="text-end">Unit Cost</th>
                    <th class="text-end">Total Cost</th>
                    <th class="text-end">Inventory Cost</th>
                    <th class="text-end">Mark Up</th>
                </tr>
            </thead>
            <tbody>
                @if (costEstimateDetails().length === 0) {
                    <tr><td colspan="6" class="text-center text-muted py-3">No records found.</td></tr>
                } @else {
                    @for (ce of costEstimateDetails(); track $index) {
                        <tr>
                            <td>{{ ce.itemDescription }}</td>
                            <td class="text-end">{{ ce.quantity }}</td>
                            <td class="text-end">{{ ce.unitCost | number:'1.2-2' }}</td>
                            <td class="text-end">{{ ce.totalCost | number:'1.2-2' }}</td>
                            <td class="text-end">{{ ce.inventoryCost | number:'1.2-2' }}</td>
                            <td class="text-end">{{ ce.markUp | number:'1.2-2' }}</td>
                        </tr>
                    }
                }
            </tbody>
        </table>
    </div>
}

<!-- Site Inspection Report details (full width) -->
@if (showSiteInspectionDetails) {
    <div class="table-responsive">
        <table class="table table-custom table-centered table-hover w-100 mb-0">
            <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                <tr class="text-uppercase fs-xxs">
                    <th>Description</th>
                    <th>Remark</th>
                </tr>
            </thead>
            <tbody>
                @if (siteInspectionDetails().length === 0) {
                    <tr><td colspan="2" class="text-center text-muted py-3">No records found.</td></tr>
                } @else {
                    @for (s of siteInspectionDetails(); track $index) {
                        <tr>
                            <td>{{ s.description }}</td>
                            <td>{{ s.remark }}</td>
                        </tr>
                    }
                }
            </tbody>
        </table>
    </div>
}
```

- [ ] **Step 2: Verify special panels**

Select a Quotation Summary document — verify the 3-supplier table appears.
Select a Cost Estimate document — verify the CE table appears.
Select a Site Inspection Report document — verify the SIR table appears.

---

## Task 9: Frontend — View Logs Panel + Print

**Files:**
- Modify: `frontend/src/app/pages/di/di-main/di-main.component.ts`
- Modify: `frontend/src/app/pages/di/di-main/di-main.component.html`

---

- [ ] **Step 1: Add `toggleLogs()` and `print()` to the component**

```typescript
// Add inside DiMainComponent class

toggleLogs(): void {
    if (this.showLogs()) { this.showLogs.set(false); return; }
    const transId = this.selectedDoc()?.transId;
    if (!transId) return;
    this.svc.getLogs(transId).subscribe({
        next: data => { this.logs.set(data ?? []); this.showLogs.set(true); },
        error: ()  => this.alertService.error(this.module, 'Load', 'Failed to load logs.')
    });
}

deserializeLog(json: string): any {
    try { return JSON.parse(json); } catch { return {}; }
}

print(): void {
    const doc  = this.selectedDoc();
    const desc = this.selectedDocType?.desc;
    if (!doc || !desc) return;
    const base = PRINT_BASE_MAP[desc];
    if (!base) { this.alertService.warning(this.module, 'Print', 'Print not supported for this document type.'); return; }
    const id = doc.id;
    let path: string;
    if (desc !== 'Purchase or Work Request') {
        path = `/${base}/export/${id}`;
    } else {
        const rvTypeId = doc.rvTypeId;
        if (rvTypeId === RV_FOR_REP)        path = `/${base}/export2/${id}`;
        else if (rvTypeId === RV_FOR_LABOR)  path = `/${base}/export3/${id}`;
        else if (rvTypeId === RV_FOR_IT)     path = `/${base}/export1/${id}`;
        else                                 path = `/${base}/export/${id}`;
    }
    this.downloadSvc.print(path, { type: 'pdf' });
}

private loadLogs(transId: number): void {
    // Auto-called on row select; user can also toggle with the link
    this.svc.getLogs(transId).subscribe({
        next: data => this.logs.set(data ?? [])
    });
}
```

- [ ] **Step 2: Add the Logs panel to the template**

Append inside the `@if (selectedDoc())` block, after all detail panels:

```html
<!-- View Logs -->
<div class="mt-3">
    <a class="text-warning" style="cursor:pointer;font-size:12px;" (click)="toggleLogs()">
        <ng-icon name="tablerSearch" class="pe-1"></ng-icon>
        {{ showLogs() ? 'Hide Logs' : 'View Logs' }}
    </a>

    @if (showLogs() && logs().length > 0) {
        <div class="table-responsive mt-2">
            <table class="table table-custom table-centered table-hover w-100 mb-0">
                <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                    <tr class="text-uppercase fs-xxs">
                        <th style="width:15%">Timestamp</th>
                        <th style="width:50%">Log</th>
                        <th style="width:20%">Remarks</th>
                        <th>User</th>
                    </tr>
                </thead>
                <tbody>
                    @for (log of logs(); track $index) {
                        <tr>
                            <td>{{ log.createdAt | date:'yyyy-MM-dd h:mma' }}</td>
                            <td>
                                <!-- Standard (non-inventory) log -->
                                @if (!isReceivingReport && !isWithdrawal && !isReleasing && !isStockTransfer && !isReceiving && !isStockAdjustment && !isMCT && !isMST) {
                                    <div>
                                        <h6 class="fw-semibold mb-1">Basic Details</h6>
                                        <p class="mb-0"><span class="fw-bold">Code:</span> {{ deserializeLog(log.newValue).code }}</p>
                                        <p class="mb-0"><span class="fw-bold">Voucher Date:</span> {{ deserializeLog(log.newValue).voucherDate | date:'MMM dd, yyyy' }}</p>
                                        <p class="mb-0"><span class="fw-bold">Amount:</span> {{ deserializeLog(log.newValue).amount | number:'1.2-2' }}</p>
                                        <p class="mb-0"><span class="fw-bold">Document Status:</span> {{ deserializeLog(log.newValue).documentStatus }}</p>
                                        <p class="mb-0"><span class="fw-bold">Created By:</span> {{ deserializeLog(log.newValue).createdBy }}</p>
                                        <p class="mb-0"><span class="fw-bold">Noted By:</span> {{ deserializeLog(log.newValue).notedBy }}</p>
                                        <p class="mb-0"><span class="fw-bold">Checked By:</span> {{ deserializeLog(log.newValue).checkedBy }}</p>
                                        <p class="mb-0"><span class="fw-bold">Approved By:</span> {{ deserializeLog(log.newValue).approvedBy }}</p>
                                    </div>
                                }
                                <!-- Receiving Report -->
                                @if (isReceivingReport) {
                                    <div>
                                        <h6 class="fw-semibold mb-1">Basic Details</h6>
                                        <p class="mb-0"><span class="fw-bold">Code:</span> {{ deserializeLog(log.newValue).code }}</p>
                                        <p class="mb-0"><span class="fw-bold">Delivery Date:</span> {{ deserializeLog(log.newValue).deliveryDate | date:'MMM dd, yyyy' }}</p>
                                        <p class="mb-0"><span class="fw-bold">Invoice Description:</span> {{ deserializeLog(log.newValue).invoiceDescription }}</p>
                                        <p class="mb-0"><span class="fw-bold">Supplier:</span> {{ deserializeLog(log.newValue).supplier }}</p>
                                        <p class="mb-0"><span class="fw-bold">PO No.:</span> {{ deserializeLog(log.newValue).poNumber }}</p>
                                        <p class="mb-0"><span class="fw-bold">Amount:</span> {{ deserializeLog(log.newValue).totalAmount | number:'1.2-2' }}</p>
                                        <p class="mb-0"><span class="fw-bold">Document Status:</span> {{ deserializeLog(log.newValue).documentStatus }}</p>
                                        <p class="mb-0"><span class="fw-bold">Created By:</span> {{ deserializeLog(log.newValue).createdBy }}</p>
                                        <p class="mb-0"><span class="fw-bold">Approved By:</span> {{ deserializeLog(log.newValue).approvedBy }}</p>
                                    </div>
                                }
                                <!-- Stock Withdrawal -->
                                @if (isWithdrawal) {
                                    <div>
                                        <h6 class="fw-semibold mb-1">Basic Details</h6>
                                        <p class="mb-0"><span class="fw-bold">Code:</span> {{ deserializeLog(log.newValue).code }}</p>
                                        <p class="mb-0"><span class="fw-bold">Date:</span> {{ deserializeLog(log.newValue).voucherDate | date:'MMM dd, yyyy' }}</p>
                                        <p class="mb-0"><span class="fw-bold">Description:</span> {{ deserializeLog(log.newValue).description }}</p>
                                        <p class="mb-0"><span class="fw-bold">Inventory Category:</span> {{ deserializeLog(log.newValue).inventoryCategory }}</p>
                                        <p class="mb-0"><span class="fw-bold">Inventory Location:</span> {{ deserializeLog(log.newValue).inventoryLocation }}</p>
                                        <p class="mb-0"><span class="fw-bold">Document Status:</span> {{ deserializeLog(log.newValue).documentStatus }}</p>
                                        <p class="mb-0"><span class="fw-bold">Created By:</span> {{ deserializeLog(log.newValue).createdBy }}</p>
                                        <p class="mb-0"><span class="fw-bold">Recommended By:</span> {{ deserializeLog(log.newValue).checkedBy }}</p>
                                        <p class="mb-0"><span class="fw-bold">Approved By:</span> {{ deserializeLog(log.newValue).approvedBy }}</p>
                                    </div>
                                }
                                <!-- Stock Release -->
                                @if (isReleasing) {
                                    <div>
                                        <h6 class="fw-semibold mb-1">Basic Details</h6>
                                        <p class="mb-0"><span class="fw-bold">Code:</span> {{ deserializeLog(log.newValue).code }}</p>
                                        <p class="mb-0"><span class="fw-bold">Date:</span> {{ deserializeLog(log.newValue).voucherDate | date:'MMM dd, yyyy' }}</p>
                                        <p class="mb-0"><span class="fw-bold">Description:</span> {{ deserializeLog(log.newValue).description }}</p>
                                        <p class="mb-0"><span class="fw-bold">Document Status:</span> {{ deserializeLog(log.newValue).documentStatus }}</p>
                                        @if (!isMCT) {
                                            <p class="mb-0"><span class="fw-bold">Prepared By:</span> {{ deserializeLog(log.newValue).createdBy }}</p>
                                            <p class="mb-0"><span class="fw-bold">Audited By:</span> {{ deserializeLog(log.newValue).auditor }}</p>
                                        } @else {
                                            <p class="mb-0"><span class="fw-bold">Issued By:</span> {{ deserializeLog(log.newValue).createdBy }}</p>
                                        }
                                        <p class="mb-0"><span class="fw-bold">Received By:</span> {{ deserializeLog(log.newValue).receivedBy }}</p>
                                    </div>
                                }
                                <!-- Stock Transfer -->
                                @if (isStockTransfer) {
                                    <div>
                                        <h6 class="fw-semibold mb-1">Basic Details</h6>
                                        <p class="mb-0"><span class="fw-bold">Code:</span> {{ deserializeLog(log.newValue).code }}</p>
                                        <p class="mb-0"><span class="fw-bold">Date:</span> {{ deserializeLog(log.newValue).voucherDate | date:'MMM dd, yyyy' }}</p>
                                        <p class="mb-0"><span class="fw-bold">Source:</span> {{ deserializeLog(log.newValue).fromInventoryLocation }}</p>
                                        <p class="mb-0"><span class="fw-bold">Destination:</span> {{ deserializeLog(log.newValue).toInventoryLocation }}</p>
                                        <p class="mb-0"><span class="fw-bold">Remarks:</span> {{ deserializeLog(log.newValue).remarks }}</p>
                                        <p class="mb-0"><span class="fw-bold">Document Status:</span> {{ deserializeLog(log.newValue).documentStatus }}</p>
                                        <p class="mb-0"><span class="fw-bold">Created By:</span> {{ deserializeLog(log.newValue).createdBy }}</p>
                                        <p class="mb-0"><span class="fw-bold">Checked By:</span> {{ deserializeLog(log.newValue).checkedBy }}</p>
                                        <p class="mb-0"><span class="fw-bold">Approved By:</span> {{ deserializeLog(log.newValue).approvedBy }}</p>
                                    </div>
                                }
                                <!-- Stock Receive -->
                                @if (isReceiving) {
                                    <div>
                                        <h6 class="fw-semibold mb-1">Basic Details</h6>
                                        <p class="mb-0"><span class="fw-bold">Code:</span> {{ deserializeLog(log.newValue).code }}</p>
                                        <p class="mb-0"><span class="fw-bold">Date:</span> {{ deserializeLog(log.newValue).voucherDate | date:'MMM dd, yyyy' }}</p>
                                        <p class="mb-0"><span class="fw-bold">Description:</span> {{ deserializeLog(log.newValue).description }}</p>
                                        <p class="mb-0"><span class="fw-bold">Document Status:</span> {{ deserializeLog(log.newValue).documentStatus }}</p>
                                        <p class="mb-0"><span class="fw-bold">Received By:</span> {{ deserializeLog(log.newValue).createdBy }}</p>
                                        <p class="mb-0"><span class="fw-bold">Issued By:</span> {{ deserializeLog(log.newValue).issuedBy }}</p>
                                    </div>
                                }
                                <!-- Stock Adjustment -->
                                @if (isStockAdjustment) {
                                    <div>
                                        <h6 class="fw-semibold mb-1">Basic Details</h6>
                                        <p class="mb-0"><span class="fw-bold">Code:</span> {{ deserializeLog(log.newValue).code }}</p>
                                        <p class="mb-0"><span class="fw-bold">Date:</span> {{ deserializeLog(log.newValue).voucherDate | date:'MMM dd, yyyy' }}</p>
                                        <p class="mb-0"><span class="fw-bold">Remarks:</span> {{ deserializeLog(log.newValue).remarks }}</p>
                                        <p class="mb-0"><span class="fw-bold">Document Status:</span> {{ deserializeLog(log.newValue).documentStatus }}</p>
                                        <p class="mb-0"><span class="fw-bold">Created By:</span> {{ deserializeLog(log.newValue).createdBy }}</p>
                                        <p class="mb-0"><span class="fw-bold">Checked By:</span> {{ deserializeLog(log.newValue).checkedBy }}</p>
                                        <p class="mb-0"><span class="fw-bold">Approved By:</span> {{ deserializeLog(log.newValue).approvedBy }}</p>
                                    </div>
                                }
                                <!-- MCT -->
                                @if (isMCT) {
                                    <div>
                                        <h6 class="fw-semibold mb-1">Basic Details</h6>
                                        <p class="mb-0"><span class="fw-bold">Code:</span> {{ deserializeLog(log.newValue).code }}</p>
                                        <p class="mb-0"><span class="fw-bold">Date:</span> {{ deserializeLog(log.newValue).voucherDate | date:'MMM dd, yyyy' }}</p>
                                        <p class="mb-0"><span class="fw-bold">Remarks:</span> {{ deserializeLog(log.newValue).remarks }}</p>
                                        <p class="mb-0"><span class="fw-bold">Document Status:</span> {{ deserializeLog(log.newValue).documentStatus }}</p>
                                        <p class="mb-0"><span class="fw-bold">Returned By:</span> {{ deserializeLog(log.newValue).createdBy }}</p>
                                        <p class="mb-0"><span class="fw-bold">Requested By:</span> {{ deserializeLog(log.newValue).requestedBy }}</p>
                                        <p class="mb-0"><span class="fw-bold">Received By:</span> {{ deserializeLog(log.newValue).approvedBy }}</p>
                                    </div>
                                }
                                <!-- MST -->
                                @if (isMST) {
                                    <div>
                                        <h6 class="fw-semibold mb-1">Basic Details</h6>
                                        <p class="mb-0"><span class="fw-bold">Code:</span> {{ deserializeLog(log.newValue).code }}</p>
                                        <p class="mb-0"><span class="fw-bold">Date:</span> {{ deserializeLog(log.newValue).voucherDate | date:'MMM dd, yyyy' }}</p>
                                        <p class="mb-0"><span class="fw-bold">Purpose:</span> {{ deserializeLog(log.newValue).purpose }}</p>
                                        <p class="mb-0"><span class="fw-bold">Document Status:</span> {{ deserializeLog(log.newValue).documentStatus }}</p>
                                        <p class="mb-0"><span class="fw-bold">Created By:</span> {{ deserializeLog(log.newValue).createdBy }}</p>
                                        <p class="mb-0"><span class="fw-bold">Received By:</span> {{ deserializeLog(log.newValue).receivedBy }}</p>
                                        <p class="mb-0"><span class="fw-bold">Returned By:</span> {{ deserializeLog(log.newValue).returnedBy }}</p>
                                    </div>
                                }
                            </td>
                            <td>{{ deserializeLog(log.newValue).remarks }}</td>
                            <td>{{ log.loggedBy?.fullName }}</td>
                        </tr>
                    }
                </tbody>
            </table>
        </div>
    }
}
```

- [ ] **Step 2: Verify logs toggle and print**

1. Select a document row → "View Logs" link appears. Click it → logs table expands.
2. Click "Hide Logs" → table collapses.
3. Click "Print" button → browser opens print PDF. If `DownloadService.print()` doesn't open a tab, verify its signature in `download.service.ts` — it may take different params.

---

## Self-Review

**Spec coverage check:**

| Spec requirement | Covered in task |
|---|---|
| Department filter with user default | Task 4 (ngOnInit) |
| Document type grouped select | Task 5 (template) |
| From/To date with flatpickr | Task 5 (template) |
| Go! search | Task 5 (search()) |
| Supplier browse + clear (APV/CV) | Task 6 |
| Due date (APV only) | Task 6 |
| Account/amount filters (APV,CV,JV,CRV,SV) | Task 6 |
| Add Filters dropdown (Status/Date/DocNo/Particulars) | Task 5 |
| Transacted By user filter | Task 5 |
| Search by voucher code | Task 5 |
| Print button (post-selection) | Task 9 |
| Main table Code/Date/Particulars/Status/Amount | Task 5 |
| Row click → details | Task 7 |
| Purchasing details (Desc/Qty/Unit/Amount) | Task 7 |
| Accounting details (Account/Debit/Credit) | Task 7 |
| Inventory details (Desc/Qty/Unit/Amount conditional) | Task 7 |
| Cycles table (unique by code) | Task 7 |
| Quotation details (3-supplier) | Task 8 |
| Cost Estimate details | Task 8 |
| Site Inspection Report details | Task 8 |
| View Logs toggle + 9 type-specific log templates | Task 9 |
| Backend 7 core DI endpoints | Task 1 |
| Backend detail endpoints (RR, SW, SRL, ST, SA, MCT, MST, SRC, CE, SIR) | Task 2 |
| Route registered in pages.route.ts | Task 3 |
| Angular 19 signals, standalone, inject() | Task 4 |
| v2 table classes and button standards | All tasks |

**No gaps found.** All spec requirements are implemented across the 9 tasks.
