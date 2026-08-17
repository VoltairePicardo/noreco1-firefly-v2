# PCV Migration Alignment — Design Spec
**Date:** 2026-08-07
**Module:** Petty Cash Voucher (`frontend/src/app/pages/pcv`)
**Task:** Align Firefly V2 PCV with old Firefly PCV functionality + V2 frontend standards

---

## 1. Context

The existing V2 PCV implementation was previously migrated but is incomplete. This spec defines all gaps between old Firefly (`src/main/webapp/WEB-INF/views/pcv/`) and V2 (`frontend/src/app/pages/pcv/`) and specifies exactly what must be added or changed to bring V2 to full parity.

The old Firefly is the source of truth for behavior. V2 Angular 19 conventions (reactive signals, `app-ui-card`, `mwlFlatpickr`, `ModalService`, SweetAlert2, NgbModal) are the implementation standard.

---

## 2. Scope

**Files modified:**
1. `pcv-main/pcv-main.component.ts` + `.html`
2. `pcv-add-edit/pcv-add-edit.component.ts` + `.html`
3. `pcv-detail/pcv-detail.component.ts` + `.html`
4. `pcv.service.ts`
5. `pcv.route.ts`

**Files created:**
6. `pcv-closeout/pcv-closeout.component.ts`
7. `pcv-closeout/pcv-closeout.component.html`

**No new shared modals.** Replenish modal and cash flow item browser are inline NgbModal components within the PCV module (PCV-specific, no reusable equivalent exists in shared).

---

## 3. Component Designs

### 3.1 `pcv-main` — List Page

#### Table columns (fix)
Old Firefly table: **Code, Payee, Amount, Batch, Date, Status, Actions**
V2 current: Code, Payee, Amount, Request, Date, Status, Actions

Fix: replace "Request" column with "Batch" (`rec.pettyCashBatch?.id`).

#### Batch management toolbar (add)
Below the existing search/filter row, add a second toolbar row containing:

| Element | Behavior |
|---------|----------|
| Batch dropdown | `[(ngModel)]="selectedBatch"` populated from `service.getBatches()`. Filter list to show only batches with `status` filter matching `activeOnly`. On change: reload PCV list filtered to that batch. |
| Active Only checkbox (toggle button style) | `[(ngModel)]="activeOnly"`. When toggled: re-filter batch dropdown to show only Open/Active batches. |
| Create New Batch button (`btn-primary`) | Calls `service.createBatch()`. On success: reload batches + SweetAlert success. |
| Close button (`btn-danger`) | SweetAlert confirm → `service.closeBatch(selectedBatch.id)`. Disabled if no batch selected or batch already closed. |
| Replenish button (`btn-success`) | Opens replenish modal (inline NgbModal). |

#### Replenish modal (inline)
Implement as a `ng-template #replenishModal` opened via `NgbModal.open()`.

- **Title:** "Check Voucher List"
- **Filter row:** From date (`mwlFlatpickr`), To date (`mwlFlatpickr`), Code text input, Search button (`btn-success`)
- **Table:** Code, Payee, Amount, Particulars, Voucher Date — loaded via `service.getCheckVouchers(from, to, code)`
- Row click → highlights selected CV
- **Footer:** Cancel (`btn-light`), Replenish CV (`btn-danger`) → calls `service.replenish(selectedCv, selectedBatch.id)` → SweetAlert success/error → close modal + reload

---

### 3.2 `pcv-add-edit` — Create/Edit Form

#### Budget balance info rows (add)
When `budgetLineItemDetail` is selected, show three read-only info rows below the Budget Line Item browse field:

| Label | Binding | Source |
|-------|---------|--------|
| Budget Amount Balance PCL | `budgetBalancePCL` | loaded via `service.getBudgetBalances(budgetLineItemDetail.id)` |
| Amount Balance (PO/JO/RFP/CA/PCV) | `budgetBalancePOJO` | same call, different field |
| Amount Balance (PCV/CV) | `budgetBalanceCV` | same call, different field |

Display style: alert-info row (light blue tinted) with `fw-bold` value, matching old Firefly pattern. Call `loadBudgetBalances()` on BLI selection; clear on BLI removal.

#### `budgetUpdate` disable state (add)
The old Firefly disables several fields when `budgetUpdate = true`. This corresponds to a workflow state where the document is being processed and should not be edited.

Implementation: after loading a record in edit mode, check `data.budgetUpdate === true`. Store as `budgetUpdate = false` property. When `true`, disable: Voucher Date, Request, Payee, Budget Line Item browse, Petty Cash Items table (add/remove/edit), all signatory browse buttons.

Visual: disabled inputs use `[disabled]="budgetUpdate"`.

#### Request dropdown
Keep the 3 hardcoded options (Reimbursement, Advance, Emergency) — sufficient alignment with old Firefly usage.

---

### 3.3 `pcv-detail` — Detail / View Page

#### Workflow actions (add — follow CA pattern exactly)
After loading the record, call `service.getWorkflowActions(transId)`. If actions returned:

Show an `app-ui-card` titled "Process Document" with:
- Action select (`[(ngModel)]="selectedAction"`)
- Remarks text input (`[(ngModel)]="remarks"`)
- Submit button (`btn-primary`, disabled when no action or `formSubmit`) → calls `processWorkflow()` which calls `service.process(payload)` → SweetAlert success/error → reload

#### Print button (add)
Add a Print button (`btn-success fw-bold`) in the detail page footer (alongside the Back button, justified to the right). Calls `service.print(id)`.

#### Budget Balance field (add)
Add to the info grid in "PCV Details" card:
```
Budget Balance: {{ data.budgetAmountBalance | number:'1.2-2' }}
```

#### Cash Flow section (add)
Add a new `app-ui-card` titled "Cash Flow" below Petty Cash Items.

Two modes driven by `data.isDocumentForCashFlowItemAssignment`:

**Assignment mode** (`true`):
- Editable table: Cash Flow Item, Cash Flow Type, Amount (editable input), Actions (Edit / Remove)
- "Add" button in table header opens inline NgbModal cash flow item picker: browse `service.getCashFlowItems()`, select one, add to `cashFlowDetails[]`
- Balance rows: Amount Balance (PO/JO), Amount Balance (CV) — from `service.getCashFlowBalances(budgetLineItemDetailId)`
- Save button → `service.saveCashFlowItems(pcvId, cashFlowDetails)` → SweetAlert success/error → reload

**View mode** (`false`):
- Read-only table: Cash Flow Item, Parent Cash Flow Item, Cash Flow Type, Amount

#### Checked By (add to detail info)
Add "Checked By" field to the PCV Details info grid: `data.checker?.name || data.checker?.fullName || '—'`

---

### 3.4 `pcv-closeout` — New Standalone Component

**Route:** `pcv/closeout`

**Structure:** Single `app-ui-card` with filter section, table, officer pickers, footer.

#### Filter row
| Field | Control |
|-------|---------|
| From date | `mwlFlatpickr` |
| To date | `mwlFlatpickr` |
| Office | `<select>` populated from `service.getOffices()` |
| Status | `<select>` populated from `service.getDocumentStatuses()` |
| View Only button (`btn-success`) | loads table via `service.getCloseoutVouchers(params)` |
| Print Summary button (`btn-success`) | calls `service.printCloseout(params)` |

#### Batch management toolbar
Same as main list: batch dropdown, Active Only, Create New Batch, Close, Replenish (same behavior).

#### Table
Columns: Date, Code, Payee, Nature of Payment, G/L Acct., Amount, Status

#### Officers section (below table)
Two browse inputs using the same pattern as add-edit signatories:
- Checked By (`btn-success` Browse → `openSignatoryBrowse('checkedBy')`)
- Replenished By (`btn-success` Browse → `openSignatoryBrowse('replenishedBy')`)

#### Footer
```
Back button (btn-light, [routerLink]="['/pcv']")
```

---

## 4. Service Additions (`pcv.service.ts`)

| Method | HTTP | Endpoint |
|--------|------|----------|
| `getBatches()` | GET | `${BASE_URL}/pcv/batches` |
| `createBatch()` | POST | `${BASE_URL}/pcv/batch/create` |
| `closeBatch(batchId)` | POST | `${BASE_URL}/pcv/batch/close` |
| `getCheckVouchers(from, to, code)` | GET | `${BASE_URL}/pcv/check-vouchers` |
| `replenish(payload)` | POST | `${BASE_URL}/pcv/replenish` |
| `getBudgetBalances(budgetLineItemDetailId)` | GET | `${BASE_URL}/pcv/budget-balances/${id}` |
| `getCashFlowItems()` | GET | `${BASE_URL}/json/cashflow-items` |
| `getCashFlowBalances(budgetLineItemDetailId)` | GET | `${BASE_URL}/pcv/cashflow-balances/${id}` |
| `saveCashFlowItems(payload)` | POST | `${BASE_URL}/pcv/cashflow-items/save` |
| `print(id)` | GET (window.open) | `${BASE_URL}/pcv/print/${id}` |
| `getOffices()` | GET | `${BASE_URL}/json/offices` |
| `getDocumentStatuses()` | GET | `${BASE_URL}/json/document-statuses` |
| `getCloseoutVouchers(params)` | GET | `${BASE_URL}/pcv/closeout-vouchers` |
| `printCloseout(params)` | GET (window.open) | `${BASE_URL}/pcv/print-summary` |

---

## 5. Route Addition (`pcv.route.ts`)

```typescript
{
    path: 'closeout',
    loadComponent: () => import('./pcv-closeout/pcv-closeout.component').then(m => m.PcvCloseoutComponent),
    data: { title: 'PCV Closeout', mainPath }
}
```

---

## 6. V2 Standards Checklist

- [x] All buttons follow CLAUDE.md color standards
- [x] All date inputs use `mwlFlatpickr`
- [x] All alerts/confirms use SweetAlert2
- [x] Browse inputs use `input-group` pattern with `btn-success Browse`
- [x] Modals opened with `{ size: 'lg', centered: true }`
- [x] Save/Cancel pattern follows CLAUDE.md
- [x] Tables use `table table-custom table-centered table-select table-hover w-100 mb-0`
- [x] Table heads use `bg-light align-middle bg-opacity-25 thead-sm` + `text-uppercase fs-xxs`
- [x] Workflow actions follow CA detail pattern exactly
- [x] No new shared components created unnecessarily

---

## 7. Out of Scope

- Backend controller/service changes (Java)
- Any module outside `pcv/`
- Redesign of existing working V2 PCV logic
