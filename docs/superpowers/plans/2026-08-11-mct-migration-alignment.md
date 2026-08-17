# MCT Migration Alignment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bring the Angular 19 MCT (Material Credit Ticket) module to full feature parity with the legacy module — stock-release browse modal, full add-edit form, items table on detail, and text search on the list.

**Architecture:** MCT is the inventory return flow: a stock release document is selected, its items are pre-loaded with editable return quantities, and an approvingOfficer (Received By) signs off. Five tasks in dependency order: service → browse modal → main search → add-edit overhaul → detail fix.

**Tech Stack:** Angular 19, TypeScript signals, NgbModal/NgbActiveModal, ChangeDetectionStrategy.OnPush, COMMON_ALL_PAGE_IMPORTS / COMMON_MAIN_PAGE_IMPORTS / SHARED_PROVIDERS, provideIcons, AlertService, ModalService.

## Global Constraints
- Angular 19 control flow: `@if`, `@for`, `@else` — NEVER `*ngIf` / `*ngFor`
- `provideIcons({...})` in component `providers[]` — NOT in `imports[]`
- `ModalService.openModal()` async/await; result shape `{ action: 'select', data: any }`
- `ChangeDetectionStrategy.OnPush` + `cdr.markForCheck()` in all browse modals
- `BrowseEntityModalComponent` for person/signatory browses — at `@/app/shared/modals/browse-entity-modal/browse-entity-modal.component`
- Table classes: `table table-custom table-centered table-select table-hover w-100 mb-0`; thead: `bg-light align-middle bg-opacity-25 thead-sm`; tr in thead: `text-uppercase fs-xxs`
- Unique `[name]="'field_' + $index"` on inputs inside `@for` loops
- `AlertService` for all notifications — no native alert/confirm
- No `git commit` or `git push` — user handles all git operations
- Button standards: Save=btn-primary, Browse/Search/Print=btn-success, Back/Cancel=btn-light, Remove/Delete=btn-danger; all fw-bold; icon-only table buttons: `btn-light btn-icon btn-sm rounded-circle`

---

### Task 1: Augment MctService with missing API methods

**Files:**
- Modify: `frontend/src/app/pages/mct/mct.service.ts`

**Interfaces:**
- Produces:
  - `getDefaultSignatories()` — used by Task 4 (pre-fill received by in create mode)
  - `getInventoryLocations()` — used by Task 4 (location dropdown)
  - `getStockReleasesForMct()` — used by Task 2 (browse modal data source)
  - `getStockReleaseDetails(id)` — used by Task 4 (load items of selected stock release)

- [ ] **Step 1: Read the current service file**

Open and read `frontend/src/app/pages/mct/mct.service.ts`.

- [ ] **Step 2: Add the four missing methods**

Append these four methods inside the `MctService` class, after the existing `print()` method:

```typescript
getDefaultSignatories(): Observable<any> {
    return this.http.get(`${BASE_API}/mct/default-signatories`);
}

getInventoryLocations(): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_API}/mct/inventory-locations`);
}

getStockReleasesForMct(): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_API}/mct/stock-releases`);
}

getStockReleaseDetails(id: number): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_API}/mct/stock-release-details/${id}`);
}
```

No new imports are needed — `Observable` and `BASE_API` are already declared.

- [ ] **Step 3: Verify**

Read the file back. Confirm:
- All four methods present with exact URLs as written above
- No other lines were modified
- The file still compiles (no new imports needed)

- [ ] **Step 4: Report DONE**

---

### Task 2: Create BrowseMctStockReleaseModalComponent

**Files:**
- Create: `frontend/src/app/shared/modals/browse-mct-stock-release-modal/browse-mct-stock-release-modal.component.ts`
- Create: `frontend/src/app/shared/modals/browse-mct-stock-release-modal/browse-mct-stock-release-modal.component.html`

**Interfaces:**
- Consumes: `MctService.getStockReleasesForMct()` (Task 1) → `any[]`
- Produces: `BrowseMctStockReleaseModalComponent` — used by Task 4's `openStockReleaseBrowse()`
- Result shape: `activeModal.close({ action: 'select', data: stockRelease })` where `stockRelease` has `id`, `code`, `voucherDate`, `inventoryLocation`, `documentStatus`, `createdBy`

Note: This is a new modal distinct from the existing `BrowseStockReleaseModalComponent` (that one uses `MaintenanceRecordService` for a different flow). Do NOT modify the existing modal.

- [ ] **Step 1: Create the TypeScript file**

Write the full content of `frontend/src/app/shared/modals/browse-mct-stock-release-modal/browse-mct-stock-release-modal.component.ts`:

```typescript
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch } from '@ng-icons/tabler-icons';
import { MctService } from '@/app/pages/mct/mct.service';

@Component({
    selector: 'app-browse-mct-stock-release-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerSearch })],
    templateUrl: './browse-mct-stock-release-modal.component.html'
})
export class BrowseMctStockReleaseModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private service = inject(MctService);
    private cdr = inject(ChangeDetectorRef);

    items: any[] = [];
    loading = false;
    searchText = '';

    ngOnInit(): void {
        this.loading = true;
        this.service.getStockReleasesForMct().subscribe({
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
        return this.items.filter(sr =>
            sr.code?.toLowerCase().includes(q) ||
            sr.inventoryLocation?.description?.toLowerCase().includes(q) ||
            sr.inventoryLocation?.name?.toLowerCase().includes(q) ||
            sr.createdBy?.fullName?.toLowerCase().includes(q)
        );
    }

    select(stockRelease: any): void {
        this.activeModal.close({ action: 'select', data: stockRelease });
    }
}
```

- [ ] **Step 2: Create the HTML template**

Write the full content of `frontend/src/app/shared/modals/browse-mct-stock-release-modal/browse-mct-stock-release-modal.component.html`:

```html
<div class="modal-header">
    <h4 class="modal-title">Browse Stock Release</h4>
    <button type="button" class="btn-close" (click)="activeModal.dismiss()"></button>
</div>

<div class="modal-body">
    <div class="d-flex gap-2 mb-3">
        <div class="app-search flex-grow-1">
            <input ngbAutofocus [(ngModel)]="searchText" type="search" class="form-control"
                   placeholder="Search by code, location, or prepared by..."/>
            <ng-icon name="tablerSearch" class="app-search-icon text-muted"/>
        </div>
    </div>

    <div class="table-responsive" style="max-height: 420px; overflow-y: auto;">
        <table class="table table-custom table-centered table-select table-hover w-100 mb-0">
            <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                <tr class="text-uppercase fs-xxs">
                    <th>Code</th>
                    <th>Date</th>
                    <th>Inventory Location</th>
                    <th>Prepared By</th>
                    <th>Status</th>
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
                    @for (sr of filtered; track sr.id) {
                        <tr style="cursor: pointer" (click)="select(sr)">
                            <td class="fw-bold">{{ sr.code }}</td>
                            <td>{{ sr.voucherDate | date:'MM/dd/yyyy' }}</td>
                            <td>{{ sr.inventoryLocation?.description || sr.inventoryLocation?.name || '—' }}</td>
                            <td>{{ sr.createdBy?.fullName || '—' }}</td>
                            <td>
                                <span class="badge bg-secondary bg-opacity-25 text-dark border">
                                    {{ sr.documentStatus?.status || '—' }}
                                </span>
                            </td>
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
- TS: `ChangeDetectionStrategy.OnPush`, `cdr.markForCheck()` in both next and error handlers, `get filtered()` searches code/location/createdBy, `select()` closes with `{ action: 'select', data: stockRelease }`
- HTML: 5-column table, `@if loading @else if empty @else @for`, `style="cursor: pointer"` on rows
- No `*ngIf` / `*ngFor`

- [ ] **Step 4: Report DONE**

---

### Task 3: Add text search filter to mct-main

**Files:**
- Modify: `frontend/src/app/pages/mct/mct-main/mct-main.component.ts`
- Modify: `frontend/src/app/pages/mct/mct-main/mct-main.component.html`

**Interfaces:**
- Consumes: existing `records` signal, `page`, `pageSize`
- Produces: `searchText`, `filteredTotal`, updated `pagedRecords` — internal only

- [ ] **Step 1: Update the TypeScript file**

Read `mct-main.component.ts`. Then make these targeted changes:

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

Read `mct-main.component.html`. Then make these targeted changes:

**Add** a `col-md-3` search input after the Status `</div>`:
```html
<div class="col-md-3">
    <label class="form-label fw-bold mb-1 fs-xs text-uppercase">Search</label>
    <input type="text" class="form-control" [(ngModel)]="searchText"
           (ngModelChange)="page = 1" placeholder="Code, description, returned by..."/>
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

### Task 4: Overhaul mct-add-edit component

**Files:**
- Modify: `frontend/src/app/pages/mct/mct-add-edit/mct-add-edit.component.ts`
- Modify: `frontend/src/app/pages/mct/mct-add-edit/mct-add-edit.component.html`

**Interfaces:**
- Consumes:
  - `MctService.getDefaultSignatories()` (Task 1) → `{ approvedBy: { accountNo, fullName } }` — pre-fill received by in create mode
  - `MctService.getInventoryLocations()` (Task 1) → `any[]` — location dropdown
  - `BrowseMctStockReleaseModalComponent` (Task 2) — opened via `ModalService.openModal(..., {}, { size: 'xl', centered: true })`; result: `{ action: 'select', data: stockRelease }` where `stockRelease` has `id`, `code`, `voucherDate`, `inventoryLocation`
  - `MctService.getStockReleaseDetails(id)` (Task 1) → `any[]` of `{ itemId, code, unitId, unitCode, description, unitCost, quantity }` — items from selected stock release; `quantity` here is the released qty (cap for return)
  - `BrowseEntityModalComponent` — for Received By; result: `{ action: 'select', data: entity }` where entity has `accountNo`, `fullName`, `name`
  - `MctService.getData(id)` — for edit mode; returns object with `voucherDate`, `remarks`, `approvingOfficer`, `inventoryLocation`, `stockRelease`, `details[]`
  - `MctService.create(payload)` and `MctService.update(payload)`

**Payload shape for create/update:**
```typescript
{
  id?: number,                               // edit mode only
  voucherDate: string,                       // 'YYYY-MM-DD'
  remarks: string | null,
  stockRelease: { id: number },              // selected stock release
  inventoryLocation: { id: number },
  approvingOfficer: { accountNo: string, fullName: string },
  details: [{
    itemId: number | null,
    itemCode: string,
    unitId: number | null,
    unitCode: string,
    itemDescription: string,
    quantity: number,           // return quantity entered by user
    quantityReleased: number,   // original released qty (cap)
    unitCost: number
  }]
}
```

- [ ] **Step 1: Write the full TypeScript file**

Overwrite `frontend/src/app/pages/mct/mct-add-edit/mct-add-edit.component.ts` with:

```typescript
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { MctService } from '../mct.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseMctStockReleaseModalComponent } from '@/app/shared/modals/browse-mct-stock-release-modal/browse-mct-stock-release-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerArrowLeft, tablerCheck } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-mct-add-edit',
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
        provideIcons({ tablerSearch, tablerArrowLeft, tablerCheck })
    ],
    templateUrl: './mct-add-edit.component.html'
})
export class MctAddEditComponent {
    module    = 'Material Credit Ticket';
    subModule = 'Create';
    menuLink  = 'mct';

    id: any  = null;
    editMode = false;
    isLoading = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // Header fields
    voucherDate = '';
    remarks     = '';

    // Dropdowns
    inventoryLocations  = signal<any[]>([]);
    selectedLocation: any = null;

    // Selected stock release (source document)
    selectedStockRelease: any = null;

    // Signatories
    approvingOfficer: any = null;

    // Items
    details: any[] = [];

    get totalQuantity(): number {
        return this.details.reduce((s, r) => s + (Number(r.quantity) || 0), 0);
    }

    private service      = inject(MctService);
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
                if (data) this.approvingOfficer = data.approvedBy || data.approvingOfficer || null;
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
                this.voucherDate      = toYmd(data.voucherDate);
                this.remarks          = data.remarks || '';
                this.approvingOfficer = data.approvingOfficer || null;
                this.details          = (data.details || []).map((d: any) => ({ ...d, quantity: Number(d.quantity) || 0 }));

                if (data.stockRelease) {
                    this.selectedStockRelease = { id: data.stockRelease.id, code: data.stockRelease.code || '—' };
                }

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

    // ─── Browse: Stock Release ────────────────────────────────────────────────

    async openStockReleaseBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseMctStockReleaseModalComponent, {}, { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.onStockReleaseSelected(result.data);
            }
        } catch { }
    }

    private onStockReleaseSelected(sr: any): void {
        this.selectedStockRelease = sr;
        // Auto-set inventory location from the selected stock release
        if (sr.inventoryLocation?.id) {
            this.selectedLocation = this.inventoryLocations().find(l => l.id === sr.inventoryLocation.id) ?? sr.inventoryLocation;
        }
        // Load items from the stock release
        this.isLoading.set(true);
        this.service.getStockReleaseDetails(sr.id).subscribe({
            next: (items) => {
                this.isLoading.set(false);
                this.details = (items || []).map((item: any) => ({
                    itemId:           item.itemId    || item.id    || null,
                    itemCode:         item.itemCode  || item.code  || '',
                    unitId:           item.unitId                  || null,
                    unitCode:         item.unitCode                || '',
                    itemDescription:  item.itemDescription || item.description || '',
                    unitCost:         Number(item.unitCost) || 0,
                    quantityReleased: Number(item.quantity || item.quantityReleased) || 0,
                    quantity:         0
                }));
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load stock release items.', '');
            }
        });
    }

    clearStockRelease(): void {
        this.selectedStockRelease = null;
        this.details = [];
    }

    // ─── Browse: Received By (Approving Officer) ──────────────────────────────

    async openApprovingOfficerBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const entity = result.data;
                this.approvingOfficer = { accountNo: entity.accountNo, fullName: entity.fullName || entity.name };
            }
        } catch { }
    }

    // ─── Items Helpers ────────────────────────────────────────────────────────

    onQuantityChange(index: number): void {
        const row = this.details[index];
        if (!row) return;
        const qty = Number(row.quantity) || 0;
        const max = Number(row.quantityReleased) || 0;
        if (qty > max) {
            row.quantity = max;
            this.alertService.warning(this.module, 'Validation',
                `Return quantity cannot exceed quantity released (${max}).`);
        }
        if (qty < 0) row.quantity = 0;
    }

    // ─── Save ─────────────────────────────────────────────────────────────────

    save(): void {
        if (!this.voucherDate) {
            this.alertService.warning(this.module, 'Validation', 'Date is required.');
            return;
        }
        if (!this.selectedStockRelease?.id && !this.editMode) {
            this.alertService.warning(this.module, 'Validation', 'Please browse and select a Stock Release document.');
            return;
        }
        if (!this.selectedLocation?.id) {
            this.alertService.warning(this.module, 'Validation', 'Inventory Location is required.');
            return;
        }
        if (this.details.length === 0) {
            this.alertService.warning(this.module, 'Validation', 'No items loaded. Please select a Stock Release first.');
            return;
        }
        const hasQty = this.details.some(d => (Number(d.quantity) || 0) > 0);
        if (!hasQty) {
            this.alertService.warning(this.module, 'Validation', 'Total return quantity is zero — enter quantities for at least one item.');
            return;
        }
        if (!this.approvingOfficer?.accountNo) {
            this.alertService.warning(this.module, 'Validation', 'Received By is required.');
            return;
        }

        this.isLoading.set(true);

        const payload: any = {
            voucherDate:       this.voucherDate,
            remarks:           this.remarks.trim() || null,
            stockRelease:      { id: this.selectedStockRelease?.id },
            inventoryLocation: { id: this.selectedLocation.id },
            approvingOfficer:  { accountNo: this.approvingOfficer.accountNo, fullName: this.approvingOfficer.fullName },
            details:           this.details.map(d => ({
                itemId:           d.itemId           || null,
                itemCode:         d.itemCode          || '',
                unitId:           d.unitId            || null,
                unitCode:         d.unitCode           || '',
                itemDescription:  d.itemDescription   || '',
                quantity:         Number(d.quantity)  || 0,
                quantityReleased: Number(d.quantityReleased) || 0,
                unitCost:         Number(d.unitCost)  || 0
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

Overwrite `frontend/src/app/pages/mct/mct-add-edit/mct-add-edit.component.html` with:

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
                <app-ui-card title="Material Credit Ticket Information">
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
                                <label class="form-label fw-bold">Inventory Location <span class="text-danger">*</span></label>
                                <select class="form-select"
                                        [(ngModel)]="selectedLocation"
                                        name="inventoryLocation"
                                        [compareWith]="compareById">
                                    <option [ngValue]="null">— Select Location —</option>
                                    @for (loc of inventoryLocations(); track loc.id) {
                                        <option [ngValue]="loc">{{ loc.description || loc.name }}</option>
                                    }
                                </select>
                            </div>
                        </div>

                        <!-- ─── Section: Stock Release Document ────────────────── -->
                        <hr/>
                        <div class="col-12 fs-xs text-uppercase fw-semibold text-muted mb-2">Source Stock Release Document</div>

                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label fw-bold">Stock Release <span class="text-danger">*</span></label>
                                @if (editMode) {
                                    <div class="form-control bg-light">{{ selectedStockRelease?.code || '—' }}</div>
                                } @else {
                                    <div class="input-group">
                                        <input type="text" class="form-control"
                                               [value]="selectedStockRelease?.code || ''"
                                               readonly placeholder="Browse stock release..."/>
                                        <button type="button" class="btn btn-success fw-bold"
                                                (click)="openStockReleaseBrowse()">
                                            <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                                        </button>
                                        @if (selectedStockRelease) {
                                            <button type="button" class="btn btn-danger fw-bold"
                                                    (click)="clearStockRelease()">✕</button>
                                        }
                                    </div>
                                }
                            </div>
                        </div>

                        <!-- ─── Section: Items Table ───────────────────────────── -->
                        <hr/>
                        <div class="col-12 fs-xs text-uppercase fw-semibold text-muted mb-2">Items to Return</div>

                        <div class="table-responsive">
                            <table class="table table-custom table-centered table-hover w-100 mb-0">
                                <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                                    <tr class="text-uppercase fs-xxs">
                                        <th style="width:40px">#</th>
                                        <th style="width:80px">Code</th>
                                        <th>Description</th>
                                        <th class="text-center" style="width:120px">Qty Released</th>
                                        <th class="text-center" style="width:140px">Return Qty <span class="text-danger">*</span></th>
                                        <th class="text-end" style="width:120px">Unit Cost</th>
                                        <th style="width:70px">Unit</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    @if (details.length === 0) {
                                        <tr>
                                            <td colspan="7" class="text-center text-muted py-3">
                                                Browse and select a <strong>Stock Release</strong> to load items.
                                            </td>
                                        </tr>
                                    }
                                    @for (row of details; track $index) {
                                        <tr>
                                            <td class="text-center text-muted">{{ $index + 1 }}</td>
                                            <td class="fw-bold">{{ row.itemCode }}</td>
                                            <td>{{ row.itemDescription }}</td>
                                            <td class="text-center fw-bold text-muted">{{ row.quantityReleased | number:'1.0-3' }}</td>
                                            <td>
                                                <input type="number"
                                                       class="form-control form-control-sm text-center fw-bold"
                                                       [(ngModel)]="row.quantity"
                                                       [name]="'qty_' + $index"
                                                       (ngModelChange)="onQuantityChange($index)"
                                                       min="0" step="0.01" placeholder="0"/>
                                            </td>
                                            <td>
                                                <input type="number"
                                                       class="form-control form-control-sm text-end"
                                                       [(ngModel)]="row.unitCost"
                                                       [name]="'uc_' + $index"
                                                       min="0" step="0.01" placeholder="0.00"/>
                                            </td>
                                            <td>{{ row.unitCode }}</td>
                                        </tr>
                                    }
                                    @if (details.length > 0) {
                                        <tr class="fw-bold bg-light bg-opacity-50">
                                            <td colspan="4" class="text-end text-uppercase fs-xxs">Total Return Qty:</td>
                                            <td class="text-center">{{ totalQuantity | number:'1.0-3' }}</td>
                                            <td></td>
                                            <td></td>
                                        </tr>
                                    }
                                </tbody>
                            </table>
                        </div>

                        <!-- ─── Section: Remarks ───────────────────────────────── -->
                        <hr/>
                        <div class="row g-3">
                            <div class="col-md-12">
                                <label class="form-label fw-bold">Remarks</label>
                                <textarea class="form-control" [(ngModel)]="remarks" name="remarks"
                                          rows="2" placeholder="Optional remarks" maxlength="1024"></textarea>
                            </div>
                        </div>

                        <!-- ─── Section: Signatories ───────────────────────────── -->
                        <hr/>
                        <div class="col-12 fs-xs text-uppercase fw-semibold text-muted mb-2">Signatories</div>

                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label fw-bold">Received By <span class="text-danger">*</span></label>
                                <div class="input-group">
                                    <input type="text" class="form-control"
                                           [value]="approvingOfficer ? (approvingOfficer.fullName || approvingOfficer.name || '') : ''"
                                           readonly placeholder="Browse receiving officer..."/>
                                    <button type="button" class="btn btn-success fw-bold"
                                            (click)="openApprovingOfficerBrowse()">
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
- TS: `BrowseMctStockReleaseModalComponent` and `BrowseEntityModalComponent` imported; `RouterLink` in imports array; `provideIcons({ tablerSearch, tablerArrowLeft, tablerCheck })` in providers; `loadInventoryLocations()` called in `ngOnInit()`; `loadDefaultSignatories()` called in create mode; `onStockReleaseSelected()` calls `getStockReleaseDetails()` and maps items; `onQuantityChange()` caps at `quantityReleased`; `save()` has 5 validations; payload has `stockRelease: { id }`, `inventoryLocation: { id }`, `approvingOfficer: { accountNo, fullName }`, `details[]` with correct field names
- HTML: Inventory Location dropdown with `[compareWith]="compareById"`; `@if (editMode)` shows readonly vs browse input group; items table 7 columns with `[name]="'qty_' + $index"` and `[name]="'uc_' + $index"`; Received By browse; no `*ngIf`/`*ngFor`

- [ ] **Step 4: Report DONE**

---

### Task 5: Fix mct-detail component

**Files:**
- Modify: `frontend/src/app/pages/mct/mct-detail/mct-detail.component.ts`
- Modify: `frontend/src/app/pages/mct/mct-detail/mct-detail.component.html`

**Interfaces:**
- Consumes: existing `data` object — assumed to have `data.details[]` with `itemDescription`, `quantity`, `unitCode`, `unitCost`, `totalCost`
- Produces: no new outputs — display expansion only

**Note:** The detail TS already has `isEditable()` and `RouterLink` imported. The HTML already has the Edit button and full header. What's missing: `provideIcons` in the TS decorator, and the items table card in the HTML.

- [ ] **Step 1: Update the TypeScript file**

Read `mct-detail.component.ts`. The current decorator has no `providers` array. Make these changes:

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
    selector: 'app-mct-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule, FormsModule, RouterLink],
    templateUrl: './mct-detail.component.html',
    providers: [provideIcons({ tablerPrinter, tablerEdit, tablerArrowLeft })]
})
```

- [ ] **Step 2: Update the HTML template**

Read `mct-detail.component.html`. Insert an items table card between the header card and the workflow section — after the closing `</div>` tag of the header card (`class="card mb-3"`) and BEFORE `@if (!isTerminal() && workflowActions.length > 0)`.

Add:
```html
@if (data.details?.length > 0) {
<div class="card mb-3">
    <div class="card-header">
        <h6 class="fw-semibold text-uppercase fs-xs mb-0">Items Returned</h6>
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
                        <th class="text-end">Unit Cost</th>
                        <th class="text-end">Total Cost</th>
                    </tr>
                </thead>
                <tbody>
                    @for (item of data.details; track $index; let i = $index) {
                        <tr>
                            <td class="text-center">{{ i + 1 }}</td>
                            <td>{{ item.itemDescription }}</td>
                            <td class="text-center fw-bold text-primary">{{ item.quantity | number:'1.0-3' }}</td>
                            <td>{{ item.unitCode }}</td>
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
- HTML: Items Returned card with `@if (data.details?.length > 0)` — 6 columns (#, Description, Quantity, Unit, Unit Cost, Total Cost); positioned before the workflow card; no `*ngIf`/`*ngFor`

- [ ] **Step 4: Report DONE**
