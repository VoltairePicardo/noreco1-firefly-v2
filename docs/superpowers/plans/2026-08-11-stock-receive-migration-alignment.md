# Stock Receive Migration Alignment Plan (Phase 2 — Remaining Gaps)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix the remaining parity gaps in the already-partially-migrated `stock-receive` Angular module. Phase 1 (service methods, browse modal, basic structure) is already complete. This plan targets the specific business-logic and field gaps identified by comparing the current NEW code against the OLD JSP/AngularJS implementation.

**Architecture:** Three targeted edits across two files (`stock-receive-add-edit.component.ts/.html`, `stock-receive-detail.component.ts/.html`). No new files created. No structural changes.

**Tech Stack:** Angular 19, `BrowseEntityModalComponent` (already exists in shared/modals), `ModalService`, `AlertService`.

## Global Constraints

- Follow CLAUDE.md: `btn-success fw-bold` for Browse buttons, standard input-group pattern for browse inputs
- `BrowseEntityModalComponent` is the shared signatory browser — same pattern used in `rr-add-edit`
- All alerts: `AlertService` (SweetAlert2) — no `alert()` / `confirm()`
- Do NOT restructure unrelated code — minimal targeted edits only
- No `git commit` or `git push` — user handles all git operations
- OLD Firefly = source of truth for business logic and field requirements

---

## Parity Matrix (Current State after Phase 1)

```
Area                         OLD (source of truth)                NEW (current — to fix)      Status
──────────────────────────── ──────────────────────────────────── ─────────────────────────── ──────────
add-edit: Noted By input     Read-only input + Browse button      Read-only only, NO browse   MISSING
add-edit: Noted By required  Required — validated before save     No validation at all        MISSING
add-edit: documentType       Captured from selected doc (.type)   Always null                 INCORRECT
add-edit: Signatories sect.  Always visible                       Hidden when checkedBy=null  INCORRECT
detail: Received By label    "Received By"                        "Prepared By"               DIFFERENT
detail: Office field         Shows office.name                    Not present                 MISSING
detail: Last Update field    Shows updatedAt                      Not present                 MISSING
detail: transId safety       transaction.id (nested object)       data.transId (may be undef) VERIFY
```

---

## Task 1: Add-Edit — Add Noted By Browse Button + Required Validation

**Files:**
- Modify: `frontend/src/app/pages/stock-receive/stock-receive-add-edit/stock-receive-add-edit.component.ts`
- Modify: `frontend/src/app/pages/stock-receive/stock-receive-add-edit/stock-receive-add-edit.component.html`

**Interfaces:**
- Consumes: `BrowseEntityModalComponent` from `@/app/shared/modals/browse-entity-modal/browse-entity-modal.component`
- Consumes: `ModalService.openModal(BrowseEntityModalComponent, {}, { size: 'lg', centered: true })`
- Result shape: `{ action: 'select', data: entity }` where `entity` has `.fullName`, `.accountNo`

- [ ] **Step 1: Import BrowseEntityModalComponent in the TS file**

In `stock-receive-add-edit.component.ts`, add this import after the `BrowseStockReceiveDocModalComponent` import:

```typescript
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
```

- [ ] **Step 2: Add `openSignatoryBrowse()` method**

In `stock-receive-add-edit.component.ts`, add this method after `onReceiveQtyChange()`:

```typescript
async openSignatoryBrowse(): Promise<void> {
    try {
        const result = await this.modalService.openModal(
            BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
        );
        if (result?.action === 'select' && result?.data) {
            this.checkedBy = result.data;
        }
    } catch { }
}
```

- [ ] **Step 3: Add `checkedBy` validation to `save()`**

In `stock-receive-add-edit.component.ts`, inside `save()`, after the `details.length === 0` validation block and before `this.isLoading.set(true)`, add:

```typescript
        if (!this.checkedBy)
            { this.alertService.warning(this.module, 'Validation', 'Please select a Noted By (noting officer).'); return; }
```

- [ ] **Step 4: Fix the Signatories section in HTML — always visible + add Browse button**

In `stock-receive-add-edit.component.html`, find the entire current signatories block:

```html
                        @if (checkedBy) {
                        <hr class="my-3"/>
                        <div class="fs-xs text-uppercase fw-semibold text-muted mb-2">Signatories</div>
                        <div class="row g-3">
                            <div class="col-md-4">
                                <label class="form-label fw-bold">Noted By</label>
                                <input type="text" class="form-control" [value]="checkedBy?.fullName || ''" readonly/>
                            </div>
                        </div>
                        }
```

Replace it with (always visible, with Browse button):

```html
                        <hr class="my-3"/>
                        <div class="fs-xs text-uppercase fw-semibold text-muted mb-2">Signatories</div>
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label fw-bold">Noted By <span class="text-danger">*</span></label>
                                <div class="input-group">
                                    <input type="text" class="form-control"
                                           [value]="checkedBy ? (checkedBy.name || checkedBy.fullName || '') : ''"
                                           readonly placeholder="Browse noting officer..."/>
                                    <button type="button" class="btn btn-success fw-bold"
                                            (click)="openSignatoryBrowse()">
                                        <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
                                    </button>
                                </div>
                            </div>
                        </div>
```

---

## Task 2: Add-Edit — Capture documentType from Selected Document

**Files:**
- Modify: `frontend/src/app/pages/stock-receive/stock-receive-add-edit/stock-receive-add-edit.component.ts`

**Interfaces:**
- OLD: `postData['documentType'] = $scope.receiving.type` (captured in `stock_transfer_selection_handler` as `$scope.receiving.type = document.type`)
- NEW: `doc` returned from `BrowseStockReceiveDocModalComponent` may carry a `.type` field

- [ ] **Step 1: Add `documentType` property to the component**

In the component class property declarations (alongside `selectedDocCode`, `details`, `checkedBy`), add:

```typescript
    documentType: any = null;
```

- [ ] **Step 2: Capture documentType when a document is selected**

In `openDocumentBrowse()`, after `this.selectedDocCode = doc.code || '';`, add:

```typescript
                this.documentType = doc.type || null;
```

- [ ] **Step 3: Send documentType in save payload**

In `save()`, in the payload object, change:

```typescript
            documentType:        null,
```

to:

```typescript
            documentType:        this.documentType,
```

---

## Task 3: Detail — Fix Labels and Add Missing Fields

**Files:**
- Modify: `frontend/src/app/pages/stock-receive/stock-receive-detail/stock-receive-detail.component.html`
- Modify: `frontend/src/app/pages/stock-receive/stock-receive-detail/stock-receive-detail.component.ts`

**Interfaces:**
- No new service calls; all data already available in `data` object
- Fields to use: `data.office?.name`, `data.updatedAt`

- [ ] **Step 1: Fix "Prepared By" label to "Received By" in detail HTML**

In `stock-receive-detail.component.html`, find:

```html
            <div class="col-md-3"><div class="fs-xs text-uppercase fw-bold text-muted mb-1">Prepared By</div><div>{{ data.createdBy?.fullName || data.preparedBy || '—' }}</div></div>
```

Change label text from `Prepared By` to `Received By`:

```html
            <div class="col-md-3"><div class="fs-xs text-uppercase fw-bold text-muted mb-1">Received By</div><div>{{ data.createdBy?.fullName || data.preparedBy || '—' }}</div></div>
```

- [ ] **Step 2: Add Office and Last Update fields to the detail header card**

In `stock-receive-detail.component.html`, in the first `<div class="card mb-3">` (header info card), after the existing `Noted By` col-md-3 div (or after the last existing field div), add:

```html
            <div class="col-md-3"><div class="fs-xs text-uppercase fw-bold text-muted mb-1">Office</div><div>{{ data.office?.name || '—' }}</div></div>
            @if (data.updatedAt) {
            <div class="col-md-3"><div class="fs-xs text-uppercase fw-bold text-muted mb-1">Last Update</div><div>{{ data.updatedAt | date:'MMMM d, yyyy h:mm a' }}</div></div>
            }
```

- [ ] **Step 3: Add a `transactionId` getter in the detail TS for safe field access**

In `stock-receive-detail.component.ts`, add a private getter after the class properties:

```typescript
    private get transactionId(): number | undefined {
        return this.data?.transId ?? this.data?.transaction?.id;
    }
```

- [ ] **Step 4: Replace all `this.data.transId` usages with `this.transactionId`**

In `stock-receive-detail.component.ts`:

Find in `loadWorkflowActions()`:
```typescript
        if (!this.data?.transId || this.isTerminal()) return;
        this.service.getWorkflowActions(this.data.transId).subscribe(...)
```

Replace with:
```typescript
        if (!this.transactionId || this.isTerminal()) return;
        this.service.getWorkflowActions(this.transactionId).subscribe(...)
```

Find in `toggleLogs()`:
```typescript
            this.service.getDocumentLogs(this.data.transId).subscribe(...)
```

Replace with:
```typescript
            this.service.getDocumentLogs(this.transactionId!).subscribe(...)
```

---

## Validation Checklist (after all tasks complete)

- [ ] TypeScript compiles without errors on changed files
- [ ] `openSignatoryBrowse()` is `async` and wrapped in `try/catch` (dismissal is silently caught)
- [ ] Signatories section shows with Browse button even when `checkedBy` is null on page load
- [ ] `documentType` property initialized as `null`, captured on document select, sent in payload
- [ ] `transactionId` getter safely falls back to `transaction?.id` when `transId` is absent
- [ ] No native `alert()` calls introduced
- [ ] No unrelated files modified

---

## Final OLD → NEW Parity Checklist

### Functionality
- [ ] Noted By can be browsed and changed in add-edit form
- [ ] Save fails with warning if Noted By not selected
- [ ] `documentType` is sent from selected document in save payload
- [ ] Workflow actions load correctly (transactionId getter resolves both field names)
- [ ] Document logs load correctly (same getter)

### UI
- [ ] Signatories section always visible in add-edit (no `@if (checkedBy)` guard on section)
- [ ] Noted By shows Browse button (input-group + btn-success pattern)
- [ ] Detail shows "Received By" label (not "Prepared By")
- [ ] Detail header card shows Office field
- [ ] Detail header card shows Last Update field (conditional on `data.updatedAt`)

### V2 Standard
- [ ] Browse button: `btn-success fw-bold` with `ng-icon tablerSearch`
- [ ] Input group pattern matches CLAUDE.md Browse Input Pattern
- [ ] `BrowseEntityModalComponent` reused (not a new modal)
- [ ] No competing patterns introduced

### Regression
- [ ] Main list still loads, filters, paginates correctly
- [ ] Create workflow still works end-to-end
- [ ] Edit still loads existing data (checkedBy, inventoryLocation, details)
- [ ] Detail print still works
- [ ] Workflow processing still works
