# Withdrawal Migration Alignment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bring the Angular 19 Stock Withdrawal module to full feature parity with the legacy Firefly AngularJS implementation, adding all missing fields, items table, browse modals, and detail view content.

**Architecture:** Five targeted tasks — service augmentation, a new shared item-stock browse modal, list-screen search filter, full add-edit overhaul, and detail view expansion. No new routes needed; all files already exist except the new shared modal. Tasks 3–5 depend on Tasks 1–2 being complete.

**Tech Stack:** Angular 19, TypeScript signals, `@if`/`@for` control flow, NgbModal, ModalService async/await, mwlFlatpickr, AlertService (SweetAlert2), `@ng-icons/tabler-icons`, `provideIcons`.

## Global Constraints

- Angular 19 control flow ONLY: `@if`, `@for`, `@else` — never `*ngIf` or `*ngFor`
- Signals for async state: `signal<T>()` — never BehaviorSubject for component state
- All alerts/confirms: `AlertService` methods — never `alert()`, `confirm()`, or Bootstrap toasts
- Date inputs: `mwlFlatpickr` with `[options]="flatpickrOptions"` — `{ dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' }`; provide `provideFlatpickrDefaults()` in component providers
- Browse modals: `ModalService.openModal()` with `async/await` and `try/catch`; result shape `{ action: 'select', data: any }`
- Browse input pattern: `<div class="input-group">` + readonly `<input>` + `<button class="btn btn-success fw-bold">`
- NgModel in `@for` loops: always use unique `[name]="'fieldName_' + $index"`
- Icons: `provideIcons({...})` in component `providers` array — not in imports
- Imports barrel: use `COMMON_ALL_PAGE_IMPORTS`, `COMMON_ADD_EDIT_PAGE_IMPORTS`, `COMMON_MAIN_PAGE_IMPORTS`, `SHARED_PROVIDERS`
- User handles all git commits — do NOT run git commit or git push
- No TypeScript strict-mode violations — avoid `any` where a shape is known, but `any` is acceptable for API responses
- Save button label: `{{ editMode ? 'Update' : 'Save ' + module }}`
- Back button: `<a [routerLink]>` not `<button>`; icon padding `ps-0 pe-3`
- Table class: `table table-custom table-centered table-select table-hover w-100 mb-0`
- Thead class: `bg-light align-middle bg-opacity-25 thead-sm` with `tr` class `text-uppercase fs-xxs`

---

## File Map

| File | Action |
|---|---|
| `frontend/src/app/pages/withdrawal/withdrawal.service.ts` | Modify — add 5 new API methods |
| `frontend/src/app/shared/modals/browse-item-stock-modal/browse-item-stock-modal.component.ts` | Create — new modal |
| `frontend/src/app/shared/modals/browse-item-stock-modal/browse-item-stock-modal.component.html` | Create — new modal template |
| `frontend/src/app/pages/withdrawal/withdrawal-main/withdrawal-main.component.ts` | Modify — add searchText/filteredTotal |
| `frontend/src/app/pages/withdrawal/withdrawal-main/withdrawal-main.component.html` | Modify — add search input, fix pagination |
| `frontend/src/app/pages/withdrawal/withdrawal-add-edit/withdrawal-add-edit.component.ts` | Modify — full overhaul |
| `frontend/src/app/pages/withdrawal/withdrawal-add-edit/withdrawal-add-edit.component.html` | Modify — full overhaul |
| `frontend/src/app/pages/withdrawal/withdrawal-detail/withdrawal-detail.component.ts` | Modify — add provideIcons |
| `frontend/src/app/pages/withdrawal/withdrawal-detail/withdrawal-detail.component.html` | Modify — add location, category, employees, items table |

---

### Task 1: Augment WithdrawalService with missing API methods

**Files:**
- Modify: `frontend/src/app/pages/withdrawal/withdrawal.service.ts`

**Interfaces:**
- Produces: `getInventoryLocations()`, `getInventoryCategories()`, `getPurposes()`, `getDefaultSignatories()`, `getItemStocksForWithdrawal(locationId, categoryId)`, `getRVDetailsForWithdrawal(rvId, locationId, categoryId)`, `getWorkOrderDetails(workOrderId, locationId, categoryId)`, `getCostEstimateDetails(transactionId)` — all used by Tasks 2, 4

- [ ] **Step 1: Read the current service**

Read `frontend/src/app/pages/withdrawal/withdrawal.service.ts` and confirm current methods: `list`, `listByDateRange`, `getDocumentStatuses`, `getData`, `create`, `update`, `process`, `getWorkflowActions`, `getDocumentLogs`, `print`.

- [ ] **Step 2: Add the 5 missing service methods**

Append to `withdrawal.service.ts` before the closing brace:

```typescript
    getInventoryLocations(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/withdrawal/inventory-locations`);
    }

    getInventoryCategories(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/withdrawal/inventory-categories`);
    }

    getPurposes(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/withdrawal/purposes`);
    }

    getDefaultSignatories(): Observable<any> {
        return this.http.get(`${BASE_API}/withdrawal/default-signatories`);
    }

    getItemStocksForWithdrawal(locationId: number, categoryId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/withdrawal/item-stocks/${locationId}/${categoryId}`);
    }

    getRVDetailsForWithdrawal(rvId: number, locationId: number, categoryId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/withdrawal/rv-details/${rvId}/${locationId}/${categoryId}`);
    }

    getWorkOrderDetails(workOrderId: number, locationId: number, categoryId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/withdrawal/work-order-details/${workOrderId}/${locationId}/${categoryId}`);
    }

    getCostEstimateDetails(transactionId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/withdrawal/cost-estimate-details/${transactionId}`);
    }
```

- [ ] **Step 3: Verify by inspection**

Re-read the file and confirm all 8 new methods are present with correct URL patterns. The file should now have 18 total methods. No compilation step required — method signatures are the deliverable.

---

### Task 2: Create BrowseItemStockModalComponent

**Files:**
- Create: `frontend/src/app/shared/modals/browse-item-stock-modal/browse-item-stock-modal.component.ts`
- Create: `frontend/src/app/shared/modals/browse-item-stock-modal/browse-item-stock-modal.component.html`

**Interfaces:**
- Consumes: `WithdrawalService.getItemStocksForWithdrawal(locationId, categoryId)` from Task 1
- Inputs: `@Input() locationId: number`, `@Input() categoryId: number`
- Produces: Closes with `{ action: 'select', data: itemStock }` where `itemStock` has shape `{ id, item: { id, code, description, unit: { id, code } }, unitCost, totalQuantity, inventoryLocation }`
- Used by: Task 4 `openItemStockBrowse()`

- [ ] **Step 1: Create the TypeScript component file**

Create `frontend/src/app/shared/modals/browse-item-stock-modal/browse-item-stock-modal.component.ts`:

```typescript
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, Input, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch } from '@ng-icons/tabler-icons';
import { WithdrawalService } from '@/app/pages/withdrawal/withdrawal.service';

@Component({
    selector: 'app-browse-item-stock-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerSearch })],
    templateUrl: './browse-item-stock-modal.component.html'
})
export class BrowseItemStockModalComponent implements OnInit {
    @Input() locationId!: number;
    @Input() categoryId!: number;

    activeModal      = inject(NgbActiveModal);
    private service  = inject(WithdrawalService);
    private cdr      = inject(ChangeDetectorRef);

    items:      any[] = [];
    loading           = false;
    searchText        = '';

    get filtered(): any[] {
        const q = this.searchText.trim().toLowerCase();
        if (!q) return this.items;
        return this.items.filter(s =>
            (s.item?.code        || '').toLowerCase().includes(q) ||
            (s.item?.description || '').toLowerCase().includes(q) ||
            (s.item?.unit?.code  || '').toLowerCase().includes(q)
        );
    }

    ngOnInit(): void {
        this.loading = true;
        this.service.getItemStocksForWithdrawal(this.locationId, this.categoryId).subscribe({
            next: (data) => {
                this.items   = data || [];
                this.loading = false;
                this.cdr.markForCheck();
            },
            error: () => {
                this.loading = false;
                this.cdr.markForCheck();
            }
        });
    }

    select(stock: any): void {
        this.activeModal.close({ action: 'select', data: stock });
    }
}
```

- [ ] **Step 2: Create the HTML template**

Create `frontend/src/app/shared/modals/browse-item-stock-modal/browse-item-stock-modal.component.html`:

```html
<div class="modal-header">
    <h4 class="modal-title fw-bold">Browse Item Stock</h4>
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
                    <th class="text-end">Balance</th>
                    <th class="text-end">Unit Cost</th>
                </tr>
            </thead>
            <tbody>
                @if (loading) {
                    <tr>
                        <td colspan="5" class="text-center py-4">
                            <span class="spinner-border spinner-border-sm me-2"></span>Loading...
                        </td>
                    </tr>
                } @else if (filtered.length === 0) {
                    <tr class="no-results">
                        <td colspan="5" class="text-center text-muted py-3">Nothing found.</td>
                    </tr>
                } @else {
                    @for (stock of filtered; track stock.id) {
                        <tr style="cursor: pointer" (click)="select(stock)">
                            <td class="fw-bold">{{ stock.item?.code || '—' }}</td>
                            <td>{{ stock.item?.description || '—' }}</td>
                            <td>{{ stock.item?.unit?.code || '—' }}</td>
                            <td class="text-end fw-bold">{{ stock.totalQuantity | number:'1.0-3' }}</td>
                            <td class="text-end">{{ stock.unitCost | number:'1.2-2' }}</td>
                        </tr>
                    }
                }
            </tbody>
        </table>
    </div>
</div>
```

- [ ] **Step 3: Verify by inspection**

Re-read both files. Confirm: `@Input() locationId`, `@Input() categoryId`, `getItemStocksForWithdrawal` call, `filtered` getter, `select()` closes with `{ action: 'select', data: stock }`. HTML uses `@if`/`@for`, no `*ngIf`/`*ngFor`. Template uses `stock.item?.code`, `stock.totalQuantity`, `stock.unitCost`.

---

### Task 3: Add text search filter to withdrawal-main

**Files:**
- Modify: `frontend/src/app/pages/withdrawal/withdrawal-main/withdrawal-main.component.ts`
- Modify: `frontend/src/app/pages/withdrawal/withdrawal-main/withdrawal-main.component.html`

**Interfaces:**
- Produces: `searchText: string`, `filteredTotal: number`, updated `pagedRecords` getter — used in HTML

- [ ] **Step 1: Read both files**

Read `withdrawal-main.component.ts` and `withdrawal-main.component.html` to see the current state. The TS has `pagedRecords` as a plain slice with no filtering. The HTML has no search input and uses `records().length` for pagination collectionSize.

- [ ] **Step 2: Update the TypeScript**

In `withdrawal-main.component.ts`:

1. Add `searchText = '';` after the `pageSize = 10;` line.

2. Replace the existing `pagedRecords` getter with:

```typescript
    get pagedRecords(): any[] {
        const q = this.searchText.trim().toLowerCase();
        const filtered = q
            ? this.records().filter(r =>
                (r.code        || '').toLowerCase().includes(q) ||
                (r.description || '').toLowerCase().includes(q) ||
                (r.createdBy?.fullName || r.preparedBy || '').toLowerCase().includes(q)
              )
            : this.records();
        const start = (this.page - 1) * this.pageSize;
        return filtered.slice(start, start + this.pageSize);
    }

    get filteredTotal(): number {
        const q = this.searchText.trim().toLowerCase();
        if (!q) return this.records().length;
        return this.records().filter(r =>
            (r.code        || '').toLowerCase().includes(q) ||
            (r.description || '').toLowerCase().includes(q) ||
            (r.createdBy?.fullName || r.preparedBy || '').toLowerCase().includes(q)
        ).length;
    }
```

- [ ] **Step 3: Update the HTML**

In `withdrawal-main.component.html`:

1. In the filter row (`div.row.mb-3`), add a new `col-md-3` for the search input after the Status column (before the buttons column):

```html
                    <div class="col-md-3">
                        <label class="form-label fw-bold mb-1 fs-xs text-uppercase">Search</label>
                        <input type="text" class="form-control" [(ngModel)]="searchText" name="searchText"
                               placeholder="Code, description, prepared by…" (ngModelChange)="page = 1"/>
                    </div>
```

2. Change the pagination block:
   - `[collectionSize]="records().length"` → `[collectionSize]="filteredTotal"`
   - `@if (records().length > pageSize)` → `@if (filteredTotal > pageSize)`

- [ ] **Step 4: Verify by inspection**

Re-read both files. Confirm `filteredTotal` getter exists, `pagedRecords` uses it, pagination uses `filteredTotal`, search input has `(ngModelChange)="page = 1"`.

---

### Task 4: Overhaul withdrawal-add-edit component

**Files:**
- Modify: `frontend/src/app/pages/withdrawal/withdrawal-add-edit/withdrawal-add-edit.component.ts`
- Modify: `frontend/src/app/pages/withdrawal/withdrawal-add-edit/withdrawal-add-edit.component.html`

**Interfaces:**
- Consumes: `WithdrawalService` methods from Task 1, `BrowseItemStockModalComponent` from Task 2, existing shared modals: `BrowseEntityModalComponent`, `BrowseWorkOrderModalComponent`
- Produces: Full save payload matching the legacy backend contract

**OLD save payload (exact field names to match):**
```json
{
  "voucherDate": "...",
  "description": "...",
  "approvingOfficer": { "accountNo": "...", "fullName": "..." },
  "inventoryLocation": { "id": 1 },
  "inventoryCategory": { "id": 2 },
  "purpose": { "id": 3 },
  "employees": [{ "name": "...", "accountNo": "..." }],
  "details": [{
    "itemStockId": 1, "itemId": 2, "itemCode": "...", "itemDescription": "...",
    "unitCode": "...", "unitId": 3, "unitCost": 0.00,
    "quantity": 5, "quantityReleased": 0, "inventoryBalance": 10,
    "inventoryLocationId": 1, "specialEquipment": false
  }],
  "type": "...",
  "workOrder": { "id": 1 } | null,
  "purchaseRequest": null,
  "costEstimate": null
}
```

- [ ] **Step 1: Read both files**

Read the current `withdrawal-add-edit.component.ts` and `.html`. The TS currently has only `voucherDate`, `description`, and a minimal `save()`. The HTML has only those 2 fields.

- [ ] **Step 2: Replace the TypeScript component**

Write the full replacement for `withdrawal-add-edit.component.ts`:

```typescript
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { WithdrawalService } from '../withdrawal.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseWorkOrderModalComponent } from '@/app/shared/modals/browse-work-order-modal/browse-work-order-modal.component';
import { BrowseItemStockModalComponent } from '@/app/shared/modals/browse-item-stock-modal/browse-item-stock-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerTrash, tablerPlus, tablerArrowLeft, tablerCheck } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-withdrawal-add-edit',
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
    templateUrl: './withdrawal-add-edit.component.html'
})
export class WithdrawalAddEditComponent {
    module    = 'Stock Withdrawal';
    subModule = 'Create';
    menuLink  = 'withdrawal';

    id: any   = null;
    editMode  = false;
    isLoading = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // Header fields
    voucherDate = '';
    description = '';

    // Dropdowns
    inventoryLocations  = signal<any[]>([]);
    inventoryCategories = signal<any[]>([]);
    purposes            = signal<any[]>([]);

    selectedLocation: any = null;
    selectedCategory: any = null;
    selectedPurpose:  any = null;

    // Reference documents
    selectedWorkOrder: any = null;
    workOrderDesc          = '';

    // Approving officer
    approvingOfficer: any = null;

    // Employees (shown when category type = OFFICE_EQUIPMENT_FURNITURE_FIXTURES)
    employees: any[] = [];

    // Items
    details: any[] = [];

    // Computed visibility
    get showPurposeInput(): boolean {
        return this.selectedPurpose?.type === 'OTHERS';
    }
    get showEmployeeSection(): boolean {
        return this.selectedCategory?.type === 'OFFICE_EQUIPMENT_FURNITURE_FIXTURES';
    }
    get totalQuantity(): number {
        return this.details.reduce((s, r) => s + (Number(r.quantity) || 0), 0);
    }

    private service      = inject(WithdrawalService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    ngOnInit(): void {
        this.loadDropdowns();

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

    private loadDropdowns(): void {
        this.service.getInventoryLocations().subscribe({
            next: (d) => this.inventoryLocations.set(d || []),
            error: () => {}
        });
        this.service.getInventoryCategories().subscribe({
            next: (d) => this.inventoryCategories.set(d || []),
            error: () => {}
        });
        this.service.getPurposes().subscribe({
            next: (d) => this.purposes.set(d || []),
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
                this.description      = data.description || '';
                this.approvingOfficer = data.approvingOfficer || null;
                this.employees        = data.employees || [];
                this.details          = data.details   || [];

                // Match dropdowns by id after they've loaded
                const tryMatch = () => {
                    if (data.inventoryLocation?.id) {
                        this.selectedLocation = this.inventoryLocations().find(l => l.id === data.inventoryLocation.id) ?? data.inventoryLocation;
                    }
                    if (data.inventoryCategory?.id) {
                        this.selectedCategory = this.inventoryCategories().find(c => c.id === data.inventoryCategory.id) ?? data.inventoryCategory;
                    }
                    if (data.purpose?.id) {
                        this.selectedPurpose = this.purposes().find(p => p.id === data.purpose.id) ?? data.purpose;
                    }
                };
                // Retry once after a tick in case dropdowns haven't loaded yet
                tryMatch();
                setTimeout(tryMatch, 400);

                if (data.workOrder?.id) {
                    this.selectedWorkOrder = data.workOrder;
                    this.workOrderDesc = data.workOrder.code || '';
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load record.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    // ─── Category Change Handler ──────────────────────────────────────────────

    onCategoryChange(): void {
        if (!this.showEmployeeSection) {
            this.employees = [];
        }
    }

    // ─── Browse: Approving Officer ────────────────────────────────────────────

    async openApprovingOfficerBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.approvingOfficer = result.data;
            }
        } catch { }
    }

    // ─── Browse: Work Order ───────────────────────────────────────────────────

    async openWorkOrderBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseWorkOrderModalComponent, {}, { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const wo = result.data;
                this.selectedWorkOrder = wo;
                this.workOrderDesc = (wo.code || '') + (wo.project?.name ? ' — ' + wo.project.name : '');
                // Load WO items if location and category selected
                if (this.selectedLocation?.id && this.selectedCategory?.id) {
                    this.service.getWorkOrderDetails(wo.id, this.selectedLocation.id, this.selectedCategory.id).subscribe({
                        next: (items) => { this.details = items || []; },
                        error: () => this.alertService.error(this.module, 'Failed to load Work Order items.', '')
                    });
                }
            }
        } catch { }
    }

    clearWorkOrder(): void {
        this.selectedWorkOrder = null;
        this.workOrderDesc     = '';
        this.details           = [];
    }

    // ─── Browse: Item Stock ───────────────────────────────────────────────────

    async openItemStockBrowse(): Promise<void> {
        if (!this.selectedLocation?.id || !this.selectedCategory?.id) {
            this.alertService.warning(this.module, 'Validation', 'Please select Inventory Location and Category first.');
            return;
        }
        try {
            const result = await this.modalService.openModal(
                BrowseItemStockModalComponent,
                { locationId: this.selectedLocation.id, categoryId: this.selectedCategory.id },
                { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const stock = result.data;
                const isDuplicate = this.details.some(d => d.itemStockId === stock.id);
                if (isDuplicate) {
                    this.alertService.warning(this.module, 'Validation', 'Duplicate item.');
                    return;
                }
                this.details.push({
                    itemStockId:         stock.id,
                    itemId:              stock.item?.id,
                    itemCode:            stock.item?.code,
                    itemDescription:     stock.item?.description,
                    unitCode:            stock.item?.unit?.code,
                    unitId:              stock.item?.unit?.id,
                    unitCost:            stock.unitCost,
                    quantity:            0,
                    quantityReleased:    0,
                    inventoryBalance:    stock.totalQuantity,
                    inventoryLocationId: stock.inventoryLocation?.id ?? this.selectedLocation.id,
                    specialEquipment:    false
                });
            }
        } catch { }
    }

    // ─── Browse: Employee ─────────────────────────────────────────────────────

    async openEmployeeBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const emp = result.data;
                const isDuplicate = this.employees.some(e => e.accountNo === emp.accountNo);
                if (isDuplicate) {
                    this.alertService.warning(this.module, 'Validation', 'Duplicate employee.');
                    return;
                }
                this.employees.push({ name: emp.name || emp.fullName, accountNo: emp.accountNo });
            }
        } catch { }
    }

    removeEmployee(index: number): void {
        this.employees.splice(index, 1);
    }

    // ─── Items Table Helpers ──────────────────────────────────────────────────

    /**
     * Cap quantity to inventoryBalance on change.
     */
    onQuantityChange(index: number): void {
        const row = this.details[index];
        if (!row) return;
        const qty = Number(row.quantity) || 0;
        const bal = Number(row.inventoryBalance) || 0;
        if (qty > bal) {
            row.quantity = bal;
            this.alertService.warning(this.module, 'Validation',
                `Quantity cannot exceed inventory balance (${bal}).`);
        }
    }

    removeRow(index: number): void {
        this.details.splice(index, 1);
    }

    compareById(a: any, b: any): boolean {
        return a && b ? a.id === b.id : a === b;
    }

    // ─── Save ─────────────────────────────────────────────────────────────────

    save(): void {
        if (!this.voucherDate) {
            this.alertService.warning(this.module, 'Validation', 'Voucher Date is required.');
            return;
        }
        if (!this.selectedLocation?.id) {
            this.alertService.warning(this.module, 'Validation', 'Inventory Location is required.');
            return;
        }
        if (!this.selectedCategory?.id) {
            this.alertService.warning(this.module, 'Validation', 'Inventory Category is required.');
            return;
        }
        if (!this.selectedPurpose?.id) {
            this.alertService.warning(this.module, 'Validation', 'Purpose is required.');
            return;
        }
        if (this.showPurposeInput && !this.description.trim()) {
            this.alertService.warning(this.module, 'Validation', 'Please describe the purpose (Others).');
            return;
        }
        if (!this.approvingOfficer?.accountNo) {
            this.alertService.warning(this.module, 'Validation', 'Approving Officer is required.');
            return;
        }
        if (this.details.length === 0) {
            this.alertService.warning(this.module, 'Validation', 'Please add at least one item.');
            return;
        }
        const hasQty = this.details.some(d => (Number(d.quantity) || 0) > 0);
        if (!hasQty) {
            this.alertService.warning(this.module, 'Validation', 'Total quantity is zero — enter quantities for items.');
            return;
        }

        this.isLoading.set(true);

        const payload: any = {
            voucherDate:      this.voucherDate,
            description:      this.description.trim() || null,
            approvingOfficer: { accountNo: this.approvingOfficer.accountNo, fullName: this.approvingOfficer.fullName || this.approvingOfficer.name },
            inventoryLocation: { id: this.selectedLocation.id },
            inventoryCategory: { id: this.selectedCategory.id },
            purpose:           { id: this.selectedPurpose.id },
            type:              this.selectedCategory.type || null,
            employees:         this.employees,
            details:           this.details.map(d => ({
                itemStockId:         d.itemStockId        || null,
                itemId:              d.itemId             || null,
                itemCode:            d.itemCode           || '',
                itemDescription:     d.itemDescription    || '',
                unitCode:            d.unitCode           || '',
                unitId:              d.unitId             || null,
                unitCost:            Number(d.unitCost)   || 0,
                quantity:            Number(d.quantity)   || 0,
                quantityReleased:    Number(d.quantityReleased) || 0,
                inventoryBalance:    Number(d.inventoryBalance) || 0,
                inventoryLocationId: d.inventoryLocationId ?? this.selectedLocation.id,
                specialEquipment:    d.specialEquipment   || false
            })),
            workOrder:      this.selectedWorkOrder ? { id: this.selectedWorkOrder.id } : null,
            purchaseRequest: null,
            costEstimate:    null
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

- [ ] **Step 3: Replace the HTML template**

Write the full replacement for `withdrawal-add-edit.component.html`:

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
                <app-ui-card title="Stock Withdrawal Information">
                    <div class="col-xl-12 p-3" card-body>

                        <!-- ─── Section: Basic Info ─────────────────────────────────── -->
                        <div class="row g-3">

                            <div class="col-md-3">
                                <label class="form-label fw-bold">Voucher Date <span class="text-danger">*</span></label>
                                <input type="text" mwlFlatpickr [options]="flatpickrOptions"
                                       class="form-control" [(ngModel)]="voucherDate"
                                       name="voucherDate" placeholder="Select date" required/>
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
                            </div>

                            <div class="col-md-3">
                                <label class="form-label fw-bold">Inventory Category <span class="text-danger">*</span></label>
                                <select class="form-select"
                                        [(ngModel)]="selectedCategory"
                                        name="inventoryCategory"
                                        [compareWith]="compareById"
                                        [disabled]="details.length > 0"
                                        (ngModelChange)="onCategoryChange()">
                                    <option [ngValue]="null">— Select Category —</option>
                                    @for (cat of inventoryCategories(); track cat.id) {
                                        <option [ngValue]="cat">{{ cat.description || cat.name }}</option>
                                    }
                                </select>
                            </div>

                            <div class="col-md-3">
                                <label class="form-label fw-bold">Purpose <span class="text-danger">*</span></label>
                                <select class="form-select"
                                        [(ngModel)]="selectedPurpose"
                                        name="purpose"
                                        [compareWith]="compareById">
                                    <option [ngValue]="null">— Select Purpose —</option>
                                    @for (p of purposes(); track p.id) {
                                        <option [ngValue]="p">{{ p.description || p.name }}</option>
                                    }
                                </select>
                            </div>

                            @if (showPurposeInput) {
                                <div class="col-md-12">
                                    <label class="form-label fw-bold">Description / Purpose <span class="text-danger">*</span></label>
                                    <textarea class="form-control" [(ngModel)]="description" name="description"
                                              rows="2" placeholder="Describe the purpose" maxlength="1024"></textarea>
                                </div>
                            }

                        </div>

                        <!-- ─── Section: Reference Document (Work Order) ────────────── -->
                        <hr/>
                        <div class="col-12 fs-xs text-uppercase fw-semibold text-muted mb-2">Reference Document (Optional)</div>

                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label fw-bold">Work Order</label>
                                <div class="input-group">
                                    <input type="text" class="form-control"
                                           [value]="workOrderDesc" readonly
                                           placeholder="Browse work order..."/>
                                    <button type="button" class="btn btn-success fw-bold"
                                            (click)="openWorkOrderBrowse()">
                                        <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                                    </button>
                                    @if (selectedWorkOrder) {
                                        <button type="button" class="btn btn-danger fw-bold" (click)="clearWorkOrder()">
                                            <ng-icon name="tablerTrash" class="ps-0 pe-0 fw-bold"></ng-icon>
                                        </button>
                                    }
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label fw-bold">Requisition Voucher / Cost Estimate</label>
                                <div class="input-group">
                                    <input type="text" class="form-control" readonly placeholder="(Pending integration)"/>
                                    <button type="button" class="btn btn-success fw-bold" disabled>
                                        <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                                    </button>
                                </div>
                                <div class="form-text text-muted fs-xxs">RV / Cost Estimate browse pending integration.</div>
                            </div>
                        </div>

                        <!-- ─── Section: Employees ─────────────────────────────────── -->
                        @if (showEmployeeSection) {
                            <hr/>
                            <div class="d-flex align-items-center justify-content-between mb-2">
                                <span class="fs-xs text-uppercase fw-semibold text-muted">Employees</span>
                                <button type="button" class="btn btn-success btn-sm fw-bold"
                                        (click)="openEmployeeBrowse()">
                                    <ng-icon name="tablerPlus" class="ps-0 pe-2 fw-bold"></ng-icon>Add Employee
                                </button>
                            </div>
                            <div class="table-responsive">
                                <table class="table table-custom table-centered w-100 mb-0">
                                    <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                                        <tr class="text-uppercase fs-xxs">
                                            <th>#</th>
                                            <th>Name</th>
                                            <th style="width:50px"></th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        @if (employees.length === 0) {
                                            <tr>
                                                <td colspan="3" class="text-center text-muted py-3">
                                                    No employees added. Click Add Employee to browse.
                                                </td>
                                            </tr>
                                        }
                                        @for (emp of employees; track $index) {
                                            <tr>
                                                <td class="text-center text-muted">{{ $index + 1 }}</td>
                                                <td>{{ emp.name }}</td>
                                                <td class="text-center">
                                                    <button type="button"
                                                            class="btn btn-light btn-icon btn-sm rounded-circle text-danger"
                                                            (click)="removeEmployee($index)">
                                                        <ng-icon name="tablerTrash"></ng-icon>
                                                    </button>
                                                </td>
                                            </tr>
                                        }
                                    </tbody>
                                </table>
                            </div>
                        }

                        <!-- ─── Section: Items Table ────────────────────────────────── -->
                        <hr/>
                        <div class="d-flex align-items-center justify-content-between mb-2">
                            <span class="fs-xs text-uppercase fw-semibold text-muted">Items</span>
                            <div class="d-flex gap-2">
                                <button type="button" class="btn btn-success btn-sm fw-bold"
                                        (click)="openItemStockBrowse()">
                                    <ng-icon name="tablerPlus" class="ps-0 pe-2 fw-bold"></ng-icon>Browse Items
                                </button>
                                <button type="button" class="btn btn-success btn-sm fw-bold" disabled
                                        title="Special Equipment browse — pending integration">
                                    <ng-icon name="tablerPlus" class="ps-0 pe-2 fw-bold"></ng-icon>Special Equipment
                                </button>
                            </div>
                        </div>

                        <div class="table-responsive">
                            <table class="table table-custom table-centered table-hover w-100 mb-0">
                                <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                                    <tr class="text-uppercase fs-xxs">
                                        <th style="width:40px">#</th>
                                        <th style="width:80px">Code</th>
                                        <th style="width:70px">Unit</th>
                                        <th>Description</th>
                                        <th class="text-center" style="width:100px">Balance</th>
                                        <th class="text-center" style="width:120px">Quantity</th>
                                        <th style="width:50px"></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    @if (details.length === 0) {
                                        <tr>
                                            <td colspan="7" class="text-center text-muted py-3">
                                                Click <strong>Browse Items</strong> to add items.
                                            </td>
                                        </tr>
                                    }
                                    @for (row of details; track $index) {
                                        <tr>
                                            <td class="text-center text-muted">{{ $index + 1 }}</td>
                                            <td class="fw-bold">{{ row.itemCode }}</td>
                                            <td>{{ row.unitCode }}</td>
                                            <td>{{ row.itemDescription }}</td>
                                            <td class="text-center fw-bold">{{ row.inventoryBalance | number:'1.0-3' }}</td>
                                            <td>
                                                <input type="number"
                                                       class="form-control form-control-sm text-center fw-bold"
                                                       [(ngModel)]="row.quantity"
                                                       [name]="'qty_' + $index"
                                                       (ngModelChange)="onQuantityChange($index)"
                                                       min="0" step="0.01" placeholder="0"/>
                                            </td>
                                            <td class="text-center">
                                                <button type="button"
                                                        class="btn btn-light btn-icon btn-sm rounded-circle text-danger"
                                                        (click)="removeRow($index)"
                                                        title="Remove">
                                                    <ng-icon name="tablerTrash"></ng-icon>
                                                </button>
                                            </td>
                                        </tr>
                                    }
                                    @if (details.length > 0) {
                                        <tr class="fw-bold bg-light bg-opacity-50">
                                            <td colspan="5" class="text-end text-uppercase fs-xxs">Total Quantity:</td>
                                            <td class="text-center">{{ totalQuantity | number:'1.0-3' }}</td>
                                            <td></td>
                                        </tr>
                                    }
                                </tbody>
                            </table>
                        </div>

                        <!-- ─── Section: Approving Officer ──────────────────────────── -->
                        <hr/>
                        <div class="col-12 fs-xs text-uppercase fw-semibold text-muted mb-2">Signatories</div>

                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label fw-bold">Approving Officer <span class="text-danger">*</span></label>
                                <div class="input-group">
                                    <input type="text" class="form-control"
                                           [value]="approvingOfficer ? (approvingOfficer.fullName || approvingOfficer.name || '') : ''"
                                           readonly placeholder="Browse approving officer..."/>
                                    <button type="button" class="btn btn-success fw-bold"
                                            (click)="openApprovingOfficerBrowse()">
                                        <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                                    </button>
                                </div>
                            </div>
                        </div>

                        <!-- ─── Footer ──────────────────────────────────────────────── -->
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

- [ ] **Step 4: Verify by inspection**

Re-read both files. Confirm:
- All imports present: `BrowseItemStockModalComponent`, `BrowseEntityModalComponent`, `BrowseWorkOrderModalComponent`, `RouterLink`, `provideIcons`
- `showPurposeInput` and `showEmployeeSection` getters use `.type` comparison
- `onQuantityChange()` caps at `inventoryBalance`
- `save()` validates all required fields individually before posting
- Save payload includes `approvingOfficer` with `accountNo + fullName`, `inventoryLocation: { id }`, `inventoryCategory: { id }`, `purpose: { id }`, `type`, `employees`, `details`, `workOrder`
- HTML: `@if`/`@for` only (no `*ngIf`), unique `[name]="'qty_' + $index"`, `[compareWith]="compareById"` on selects
- Disabled inventory location and category selects when `details.length > 0`

---

### Task 5: Fix withdrawal-detail — missing fields, employees, items table

**Files:**
- Modify: `frontend/src/app/pages/withdrawal/withdrawal-detail/withdrawal-detail.component.ts`
- Modify: `frontend/src/app/pages/withdrawal/withdrawal-detail/withdrawal-detail.component.html`

**Interfaces:**
- Consumes: API response object with fields: `data.inventoryLocation`, `data.inventoryCategory`, `data.purpose`, `data.employees[]`, `data.details[]` (each: `itemDescription`, `quantity`, `unitCode`, `quantityReleased`)

- [ ] **Step 1: Read both files**

Read `withdrawal-detail.component.ts` and `withdrawal-detail.component.html`. Current header shows: Code, Voucher Date, Status, Prepared By, Approved By, Description. Missing: Inventory Location, Inventory Category, Purpose, Employees, Items table.

- [ ] **Step 2: Update the TypeScript — add provideIcons**

In `withdrawal-detail.component.ts`, add `provideIcons` to the imports and providers:

```typescript
import { provideIcons } from '@ng-icons/core';
import { tablerPrinter, tablerEdit, tablerArrowLeft } from '@ng-icons/tabler-icons';
```

Add to `@Component` decorator:
```typescript
    providers: [provideIcons({ tablerPrinter, tablerEdit, tablerArrowLeft })]
```

- [ ] **Step 3: Update the HTML — expand header card**

In `withdrawal-detail.component.html`, in the first `<div class="card mb-3">` header card, **add these missing fields** to the existing `<div class="row g-3">`:

Add after the `Approved By` block (before the `@if (data.description)` conditional):

```html
            <div class="col-md-3">
                <div class="fs-xs text-uppercase fw-bold text-muted mb-1">Inventory Location</div>
                <div>{{ data.inventoryLocation?.description || data.inventoryLocation?.name || '—' }}</div>
            </div>
            <div class="col-md-3">
                <div class="fs-xs text-uppercase fw-bold text-muted mb-1">Inventory Category</div>
                <div>{{ data.inventoryCategory?.description || data.inventoryCategory?.name || '—' }}</div>
            </div>
            <div class="col-md-3">
                <div class="fs-xs text-uppercase fw-bold text-muted mb-1">Purpose</div>
                <div>{{ data.purpose?.description || data.purpose?.name || '—' }}</div>
            </div>
```

- [ ] **Step 4: Add Employees table card (conditional)**

After the header card's closing `</div>` (after `</div>` that wraps `class="card mb-3"`) and before the workflow-actions card, insert:

```html
@if (data.employees?.length > 0) {
<div class="card mb-3">
    <div class="card-header">
        <h6 class="fw-semibold text-uppercase fs-xs mb-0">Employees</h6>
    </div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-custom table-centered w-100 mb-0">
                <thead class="bg-light bg-opacity-25 thead-sm">
                    <tr class="text-uppercase fs-xxs">
                        <th>#</th>
                        <th>Name</th>
                    </tr>
                </thead>
                <tbody>
                    @for (emp of data.employees; track $index; let i = $index) {
                        <tr>
                            <td class="text-center">{{ i + 1 }}</td>
                            <td>{{ emp.name }}</td>
                        </tr>
                    }
                </tbody>
            </table>
        </div>
    </div>
</div>
}
```

- [ ] **Step 5: Add Items table card**

Directly after the employees card block (before the workflow-actions card), insert:

```html
@if (data.details?.length > 0) {
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
                        <th class="text-center">Qty Released</th>
                    </tr>
                </thead>
                <tbody>
                    @for (item of data.details; track $index; let i = $index) {
                        <tr>
                            <td class="text-center">{{ i + 1 }}</td>
                            <td>{{ item.itemDescription }}</td>
                            <td class="text-center fw-bold">{{ item.quantity | number:'1.0-3' }}</td>
                            <td>{{ item.unitCode }}</td>
                            <td class="text-center fw-bold text-primary">{{ item.quantityReleased | number:'1.0-3' }}</td>
                        </tr>
                    }
                </tbody>
            </table>
        </div>
    </div>
</div>
}
```

- [ ] **Step 6: Verify by inspection**

Re-read both files. Confirm:
- `.ts` has `provideIcons` in providers with tablerPrinter, tablerEdit, tablerArrowLeft
- `.html` header card has Location, Category, Purpose fields added
- Employees card uses `@if (data.employees?.length > 0)` and `@for`
- Items card uses `@if (data.details?.length > 0)` and displays `itemDescription`, `quantity`, `unitCode`, `quantityReleased`
- No `*ngIf` / `*ngFor` anywhere

---

## Deferred Items (non-critical for initial parity)

| Item | Reason |
|---|---|
| RV (Requisition Voucher) browse in add-edit | No `BrowseRvForWithdrawalModalComponent` exists in v2 shared/modals |
| Cost Estimate browse in add-edit | No `BrowseCostEstimateModalComponent` exists in v2 shared/modals |
| Special Equipment browse | No `BrowseSpecialEquipmentModalComponent` exists |
| Turn-on-order withdrawal | Legacy-specific feature, deferred |
| MRS category switching logic | Complex multi-category MRS override logic, deferred |
| 30-day date validation | Server-side enforcement, not user-facing in v2 |

All deferred browses are rendered as disabled buttons with notes in the form (already included in Task 4 HTML).
