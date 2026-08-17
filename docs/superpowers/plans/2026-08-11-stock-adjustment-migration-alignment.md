# Stock Adjustment Migration Alignment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bring the Angular 19 Stock Adjustment module to full feature parity with the legacy module — inventory location dropdown, item stock browse, full add-edit form with signatories and validations, items table on detail, and text search on the list.

**Architecture:** Stock Adjustment lets the user select an inventory location, then individually browse and add item stocks from that location to an adjustment list. Each item has a current quantity and an editable adjustment (+/-). Two signatories: Checked By and Approved By. Five tasks in dependency order: service → browse modal → main search → add-edit overhaul → detail fix.

**Tech Stack:** Angular 19, TypeScript signals, NgbModal/NgbActiveModal, ChangeDetectionStrategy.OnPush, COMMON_ALL_PAGE_IMPORTS / COMMON_MAIN_PAGE_IMPORTS / SHARED_PROVIDERS, provideIcons, AlertService, ModalService.

## Global Constraints
- Angular 19 control flow: `@if`, `@for`, `@else` — NEVER `*ngIf` / `*ngFor`
- `provideIcons({...})` in component `providers[]` — NOT in `imports[]`
- `ModalService.openModal(Component, data, options)` — data keys are assigned directly onto `componentInstance`; result shape `{ action: 'select', data: any }`
- `ChangeDetectionStrategy.OnPush` + `cdr.markForCheck()` in all browse modals
- `BrowseEntityModalComponent` for person/signatory browses — at `@/app/shared/modals/browse-entity-modal/browse-entity-modal.component`
- Table classes: `table table-custom table-centered table-select table-hover w-100 mb-0`; thead: `bg-light align-middle bg-opacity-25 thead-sm`; tr in thead: `text-uppercase fs-xxs`
- Unique `[name]="'field_' + $index"` on inputs inside `@for` loops
- `AlertService` for all notifications — no native alert/confirm
- No `git commit` or `git push` — user handles all git operations
- Button standards: Save=btn-primary, Browse/Search/Print=btn-success, Back/Cancel=btn-light, Remove/Delete=btn-danger; all fw-bold; icon-only table buttons: `btn-light btn-icon btn-sm rounded-circle`

---

### Task 1: Augment StockAdjustmentService with missing API methods

**Files:**
- Modify: `frontend/src/app/pages/stock-adjustment/stock-adjustment.service.ts`

**Interfaces:**
- Produces:
  - `getDefaultSignatories()` — used by Task 4 (pre-fill checker + approvedBy in create mode)
  - `getInventoryLocations()` — used by Task 4 (location dropdown) and Task 2 indirectly
  - `getItemStocks(locationId: number)` — used by Task 2 (browse modal data source)

- [ ] **Step 1: Read the current service file**

Open and read `frontend/src/app/pages/stock-adjustment/stock-adjustment.service.ts`.

- [ ] **Step 2: Add the three missing methods**

Append these three methods inside the `StockAdjustmentService` class, after the existing `print()` method:

```typescript
getDefaultSignatories(): Observable<any> {
    return this.http.get(`${BASE_API}/stock-adjustment/default-signatories`);
}

getInventoryLocations(): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_API}/stock-adjustment/inventory-locations`);
}

getItemStocks(locationId: number): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_API}/stock-adjustment/item-stocks/${locationId}`);
}
```

No new imports needed — `Observable` and `BASE_API` are already declared.

- [ ] **Step 3: Verify**

Read the file back. Confirm:
- All three methods present with exact URLs as written above
- No other lines were modified

- [ ] **Step 4: Report DONE**

---

### Task 2: Create BrowseItemStockModalComponent

**Files:**
- Create: `frontend/src/app/shared/modals/browse-item-stock-modal/browse-item-stock-modal.component.ts`
- Create: `frontend/src/app/shared/modals/browse-item-stock-modal/browse-item-stock-modal.component.html`

**Interfaces:**
- Consumes: `StockAdjustmentService.getItemStocks(locationId)` (Task 1) → `any[]`
- Input (set by ModalService): `locationId: number = 0` — the inventory location to filter items by
- Produces: `BrowseItemStockModalComponent` — used by Task 4's `openItemStockBrowse()`
- Result shape: `activeModal.close({ action: 'select', data: itemStock })` where `itemStock` has `id` (itemStockId), `item.id`, `item.code`, `item.unit.code`, `unitCost`, `item.description`, `totalQuantity`, `inventoryLocation.id`

- [ ] **Step 1: Create the TypeScript file**

Write the full content of `frontend/src/app/shared/modals/browse-item-stock-modal/browse-item-stock-modal.component.ts`:

```typescript
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch } from '@ng-icons/tabler-icons';
import { StockAdjustmentService } from '@/app/pages/stock-adjustment/stock-adjustment.service';

@Component({
    selector: 'app-browse-item-stock-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerSearch })],
    templateUrl: './browse-item-stock-modal.component.html'
})
export class BrowseItemStockModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private service = inject(StockAdjustmentService);
    private cdr = inject(ChangeDetectorRef);

    // Set by ModalService from openModal(Component, { locationId: X }, ...)
    locationId: number = 0;

    items: any[] = [];
    loading = false;
    searchText = '';

    ngOnInit(): void {
        this.loading = true;
        this.service.getItemStocks(this.locationId).subscribe({
            next: (data) => {
                this.items = data || [];
                this.loading = false;
                this.cdr.markForCheck();
            },
            error: () => {
                this.loading = false;
                this.cdr.markForCheck();
            }
        });
    }

    get filtered(): any[] {
        const q = this.searchText.toLowerCase();
        if (!q) return this.items;
        return this.items.filter(s =>
            s.item?.code?.toLowerCase().includes(q) ||
            s.code?.toLowerCase().includes(q) ||
            s.item?.description?.toLowerCase().includes(q) ||
            s.description?.toLowerCase().includes(q)
        );
    }

    select(itemStock: any): void {
        this.activeModal.close({ action: 'select', data: itemStock });
    }
}
```

- [ ] **Step 2: Create the HTML template**

Write the full content of `frontend/src/app/shared/modals/browse-item-stock-modal/browse-item-stock-modal.component.html`:

```html
<div class="modal-header">
    <h4 class="modal-title">Browse Item Stock</h4>
    <button type="button" class="btn-close" (click)="activeModal.dismiss()"></button>
</div>

<div class="modal-body">
    <div class="d-flex gap-2 mb-3">
        <div class="app-search flex-grow-1">
            <input ngbAutofocus [(ngModel)]="searchText" type="search" class="form-control"
                   placeholder="Search by code or description..."/>
            <ng-icon name="tablerSearch" class="app-search-icon text-muted"/>
        </div>
    </div>

    <div class="table-responsive" style="max-height: 420px; overflow-y: auto;">
        <table class="table table-custom table-centered table-select table-hover w-100 mb-0">
            <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                <tr class="text-uppercase fs-xxs">
                    <th>Code</th>
                    <th>Description</th>
                    <th>Unit</th>
                    <th class="text-center">Quantity</th>
                    <th class="text-end">Unit Cost</th>
                </tr>
            </thead>
            <tbody>
                @if (loading) {
                    <tr>
                        <td colspan="5" class="text-center py-4">
                            <div class="d-flex justify-content-center align-items-center gap-2 py-2 text-muted">
                                <span class="spinner-border spinner-border-sm"></span>
                                <span>Loading...</span>
                            </div>
                        </td>
                    </tr>
                } @else if (filtered.length === 0) {
                    <tr class="no-results">
                        <td colspan="5" class="text-center text-muted py-3">Nothing found.</td>
                    </tr>
                } @else {
                    @for (s of filtered; track s.id) {
                        <tr style="cursor: pointer" (click)="select(s)">
                            <td class="fw-bold">{{ s.item?.code || s.code || '—' }}</td>
                            <td>{{ s.item?.description || s.description || '—' }}</td>
                            <td>{{ s.item?.unit?.code || s.unitCode || '—' }}</td>
                            <td class="text-center fw-bold">{{ s.totalQuantity ?? s.quantity ?? 0 | number:'1.0-3' }}</td>
                            <td class="text-end">{{ s.unitCost | number:'1.2-2' }}</td>
                        </tr>
                    }
                }
            </tbody>
        </table>
    </div>
</div>
```

- [ ] **Step 3: Verify**

Read both files. Confirm:
- TS: `ChangeDetectionStrategy.OnPush`, `locationId: number = 0` property, `cdr.markForCheck()` in both next/error, `get filtered()` searches item code and description, `select()` closes with `{ action: 'select', data: itemStock }`
- HTML: 5-column table (Code, Description, Unit, Quantity, Unit Cost), `@if loading @else if empty @else @for`, `style="cursor: pointer"` on rows
- No `*ngIf` / `*ngFor`

- [ ] **Step 4: Report DONE**

---

### Task 3: Add text search filter to stock-adjustment-main

**Files:**
- Modify: `frontend/src/app/pages/stock-adjustment/stock-adjustment-main/stock-adjustment-main.component.ts`
- Modify: `frontend/src/app/pages/stock-adjustment/stock-adjustment-main/stock-adjustment-main.component.html`

**Interfaces:**
- Consumes: existing `records` signal, `page`, `pageSize`
- Produces: `searchText`, `filteredTotal`, updated `pagedRecords` — internal only

- [ ] **Step 1: Update the TypeScript file**

Read `stock-adjustment-main.component.ts`. Then make these targeted changes:

**Add** `searchText = '';` immediately after `pageSize = 10;`:
```typescript
page     = 1;
pageSize = 10;
searchText = '';
```

**Replace** the existing `pagedRecords` getter with these three getters:
```typescript
get filteredRecords(): any[] {
    const q = this.searchText.toLowerCase();
    if (!q) return this.records();
    return this.records().filter(r =>
        r.code?.toLowerCase().includes(q) ||
        r.type?.toLowerCase().includes(q) ||
        r.remarks?.toLowerCase().includes(q) ||
        r.description?.toLowerCase().includes(q) ||
        r.createdBy?.fullName?.toLowerCase().includes(q)
    );
}

get filteredTotal(): number { return this.filteredRecords.length; }

get pagedRecords(): any[] {
    const start = (this.page - 1) * this.pageSize;
    return this.filteredRecords.slice(start, start + this.pageSize);
}
```

**Update** `reset()` to also clear `searchText`:
```typescript
reset(): void {
    this.setDefaultDates();
    this.selectedStatus.set(null);
    this.searchText = '';
    this.load();
}
```

- [ ] **Step 2: Update the HTML template**

Read `stock-adjustment-main.component.html`. Then make these targeted changes:

**Add** a `col-md-3` search input after the Status `</div>` and before the buttons `<div class="col-auto...">`:
```html
<div class="col-md-3">
    <label class="form-label fw-bold mb-1 fs-xs text-uppercase">Search</label>
    <input type="text" class="form-control" [(ngModel)]="searchText"
           (ngModelChange)="page = 1" placeholder="Code, type, remarks, prepared by..."/>
</div>
```

**Replace** the pagination block — change `records().length` → `filteredTotal` in both the `@if` condition and `[collectionSize]`:
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

- [ ] **Step 3: Verify**

Read both files. Confirm:
- `filteredRecords`, `filteredTotal`, `pagedRecords` getters all present and correct
- Search input added with `(ngModelChange)="page = 1"`
- Pagination uses `filteredTotal`
- `reset()` clears `searchText`
- No `*ngIf` / `*ngFor`

- [ ] **Step 4: Report DONE**

---

### Task 4: Overhaul stock-adjustment-add-edit component

**Files:**
- Modify: `frontend/src/app/pages/stock-adjustment/stock-adjustment-add-edit/stock-adjustment-add-edit.component.ts`
- Modify: `frontend/src/app/pages/stock-adjustment/stock-adjustment-add-edit/stock-adjustment-add-edit.component.html`

**Interfaces:**
- Consumes:
  - `StockAdjustmentService.getDefaultSignatories()` (Task 1) → `{ checker: { accountNo, fullName }, approvedBy: { accountNo, fullName } }` — pre-fill in create mode
  - `StockAdjustmentService.getInventoryLocations()` (Task 1) → `any[]` — location dropdown
  - `BrowseItemStockModalComponent` (Task 2) — opened via `ModalService.openModal(BrowseItemStockModalComponent, { locationId: selectedLocation.id }, { size: 'xl', centered: true })`; result: `{ action: 'select', data: itemStock }` where itemStock has `id` (itemStockId), `item.id`, `item.code`, `item.unit.code`, `unitCost`, `item.description`, `totalQuantity`, `inventoryLocation.id`
  - `BrowseEntityModalComponent` — for Checked By and Approved By browses; result: `{ action: 'select', data: entity }` where entity has `accountNo`, `fullName`, `name`
  - `StockAdjustmentService.getData(id)` — for edit mode; returns object with `voucherDate`, `type`, `remarks`, `inventoryLocation`, `checker`, `approvingOfficer`, `details[]`
  - `StockAdjustmentService.create(payload)` and `StockAdjustmentService.update(payload)`

**Payload shape for create/update:**
```typescript
{
  id?: number,                                    // edit mode only
  voucherDate: string,                            // 'YYYY-MM-DD'
  type: string | null,                            // 'Increase' or 'Decrease' or null
  remarks: string,                                // required
  inventoryLocation: { id: number },
  checker: { accountNo: string, fullName: string },
  approvingOfficer: { accountNo: string, fullName: string },
  details: [{
    itemStockId: number | null,
    itemId: number | null,
    itemCode: string,
    unitCode: string,
    unitCost: number,
    itemDescription: string,
    quantity: number,      // current stock quantity (readonly reference)
    inventoryLocationId: number | null,
    adjustment: number     // +/- value entered by user
  }]
}
```

- [ ] **Step 1: Write the full TypeScript file**

Overwrite `frontend/src/app/pages/stock-adjustment/stock-adjustment-add-edit/stock-adjustment-add-edit.component.ts` with:

```typescript
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { StockAdjustmentService } from '../stock-adjustment.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseItemStockModalComponent } from '@/app/shared/modals/browse-item-stock-modal/browse-item-stock-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerTrash, tablerArrowLeft, tablerCheck, tablerPlus } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-stock-adjustment-add-edit',
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
        provideIcons({ tablerSearch, tablerTrash, tablerArrowLeft, tablerCheck, tablerPlus })
    ],
    templateUrl: './stock-adjustment-add-edit.component.html'
})
export class StockAdjustmentAddEditComponent {
    module    = 'Stock Adjustment';
    subModule = 'Create';
    menuLink  = 'stock-adjustment';

    id: any  = null;
    editMode = false;
    isLoading = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // Header fields
    voucherDate = '';
    type        = '';
    remarks     = '';

    // Location
    inventoryLocations  = signal<any[]>([]);
    selectedLocation: any = null;

    // Signatories
    checker:     any = null;
    approvedBy:  any = null;

    // Items
    details: any[] = [];

    private service      = inject(StockAdjustmentService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    ngOnInit(): void {
        this.loadInventoryLocations();

        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.editMode = idParam != null && /^\d+$/.test(idParam);
            if (this.editMode) {
                this.id = Number(idParam);
                this.subModule = 'Edit';
                this.loadForEdit();
            } else {
                this.subModule = 'Create';
                const today = new Date().toISOString().substring(0, 10);
                this.voucherDate = today;
                this.loadDefaultSignatories();
            }
        });
    }

    // ─── Data Loading ─────────────────────────────────────────────────────────

    private loadInventoryLocations(): void {
        this.service.getInventoryLocations().subscribe({
            next: (d) => this.inventoryLocations.set(d || []),
            error: () => {}
        });
    }

    private loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (data) => {
                if (data) {
                    this.checker    = data.checker    || null;
                    this.approvedBy = data.approvedBy || null;
                }
            },
            error: () => {}
        });
    }

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
                this.voucherDate = toYmd(data.voucherDate);
                this.type        = data.type || '';
                this.remarks     = data.remarks || '';
                this.checker     = data.checker    || null;
                this.approvedBy  = data.approvingOfficer || null;
                this.details     = (data.details || []).map((d: any) => ({ ...d }));

                // Match inventory location after dropdown loads
                const tryMatch = () => {
                    if (data.inventoryLocation?.id) {
                        this.selectedLocation = this.inventoryLocations().find(l => l.id === data.inventoryLocation.id) ?? data.inventoryLocation;
                    }
                };
                tryMatch();
                setTimeout(tryMatch, 400);
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load record.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    // ─── Items ────────────────────────────────────────────────────────────────

    async openItemStockBrowse(): Promise<void> {
        if (!this.selectedLocation?.id) {
            this.alertService.warning(this.module, 'Validation', 'Please select an Inventory Location first.');
            return;
        }
        try {
            const result = await this.modalService.openModal(
                BrowseItemStockModalComponent,
                { locationId: this.selectedLocation.id },
                { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.onItemStockSelected(result.data);
            }
        } catch { }
    }

    private onItemStockSelected(itemStock: any): void {
        const itemStockId = itemStock.id;
        const isDuplicate = this.details.some(d => d.itemStockId === itemStockId);
        if (isDuplicate) {
            this.alertService.error(this.module, 'Duplicate Item', 'This item is already in the list.');
            return;
        }
        this.details = [...this.details, {
            itemStockId:       itemStockId,
            itemId:            itemStock.item?.id            ?? null,
            itemCode:          itemStock.item?.code          ?? itemStock.code ?? '',
            unitCode:          itemStock.item?.unit?.code    ?? itemStock.unitCode ?? '',
            unitCost:          Number(itemStock.unitCost)    || 0,
            itemDescription:   itemStock.item?.description   ?? itemStock.description ?? '',
            quantity:          Number(itemStock.totalQuantity ?? itemStock.quantity) || 0,
            inventoryLocationId: itemStock.inventoryLocation?.id ?? this.selectedLocation?.id ?? null,
            adjustment:        0
        }];
    }

    onAdjustmentChange(index: number): void {
        const row = this.details[index];
        if (!row) return;
        const adj = Number(row.adjustment) || 0;
        const qty = Number(row.quantity)   || 0;
        if (adj < 0 && Math.abs(adj) > qty) {
            this.details[index] = { ...row, adjustment: -qty };
            this.alertService.warning(this.module, 'Validation',
                `Adjustment for ${row.itemCode} cannot reduce stock below zero (max decrease: ${qty}).`);
        }
    }

    removeRow(index: number): void {
        this.details = this.details.filter((_, i) => i !== index);
    }

    // ─── Browse: Signatories ──────────────────────────────────────────────────

    async openCheckerBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const e = result.data;
                this.checker = { accountNo: e.accountNo, fullName: e.fullName || e.name };
            }
        } catch { }
    }

    async openApprovedByBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const e = result.data;
                this.approvedBy = { accountNo: e.accountNo, fullName: e.fullName || e.name };
            }
        } catch { }
    }

    // ─── Save ─────────────────────────────────────────────────────────────────

    save(): void {
        if (!this.voucherDate) {
            this.alertService.warning(this.module, 'Validation', 'Date is required.');
            return;
        }
        if (!this.remarks?.trim()) {
            this.alertService.warning(this.module, 'Validation', 'Remarks is required.');
            return;
        }
        if (!this.selectedLocation?.id) {
            this.alertService.warning(this.module, 'Validation', 'Inventory Location is required.');
            return;
        }
        if (this.details.length === 0) {
            this.alertService.warning(this.module, 'Validation', 'No items selected. Please add at least one item.');
            return;
        }
        if (!this.checker?.accountNo) {
            this.alertService.warning(this.module, 'Validation', 'Checked By is required.');
            return;
        }
        if (!this.approvedBy?.accountNo) {
            this.alertService.warning(this.module, 'Validation', 'Approved By is required.');
            return;
        }

        this.isLoading.set(true);

        const payload: any = {
            voucherDate:       this.voucherDate,
            type:              this.type || null,
            remarks:           this.remarks.trim(),
            inventoryLocation: { id: this.selectedLocation.id },
            checker:           { accountNo: this.checker.accountNo,    fullName: this.checker.fullName },
            approvingOfficer:  { accountNo: this.approvedBy.accountNo, fullName: this.approvedBy.fullName },
            details:           this.details.map(d => ({
                itemStockId:       d.itemStockId       || null,
                itemId:            d.itemId            || null,
                itemCode:          d.itemCode          || '',
                unitCode:          d.unitCode          || '',
                unitCost:          Number(d.unitCost)  || 0,
                itemDescription:   d.itemDescription   || '',
                quantity:          Number(d.quantity)  || 0,
                inventoryLocationId: d.inventoryLocationId || null,
                adjustment:        Number(d.adjustment) || 0
            }))
        };
        if (this.editMode) payload.id = this.id;

        const request$ = this.editMode ? this.service.update(payload) : this.service.create(payload);

        request$.subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.success === false) {
                    this.alertService.error(this.module, 'Save', data.failureMessage || '');
                } else {
                    this.alertService.success(
                        this.module,
                        this.editMode ? 'Updated successfully.' : 'Created successfully.',
                        ''
                    );
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

    compareById(a: any, b: any): boolean {
        return a && b ? a.id === b.id : a === b;
    }
}
```

- [ ] **Step 2: Write the full HTML template**

Overwrite `frontend/src/app/pages/stock-adjustment/stock-adjustment-add-edit/stock-adjustment-add-edit.component.html` with:

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
                <app-ui-card title="Stock Adjustment Information">
                    <div class="col-xl-12 p-3" card-body>

                        <!-- ─── Section: Basic Info ─────────────────────────────── -->
                        <div class="row g-3">
                            <div class="col-md-3">
                                <label class="form-label fw-bold">Date <span class="text-danger">*</span></label>
                                <input type="text" mwlFlatpickr [options]="flatpickrOptions"
                                       class="form-control" [(ngModel)]="voucherDate"
                                       name="voucherDate" placeholder="Select date" required/>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label fw-bold">Type</label>
                                <select class="form-select" [(ngModel)]="type" name="type">
                                    <option value="">— Select type —</option>
                                    <option value="Increase">Increase</option>
                                    <option value="Decrease">Decrease</option>
                                </select>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label fw-bold">Inventory Location <span class="text-danger">*</span></label>
                                <select class="form-select"
                                        [(ngModel)]="selectedLocation"
                                        name="inventoryLocation"
                                        [compareWith]="compareById"
                                        [disabled]="details.length > 0">
                                    <option [ngValue]="null">— Select Location —</option>
                                    @for (loc of inventoryLocations(); track loc.id) {
                                        <option [ngValue]="loc">{{ loc.description || loc.name }}</option>
                                    }
                                </select>
                                @if (details.length > 0) {
                                    <small class="text-muted">Clear items to change location.</small>
                                }
                            </div>
                        </div>

                        <!-- ─── Section: Remarks ───────────────────────────────── -->
                        <hr/>
                        <div class="row g-3">
                            <div class="col-md-12">
                                <label class="form-label fw-bold">Remarks <span class="text-danger">*</span></label>
                                <textarea class="form-control" [(ngModel)]="remarks" name="remarks"
                                          rows="2" placeholder="Required remarks" maxlength="1024"></textarea>
                            </div>
                        </div>

                        <!-- ─── Section: Items Table ───────────────────────────── -->
                        <hr/>
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <div class="col-12 fs-xs text-uppercase fw-semibold text-muted">Items</div>
                            <button type="button" class="btn btn-success fw-bold btn-sm"
                                    (click)="openItemStockBrowse()">
                                <ng-icon name="tablerPlus" class="ps-0 pe-2 fw-bold"></ng-icon>Add Item
                            </button>
                        </div>

                        <div class="table-responsive">
                            <table class="table table-custom table-centered table-hover w-100 mb-0">
                                <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                                    <tr class="text-uppercase fs-xxs">
                                        <th style="width:40px">#</th>
                                        <th style="width:80px">Code</th>
                                        <th>Description</th>
                                        <th>Unit</th>
                                        <th class="text-center" style="width:110px">Quantity</th>
                                        <th class="text-center" style="width:130px">Adjustment (+/-)</th>
                                        <th style="width:50px"></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    @if (details.length === 0) {
                                        <tr>
                                            <td colspan="7" class="text-center text-muted py-3">
                                                Select an <strong>Inventory Location</strong> then click <strong>Add Item</strong> to add items.
                                            </td>
                                        </tr>
                                    }
                                    @for (row of details; track $index) {
                                        <tr>
                                            <td class="text-center text-muted">{{ $index + 1 }}</td>
                                            <td class="fw-bold">{{ row.itemCode }}</td>
                                            <td>{{ row.itemDescription }}</td>
                                            <td>{{ row.unitCode }}</td>
                                            <td class="text-center fw-bold text-muted">{{ row.quantity | number:'1.0-3' }}</td>
                                            <td>
                                                <input type="number"
                                                       class="form-control form-control-sm text-center fw-bold"
                                                       [(ngModel)]="row.adjustment"
                                                       [name]="'adj_' + $index"
                                                       (ngModelChange)="onAdjustmentChange($index)"
                                                       step="0.01" placeholder="0"/>
                                            </td>
                                            <td class="text-center">
                                                <button type="button"
                                                        class="btn btn-light btn-icon btn-sm rounded-circle"
                                                        (click)="removeRow($index)"
                                                        ngbTooltip="Remove">
                                                    <ng-icon name="tablerTrash" class="fs-lg text-danger"></ng-icon>
                                                </button>
                                            </td>
                                        </tr>
                                    }
                                </tbody>
                            </table>
                        </div>

                        <!-- ─── Section: Signatories ───────────────────────────── -->
                        <hr/>
                        <div class="col-12 fs-xs text-uppercase fw-semibold text-muted mb-2">Signatories</div>

                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label fw-bold">Checked By <span class="text-danger">*</span></label>
                                <div class="input-group">
                                    <input type="text" class="form-control"
                                           [value]="checker ? (checker.fullName || checker.name || '') : ''"
                                           readonly placeholder="Browse checker..."/>
                                    <button type="button" class="btn btn-success fw-bold"
                                            (click)="openCheckerBrowse()">
                                        <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                                    </button>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label fw-bold">Approved By <span class="text-danger">*</span></label>
                                <div class="input-group">
                                    <input type="text" class="form-control"
                                           [value]="approvedBy ? (approvedBy.fullName || approvedBy.name || '') : ''"
                                           readonly placeholder="Browse approving officer..."/>
                                    <button type="button" class="btn btn-success fw-bold"
                                            (click)="openApprovedByBrowse()">
                                        <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                                    </button>
                                </div>
                            </div>
                        </div>

                        <!-- ─── Footer ─────────────────────────────────────────── -->
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

- [ ] **Step 3: Verify both files**

Read both files. Confirm:
- TS: `BrowseItemStockModalComponent` and `BrowseEntityModalComponent` imported; `RouterLink` in imports array; `provideIcons({ tablerSearch, tablerTrash, tablerArrowLeft, tablerCheck, tablerPlus })` in providers; `loadInventoryLocations()` and `loadDefaultSignatories()` called in `ngOnInit()`; `onItemStockSelected()` checks for duplicates via `itemStockId` before pushing; `onAdjustmentChange()` caps negative adj at `-quantity`; `removeRow()` filters array; `save()` has 6 validations; payload has `inventoryLocation: { id }`, `checker: { accountNo, fullName }`, `approvingOfficer: { accountNo, fullName }`, `details[]` with all 9 fields; `compareById` defined
- HTML: Location dropdown with `[compareWith]="compareById"` and `[disabled]="details.length > 0"`; Add Item button calls `openItemStockBrowse()`; items table 7 columns with `[name]="'adj_' + $index"`; remove button per row; Checked By + Approved By browse inputs; `Save + module` in footer; no `*ngIf`/`*ngFor`

- [ ] **Step 4: Report DONE**

---

### Task 5: Fix stock-adjustment-detail component

**Files:**
- Modify: `frontend/src/app/pages/stock-adjustment/stock-adjustment-detail/stock-adjustment-detail.component.ts`
- Modify: `frontend/src/app/pages/stock-adjustment/stock-adjustment-detail/stock-adjustment-detail.component.html`

**Interfaces:**
- Consumes: existing `data` object — assumed to have `data.inventoryLocation`, `data.checker`, `data.approvingOfficer`, `data.details[]` with `itemCode`, `itemDescription`, `unitCode`, `quantity`, `adjustment`, `unitCost`, `totalCost`
- Produces: no new outputs — display expansion only

**Note:** The detail TS has no `providers` array yet. The HTML has Code, Date, Type, Status, Prepared By, Remarks — but is missing Inventory Location, Checked By, Approved By, and the items table.

- [ ] **Step 1: Update the TypeScript file**

Read `stock-adjustment-detail.component.ts`. Then:

**Add** two import lines after the existing imports block:
```typescript
import { provideIcons } from '@ng-icons/core';
import { tablerPrinter, tablerEdit, tablerArrowLeft } from '@ng-icons/tabler-icons';
```

**Add** `providers` to the `@Component` decorator:
```typescript
providers: [provideIcons({ tablerPrinter, tablerEdit, tablerArrowLeft })]
```

The decorator should become:
```typescript
@Component({
    selector: 'app-stock-adjustment-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule, FormsModule, RouterLink],
    templateUrl: './stock-adjustment-detail.component.html',
    providers: [provideIcons({ tablerPrinter, tablerEdit, tablerArrowLeft })]
})
```

- [ ] **Step 2: Update the HTML template**

Read `stock-adjustment-detail.component.html`. Then make two targeted changes:

**Change 1 — Add missing header fields.** In the header card, after the existing Prepared By `<div class="col-md-6">...</div>`, insert:

```html
<div class="col-md-3">
    <div class="fs-xs text-uppercase fw-bold text-muted mb-1">Inventory Location</div>
    <div>{{ data.inventoryLocation?.description || data.inventoryLocation?.name || '—' }}</div>
</div>
<div class="col-md-3">
    <div class="fs-xs text-uppercase fw-bold text-muted mb-1">Checked By</div>
    <div>{{ data.checker?.fullName || data.checker?.name || '—' }}</div>
</div>
<div class="col-md-3">
    <div class="fs-xs text-uppercase fw-bold text-muted mb-1">Approved By</div>
    <div>{{ data.approvingOfficer?.fullName || data.approvingOfficer?.name || '—' }}</div>
</div>
```

**Change 2 — Add items table card.** Insert before `@if (!isTerminal() && workflowActions.length > 0)`:

```html
@if (data.details?.length > 0) {
<div class="card mb-3">
    <div class="card-header">
        <h6 class="fw-semibold text-uppercase fs-xs mb-0">Items Adjusted</h6>
    </div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-custom table-centered w-100 mb-0">
                <thead class="bg-light bg-opacity-25 thead-sm">
                    <tr class="text-uppercase fs-xxs">
                        <th>#</th>
                        <th>Code</th>
                        <th>Description</th>
                        <th>Unit</th>
                        <th class="text-center">Quantity</th>
                        <th class="text-center">Adjustment</th>
                        <th class="text-end">Unit Cost</th>
                        <th class="text-end">Total Cost</th>
                    </tr>
                </thead>
                <tbody>
                    @for (item of data.details; track $index; let i = $index) {
                        <tr>
                            <td class="text-center">{{ i + 1 }}</td>
                            <td class="fw-bold">{{ item.itemCode }}</td>
                            <td>{{ item.itemDescription }}</td>
                            <td>{{ item.unitCode }}</td>
                            <td class="text-center fw-bold text-muted">{{ item.quantity | number:'1.0-3' }}</td>
                            <td class="text-center fw-bold text-primary">{{ item.adjustment | number:'1.0-3' }}</td>
                            <td class="text-end">{{ item.unitCost | number:'1.2-2' }}</td>
                            <td class="text-end fw-bold">{{ item.totalCost | number:'1.2-2' }}</td>
                        </tr>
                    }
                </tbody>
            </table>
        </div>
    </div>
</div>
}
```

- [ ] **Step 3: Verify both files**

Read both files. Confirm:
- TS: `provideIcons({ tablerPrinter, tablerEdit, tablerArrowLeft })` in providers; imports for `provideIcons` and tabler icons added; no other changes
- HTML: 3 new header fields (Inventory Location, Checked By, Approved By) added after Prepared By; Items Adjusted card with `@if (data.details?.length > 0)` — 8 columns (#, Code, Description, Unit, Quantity, Adjustment, Unit Cost, Total Cost); positioned before the workflow card; no `*ngIf`/`*ngFor`

- [ ] **Step 4: Report DONE**
