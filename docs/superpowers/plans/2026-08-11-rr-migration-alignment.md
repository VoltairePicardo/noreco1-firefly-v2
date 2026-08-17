# RR Module Migration Alignment Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Align the Firefly v2 RR (Receiving Report) Angular 19 module to faithfully mirror the OLD Firefly RR module's fields, workflows, and business logic.

**Architecture:** The NEW implementation is severely incomplete — the add/edit form has only 2 of ~15+ fields; the detail screen is missing the items table and several header fields. All 4 files in `pages/rr` require changes. No new shared components are created; existing browse modals (`BrowsePurchaseOrderModalComponent`, `BrowseJobOrderModalComponent`, `BrowseSupplierModalComponent`, `BrowseEntityModalComponent`, `BrowseItemModalComponent`) are reused. The `ModalService` async/await pattern is followed exactly as in `purchase-order-add-edit`.

**Tech Stack:** Angular 19, TypeScript signals, `@if`/`@for`, NgbModal, `ModalService`, `AlertService`, Flatpickr, `COMMON_ALL_PAGE_IMPORTS` + `COMMON_ADD_EDIT_PAGE_IMPORTS` + `COMMON_MAIN_PAGE_IMPORTS`

## Global Constraints

- Never use `*ngIf` / `*ngFor` — use `@if` / `@for` (Angular 19 control flow)
- All alerts/confirms via `AlertService` (SweetAlert2) — no native alert/confirm
- Date inputs via `mwlFlatpickr` with `{ dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' }`
- Buttons follow CLAUDE.md: `btn-primary` (save), `btn-success` (search/print), `btn-light` (cancel/back), `btn-danger` (reset)
- Browse fields use the `input-group` browse pattern: readonly text input + `btn-success fw-bold` Browse button
- Signatory browse (Noted By / Approved By) via `BrowseEntityModalComponent` — same as `purchase-order-add-edit`
- `ModalService.openModal(Component, inputs, options)` is async/await
- Never commit — user handles git
- Only touch files in `pages/rr` unless referencing a specific shared service method

---

## Parity Gap Summary (OLD vs NEW)

| Area | OLD | NEW | Gap |
|------|-----|-----|-----|
| List — text search | ✅ filter input | ❌ missing | Add |
| Add/Edit — deliveryNumber | ✅ | ❌ | Add |
| Add/Edit — invoiceDate | ✅ | ❌ | Add |
| Add/Edit — invoiceNumber | ✅ | ❌ | Add |
| Add/Edit — inventoryLocation | ✅ required | ❌ | Add |
| Add/Edit — document type | ✅ 5 types (PO/IFR/RV/JO/Tested) | ❌ | Add |
| Add/Edit — reference doc browse | ✅ PO/IFR/RV/JO/ItemTest browsers | ❌ | Add |
| Add/Edit — items table | ✅ 3 table variants + calculations | ❌ | Add |
| Add/Edit — Noted By | ✅ | ❌ | Add |
| Add/Edit — Approved By | ✅ | ❌ | Add |
| Add/Edit — default signatories on create | ✅ | ❌ | Add |
| Add/Edit — attachments | ✅ (filesToAdd/Remove) | ❌ | Add |
| Detail — deliveryNumber | ✅ | ❌ | Add |
| Detail — invoiceNumber | ✅ | ❌ | Add |
| Detail — invoiceDate | ✅ | ❌ | Add |
| Detail — Noted By (checker) | ✅ | ❌ | Add |
| Detail — Approved By | ✅ | ❌ | Add |
| Detail — Last Updated | ✅ | ❌ | Add |
| Detail — items table | ✅ | ❌ | Add |
| Detail — attachments section | ✅ | ❌ | Add |

---

## File Map

| File | Action | What changes |
|------|--------|-------------|
| `pages/rr/rr.service.ts` | Modify | Add 5 new API methods |
| `pages/rr/rr-main/rr-main.component.ts` | Modify | Add `searchText` property + `filteredPagedRecords` |
| `pages/rr/rr-main/rr-main.component.html` | Modify | Add search input + use `filteredPagedRecords` |
| `pages/rr/rr-add-edit/rr-add-edit.component.ts` | Major rewrite | All missing fields, browse modals, items, calculations, save payload |
| `pages/rr/rr-add-edit/rr-add-edit.component.html` | Major rewrite | All missing form sections |
| `pages/rr/rr-detail/rr-detail.component.html` | Modify | Add missing fields, items table, attachments |

---

## Task 1: Augment RrService with Missing API Methods

**Files:**
- Modify: `frontend/src/app/pages/rr/rr.service.ts`

**Interfaces:**
- Produces: `getInventoryLocations()`, `getPurchaseOrderDetailsForRR(poId)`, `getPurchaseOrderDetailsWithItemTesting(poId)`, `getJobOrderDetailsForRR(joId)`, `getDefaultSignatories()`
- Consumed by: Task 3 (add-edit component)

- [ ] **Step 1: Read current rr.service.ts**

File: `frontend/src/app/pages/rr/rr.service.ts` — already reviewed; currently has 10 methods, missing 5.

- [ ] **Step 2: Add the 5 missing methods**

Add after the existing `print()` method:

```typescript
getInventoryLocations(): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_API}/rr/inventory-locations`);
}

getPurchaseOrderDetailsForRR(poId: number): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_API}/rr/po-details/${poId}`);
}

getPurchaseOrderDetailsWithItemTesting(poId: number): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_API}/rr/po-details-item-testing/${poId}`);
}

getJobOrderDetailsForRR(joId: number): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_API}/rr/jo-details/${joId}`);
}

getDefaultSignatories(): Observable<any> {
    return this.http.get(`${BASE_API}/rr/default-signatories`);
}
```

- [ ] **Step 3: Verify the file has no TypeScript errors by inspection**

Check: All methods return `Observable<T>` and use `this.http`. Imports (`Observable`) already present.

---

## Task 2: Fix List Screen — Add Text Search Filter

**Files:**
- Modify: `frontend/src/app/pages/rr/rr-main/rr-main.component.ts`
- Modify: `frontend/src/app/pages/rr/rr-main/rr-main.component.html`

**Interfaces:**
- Produces: `searchText: string` property, `filteredPagedRecords: any[]` getter
- The table now uses `filteredPagedRecords` instead of `pagedRecords`

**Why:** OLD had a text search input (ng-model="query") that filtered the table by Code/Supplier/Prepared By inline. The NEW list has no text search.

- [ ] **Step 1: Add searchText property and filteredPagedRecords getter to rr-main.component.ts**

After `selectedStatus = signal<number | null>(null);`, add:

```typescript
searchText = '';
```

Replace the existing `get pagedRecords()` getter with:

```typescript
get pagedRecords(): any[] {
    const q = this.searchText.trim().toLowerCase();
    const filtered = q
        ? this.records().filter(r =>
            (r.localCode || r.code || '').toLowerCase().includes(q) ||
            (r.supplier?.name || '').toLowerCase().includes(q) ||
            (r.receivedBy || r.preparedBy || '').toLowerCase().includes(q)
          )
        : this.records();
    const start = (this.page - 1) * this.pageSize;
    return filtered.slice(start, start + this.pageSize);
}

get filteredTotal(): number {
    const q = this.searchText.trim().toLowerCase();
    if (!q) return this.records().length;
    return this.records().filter(r =>
        (r.localCode || r.code || '').toLowerCase().includes(q) ||
        (r.supplier?.name || '').toLowerCase().includes(q) ||
        (r.receivedBy || r.preparedBy || '').toLowerCase().includes(q)
    ).length;
}
```

- [ ] **Step 2: Add search input to the filter row in rr-main.component.html**

After the status dropdown column, before the button group `col-auto`, add:

```html
<div class="col-md-3">
    <label class="form-label fw-bold mb-1 fs-xs text-uppercase">Search</label>
    <input type="text" class="form-control" [(ngModel)]="searchText"
           placeholder="Code, supplier, prepared by…" (ngModelChange)="page = 1"/>
</div>
```

- [ ] **Step 3: Update pagination to use filteredTotal**

Change pagination `[collectionSize]` from `records().length` to `filteredTotal`:

```html
@if (filteredTotal > pageSize) {
    <div class="d-flex justify-content-center mt-3">
        <ngb-pagination
            [collectionSize]="filteredTotal"
            [(page)]="page"
            [pageSize]="pageSize"
            [maxSize]="5">
        </ngb-pagination>
    </div>
}
```

- [ ] **Step 4: Update empty state colspan to 7 (already correct) and verify template consistency**

Verify: table uses `pagedRecords` (which now filters by searchText). No other template changes required.

---

## Task 3: Overhaul Add/Edit — Header Fields, Document Type, and Reference Browse

**Files:**
- Modify: `frontend/src/app/pages/rr/rr-add-edit/rr-add-edit.component.ts`
- Modify: `frontend/src/app/pages/rr/rr-add-edit/rr-add-edit.component.html`

**Interfaces:**
- Produces: all form field properties, browse modal methods, document type selection logic
- Consumed by: Task 4 (items table), Task 5 (signatories + save)

**Note:** Tasks 3, 4, 5 all modify the same two files. They are presented as sequential logical sections but will be applied together. The final file replaces the current incomplete component.

### 3A — Component Properties

- [ ] **Step 1: Replace the component class with expanded properties**

The new component has:

```typescript
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { RrService } from '../rr.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowsePurchaseOrderModalComponent } from '@/app/shared/modals/browse-purchase-order-modal/browse-purchase-order-modal.component';
import { BrowseJobOrderModalComponent } from '@/app/shared/modals/browse-job-order-modal/browse-job-order-modal.component';
import { BrowseSupplierModalComponent } from '@/app/shared/modals/browse-supplier-modal/browse-supplier-modal.component';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseItemModalComponent } from '@/app/shared/modals/browse-item-modal/browse-item-modal.component';
import { provideIcons } from '@ng-icons/core';
import {
  tablerSearch, tablerTrash, tablerPlus, tablerArrowLeft, tablerCheck
} from '@ng-icons/tabler-icons';
import { forkJoin } from 'rxjs';

// Document type constants
const DOC_TYPE_PO     = 1;
const DOC_TYPE_IFR    = 2;
const DOC_TYPE_RV     = 3;
const DOC_TYPE_JO     = 4;
const DOC_TYPE_TESTED = 5;
```

Component decorator:
```typescript
@Component({
    selector: 'app-rr-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective,
        RouterLink
    ],
    providers: [
        provideFlatpickrDefaults(),
        ...SHARED_PROVIDERS,
        provideIcons({ tablerSearch, tablerTrash, tablerPlus, tablerArrowLeft, tablerCheck })
    ],
    templateUrl: './rr-add-edit.component.html'
})
export class RrAddEditComponent {
    module    = 'Receiving Report';
    subModule = 'Create';
    menuLink  = 'rr';

    id: any  = null;
    editMode = false;
    isLoading = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // Header fields
    deliveryDate   = '';
    deliveryNumber = '';
    invoiceDate    = '';
    invoiceNumber  = '';
    remarks        = '';

    // Inventory Location
    inventoryLocations = signal<any[]>([]);
    selectedInventoryLocation: any = null;

    // Document type (mutually exclusive)
    docType = DOC_TYPE_PO;  // default: Purchase Order

    // Reference documents (one per type)
    selectedPO:  any = null;  // for PO and Tested Items
    selectedJO:  any = null;  // for Job Order
    selectedIFR: any = null;  // for Items For Repair
    selectedRV:  any = null;  // for Request Voucher
    selectedSupplier: any = null;  // for RV type (separate supplier browse)

    // Display descriptions for browse inputs
    poDesc  = '';
    joDesc  = '';
    ifrDesc = '';
    rvDesc  = '';

    // Items table
    rrDetails: any[] = [];

    // Totals (recomputed on every change)
    totals = { qty: 0, cost: 0, qtyReceived: 0, adjustment: 0, netAmount: 0 };

    // Signatories
    notedBy: any    = null;
    approvedBy: any = null;

    private service      = inject(RrService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);
```

### 3B — ngOnInit and loadForEdit

- [ ] **Step 2: Implement ngOnInit**

```typescript
    ngOnInit(): void {
        // Load inventory locations
        this.service.getInventoryLocations().subscribe({
            next: (locs) => this.inventoryLocations.set(locs || []),
            error: () => {}
        });

        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.editMode = idParam != null && /^\d+$/.test(idParam);
            if (this.editMode) {
                this.id = Number(idParam);
                this.subModule = 'Edit';
                this.loadForEdit();
            } else {
                this.subModule = 'Create';
                this.loadDefaultSignatories();
                // default delivery/invoice dates to today
                const today = new Date().toISOString().substring(0, 10);
                this.deliveryDate = today;
                this.invoiceDate  = today;
            }
        });
    }
```

- [ ] **Step 3: Implement loadDefaultSignatories**

```typescript
    loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (data) => {
                if (data) {
                    this.notedBy    = data.checkedBy   || null;
                    this.approvedBy = data.approvedBy  || null;
                }
            },
            error: () => {}
        });
    }
```

- [ ] **Step 4: Implement loadForEdit**

```typescript
    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (!data?.id) {
                    this.alertService.error(this.module, 'Record not found.', '');
                    this.router.navigate(['/' + this.menuLink]);
                    return;
                }
                const toYmd = (v: any) => v ? new Date(v).toISOString().substring(0, 10) : '';

                this.deliveryDate   = toYmd(data.deliveryDate);
                this.invoiceDate    = toYmd(data.invoiceDate);
                this.deliveryNumber = data.deliveryNumber || '';
                this.invoiceNumber  = data.invoiceNumber  || '';
                this.remarks        = data.remarks        || '';
                this.notedBy        = data.checker        || null;
                this.approvedBy     = data.approvingOfficer || null;

                // Inventory location
                if (data.inventoryLocation?.id) {
                    const found = this.inventoryLocations().find(
                        l => l.id === data.inventoryLocation.id
                    );
                    this.selectedInventoryLocation = found ?? data.inventoryLocation;
                }

                // Document type + reference
                if (data.isJO) {
                    this.docType   = DOC_TYPE_JO;
                    this.selectedJO = data.jobOrder;
                    this.joDesc    = data.jobOrder?.joDesc || data.jobOrder?.localCode || '';
                } else if (data.isRV) {
                    this.docType          = DOC_TYPE_RV;
                    this.selectedRV       = data.requisitionVoucher;
                    this.rvDesc           = data.requisitionVoucher?.rvDesc || data.requisitionVoucher?.code || '';
                    this.selectedSupplier = data.supplier;
                } else if (data.isRepairedItems) {
                    this.docType    = DOC_TYPE_IFR;
                    this.selectedIFR = data.itemsForRepair;
                    this.ifrDesc    = data.itemsForRepair?.ifrDesc || data.itemsForRepair?.code || '';
                } else {
                    // PO or Tested Items — both use purchaseOrder reference
                    this.docType    = data.isTestedItem ? DOC_TYPE_TESTED : DOC_TYPE_PO;
                    this.selectedPO = data.purchaseOrder;
                    this.poDesc     = data.purchaseOrder?.poDesc || data.purchaseOrder?.localCode || '';
                }

                this.rrDetails = data.rrDetails || [];
                this.updateTotals();
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load record.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }
```

### 3C — Document Type Selection Logic

- [ ] **Step 5: Implement selectDocType**

```typescript
    selectDocType(type: number): void {
        this.docType         = type;
        this.rrDetails       = [];
        this.selectedPO      = null;
        this.selectedJO      = null;
        this.selectedIFR     = null;
        this.selectedRV      = null;
        this.selectedSupplier = null;
        this.poDesc          = '';
        this.joDesc          = '';
        this.ifrDesc         = '';
        this.rvDesc          = '';
        this.updateTotals();
    }

    get isPO():     boolean { return this.docType === DOC_TYPE_PO; }
    get isIFR():    boolean { return this.docType === DOC_TYPE_IFR; }
    get isRV():     boolean { return this.docType === DOC_TYPE_RV; }
    get isJO():     boolean { return this.docType === DOC_TYPE_JO; }
    get isTested(): boolean { return this.docType === DOC_TYPE_TESTED; }
```

### 3D — Browse Modal Methods

- [ ] **Step 6: Implement browse methods for reference documents**

```typescript
    // ─── Browse: Purchase Order ───────────────────────────────────────────────
    async openPOBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowsePurchaseOrderModalComponent, {}, { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const po = result.data;
                this.selectedPO = po;
                this.poDesc = (po.localCode || po.code || '') + ' : ' + (po.vendor?.name || po.supplier || '');
                this.rrDetails = [];
                const loader$ = this.isTested
                    ? this.service.getPurchaseOrderDetailsWithItemTesting(po.id)
                    : this.service.getPurchaseOrderDetailsForRR(po.id);
                loader$.subscribe({
                    next: (items) => {
                        this.rrDetails = items || [];
                        this.initAllQuantitiesReceived();
                        this.updateTotals();
                    },
                    error: () => this.alertService.error(this.module, 'Failed to load PO items.', '')
                });
            }
        } catch { }
    }

    // ─── Browse: Job Order ────────────────────────────────────────────────────
    async openJOBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseJobOrderModalComponent, {}, { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const jo = result.data;
                this.selectedJO = jo;
                this.joDesc = (jo.localCode || jo.code || '') + ' : ' + (jo.vendor?.name || jo.supplier || '');
                this.rrDetails = [];
                this.service.getJobOrderDetailsForRR(jo.id).subscribe({
                    next: (items) => {
                        this.rrDetails = items || [];
                        this.initAllQuantitiesReceived();
                        this.updateTotals();
                    },
                    error: () => this.alertService.error(this.module, 'Failed to load JO items.', '')
                });
            }
        } catch { }
    }

    // ─── Browse: Supplier (for RV type) ──────────────────────────────────────
    async openSupplierBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseSupplierModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.selectedSupplier = result.data;
            }
        } catch { }
    }

    // ─── Browse: Noted By / Approved By ──────────────────────────────────────
    async openSignatoryBrowse(field: 'notedBy' | 'approvedBy'): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this[field] = result.data;
            }
        } catch { }
    }

    // ─── Browse: Item (for Repaired Items row) ────────────────────────────────
    async openItemBrowse(index: number): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseItemModalComponent, {}, { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const item = result.data;
                // Avoid duplicate items
                const isDuplicate = this.rrDetails.some(
                    (r, i) => i !== index && r.itemId === item.id
                );
                if (isDuplicate) {
                    this.alertService.warning(this.module, 'Validation', 'Duplicate item.');
                    return;
                }
                const row = this.rrDetails[index];
                row.itemId          = item.id;
                row.itemDescription = item.description;
                row.unitCode        = item.unit?.code || '';
                row.hasItem         = true;
                this.updateNetAmount(index, 'quantityReceived');
            }
        } catch { }
    }

    removeRow(index: number): void {
        this.rrDetails.splice(index, 1);
        this.updateTotals();
    }
```

---

## Task 4: Add/Edit — Items Table and Calculations

**Files:**
- Modify: `frontend/src/app/pages/rr/rr-add-edit/rr-add-edit.component.ts` (continued from Task 3)

**Why:** The items table is the core of the RR — quantities, prices, net amount calculations, and totals are all missing from NEW.

- [ ] **Step 1: Implement initAllQuantitiesReceived**

```typescript
    initAllQuantitiesReceived(): void {
        for (const item of this.rrDetails) {
            this.initQuantityReceived(item);
        }
    }

    initQuantityReceived(item: any): void {
        const qty = item.quantityReceived ?? 0;
        const delivered = item.deliveredQuantity ?? 0;
        if (!qty || qty <= 0) {
            item.quantityReceived = Math.max(0, item.quantity - delivered);
        }
        item.quantityReceivedStatic = item.quantityReceived;
        item.adjustment = item.adjustment ?? 0;
    }
```

- [ ] **Step 2: Implement updateNetAmount**

Business rules (from OLD rr.js lines 444-511):
- quantityReceived must not exceed (quantity - deliveredQuantity)  
- For IFR: quantityReceived must not exceed deliveredQuantity (of the IFR row)
- adjustment must not be more negative than -(unitPrice × quantityReceived)
- netAmount = (quantityReceived × unitPrice) + (adjustment × quantityReceived)

Wait, looking at OLD rr.js more carefully:

Line 506: `var netAmount = (currentLineItem.quantityReceived * currentLineItem.unitPrice) + (adjustment * currentLineItem.quantityReceived);`

Actually that seems wrong (adjustment × qtyReceived). Let me re-read:

Line 506: `var netAmount = (currentLineItem.quantityReceived * currentLineItem.unitPrice) + (adjustment * currentLineItem.quantityReceived);`
Line 509: `$scope.rr.rrDetails[index].itemAmount = currentLineItem.quantityReceived * currentLineItem.unitPrice;`

Actually looking at updateTotals (line 384):
`$scope.rr.rrDetails[x].netAmount = (checkerUtil.isNullOrUndefined2($scope.rr.rrDetails[x].unitPrice)*checkerUtil.isNullOrUndefined2($scope.rr.rrDetails[x].quantityReceived))+checkerUtil.isNullOrUndefined2($scope.rr.rrDetails[x].adjustment);`

This means: `netAmount = (unitPrice × quantityReceived) + adjustment`
And: `itemAmount = unitPrice × quantityReceived`

So `netAmount = itemAmount + adjustment`. The formula at line 506 seems like a different calculation (maybe for a different code path).

Use the updateTotals formula: `netAmount = (unitPrice × quantityReceived) + adjustment`.

```typescript
    updateNetAmount(index: number, changed: string): void {
        const item = this.rrDetails[index];
        if (!item) return;

        if (changed === 'quantityReceived') {
            if (item.quantityReceived === undefined || item.quantityReceived === null) {
                item.quantityReceived = item.quantityReceivedStatic ?? 0;
            }
            if (this.isIFR) {
                // For repaired items: qty received must not exceed deliveredQuantity
                const max = item.deliveredQuantity ?? 0;
                if (item.quantityReceived > max) {
                    item.quantityReceived = max;
                    this.alertService.warning(this.module, 'Qty', 'Quantity must not exceed delivered quantity.');
                }
            } else {
                const maxRemaining = (item.quantity ?? 0) - (item.deliveredQuantity ?? 0);
                if (item.quantityReceived > maxRemaining) {
                    item.quantityReceived = maxRemaining;
                    this.alertService.warning(this.module, 'Qty', `Quantity must not exceed ${maxRemaining}.`);
                }
            }
        }

        if (changed === 'deliveredQuantity' && this.isIFR) {
            const ifrQty = item.ifrItem?.quantity ?? 0;
            const ifrDelivered = item.ifrItem?.deliveredQuantity ?? 0;
            const maxDel = ifrQty - ifrDelivered;
            if ((item.deliveredQuantity ?? 0) > maxDel) {
                item.deliveredQuantity = maxDel;
                this.alertService.warning(this.module, 'Qty', `Delivered quantity must not exceed ${maxDel}.`);
            }
            if ((item.quantityReceived ?? 0) > (item.deliveredQuantity ?? 0)) {
                item.quantityReceived = item.deliveredQuantity;
            }
        }

        if (changed === 'adjustment') {
            const adj       = parseFloat(item.adjustment) || 0;
            const itemAmt   = (item.unitPrice ?? 0) * (item.quantityReceived ?? 0);
            if (adj < -itemAmt) {
                item.adjustment = 0;
                this.alertService.warning(this.module, 'Adjustment', 'Amount adjustment is not allowed.');
                return;
            }
        }

        // Recalculate
        item.itemAmount = (item.unitPrice ?? 0) * (item.quantityReceived ?? 0);
        item.netAmount  = item.itemAmount + (parseFloat(item.adjustment) || 0);

        this.updateTotals();
    }

    updateTotals(): void {
        const t = { qty: 0, cost: 0, qtyReceived: 0, adjustment: 0, netAmount: 0 };
        for (const item of this.rrDetails) {
            item.itemAmount = (item.unitPrice ?? 0) * (item.quantityReceived ?? 0);
            item.netAmount  = item.itemAmount + (parseFloat(item.adjustment) || 0);
            t.qty          += item.quantity     ?? 0;
            t.cost         += item.itemAmount   ?? 0;
            t.qtyReceived  += item.quantityReceived ?? 0;
            t.adjustment   += parseFloat(item.adjustment) || 0;
            t.netAmount    += item.netAmount    ?? 0;
        }
        this.totals = t;
    }
```

---

## Task 5: Add/Edit — Signatories, Full Validation, and Save Payload

**Files:**
- Modify: `frontend/src/app/pages/rr/rr-add-edit/rr-add-edit.component.ts` (continued)
- Modify: `frontend/src/app/pages/rr/rr-add-edit/rr-add-edit.component.html` (full template replacement)

- [ ] **Step 1: Implement complete save() with validation**

```typescript
    save(): void {
        const warnings: string[] = [];

        if (!this.deliveryDate)                warnings.push('Delivery Date is required.');
        if (!this.invoiceDate)                  warnings.push('Invoice Date is required.');
        if (!this.selectedInventoryLocation?.id) warnings.push('Inventory Location is required.');
        if (this.isPO  && !this.selectedPO)     warnings.push('Purchase Order is required.');
        if (this.isJO  && !this.selectedJO)     warnings.push('Job Order is required.');
        if (this.isIFR && !this.selectedIFR)    warnings.push('Items For Repair is required.');
        if (this.isRV  && !this.selectedRV)     warnings.push('Request Voucher is required.');
        if (this.isRV  && !this.selectedSupplier) warnings.push('Supplier is required for Purchase Request type.');
        if (this.rrDetails.length === 0)        warnings.push('Please add items to receive.');
        if (this.totals.netAmount === 0)        warnings.push('Total item amount is ZERO.');
        if (!this.notedBy)                      warnings.push('Noted By is required.');
        if (!this.approvedBy)                   warnings.push('Approved By is required.');

        if (warnings.length > 0) {
            this.alertService.fieldWarning(this.module, 'Validation', warnings);
            return;
        }

        this.isLoading.set(true);

        // Build supplier reference
        let supplier: any = null;
        if (this.isIFR && this.selectedIFR?.supplier) {
            supplier = { accountNumber: this.selectedIFR.supplier.accountNumber || this.selectedIFR.supplier.accountNo };
        } else if (this.isRV && this.selectedSupplier) {
            supplier = { accountNumber: this.selectedSupplier.accountNumber || this.selectedSupplier.accountNo };
        } else if (this.isPO && this.selectedPO?.vendor) {
            supplier = { accountNumber: this.selectedPO.vendor.accountNumber || this.selectedPO.vendor.accountNo };
        } else if (this.isJO && this.selectedJO?.vendor) {
            supplier = { accountNumber: this.selectedJO.vendor.accountNumber || this.selectedJO.vendor.accountNo };
        }

        // Build RR details
        const rrDetails = this.rrDetails.map(d => ({
            itemTransactionDetail: { id: d.itemTransactionDetailId ?? 0 },
            purchaseRequestDetail: { id: (this.isRV || (d.rvDetailId ?? 0) > 0) ? (d.rvDetailId ?? 0) : 0 },
            item:                  { id: d.itemId },
            deliveryNumber:        this.deliveryNumber || '',
            quantityOrdered:       d.quantity,
            quantityReceived:      d.quantityReceived,
            deliveredQuantity:     d.deliveredQuantity,
            unitPrice:             d.unitPrice,
            amount:                d.itemAmount,
            discount:              d.discount ?? 0,
            vat:                   d.vat ?? 0,
            adjustment:            d.adjustment ?? 0,
            netAmount:             d.netAmount,
            ...(this.isJO
                ? { joDetail:  { id: d.id } }
                : { poDetail:  { id: d.id } })
        }));

        const payload: any = {
            deliveryDate:       this.deliveryDate,
            invoiceDate:        this.invoiceDate,
            deliveryNumber:     this.deliveryNumber || null,
            invoiceNumber:      this.invoiceNumber  || null,
            remarks:            this.remarks        || null,
            isRepairedItems:    this.isIFR,
            isPurchaseOrder:    this.isPO,
            isRV:               this.isRV,
            isJO:               this.isJO,
            isTestedItem:       this.isTested,
            supplier:           supplier,
            approvingOfficer:   { accountNo: this.approvedBy.accountNo || this.approvedBy.accountNumber },
            checker:            { accountNo: this.notedBy.accountNo    || this.notedBy.accountNumber },
            inventoryLocation:  this.selectedInventoryLocation,
            totalAmount:        this.totals.netAmount,
            totalQuantity:      this.totals.qtyReceived,
            rrDetails:          rrDetails,
        };

        if (this.editMode) payload.id = this.id;

        const request$ = this.editMode ? this.service.update(payload) : this.service.create(payload);
        request$.subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.success === false) {
                    this.alertService.error(this.module, 'Save', data.failureMessage || 'Save failed.');
                } else {
                    this.alertService.success(this.module, this.editMode ? 'Updated' : 'Created', '');
                    const id = data?.modelId ?? data?.id ?? this.id;
                    this.router.navigate(['/' + this.menuLink, id, 'detail']);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Save', 'An error occurred.');
            }
        });
    }
```

- [ ] **Step 2: Write the complete rr-add-edit.component.html template**

The template has these sections:
1. Page title
2. Loading guard
3. Form with single `app-ui-card` containing:
   - Section "Basic Information": deliveryDate, deliveryNumber, invoiceDate, invoiceNumber, inventoryLocation, remarks
   - `<hr/>` separator
   - Section "Document Type": radio-like buttons for 5 types (PO / IFR / RV / JO / Tested)
   - Conditional browse row per type
   - `<hr/>` separator
   - Section "Items": table varies by type
   - `<hr/>` separator
   - Section "Signatories": Noted By, Approved By browse inputs
   - Footer: Back + Save buttons

```html
<div class="container-fluid">
    <app-page-title [title]="module" [subTitle]="subModule" [menuLink]="menuLink"/>
</div>

<div class="container-fluid">
    @if (isLoading() && editMode) {
        <div class="d-flex align-items-center gap-2 text-muted py-4">
            <span class="spinner-border spinner-border-sm" role="status"></span> Loading...
        </div>
    } @else {
    <form (ngSubmit)="save()" #f="ngForm">
        <div class="row g-3">
            <div class="col-xl-12">
                <app-ui-card title="Receiving Report">
                    <div class="col-xl-12 p-3" card-body>

                        <!-- SECTION: Basic Information -->
                        <p class="fs-xs text-uppercase fw-semibold text-muted mb-2">Basic Information</p>
                        <div class="row g-3">
                            <div class="col-md-3">
                                <label class="form-label fw-bold">Delivery Date <span class="text-danger">*</span></label>
                                <input type="text" mwlFlatpickr [options]="flatpickrOptions"
                                       class="form-control" [(ngModel)]="deliveryDate"
                                       name="deliveryDate" placeholder="Select date"/>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label fw-bold">Delivery Receipt</label>
                                <input type="text" class="form-control" [(ngModel)]="deliveryNumber"
                                       name="deliveryNumber" placeholder="Delivery receipt number"/>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label fw-bold">Invoice Date <span class="text-danger">*</span></label>
                                <input type="text" mwlFlatpickr [options]="flatpickrOptions"
                                       class="form-control" [(ngModel)]="invoiceDate"
                                       name="invoiceDate" placeholder="Select date"/>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label fw-bold">Invoice Number</label>
                                <input type="text" class="form-control" [(ngModel)]="invoiceNumber"
                                       name="invoiceNumber" placeholder="Invoice number"/>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label fw-bold">Inventory Location <span class="text-danger">*</span></label>
                                <select class="form-select" [(ngModel)]="selectedInventoryLocation"
                                        name="inventoryLocation" [compareWith]="compareById">
                                    <option [ngValue]="null" disabled>Select Inventory Location</option>
                                    @for (loc of inventoryLocations(); track loc.id) {
                                        <option [ngValue]="loc">{{ loc.description }}</option>
                                    }
                                </select>
                            </div>
                            <div class="col-md-12">
                                <label class="form-label fw-bold">Remarks</label>
                                <textarea class="form-control" [(ngModel)]="remarks"
                                          name="remarks" rows="3" placeholder="Remarks"></textarea>
                            </div>
                        </div>

                        <hr class="my-4"/>

                        <!-- SECTION: Document Type -->
                        <p class="fs-xs text-uppercase fw-semibold text-muted mb-2">Document Type</p>
                        <div class="d-flex flex-wrap gap-2 mb-3">
                            <button type="button" (click)="selectDocType(1)"
                                    [class]="'btn fw-bold ' + (isPO ? 'btn-primary' : 'btn-outline-secondary')">
                                Purchase Order
                            </button>
                            <button type="button" (click)="selectDocType(2)"
                                    [class]="'btn fw-bold ' + (isIFR ? 'btn-primary' : 'btn-outline-secondary')">
                                Repaired Items
                            </button>
                            <button type="button" (click)="selectDocType(3)"
                                    [class]="'btn fw-bold ' + (isRV ? 'btn-primary' : 'btn-outline-secondary')">
                                Purchase Request
                            </button>
                            <button type="button" (click)="selectDocType(4)"
                                    [class]="'btn fw-bold ' + (isJO ? 'btn-primary' : 'btn-outline-secondary')">
                                Job Order
                            </button>
                            <button type="button" (click)="selectDocType(5)"
                                    [class]="'btn fw-bold ' + (isTested ? 'btn-primary' : 'btn-outline-secondary')">
                                Tested Items
                            </button>
                        </div>

                        <!-- Browse row per document type -->
                        <div class="row g-3">
                            @if (isPO || isTested) {
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">
                                        {{ isTested ? 'Item Testing (PO)' : 'Purchase Order' }}
                                        <span class="text-danger">*</span>
                                    </label>
                                    <div class="input-group">
                                        <input type="text" class="form-control" [value]="poDesc" readonly
                                               placeholder="Browse purchase order"/>
                                        <button type="button" class="btn btn-success fw-bold" (click)="openPOBrowse()">
                                            <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                                        </button>
                                    </div>
                                </div>
                            }
                            @if (isJO) {
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">Job Order <span class="text-danger">*</span></label>
                                    <div class="input-group">
                                        <input type="text" class="form-control" [value]="joDesc" readonly
                                               placeholder="Browse job order"/>
                                        <button type="button" class="btn btn-success fw-bold" (click)="openJOBrowse()">
                                            <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                                        </button>
                                    </div>
                                </div>
                            }
                            @if (isIFR) {
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">Items For Repair <span class="text-danger">*</span></label>
                                    <div class="input-group">
                                        <input type="text" class="form-control" [value]="ifrDesc" readonly
                                               placeholder="Browse items for repair"/>
                                        <button type="button" class="btn btn-success fw-bold" disabled>
                                            <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                                        </button>
                                    </div>
                                    <div class="form-text text-muted">IFR browse — pending integration</div>
                                </div>
                            }
                            @if (isRV) {
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">Request Voucher <span class="text-danger">*</span></label>
                                    <div class="input-group">
                                        <input type="text" class="form-control" [value]="rvDesc" readonly
                                               placeholder="Browse request voucher"/>
                                        <button type="button" class="btn btn-success fw-bold" disabled>
                                            <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                                        </button>
                                    </div>
                                    <div class="form-text text-muted">RV browse — pending integration</div>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">Supplier <span class="text-danger">*</span></label>
                                    <div class="input-group">
                                        <input type="text" class="form-control"
                                               [value]="selectedSupplier?.name || ''" readonly
                                               placeholder="Browse supplier"/>
                                        <button type="button" class="btn btn-success fw-bold" (click)="openSupplierBrowse()">
                                            <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                                        </button>
                                    </div>
                                </div>
                            }
                        </div>

                        <hr class="my-4"/>

                        <!-- SECTION: Items -->
                        <p class="fs-xs text-uppercase fw-semibold text-muted mb-2">
                            {{ isIFR ? 'Repaired Items' : isTested ? 'Tested Items' : 'Delivered Items' }}
                        </p>

                        <!-- PO / JO / Tested Items table -->
                        @if (isPO || isJO || isTested) {
                            <div class="table-responsive">
                                <table class="table table-custom table-centered w-100 mb-0">
                                    <thead class="bg-light bg-opacity-25 thead-sm">
                                        <tr class="text-uppercase fs-xxs">
                                            <th>#</th>
                                            <th>Description</th>
                                            <th class="text-center">Qty Ordered</th>
                                            <th>Unit</th>
                                            <th class="text-end">Price</th>
                                            <th class="text-end">Cost</th>
                                            <th class="text-end">Qty Rcvd (prev)</th>
                                            <th class="text-end" style="min-width:110px">Qty Received</th>
                                            <th class="text-end" style="min-width:110px">Adjustment</th>
                                            <th class="text-end">Net Amount</th>
                                            <th></th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        @for (item of rrDetails; track $index; let i = $index) {
                                            <tr>
                                                <td class="text-center">{{ i + 1 }}</td>
                                                <td>{{ item.itemDescription }}</td>
                                                <td class="text-center fw-bold">{{ item.quantity }}</td>
                                                <td>{{ item.unitCode }}</td>
                                                <td class="text-end fw-bold text-warning">{{ item.unitPrice | number:'1.2-2' }}</td>
                                                <td class="text-end fw-bold text-danger">{{ item.itemAmount | number:'1.2-2' }}</td>
                                                <td class="text-end fw-bold">{{ item.deliveredQuantity }}</td>
                                                <td>
                                                    <input type="number" class="form-control form-control-sm text-end fw-bold"
                                                           [min]="0" [max]="item.quantity - (item.deliveredQuantity ?? 0)"
                                                           step="1" [(ngModel)]="item.quantityReceived"
                                                           [name]="'qtyRcvd_' + i"
                                                           (ngModelChange)="updateNetAmount(i, 'quantityReceived')"/>
                                                </td>
                                                <td>
                                                    <input type="number" class="form-control form-control-sm text-end fw-bold"
                                                           step="0.01" [(ngModel)]="item.adjustment"
                                                           [name]="'adj_' + i"
                                                           (ngModelChange)="updateNetAmount(i, 'adjustment')"/>
                                                </td>
                                                <td class="text-end fw-bold">{{ item.netAmount | number:'1.2-2' }}</td>
                                                <td>
                                                    <button type="button"
                                                            class="btn btn-light btn-icon btn-sm rounded-circle"
                                                            (click)="removeRow(i)">
                                                        <ng-icon name="tablerTrash" class="fs-lg text-danger"></ng-icon>
                                                    </button>
                                                </td>
                                            </tr>
                                        }
                                        <!-- Totals row -->
                                        <tr class="fw-bold text-primary">
                                            <td colspan="2">TOTAL:</td>
                                            <td class="text-center">{{ totals.qty | number:'1.2-2' }}</td>
                                            <td colspan="4" class="text-end">{{ totals.cost | number:'1.2-2' }}</td>
                                            <td class="text-end">{{ totals.qtyReceived | number:'1.2-2' }}</td>
                                            <td class="text-end">{{ totals.adjustment | number:'1.2-2' }}</td>
                                            <td class="text-end">{{ totals.netAmount | number:'1.2-2' }}</td>
                                            <td></td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        }

                        <!-- RV (Purchase Request) table -->
                        @if (isRV) {
                            <div class="table-responsive">
                                <table class="table table-custom table-centered w-100 mb-0">
                                    <thead class="bg-light bg-opacity-25 thead-sm">
                                        <tr class="text-uppercase fs-xxs">
                                            <th>#</th>
                                            <th>Description</th>
                                            <th class="text-center">Qty Requested</th>
                                            <th>Unit</th>
                                            <th class="text-end" style="min-width:110px">Price</th>
                                            <th class="text-end">Cost</th>
                                            <th class="text-end">Qty Delivered (prev)</th>
                                            <th class="text-end" style="min-width:110px">Qty Received</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        @for (item of rrDetails; track $index; let i = $index) {
                                            <tr>
                                                <td class="text-center">{{ i + 1 }}</td>
                                                <td>{{ item.itemDescription }}</td>
                                                <td class="text-center fw-bold">{{ item.quantity }}</td>
                                                <td>{{ item.unitCode }}</td>
                                                <td>
                                                    @if (item.itemDescription) {
                                                        <input type="number" class="form-control form-control-sm text-end fw-bold"
                                                               min="0" step="0.01" [(ngModel)]="item.unitPrice"
                                                               [name]="'price_' + i"
                                                               (ngModelChange)="updateNetAmount(i, 'unitPrice')"/>
                                                    }
                                                </td>
                                                <td class="text-end fw-bold text-danger">{{ item.itemAmount | number:'1.2-2' }}</td>
                                                <td class="text-end fw-bold">{{ item.deliveredQuantity }}</td>
                                                <td>
                                                    <input type="number" class="form-control form-control-sm text-end fw-bold"
                                                           min="0" step="1" [(ngModel)]="item.quantityReceived"
                                                           [name]="'qtyRcvd_' + i"
                                                           (ngModelChange)="updateNetAmount(i, 'quantityReceived')"/>
                                                </td>
                                            </tr>
                                        }
                                        <!-- Totals -->
                                        <tr class="fw-bold text-primary">
                                            <td colspan="2">TOTAL:</td>
                                            <td class="text-center">{{ totals.qty | number:'1.2-2' }}</td>
                                            <td colspan="3" class="text-end">{{ totals.netAmount | number:'1.2-2' }}</td>
                                            <td colspan="2" class="text-end">{{ totals.qtyReceived | number:'1.2-2' }}</td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        }

                        <!-- Repaired Items table -->
                        @if (isIFR) {
                            <div class="table-responsive">
                                <table class="table table-custom table-centered w-100 mb-0">
                                    <thead class="bg-light bg-opacity-25 thead-sm">
                                        <tr class="text-uppercase fs-xxs">
                                            <th>#</th>
                                            <th>IFR Item</th>
                                            <th class="text-end">IFR Qty</th>
                                            <th class="text-end">IFR Accepted Qty</th>
                                            <th></th>
                                            <th>Description</th>
                                            <th>Unit</th>
                                            <th class="text-end" style="min-width:110px">Price</th>
                                            <th class="text-end">Cost</th>
                                            <th class="text-end" style="min-width:110px">Delivered Qty</th>
                                            <th class="text-end" style="min-width:110px">Accepted Qty</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        @for (item of rrDetails; track $index; let i = $index) {
                                            <tr>
                                                <td class="text-center">{{ i + 1 }}</td>
                                                <td>{{ item.ifrItem?.description }}</td>
                                                <td class="text-end fw-bold">{{ item.ifrItem?.quantity }}</td>
                                                <td class="text-end fw-bold">{{ item.ifrItem?.deliveredQuantity }}</td>
                                                <td>
                                                    <button type="button"
                                                            class="btn btn-light btn-icon btn-sm rounded-circle"
                                                            [disabled]="item.hasItem"
                                                            (click)="openItemBrowse(i)">
                                                        <ng-icon name="tablerPlus" class="fs-lg"></ng-icon>
                                                    </button>
                                                </td>
                                                <td>{{ item.itemDescription }}</td>
                                                <td>{{ item.itemDescription ? item.unitCode : '' }}</td>
                                                <td>
                                                    @if (item.itemDescription) {
                                                        <input type="number" class="form-control form-control-sm text-end fw-bold"
                                                               min="0" step="0.01" [(ngModel)]="item.unitPrice"
                                                               [name]="'price_' + i"
                                                               (ngModelChange)="updateNetAmount(i, 'unitPrice')"/>
                                                    }
                                                </td>
                                                <td class="text-end fw-bold text-danger">
                                                    {{ item.itemDescription ? (item.itemAmount | number:'1.2-2') : '' }}
                                                </td>
                                                <td>
                                                    @if (item.itemDescription) {
                                                        <input type="number" class="form-control form-control-sm text-end fw-bold"
                                                               min="0" step="1" [(ngModel)]="item.deliveredQuantity"
                                                               [name]="'delQty_' + i"
                                                               (ngModelChange)="updateNetAmount(i, 'deliveredQuantity')"/>
                                                    }
                                                </td>
                                                <td>
                                                    @if (item.itemDescription) {
                                                        <input type="number" class="form-control form-control-sm text-end fw-bold"
                                                               min="0" step="1" [(ngModel)]="item.quantityReceived"
                                                               [name]="'qtyRcvd_' + i"
                                                               (ngModelChange)="updateNetAmount(i, 'quantityReceived')"/>
                                                    }
                                                </td>
                                            </tr>
                                        }
                                        <!-- Totals -->
                                        <tr class="fw-bold text-primary">
                                            <td colspan="7">TOTAL:</td>
                                            <td class="text-end">{{ totals.cost | number:'1.2-2' }}</td>
                                            <td colspan="3" class="text-end">{{ totals.qtyReceived | number:'1.2-2' }}</td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        }

                        @if (rrDetails.length === 0) {
                            <div class="text-center text-muted py-3 fst-italic">
                                No items. {{ (isPO || isTested) ? 'Browse a Purchase Order above to load items.' : (isJO ? 'Browse a Job Order above to load items.' : 'Browse a reference document above to load items.') }}
                            </div>
                        }

                        <hr class="my-4"/>

                        <!-- SECTION: Signatories -->
                        <p class="fs-xs text-uppercase fw-semibold text-muted mb-2">Signatories</p>
                        <div class="row g-3">
                            <div class="col-md-5">
                                <label class="form-label fw-bold">Noted By <span class="text-danger">*</span></label>
                                <div class="input-group">
                                    <input type="text" class="form-control"
                                           [value]="notedBy?.name || notedBy?.fullName || ''" readonly
                                           placeholder="Browse noting officer"/>
                                    <button type="button" class="btn btn-success fw-bold"
                                            (click)="openSignatoryBrowse('notedBy')">
                                        <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                                    </button>
                                </div>
                            </div>
                            <div class="col-md-5">
                                <label class="form-label fw-bold">Approved By <span class="text-danger">*</span></label>
                                <div class="input-group">
                                    <input type="text" class="form-control"
                                           [value]="approvedBy?.name || approvedBy?.fullName || ''" readonly
                                           placeholder="Browse approving officer"/>
                                    <button type="button" class="btn btn-success fw-bold"
                                            (click)="openSignatoryBrowse('approvedBy')">
                                        <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                                    </button>
                                </div>
                            </div>
                        </div>

                        <!-- Footer -->
                        <div class="d-flex gap-2 justify-content-end align-items-center pt-3 border-top mt-3">
                            <a [routerLink]="['/' + menuLink]" class="btn btn-light fw-bold">
                                <ng-icon name="tablerArrowLeft" class="ps-0 pe-3 fw-bold"></ng-icon>Back
                            </a>
                            <button type="submit" class="btn btn-primary fw-bold" [disabled]="isLoading()">
                                <ng-icon name="tablerCheck" class="ps-0 pe-3 fw-bold"></ng-icon>
                                {{ editMode ? 'Update' : 'Save ' + module }}
                            </button>
                        </div>

                    </div>
                </app-ui-card>
            </div>
        </div>
    </form>
    }
</div>
```

- [ ] **Step 3: Add compareById helper method to component class**

```typescript
    compareById(a: any, b: any): boolean {
        return a && b && a.id === b.id;
    }
```

---

## Task 6: Fix Detail Screen — Missing Fields and Items Table

**Files:**
- Modify: `frontend/src/app/pages/rr/rr-detail/rr-detail.component.html`

**Why:** The detail screen is missing deliveryNumber, invoiceNumber, invoiceDate, notedBy (checker), approvedBy, lastUpdated, the items table, and the attachments section. The component TS needs no changes (it already loads full `data` object from `getData()`).

- [ ] **Step 1: Read current rr-detail.component.html**

Already read above (lines 1-135).

- [ ] **Step 2: Replace the header info card to add missing fields**

Replace the current header card (first `<div class="card mb-3">` block) with:

```html
<div class="card mb-3">
    <div class="card-body">
        <div class="row g-3">
            <div class="col-md-3">
                <div class="fs-xs text-uppercase fw-bold text-muted mb-1">Code</div>
                <div class="fw-bold fs-5">{{ data.localCode || data.code || '—' }}</div>
            </div>
            <div class="col-md-3">
                <div class="fs-xs text-uppercase fw-bold text-muted mb-1">Status</div>
                <span class="badge bg-secondary bg-opacity-25 text-dark border">
                    {{ data.documentStatus?.status || '—' }}
                </span>
            </div>
            <div class="col-md-3">
                <div class="fs-xs text-uppercase fw-bold text-muted mb-1">Total Amount</div>
                <div class="fw-bold">{{ data.totalAmount | number:'1.2-2' }}</div>
            </div>
            <div class="col-md-3">
                <div class="fs-xs text-uppercase fw-bold text-muted mb-1">Delivery Date</div>
                <div>{{ data.deliveryDate | date:'MMMM d, yyyy' }}</div>
            </div>
            <div class="col-md-3">
                <div class="fs-xs text-uppercase fw-bold text-muted mb-1">Delivery Receipt</div>
                <div>{{ data.deliveryNumber || '—' }}</div>
            </div>
            <div class="col-md-3">
                <div class="fs-xs text-uppercase fw-bold text-muted mb-1">Invoice Date</div>
                <div>{{ data.invoiceDate ? (data.invoiceDate | date:'MMMM d, yyyy') : '—' }}</div>
            </div>
            <div class="col-md-3">
                <div class="fs-xs text-uppercase fw-bold text-muted mb-1">Invoice Number</div>
                <div>{{ data.invoiceNumber || '—' }}</div>
            </div>
            <div class="col-md-6">
                <div class="fs-xs text-uppercase fw-bold text-muted mb-1">Supplier</div>
                <div>{{ data.supplier?.name || data.vendor?.name || '—' }}</div>
            </div>
            <div class="col-md-3">
                <div class="fs-xs text-uppercase fw-bold text-muted mb-1">Prepared By</div>
                <div>{{ data.createdBy?.name || data.receivedBy || data.preparedBy || '—' }}</div>
            </div>
            <div class="col-md-3">
                <div class="fs-xs text-uppercase fw-bold text-muted mb-1">Noted By</div>
                <div>{{ data.checker?.name || '—' }}</div>
            </div>
            <div class="col-md-3">
                <div class="fs-xs text-uppercase fw-bold text-muted mb-1">Approved By</div>
                <div>{{ data.approvingOfficer?.name || '—' }}</div>
            </div>
            <div class="col-md-3">
                <div class="fs-xs text-uppercase fw-bold text-muted mb-1">Last Updated</div>
                <div>{{ data.lastUpdated | date:'MMM dd, yyyy HH:mm' }}</div>
            </div>
            @if (data.remarks) {
                <div class="col-md-12">
                    <div class="fs-xs text-uppercase fw-bold text-muted mb-1">Remarks</div>
                    <div>{{ data.remarks }}</div>
                </div>
            }
        </div>
    </div>
</div>
```

- [ ] **Step 3: Add Items table card after the header info card (before workflow card)**

Insert after the header card and before `@if (!isTerminal() && workflowActions.length > 0)`:

```html
@if (data.rrDetails?.length > 0) {
<div class="card mb-3">
    <div class="card-header">
        <h6 class="fw-semibold text-uppercase fs-xs mb-0">Items</h6>
    </div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-custom table-centered w-100 mb-0">
                <thead class="bg-light bg-opacity-25 thead-sm">
                    <tr class="text-uppercase fs-xxs">
                        <th>#</th>
                        <th>Description</th>
                        <th class="text-center">Quantity</th>
                        <th>Unit</th>
                        <th class="text-end">Price</th>
                        <th class="text-end">Amount</th>
                        <th class="text-end">Adjustment</th>
                        <th class="text-end">Net Amount</th>
                    </tr>
                </thead>
                <tbody>
                    @for (item of data.rrDetails; track $index; let i = $index) {
                        <tr>
                            <td class="text-center">{{ i + 1 }}</td>
                            <td>{{ item.itemDescription }}</td>
                            <td class="text-center fw-bold">{{ item.quantityReceived }}</td>
                            <td>{{ item.unitCode }}</td>
                            <td class="text-end fw-bold text-warning">{{ item.unitPrice | number:'1.2-2' }}</td>
                            <td class="text-end fw-bold text-danger">{{ item.itemAmount | number:'1.2-2' }}</td>
                            <td class="text-end fw-bold">{{ item.adjustment | number:'1.2-2' }}</td>
                            <td class="text-end fw-bold text-primary">{{ item.netAmount | number:'1.2-2' }}</td>
                        </tr>
                    }
                </tbody>
            </table>
        </div>
    </div>
</div>
}
```

- [ ] **Step 4: Add attachments section before the logs card**

Insert after the action buttons card, before the logs card:

```html
@if (data.attachments?.length > 0) {
<div class="card mt-3">
    <div class="card-header">
        <h6 class="fw-semibold text-uppercase fs-xs mb-0">Attachments</h6>
    </div>
    <div class="card-body">
        <ul class="list-unstyled mb-0">
            @for (f of data.attachments; track f.id) {
                <li class="mb-1">
                    <a [href]="f.url" target="_blank" class="text-primary">
                        <ng-icon name="tablerPaperclip" class="me-1"></ng-icon>{{ f.oFile || f.fileName }}
                    </a>
                </li>
            }
        </ul>
    </div>
</div>
}
```

Note: `tablerPaperclip` needs to be added to the `provideIcons` in `rr-detail.component.ts`. Add to the component's provider icons.

- [ ] **Step 5: Update rr-detail.component.ts to add tablerPaperclip icon**

Add `provideIcons` with `tablerPaperclip` to the component's providers:

```typescript
import { provideIcons } from '@ng-icons/core';
import { tablerPrinter, tablerEdit, tablerPaperclip } from '@ng-icons/tabler-icons';
```

And in component decorator:
```typescript
providers: [provideIcons({ tablerPrinter, tablerEdit, tablerPaperclip })]
```

---

## Remaining Issues / Out-of-Scope

The following items exist in OLD but are deferred to a follow-up:

1. **IFR (Items For Repair) browse modal** — No `browse-ifr-modal` exists in shared/modals. The IFR browse button is rendered as disabled with a note. To implement: create `BrowseItemsForRepairModalComponent` in shared/modals calling the IFR backend API, then wire it into the RR add/edit form.

2. **RV (Request Voucher) browse modal** — No RV document browser exists in shared/modals distinct from `BrowsePurchaseRequestModalComponent` (which browses PRs for PO/JO, not RV documents). The RV browse button is disabled with a note. To implement: determine if v2 uses the same concept; create or adapt a modal calling the RV backend API.

3. **Attachment upload on create/update** — OLD sent files as multipart. The v2 `create`/`update` service methods use JSON (no multipart). To implement: change `rr.service.ts` `create()`/`update()` to use `FormData` and add attachment file input to the form. This requires backend endpoint verification.

4. **Server-date validation (30-day rule)** — OLD checked that delivery date is not more than 30 days before server date. Add `getServerDate()` to service and enforce this in `save()` if the backend does not already reject such dates.

---

## Validation Checklist

After completing all tasks:

- [ ] Run `ng build` or `npm run build` from `frontend/` — must compile with 0 errors
- [ ] Verify template `@if`/`@for` syntax (no `*ngIf`/`*ngFor`)
- [ ] Verify all `[(ngModel)]` bindings have a `name` attribute
- [ ] Verify `compareById` is used for object-valued selects
- [ ] Verify `tablerPaperclip` is imported and provided in detail component
- [ ] Verify browse modal methods use `try { ... } catch { }` pattern
- [ ] Verify `AlertService.fieldWarning` signature: `(module, action, messages[])` — 3 args
- [ ] Verify `filteredTotal` getter used in pagination

---

## Summary

| Area | Gap | Fix |
|------|-----|-----|
| List | Missing text search | Added `searchText` filter with live filtering |
| Add/Edit | Missing 13+ fields | Full component rewrite with all OLD fields |
| Add/Edit | Missing 5 doc type checkboxes | Added button-group doc type selector |
| Add/Edit | Missing 4 browse refs (PO/JO/IFR/RV) | PO + JO wired; IFR + RV disabled pending modals |
| Add/Edit | Missing 3 items table variants | All 3 variants implemented |
| Add/Edit | Missing net amount calc | `updateNetAmount()` + `updateTotals()` per OLD logic |
| Add/Edit | Missing Noted By / Approved By | Wired to `BrowseEntityModalComponent` |
| Add/Edit | Missing default signatories | Loaded via `getDefaultSignatories()` on create |
| Add/Edit | Missing full validation | `fieldWarning()` with all required checks |
| Detail | Missing 6 header fields | All added to header card |
| Detail | Missing items table | Added as card before workflow section |
| Detail | Missing attachments | Added conditional attachments card |
