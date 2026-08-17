# CA Module Alignment Design
**Date:** 2026-08-07
**Scope:** Align `frontend/src/app/pages/ca` (Firefly V2) with legacy `WEB-INF/views/ca` (Firefly V1)

---

## Goal

Bring the V2 Cash Advance (CA) module into full behavioral and field-level alignment with the old AngularJS/JSP implementation, removing V2-only inputs that have no counterpart in old, and adding missing features confirmed for inclusion.

---

## Features Confirmed for Alignment

| Feature | Include | Source |
|---|---|---|
| Single "Voucher Date" field (sets both voucherDate + cashAdvanceDate) | ✅ | Old had one field; V2 mistakenly split it |
| Office dropdown — main filter | ✅ | Old had it |
| Office dropdown — add-edit form | ✅ | Old had it |
| Unliquidated CAs warning — add-edit | ✅ | Old showed at top of form |
| Liquidated button — detail | ✅ | Done this session |
| Document Logs — detail | ✅ | Done this session |
| Budget Line Item browser | ❌ Skip | Complex, separate feature |
| Cash Flow section — detail | ❌ Skip | Complex |
| Employee browse — add-edit | ❌ Remove | Old had no UI; payload sends `employee: null` |

---

## Section 1: `ca.service.ts`

**Add three methods:**

```typescript
getOffices(): Observable<any[]>
  → GET /json/offices/

getUserOffice(): Observable<any>
  → GET /json/office-user/

getUnliquidatedList(): Observable<any[]>
  → GET /cash-advance/unliquidated/list   // verify exact path during impl
```

**Update existing method:**

```typescript
list(from?, to?, statusId?, officeId?): Observable<any[]>
  → append officeId as HttpParam when present
```

---

## Section 2: `ca-main`

### Additions
- `offices = signal<any[]>([])`
- `selectedOffice: any = null`
- On `ngOnInit`: call `getOffices()` to populate dropdown choices; call `getUserOffice()` → set `selectedOffice` to user default; then `load()`
- Office dropdown in filter row (after Document Status, before Search input)
- `load()` passes `selectedOffice?.id` as officeId (omitted when null → backend returns all)
- `reset()` sets `selectedOffice = null` — dropdown choices remain (offices list is not cleared)

### Removals
None — main is clean.

---

## Section 3: `ca-add-edit`

### Removals
- **`cashAdvanceDate` property** — deleted entirely
- **"Cash Advance Date" input** in HTML — deleted
- `cashAdvanceDate` assignment in `setDefaultDates()` — removed
- `cashAdvanceDate` population in `loadForEdit()` — removed
- `cashAdvanceDate` check in `isValid()` — removed
- `cashAdvanceDate` in save payload — replaced: both `voucherDate` and `cashAdvanceDate` now set to `this.voucherDate`

### Additions
- `offices = signal<any[]>([])`
- `selectedOffice: any = null`
- On `ngOnInit` (create mode): load offices, set user default, load unliquidated CAs
- On `ngOnInit` (edit mode): load offices, populate `selectedOffice` from `data.office` in `loadForEdit()`
- `unliquidatedCAs = signal<any[]>([])`
- `getUnliquidatedList()` called on create mode only
- **Warning section** at top of form: shown when `unliquidatedCAs().length > 0`, lists CA codes
- **Office dropdown** in form (first field, above Voucher Date)
- Payload: `office: this.selectedOffice ? { id: this.selectedOffice.id } : null`

### Payload change
```typescript
// Before
cashAdvanceDate: this.cashAdvanceDate,
voucherDate:     this.voucherDate,

// After
cashAdvanceDate: this.voucherDate,   // same value
voucherDate:     this.voucherDate,
```

---

## Section 4: `ca-detail`

Already completed this session:
- **Liquidated button** — shown when `isApproved() && !isLiquidated()`; triggers `POST /cash-advance/liquidated/{id}` with SweetAlert2 confirmation
- **Document Logs** — lazy-loaded via "View Logs" button; shows timestamp, log, remarks, user columns

No removals needed in detail.

---

## Skipped Features (deliberately excluded)

| Feature | Reason |
|---|---|
| Office filter in detail | Not present in old detail view |
| Budget Line Item browser (add-edit + detail) | Complex subsystem, separate feature |
| Cash Flow section (detail) | Complex, not core to CA alignment |
| Unliquidated CAs warning (detail) | Only in add-edit in old |
| Confirm Budget Line Item button | Tied to budget line item feature |

---

## Files Changed

| File | Type |
|---|---|
| `ca.service.ts` | Add 3 methods, update `list()` |
| `ca-main.component.ts` | Add office state + load logic |
| `ca-main.component.html` | Add office dropdown to filter row |
| `ca-add-edit.component.ts` | Remove `cashAdvanceDate`, add office + unliquidated logic |
| `ca-add-edit.component.html` | Remove Cash Advance Date field, add office dropdown + unliquidated warning |
