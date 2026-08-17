# Stock Release Migration Alignment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bring the Angular 19 stock-release module to full feature parity with the legacy releasing module — browse-withdrawal-document modal, full add-edit form, expanded detail view, and text search on the list.

**Architecture:** Five independent tasks, each touching a small set of files. Service augmentation first, shared modal second, then main → add-edit → detail. No cross-task state; each task is self-contained.

**Tech Stack:** Angular 19, TypeScript signals, NgbModal/NgbActiveModal, ChangeDetectionStrategy.OnPush, COMMON_ALL_PAGE_IMPORTS / COMMON_MAIN_PAGE_IMPORTS / SHARED_PROVIDERS, provideIcons, AlertService, ModalService.

## Global Constraints
- Angular 19 control flow: `@if`, `@for`, `@else` — NEVER `*ngIf` / `*ngFor`
- `provideIcons({...})` in component `providers[]` — NOT in `imports[]`
- `ModalService.openModal()` async/await; result shape `{ action: 'select', data: any }`
- `ChangeDetectionStrategy.OnPush` + `cdr.markForCheck()` in all browse modals
- `BrowseEntityModalComponent` for person/signatory browses (same as withdrawal module)
- Table classes: `table table-custom table-centered table-select table-hover w-100 mb-0`; thead: `bg-light align-middle bg-opacity-25 thead-sm`; tr: `text-uppercase fs-xxs`
- Unique `[name]="'field_' + $index"` on inputs inside `@for` loops
- `AlertService` for all user notifications — no native alert/confirm
- No `git commit` or `git push` — user handles all git operations
- Button standards from CLAUDE.md: Save=btn-primary, Browse/Search/Print=btn-success, Back/Cancel=btn-light, Remove/Delete=btn-danger; all fw-bold; icon-only table buttons: `btn-light btn-icon btn-sm rounded-circle`

---

### Task 1: Augment StockReleaseService with missing API methods

**Files:**
- Modify: `frontend/src/app/pages/stock-release/stock-release.service.ts`

**Interfaces:**
- Produces: `getDefaultSignatories()`, `getWithdrawalDocuments()` — used by Tasks 2 and 4

- [ ] **Step 1: Read the current service file**

Open and read `frontend/src/app/pages/stock-release/stock-release.service.ts` to understand the existing import block and class structure.

- [ ] **Step 2: Add the two missing methods**

Append these two methods inside the `StockReleaseService` class, after the existing `print()` method:

```typescript
getDefaultSignatories(): Observable<any> {
    return this.http.get(`${BASE_API}/stock-release/default-signatories`);
}

getWithdrawalDocuments(): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_API}/stock-release/withdrawal-documents`);
}
```

No new imports are required (Observable and BASE_API are already imported/declared).

- [ ] **Step 3: Verify**

Read the file back. Confirm:
- `getDefaultSignatories` and `getWithdrawalDocuments` are present
- URL strings match exactly as written above
- No import changes were needed (both use existing Observable and BASE_API)
- No other lines were modified

- [ ] **Step 4: Report DONE**

---

### Task 2: Create BrowseWithdrawalDocumentModalComponent

**Files:**
- Create: `frontend/src/app/shared/modals/browse-withdrawal-document-modal/browse-withdrawal-document-modal.component.ts`
- Create: `frontend/src/app/shared/modals/browse-withdrawal-document-modal/browse-withdrawal-document-modal.component.html`

**Interfaces:**
- Consumes: `StockReleaseService.getWithdrawalDocuments()` added in Task 1
- Produces: `BrowseWithdrawalDocumentModalComponent` — used by Task 4's `openWithdrawalBrowse()`
- Result shape: `activeModal.close({ action: 'select', data: withdrawal })` where `withdrawal` is the full object from the API (has `id`, `transId`, `code`, `voucherDate`, `inventoryLocation`, `inventoryCategory`, `documentStatus`, `createdBy`, `purpose`, `details[]`)

- [ ] **Step 1: Create the TypeScript file**

Write the full content of `frontend/src/app/shared/modals/browse-withdrawal-document-modal/browse-withdrawal-document-modal.component.ts`:

```typescript
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch } from '@ng-icons/tabler-icons';
import { StockReleaseService } from '@/app/pages/stock-release/stock-release.service';

@Component({
    selector: 'app-browse-withdrawal-document-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerSearch })],
    templateUrl: './browse-withdrawal-document-modal.component.html'
})
export class BrowseWithdrawalDocumentModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private service = inject(StockReleaseService);
    private cdr = inject(ChangeDetectorRef);

    items: any[] = [];
    loading = false;
    searchText = '';

    ngOnInit(): void {
        this.loading = true;
        this.service.getWithdrawalDocuments().subscribe({
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
        return this.items.filter(w =>
            w.code?.toLowerCase().includes(q) ||
            w.inventoryLocation?.description?.toLowerCase().includes(q) ||
            w.inventoryLocation?.name?.toLowerCase().includes(q) ||
            w.inventoryCategory?.description?.toLowerCase().includes(q) ||
            w.createdBy?.fullName?.toLowerCase().includes(q)
        );
    }

    select(withdrawal: any): void {
        this.activeModal.close({ action: 'select', data: withdrawal });
    }
}
```

- [ ] **Step 2: Create the HTML template**

Write the full content of `frontend/src/app/shared/modals/browse-withdrawal-document-modal/browse-withdrawal-document-modal.component.html`:

```html
<div class="modal-header">
    <h4 class="modal-title">Browse Withdrawal Document</h4>
    <button type="button" class="btn-close" (click)="activeModal.dismiss()"></button>
</div>

<div class="modal-body">
    <div class="d-flex gap-2 mb-3">
        <div class="app-search flex-grow-1">
            <input ngbAutofocus [(ngModel)]="searchText" type="search" class="form-control"
                   placeholder="Search by code, location, category, or prepared by..."/>
            <ng-icon name="tablerSearch" class="app-search-icon text-muted"/>
        </div>
    </div>

    <div class="table-responsive" style="max-height: 420px; overflow-y: auto;">
        <table class="table table-custom table-centered table-select table-hover w-100 mb-0">
            <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                <tr class="text-uppercase fs-xxs">
                    <th>Code</th>
                    <th>Date</th>
                    <th>Location</th>
                    <th>Category</th>
                    <th>Prepared By</th>
                    <th>Status</th>
                </tr>
            </thead>
            <tbody>
                @if (loading) {
                    <tr>
                        <td colspan="6" class="text-center py-4">
                            <div class="d-flex justify-content-center align-items-center gap-2 py-2 text-muted">
                                <span class="spinner-border spinner-border-sm"></span>
                                <span>Loading...</span>
                            </div>
                        </td>
                    </tr>
                } @else if (filtered.length === 0) {
                    <tr class="no-results">
                        <td colspan="6" class="text-center text-muted py-3">Nothing found.</td>
                    </tr>
                } @else {
                    @for (w of filtered; track w.id) {
                        <tr style="cursor: pointer" (click)="select(w)">
                            <td class="fw-bold">{{ w.code }}</td>
                            <td>{{ w.voucherDate | date:'MM/dd/yyyy' }}</td>
                            <td>{{ w.inventoryLocation?.description || w.inventoryLocation?.name || '—' }}</td>
                            <td>{{ w.inventoryCategory?.description || w.inventoryCategory?.name || '—' }}</td>
                            <td>{{ w.createdBy?.fullName || '—' }}</td>
                            <td>
                                <span class="badge bg-secondary bg-opacity-25 text-dark border">
                                    {{ w.documentStatus?.status || '—' }}
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

Read both files back. Confirm:
- TS: `ChangeDetectionStrategy.OnPush`, `cdr.markForCheck()` called after every `loading = false`, `get filtered()` searches code/location/category/createdBy, `select()` closes with `{ action: 'select', data: withdrawal }`
- HTML: 6-column table, `@if loading @else if empty @else @for`, `style="cursor: pointer"` on rows
- No `*ngIf` / `*ngFor` anywhere

- [ ] **Step 4: Report DONE**

---

### Task 3: Add text search filter to stock-release-main

**Files:**
- Modify: `frontend/src/app/pages/stock-release/stock-release-main/stock-release-main.component.ts`
- Modify: `frontend/src/app/pages/stock-release/stock-release-main/stock-release-main.component.html`

**Interfaces:**
- Consumes: existing `records` signal, `page`, `pageSize`
- Produces: `searchText` field, `filteredTotal` getter, updated `pagedRecords` — used only internally

- [ ] **Step 1: Update the TypeScript file**

Read `stock-release-main.component.ts`. Then make these targeted changes:

**Add** `searchText = '';` immediately after the `pageSize = 10;` line:
```typescript
page     = 1;
pageSize = 10;
searchText = '';
```

**Replace** the existing `pagedRecords` getter with:
```typescript
get filteredRecords(): any[] {
    const q = this.searchText.toLowerCase();
    if (!q) return this.records();
    return this.records().filter(r =>
        r.code?.toLowerCase().includes(q) ||
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

**Update** the `reset()` method to also clear `searchText`:
```typescript
reset(): void {
    this.setDefaultDates();
    this.selectedStatus.set(null);
    this.searchText = '';
    this.load();
}
```

- [ ] **Step 2: Update the HTML template**

Read `stock-release-main.component.html`. Then make these targeted changes:

**Add** a `col-md-3` search input after the Status select div (after its closing `</div>`):
```html
<div class="col-md-3">
    <label class="form-label fw-bold mb-1 fs-xs text-uppercase">Search</label>
    <input type="text" class="form-control" [(ngModel)]="searchText"
           (ngModelChange)="page = 1" placeholder="Code, description, prepared by..."/>
</div>
```

**Replace** the pagination block — change `records().length` to `filteredTotal` in both the condition and `[collectionSize]`:
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
- `searchText = ''` field present
- `filteredRecords`, `filteredTotal`, `pagedRecords` getters correct
- `reset()` clears `searchText`
- HTML: search input added, pagination uses `filteredTotal`

- [ ] **Step 4: Report DONE**

---

### Task 4: Overhaul stock-release-add-edit component

**Files:**
- Modify: `frontend/src/app/pages/stock-release/stock-release-add-edit/stock-release-add-edit.component.ts`
- Modify: `frontend/src/app/pages/stock-release/stock-release-add-edit/stock-release-add-edit.component.html`

**Interfaces:**
- Consumes:
  - `StockReleaseService.getDefaultSignatories()` (Task 1) → `{ receivedBy: { accountNo, fullName, name } }`
  - `BrowseWithdrawalDocumentModalComponent` (Task 2) — opened via `ModalService.openModal(..., {}, { size: 'xl', centered: true })`; result `{ action: 'select', data: withdrawal }` where `withdrawal.details[]` has `{ itemId, itemCode, unitId, unitCode, itemDescription, quantity, quantityReleased, inventoryCategoryId }`
  - `BrowseEntityModalComponent` — for Received By; result `{ action: 'select', data: entity }` where entity has `accountNo`, `fullName`, `name`
  - `StockReleaseService.getData(id)` — for edit mode; returns object with `voucherDate`, `description`, `receivedBy`, `details[]`
  - `StockReleaseService.create(payload)` and `StockReleaseService.update(payload)`

**Payload shape for create/update:**
```typescript
{
  id?: number,                              // edit mode only
  voucherDate: string,                      // 'YYYY-MM-DD'
  description: string | null,
  documentTransaction: { id: number },      // withdrawal.transId
  receivedBy: { accountNo: string, fullName: string },
  details: [{
    itemId: number | null,
    itemCode: string,
    unitId: number | null,
    unitCode: string,
    itemDescription: string,
    quantityOrdered: number,
    releaseQuantity: number,
    quantityReleased: number,
    inventoryCategoryId: number | null
  }]
}
```

- [ ] **Step 1: Write the full TypeScript file**

Overwrite `frontend/src/app/pages/stock-release/stock-release-add-edit/stock-release-add-edit.component.ts` with:

```typescript
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { StockReleaseService } from '../stock-release.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseWithdrawalDocumentModalComponent } from '@/app/shared/modals/browse-withdrawal-document-modal/browse-withdrawal-document-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerTrash, tablerArrowLeft, tablerCheck } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-stock-release-add-edit',
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
        provideIcons({ tablerSearch, tablerTrash, tablerArrowLeft, tablerCheck })
    ],
    templateUrl: './stock-release-add-edit.component.html'
})
export class StockReleaseAddEditComponent {
    module    = 'Stock Release';
    subModule = 'Create';
    menuLink  = 'stock-release';

    id: any  = null;
    editMode = false;
    isLoading = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // Header fields
    voucherDate = '';
    description = '';

    // Selected withdrawal document (create mode)
    selectedWithdrawal: any = null;

    // Received by signatory
    receivedBy: any = null;

    // Items
    details: any[] = [];

    get totalReleaseQuantity(): number {
        return this.details.reduce((s, r) => s + (Number(r.releaseQuantity) || 0), 0);
    }

    private service      = inject(StockReleaseService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    ngOnInit(): void {
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

    private loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (data) => {
                if (data) this.receivedBy = data.receivedBy || null;
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
                this.voucherDate  = toYmd(data.voucherDate);
                this.description  = data.description || '';
                this.receivedBy   = data.receivedBy || null;
                this.details      = (data.details || []).map((d: any) => ({ ...d, releaseQuantity: Number(d.releaseQuantity ?? d.quantityReleased) || 0 }));
                if (data.documentTransaction) {
                    this.selectedWithdrawal = { id: data.documentTransaction.id, transId: data.documentTransaction.id, code: data.withdrawalCode || '—' };
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load record.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    // ─── Browse: Withdrawal Document ──────────────────────────────────────────

    async openWithdrawalBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseWithdrawalDocumentModalComponent, {}, { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.onWithdrawalSelected(result.data);
            }
        } catch { }
    }

    private onWithdrawalSelected(withdrawal: any): void {
        this.selectedWithdrawal = withdrawal;
        this.description = withdrawal.purpose?.description || withdrawal.purpose?.name || this.description;
        if (withdrawal.createdBy) {
            this.receivedBy = { accountNo: withdrawal.createdBy.accountNo, fullName: withdrawal.createdBy.fullName };
        }
        this.details = (withdrawal.details || []).map((d: any) => ({
            itemId:              d.itemId             || null,
            itemCode:            d.itemCode            || '',
            unitId:              d.unitId              || null,
            unitCode:            d.unitCode            || '',
            itemDescription:     d.itemDescription     || '',
            quantityOrdered:     Number(d.quantity)    || 0,
            quantityReleased:    Number(d.quantityReleased) || 0,
            releaseQuantity:     Math.max(0, (Number(d.quantity) || 0) - (Number(d.quantityReleased) || 0)),
            inventoryCategoryId: d.inventoryCategoryId || null
        }));
    }

    clearWithdrawal(): void {
        this.selectedWithdrawal = null;
        this.details = [];
    }

    // ─── Browse: Received By ──────────────────────────────────────────────────

    async openReceivedByBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const entity = result.data;
                this.receivedBy = { accountNo: entity.accountNo, fullName: entity.fullName || entity.name };
            }
        } catch { }
    }

    // ─── Items Helpers ────────────────────────────────────────────────────────

    onReleaseQuantityChange(index: number): void {
        const row = this.details[index];
        if (!row) return;
        const qty = Number(row.releaseQuantity) || 0;
        const max = Math.max(0, (Number(row.quantityOrdered) || 0) - (Number(row.quantityReleased) || 0));
        if (qty > max) {
            row.releaseQuantity = max;
            this.alertService.warning(this.module, 'Validation', `Release quantity cannot exceed the remaining balance (${max}).`);
        }
        if (qty < 0) row.releaseQuantity = 0;
    }

    // ─── Save ─────────────────────────────────────────────────────────────────

    save(): void {
        if (!this.voucherDate) {
            this.alertService.warning(this.module, 'Validation', 'Voucher Date is required.');
            return;
        }
        if (!this.selectedWithdrawal?.transId && !this.editMode) {
            this.alertService.warning(this.module, 'Validation', 'Please browse and select a Withdrawal Document.');
            return;
        }
        if (this.details.length === 0) {
            this.alertService.warning(this.module, 'Validation', 'No items loaded. Please select a Withdrawal Document first.');
            return;
        }
        const hasQty = this.details.some(d => (Number(d.releaseQuantity) || 0) > 0);
        if (!hasQty) {
            this.alertService.warning(this.module, 'Validation', 'Total release quantity is zero — enter quantities for at least one item.');
            return;
        }
        if (!this.receivedBy?.accountNo) {
            this.alertService.warning(this.module, 'Validation', 'Received By is required.');
            return;
        }

        this.isLoading.set(true);

        const payload: any = {
            voucherDate:         this.voucherDate,
            description:         this.description.trim() || null,
            documentTransaction: { id: this.selectedWithdrawal?.transId ?? this.selectedWithdrawal?.id },
            receivedBy:          { accountNo: this.receivedBy.accountNo, fullName: this.receivedBy.fullName },
            details:             this.details.map(d => ({
                itemId:              d.itemId             || null,
                itemCode:            d.itemCode            || '',
                unitId:              d.unitId              || null,
                unitCode:            d.unitCode            || '',
                itemDescription:     d.itemDescription     || '',
                quantityOrdered:     Number(d.quantityOrdered)  || 0,
                releaseQuantity:     Number(d.releaseQuantity)  || 0,
                quantityReleased:    Number(d.quantityReleased) || 0,
                inventoryCategoryId: d.inventoryCategoryId      || null
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
}
```

- [ ] **Step 2: Write the full HTML template**

Overwrite `frontend/src/app/pages/stock-release/stock-release-add-edit/stock-release-add-edit.component.html` with:

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
                <app-ui-card title="Stock Release Information">
                    <div class="col-xl-12 p-3" card-body>

                        <!-- ─── Section: Basic Info ─────────────────────────────── -->
                        <div class="row g-3">
                            <div class="col-md-3">
                                <label class="form-label fw-bold">Voucher Date <span class="text-danger">*</span></label>
                                <input type="text" mwlFlatpickr [options]="flatpickrOptions"
                                       class="form-control" [(ngModel)]="voucherDate"
                                       name="voucherDate" placeholder="Select date" required/>
                            </div>
                        </div>

                        <!-- ─── Section: Withdrawal Document ──────────────────────── -->
                        <hr/>
                        <div class="col-12 fs-xs text-uppercase fw-semibold text-muted mb-2">Source Withdrawal Document</div>

                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label fw-bold">Withdrawal Document <span class="text-danger">*</span></label>
                                @if (editMode) {
                                    <div class="form-control bg-light">{{ selectedWithdrawal?.code || '—' }}</div>
                                } @else {
                                    <div class="input-group">
                                        <input type="text" class="form-control"
                                               [value]="selectedWithdrawal?.code || ''"
                                               readonly placeholder="Browse withdrawal document..."/>
                                        <button type="button" class="btn btn-success fw-bold"
                                                (click)="openWithdrawalBrowse()">
                                            <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                                        </button>
                                        @if (selectedWithdrawal) {
                                            <button type="button" class="btn btn-danger fw-bold"
                                                    (click)="clearWithdrawal()">
                                                <ng-icon name="tablerTrash" class="ps-0 pe-0 fw-bold"></ng-icon>
                                            </button>
                                        }
                                    </div>
                                }
                            </div>
                        </div>

                        <!-- ─── Section: Items Table ───────────────────────────────── -->
                        <hr/>
                        <div class="col-12 fs-xs text-uppercase fw-semibold text-muted mb-2">Items</div>

                        <div class="table-responsive">
                            <table class="table table-custom table-centered table-hover w-100 mb-0">
                                <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                                    <tr class="text-uppercase fs-xxs">
                                        <th style="width:40px">#</th>
                                        <th style="width:80px">Code</th>
                                        <th>Description</th>
                                        <th class="text-center" style="width:120px">Qty Ordered</th>
                                        <th class="text-center" style="width:120px">Qty Released</th>
                                        <th class="text-center" style="width:140px">Release Qty <span class="text-danger">*</span></th>
                                        <th style="width:70px">Unit</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    @if (details.length === 0) {
                                        <tr>
                                            <td colspan="7" class="text-center text-muted py-3">
                                                Browse and select a <strong>Withdrawal Document</strong> to load items.
                                            </td>
                                        </tr>
                                    }
                                    @for (row of details; track $index) {
                                        <tr>
                                            <td class="text-center text-muted">{{ $index + 1 }}</td>
                                            <td class="fw-bold">{{ row.itemCode }}</td>
                                            <td>{{ row.itemDescription }}</td>
                                            <td class="text-center fw-bold">{{ row.quantityOrdered | number:'1.0-3' }}</td>
                                            <td class="text-center fw-bold text-muted">{{ row.quantityReleased | number:'1.0-3' }}</td>
                                            <td>
                                                <input type="number"
                                                       class="form-control form-control-sm text-center fw-bold"
                                                       [(ngModel)]="row.releaseQuantity"
                                                       [name]="'rqty_' + $index"
                                                       (ngModelChange)="onReleaseQuantityChange($index)"
                                                       min="0" step="0.01" placeholder="0"/>
                                            </td>
                                            <td>{{ row.unitCode }}</td>
                                        </tr>
                                    }
                                    @if (details.length > 0) {
                                        <tr class="fw-bold bg-light bg-opacity-50">
                                            <td colspan="5" class="text-end text-uppercase fs-xxs">Total Release Qty:</td>
                                            <td class="text-center">{{ totalReleaseQuantity | number:'1.0-3' }}</td>
                                            <td></td>
                                        </tr>
                                    }
                                </tbody>
                            </table>
                        </div>

                        <!-- ─── Section: Remarks ───────────────────────────────────── -->
                        <hr/>
                        <div class="col-12 fs-xs text-uppercase fw-semibold text-muted mb-2">Remarks</div>
                        <div class="row g-3">
                            <div class="col-md-12">
                                <label class="form-label fw-bold">Description / Remarks</label>
                                <textarea class="form-control" [(ngModel)]="description" name="description"
                                          rows="2" placeholder="Optional remarks" maxlength="1024"></textarea>
                            </div>
                        </div>

                        <!-- ─── Section: Signatories ───────────────────────────────── -->
                        <hr/>
                        <div class="col-12 fs-xs text-uppercase fw-semibold text-muted mb-2">Signatories</div>

                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label fw-bold">Received By <span class="text-danger">*</span></label>
                                <div class="input-group">
                                    <input type="text" class="form-control"
                                           [value]="receivedBy ? (receivedBy.fullName || receivedBy.name || '') : ''"
                                           readonly placeholder="Browse receiving officer..."/>
                                    <button type="button" class="btn btn-success fw-bold"
                                            (click)="openReceivedByBrowse()">
                                        <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                                    </button>
                                </div>
                            </div>
                        </div>

                        <!-- ─── Footer ─────────────────────────────────────────────── -->
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
- TS: `BrowseWithdrawalDocumentModalComponent` and `BrowseEntityModalComponent` imported; `getDefaultSignatories()` called in create mode; `onWithdrawalSelected()` maps withdrawal details to form rows with correct field names; `onReleaseQuantityChange()` caps at remaining balance; `save()` has 4 validations in order; payload has `documentTransaction: { id }`, `receivedBy: { accountNo, fullName }`, full `details[]`
- HTML: `@if (editMode)` shows readonly code vs browse input group; items table has 7 columns with `[name]="'rqty_' + $index"` on release qty input; signatories section has Received By browse; no `*ngIf`/`*ngFor` anywhere

- [ ] **Step 4: Report DONE**

---

### Task 5: Fix stock-release-detail component

**Files:**
- Modify: `frontend/src/app/pages/stock-release/stock-release-detail/stock-release-detail.component.ts`
- Modify: `frontend/src/app/pages/stock-release/stock-release-detail/stock-release-detail.component.html`

**Interfaces:**
- Consumes: existing `data` object from `StockReleaseService.getData(id)` — assumed to have `data.inventoryLocation`, `data.receivedBy`, `data.details[]` (with `itemDescription`, `quantityOrdered`, `quantityReleased`, `unitCode`, `unitCost`, `totalCost`)
- Produces: no new outputs; this task only expands the display

- [ ] **Step 1: Update the TypeScript file**

Read `stock-release-detail.component.ts`. Make these changes:

**Add** `RouterLink` to the imports array (it is used by the edit button):
```typescript
import { RouterLink } from '@angular/router';
```
Add `RouterLink` to the `imports: [...]` array in the decorator.

**Add** `provideIcons` and icon imports:
```typescript
import { provideIcons } from '@ng-icons/core';
import { tablerPrinter, tablerEdit, tablerArrowLeft } from '@ng-icons/tabler-icons';
```
Add to `providers: [provideIcons({ tablerPrinter, tablerEdit, tablerArrowLeft })]` in the decorator.

**Add** `isEditable()` method to the class (after `isTerminal()`):
```typescript
isEditable(): boolean {
    const s = this.data?.documentStatus?.status || '';
    return s === 'Document Created' || s === 'For Revision';
}
```

The final decorator should be:
```typescript
@Component({
    selector: 'app-stock-release-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule, FormsModule, RouterLink],
    templateUrl: './stock-release-detail.component.html',
    providers: [provideIcons({ tablerPrinter, tablerEdit, tablerArrowLeft })]
})
```

- [ ] **Step 2: Update the HTML template**

Read `stock-release-detail.component.html`. Make these targeted changes:

**In the header card** (`<div class="row g-3">`), add two new `col-md-3` fields after the existing Prepared By field (after its closing `</div>`):

```html
<div class="col-md-3">
    <div class="fs-xs text-uppercase fw-bold text-muted mb-1">Inventory Location</div>
    <div>{{ data.inventoryLocation?.description || data.inventoryLocation?.name || '—' }}</div>
</div>
<div class="col-md-3">
    <div class="fs-xs text-uppercase fw-bold text-muted mb-1">Received By</div>
    <div>{{ data.receivedBy?.fullName || data.receivedBy?.name || '—' }}</div>
</div>
```

**Add an Items table card** between the header card and the workflow card. Insert this block immediately after the closing `</div>` of the header card (before `@if (!isTerminal() && workflowActions.length > 0)`):

```html
@if (data.details?.length > 0) {
<div class="card mb-3">
    <div class="card-header">
        <h6 class="fw-semibold text-uppercase fs-xs mb-0">Items Released</h6>
    </div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-custom table-centered w-100 mb-0">
                <thead class="bg-light bg-opacity-25 thead-sm">
                    <tr class="text-uppercase fs-xxs">
                        <th>#</th>
                        <th>Description</th>
                        <th class="text-center">Qty Ordered</th>
                        <th>Unit</th>
                        <th class="text-center">Qty Released</th>
                        <th class="text-end">Unit Cost</th>
                        <th class="text-end">Total Cost</th>
                    </tr>
                </thead>
                <tbody>
                    @for (item of data.details; track $index; let i = $index) {
                        <tr>
                            <td class="text-center">{{ i + 1 }}</td>
                            <td>{{ item.itemDescription }}</td>
                            <td class="text-center fw-bold">{{ item.quantityOrdered | number:'1.0-3' }}</td>
                            <td>{{ item.unitCode }}</td>
                            <td class="text-center fw-bold text-primary">{{ item.quantityReleased | number:'1.0-3' }}</td>
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

**In the footer card** (the card with Back + Print buttons), add an Edit button after the Print button, inside the `<div class="d-flex gap-2">` wrapper:

```html
@if (isEditable()) {
    <a [routerLink]="['/' + menuLink, data.id, 'edit']" class="btn btn-primary fw-bold">
        <ng-icon name="tablerEdit" class="ps-0 pe-3 fw-bold"></ng-icon>Edit
    </a>
}
```

- [ ] **Step 3: Verify both files**

Read both files. Confirm:
- TS: `RouterLink` in imports array; `provideIcons({ tablerPrinter, tablerEdit, tablerArrowLeft })` in providers; `isEditable()` method present returning true for `'Document Created'` and `'For Revision'`
- HTML: Inventory Location and Received By fields added to header card; Items Released card with `@if (data.details?.length > 0)` — 7 columns (#, Description, Qty Ordered, Unit, Qty Released, Unit Cost, Total Cost); Edit button behind `@if (isEditable())`
- No `*ngIf`/`*ngFor` anywhere

- [ ] **Step 4: Report DONE**
