# Memorandum Receipt Migration Alignment Plan (Phase 2)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete alignment of the Angular 19 Memorandum Receipt module with its legacy JSP/AngularJS counterpart — fixing the `assignItem()` quantity bug, adding 5 missing service methods, adding employee browse filter with `?em=` server-side filtering, and implementing two entirely missing workflows: "Create Multiple Employee MR" and "Re-issue Returned MR".

**Architecture:** Six sequential tasks: (1) fix `assignItem()` bug + add 5 service methods, (2) update main list with employee filter and 3 create buttons, (3) create BrowseMrEmployeeItemsModalComponent, (4) create memorandum-receipt-create-multiple component + route, (5) create BrowseReturnedMrModalComponent, (6) create memorandum-receipt-reissue component + route.

**Tech Stack:** Angular 19, TypeScript, NgBootstrap, ng-icons/tabler-icons, COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, SHARED_PROVIDERS, ModalService, BrowseEntityModalComponent

## Global Constraints

- Angular 19 control flow only: `@if`, `@for`, `@else` — never `*ngIf`/`*ngFor`
- `provideIcons({...})` in component `providers[]`, never in `imports[]`
- All browse modals: `ChangeDetectionStrategy.OnPush` + `cdr.markForCheck()`, `imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS]` (or just `COMMON_ALL_PAGE_IMPORTS` + `FormsModule` for lighter modals)
- `ModalService` path: `@/app/shared/modals/modal-service` — use `async/await` + `try/catch`, NOT `.then()/.catch()`
- Modal result: `{ action: 'select', data: any }` — check `result?.action === 'select' && result?.data`
- Browse modal components are imported at the top of the TS file but NOT added to `imports[]` array in `@Component`
- `AlertService` for all notifications — no native `alert()`/`confirm()`
- No `git commit` or `git push` — user handles all git operations
- MERGE_BASE: `1e5090ff210e95060644e2d0090c3e033f81ce61`
- Unique `[name]="'field_' + $index"` on inputs inside `@for` loops
- Save button label: `{{ editMode ? 'Update' : 'Save ' + module }}` for edit components; custom label for new components
- **MR-specific:** API uses flat `date` field (not `voucherDate`), and flat `status` field
- Module string is `'Memorandum Receipt'` throughout
- BASE_API from `environment.get('baseApiUrl')`, BASE_URL from `environment.get('baseUrl')`

---

### Task 1: Fix `assignItem()` bug + add 5 service methods

**Files:**
- Modify: `frontend/src/app/pages/memorandum-receipt/memorandum-receipt-add-edit/memorandum-receipt-add-edit.component.ts`
- Modify: `frontend/src/app/pages/memorandum-receipt/memorandum-receipt.service.ts`

**Interfaces:**
- Produces: `listByEmployee(from, to, accountNo)`, `createMultiple(forms[])`, `createReturnedMr(form)`, `getStockWithdrawalEmployees(swId)`, `getReturnedMrsByEmployee(accountNo)` for Tasks 2, 4, 6
- The `assignItem()` fix: `oldQuantity: 0` (was `oldQuantity: qty`) so `onQtyChange()` computes `newQty = userQty - 0 = userQty`, giving `remaining = oldRemainingBalance - userQty` (matching OLD behavior)

- [ ] **Step 1: Fix `assignItem()` in add-edit TS**

In `frontend/src/app/pages/memorandum-receipt/memorandum-receipt-add-edit/memorandum-receipt-add-edit.component.ts`, find the `assignItem()` method (around line 184). Change `oldQuantity: qty` to `oldQuantity: 0`:

```typescript
assignItem(item: any): void {
    item.assigned = true;
    const qty = 1;
    item.remaining    = item.oldRemainingBalance - qty;
    item.totalAssigned = item.oldTotalAssigned + qty;
    this.assignedItems = [...this.assignedItems, {
        stockWithdrawalDetail: item.stockWithdrawalDetail,
        quantity:              qty,
        reassignedQuantity:    0,
        oldQuantity:           0,    // FIXED: was qty; must be 0 so onQtyChange computes absolute qty
        itemCode:              item.itemCode,
        itemDescription:       item.itemDescription,
        unitCode:              item.unitCode,
        _availableRef:         item
    }];
}
```

- [ ] **Step 2: Add 5 methods to memorandum-receipt.service.ts**

Append after the closing `}` of the `getStockWithdrawalBalance()` method (before the final `}` of the class):

```typescript
listByEmployee(from: string, to: string, accountNo: number): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_API}/memorandum-receipt/list/${from}/${to}?em=${accountNo}`);
}

createMultiple(forms: any[]): Observable<any> {
    return this.http.post(`${BASE_API}/memorandum-receipt/create-multiple`, forms, httpOptions);
}

createReturnedMr(form: any): Observable<any> {
    return this.http.post(`${BASE_API}/memorandum-receipt/create-returned-mr`, form, httpOptions);
}

getStockWithdrawalEmployees(swId: number): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_API}/memorandum-receipt/sw-employees/${swId}`);
}

getReturnedMrsByEmployee(accountNo: number): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_API}/memorandum-receipt/returned-memos/${accountNo}`);
}
```

- [ ] **Step 3: Verify no compile errors**

Run: `cd frontend && npx ng build --configuration development 2>&1 | grep -E "ERROR|error TS" | head -20`
Expected: no error lines mentioning memorandum-receipt

---

### Task 2: Main — employee browse filter + 3 create buttons

**Files:**
- Modify: `frontend/src/app/pages/memorandum-receipt/memorandum-receipt-main/memorandum-receipt-main.component.ts`
- Modify: `frontend/src/app/pages/memorandum-receipt/memorandum-receipt-main/memorandum-receipt-main.component.html`

**Interfaces:**
- Consumes: `MemorandumReceiptService.listByEmployee(from, to, accountNo)` from Task 1, `ModalService`, `BrowseEntityModalComponent`
- `employee.accountNo` is used for the `?em=` query param
- `employee.name || employee.fullName` for display

- [ ] **Step 1: Rewrite the main TS**

Replace the full content of `memorandum-receipt-main.component.ts` with:

```typescript
import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { MemorandumReceiptService } from '../memorandum-receipt.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerRefresh, tablerPlus, tablerEye, tablerEdit, tablerUsers, tablerRepeat } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-memorandum-receipt-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults(), provideIcons({ tablerSearch, tablerRefresh, tablerPlus, tablerEye, tablerEdit, tablerUsers, tablerRepeat })],
    templateUrl: './memorandum-receipt-main.component.html'
})
export class MemorandumReceiptMainComponent {
    module    = 'Memorandum Receipt';
    subModule = '';
    menuLink  = 'memorandum-receipt';

    records   = signal<any[]>([]);
    isLoading = signal(false);

    page     = 1;
    pageSize = 10;

    get filteredRecords(): any[] {
        const q = this.searchText.toLowerCase();
        return q
            ? this.records().filter(r =>
                (r.code        || '').toLowerCase().includes(q) ||
                (r.employee?.name     || '').toLowerCase().includes(q) ||
                (r.employee?.fullName || '').toLowerCase().includes(q))
            : this.records();
    }

    get filteredTotal(): number { return this.filteredRecords.length; }

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.filteredRecords.slice(start, start + this.pageSize);
    }

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };
    fromDate   = '';
    toDate     = '';
    searchText = '';
    employee: any = null;

    private service      = inject(MemorandumReceiptService);
    private modalService = inject(ModalService);
    private alertService = inject(AlertService);

    ngOnInit(): void { this.setDefaultDates(); this.load(); }

    setDefaultDates(): void {
        const now = new Date(), first = new Date(now.getFullYear(), now.getMonth(), 1);
        this.fromDate = first.toISOString().substring(0, 10);
        this.toDate   = now.toISOString().substring(0, 10);
    }

    load(): void {
        this.isLoading.set(true);
        let obs;
        if (this.fromDate && this.toDate && this.employee?.accountNo) {
            obs = this.service.listByEmployee(this.fromDate, this.toDate, this.employee.accountNo);
        } else if (this.fromDate && this.toDate) {
            obs = this.service.listByDateRange(this.fromDate, this.toDate);
        } else {
            obs = this.service.list();
        }
        obs.subscribe({
            next: (data) => { this.records.set(data || []); this.page = 1; this.isLoading.set(false); },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    reset(): void { this.setDefaultDates(); this.searchText = ''; this.employee = null; this.load(); }

    async openEmployeeBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.employee = result.data;
            }
        } catch { }
    }

    isEditable(rec: any): boolean {
        const s = rec?.status || '';
        return s === 'Document Created' || s === 'Returned to Creator';
    }
}
```

- [ ] **Step 2: Rewrite the main HTML**

Replace the full content of `memorandum-receipt-main.component.html` with:

```html
<div class="container-fluid">
    <app-page-title [title]="module" [subTitle]="subModule" [menuLink]="menuLink"/>
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
                <div class="col-md-auto">
                    <label class="form-label fw-bold mb-1">Employee</label>
                    <div class="input-group">
                        <input type="text" class="form-control"
                               [value]="employee?.name || employee?.fullName || ''"
                               readonly placeholder="All employees"/>
                        <button type="button" class="btn btn-success fw-bold" (click)="openEmployeeBrowse()">
                            <ng-icon name="tablerUsers" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                        </button>
                    </div>
                </div>
                <div class="col-md-auto">
                    <button type="button" class="btn btn-success fw-bold" (click)="load()">
                        <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Search
                    </button>
                </div>
                <div class="col-md-auto">
                    <button type="button" class="btn btn-danger fw-bold" (click)="reset()">
                        <ng-icon name="tablerRefresh" class="ps-0 pe-2 fw-bold"></ng-icon>Reset
                    </button>
                </div>
                <div class="col-md-auto ms-auto">
                    <input type="text" class="form-control" [(ngModel)]="searchText"
                           placeholder="Search..." (input)="page = 1"/>
                </div>
                <div class="col-md-auto">
                    <a [routerLink]="['/' + menuLink, 'create']" class="btn btn-primary fw-bold">
                        <ng-icon name="tablerPlus" class="ps-0 pe-2 fw-bold"></ng-icon>Create Office MR
                    </a>
                </div>
                <div class="col-md-auto">
                    <a [routerLink]="['/' + menuLink, 'create-multiple']" class="btn btn-primary fw-bold">
                        <ng-icon name="tablerPlus" class="ps-0 pe-2 fw-bold"></ng-icon>Create Multiple Employee MR
                    </a>
                </div>
                <div class="col-md-auto">
                    <a [routerLink]="['/' + menuLink, 'reissue']" class="btn btn-primary fw-bold">
                        <ng-icon name="tablerRepeat" class="ps-0 pe-2 fw-bold"></ng-icon>Re-issue Returned MR
                    </a>
                </div>
            </div>

            <div class="table-responsive">
                <table class="table table-custom table-centered table-select table-hover w-100 mb-0">
                    <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                        <tr class="text-uppercase fs-xxs">
                            <th>Code</th><th>Returned MR</th><th>Date</th><th>Office</th><th>Employee</th><th>Status</th><th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        @if (isLoading()) {
                            <tr><td colspan="7" class="text-center text-muted py-4">
                                <span class="spinner-border spinner-border-sm me-2" role="status"></span>Loading...
                            </td></tr>
                        } @else if (pagedRecords.length === 0) {
                            <tr><td colspan="7" class="text-center text-muted py-4">No records found.</td></tr>
                        } @else {
                            @for (rec of pagedRecords; track rec.id) {
                                <tr>
                                    <td class="fw-bold">{{ rec.code }}</td>
                                    <td>{{ rec.returnedMR || '—' }}</td>
                                    <td>{{ rec.date | date:'MM/dd/yyyy' }}</td>
                                    <td>{{ rec.office?.name || '—' }}</td>
                                    <td>{{ rec.employee?.name || rec.employee?.fullName || '—' }}</td>
                                    <td><span class="badge bg-secondary bg-opacity-25 text-dark border">{{ rec.status || '—' }}</span></td>
                                    <td>
                                        <div class="d-flex gap-1">
                                            <a [routerLink]="['/' + menuLink, rec.id, 'detail']"
                                               class="btn btn-light btn-icon btn-sm rounded-circle" ngbTooltip="View">
                                                <ng-icon name="tablerEye"></ng-icon>
                                            </a>
                                            @if (isEditable(rec)) {
                                                <a [routerLink]="['/' + menuLink, rec.id, 'edit']"
                                                   class="btn btn-light btn-icon btn-sm rounded-circle" ngbTooltip="Edit">
                                                    <ng-icon name="tablerEdit"></ng-icon>
                                                </a>
                                            }
                                        </div>
                                    </td>
                                </tr>
                            }
                        }
                    </tbody>
                </table>
            </div>

            @if (filteredTotal > pageSize) {
                <div class="d-flex justify-content-end mt-3">
                    <ngb-pagination [(page)]="page" [pageSize]="pageSize" [collectionSize]="filteredTotal"/>
                </div>
            }
        </div>
    </div>
</div>
```

- [ ] **Step 3: Verify no compile errors**

Run: `cd frontend && npx ng build --configuration development 2>&1 | grep -E "ERROR|error TS" | head -20`
Expected: no error lines mentioning memorandum-receipt-main

---

### Task 3: BrowseMrEmployeeItemsModalComponent (new)

**Files:**
- Create: `frontend/src/app/shared/modals/browse-mr-employee-items-modal/browse-mr-employee-items-modal.component.ts`
- Create: `frontend/src/app/shared/modals/browse-mr-employee-items-modal/browse-mr-employee-items-modal.component.html`

**Interfaces:**
- Input props (set by ModalService data param): `employee: any = null`, `items: any[] = []`
- Each `item` has: `itemCode`, `itemDescription`, `unitCode`, `remaining` (computed by caller), `stockWithdrawalDetail`
- Emits: `{ action: 'select', data: assignedItems[] }` where each assigned item has `stockWithdrawalDetail`, `itemCode`, `itemDescription`, `unitCode`, `quantity`
- Used by Task 4 (create-multiple component)

- [ ] **Step 1: Create the TypeScript file**

```typescript
import { Component, ChangeDetectionStrategy, inject } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-browse-mr-employee-items-modal',
    templateUrl: './browse-mr-employee-items-modal.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, FormsModule]
})
export class BrowseMrEmployeeItemsModalComponent {
    employee: any  = null;
    items:    any[] = [];
    quantities: number[] = [];

    private activeModal = inject(NgbActiveModal);

    ngOnInit(): void {
        this.quantities = this.items.map(() => 0);
    }

    confirm(): void {
        const assigned = this.items
            .map((item, i) => ({ item, qty: this.quantities[i] }))
            .filter(x => x.qty > 0)
            .map(x => ({
                stockWithdrawalDetail: x.item.stockWithdrawalDetail,
                itemCode:              x.item.itemCode,
                itemDescription:       x.item.itemDescription,
                unitCode:              x.item.unitCode,
                quantity:              x.qty
            }));
        this.activeModal.close({ action: 'select', data: assigned });
    }

    dismiss(): void { this.activeModal.dismiss(); }
}
```

- [ ] **Step 2: Create the HTML file**

```html
<div class="modal-header">
    <h5 class="modal-title fw-bold">
        Assign Items — {{ employee?.name || employee?.fullName || 'Employee' }}
    </h5>
    <button type="button" class="btn-close" (click)="dismiss()"></button>
</div>
<div class="modal-body">
    <div class="table-responsive">
        <table class="table table-custom table-centered w-100 mb-0">
            <thead class="bg-light bg-opacity-25 thead-sm">
                <tr class="text-uppercase fs-xxs">
                    <th>#</th>
                    <th>Code</th>
                    <th>Description</th>
                    <th>Unit</th>
                    <th class="text-center">Remaining</th>
                    <th class="text-center" style="width:120px">Quantity</th>
                </tr>
            </thead>
            <tbody>
                @if (items.length === 0) {
                    <tr><td colspan="6" class="text-center text-muted py-4">No items available.</td></tr>
                } @else {
                    @for (item of items; track $index; let i = $index) {
                        <tr>
                            <td class="text-center">{{ i + 1 }}</td>
                            <td class="fw-bold">{{ item.itemCode }}</td>
                            <td>{{ item.itemDescription }}</td>
                            <td>{{ item.unitCode }}</td>
                            <td class="text-center">{{ item.remaining | number:'1.0-3' }}</td>
                            <td class="text-center">
                                <input type="number" class="form-control form-control-sm text-center"
                                       [name]="'empQty_' + i"
                                       [(ngModel)]="quantities[i]"
                                       min="0" [max]="item.remaining" step="1"/>
                            </td>
                        </tr>
                    }
                }
            </tbody>
        </table>
    </div>
</div>
<div class="modal-footer">
    <button type="button" class="btn btn-light fw-bold" (click)="dismiss()">Cancel</button>
    <button type="button" class="btn btn-primary fw-bold" (click)="confirm()">Confirm Assignment</button>
</div>
```

- [ ] **Step 3: Verify no compile errors**

Run: `cd frontend && npx ng build --configuration development 2>&1 | grep -E "ERROR|error TS" | head -20`
Expected: no error lines mentioning browse-mr-employee-items-modal

---

### Task 4: memorandum-receipt-create-multiple component + route

**Files:**
- Create: `frontend/src/app/pages/memorandum-receipt/memorandum-receipt-create-multiple/memorandum-receipt-create-multiple.component.ts`
- Create: `frontend/src/app/pages/memorandum-receipt/memorandum-receipt-create-multiple/memorandum-receipt-create-multiple.component.html`
- Modify: `frontend/src/app/pages/memorandum-receipt/memorandum-receipt.route.ts`

**Interfaces:**
- Consumes: `BrowseMrStockWithdrawalModalComponent` (existing at `@/app/shared/modals/browse-mr-stock-withdrawal-modal/browse-mr-stock-withdrawal-modal.component`), `BrowseMrEmployeeItemsModalComponent` (Task 3), `BrowseEntityModalComponent` (existing), `MemorandumReceiptService.createMultiple/getStockWithdrawalEmployees/getStockWithdrawalBalance` (Task 1)
- `multipleMR[]` entry: `{ employee: any; assignedItems: any[] }` — `employee` from `getStockWithdrawalEmployees`, `assignedItems` from modal result
- Payload to `createMultiple()`: array of `{ date, stockWithdrawal: {id}, employee: {accountNo, name}, approvingOfficer: {accountNo, fullName}, memorandumReceiptDetails: [{stockWithdrawalDetail: {id}, quantity, reassignedQuantity: 0}] }`
- OLD validation: total qty assigned across all employees for each item must equal `item.remaining` (the available balance)
- On success: navigate to `'/' + menuLink` (list page)

- [ ] **Step 1: Create the TypeScript file**

```typescript
import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { MemorandumReceiptService } from '../memorandum-receipt.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseMrStockWithdrawalModalComponent } from '@/app/shared/modals/browse-mr-stock-withdrawal-modal/browse-mr-stock-withdrawal-modal.component';
import { BrowseMrEmployeeItemsModalComponent } from '@/app/shared/modals/browse-mr-employee-items-modal/browse-mr-employee-items-modal.component';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerArrowLeft, tablerCheck } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-memorandum-receipt-create-multiple',
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
    templateUrl: './memorandum-receipt-create-multiple.component.html'
})
export class MemorandumReceiptCreateMultipleComponent {
    module    = 'Memorandum Receipt';
    subModule = 'Create Multiple Employee MR';
    menuLink  = 'memorandum-receipt';

    isLoading = signal(false);
    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    date            = '';
    selectedSW: any = null;
    selectedSWCode  = '';
    availableItems: any[]  = [];   // items from SW with their remaining balance
    employees:      any[]  = [];   // employees associated with this SW
    multipleMR: { employee: any; assignedItems: any[] }[] = [];
    approvingOfficer: any  = null;

    private service      = inject(MemorandumReceiptService);
    private modalService = inject(ModalService);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.date = new Date().toISOString().substring(0, 10);
        this.loadDefaultSignatories();
    }

    private loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (data) => { if (data?.approvedBy) this.approvingOfficer = data.approvedBy; },
            error: () => {}
        });
    }

    async openStockWithdrawalBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseMrStockWithdrawalModalComponent, {}, { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const doc = result.data;
                this.selectedSW     = doc;
                this.selectedSWCode = doc.code || '';
                this.multipleMR     = [];
                this.availableItems = (doc.items || doc.details || []).map((item: any) => ({
                    stockWithdrawalDetail: item,
                    itemCode:              item.item?.code || item.itemCode || '',
                    itemDescription:       item.item?.description || item.itemDescription || '',
                    unitCode:              item.item?.unit?.code || item.unitCode || '',
                    quantity:              item.quantity,
                    remaining:             item.quantity,
                    oldRemainingBalance:   item.quantity
                }));
                this.loadBalances();
                this.loadEmployees(doc.id);
            }
        } catch { }
    }

    private loadBalances(): void {
        this.availableItems.forEach(item => {
            const detailId = item.stockWithdrawalDetail?.id;
            if (!detailId) return;
            this.service.getStockWithdrawalBalance(detailId).subscribe({
                next: (data) => {
                    const assigned = data[0]?.assigned ?? 0;
                    item.remaining          = item.quantity - assigned;
                    item.oldRemainingBalance = item.remaining;
                },
                error: () => {}
            });
        });
    }

    private loadEmployees(swId: number): void {
        this.service.getStockWithdrawalEmployees(swId).subscribe({
            next: (employees) => {
                this.employees  = employees || [];
                this.multipleMR = this.employees.map(emp => ({ employee: emp, assignedItems: [] }));
            },
            error: () => { this.employees = []; this.multipleMR = []; }
        });
    }

    async openEmployeeItemsModal(entry: { employee: any; assignedItems: any[] }): Promise<void> {
        const itemsForModal = this.availableItems.map(item => {
            const detailId = item.stockWithdrawalDetail?.id;
            const alreadyAssignedToOthers = this.multipleMR
                .filter(e => e.employee.accountNo !== entry.employee.accountNo)
                .reduce((sum, e) => {
                    const found = e.assignedItems.find((a: any) => a.stockWithdrawalDetail?.id === detailId);
                    return sum + (found?.quantity || 0);
                }, 0);
            return { ...item, remaining: item.remaining - alreadyAssignedToOthers };
        }).filter(i => i.remaining > 0);

        try {
            const result = await this.modalService.openModal(
                BrowseMrEmployeeItemsModalComponent,
                { employee: entry.employee, items: itemsForModal },
                { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                entry.assignedItems = result.data;
            }
        } catch { }
    }

    hasEmployeeAssignment(entry: { employee: any; assignedItems: any[] }): boolean {
        return entry.assignedItems.length > 0;
    }

    async openApprovingOfficerBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const e = result.data;
                this.approvingOfficer = { accountNo: e.accountNo, fullName: e.fullName || e.name || '' };
            }
        } catch { }
    }

    save(): void {
        if (!this.date)
            { this.alertService.warning(this.module, 'Validation', 'Date is required.'); return; }
        if (!this.selectedSW?.id)
            { this.alertService.warning(this.module, 'Validation', 'Stock Withdrawal is required.'); return; }
        if (!this.approvingOfficer?.accountNo)
            { this.alertService.warning(this.module, 'Validation', 'Noted By is required.'); return; }

        // Validate all items are fully distributed
        const unassigned = this.availableItems.filter(item => {
            const detailId = item.stockWithdrawalDetail?.id;
            const totalQty = this.multipleMR.reduce((sum, e) => {
                const found = e.assignedItems.find((a: any) => a.stockWithdrawalDetail?.id === detailId);
                return sum + (found?.quantity || 0);
            }, 0);
            return totalQty < item.remaining;
        });
        if (unassigned.length > 0)
            { this.alertService.warning(this.module, 'Validation', 'All items must be fully distributed to employees.'); return; }

        const forms = this.multipleMR
            .filter(e => e.assignedItems.length > 0)
            .map(e => ({
                date:             this.date,
                stockWithdrawal:  { id: this.selectedSW.id },
                employee:         { accountNo: e.employee.accountNo, name: e.employee.name || e.employee.fullName || '' },
                approvingOfficer: { accountNo: this.approvingOfficer.accountNo, fullName: this.approvingOfficer.fullName || this.approvingOfficer.name || '' },
                memorandumReceiptDetails: e.assignedItems.map((d: any) => ({
                    stockWithdrawalDetail: { id: d.stockWithdrawalDetail?.id },
                    quantity:              d.quantity,
                    reassignedQuantity:    0
                }))
            }));

        if (forms.length === 0)
            { this.alertService.warning(this.module, 'Validation', 'At least one employee must have items assigned.'); return; }

        this.isLoading.set(true);
        this.service.createMultiple(forms).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.success === false) {
                    this.alertService.error(this.module, 'Save', data.failureMessage || '');
                } else {
                    this.alertService.success(this.module, 'Multiple MRs created successfully.', '');
                    this.router.navigate(['/' + this.menuLink]);
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

- [ ] **Step 2: Create the HTML file**

```html
<div class="container-fluid">
    <app-page-title [title]="module" [subTitle]="subModule" [menuLink]="menuLink"/>
</div>
<div class="container-fluid">
    <form (ngSubmit)="save()" #f="ngForm">
        <div class="row g-3">
            <div class="col-xl-12">
                <app-ui-card title="Create Multiple Employee MR">
                    <div class="col-xl-12 p-3" card-body>
                        <div class="row g-3">
                            <div class="col-md-3">
                                <label class="form-label fw-bold">Date <span class="text-danger">*</span></label>
                                <input type="text" mwlFlatpickr [options]="flatpickrOptions"
                                       class="form-control" [(ngModel)]="date"
                                       name="date" placeholder="Select date" required/>
                            </div>
                            <div class="col-md-5">
                                <label class="form-label fw-bold">Stock Withdrawal <span class="text-danger">*</span></label>
                                <div class="input-group">
                                    <input type="text" class="form-control" [value]="selectedSWCode" readonly
                                           placeholder="No stock withdrawal selected"/>
                                    <button type="button" class="btn btn-success fw-bold" (click)="openStockWithdrawalBrowse()">
                                        <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                                    </button>
                                </div>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label fw-bold">Noted By <span class="text-danger">*</span></label>
                                <div class="input-group">
                                    <input type="text" class="form-control"
                                           [value]="approvingOfficer?.fullName || approvingOfficer?.name || ''" readonly
                                           placeholder="Browse approving officer"/>
                                    <button type="button" class="btn btn-success fw-bold" (click)="openApprovingOfficerBrowse()">
                                        <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                                    </button>
                                </div>
                            </div>
                        </div>

                        @if (multipleMR.length > 0) {
                        <hr class="my-3"/>
                        <div class="fs-xs text-uppercase fw-semibold text-muted mb-2">Employees</div>
                        <div class="table-responsive">
                            <table class="table table-custom table-centered w-100 mb-0">
                                <thead class="bg-light bg-opacity-25 thead-sm">
                                    <tr class="text-uppercase fs-xxs">
                                        <th>#</th>
                                        <th>Employee</th>
                                        <th class="text-center">Items Assigned</th>
                                        <th></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    @for (entry of multipleMR; track $index; let i = $index) {
                                        <tr>
                                            <td class="text-center">{{ i + 1 }}</td>
                                            <td>{{ entry.employee?.name || entry.employee?.fullName }}</td>
                                            <td class="text-center">
                                                @if (hasEmployeeAssignment(entry)) {
                                                    <span class="badge bg-success bg-opacity-25 text-success border">
                                                        {{ entry.assignedItems.length }} item(s)
                                                    </span>
                                                } @else {
                                                    <span class="text-muted">—</span>
                                                }
                                            </td>
                                            <td class="text-center">
                                                <button type="button" class="btn btn-success btn-sm fw-bold"
                                                        (click)="openEmployeeItemsModal(entry)">
                                                    Assign Items
                                                </button>
                                            </td>
                                        </tr>
                                    }
                                </tbody>
                            </table>
                        </div>
                        }

                        <div class="d-flex gap-2 justify-content-end align-items-center pt-3 border-top mt-3">
                            <a [routerLink]="['/' + menuLink]" class="btn btn-light fw-bold">
                                <ng-icon name="tablerArrowLeft" class="ps-0 pe-3 fw-bold"></ng-icon>Back
                            </a>
                            <button type="submit" class="btn btn-primary fw-bold" [disabled]="isLoading()">
                                <ng-icon name="tablerCheck" class="ps-0 pe-3 fw-bold"></ng-icon>
                                Save Multiple MR
                            </button>
                        </div>
                    </div>
                </app-ui-card>
            </div>
        </div>
    </form>
</div>
```

- [ ] **Step 3: Add the `create-multiple` route**

In `frontend/src/app/pages/memorandum-receipt/memorandum-receipt.route.ts`, add before the final `];`:

```typescript
{
    path: 'create-multiple',
    loadComponent: () => import('./memorandum-receipt-create-multiple/memorandum-receipt-create-multiple.component').then(m => m.MemorandumReceiptCreateMultipleComponent),
    data: { title: 'Create Multiple Employee MR', mainPath }
},
```

- [ ] **Step 4: Verify no compile errors**

Run: `cd frontend && npx ng build --configuration development 2>&1 | grep -E "ERROR|error TS" | head -20`
Expected: no error lines mentioning memorandum-receipt-create-multiple

---

### Task 5: BrowseReturnedMrModalComponent (new)

**Files:**
- Create: `frontend/src/app/shared/modals/browse-returned-mr-modal/browse-returned-mr-modal.component.ts`
- Create: `frontend/src/app/shared/modals/browse-returned-mr-modal/browse-returned-mr-modal.component.html`

**Interfaces:**
- Input prop (set by ModalService data param): `employeeAccountNo: number = 0`
- Fetches via `MemorandumReceiptService.getReturnedMrsByEmployee(accountNo)` (Task 1)
- Emits: `{ action: 'select', data: doc }` where `doc` has `.id`, `.code`, `.date`, `.employee`, `.memorandumReceiptDetails[]`
- Used by Task 6 (reissue component)

- [ ] **Step 1: Create the TypeScript file**

```typescript
import { Component, ChangeDetectionStrategy, ChangeDetectorRef, inject } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { MemorandumReceiptService } from '@/app/pages/memorandum-receipt/memorandum-receipt.service';

@Component({
    selector: 'app-browse-returned-mr-modal',
    templateUrl: './browse-returned-mr-modal.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS]
})
export class BrowseReturnedMrModalComponent {
    employeeAccountNo: number = 0;
    records:  any[]  = [];
    isLoading        = false;
    page      = 1;
    pageSize  = 10;

    get total(): number { return this.records.length; }

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.records.slice(start, start + this.pageSize);
    }

    private activeModal = inject(NgbActiveModal);
    private service     = inject(MemorandumReceiptService);
    private cdr         = inject(ChangeDetectorRef);

    ngOnInit(): void { this.load(); }

    load(): void {
        this.isLoading = true;
        this.service.getReturnedMrsByEmployee(this.employeeAccountNo).subscribe({
            next: (data) => { this.records = data || []; this.isLoading = false; this.cdr.markForCheck(); },
            error: () => { this.isLoading = false; this.cdr.markForCheck(); }
        });
    }

    select(doc: any): void { this.activeModal.close({ action: 'select', data: doc }); }
    dismiss(): void { this.activeModal.dismiss(); }
}
```

- [ ] **Step 2: Create the HTML file**

```html
<div class="modal-header">
    <h5 class="modal-title fw-bold">Browse Returned Memorandum Receipts</h5>
    <button type="button" class="btn-close" (click)="dismiss()"></button>
</div>
<div class="modal-body">
    <div class="table-responsive">
        <table class="table table-custom table-centered table-select table-hover w-100 mb-0">
            <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                <tr class="text-uppercase fs-xxs">
                    <th>#</th>
                    <th>Code</th>
                    <th>Date</th>
                    <th>Employee</th>
                    <th></th>
                </tr>
            </thead>
            <tbody>
                @if (isLoading) {
                    <tr><td colspan="5" class="text-center text-muted py-4">
                        <span class="spinner-border spinner-border-sm me-2" role="status"></span>Loading...
                    </td></tr>
                } @else if (pagedRecords.length === 0) {
                    <tr><td colspan="5" class="text-center text-muted py-4">No returned MRs found.</td></tr>
                } @else {
                    @for (doc of pagedRecords; track doc.id; let i = $index) {
                        <tr>
                            <td>{{ (page - 1) * pageSize + i + 1 }}</td>
                            <td class="fw-bold">{{ doc.code }}</td>
                            <td>{{ doc.date | date:'MM/dd/yyyy' }}</td>
                            <td>{{ doc.employee?.name || doc.employee?.fullName || '—' }}</td>
                            <td>
                                <button type="button" class="btn btn-primary btn-sm fw-bold" (click)="select(doc)">Select</button>
                            </td>
                        </tr>
                    }
                }
            </tbody>
        </table>
    </div>
    @if (total > pageSize) {
        <div class="d-flex justify-content-end mt-3">
            <ngb-pagination [(page)]="page" [pageSize]="pageSize" [collectionSize]="total" [maxSize]="5" [rotate]="true"/>
        </div>
    }
</div>
<div class="modal-footer">
    <button type="button" class="btn btn-light fw-bold" (click)="dismiss()">Close</button>
</div>
```

- [ ] **Step 3: Verify no compile errors**

Run: `cd frontend && npx ng build --configuration development 2>&1 | grep -E "ERROR|error TS" | head -20`
Expected: no error lines mentioning browse-returned-mr-modal

---

### Task 6: memorandum-receipt-reissue component + route

**Files:**
- Create: `frontend/src/app/pages/memorandum-receipt/memorandum-receipt-reissue/memorandum-receipt-reissue.component.ts`
- Create: `frontend/src/app/pages/memorandum-receipt/memorandum-receipt-reissue/memorandum-receipt-reissue.component.html`
- Modify: `frontend/src/app/pages/memorandum-receipt/memorandum-receipt.route.ts`

**Interfaces:**
- Consumes: `BrowseEntityModalComponent` (existing), `BrowseReturnedMrModalComponent` (Task 5), `MemorandumReceiptService.createReturnedMr/getDefaultSignatories` (Task 1)
- `returnMrItems[]` built from `doc.memorandumReceiptDetails[]`: each item needs `returnedQuantity` (from `d.returnedQuantity || d.quantity`), `reassignedQuantity` (already re-issued, from `d.reassignedQuantity || 0`), `maxReassignQty` = `returnedQuantity - reassignedQuantity`; only items with `maxReassignQty > 0` are shown
- Payload: `{ date, employee: {accountNo, name}, approvingOfficer: {accountNo, fullName}, returnMemorandumReceipt: {id}, memorandumReceiptDetails: [{stockWithdrawalDetail: {id}, quantity, returnedQuantity, reassignedQuantity}] }`
- On success: navigate to `'/' + menuLink` (list page)

- [ ] **Step 1: Create the TypeScript file**

```typescript
import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { MemorandumReceiptService } from '../memorandum-receipt.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseReturnedMrModalComponent } from '@/app/shared/modals/browse-returned-mr-modal/browse-returned-mr-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerTrash, tablerArrowLeft, tablerCheck } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-memorandum-receipt-reissue',
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
    templateUrl: './memorandum-receipt-reissue.component.html'
})
export class MemorandumReceiptReissueComponent {
    module    = 'Memorandum Receipt';
    subModule = 'Re-issue Returned MR';
    menuLink  = 'memorandum-receipt';

    isLoading = signal(false);
    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    date                    = '';
    employee:         any   = null;
    selectedReturnedMr: any = null;
    selectedReturnedMrCode  = '';
    returnMrItems:   any[]  = [];   // items from returned MR with maxReassignQty > 0
    reassignedItems: any[]  = [];   // items being re-issued
    approvingOfficer: any   = null;

    private service      = inject(MemorandumReceiptService);
    private modalService = inject(ModalService);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.date = new Date().toISOString().substring(0, 10);
        this.loadDefaultSignatories();
    }

    private loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (data) => { if (data?.approvedBy) this.approvingOfficer = data.approvedBy; },
            error: () => {}
        });
    }

    async openEmployeeBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.employee              = result.data;
                this.selectedReturnedMr    = null;
                this.selectedReturnedMrCode = '';
                this.returnMrItems         = [];
                this.reassignedItems       = [];
            }
        } catch { }
    }

    async openReturnedMrBrowse(): Promise<void> {
        if (!this.employee?.accountNo) {
            this.alertService.warning(this.module, 'Validation', 'Please select an employee first.');
            return;
        }
        try {
            const result = await this.modalService.openModal(
                BrowseReturnedMrModalComponent,
                { employeeAccountNo: this.employee.accountNo },
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const doc = result.data;
                this.selectedReturnedMr     = doc;
                this.selectedReturnedMrCode = doc.code || '';
                this.returnMrItems = (doc.memorandumReceiptDetails || [])
                    .map((d: any) => {
                        const returnedQty   = d.returnedQuantity || d.quantity || 0;
                        const alreadyReissued = d.reassignedQuantity || 0;
                        const maxReassign   = returnedQty - alreadyReissued;
                        return {
                            stockWithdrawalDetail: d.stockWithdrawalDetail,
                            itemCode:              d.stockWithdrawalDetail?.item?.code || d.itemCode || '',
                            itemDescription:       d.stockWithdrawalDetail?.item?.description || d.itemDescription || '',
                            unitCode:              d.stockWithdrawalDetail?.item?.unit?.code || d.unitCode || '',
                            returnedQuantity:      returnedQty,
                            reassignedQuantity:    alreadyReissued,
                            maxReassignQty:        maxReassign,
                            assigned:              false
                        };
                    })
                    .filter((item: any) => item.maxReassignQty > 0);
                this.reassignedItems = [];
            }
        } catch { }
    }

    assignItem(item: any): void {
        item.assigned = true;
        this.reassignedItems = [...this.reassignedItems, {
            stockWithdrawalDetail: item.stockWithdrawalDetail,
            itemCode:              item.itemCode,
            itemDescription:       item.itemDescription,
            unitCode:              item.unitCode,
            returnedQuantity:      item.returnedQuantity,
            reassignedQuantity:    item.maxReassignQty,   // default to full available amount
            maxReassignQty:        item.maxReassignQty
        }];
    }

    removeItem(idx: number, swDetailId: number): void {
        this.reassignedItems = this.reassignedItems.filter((_, i) => i !== idx);
        const item = this.returnMrItems.find(r => r.stockWithdrawalDetail?.id === swDetailId);
        if (item) item.assigned = false;
    }

    onReassignQtyChange(detail: any, value: string): void {
        let qty = parseFloat(value) || 0;
        if (qty <= 0) qty = 1;
        if (qty > detail.maxReassignQty) qty = detail.maxReassignQty;
        detail.reassignedQuantity = qty;
    }

    async openApprovingOfficerBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const e = result.data;
                this.approvingOfficer = { accountNo: e.accountNo, fullName: e.fullName || e.name || '' };
            }
        } catch { }
    }

    save(): void {
        if (!this.date)
            { this.alertService.warning(this.module, 'Validation', 'Date is required.'); return; }
        if (!this.employee?.accountNo)
            { this.alertService.warning(this.module, 'Validation', 'Employee is required.'); return; }
        if (!this.selectedReturnedMr?.id)
            { this.alertService.warning(this.module, 'Validation', 'Returned MR is required.'); return; }
        if (this.reassignedItems.length === 0)
            { this.alertService.warning(this.module, 'Validation', 'Please assign at least one item.'); return; }
        if (!this.approvingOfficer?.accountNo)
            { this.alertService.warning(this.module, 'Validation', 'Noted By is required.'); return; }

        this.isLoading.set(true);
        const payload = {
            date:                     this.date,
            employee:                 { accountNo: this.employee.accountNo, name: this.employee.name || this.employee.fullName || '' },
            approvingOfficer:         { accountNo: this.approvingOfficer.accountNo, fullName: this.approvingOfficer.fullName || this.approvingOfficer.name || '' },
            returnMemorandumReceipt:  { id: this.selectedReturnedMr.id },
            memorandumReceiptDetails: this.reassignedItems.map(d => ({
                stockWithdrawalDetail: { id: d.stockWithdrawalDetail?.id },
                quantity:              d.returnedQuantity,
                returnedQuantity:      d.returnedQuantity,
                reassignedQuantity:    d.reassignedQuantity
            }))
        };

        this.service.createReturnedMr(payload).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.success === false) {
                    this.alertService.error(this.module, 'Save', data.failureMessage || '');
                } else {
                    this.alertService.success(this.module, 'Re-issued successfully.', '');
                    this.router.navigate(['/' + this.menuLink]);
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

- [ ] **Step 2: Create the HTML file**

```html
<div class="container-fluid">
    <app-page-title [title]="module" [subTitle]="subModule" [menuLink]="menuLink"/>
</div>
<div class="container-fluid">
    <form (ngSubmit)="save()" #f="ngForm">
        <div class="row g-3">
            <div class="col-xl-12">
                <app-ui-card title="Re-issue Returned Memorandum Receipt">
                    <div class="col-xl-12 p-3" card-body>
                        <div class="row g-3">
                            <div class="col-md-3">
                                <label class="form-label fw-bold">Date <span class="text-danger">*</span></label>
                                <input type="text" mwlFlatpickr [options]="flatpickrOptions"
                                       class="form-control" [(ngModel)]="date"
                                       name="date" placeholder="Select date" required/>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label fw-bold">Employee <span class="text-danger">*</span></label>
                                <div class="input-group">
                                    <input type="text" class="form-control"
                                           [value]="employee?.name || employee?.fullName || ''" readonly
                                           placeholder="Browse employee..."/>
                                    <button type="button" class="btn btn-success fw-bold" (click)="openEmployeeBrowse()">
                                        <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                                    </button>
                                </div>
                            </div>
                            <div class="col-md-5">
                                <label class="form-label fw-bold">Returned MR <span class="text-danger">*</span></label>
                                <div class="input-group">
                                    <input type="text" class="form-control" [value]="selectedReturnedMrCode" readonly
                                           placeholder="Browse returned MR..."/>
                                    <button type="button" class="btn btn-success fw-bold"
                                            [disabled]="!employee?.accountNo" (click)="openReturnedMrBrowse()">
                                        <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                                    </button>
                                </div>
                            </div>
                        </div>

                        @if (returnMrItems.length > 0) {
                        <hr class="my-3"/>
                        <div class="fs-xs text-uppercase fw-semibold text-muted mb-2">Items Available to Re-issue</div>
                        <div class="table-responsive">
                            <table class="table table-custom table-centered w-100 mb-0">
                                <thead class="bg-light bg-opacity-25 thead-sm">
                                    <tr class="text-uppercase fs-xxs">
                                        <th>#</th>
                                        <th>Code</th>
                                        <th>Description</th>
                                        <th class="text-center">Returned Qty</th>
                                        <th class="text-center">Available</th>
                                        <th></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    @for (item of returnMrItems; track $index; let i = $index) {
                                        <tr>
                                            <td class="text-center">{{ i + 1 }}</td>
                                            <td class="fw-bold">{{ item.itemCode }}</td>
                                            <td>{{ item.itemDescription }}</td>
                                            <td class="text-center">{{ item.returnedQuantity | number:'1.0-3' }}</td>
                                            <td class="text-center">{{ item.maxReassignQty | number:'1.0-3' }}</td>
                                            <td class="text-center">
                                                <button type="button" class="btn btn-primary btn-sm fw-bold"
                                                        [disabled]="item.assigned" (click)="assignItem(item)">
                                                    Assign
                                                </button>
                                            </td>
                                        </tr>
                                    }
                                </tbody>
                            </table>
                        </div>
                        }

                        @if (reassignedItems.length > 0) {
                        <hr class="my-3"/>
                        <div class="fs-xs text-uppercase fw-semibold text-muted mb-2">Items to Re-issue</div>
                        <div class="table-responsive">
                            <table class="table table-custom table-centered w-100 mb-0">
                                <thead class="bg-light bg-opacity-25 thead-sm">
                                    <tr class="text-uppercase fs-xxs">
                                        <th>#</th>
                                        <th>Code</th>
                                        <th>Description</th>
                                        <th class="text-end" style="width:130px">Re-issue Qty</th>
                                        <th></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    @for (detail of reassignedItems; track $index; let i = $index) {
                                        <tr>
                                            <td class="text-center">{{ i + 1 }}</td>
                                            <td class="fw-bold">{{ detail.itemCode }}</td>
                                            <td>{{ detail.itemDescription }}</td>
                                            <td class="text-end">
                                                <input type="number" class="form-control form-control-sm text-end"
                                                       [name]="'rqty_' + $index"
                                                       [value]="detail.reassignedQuantity"
                                                       (change)="onReassignQtyChange(detail, $any($event.target).value)"
                                                       min="1" [max]="detail.maxReassignQty" step="1"/>
                                            </td>
                                            <td class="text-center">
                                                <button type="button" class="btn btn-danger btn-icon btn-sm rounded-circle"
                                                        (click)="removeItem(i, detail.stockWithdrawalDetail?.id)">
                                                    <ng-icon name="tablerTrash"></ng-icon>
                                                </button>
                                            </td>
                                        </tr>
                                    }
                                </tbody>
                            </table>
                        </div>
                        }

                        <hr class="my-3"/>
                        <div class="fs-xs text-uppercase fw-semibold text-muted mb-2">Signatories</div>
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label fw-bold">Noted By <span class="text-danger">*</span></label>
                                <div class="input-group">
                                    <input type="text" class="form-control"
                                           [value]="approvingOfficer?.fullName || approvingOfficer?.name || ''" readonly
                                           placeholder="Browse approving officer..."/>
                                    <button type="button" class="btn btn-success fw-bold" (click)="openApprovingOfficerBrowse()">
                                        <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                                    </button>
                                </div>
                            </div>
                        </div>

                        <div class="d-flex gap-2 justify-content-end align-items-center pt-3 border-top mt-3">
                            <a [routerLink]="['/' + menuLink]" class="btn btn-light fw-bold">
                                <ng-icon name="tablerArrowLeft" class="ps-0 pe-3 fw-bold"></ng-icon>Back
                            </a>
                            <button type="submit" class="btn btn-primary fw-bold" [disabled]="isLoading()">
                                <ng-icon name="tablerCheck" class="ps-0 pe-3 fw-bold"></ng-icon>
                                Re-issue
                            </button>
                        </div>
                    </div>
                </app-ui-card>
            </div>
        </div>
    </form>
</div>
```

- [ ] **Step 3: Add the `reissue` route**

In `frontend/src/app/pages/memorandum-receipt/memorandum-receipt.route.ts`, after the `create-multiple` route added in Task 4 (before the final `];`), add:

```typescript
{
    path: 'reissue',
    loadComponent: () => import('./memorandum-receipt-reissue/memorandum-receipt-reissue.component').then(m => m.MemorandumReceiptReissueComponent),
    data: { title: 'Re-issue Returned MR', mainPath }
},
```

- [ ] **Step 4: Verify no compile errors**

Run: `cd frontend && npx ng build --configuration development 2>&1 | grep -E "ERROR|error TS" | head -20`
Expected: no error lines

---
