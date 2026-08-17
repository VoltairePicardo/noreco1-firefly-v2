# MST Migration Alignment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bring the Angular 19 MST (Material Salvage Ticket) module to feature parity with the legacy JSP/AngularJS implementation.

**Architecture:** Five targeted tasks — augment the service, create an item-stock browse modal, add text search to the list page, fully overhaul the add-edit form, and fix the detail view. No new routes or modules are added.

**Tech Stack:** Angular 19, TypeScript, NgBootstrap, ng-icons (tabler), ModalService, AlertService, signals, OnPush change detection.

## Global Constraints

- Angular 19 control flow ONLY: `@if`, `@for`, `@else` — NEVER `*ngIf` / `*ngFor`
- `provideIcons({...})` goes in component `providers[]`, NEVER in `imports[]`
- `ChangeDetectionStrategy.OnPush` + `cdr.markForCheck()` in all new browse modal components
- `ModalService.openModal(Component, data, options)` injects data via `Object.assign(componentInstance, data)` — use plain properties (not `@Input()`) on modal components
- All notifications via `AlertService` — no native `alert()`/`confirm()`
- No `git commit` or `git push` — user handles all git operations
- MERGE_BASE: `1e5090ff210e95060644e2d0090c3e033f81ce61`
- Unique `[name]="'field_' + $index"` on every `<input>` / `<select>` / `<textarea>` inside `@for` loops
- `[compareWith]="compareById"` on every `<select>` bound to an object value

---

### Task 1: Augment MstService

**Files:**
- Modify: `frontend/src/app/pages/mst/mst.service.ts`

**Interfaces:**
- Produces: `getDefaultSignatories()`, `getDepartments()`, `getInventoryLocations()`, `getItemStocks(locationId)` — used by Tasks 2, 4

- [ ] **Step 1: Read the file**

Read `frontend/src/app/pages/mst/mst.service.ts` to confirm current content before editing.

- [ ] **Step 2: Add four methods after `print()`**

Insert the following four methods immediately after the `print(id: number): void { ... }` method (after its closing brace, before the closing `}`  of the class):

```typescript
    getDefaultSignatories(): Observable<any> {
        return this.http.get(`${BASE_API}/mst/default-signatories`);
    }

    getDepartments(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/mst/departments`);
    }

    getInventoryLocations(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/mst/inventory-locations`);
    }

    getItemStocks(locationId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/mst/item-stocks/${locationId}`);
    }
```

- [ ] **Step 3: Verify no compile errors**

Read the file after editing; visually confirm that all four methods are present, all `Observable` generics are correctly typed, and `BASE_API` is used (not `BASE_URL`).

---

### Task 2: Create BrowseMstItemStockModalComponent

**Files:**
- Create: `frontend/src/app/shared/modals/browse-mst-item-stock-modal/browse-mst-item-stock-modal.component.ts`
- Create: `frontend/src/app/shared/modals/browse-mst-item-stock-modal/browse-mst-item-stock-modal.component.html`

**Interfaces:**
- Consumes: `MstService.getItemStocks(locationId: number)` from Task 1
- Produces: `BrowseMstItemStockModalComponent` — used by Task 4 via `ModalService.openModal(BrowseMstItemStockModalComponent, { locationId }, { size: 'xl', centered: true })`; result shape `{ action: 'select', data: itemStock }`

- [ ] **Step 1: Verify the directory for existing files**

Run `ls frontend/src/app/shared/modals/` to confirm `browse-mst-item-stock-modal/` does NOT yet exist. (The existing `browse-item-stock-modal/` belongs to the withdrawal module — do NOT touch it.)

- [ ] **Step 2: Write the TypeScript file**

Write `frontend/src/app/shared/modals/browse-mst-item-stock-modal/browse-mst-item-stock-modal.component.ts` with this exact content:

```typescript
import { Component, ChangeDetectionStrategy, ChangeDetectorRef, inject } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { MstService } from '@/app/pages/mst/mst.service';

@Component({
    selector: 'app-browse-mst-item-stock-modal',
    templateUrl: './browse-mst-item-stock-modal.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS]
})
export class BrowseMstItemStockModalComponent {
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
    private service     = inject(MstService);
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

Write `frontend/src/app/shared/modals/browse-mst-item-stock-modal/browse-mst-item-stock-modal.component.html` with this exact content:

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
                    <th class="text-center">Quantity</th>
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

- [ ] **Step 4: Verify files exist**

Read both files back to confirm content is correct.

---

### Task 3: Add text search to mst-main

**Files:**
- Modify: `frontend/src/app/pages/mst/mst-main/mst-main.component.ts`
- Modify: `frontend/src/app/pages/mst/mst-main/mst-main.component.html`

**Interfaces:**
- No dependencies on other tasks

- [ ] **Step 1: Read both files**

Read both files to confirm current state before editing.

- [ ] **Step 2: Update mst-main.component.ts**

The current file has `searchText` missing and `pagedRecords` getter slices `this.records()` directly. Make these changes:

1. Add `searchText = '';` property after `selectedStatus = signal<number | null>(null);`

2. Add `filteredRecords`, `filteredTotal` getters, and update `pagedRecords` — replace the existing `pagedRecords` getter with:

```typescript
    get filteredRecords(): any[] {
        const q = this.searchText.toLowerCase();
        return q
            ? this.records().filter(r =>
                (r.code             || '').toLowerCase().includes(q) ||
                (r.department?.name || '').toLowerCase().includes(q) ||
                (r.purpose          || r.remarks || '').toLowerCase().includes(q) ||
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
    reset(): void {
        this.setDefaultDates();
        this.selectedStatus.set(null);
        this.searchText = '';
        this.load();
    }
```

- [ ] **Step 3: Update mst-main.component.html**

Add a search input `col-md-3` to the filter row, and update pagination `[collectionSize]` to `filteredTotal`.

3a. In the filter row (the `div.row.mb-3.align-items-end.g-2`), insert a new `col-md-3` after the Status select column (before `col-auto d-flex gap-2`):

```html
                    <div class="col-md-3">
                        <label class="form-label fw-bold mb-1 fs-xs text-uppercase">Search</label>
                        <input type="text" class="form-control" [(ngModel)]="searchText"
                               (ngModelChange)="page = 1" placeholder="Code, dept, purpose..."/>
                    </div>
```

3b. Update pagination `[collectionSize]`:
- Change `[collectionSize]="records().length"` → `[collectionSize]="filteredTotal"`

- [ ] **Step 4: Verify files**

Read both files and confirm: `searchText` property exists, `filteredRecords`/`filteredTotal`/`pagedRecords` getters correct, search input present in HTML, pagination uses `filteredTotal`.

---

### Task 4: Full overhaul of mst-add-edit

**Files:**
- Overwrite: `frontend/src/app/pages/mst/mst-add-edit/mst-add-edit.component.ts`
- Overwrite: `frontend/src/app/pages/mst/mst-add-edit/mst-add-edit.component.html`

**Interfaces:**
- Consumes: `MstService.getDefaultSignatories()`, `MstService.getDepartments()`, `MstService.getInventoryLocations()`, `MstService.getItemStocks(locationId)` from Task 1
- Consumes: `BrowseMstItemStockModalComponent` from Task 2
- Consumes: `BrowseEntityModalComponent` at `@/app/shared/modals/browse-entity-modal/browse-entity-modal.component`
- Consumes: `ModalService` at `@/app/shared/modals/modal-service`

- [ ] **Step 1: Read current files**

Read both current files to understand what will be replaced.

- [ ] **Step 2: Write mst-add-edit.component.ts**

Write the complete TypeScript file with this exact content:

```typescript
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { MstService } from '../mst.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseMstItemStockModalComponent } from '@/app/shared/modals/browse-mst-item-stock-modal/browse-mst-item-stock-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerTrash, tablerArrowLeft, tablerCheck, tablerPlus } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-mst-add-edit',
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
    templateUrl: './mst-add-edit.component.html'
})
export class MstAddEditComponent {
    module    = 'Material Salvage Ticket';
    subModule = 'Create';
    menuLink  = 'mst';

    id: any  = null;
    editMode = false;
    isLoading = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // Header fields
    voucherDate = '';
    purpose     = '';

    // Dropdowns
    departments        = signal<any[]>([]);
    inventoryLocations = signal<any[]>([]);
    selectedDepartment: any = null;
    selectedLocation:   any = null;

    // Signatories
    returnedBy: any = null;
    receivedBy: any = null;

    // Items
    details: any[] = [];

    private service      = inject(MstService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    ngOnInit(): void {
        this.loadDepartments();
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

    private loadDepartments(): void {
        this.service.getDepartments().subscribe({
            next: (d) => this.departments.set(d || []),
            error: () => {}
        });
    }

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
                    this.returnedBy = data.returnedBy || null;
                    this.receivedBy = data.receivedBy || null;
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
                this.purpose     = data.purpose || data.remarks || '';
                this.returnedBy  = data.returnedBy || null;
                this.receivedBy  = data.receivedBy || null;
                this.details     = (data.details || []).map((d: any) => ({ ...d }));

                // Match department after dropdown loads
                const tryMatchDept = () => {
                    if (data.department?.id) {
                        this.selectedDepartment = this.departments().find(d => d.id === data.department.id) ?? data.department;
                    }
                };
                tryMatchDept();
                setTimeout(tryMatchDept, 400);

                // Match inventory location after dropdown loads
                const tryMatchLoc = () => {
                    if (data.inventoryLocation?.id) {
                        this.selectedLocation = this.inventoryLocations().find(l => l.id === data.inventoryLocation.id) ?? data.inventoryLocation;
                    }
                };
                tryMatchLoc();
                setTimeout(tryMatchLoc, 400);
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
                BrowseMstItemStockModalComponent,
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
            itemStockId:         itemStockId,
            itemId:              itemStock.item?.id          ?? null,
            itemCode:            itemStock.item?.code        ?? itemStock.code ?? '',
            unitCode:            itemStock.item?.unit?.code  ?? itemStock.unitCode ?? '',
            unitCost:            Number(itemStock.unitCost)  || 0,
            itemDescription:     itemStock.item?.description ?? itemStock.description ?? '',
            quantity:            0,
            inventoryLocationId: itemStock.inventoryLocation?.id ?? this.selectedLocation?.id ?? null,
            isUsable:            true
        }];
    }

    onQuantityChange(index: number): void {
        const row = this.details[index];
        if (!row) return;
        const qty = Number(row.quantity) || 0;
        if (qty < 0) {
            this.details[index] = { ...row, quantity: 0 };
        }
    }

    removeRow(index: number): void {
        this.details = this.details.filter((_, i) => i !== index);
    }

    // ─── Browse: Signatories ──────────────────────────────────────────────────

    async openReturnedByBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const e = result.data;
                this.returnedBy = { accountNo: e.accountNo, fullName: e.fullName || e.name };
            }
        } catch { }
    }

    async openReceivedByBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const e = result.data;
                this.receivedBy = { accountNo: e.accountNo, fullName: e.fullName || e.name };
            }
        } catch { }
    }

    // ─── Save ─────────────────────────────────────────────────────────────────

    save(): void {
        if (!this.voucherDate) {
            this.alertService.warning(this.module, 'Validation', 'Date is required.');
            return;
        }
        if (!this.purpose?.trim()) {
            this.alertService.warning(this.module, 'Validation', 'Purpose is required.');
            return;
        }
        if (!this.selectedDepartment?.id) {
            this.alertService.warning(this.module, 'Validation', 'Department is required.');
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
        const invalidQty = this.details.find(d => !Number(d.quantity) || Number(d.quantity) <= 0);
        if (invalidQty) {
            this.alertService.warning(this.module, 'Validation',
                `Quantity for item "${invalidQty.itemCode}" must be greater than zero.`);
            return;
        }
        if (!this.returnedBy?.accountNo) {
            this.alertService.warning(this.module, 'Validation', 'Returned By is required.');
            return;
        }
        if (!this.receivedBy?.accountNo) {
            this.alertService.warning(this.module, 'Validation', 'Received By is required.');
            return;
        }

        this.isLoading.set(true);

        const payload: any = {
            voucherDate:       this.voucherDate,
            purpose:           this.purpose.trim(),
            inventoryLocation: { id: this.selectedLocation.id },
            department:        { id: this.selectedDepartment.id },
            returnedBy:        { accountNo: this.returnedBy.accountNo, fullName: this.returnedBy.fullName },
            receivedBy:        { accountNo: this.receivedBy.accountNo, fullName: this.receivedBy.fullName },
            details:           this.details.map(d => ({
                itemStockId:         d.itemStockId         || null,
                itemId:              d.itemId              || null,
                itemCode:            d.itemCode            || '',
                unitCode:            d.unitCode            || '',
                unitCost:            Number(d.unitCost)    || 0,
                itemDescription:     d.itemDescription     || '',
                quantity:            Number(d.quantity)    || 0,
                inventoryLocationId: d.inventoryLocationId || null,
                isUsable:            d.isUsable !== false
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

- [ ] **Step 3: Write mst-add-edit.component.html**

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
                <app-ui-card title="Material Salvage Ticket Information">
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
                                <label class="form-label fw-bold">Department <span class="text-danger">*</span></label>
                                <select class="form-select"
                                        [(ngModel)]="selectedDepartment"
                                        name="department"
                                        [compareWith]="compareById">
                                    <option [ngValue]="null">— Select Department —</option>
                                    @for (dept of departments(); track dept.id) {
                                        <option [ngValue]="dept">{{ dept.name || dept.description }}</option>
                                    }
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

                        <!-- ─── Section: Purpose ───────────────────────────────── -->
                        <hr/>
                        <div class="row g-3">
                            <div class="col-md-12">
                                <label class="form-label fw-bold">Purpose <span class="text-danger">*</span></label>
                                <textarea class="form-control" [(ngModel)]="purpose" name="purpose"
                                          rows="2" placeholder="Required purpose" maxlength="1024"></textarea>
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
                                        <th class="text-center" style="width:110px">Quantity</th>
                                        <th class="text-center" style="width:120px">Unit Cost</th>
                                        <th class="text-center" style="width:80px">Usable?</th>
                                        <th style="width:50px"></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    @if (details.length === 0) {
                                        <tr>
                                            <td colspan="8" class="text-center text-muted py-3">
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
                                            <td>
                                                <input type="number"
                                                       class="form-control form-control-sm text-center fw-bold"
                                                       [(ngModel)]="row.quantity"
                                                       [name]="'qty_' + $index"
                                                       (ngModelChange)="onQuantityChange($index)"
                                                       step="0.01" min="0" placeholder="0"/>
                                            </td>
                                            <td>
                                                <input type="number"
                                                       class="form-control form-control-sm text-end"
                                                       [(ngModel)]="row.unitCost"
                                                       [name]="'uc_' + $index"
                                                       step="0.01" min="0" placeholder="0.00"/>
                                            </td>
                                            <td class="text-center">
                                                <input type="checkbox"
                                                       class="form-check-input"
                                                       [(ngModel)]="row.isUsable"
                                                       [name]="'usable_' + $index"/>
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
                                <label class="form-label fw-bold">Returned By <span class="text-danger">*</span></label>
                                <div class="input-group">
                                    <input type="text" class="form-control"
                                           [value]="returnedBy ? (returnedBy.fullName || returnedBy.name || '') : ''"
                                           readonly placeholder="Browse returned by..."/>
                                    <button type="button" class="btn btn-success fw-bold"
                                            (click)="openReturnedByBrowse()">
                                        <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                                    </button>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label fw-bold">Received By <span class="text-danger">*</span></label>
                                <div class="input-group">
                                    <input type="text" class="form-control"
                                           [value]="receivedBy ? (receivedBy.fullName || receivedBy.name || '') : ''"
                                           readonly placeholder="Browse received by..."/>
                                    <button type="button" class="btn btn-success fw-bold"
                                            (click)="openReceivedByBrowse()">
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
- TS: All imports present, `provideIcons` in `providers[]`, 8 validations, `compareById` defined, `details[]` mapped with 9 fields (`isUsable: d.isUsable !== false`)
- HTML: No `*ngIf`/`*ngFor`, `[name]="'qty_' + $index"`, `[name]="'uc_' + $index"`, `[name]="'usable_' + $index"`, `[compareWith]="compareById"` on both selects, `[disabled]="details.length > 0"` on location select

---

### Task 5: Fix mst-detail

**Files:**
- Modify: `frontend/src/app/pages/mst/mst-detail/mst-detail.component.ts`
- Modify: `frontend/src/app/pages/mst/mst-detail/mst-detail.component.html`

**Interfaces:**
- No dependencies on other tasks

- [ ] **Step 1: Read both files**

Read both files to confirm current state before editing.

- [ ] **Step 2: Update mst-detail.component.ts**

Make two changes:

2a. Add the import line for `provideIcons` and tabler icons after the existing imports (after line `import { MstService } from '../mst.service';`):

```typescript
import { provideIcons } from '@ng-icons/core';
import { tablerPrinter, tablerEdit, tablerArrowLeft } from '@ng-icons/tabler-icons';
```

2b. Add `providers` to the `@Component` decorator. The current decorator has no `providers` array. Add it:

```typescript
    providers: [provideIcons({ tablerPrinter, tablerEdit, tablerArrowLeft })]
```

2c. Fix `isEditable()` — change `'For Revision'` to `'Returned to Creator'` to match mst-main:

```typescript
    isEditable(): boolean {
        const s = this.data?.documentStatus?.status || '';
        return s === 'Document Created' || s === 'Returned to Creator';
    }
```

- [ ] **Step 3: Update mst-detail.component.html — add missing header fields**

Currently the header card has: Code, Date, Status, Department, Purpose, Prepared By.
Add Inventory Location, Returned By, Received By after Prepared By.

After the `Prepared By` `col-md-6` block, insert:

```html
            <div class="col-md-3">
                <div class="fs-xs text-uppercase fw-bold text-muted mb-1">Inventory Location</div>
                <div>{{ data.inventoryLocation?.description || data.inventoryLocation?.name || '—' }}</div>
            </div>
            <div class="col-md-3">
                <div class="fs-xs text-uppercase fw-bold text-muted mb-1">Returned By</div>
                <div>{{ data.returnedBy?.fullName || data.returnedBy?.name || '—' }}</div>
            </div>
            <div class="col-md-3">
                <div class="fs-xs text-uppercase fw-bold text-muted mb-1">Received By</div>
                <div>{{ data.receivedBy?.fullName || data.receivedBy?.name || '—' }}</div>
            </div>
```

- [ ] **Step 4: Update mst-detail.component.html — add Items Salvaged card**

Insert the Items Salvaged card between the closing `</div>` of the header card (after line 40, the `</div>` that closes `class="card mb-3"`) and the workflow actions `@if` block.

Insert this block:

```html
@if (data.details?.length > 0) {
<div class="card mb-3">
    <div class="card-header">
        <h6 class="fw-semibold text-uppercase fs-xs mb-0">Items Salvaged</h6>
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
                        <th class="text-center">Usable?</th>
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
                            <td class="text-center">
                                <span class="badge" [class]="item.isUsable ? 'bg-success bg-opacity-75' : 'bg-secondary bg-opacity-25 text-dark border'">
                                    {{ item.isUsable ? 'Yes' : 'No' }}
                                </span>
                            </td>
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

Read both files and verify:
- TS: `provideIcons` import present, `providers` array present in `@Component`, `isEditable()` uses `'Returned to Creator'`
- HTML: No `*ngIf`/`*ngFor`; 3 new header fields present (Inventory Location, Returned By, Received By); Items Salvaged card present with 8 columns and `@if (data.details?.length > 0)` guard; Items card appears before the workflow `@if` block
