# PCV Migration Alignment — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Align the Firefly V2 PCV module with old Firefly functionality — batch management, budget balances, workflow actions, print, cash flow section, and a new closeout page.

**Architecture:** Five targeted modifications to existing components plus two new files (pcv-closeout component). Service additions first (all components depend on them), then components in dependency order. Each component is self-contained.

**Tech Stack:** Angular 19, TypeScript, NgbModal (ng-template inline), SweetAlert2, mwlFlatpickr, ModalService (shared), `app-ui-card`, Bootstrap 5 classes.

## Global Constraints

- All date inputs must use `mwlFlatpickr` with `{ dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' }`
- All confirmations use `Swal.fire(...)` — no native `confirm()`
- Browse inputs use `input-group` pattern with `btn-success fw-bold` Browse button
- Signatory browse uses `BrowseEntityModalComponent` via `ModalService.openModal()`
- Button colors: primary=save/confirm, success=search/browse/print, danger=delete/close/replenish, light=back/cancel
- All buttons have `fw-bold`
- Save/Cancel footer follows CLAUDE.md pattern: `d-flex gap-2 justify-content-end`, Back on left, Save on right
- Tables: `table table-custom table-centered table-select table-hover w-100 mb-0` / thead `bg-light align-middle bg-opacity-25 thead-sm` / tr `text-uppercase fs-xxs`
- Working directory for build: `frontend/` — run `npx ng build --configuration=development 2>&1 | tail -5`
- No new shared components — PCV-specific modals are inline `ng-template` + `NgbModal`

---

### Task 1: Service additions (`pcv.service.ts`)

**Files:**
- Modify: `frontend/src/app/pages/pcv/pcv.service.ts`

**Interfaces:**
- Produces: `getBatches()`, `createBatch()`, `closeBatch(batchId)`, `getCheckVouchers(from,to,code)`, `replenish(payload)`, `getBudgetBalances(id)`, `getCashFlowItems()`, `getCashFlowBalances(id)`, `saveCashFlowItems(payload)`, `print(id)`, `getOffices()`, `getDocumentStatuses()`, `getCloseoutVouchers(params)`, `printCloseout(params)` — all consumed by Tasks 2–5.

- [ ] **Step 1: Add batch, replenish, budget balance, cash flow, print, and closeout methods**

Append the following methods inside the `PcvService` class (after the existing `getLogs` method):

```typescript
getBatches(): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_URL}/pcv/batches`);
}

createBatch(): Observable<any> {
    return this.http.post(`${BASE_URL}/pcv/batch/create`, {}, httpOptions);
}

closeBatch(batchId: number): Observable<any> {
    return this.http.post(`${BASE_URL}/pcv/batch/close`, { id: batchId }, httpOptions);
}

getCheckVouchers(from: string, to: string, code: string): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_URL}/pcv/check-vouchers`, { params: { from, to, code } });
}

replenish(payload: any): Observable<any> {
    return this.http.post(`${BASE_URL}/pcv/replenish`, payload, httpOptions);
}

getBudgetBalances(budgetLineItemDetailId: number): Observable<any> {
    return this.http.get(`${BASE_URL}/pcv/budget-balances/${budgetLineItemDetailId}`);
}

getCashFlowItems(): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_URL}/json/cashflow-items`);
}

getCashFlowBalances(budgetLineItemDetailId: number): Observable<any> {
    return this.http.get(`${BASE_URL}/pcv/cashflow-balances/${budgetLineItemDetailId}`);
}

saveCashFlowItems(payload: any): Observable<any> {
    return this.http.post(`${BASE_URL}/pcv/cashflow-items/save`, payload, httpOptions);
}

print(id: number): void {
    window.open(`${BASE_URL}/pcv/print/${id}`, '_blank');
}

getOffices(): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_URL}/json/offices`);
}

getDocumentStatuses(): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_URL}/json/document-statuses`);
}

getCloseoutVouchers(params: any): Observable<any[]> {
    return this.http.get<any[]>(`${BASE_URL}/pcv/closeout-vouchers`, { params });
}

printCloseout(params: any): void {
    const query = new URLSearchParams(params).toString();
    window.open(`${BASE_URL}/pcv/print-summary?${query}`, '_blank');
}
```

- [ ] **Step 2: Verify build**

```bash
cd frontend && npx ng build --configuration=development 2>&1 | tail -5
```
Expected: no TypeScript errors in `pcv.service.ts`.

- [ ] **Step 3: Commit**

```bash
git add frontend/src/app/pages/pcv/pcv.service.ts
git commit -m "feat(pcv): add batch, budget balance, cashflow, closeout, print service methods"
```

---

### Task 2: `pcv-main` — batch management toolbar + replenish modal

**Files:**
- Modify: `frontend/src/app/pages/pcv/pcv-main/pcv-main.component.ts`
- Modify: `frontend/src/app/pages/pcv/pcv-main/pcv-main.component.html`

**Interfaces:**
- Consumes: `service.getBatches()`, `service.createBatch()`, `service.closeBatch()`, `service.getCheckVouchers()`, `service.replenish()` from Task 1
- Produces: nothing consumed by other tasks

- [ ] **Step 1: Update `pcv-main.component.ts`**

Replace the entire file with this updated version:

```typescript
import { Component, TemplateRef, ViewChild, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { AlertService } from '@/app/shared/services/alert.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { PcvService } from '../pcv.service';
import Swal from 'sweetalert2';

@Component({
    selector: 'app-pcv-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './pcv-main.component.html'
})
export class PcvMainComponent {
    module    = 'Petty Cash Voucher';
    subModule = '';
    menuLink  = 'pcv';

    records   = signal<any[]>([]);
    isLoading = signal(false);

    searchQuery  = '';
    statusFilter = '';
    page         = 1;
    pageSize     = 10;

    // Batch management
    batches       : any[]    = [];
    selectedBatch : any      = null;
    activeOnly               = false;

    // Replenish modal state
    @ViewChild('replenishModal') replenishModalRef!: TemplateRef<any>;
    cvs               : any[]    = [];
    cvsLoading                   = false;
    selectedCv        : any      = null;
    replenishFrom                = '';
    replenishTo                  = '';
    replenishCode                = '';
    replenishProcessing          = false;

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    private service      = inject(PcvService);
    private alertService = inject(AlertService);
    private ngbModal     = inject(NgbModal);

    get filteredBatches(): any[] {
        if (!this.activeOnly) return this.batches;
        const s = (b: any) => (b.status || '').toLowerCase();
        return this.batches.filter(b => s(b).includes('open') || s(b).includes('active'));
    }

    get filteredRecords(): any[] {
        const q = this.searchQuery.toLowerCase();
        return this.records().filter(r => {
            const matchQuery = !q ||
                (r.code   || '').toLowerCase().includes(q) ||
                (r.payee  || '').toLowerCase().includes(q) ||
                (r.particulars || '').toLowerCase().includes(q);
            const matchStatus = !this.statusFilter ||
                String(r.documentStatusId) === this.statusFilter ||
                (r.status || r.documentStatus || '').toLowerCase().includes(this.statusFilter.toLowerCase());
            const matchBatch = !this.selectedBatch ||
                String(r.batchId || r.pettyCashBatch?.id) === String(this.selectedBatch.id);
            return matchQuery && matchStatus && matchBatch;
        });
    }

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.filteredRecords.slice(start, start + this.pageSize);
    }

    ngOnInit(): void {
        this.load();
        this.loadBatches();
    }

    load(): void {
        this.isLoading.set(true);
        this.service.list().subscribe({
            next: (data) => { this.records.set(data || []); this.page = 1; this.isLoading.set(false); },
            error: () => { this.alertService.error(this.module, 'Failed to load records.', ''); this.isLoading.set(false); }
        });
    }

    onSearch(): void { this.page = 1; }

    reset(): void {
        this.searchQuery   = '';
        this.statusFilter  = '';
        this.selectedBatch = null;
        this.activeOnly    = false;
        this.page          = 1;
        this.load();
    }

    isEditable(rec: any): boolean {
        const s = (rec?.status || rec?.documentStatus?.status || '').toString();
        return s === 'Document Created' || s === 'Returned to Creator' || s === '1';
    }

    // ─── Batch management ────────────────────────────────────────────

    loadBatches(): void {
        this.service.getBatches().subscribe({
            next: (data) => { this.batches = data || []; },
            error: () => {}
        });
    }

    createBatch(): void {
        this.service.createBatch().subscribe({
            next: (res) => {
                if (res?.success) {
                    this.alertService.success(this.module, 'Batch created.', '');
                    this.loadBatches();
                } else {
                    this.alertService.error(this.module, 'Failed to create batch.', res?.failureMessage || '');
                }
            },
            error: () => this.alertService.error(this.module, 'Error creating batch.', '')
        });
    }

    async closeBatch(): Promise<void> {
        if (!this.selectedBatch) return;
        const result = await Swal.fire({
            title: 'Close Batch',
            text: `Are you sure you want to close Batch ${this.selectedBatch.id} (${this.selectedBatch.status})?`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Confirm',
            cancelButtonText: 'Cancel'
        });
        if (!result.isConfirmed) return;
        this.service.closeBatch(this.selectedBatch.id).subscribe({
            next: (res) => {
                if (res?.success) {
                    this.alertService.success(this.module, 'Batch closed.', '');
                    this.selectedBatch = null;
                    this.loadBatches();
                    this.load();
                } else {
                    this.alertService.error(this.module, 'Failed to close batch.', res?.failureMessage || '');
                }
            },
            error: () => this.alertService.error(this.module, 'Error closing batch.', '')
        });
    }

    // ─── Replenish modal ─────────────────────────────────────────────

    openReplenish(): void {
        this.replenishFrom       = '';
        this.replenishTo         = '';
        this.replenishCode       = '';
        this.cvs                 = [];
        this.selectedCv          = null;
        this.replenishProcessing = false;
        this.ngbModal.open(this.replenishModalRef, { size: 'lg', centered: true });
    }

    loadCheckVouchers(): void {
        this.cvsLoading = true;
        this.service.getCheckVouchers(this.replenishFrom, this.replenishTo, this.replenishCode).subscribe({
            next: (data) => { this.cvs = data || []; this.cvsLoading = false; },
            error: () => { this.cvsLoading = false; }
        });
    }

    selectCv(cv: any): void {
        this.selectedCv = cv;
    }

    async confirmReplenish(modal: any): Promise<void> {
        if (!this.selectedCv) return;
        this.replenishProcessing = true;
        this.service.replenish({ cvId: this.selectedCv.id, batchId: this.selectedBatch?.id }).subscribe({
            next: (res) => {
                this.replenishProcessing = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Replenished successfully.', '');
                    modal.close();
                    this.load();
                } else {
                    this.alertService.error(this.module, 'Replenish failed.', res?.failureMessage || '');
                }
            },
            error: () => { this.replenishProcessing = false; this.alertService.error(this.module, 'Error.', ''); }
        });
    }
}
```

- [ ] **Step 2: Update `pcv-main.component.html`**

Replace the entire file with this updated version:

```html
<div class="container-fluid">
    <app-page-title [title]="module" [subTitle]="subModule" [menuLink]="menuLink"/>
</div>

<div class="card">
    <div class="row">
        <div class="col-xl-12">
            <div class="card-body">

                <!-- Row 1: Search + Create -->
                <div class="row mb-2 align-items-end g-2">
                    <div class="col-md-4">
                        <label class="form-label fw-bold mb-1 fs-xs text-uppercase">Search</label>
                        <input type="text" class="form-control" [(ngModel)]="searchQuery"
                               placeholder="Code, payee, particulars..."
                               (ngModelChange)="onSearch()"/>
                    </div>
                    <div class="col-auto d-flex gap-2">
                        <button type="button" class="btn btn-success fw-bold" (click)="load()">
                            <ng-icon name="tablerSearch" class="ps-0 pe-3 fw-bold"></ng-icon>Search
                        </button>
                        <button type="button" class="btn btn-danger fw-bold" (click)="reset()">
                            <ng-icon name="tablerRefresh" class="ps-0 pe-3 fw-bold"></ng-icon>Reset
                        </button>
                    </div>
                    <div class="col-md-auto ms-auto">
                        <a class="btn btn-primary fw-bold" [routerLink]="['/' + menuLink + '/create']">
                            <ng-icon name="tablerPlus" class="ps-0 pe-3 fw-bold"></ng-icon>New PCV
                        </a>
                    </div>
                </div>

                <!-- Row 2: Batch management toolbar -->
                <div class="row mb-3 align-items-end g-2">
                    <div class="col-md-3">
                        <label class="form-label fw-bold mb-1 fs-xs text-uppercase">Batch</label>
                        <select class="form-select" [(ngModel)]="selectedBatch" (ngModelChange)="onSearch()"
                                [compareWith]="compareBatch">
                            <option [ngValue]="null">— All Batches —</option>
                            @for (b of filteredBatches; track b.id) {
                                <option [ngValue]="b">{{ b.id }} — {{ b.status }}</option>
                            }
                        </select>
                    </div>
                    <div class="col-auto d-flex align-items-center gap-2 pb-1">
                        <div class="form-check form-switch mb-0">
                            <input class="form-check-input" type="checkbox" id="activeOnly"
                                   [(ngModel)]="activeOnly" (ngModelChange)="onSearch()"/>
                            <label class="form-check-label fw-bold fs-xs" for="activeOnly">Active Only</label>
                        </div>
                    </div>
                    <div class="col-auto d-flex gap-2">
                        <button type="button" class="btn btn-primary fw-bold" (click)="createBatch()">
                            <ng-icon name="tablerPlus" class="ps-0 pe-3 fw-bold"></ng-icon>Create New Batch
                        </button>
                        <button type="button" class="btn btn-danger fw-bold"
                                [disabled]="!selectedBatch"
                                (click)="closeBatch()">
                            <ng-icon name="tablerX" class="ps-0 pe-3 fw-bold"></ng-icon>Close
                        </button>
                        <button type="button" class="btn btn-success fw-bold" (click)="openReplenish()">
                            <ng-icon name="tablerCheck" class="ps-0 pe-3 fw-bold"></ng-icon>Replenish
                        </button>
                    </div>
                </div>

                <!-- Table -->
                <div class="row">
                    <div class="table-responsive">
                        <table class="table table-custom table-centered table-select table-hover w-100 mb-0">
                            <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                                <tr class="text-uppercase fs-xxs">
                                    <th>Code</th>
                                    <th>Payee</th>
                                    <th class="text-end">Amount</th>
                                    <th>Batch</th>
                                    <th>Date</th>
                                    <th>Status</th>
                                    <th></th>
                                </tr>
                            </thead>
                            <tbody>
                                @if (pagedRecords.length > 0) {
                                    @for (rec of pagedRecords; track rec.id) {
                                        <tr>
                                            <td class="fw-bold">{{ rec.code }}</td>
                                            <td>{{ rec.payee }}</td>
                                            <td class="text-end fw-bold">{{ rec.amount | number:'1.2-2' }}</td>
                                            <td>{{ rec.pettyCashBatch?.id || rec.batchId || '—' }}</td>
                                            <td>{{ rec.pettyCashDate | date:'MMM dd, yyyy' }}</td>
                                            <td>
                                                <span class="badge" [ngClass]="isEditable(rec) ? 'bg-secondary bg-opacity-25 text-dark border' : 'bg-success bg-opacity-25 text-success border'">
                                                    {{ rec.documentStatus?.status || '—' }}
                                                </span>
                                            </td>
                                            <td class="text-nowrap">
                                                <div class="d-flex justify-content-end gap-1">
                                                    @if (isEditable(rec)) {
                                                        <a class="btn btn-light btn-icon btn-sm rounded-circle"
                                                           [routerLink]="['/' + menuLink, rec.id, 'edit']"
                                                           ngbTooltip="Edit">
                                                            <ng-icon name="tablerEdit"></ng-icon>
                                                        </a>
                                                    }
                                                    <a class="btn btn-light btn-icon btn-sm rounded-circle"
                                                       [routerLink]="['/' + menuLink, rec.id, 'detail']"
                                                       ngbTooltip="View">
                                                        <ng-icon name="tablerEye"></ng-icon>
                                                    </a>
                                                </div>
                                            </td>
                                        </tr>
                                    }
                                } @else {
                                    <tr class="no-results">
                                        <td colspan="7" class="text-center text-muted py-3">
                                            @if (isLoading()) {
                                                <span class="spinner-border spinner-border-sm me-2" role="status"></span>Loading...
                                            } @else { Nothing found. }
                                        </td>
                                    </tr>
                                }
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Pagination -->
                @if (filteredRecords.length > pageSize) {
                    <div class="d-flex justify-content-center mt-3">
                        <ngb-pagination
                            [collectionSize]="filteredRecords.length"
                            [(page)]="page"
                            [pageSize]="pageSize"
                            [maxSize]="5">
                        </ngb-pagination>
                    </div>
                }

            </div>
        </div>
    </div>
</div>

<!-- Replenish Modal (ng-template) -->
<ng-template #replenishModal let-modal>
    <div class="modal-header">
        <h5 class="modal-title fw-bold">Check Voucher List</h5>
        <button type="button" class="btn-close" (click)="modal.dismiss()"></button>
    </div>
    <div class="modal-body">
        <!-- Filter row -->
        <div class="row g-2 mb-3 align-items-end">
            <div class="col-md-3">
                <label class="form-label fw-bold fs-xs text-uppercase">From</label>
                <input type="text" mwlFlatpickr [options]="flatpickrOptions"
                       class="form-control" [(ngModel)]="replenishFrom" placeholder="From date"/>
            </div>
            <div class="col-md-3">
                <label class="form-label fw-bold fs-xs text-uppercase">To</label>
                <input type="text" mwlFlatpickr [options]="flatpickrOptions"
                       class="form-control" [(ngModel)]="replenishTo" placeholder="To date"/>
            </div>
            <div class="col-md-3">
                <label class="form-label fw-bold fs-xs text-uppercase">Code</label>
                <input type="text" class="form-control" [(ngModel)]="replenishCode" placeholder="CV code"/>
            </div>
            <div class="col-md-3">
                <button type="button" class="btn btn-success fw-bold w-100" (click)="loadCheckVouchers()">
                    <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Search
                </button>
            </div>
        </div>
        <!-- CV table -->
        <div class="table-responsive">
            @if (cvsLoading) {
                <div class="d-flex align-items-center gap-2 text-muted py-3">
                    <span class="spinner-border spinner-border-sm" role="status"></span> Loading...
                </div>
            } @else {
                <table class="table table-custom table-centered table-hover w-100 mb-0">
                    <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                        <tr class="text-uppercase fs-xxs">
                            <th>Code</th>
                            <th>Payee</th>
                            <th class="text-end">Amount</th>
                            <th>Particulars</th>
                            <th>Voucher Date</th>
                        </tr>
                    </thead>
                    <tbody>
                        @if (cvs.length > 0) {
                            @for (cv of cvs; track cv.id) {
                                <tr (click)="selectCv(cv)" style="cursor:pointer"
                                    [class.table-info]="selectedCv?.id === cv.id">
                                    <td class="fw-bold">{{ cv.code }}</td>
                                    <td>{{ cv.payee }}</td>
                                    <td class="text-end fw-bold">{{ cv.checkAmount | number:'1.2-2' }}</td>
                                    <td>{{ cv.particulars }}</td>
                                    <td>{{ cv.voucherDate | date:'MMM dd, yyyy' }}</td>
                                </tr>
                            }
                        } @else {
                            <tr><td colspan="5" class="text-center text-muted py-3">No records found.</td></tr>
                        }
                    </tbody>
                </table>
            }
        </div>
    </div>
    <div class="modal-footer d-flex justify-content-between">
        <button type="button" class="btn btn-light fw-bold" (click)="modal.dismiss()">
            <ng-icon name="tablerArrowLeft" class="ps-0 pe-3 fw-bold"></ng-icon>Cancel
        </button>
        <button type="button" class="btn btn-danger fw-bold"
                [disabled]="!selectedCv || replenishProcessing"
                (click)="confirmReplenish(modal)">
            <ng-icon name="tablerCheck" class="ps-0 pe-3 fw-bold"></ng-icon>
            {{ replenishProcessing ? 'Processing...' : 'Replenish CV' }}
        </button>
    </div>
</ng-template>
```

- [ ] **Step 3: Add missing `compareBatch` method to the TypeScript**

Inside `PcvMainComponent`, after the `isEditable` method, add:

```typescript
compareBatch(a: any, b: any): boolean {
    return a && b ? a.id === b.id : a === b;
}
```

- [ ] **Step 4: Verify build**

```bash
cd frontend && npx ng build --configuration=development 2>&1 | tail -5
```
Expected: no errors.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/app/pages/pcv/pcv-main/pcv-main.component.ts frontend/src/app/pages/pcv/pcv-main/pcv-main.component.html
git commit -m "feat(pcv): add batch management toolbar and replenish modal to main list"
```

---

### Task 3: `pcv-add-edit` — budget balance info rows + `budgetUpdate` disable state

**Files:**
- Modify: `frontend/src/app/pages/pcv/pcv-add-edit/pcv-add-edit.component.ts`
- Modify: `frontend/src/app/pages/pcv/pcv-add-edit/pcv-add-edit.component.html`

**Interfaces:**
- Consumes: `service.getBudgetBalances(id)` from Task 1
- Produces: nothing consumed by other tasks

- [ ] **Step 1: Add budget balance properties and `budgetUpdate` flag to the TypeScript**

In `pcv-add-edit.component.ts`, after the `budgetLineItems` line add:

```typescript
budgetUpdate      = false;
budgetBalancePCL  : number | null = null;
budgetBalancePOJO : number | null = null;
budgetBalanceCV   : number | null = null;
```

- [ ] **Step 2: Set `budgetUpdate` when loading for edit**

In `loadForEdit()`, after `this.budgetLineItemDetail = data.budgetLineItemDetail || null;` add:

```typescript
this.budgetUpdate = data.budgetUpdate === true;
if (this.budgetLineItemDetail?.id) {
    this.loadBudgetBalances();
}
```

- [ ] **Step 3: Add `loadBudgetBalances()` method and update `clearBudgetLineItem()`**

After the `clearBudgetLineItem()` method, add:

```typescript
loadBudgetBalances(): void {
    if (!this.budgetLineItemDetail?.id) return;
    this.service.getBudgetBalances(this.budgetLineItemDetail.id).subscribe({
        next: (data) => {
            this.budgetBalancePCL  = data?.budgetAmountBalancePCL        ?? null;
            this.budgetBalancePOJO = data?.budgetLineItemBalancePOJORFP  ?? null;
            this.budgetBalanceCV   = data?.budgetLineItemBalanceCV        ?? null;
        },
        error: () => {}
    });
}
```

Replace `clearBudgetLineItem()` with:

```typescript
clearBudgetLineItem(): void {
    this.budgetLineItemDetail = null;
    this.budgetBalancePCL     = null;
    this.budgetBalancePOJO    = null;
    this.budgetBalanceCV      = null;
}
```

- [ ] **Step 4: Call `loadBudgetBalances()` after BLI browse selection**

In `openBudgetLineItemBrowse()`, after `this.budgetLineItemDetail = result.data;` add:

```typescript
this.loadBudgetBalances();
```

- [ ] **Step 5: Update the HTML — budget balance rows**

In `pcv-add-edit.component.html`, after the closing `</div>` of the Budget Line Item `col-md-12` block (after the Browse/Clear buttons), add:

```html
<!-- Budget balance info (shown when BLI selected) -->
@if (budgetLineItemDetail) {
    <div class="col-md-12">
        <div class="alert alert-info py-2 mb-0">
            <div class="row g-2">
                <div class="col-md-4">
                    <div class="fw-bold fs-xs text-uppercase text-muted mb-1">Budget Amount Balance PCL</div>
                    <div class="fw-bold">{{ budgetBalancePCL | number:'1.2-2' }}</div>
                </div>
                <div class="col-md-4">
                    <div class="fw-bold fs-xs text-uppercase text-muted mb-1">Amount Balance (PO/JO/RFP/CA/PCV)</div>
                    <div class="fw-bold">{{ budgetBalancePOJO | number:'1.2-2' }}</div>
                </div>
                <div class="col-md-4">
                    <div class="fw-bold fs-xs text-uppercase text-muted mb-1">Amount Balance (PCV/CV)</div>
                    <div class="fw-bold">{{ budgetBalanceCV | number:'1.2-2' }}</div>
                </div>
            </div>
        </div>
    </div>
}
```

- [ ] **Step 6: Update the HTML — `budgetUpdate` disable states**

Apply `[disabled]="budgetUpdate"` to the following inputs in the template:

1. Voucher Date input: change `required` line to include `[disabled]="budgetUpdate"`
2. Request select: add `[disabled]="budgetUpdate"`
3. Payee input: add `[disabled]="budgetUpdate"`
4. Budget Line Item Browse button: add `[disabled]="budgetUpdate"`
5. "Add Row" button in petty cash items: add `[disabled]="budgetUpdate"`
6. Each `textarea` (remarks) in petty cash items `@for` loop: add `[disabled]="budgetUpdate"`
7. Each amount `input` in petty cash items: add `[disabled]="budgetUpdate"`
8. Remove row button in petty cash items: add `[disabled]="budgetUpdate || details.length === 1"`
9. All three signatory Browse buttons: add `[disabled]="budgetUpdate"`

- [ ] **Step 7: Verify build**

```bash
cd frontend && npx ng build --configuration=development 2>&1 | tail -5
```

- [ ] **Step 8: Commit**

```bash
git add frontend/src/app/pages/pcv/pcv-add-edit/pcv-add-edit.component.ts frontend/src/app/pages/pcv/pcv-add-edit/pcv-add-edit.component.html
git commit -m "feat(pcv): add budget balance info rows and budgetUpdate disable state to add-edit"
```

---

### Task 4: `pcv-detail` — workflow actions, print, cash flow, budget balance, checked by

**Files:**
- Modify: `frontend/src/app/pages/pcv/pcv-detail/pcv-detail.component.ts`
- Modify: `frontend/src/app/pages/pcv/pcv-detail/pcv-detail.component.html`

**Interfaces:**
- Consumes: `service.getWorkflowActions()`, `service.process()` (existing), `service.print()`, `service.getCashFlowItems()`, `service.getCashFlowBalances()`, `service.saveCashFlowItems()` from Task 1

- [ ] **Step 1: Replace `pcv-detail.component.ts`**

```typescript
import { Component, TemplateRef, ViewChild, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { PcvService } from '../pcv.service';

@Component({
    selector: 'app-pcv-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './pcv-detail.component.html'
})
export class PcvDetailComponent {
    module    = 'Petty Cash Voucher';
    subModule = 'Detail';
    menuLink  = 'pcv';

    id: any   = 0;
    data: any = {};
    items     = signal<any[]>([]);
    isLoading = signal(false);

    // Workflow
    workflowActions : any[]  = [];
    selectedAction  : any    = null;
    remarks                  = '';
    formSubmit               = false;

    // Logs
    showLogs    = false;
    logs        = signal<any[]>([]);
    logsLoading = signal(false);

    // Cash flow
    @ViewChild('cashFlowBrowseModal') cashFlowBrowseModalRef!: TemplateRef<any>;
    cashFlowDetails     : any[]         = [];
    cashFlowItems       : any[]         = [];
    cashFlowBalancePOJO : number | null = null;
    cashFlowBalanceCV   : number | null = null;

    private service      = inject(PcvService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);
    private ngbModal     = inject(NgbModal);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.id = params.get('id');
            if (this.id && /^\d+$/.test(String(this.id))) {
                this.loadData();
            }
        });
    }

    loadData(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.data = data;
                    this.items.set(data.pettyCashTransDetails || []);
                    this.loadWorkflowActions();
                    this.loadCashFlowData();
                } else {
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Error', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    get totalAmount(): number {
        return this.items().reduce((s, i) => s + (Number(i.amount) || 0), 0);
    }

    // ─── Workflow actions ─────────────────────────────────────────────

    loadWorkflowActions(): void {
        const transId = this.data?.transId || this.data?.transaction?.id;
        if (!transId) return;
        this.service.getWorkflowActions(transId).subscribe({
            next: (actions) => { this.workflowActions = actions || []; this.selectedAction = null; this.remarks = ''; },
            error: () => { this.workflowActions = []; }
        });
    }

    processWorkflow(): void {
        if (!this.selectedAction) return;
        this.formSubmit = true;
        const transId = this.data?.transId || this.data?.transaction?.id;
        const payload = {
            documentId:         this.data.id,
            transId:            transId,
            workflowActionsDto: this.selectedAction,
            remarks:            this.remarks || ''
        };
        this.service.process(payload).subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Processed.', '');
                    this.loadData();
                } else {
                    this.alertService.error(this.module, 'Process failed.', res?.failureMessage || '');
                }
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'Process Error', ''); }
        });
    }

    // ─── Print ────────────────────────────────────────────────────────

    print(): void {
        this.service.print(this.id);
    }

    // ─── Logs ─────────────────────────────────────────────────────────

    toggleLogs(): void {
        this.showLogs = !this.showLogs;
        if (this.showLogs && this.logs().length === 0) {
            this.loadLogs();
        }
    }

    loadLogs(): void {
        const transId = this.data?.transaction?.id;
        if (!transId) return;
        this.logsLoading.set(true);
        this.service.getLogs(transId).subscribe({
            next: (logs) => { this.logs.set(Array.isArray(logs) ? logs : []); this.logsLoading.set(false); },
            error: () => this.logsLoading.set(false)
        });
    }

    parseLogValue(newValue: string): any {
        try { return JSON.parse(newValue); } catch { return {}; }
    }

    // ─── Cash flow ────────────────────────────────────────────────────

    loadCashFlowData(): void {
        this.cashFlowDetails = this.data?.cashFlowDetails || this.data?.budgetDetails || [];
        const budgetLineItemDetailId = this.data?.budgetLineItemDetail?.id;
        if (this.data?.isDocumentForCashFlowItemAssignment && budgetLineItemDetailId) {
            this.loadCashFlowBalances(budgetLineItemDetailId);
            this.loadCashFlowItems();
        }
    }

    loadCashFlowItems(): void {
        this.service.getCashFlowItems().subscribe({
            next: (data) => { this.cashFlowItems = data || []; },
            error: () => {}
        });
    }

    loadCashFlowBalances(budgetLineItemDetailId: number): void {
        this.service.getCashFlowBalances(budgetLineItemDetailId).subscribe({
            next: (data) => {
                this.cashFlowBalancePOJO = data?.cashFlowAmountBalancePOJO ?? null;
                this.cashFlowBalanceCV   = data?.cashFlowAmountBalanceCV   ?? null;
            },
            error: () => {}
        });
    }

    get cashFlowTotal(): number {
        return this.cashFlowDetails.reduce((s, d) => s + (Number(d.amount) || 0), 0);
    }

    openCashFlowBrowse(): void {
        this.ngbModal.open(this.cashFlowBrowseModalRef, { size: 'lg', centered: true });
    }

    addCashFlowItem(item: any, modal: any): void {
        this.cashFlowDetails.push({ cashflowItem: item, amount: 0 });
        modal.close();
    }

    removeCashFlowRow(index: number): void {
        this.cashFlowDetails.splice(index, 1);
    }

    saveCashFlowItems(): void {
        this.formSubmit = true;
        this.service.saveCashFlowItems({ pcvId: this.data.id, cashFlowDetails: this.cashFlowDetails }).subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Cash flow items saved.', '');
                    this.loadData();
                } else {
                    this.alertService.error(this.module, 'Save failed.', res?.failureMessage || '');
                }
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'Error.', ''); }
        });
    }
}
```

- [ ] **Step 2: Replace `pcv-detail.component.html`**

```html
<div class="container-fluid">
    <app-page-title [title]="module" [subTitle]="subModule" [menuLink]="menuLink"/>
</div>

<div class="container-fluid">
    @if (isLoading()) {
        <div class="d-flex align-items-center gap-2 text-muted py-4">
            <span class="spinner-border spinner-border-sm" role="status"></span> Loading...
        </div>
    } @else {
    <div class="row g-3">

        <!-- PCV Details -->
        <div class="col-xl-12">
            <app-ui-card title="PCV Details">
                <div class="col-xl-12 p-3" card-body>
                    <div class="d-flex justify-content-between align-items-center bg-light rounded px-3 py-2 mb-3 border">
                        <span class="fw-bold">{{ data.code || '—' }}</span>
                        <span>{{ data.payee || '—' }}</span>
                    </div>
                    <div class="row g-3">
                        <div class="col-md-3">
                            <div class="fw-bold fs-xs text-uppercase text-muted mb-1">Status</div>
                            <span class="badge bg-secondary bg-opacity-25 text-dark border">
                                {{ data.documentStatus?.status || '—' }}
                            </span>
                        </div>
                        <div class="col-md-3">
                            <div class="fw-bold fs-xs text-uppercase text-muted mb-1">Date</div>
                            <div>{{ data.pettyCashDate | date:'MMM dd, yyyy' }}</div>
                        </div>
                        <div class="col-md-3">
                            <div class="fw-bold fs-xs text-uppercase text-muted mb-1">Request</div>
                            <div>{{ data.request || '—' }}</div>
                        </div>
                        <div class="col-md-3">
                            <div class="fw-bold fs-xs text-uppercase text-muted mb-1">PCF</div>
                            <div>{{ data.pettyCashFund?.description || '—' }}</div>
                        </div>
                        <div class="col-md-3">
                            <div class="fw-bold fs-xs text-uppercase text-muted mb-1">PCF Balance</div>
                            <div>{{ data.pettyCashFund?.balance | number:'1.2-2' }}</div>
                        </div>
                        <div class="col-md-3">
                            <div class="fw-bold fs-xs text-uppercase text-muted mb-1">Budget Line Item Detail</div>
                            <div>{{ data.budgetLineItemDetail?.code || '—' }}</div>
                        </div>
                        <div class="col-md-3">
                            <div class="fw-bold fs-xs text-uppercase text-muted mb-1">Budget Balance</div>
                            <div class="fw-bold">{{ data.budgetAmountBalance | number:'1.2-2' }}</div>
                        </div>
                        <div class="col-md-3">
                            <div class="fw-bold fs-xs text-uppercase text-muted mb-1">Amount</div>
                            <div class="fw-bold">{{ data.amount | number:'1.2-2' }}</div>
                        </div>
                        <div class="col-md-3">
                            <div class="fw-bold fs-xs text-uppercase text-muted mb-1">Requested By</div>
                            <div>{{ data.createdByUser?.fullName || '—' }}</div>
                        </div>
                        <div class="col-md-3">
                            <div class="fw-bold fs-xs text-uppercase text-muted mb-1">Approved By</div>
                            <div>{{ data.approvedByUser?.fullName || '—' }}</div>
                        </div>
                        <div class="col-md-3">
                            <div class="fw-bold fs-xs text-uppercase text-muted mb-1">Checked By</div>
                            <div>{{ data.checker?.name || data.checker?.fullName || '—' }}</div>
                        </div>
                        <div class="col-md-3">
                            <div class="fw-bold fs-xs text-uppercase text-muted mb-1">Released By</div>
                            <div>{{ data.releasedByUser?.fullName || '—' }}</div>
                        </div>
                        <div class="col-md-6">
                            <div class="fw-bold fs-xs text-uppercase text-muted mb-1">Last Updated</div>
                            <div>{{ data.updatedAt | date:'MMM dd, yyyy HH:mm a' }}</div>
                        </div>
                    </div>
                </div>
            </app-ui-card>
        </div>

        <!-- Petty Cash Items -->
        <div class="col-xl-12">
            <app-ui-card title="Petty Cash Items">
                <div class="col-xl-12 p-3" card-body>
                    <div class="table-responsive">
                        <table class="table table-custom table-centered table-select table-hover w-100 mb-0">
                            <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                                <tr class="text-uppercase fs-xxs">
                                    <th>Particulars</th>
                                    <th class="text-end">Amount</th>
                                </tr>
                            </thead>
                            <tbody>
                                @if (items().length > 0) {
                                    @for (item of items(); track $index) {
                                        <tr>
                                            <td>{{ item.remarks }}</td>
                                            <td class="text-end">{{ item.amount | number:'1.2-2' }}</td>
                                        </tr>
                                    }
                                    <tr class="fw-bold">
                                        <td></td>
                                        <td class="text-end">{{ totalAmount | number:'1.2-2' }}</td>
                                    </tr>
                                } @else {
                                    <tr class="no-results">
                                        <td colspan="2" class="text-center text-muted py-3">No items found.</td>
                                    </tr>
                                }
                            </tbody>
                        </table>
                    </div>
                </div>
            </app-ui-card>
        </div>

        <!-- Cash Flow -->
        <div class="col-xl-12">
            <app-ui-card title="Cash Flow">
                <div class="col-xl-12 p-3" card-body>
                    @if (data.isDocumentForCashFlowItemAssignment) {
                        <!-- Assignment mode -->
                        <div class="d-flex justify-content-end mb-2">
                            <button type="button" class="btn btn-success fw-bold btn-sm" (click)="openCashFlowBrowse()">
                                <ng-icon name="tablerPlus" class="ps-0 pe-2 fw-bold"></ng-icon>Add
                            </button>
                        </div>
                        <div class="table-responsive">
                            <table class="table table-custom table-centered table-hover w-100 mb-0">
                                <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                                    <tr class="text-uppercase fs-xxs">
                                        <th>Cash Flow Item</th>
                                        <th>Cash Flow Type</th>
                                        <th class="text-end">Amount</th>
                                        <th style="width:60px"></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    @if (cashFlowDetails.length > 0) {
                                        @for (d of cashFlowDetails; track $index) {
                                            <tr>
                                                <td>{{ d.cashflowItem?.name || '—' }}</td>
                                                <td>{{ d.cashflowItem?.cashflowItemType?.name || '—' }}</td>
                                                <td class="text-end">
                                                    <input type="number" class="form-control form-control-sm text-end"
                                                           [(ngModel)]="d.amount" [ngModelOptions]="{standalone:true}"
                                                           min="0" step="0.01" style="width:120px;margin-left:auto"/>
                                                </td>
                                                <td class="text-center">
                                                    <button type="button" class="btn btn-light btn-icon btn-sm rounded-circle"
                                                            (click)="removeCashFlowRow($index)">
                                                        <ng-icon name="tablerTrash"></ng-icon>
                                                    </button>
                                                </td>
                                            </tr>
                                        }
                                        <tr class="fw-bold">
                                            <td colspan="2"></td>
                                            <td class="text-end">{{ cashFlowTotal | number:'1.2-2' }}</td>
                                            <td></td>
                                        </tr>
                                    } @else {
                                        <tr><td colspan="4" class="text-center text-muted py-3">No cash flow items.</td></tr>
                                    }
                                </tbody>
                            </table>
                        </div>
                        @if (cashFlowDetails.length > 0) {
                            <div class="alert alert-info py-2 mt-2 mb-0">
                                <div class="row g-2">
                                    <div class="col-md-6">
                                        <div class="fw-bold fs-xs text-uppercase text-muted mb-1">Amount Balance (PO/JO)</div>
                                        <div class="fw-bold">{{ cashFlowBalancePOJO | number:'1.2-2' }}</div>
                                    </div>
                                    <div class="col-md-6">
                                        <div class="fw-bold fs-xs text-uppercase text-muted mb-1">Amount Balance (CV)</div>
                                        <div class="fw-bold">{{ cashFlowBalanceCV | number:'1.2-2' }}</div>
                                    </div>
                                </div>
                            </div>
                        }
                        <div class="d-flex justify-content-end mt-3">
                            <button type="button" class="btn btn-primary fw-bold"
                                    [disabled]="formSubmit"
                                    (click)="saveCashFlowItems()">
                                <ng-icon name="tablerCheck" class="ps-0 pe-3 fw-bold"></ng-icon>
                                {{ formSubmit ? 'Saving...' : 'Save Cash Flow Items' }}
                            </button>
                        </div>
                    } @else {
                        <!-- View mode -->
                        <div class="table-responsive">
                            <table class="table table-custom table-centered table-hover w-100 mb-0">
                                <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                                    <tr class="text-uppercase fs-xxs">
                                        <th>Cash Flow Item</th>
                                        <th>Parent Cash Flow Item</th>
                                        <th>Cash Flow Type</th>
                                        <th class="text-end">Amount</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    @if (cashFlowDetails.length > 0) {
                                        @for (d of cashFlowDetails; track $index) {
                                            <tr>
                                                <td>{{ d.cashflowItem?.name || '—' }}</td>
                                                <td>{{ d.parent || '—' }}</td>
                                                <td>{{ d.cashflowItem?.cashflowItemType?.name || '—' }}</td>
                                                <td class="text-end fw-bold">{{ d.amount | number:'1.2-2' }}</td>
                                            </tr>
                                        }
                                    } @else {
                                        <tr><td colspan="4" class="text-center text-muted py-3">No cash flow items.</td></tr>
                                    }
                                </tbody>
                            </table>
                        </div>
                    }
                </div>
            </app-ui-card>
        </div>

        <!-- Workflow Actions -->
        @if (workflowActions.length > 0) {
            <div class="col-xl-12">
                <app-ui-card title="Process Document">
                    <div class="col-xl-12 p-3" card-body>
                        <div class="row g-3">
                            <div class="col-md-4">
                                <label class="form-label fw-bold">Action</label>
                                <select class="form-select" [(ngModel)]="selectedAction" [ngModelOptions]="{standalone:true}">
                                    <option [ngValue]="null">— Select Action —</option>
                                    @for (a of workflowActions; track a.actionMapId) {
                                        <option [ngValue]="a">{{ a.action }}</option>
                                    }
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label fw-bold">Remarks</label>
                                <input type="text" class="form-control"
                                       [(ngModel)]="remarks" [ngModelOptions]="{standalone:true}"
                                       placeholder="Optional remarks"/>
                            </div>
                            <div class="col-md-2 d-flex align-items-end">
                                <button type="button" class="btn btn-primary fw-bold w-100"
                                        [disabled]="!selectedAction || formSubmit"
                                        (click)="processWorkflow()">
                                    {{ formSubmit ? 'Processing...' : 'Submit' }}
                                </button>
                            </div>
                        </div>
                    </div>
                </app-ui-card>
            </div>
        }

        <!-- Logs -->
        <div class="col-xl-12">
            <app-ui-card>
                <div class="col-xl-12 p-3" card-body>
                    <div class="d-flex justify-content-between align-items-center mb-2">
                        <span class="fw-semibold fs-xs text-uppercase text-muted">Logs</span>
                        <button type="button" class="btn btn-success fw-bold btn-sm" (click)="toggleLogs()">
                            <ng-icon name="tablerList" class="ps-0 pe-2 fw-bold"></ng-icon>
                            {{ showLogs ? 'Hide' : 'Show' }}
                        </button>
                    </div>
                    @if (showLogs) {
                        @if (logsLoading()) {
                            <div class="d-flex align-items-center gap-2 text-muted py-3">
                                <span class="spinner-border spinner-border-sm" role="status"></span> Loading logs...
                            </div>
                        } @else {
                            <div class="table-responsive">
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
                                        @if (logs().length > 0) {
                                            @for (log of logs(); track log.id) {
                                                @let v = parseLogValue(log.newValue);
                                                <tr>
                                                    <td class="text-nowrap align-top">{{ log.createdAt | date:'yyyy-MM-dd h:mma' }}</td>
                                                    <td class="align-top">
                                                        <div class="fs-sm">
                                                            <p class="mb-1"><span class="fw-bold">Code:</span> {{ v.code || '—' }}</p>
                                                            <p class="mb-1"><span class="fw-bold">Voucher Date:</span> {{ v.voucherDate | date:'MMM dd, yyyy' }}</p>
                                                            <p class="mb-1"><span class="fw-bold">Document Status:</span> {{ v.documentStatus || '—' }}</p>
                                                            <p class="mb-1"><span class="fw-bold">Amount:</span> {{ v.amount | number:'1.2-2' }}</p>
                                                            <p class="mb-1"><span class="fw-bold">Office:</span> {{ v.office || '—' }}</p>
                                                            <p class="mb-1"><span class="fw-bold">Created By:</span> {{ v.createdBy || '—' }}</p>
                                                            <p class="mb-1"><span class="fw-bold">Approved By:</span> {{ v.approvedBy || '—' }}</p>
                                                            <p class="mb-1"><span class="fw-bold">Checked By:</span> {{ v.checkedBy || '—' }}</p>
                                                            <p class="mb-0"><span class="fw-bold">Released By:</span> {{ v.releasedBy || '—' }}</p>
                                                            @if (v.pettyCashTransDetails?.length > 0) {
                                                                <div class="mt-2">
                                                                    <div class="fw-bold fs-xs text-uppercase text-muted mb-1">Expense Accounts</div>
                                                                    <table class="table table-sm table-bordered mb-0">
                                                                        <thead class="bg-light">
                                                                            <tr class="text-uppercase fs-xxs">
                                                                                <th>Particulars</th>
                                                                                <th class="text-end">Amount</th>
                                                                            </tr>
                                                                        </thead>
                                                                        <tbody>
                                                                            @for (d of v.pettyCashTransDetails; track $index) {
                                                                                <tr>
                                                                                    <td>{{ d.remarks || '—' }}</td>
                                                                                    <td class="text-end">{{ d.amount | number:'1.2-2' }}</td>
                                                                                </tr>
                                                                            }
                                                                        </tbody>
                                                                    </table>
                                                                </div>
                                                            }
                                                        </div>
                                                    </td>
                                                    <td class="align-top">{{ log.remarks || '—' }}</td>
                                                    <td class="align-top">{{ log.loggedBy?.fullName || '—' }}</td>
                                                </tr>
                                            }
                                        } @else {
                                            <tr class="no-results">
                                                <td colspan="4" class="text-center text-muted py-3">No logs found.</td>
                                            </tr>
                                        }
                                    </tbody>
                                </table>
                            </div>
                        }
                    }
                </div>
            </app-ui-card>
        </div>

        <!-- Footer -->
        <div class="col-xl-12">
            <div class="d-flex gap-2 justify-content-between align-items-center pt-3 border-top mt-1">
                <a [routerLink]="['/' + menuLink]" class="btn btn-light fw-bold">
                    <ng-icon name="tablerArrowLeft" class="ps-0 pe-3 fw-bold"></ng-icon>Back
                </a>
                <button type="button" class="btn btn-success fw-bold" (click)="print()">
                    <ng-icon name="tablerPrinter" class="ps-0 pe-3 fw-bold"></ng-icon>Print
                </button>
            </div>
        </div>

    </div>
    }
</div>

<!-- Cash Flow Item Browse Modal (ng-template) -->
<ng-template #cashFlowBrowseModal let-modal>
    <div class="modal-header">
        <h5 class="modal-title fw-bold">Select Cash Flow Item</h5>
        <button type="button" class="btn-close" (click)="modal.dismiss()"></button>
    </div>
    <div class="modal-body">
        <div class="table-responsive">
            <table class="table table-custom table-centered table-hover w-100 mb-0">
                <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                    <tr class="text-uppercase fs-xxs">
                        <th>Name</th>
                        <th>Type</th>
                        <th style="width:80px"></th>
                    </tr>
                </thead>
                <tbody>
                    @if (cashFlowItems.length > 0) {
                        @for (item of cashFlowItems; track item.id) {
                            <tr>
                                <td>{{ item.name }}</td>
                                <td>{{ item.cashflowItemType?.name || '—' }}</td>
                                <td>
                                    <button type="button" class="btn btn-primary btn-sm fw-bold"
                                            (click)="addCashFlowItem(item, modal)">Select</button>
                                </td>
                            </tr>
                        }
                    } @else {
                        <tr><td colspan="3" class="text-center text-muted py-3">No items found.</td></tr>
                    }
                </tbody>
            </table>
        </div>
    </div>
    <div class="modal-footer">
        <button type="button" class="btn btn-light fw-bold" (click)="modal.dismiss()">
            <ng-icon name="tablerArrowLeft" class="ps-0 pe-3 fw-bold"></ng-icon>Cancel
        </button>
    </div>
</ng-template>
```

- [ ] **Step 3: Verify build**

```bash
cd frontend && npx ng build --configuration=development 2>&1 | tail -5
```

- [ ] **Step 4: Commit**

```bash
git add frontend/src/app/pages/pcv/pcv-detail/pcv-detail.component.ts frontend/src/app/pages/pcv/pcv-detail/pcv-detail.component.html
git commit -m "feat(pcv): add workflow actions, print, cash flow section, budget balance, checked by to detail"
```

---

### Task 5: `pcv-closeout` — new standalone component

**Files:**
- Create: `frontend/src/app/pages/pcv/pcv-closeout/pcv-closeout.component.ts`
- Create: `frontend/src/app/pages/pcv/pcv-closeout/pcv-closeout.component.html`

**Interfaces:**
- Consumes: `service.getBatches()`, `service.createBatch()`, `service.closeBatch()`, `service.getCheckVouchers()`, `service.replenish()`, `service.getOffices()`, `service.getDocumentStatuses()`, `service.getCloseoutVouchers()`, `service.printCloseout()` from Task 1

- [ ] **Step 1: Create `pcv-closeout.component.ts`**

```typescript
import { Component, TemplateRef, ViewChild, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { AlertService } from '@/app/shared/services/alert.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { PcvService } from '../pcv.service';
import Swal from 'sweetalert2';

@Component({
    selector: 'app-pcv-closeout',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './pcv-closeout.component.html'
})
export class PcvCloseoutComponent {
    module    = 'Petty Cash Voucher';
    subModule = 'Closeout';
    menuLink  = 'pcv';

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // Filters
    fromDate       = '';
    toDate         = '';
    offices        : any[] = [];
    selectedOffice : any   = null;
    documentStatuses   : any[] = [];
    selectedStatus     : any   = null;

    // Records
    records   = signal<any[]>([]);
    isLoading = signal(false);

    // Batch management
    batches       : any[] = [];
    selectedBatch : any   = null;
    activeOnly            = false;

    // Officers
    checkedBy    : any = null;
    replenishedBy: any = null;

    // Replenish modal state
    @ViewChild('replenishModal') replenishModalRef!: TemplateRef<any>;
    cvs                : any[] = [];
    cvsLoading                 = false;
    selectedCv         : any   = null;
    replenishFrom              = '';
    replenishTo                = '';
    replenishCode              = '';
    replenishProcessing        = false;

    private service      = inject(PcvService);
    private alertService = inject(AlertService);
    private ngbModal     = inject(NgbModal);
    private modalService = inject(ModalService);

    ngOnInit(): void {
        this.loadBatches();
        this.loadOffices();
        this.loadDocumentStatuses();
    }

    // ─── Filters ─────────────────────────────────────────────────────

    loadOffices(): void {
        this.service.getOffices().subscribe({
            next: (data) => { this.offices = data || []; },
            error: () => {}
        });
    }

    loadDocumentStatuses(): void {
        this.service.getDocumentStatuses().subscribe({
            next: (data) => { this.documentStatuses = data || []; },
            error: () => {}
        });
    }

    search(): void {
        this.isLoading.set(true);
        const params: any = {};
        if (this.fromDate)       params['from']     = this.fromDate;
        if (this.toDate)         params['to']       = this.toDate;
        if (this.selectedOffice) params['officeId'] = this.selectedOffice.id;
        if (this.selectedStatus) params['statusId'] = this.selectedStatus.id;
        this.service.getCloseoutVouchers(params).subscribe({
            next: (data) => { this.records.set(data || []); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Failed to load records.', ''); }
        });
    }

    printSummary(): void {
        const params: any = {};
        if (this.fromDate)        params['from']             = this.fromDate;
        if (this.toDate)          params['to']               = this.toDate;
        if (this.selectedOffice)  params['officeId']         = this.selectedOffice.id;
        if (this.selectedStatus)  params['statusId']         = this.selectedStatus.id;
        if (this.checkedBy)       params['checkedById']      = this.checkedBy.accountNo;
        if (this.replenishedBy)   params['replenishedById']  = this.replenishedBy.accountNo;
        this.service.printCloseout(params);
    }

    // ─── Officers ────────────────────────────────────────────────────

    async openSignatoryBrowse(field: 'checkedBy' | 'replenishedBy'): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseEntityModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this[field] = result.data;
            }
        } catch { }
    }

    clearSignatory(field: 'checkedBy' | 'replenishedBy'): void {
        this[field] = null;
    }

    // ─── Batch management ─────────────────────────────────────────────

    loadBatches(): void {
        this.service.getBatches().subscribe({
            next: (data) => { this.batches = data || []; },
            error: () => {}
        });
    }

    get filteredBatches(): any[] {
        if (!this.activeOnly) return this.batches;
        const s = (b: any) => (b.status || '').toLowerCase();
        return this.batches.filter(b => s(b).includes('open') || s(b).includes('active'));
    }

    compareBatch(a: any, b: any): boolean {
        return a && b ? a.id === b.id : a === b;
    }

    createBatch(): void {
        this.service.createBatch().subscribe({
            next: (res) => {
                if (res?.success) {
                    this.alertService.success(this.module, 'Batch created.', '');
                    this.loadBatches();
                } else {
                    this.alertService.error(this.module, 'Failed to create batch.', res?.failureMessage || '');
                }
            },
            error: () => this.alertService.error(this.module, 'Error creating batch.', '')
        });
    }

    async closeBatch(): Promise<void> {
        if (!this.selectedBatch) return;
        const result = await Swal.fire({
            title: 'Close Batch',
            text: `Are you sure you want to close Batch ${this.selectedBatch.id} (${this.selectedBatch.status})?`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Confirm',
            cancelButtonText: 'Cancel'
        });
        if (!result.isConfirmed) return;
        this.service.closeBatch(this.selectedBatch.id).subscribe({
            next: (res) => {
                if (res?.success) {
                    this.alertService.success(this.module, 'Batch closed.', '');
                    this.selectedBatch = null;
                    this.loadBatches();
                } else {
                    this.alertService.error(this.module, 'Failed to close batch.', res?.failureMessage || '');
                }
            },
            error: () => this.alertService.error(this.module, 'Error closing batch.', '')
        });
    }

    // ─── Replenish modal ─────────────────────────────────────────────

    openReplenish(): void {
        this.replenishFrom       = '';
        this.replenishTo         = '';
        this.replenishCode       = '';
        this.cvs                 = [];
        this.selectedCv          = null;
        this.replenishProcessing = false;
        this.ngbModal.open(this.replenishModalRef, { size: 'lg', centered: true });
    }

    loadCheckVouchers(): void {
        this.cvsLoading = true;
        this.service.getCheckVouchers(this.replenishFrom, this.replenishTo, this.replenishCode).subscribe({
            next: (data) => { this.cvs = data || []; this.cvsLoading = false; },
            error: () => { this.cvsLoading = false; }
        });
    }

    selectCv(cv: any): void {
        this.selectedCv = cv;
    }

    async confirmReplenish(modal: any): Promise<void> {
        if (!this.selectedCv) return;
        this.replenishProcessing = true;
        this.service.replenish({ cvId: this.selectedCv.id, batchId: this.selectedBatch?.id }).subscribe({
            next: (res) => {
                this.replenishProcessing = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Replenished successfully.', '');
                    modal.close();
                } else {
                    this.alertService.error(this.module, 'Replenish failed.', res?.failureMessage || '');
                }
            },
            error: () => { this.replenishProcessing = false; this.alertService.error(this.module, 'Error.', ''); }
        });
    }
}
```

- [ ] **Step 2: Create `pcv-closeout.component.html`**

```html
<div class="container-fluid">
    <app-page-title [title]="module" [subTitle]="subModule" [menuLink]="menuLink"/>
</div>

<div class="container-fluid">
    <div class="row g-3">
        <div class="col-xl-12">
            <app-ui-card title="PCV Closeout / Setup Print">
                <div class="col-xl-12 p-3" card-body>

                    <!-- Filter row -->
                    <div class="fs-xs text-uppercase fw-semibold text-muted mb-2">Filters</div>
                    <div class="row g-2 mb-3 align-items-end">
                        <div class="col-md-2">
                            <label class="form-label fw-bold fs-xs text-uppercase">From</label>
                            <input type="text" mwlFlatpickr [options]="flatpickrOptions"
                                   class="form-control" [(ngModel)]="fromDate" placeholder="From date"/>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label fw-bold fs-xs text-uppercase">To</label>
                            <input type="text" mwlFlatpickr [options]="flatpickrOptions"
                                   class="form-control" [(ngModel)]="toDate" placeholder="To date"/>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label fw-bold fs-xs text-uppercase">Office</label>
                            <select class="form-select" [(ngModel)]="selectedOffice">
                                <option [ngValue]="null">— All Offices —</option>
                                @for (o of offices; track o.id) {
                                    <option [ngValue]="o">{{ o.name }}</option>
                                }
                            </select>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label fw-bold fs-xs text-uppercase">Status</label>
                            <select class="form-select" [(ngModel)]="selectedStatus">
                                <option [ngValue]="null">— All Statuses —</option>
                                @for (s of documentStatuses; track s.id) {
                                    <option [ngValue]="s">{{ s.desc || s.status }}</option>
                                }
                            </select>
                        </div>
                        <div class="col-auto d-flex gap-2">
                            <button type="button" class="btn btn-success fw-bold" (click)="search()">
                                <ng-icon name="tablerEye" class="ps-0 pe-3 fw-bold"></ng-icon>View Only
                            </button>
                            <button type="button" class="btn btn-success fw-bold" (click)="printSummary()">
                                <ng-icon name="tablerPrinter" class="ps-0 pe-3 fw-bold"></ng-icon>Print Summary
                            </button>
                        </div>
                    </div>

                    <hr/>
                    <div class="fs-xs text-uppercase fw-semibold text-muted mb-2">Batch Management</div>

                    <!-- Batch toolbar -->
                    <div class="row g-2 mb-3 align-items-end">
                        <div class="col-md-3">
                            <label class="form-label fw-bold fs-xs text-uppercase">Batch</label>
                            <select class="form-select" [(ngModel)]="selectedBatch" [compareWith]="compareBatch">
                                <option [ngValue]="null">— All Batches —</option>
                                @for (b of filteredBatches; track b.id) {
                                    <option [ngValue]="b">{{ b.id }} — {{ b.status }}</option>
                                }
                            </select>
                        </div>
                        <div class="col-auto d-flex align-items-center gap-2 pb-1">
                            <div class="form-check form-switch mb-0">
                                <input class="form-check-input" type="checkbox" id="activeOnlyCO"
                                       [(ngModel)]="activeOnly"/>
                                <label class="form-check-label fw-bold fs-xs" for="activeOnlyCO">Active Only</label>
                            </div>
                        </div>
                        <div class="col-auto d-flex gap-2">
                            <button type="button" class="btn btn-primary fw-bold" (click)="createBatch()">
                                <ng-icon name="tablerPlus" class="ps-0 pe-3 fw-bold"></ng-icon>Create New Batch
                            </button>
                            <button type="button" class="btn btn-danger fw-bold"
                                    [disabled]="!selectedBatch"
                                    (click)="closeBatch()">
                                <ng-icon name="tablerX" class="ps-0 pe-3 fw-bold"></ng-icon>Close
                            </button>
                            <button type="button" class="btn btn-success fw-bold" (click)="openReplenish()">
                                <ng-icon name="tablerCheck" class="ps-0 pe-3 fw-bold"></ng-icon>Replenish
                            </button>
                        </div>
                    </div>

                    <hr/>
                    <div class="fs-xs text-uppercase fw-semibold text-muted mb-2">Vouchers</div>

                    <!-- Table -->
                    <div class="table-responsive mb-3">
                        <table class="table table-custom table-centered table-select table-hover w-100 mb-0">
                            <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                                <tr class="text-uppercase fs-xxs">
                                    <th>Date</th>
                                    <th>Code</th>
                                    <th>Payee</th>
                                    <th>Nature of Payment</th>
                                    <th>G/L Acct.</th>
                                    <th class="text-end">Amount</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                @if (isLoading()) {
                                    <tr><td colspan="7" class="text-center text-muted py-3">
                                        <span class="spinner-border spinner-border-sm me-2" role="status"></span>Loading...
                                    </td></tr>
                                } @else if (records().length > 0) {
                                    @for (rec of records(); track rec.id) {
                                        <tr>
                                            <td>{{ rec.date || rec.pettyCashDate | date:'MMM dd, yyyy' }}</td>
                                            <td class="fw-bold">{{ rec.code }}</td>
                                            <td>{{ rec.payee }}</td>
                                            <td>{{ rec.purpose || rec.natureOfPayment || '—' }}</td>
                                            <td>{{ rec.glAccountCode || '—' }}</td>
                                            <td class="text-end fw-bold">{{ rec.amount | number:'1.2-2' }}</td>
                                            <td>
                                                <span class="badge bg-secondary bg-opacity-25 text-dark border">
                                                    {{ rec.documentStatus || rec.documentStatus?.status || '—' }}
                                                </span>
                                            </td>
                                        </tr>
                                    }
                                } @else {
                                    <tr><td colspan="7" class="text-center text-muted py-3">No records found.</td></tr>
                                }
                            </tbody>
                        </table>
                    </div>

                    <hr/>
                    <div class="fs-xs text-uppercase fw-semibold text-muted mb-2">Officers</div>

                    <!-- Officers -->
                    <div class="row g-3 mb-3">
                        <div class="col-md-4">
                            <label class="form-label fw-bold">Checked By</label>
                            <div class="input-group">
                                <input type="text" class="form-control"
                                       [value]="checkedBy ? (checkedBy.name || checkedBy.fullName || '') : ''"
                                       readonly placeholder="Browse officer..."/>
                                <button type="button" class="btn btn-success fw-bold"
                                        (click)="openSignatoryBrowse('checkedBy')">
                                    <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                                </button>
                                @if (checkedBy) {
                                    <button type="button" class="btn btn-danger fw-bold"
                                            (click)="clearSignatory('checkedBy')">
                                        <ng-icon name="tablerX" class="ps-0 pe-0 fw-bold"></ng-icon>
                                    </button>
                                }
                            </div>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label fw-bold">Replenished By</label>
                            <div class="input-group">
                                <input type="text" class="form-control"
                                       [value]="replenishedBy ? (replenishedBy.name || replenishedBy.fullName || '') : ''"
                                       readonly placeholder="Browse officer..."/>
                                <button type="button" class="btn btn-success fw-bold"
                                        (click)="openSignatoryBrowse('replenishedBy')">
                                    <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                                </button>
                                @if (replenishedBy) {
                                    <button type="button" class="btn btn-danger fw-bold"
                                            (click)="clearSignatory('replenishedBy')">
                                        <ng-icon name="tablerX" class="ps-0 pe-0 fw-bold"></ng-icon>
                                    </button>
                                }
                            </div>
                        </div>
                    </div>

                    <!-- Footer -->
                    <div class="d-flex gap-2 justify-content-end align-items-center pt-3 border-top mt-3">
                        <a [routerLink]="['/' + menuLink]" class="btn btn-light fw-bold">
                            <ng-icon name="tablerArrowLeft" class="ps-0 pe-3 fw-bold"></ng-icon>Back
                        </a>
                    </div>

                </div>
            </app-ui-card>
        </div>
    </div>
</div>

<!-- Replenish Modal (ng-template) -->
<ng-template #replenishModal let-modal>
    <div class="modal-header">
        <h5 class="modal-title fw-bold">Check Voucher List</h5>
        <button type="button" class="btn-close" (click)="modal.dismiss()"></button>
    </div>
    <div class="modal-body">
        <div class="row g-2 mb-3 align-items-end">
            <div class="col-md-3">
                <label class="form-label fw-bold fs-xs text-uppercase">From</label>
                <input type="text" mwlFlatpickr [options]="flatpickrOptions"
                       class="form-control" [(ngModel)]="replenishFrom" placeholder="From date"/>
            </div>
            <div class="col-md-3">
                <label class="form-label fw-bold fs-xs text-uppercase">To</label>
                <input type="text" mwlFlatpickr [options]="flatpickrOptions"
                       class="form-control" [(ngModel)]="replenishTo" placeholder="To date"/>
            </div>
            <div class="col-md-3">
                <label class="form-label fw-bold fs-xs text-uppercase">Code</label>
                <input type="text" class="form-control" [(ngModel)]="replenishCode" placeholder="CV code"/>
            </div>
            <div class="col-md-3">
                <button type="button" class="btn btn-success fw-bold w-100" (click)="loadCheckVouchers()">
                    <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Search
                </button>
            </div>
        </div>
        <div class="table-responsive">
            @if (cvsLoading) {
                <div class="d-flex align-items-center gap-2 text-muted py-3">
                    <span class="spinner-border spinner-border-sm" role="status"></span> Loading...
                </div>
            } @else {
                <table class="table table-custom table-centered table-hover w-100 mb-0">
                    <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                        <tr class="text-uppercase fs-xxs">
                            <th>Code</th>
                            <th>Payee</th>
                            <th class="text-end">Amount</th>
                            <th>Particulars</th>
                            <th>Voucher Date</th>
                        </tr>
                    </thead>
                    <tbody>
                        @if (cvs.length > 0) {
                            @for (cv of cvs; track cv.id) {
                                <tr (click)="selectCv(cv)" style="cursor:pointer"
                                    [class.table-info]="selectedCv?.id === cv.id">
                                    <td class="fw-bold">{{ cv.code }}</td>
                                    <td>{{ cv.payee }}</td>
                                    <td class="text-end fw-bold">{{ cv.checkAmount | number:'1.2-2' }}</td>
                                    <td>{{ cv.particulars }}</td>
                                    <td>{{ cv.voucherDate | date:'MMM dd, yyyy' }}</td>
                                </tr>
                            }
                        } @else {
                            <tr><td colspan="5" class="text-center text-muted py-3">No records found.</td></tr>
                        }
                    </tbody>
                </table>
            }
        </div>
    </div>
    <div class="modal-footer d-flex justify-content-between">
        <button type="button" class="btn btn-light fw-bold" (click)="modal.dismiss()">
            <ng-icon name="tablerArrowLeft" class="ps-0 pe-3 fw-bold"></ng-icon>Cancel
        </button>
        <button type="button" class="btn btn-danger fw-bold"
                [disabled]="!selectedCv || replenishProcessing"
                (click)="confirmReplenish(modal)">
            <ng-icon name="tablerCheck" class="ps-0 pe-3 fw-bold"></ng-icon>
            {{ replenishProcessing ? 'Processing...' : 'Replenish CV' }}
        </button>
    </div>
</ng-template>
```

- [ ] **Step 3: Verify build**

```bash
cd frontend && npx ng build --configuration=development 2>&1 | tail -5
```

- [ ] **Step 4: Commit**

```bash
git add frontend/src/app/pages/pcv/pcv-closeout/pcv-closeout.component.ts frontend/src/app/pages/pcv/pcv-closeout/pcv-closeout.component.html
git commit -m "feat(pcv): add pcv-closeout standalone component"
```

---

### Task 6: Route registration

**Files:**
- Modify: `frontend/src/app/pages/pcv/pcv.route.ts`

**Interfaces:**
- Consumes: `PcvCloseoutComponent` from Task 5

- [ ] **Step 1: Add closeout route**

In `pcv.route.ts`, add the closeout entry before the closing `]`:

```typescript
{
    path: 'closeout',
    loadComponent: () => import('./pcv-closeout/pcv-closeout.component').then(m => m.PcvCloseoutComponent),
    data: { title: 'PCV Closeout', mainPath }
},
```

The final `PCV_ROUTES` array should be:

```typescript
export const PCV_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./pcv-main/pcv-main.component').then(m => m.PcvMainComponent),
        data: { title: 'Petty Cash Voucher', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./pcv-add-edit/pcv-add-edit.component').then(m => m.PcvAddEditComponent),
        data: { title: 'Create Petty Cash Voucher', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./pcv-add-edit/pcv-add-edit.component').then(m => m.PcvAddEditComponent),
        data: { title: 'Edit Petty Cash Voucher', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./pcv-detail/pcv-detail.component').then(m => m.PcvDetailComponent),
        data: { title: 'Petty Cash Voucher Detail', mainPath }
    },
    {
        path: 'closeout',
        loadComponent: () => import('./pcv-closeout/pcv-closeout.component').then(m => m.PcvCloseoutComponent),
        data: { title: 'PCV Closeout', mainPath }
    },
];
```

- [ ] **Step 2: Final build verification**

```bash
cd frontend && npx ng build --configuration=development 2>&1 | tail -10
```
Expected: Build successful, no errors.

- [ ] **Step 3: Commit**

```bash
git add frontend/src/app/pages/pcv/pcv.route.ts
git commit -m "feat(pcv): register closeout route"
```
