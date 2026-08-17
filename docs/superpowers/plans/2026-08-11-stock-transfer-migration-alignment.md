# Stock Transfer Migration Alignment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bring the Angular 19 Stock Transfer module to feature parity with the legacy JSP/AngularJS implementation.

**Architecture:** Five targeted tasks — augment service, create item-stock browse modal, fix text search + field names in main, fully overhaul add-edit, fix detail. No new routes or modules.

**Tech Stack:** Angular 19, TypeScript, NgBootstrap, ng-icons (tabler), ModalService, AlertService, signals, OnPush change detection.

## Global Constraints

- Angular 19 control flow ONLY: `@if`, `@for`, `@else` — NEVER `*ngIf` / `*ngFor`
- `provideIcons({...})` goes in component `providers[]`, NEVER in `imports[]`
- `ChangeDetectionStrategy.OnPush` + `cdr.markForCheck()` in all new browse modal components
- `ModalService.openModal(Component, data, options)` injects data via `Object.assign(componentInstance, data)` — use plain properties (not `@Input()`) on modal components
- All notifications via `AlertService` — no native `alert()`/`confirm()`
- No `git commit` or `git push` — user handles all git operations
- MERGE_BASE: `1e5090ff210e95060644e2d0090c3e033f81ce61`
- Unique `[name]="'field_' + $index"` on every `<input>` inside `@for` loops
- `[compareWith]="compareById"` on every `<select>` bound to an object value

---

### Task 1: Augment StockTransferService

**Files:**
- Modify: `frontend/src/app/pages/stock-transfer/stock-transfer.service.ts`

**Interfaces:**
- Produces: `getDefaultSignatories()`, `getInventoryLocations()`, `getItemStocks(locationId)` — used by Tasks 2 and 4

- [ ] **Step 1: Read the file**

Read `frontend/src/app/pages/stock-transfer/stock-transfer.service.ts` to confirm current content.

- [ ] **Step 2: Add three methods after `print()`**

Insert the following three methods immediately after the `print(id: number): void { ... }` method, before the closing `}` of the class:

```typescript
    getDefaultSignatories(): Observable<any> {
        return this.http.get(`${BASE_API}/stock-transfer/default-signatories`);
    }

    getInventoryLocations(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/stock-transfer/inventory-locations`);
    }

    getItemStocks(locationId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/stock-transfer/item-stocks/${locationId}`);
    }
```

- [ ] **Step 3: Verify**

Read the file after editing and confirm all three methods are present using `BASE_API` (not `BASE_URL`).

---

### Task 2: Create BrowseStItemStockModalComponent

**Files:**
- Create: `frontend/src/app/shared/modals/browse-st-item-stock-modal/browse-st-item-stock-modal.component.ts`
- Create: `frontend/src/app/shared/modals/browse-st-item-stock-modal/browse-st-item-stock-modal.component.html`

**Interfaces:**
- Consumes: `StockTransferService.getItemStocks(locationId: number)` from Task 1
- Produces: `BrowseStItemStockModalComponent` — opened via `ModalService.openModal(BrowseStItemStockModalComponent, { locationId }, { size: 'xl', centered: true })`; result shape `{ action: 'select', data: itemStock }`

- [ ] **Step 1: Verify directory does not exist**

Confirm `frontend/src/app/shared/modals/browse-st-item-stock-modal/` does not yet exist. Do NOT touch `browse-item-stock-modal/` (withdrawal-specific) or `browse-sa-item-stock-modal/` (SA-specific).

- [ ] **Step 2: Write the TypeScript file**

Write `frontend/src/app/shared/modals/browse-st-item-stock-modal/browse-st-item-stock-modal.component.ts` with this exact content:

```typescript
import { Component, ChangeDetectionStrategy, ChangeDetectorRef, inject } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { StockTransferService } from '@/app/pages/stock-transfer/stock-transfer.service';

@Component({
    selector: 'app-browse-st-item-stock-modal',
    templateUrl: './browse-st-item-stock-modal.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS]
})
export class BrowseStItemStockModalComponent {
    locationId: number = 0;

    records:   any[] = [];
    isLoading        = false;
    searchText       = '';

    page     = 1;
    pageSize = 10;

    get filtered(): any[] {
        const q = this.searchText.toLowerCase();
        return q
            ? this.records.filter(r =>
                (r.item?.code        || '').toLowerCase().includes(q) ||
                (r.item?.description || '').toLowerCase().includes(q))
            : this.records;
    }

    get filteredTotal(): number { return this.filtered.length; }

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.filtered.slice(start, start + this.pageSize);
    }

    private activeModal = inject(NgbActiveModal);
    private service     = inject(StockTransferService);
    private cdr         = inject(ChangeDetectorRef);

    ngOnInit(): void {
        if (this.locationId) this.load();
    }

    load(): void {
        this.isLoading = true;
        this.service.getItemStocks(this.locationId).subscribe({
            next: (data) => { this.records = data || []; this.isLoading = false; this.cdr.markForCheck(); },
            error: () => { this.isLoading = false; this.cdr.markForCheck(); }
        });
    }

    select(item: any): void {
        this.activeModal.close({ action: 'select', data: item });
    }

    dismiss(): void { this.activeModal.dismiss(); }
}
```

- [ ] **Step 3: Write the HTML file**

Write `frontend/src/app/shared/modals/browse-st-item-stock-modal/browse-st-item-stock-modal.component.html` with this exact content:

```html
<div class="modal-header">
    <h6 class="modal-title fw-semibold text-uppercase fs-xs">Browse Item Stock</h6>
    <button type="button" class="btn-close" (click)="dismiss()"></button>
</div>
<div class="modal-body">
    <div class="row mb-3">
        <div class="col-md-4">
            <input type="text" class="form-control form-control-sm"
                   placeholder="Search code or description..."
                   [(ngModel)]="searchText" (ngModelChange)="page = 1"/>
        </div>
    </div>
    <div class="table-responsive">
        <table class="table table-custom table-centered table-hover w-100 mb-0">
            <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                <tr class="text-uppercase fs-xxs">
                    <th>Code</th>
                    <th>Description</th>
                    <th>Unit</th>
                    <th class="text-center">Stock Qty</th>
                    <th class="text-end">Unit Cost</th>
                </tr>
            </thead>
            <tbody>
                @if (isLoading) {
                    <tr><td colspan="5" class="text-center py-3">
                        <span class="spinner-border spinner-border-sm me-2"></span>Loading...
                    </td></tr>
                } @else if (pagedRecords.length === 0) {
                    <tr><td colspan="5" class="text-center text-muted py-3">No items found.</td></tr>
                } @else {
                    @for (item of pagedRecords; track item.id) {
                        <tr style="cursor:pointer" (click)="select(item)">
                            <td class="fw-bold">{{ item.item?.code || item.code }}</td>
                            <td>{{ item.item?.description || item.description }}</td>
                            <td>{{ item.item?.unit?.code || item.unitCode }}</td>
                            <td class="text-center">{{ (item.totalQuantity ?? item.quantity) | number:'1.0-3' }}</td>
                            <td class="text-end">{{ item.unitCost | number:'1.2-2' }}</td>
                        </tr>
                    }
                }
            </tbody>
        </table>
    </div>
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
</div>
<div class="modal-footer">
    <button type="button" class="btn btn-light fw-bold" (click)="dismiss()">Cancel</button>
</div>
```

- [ ] **Step 4: Verify files**

Read both files back and confirm content is correct.

---

### Task 3: Fix stock-transfer-main (text search + correct field names)

**Files:**
- Modify: `frontend/src/app/pages/stock-transfer/stock-transfer-main/stock-transfer-main.component.ts`
- Modify: `frontend/src/app/pages/stock-transfer/stock-transfer-main/stock-transfer-main.component.html`

**Interfaces:**
- No dependencies on other tasks

- [ ] **Step 1: Read both files**

Read both files before editing.

- [ ] **Step 2: Update stock-transfer-main.component.ts**

1. Add `searchText = '';` after `selectedStatus = signal<number | null>(null);`

2. Replace the existing `pagedRecords` getter with these three getters:

```typescript
    get filteredRecords(): any[] {
        const q = this.searchText.toLowerCase();
        return q
            ? this.records().filter(r =>
                (r.code                                || '').toLowerCase().includes(q) ||
                (r.fromInventoryLocation?.description  || '').toLowerCase().includes(q) ||
                (r.toInventoryLocation?.description    || '').toLowerCase().includes(q) ||
                (r.remarks                             || '').toLowerCase().includes(q) ||
                (r.createdBy?.fullName || r.preparedBy || '').toLowerCase().includes(q))
            : this.records();
    }

    get filteredTotal(): number { return this.filteredRecords.length; }

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.filteredRecords.slice(start, start + this.pageSize);
    }
```

3. Update `reset()` to also clear `searchText`:

```typescript
    reset(): void { this.setDefaultDates(); this.selectedStatus.set(null); this.searchText = ''; this.load(); }
```

- [ ] **Step 3: Update stock-transfer-main.component.html**

3a. Fix the two wrong field name cells in the table body. Change:

```html
                                    <td>{{ rec.fromOffice?.name || rec.fromOffice || '—' }}</td>
                                    <td>{{ rec.toOffice?.name || rec.toOffice || '—' }}</td>
```

To:

```html
                                    <td>{{ rec.fromInventoryLocation?.description || rec.fromInventoryLocation?.name || '—' }}</td>
                                    <td>{{ rec.toInventoryLocation?.description || rec.toInventoryLocation?.name || '—' }}</td>
```

3b. Add a search input `col-md-auto` to the filter row, between the Status col and the Search button col (before `<div class="col-md-auto">` that contains the Search button):

```html
                <div class="col-md-auto">
                    <label class="form-label fw-bold mb-1">Search</label>
                    <input type="text" class="form-control" [(ngModel)]="searchText"
                           (ngModelChange)="page = 1" placeholder="Code, source, dest..."/>
                </div>
```

3c. Update pagination `[collectionSize]`:
- Change `[collectionSize]="records().length"` → `[collectionSize]="filteredTotal"`

- [ ] **Step 4: Verify files**

Read both files and confirm: `searchText` property, 3 getters correct, `reset()` clears searchText, field names fixed in table body, search input present, pagination uses `filteredTotal`.

---

### Task 4: Full overhaul of stock-transfer-add-edit

**Files:**
- Overwrite: `frontend/src/app/pages/stock-transfer/stock-transfer-add-edit/stock-transfer-add-edit.component.ts`
- Overwrite: `frontend/src/app/pages/stock-transfer/stock-transfer-add-edit/stock-transfer-add-edit.component.html`

**Interfaces:**
- Consumes: `StockTransferService.getDefaultSignatories()`, `StockTransferService.getInventoryLocations()`, `StockTransferService.getItemStocks(locationId)` from Task 1
- Consumes: `BrowseStItemStockModalComponent` from Task 2
- Consumes: `BrowseEntityModalComponent` at `@/app/shared/modals/browse-entity-modal/browse-entity-modal.component`
- Consumes: `ModalService` at `@/app/shared/modals/modal-service`

- [ ] **Step 1: Read current files**

Read both current files before overwriting.

- [ ] **Step 2: Write stock-transfer-add-edit.component.ts**

Write the complete TypeScript file with this exact content:

```typescript
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { StockTransferService } from '../stock-transfer.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseStItemStockModalComponent } from '@/app/shared/modals/browse-st-item-stock-modal/browse-st-item-stock-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerTrash, tablerArrowLeft, tablerCheck, tablerPlus } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-stock-transfer-add-edit',
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
    templateUrl: './stock-transfer-add-edit.component.html'
})
export class StockTransferAddEditComponent {
    module    = 'Stock Transfer';
    subModule = 'Create';
    menuLink  = 'stock-transfer';

    id: any  = null;
    editMode = false;
    isLoading = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // Header fields
    voucherDate = '';
    remarks     = '';

    // Locations
    inventoryLocations   = signal<any[]>([]);
    selectedFromLocation: any = null;
    selectedToLocation:   any = null;

    // Computed: to-locations exclude the selected from-location
    get toInventoryLocations(): any[] {
        return this.inventoryLocations().filter(l => l.id !== this.selectedFromLocation?.id);
    }

    // Signatory
    approvedBy: any = null;

    // Items
    details: any[] = [];

    private service      = inject(StockTransferService);
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
                if (data) { this.approvedBy = data.approvedBy || null; }
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
                this.remarks     = data.remarks || '';
                this.approvedBy  = data.approvingOfficer || null;
                this.details     = (data.details || []).map((d: any) => ({
                    ...d,
                    stockQuantity: d.stockQuantity ?? d.quantity ?? 0
                }));

                // Match from inventory location after dropdown loads
                const tryMatchFrom = () => {
                    if (data.fromInventoryLocation?.id) {
                        this.selectedFromLocation = this.inventoryLocations().find(l => l.id === data.fromInventoryLocation.id) ?? data.fromInventoryLocation;
                    }
                };
                tryMatchFrom();
                setTimeout(tryMatchFrom, 400);

                // Match to inventory location after dropdown loads
                const tryMatchTo = () => {
                    if (data.toInventoryLocation?.id) {
                        this.selectedToLocation = this.inventoryLocations().find(l => l.id === data.toInventoryLocation.id) ?? data.toInventoryLocation;
                    }
                };
                tryMatchTo();
                setTimeout(tryMatchTo, 400);
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
        if (!this.selectedFromLocation?.id) {
            this.alertService.warning(this.module, 'Validation', 'Please select a Source Location first.');
            return;
        }
        try {
            const result = await this.modalService.openModal(
                BrowseStItemStockModalComponent,
                { locationId: this.selectedFromLocation.id },
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
            itemStockId:         itemStockId,
            itemId:              itemStock.item?.id          ?? null,
            itemCode:            itemStock.item?.code        ?? itemStock.code ?? '',
            unitCode:            itemStock.item?.unit?.code  ?? itemStock.unitCode ?? '',
            unitCost:            Number(itemStock.unitCost)  || 0,
            itemDescription:     itemStock.item?.description ?? itemStock.description ?? '',
            stockQuantity:       Number(itemStock.totalQuantity ?? itemStock.quantity) || 0,
            quantity:            0,
            inventoryLocationId: itemStock.inventoryLocation?.id ?? this.selectedFromLocation?.id ?? null
        }];
    }

    onQuantityChange(index: number): void {
        const row = this.details[index];
        if (!row) return;
        const qty   = Number(row.quantity)      || 0;
        const stock = Number(row.stockQuantity) || 0;
        if (qty < 0) {
            this.details[index] = { ...row, quantity: 0 };
        } else if (stock > 0 && qty > stock) {
            this.details[index] = { ...row, quantity: stock };
            this.alertService.warning(this.module, 'Validation',
                `Transfer quantity for ${row.itemCode} cannot exceed available stock (${stock}).`);
        }
    }

    removeRow(index: number): void {
        this.details = this.details.filter((_, i) => i !== index);
    }

    // ─── Browse: Signatory ────────────────────────────────────────────────────

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
        if (!this.selectedFromLocation?.id) {
            this.alertService.warning(this.module, 'Validation', 'Source Location is required.');
            return;
        }
        if (!this.selectedToLocation?.id) {
            this.alertService.warning(this.module, 'Validation', 'Destination Location is required.');
            return;
        }
        if (this.selectedFromLocation?.id === this.selectedToLocation?.id) {
            this.alertService.warning(this.module, 'Validation', 'Source and Destination must be different.');
            return;
        }
        if (this.details.length === 0) {
            this.alertService.warning(this.module, 'Validation', 'No items selected. Please add at least one item.');
            return;
        }
        const invalidQty = this.details.find(d => !Number(d.quantity) || Number(d.quantity) <= 0);
        if (invalidQty) {
            this.alertService.warning(this.module, 'Validation',
                `Transfer quantity for "${invalidQty.itemCode}" must be greater than zero.`);
            return;
        }
        if (!this.approvedBy?.accountNo) {
            this.alertService.warning(this.module, 'Validation', 'Approved By is required.');
            return;
        }

        this.isLoading.set(true);

        const payload: any = {
            voucherDate:          this.voucherDate,
            remarks:              this.remarks.trim(),
            fromInventoryLocation: { id: this.selectedFromLocation.id },
            toInventoryLocation:   { id: this.selectedToLocation.id },
            approvingOfficer:      { accountNo: this.approvedBy.accountNo, fullName: this.approvedBy.fullName },
            details:              this.details.map(d => ({
                itemStockId:         d.itemStockId         || null,
                itemId:              d.itemId              || null,
                itemCode:            d.itemCode            || '',
                unitCode:            d.unitCode            || '',
                unitCost:            Number(d.unitCost)    || 0,
                itemDescription:     d.itemDescription     || '',
                quantity:            Number(d.quantity)    || 0,
                inventoryLocationId: d.inventoryLocationId || null
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

- [ ] **Step 3: Write stock-transfer-add-edit.component.html**

Write the complete HTML file with this exact content:

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
                <app-ui-card title="Stock Transfer Information">
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
                                <label class="form-label fw-bold">Source Location <span class="text-danger">*</span></label>
                                <select class="form-select"
                                        [(ngModel)]="selectedFromLocation"
                                        name="fromInventoryLocation"
                                        [compareWith]="compareById"
                                        [disabled]="details.length > 0">
                                    <option [ngValue]="null">— Select Source —</option>
                                    @for (loc of inventoryLocations(); track loc.id) {
                                        <option [ngValue]="loc">{{ loc.description || loc.name }}</option>
                                    }
                                </select>
                                @if (details.length > 0) {
                                    <small class="text-muted">Clear items to change source.</small>
                                }
                            </div>
                            <div class="col-md-3">
                                <label class="form-label fw-bold">Destination Location <span class="text-danger">*</span></label>
                                <select class="form-select"
                                        [(ngModel)]="selectedToLocation"
                                        name="toInventoryLocation"
                                        [compareWith]="compareById">
                                    <option [ngValue]="null">— Select Destination —</option>
                                    @for (loc of toInventoryLocations; track loc.id) {
                                        <option [ngValue]="loc">{{ loc.description || loc.name }}</option>
                                    }
                                </select>
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
                            <div class="fs-xs text-uppercase fw-semibold text-muted">Items</div>
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
                                        <th class="text-center" style="width:110px">Stock Qty</th>
                                        <th class="text-center" style="width:130px">Transfer Qty</th>
                                        <th style="width:50px"></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    @if (details.length === 0) {
                                        <tr>
                                            <td colspan="7" class="text-center text-muted py-3">
                                                Select a <strong>Source Location</strong> then click <strong>Add Item</strong> to add items.
                                            </td>
                                        </tr>
                                    }
                                    @for (row of details; track $index) {
                                        <tr>
                                            <td class="text-center text-muted">{{ $index + 1 }}</td>
                                            <td class="fw-bold">{{ row.itemCode }}</td>
                                            <td>{{ row.itemDescription }}</td>
                                            <td>{{ row.unitCode }}</td>
                                            <td class="text-center fw-bold text-muted">{{ row.stockQuantity | number:'1.0-3' }}</td>
                                            <td>
                                                <input type="number"
                                                       class="form-control form-control-sm text-center fw-bold"
                                                       [(ngModel)]="row.quantity"
                                                       [name]="'qty_' + $index"
                                                       (ngModelChange)="onQuantityChange($index)"
                                                       step="0.01" min="0" placeholder="0"/>
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

                        <!-- ─── Section: Signatory ────────────────────────────── -->
                        <hr/>
                        <div class="col-12 fs-xs text-uppercase fw-semibold text-muted mb-2">Signatory</div>

                        <div class="row g-3">
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

- [ ] **Step 4: Verify files**

Read both files and verify:
- TS: All imports present, `provideIcons` in `providers[]`, `toInventoryLocations` getter filters out selectedFromLocation, 7 validations in `save()` (including same-location check), `compareById` defined, 8 fields in `details.map()`
- HTML: No `*ngIf`/`*ngFor`, `[name]="'qty_' + $index"`, `[compareWith]="compareById"` on both selects, `[disabled]="details.length > 0"` on Source select, `toInventoryLocations` used for destination options, `colspan="7"` in empty row

---

### Task 5: Fix stock-transfer-detail

**Files:**
- Modify: `frontend/src/app/pages/stock-transfer/stock-transfer-detail/stock-transfer-detail.component.ts`
- Modify: `frontend/src/app/pages/stock-transfer/stock-transfer-detail/stock-transfer-detail.component.html`

**Interfaces:**
- No dependencies on other tasks

- [ ] **Step 1: Read both files**

Read both files before editing.

- [ ] **Step 2: Update stock-transfer-detail.component.ts**

2a. Add these two import lines after `import { StockTransferService } from '../stock-transfer.service';`:

```typescript
import { provideIcons } from '@ng-icons/core';
import { tablerPrinter, tablerEdit, tablerArrowLeft } from '@ng-icons/tabler-icons';
```

2b. Add `providers` to the `@Component` decorator (currently it has no `providers`). Change:

```typescript
@Component({
    selector: 'app-stock-transfer-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule, FormsModule, RouterLink],
    templateUrl: './stock-transfer-detail.component.html'
})
```

To:

```typescript
@Component({
    selector: 'app-stock-transfer-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule, FormsModule, RouterLink],
    templateUrl: './stock-transfer-detail.component.html',
    providers: [provideIcons({ tablerPrinter, tablerEdit, tablerArrowLeft })]
})
```

2c. Fix `isEditable()` — change `'For Revision'` to `'Returned to Creator'`:

```typescript
    isEditable(): boolean { const s = this.data?.documentStatus?.status || ''; return s === 'Document Created' || s === 'Returned to Creator'; }
```

- [ ] **Step 3: Update stock-transfer-detail.component.html — fix header fields**

The current header card has these wrong field expressions:
- `data.fromOffice?.name || data.fromOffice || '—'` (Source field)
- `data.toOffice?.name || data.toOffice || '—'` (Destination field)

Fix both:
- Change Source to: `data.fromInventoryLocation?.description || data.fromInventoryLocation?.name || '—'`
- Change Destination to: `data.toInventoryLocation?.description || data.toInventoryLocation?.name || '—'`

Also add an Approved By field after Prepared By. After the Prepared By `col-md-3` block, insert:

```html
            <div class="col-md-3"><div class="fs-xs text-uppercase fw-bold text-muted mb-1">Approved By</div><div>{{ data.approvingOfficer?.fullName || data.approvingOfficer?.name || '—' }}</div></div>
```

- [ ] **Step 4: Update stock-transfer-detail.component.html — add Items Transferred card**

Insert the Items Transferred card between the closing `</div>` of the header card (the one that closes `class="card mb-3"`) and the workflow `@if (!isTerminal() && workflowActions.length > 0)` block:

```html
@if (data.details?.length > 0) {
<div class="card mb-3">
    <div class="card-header">
        <h6 class="fw-semibold text-uppercase fs-xs mb-0">Items Transferred</h6>
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

- [ ] **Step 5: Verify files**

Read both files and confirm:
- TS: `provideIcons` imports present, `providers` array in `@Component`, `isEditable()` uses `'Returned to Creator'`
- HTML: No `*ngIf`/`*ngFor`; Source uses `fromInventoryLocation?.description`; Destination uses `toInventoryLocation?.description`; Approved By field present; Items Transferred card with 7 columns and `@if (data.details?.length > 0)` guard; card appears BEFORE the workflow `@if` block
