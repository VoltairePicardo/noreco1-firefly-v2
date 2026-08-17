# CA Module Alignment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fully align the V2 Angular Cash Advance (CA) module with the legacy Firefly V1 AngularJS/JSP implementation by adding missing features, removing V2-only inputs that have no old counterpart, and correcting broken behavior.

**Architecture:** Five files are modified in dependency order — service first, then main, then add-edit. No new files are created. All changes are additive edits to existing standalone Angular 19 components using Signals, `@if`/`@for` control flow, and `ngModel` two-way binding.

**Tech Stack:** Angular 19 standalone components, Angular Signals (`signal<T>()`), `angularx-flatpickr` for date pickers, `@ng-icons/core` with Tabler icons, NgBootstrap (`ngb-pagination`), SweetAlert2.

## Global Constraints

- Use `signal<T>()` / `.set()` / `()` for all reactive state — no `BehaviorSubject`
- Use `@if` / `@for` control flow — no `*ngIf` / `*ngFor`
- Date inputs use `mwlFlatpickr` with `[options]="flatpickrOptions"` (`dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y'`)
- Button classes per CLAUDE.md: Search/Browse/Print = `btn-success fw-bold`, Primary action = `btn-primary fw-bold`, Cancel/Back = `btn-light fw-bold`, Destructive = `btn-danger fw-bold`
- Icon-only table buttons: `btn-light btn-icon btn-sm rounded-circle`
- All action buttons include `fw-bold`
- Empty table rows: single `<td colspan="N">` centered muted text
- BASE_URL from `environment.get('baseUrl')` — CA service uses `/cash-advance/...` prefix
- `getOffices()` and `getUserOffice()` use `/json/...` prefix (legacy endpoints, not `/cash-advance/`)
- Verify `getUnliquidatedList()` endpoint path against backend before implementing — likely `GET /cash-advance/unliquidated/list`

---

## File Map

| File | Change Type | Responsibility |
|---|---|---|
| `frontend/src/app/pages/ca/ca.service.ts` | Modify | Add `getOffices()`, `getUserOffice()`, `getUnliquidatedList()`; update `list()` to accept `officeId?` |
| `frontend/src/app/pages/ca/ca-main/ca-main.component.ts` | Modify | Add `offices` signal, `selectedOffice`, `defaultOffice`; update `loadOffices()`, `load()`, `reset()` |
| `frontend/src/app/pages/ca/ca-main/ca-main.component.html` | Modify | Add office dropdown to filter row |
| `frontend/src/app/pages/ca/ca-add-edit/ca-add-edit.component.ts` | Modify | Remove `cashAdvanceDate`; add `offices`, `selectedOffice`, `unliquidatedCAs`; update `setDefaultDates()`, `loadForEdit()`, `isValid()`, `save()`, `ngOnInit()` |
| `frontend/src/app/pages/ca/ca-add-edit/ca-add-edit.component.html` | Modify | Remove "Cash Advance Date" input; add office dropdown and unliquidated CAs warning |

---

## Task 1: Extend `ca.service.ts`

**Files:**
- Modify: `frontend/src/app/pages/ca/ca.service.ts`

**Interfaces:**
- Produces:
  - `getOffices(): Observable<any[]>`
  - `getUserOffice(): Observable<any>`
  - `getUnliquidatedList(): Observable<any[]>`
  - `list(from?, to?, statusId?, officeId?): Observable<any[]>` — updated signature

- [ ] **Step 1: Verify the unliquidated list endpoint**

  Check the backend controller for the CA module. Look for a method returning a list of unliquidated CAs. Common paths:
  - `GET /cash-advance/unliquidated/list`
  - `GET /cash-advance/unliquidated`

  Confirm the exact path before proceeding. Use it in Step 2.

- [ ] **Step 2: Add the three new service methods**

  Open `frontend/src/app/pages/ca/ca.service.ts`. Add these three methods after `getDocumentStatuses()`:

  ```typescript
  getOffices(): Observable<any[]> {
      return this.http.get<any[]>(`${BASE_URL}/json/offices/`);
  }

  getUserOffice(): Observable<any> {
      return this.http.get(`${BASE_URL}/json/office-user/`);
  }

  getUnliquidatedList(): Observable<any[]> {
      // Verify this path against the backend controller before committing
      return this.http.get<any[]>(`${BASE_URL}/cash-advance/unliquidated/list`);
  }
  ```

  > Note: `BASE_URL` already resolves to `environment.get('baseUrl')` at the top of the file. The `/json/` endpoints are legacy paths that remain unchanged from V1.

- [ ] **Step 3: Update `list()` to accept `officeId`**

  Replace the existing `list()` method:

  ```typescript
  list(from?: string, to?: string, statusId?: number, officeId?: number): Observable<any[]> {
      let params = new HttpParams();
      if (from)     params = params.set('from',     from);
      if (to)       params = params.set('to',       to);
      if (statusId) params = params.set('statusId', statusId.toString());
      if (officeId) params = params.set('officeId', officeId.toString());
      return this.http.get<any[]>(`${BASE_URL}/cash-advance/list`, { params });
  }
  ```

- [ ] **Step 4: Build check**

  ```bash
  cd frontend && ng build --configuration development 2>&1 | tail -20
  ```

  Expected: 0 errors. If errors appear, fix before continuing.

- [ ] **Step 5: Commit**

  ```bash
  git add frontend/src/app/pages/ca/ca.service.ts
  git commit -m "feat(ca): add getOffices, getUserOffice, getUnliquidatedList; pass officeId to list"
  ```

---

## Task 2: Add office filter to `ca-main`

**Files:**
- Modify: `frontend/src/app/pages/ca/ca-main/ca-main.component.ts`
- Modify: `frontend/src/app/pages/ca/ca-main/ca-main.component.html`

**Interfaces:**
- Consumes: `CaService.getOffices()`, `CaService.getUserOffice()`, `CaService.list(from, to, statusId, officeId)` from Task 1

- [ ] **Step 1: Add office state to the component class**

  Open `frontend/src/app/pages/ca/ca-main/ca-main.component.ts`.

  Add two properties after `selectedStatusId`:

  ```typescript
  offices        = signal<any[]>([]);
  selectedOffice: any = null;
  ```

- [ ] **Step 2: Replace `ngOnInit` and add office load methods**

  Replace the existing `ngOnInit()`:

  ```typescript
  ngOnInit(): void {
      this.setDefaultDates();
      this.loadDocumentStatuses();
      this.loadOffices();
  }
  ```

  Add two new methods after `loadDocumentStatuses()`:

  ```typescript
  loadOffices(): void {
      this.service.getOffices().subscribe({
          next: (data) => {
              this.offices.set(data || []);
              this.loadUserOffice();
          },
          error: () => this.load()
      });
  }

  loadUserOffice(): void {
      this.service.getUserOffice().subscribe({
          next: (data) => { this.selectedOffice = data || null; this.load(); },
          error: () => this.load()
      });
  }
  ```

- [ ] **Step 3: Update `load()` to pass officeId**

  Replace the `load()` method:

  ```typescript
  load(): void {
      this.isLoading.set(true);
      this.service.list(this.fromDate, this.toDate, this.selectedStatusId, this.selectedOffice?.id).subscribe({
          next: (data) => { this.records.set(data || []); this.page = 1; this.isLoading.set(false); },
          error: () => { this.alertService.error(this.module, 'Failed to load records.', ''); this.isLoading.set(false); }
      });
  }
  ```

- [ ] **Step 4: Update `reset()` to clear office to null**

  Replace the `reset()` method:

  ```typescript
  reset(): void {
      this.searchQuery      = '';
      this.selectedStatusId = undefined;
      this.selectedOffice   = null;
      this.setDefaultDates();
      this.page             = 1;
      this.load();
  }
  ```

  > On reset, `selectedOffice` becomes `null` (no filter applied). The `offices` signal is NOT cleared — dropdown choices remain available.

- [ ] **Step 5: Add office dropdown to the filter row in HTML**

  Open `frontend/src/app/pages/ca/ca-main/ca-main.component.html`.

  After the Document Status `<div class="col-md-2">` block, insert:

  ```html
  <div class="col-md-2">
      <label class="form-label fw-bold mb-1 fs-xs text-uppercase">Office</label>
      <select class="form-select" [(ngModel)]="selectedOffice"
              [compareWith]="compareById">
          <option [ngValue]="null">— All Offices —</option>
          @for (o of offices(); track o.id) {
              <option [ngValue]="o">{{ o.name }}</option>
          }
      </select>
  </div>
  ```

- [ ] **Step 6: Add `compareById` helper to the component class**

  Add this method to `ca-main.component.ts` (after `isEditable()`):

  ```typescript
  compareById(a: any, b: any): boolean {
      return a && b ? a.id === b.id : a === b;
  }
  ```

- [ ] **Step 7: Build check**

  ```bash
  cd frontend && ng build --configuration development 2>&1 | tail -20
  ```

  Expected: 0 errors.

- [ ] **Step 8: Manual verify**

  - Office dropdown appears in filter row, populated with offices from API
  - Default selection matches logged-in user's office
  - Clicking Search filters list by selected office
  - Clicking Reset clears office to "— All Offices —" but dropdown still has choices

- [ ] **Step 9: Commit**

  ```bash
  git add frontend/src/app/pages/ca/ca-main/ca-main.component.ts \
          frontend/src/app/pages/ca/ca-main/ca-main.component.html
  git commit -m "feat(ca): add office filter to main list"
  ```

---

## Task 3: Refactor `ca-add-edit` component class

**Files:**
- Modify: `frontend/src/app/pages/ca/ca-add-edit/ca-add-edit.component.ts`

**Interfaces:**
- Consumes: `CaService.getOffices()`, `CaService.getUserOffice()`, `CaService.getUnliquidatedList()` from Task 1
- Produces:
  - `offices = signal<any[]>([])`
  - `selectedOffice: any = null`
  - `unliquidatedCAs = signal<any[]>([])`
  - `voucherDate` drives both `voucherDate` and `cashAdvanceDate` in payload
  - `compareById(a, b): boolean`

- [ ] **Step 1: Remove `cashAdvanceDate` and add new state properties**

  In the class body, remove the line:
  ```typescript
  cashAdvanceDate   = '';
  ```

  Add these properties after `units = signal<any[]>([])`:
  ```typescript
  offices         = signal<any[]>([]);
  selectedOffice: any = null;
  unliquidatedCAs = signal<any[]>([]);
  ```

- [ ] **Step 2: Update `setDefaultDates()`**

  Replace the method — remove `cashAdvanceDate` assignment:

  ```typescript
  setDefaultDates(): void {
      const t = this.today();
      this.periodCoveredFrom = t;
      this.periodCoveredTo   = t;
  }
  ```

  > `voucherDate` is set by the flatpickr date picker default (today) — no need to set it here. If you need an explicit default, add `this.voucherDate = t;`.

- [ ] **Step 3: Update `ngOnInit()` — add office + unliquidated loading**

  Replace `ngOnInit()`:

  ```typescript
  ngOnInit(): void {
      this.loadUnits();
      this.route.paramMap.subscribe(params => {
          const idParam = params.get('id');
          this.editMode = idParam != null && /^\d+$/.test(idParam);
          if (this.editMode) {
              this.id        = Number(idParam);
              this.subModule = 'Edit';
              this.loadOffices(() => this.loadForEdit());
          } else {
              this.subModule = 'Create';
              this.addParticular();
              this.setDefaultDates();
              this.loadDefaultSignatories();
              this.loadOffices(() => this.loadUserOffice());
              this.loadUnliquidatedCAs();
          }
      });
  }
  ```

- [ ] **Step 4: Add `loadOffices()`, `loadUserOffice()`, `loadUnliquidatedCAs()`**

  Add after `loadDefaultSignatories()`:

  ```typescript
  loadOffices(callback?: () => void): void {
      this.service.getOffices().subscribe({
          next: (data) => { this.offices.set(data || []); if (callback) callback(); },
          error: () => { if (callback) callback(); }
      });
  }

  loadUserOffice(): void {
      this.service.getUserOffice().subscribe({
          next: (data) => { this.selectedOffice = data || null; },
          error: () => {}
      });
  }

  loadUnliquidatedCAs(): void {
      this.service.getUnliquidatedList().subscribe({
          next: (data) => this.unliquidatedCAs.set(data || []),
          error: () => {}
      });
  }
  ```

- [ ] **Step 5: Update `loadForEdit()` — populate `selectedOffice`**

  Inside `loadForEdit()`, in the `if (data?.id)` block, add after the existing field assignments:

  ```typescript
  this.selectedOffice = data.office || null;
  ```

  Remove this line (no longer needed):
  ```typescript
  this.cashAdvanceDate   = data.cashAdvanceDate   ? new Date(data.cashAdvanceDate).toISOString().substring(0, 10) : '';
  ```

- [ ] **Step 6: Update `isValid()` — remove `cashAdvanceDate` check**

  Replace `isValid()`:

  ```typescript
  isValid(): boolean {
      return !!(
          this.voucherDate &&
          this.purpose?.trim() &&
          this.recommendedBy &&
          this.approvingOfficer &&
          this.totalAmount > 0
      );
  }
  ```

- [ ] **Step 7: Update `save()` payload**

  In the `save()` method, update the payload object. Change:
  ```typescript
  voucherDate:      this.voucherDate,
  cashAdvanceDate:  this.cashAdvanceDate,
  ```
  To:
  ```typescript
  voucherDate:      this.voucherDate,
  cashAdvanceDate:  this.voucherDate,
  ```

  Also add `office` to the payload (after `employee: null`):
  ```typescript
  office: this.selectedOffice ? { id: this.selectedOffice.id } : null,
  ```

- [ ] **Step 8: Add `compareById` helper**

  Add after `clearSignatory()`:

  ```typescript
  compareById(a: any, b: any): boolean {
      return a && b ? a.id === b.id : a === b;
  }
  ```

- [ ] **Step 9: Build check**

  ```bash
  cd frontend && ng build --configuration development 2>&1 | tail -20
  ```

  Expected: 0 errors. Fix any reference to removed `cashAdvanceDate`.

- [ ] **Step 10: Commit**

  ```bash
  git add frontend/src/app/pages/ca/ca-add-edit/ca-add-edit.component.ts
  git commit -m "feat(ca): remove cashAdvanceDate, add office + unliquidated state to add-edit"
  ```

---

## Task 4: Refactor `ca-add-edit` template

**Files:**
- Modify: `frontend/src/app/pages/ca/ca-add-edit/ca-add-edit.component.html`

**Interfaces:**
- Consumes: `offices()`, `selectedOffice`, `compareById`, `unliquidatedCAs()`, `voucherDate` from Task 3

- [ ] **Step 1: Remove the "Cash Advance Date" input block**

  Delete this entire block from the HTML:

  ```html
  <div class="col-md-6">
      <label class="form-label fw-bold">
          Cash Advance Date <span class="text-danger">*</span>
      </label>
      <input type="text" mwlFlatpickr [options]="flatpickrOptions"
             class="form-control" placeholder="Select date"
             [(ngModel)]="cashAdvanceDate" name="cashAdvanceDate"
             [class]="submit && !cashAdvanceDate ? 'is-invalid' : submit && cashAdvanceDate ? 'is-valid' : ''"
             required/>
      <div class="invalid-feedback">Please select a cash advance date.</div>
  </div>
  ```

  After removal, the "Voucher Date" field becomes the sole date field at the top. Adjust it from `col-md-6` to `col-md-6` (keep width — it was already half-width; leave as-is or widen to `col-md-4` to match old layout).

- [ ] **Step 2: Add the office dropdown as the first field in the form**

  Before the "Voucher Date" `<div class="col-md-6">` block, insert:

  ```html
  <div class="col-md-12">
      <label class="form-label fw-bold">Office</label>
      <select class="form-select" [(ngModel)]="selectedOffice"
              name="office" [compareWith]="compareById">
          <option [ngValue]="null">— Select Office —</option>
          @for (o of offices(); track o.id) {
              <option [ngValue]="o">{{ o.name }}</option>
          }
      </select>
  </div>
  ```

- [ ] **Step 3: Add unliquidated CAs warning section**

  After the opening `<form ...>` tag and before the first field block, insert:

  ```html
  @if (unliquidatedCAs().length > 0) {
      <div class="col-12">
          <div class="alert alert-warning py-2 mb-0">
              <div class="fw-bold fs-xs text-uppercase mb-1">Unliquidated Cash Advances</div>
              <ul class="mb-0 ps-3">
                  @for (ca of unliquidatedCAs(); track ca.id) {
                      <li class="fs-xs">{{ ca.code }}</li>
                  }
              </ul>
          </div>
      </div>
  }
  ```

- [ ] **Step 4: Build check**

  ```bash
  cd frontend && ng build --configuration development 2>&1 | tail -20
  ```

  Expected: 0 errors. If `cashAdvanceDate` reference remains anywhere, remove it.

- [ ] **Step 5: Manual verify — create mode**

  - Unliquidated CAs warning shows at top if any exist (hidden if none)
  - Office dropdown appears as first form field, populated and pre-selected to user's office
  - Only ONE date field ("Voucher Date") visible — no Cash Advance Date
  - Saving sends `voucherDate` and `cashAdvanceDate` with the same value, plus `office`

- [ ] **Step 6: Manual verify — edit mode**

  - Office dropdown pre-selected to the CA's saved office
  - No "Cash Advance Date" field present
  - Unliquidated CAs warning NOT shown (create mode only)

- [ ] **Step 7: Commit**

  ```bash
  git add frontend/src/app/pages/ca/ca-add-edit/ca-add-edit.component.html
  git commit -m "feat(ca): remove Cash Advance Date field, add office dropdown and unliquidated warning"
  ```

---

## Self-Review Checklist

- [x] **Spec: Single Voucher Date field** → Task 3 removes `cashAdvanceDate` property; Task 4 removes input; payload sends `cashAdvanceDate: this.voucherDate`
- [x] **Spec: Office dropdown — main** → Task 2 adds signal, loads from API, adds dropdown, passes to `list()`
- [x] **Spec: Office dropdown — add-edit** → Task 3/4 add signal, load on init, populate on edit, add to payload and form
- [x] **Spec: Unliquidated CAs warning** → Task 3 adds signal + load; Task 4 adds warning section (create mode only)
- [x] **Spec: `list()` officeId param** → Task 1 updates method signature
- [x] **Spec: reset() sets selectedOffice = null** → Task 2 Step 4 confirmed
- [x] **Spec: offices list not cleared on reset** → Task 2 Step 4: only `selectedOffice` is nulled, `offices` signal untouched
- [x] **Type consistency:** `compareById` added to both main (Task 2) and add-edit (Task 3) — same signature
- [x] **No placeholders:** All steps have actual code. Unliquidated endpoint flagged for verification in Task 1 Step 1 with exact instructions
