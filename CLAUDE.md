# CLAUDE.md — noreco1-firefly-v2

## Project Overview
## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.1-SNAPSHOT |
| Packaging | WAR (external Tomcat deployment) |
| Templates | Thymeleaf + Thymeleaf Security extras |
| Security | Spring Security 6 + JWT (jjwt 0.11.5) |
| Persistence | Spring Data JPA + Spring Data JDBC |
| Databases | Microsoft SQL Server + MySQL (dual DB) |
| Reporting | JasperReports 6.15.0 + DynamicJasper 5.3.0 |
| PDF | iText 5.5.13.2 |
| Excel | Apache POI 4.1.2 |
| CSV | Apache Commons CSV 1.11.0 |
| QR Codes | ZXing 3.5.3 |
| Utilities | Lombok, Commons Net, Groovy (JasperReports scripts) |
| Build | Maven |

## Common Commands

```bash
# Build (skip tests)
mvn clean install -DskipTests

# Run locally
mvn spring-boot:run

# Run tests
mvn test

# Package WAR for deployment
mvn clean package -DskipTests
```

## Project Structure

```
src/main/java/com/noreco1/noreco1fireflyv2/
├── Noreco1FireflyV2Application.java   # Main entry point
├── ServletInitializer.java            # WAR servlet support
└── (feature packages to be added)

src/main/resources/
└── application.properties             # App config (DB, mail, JWT, etc.)
```

## Java / Spring Boot Conventions

- Use **Lombok** (`@Data`, `@Builder`, `@Slf4j`, etc.) — it is already configured
- Use **constructor injection** (via Lombok `@RequiredArgsConstructor`) not field injection
- Group classes into feature packages (e.g., `billing`, `member`, `meter`, `report`) not layer packages
- Use `@Repository`, `@Service`, `@RestController` / `@Controller` appropriately
- REST endpoints: use `@RestController` for API, `@Controller` for Thymeleaf views
- Validation: use `@Valid` + Bean Validation annotations (`@NotNull`, `@Size`, etc.)
- Use `ResponseEntity<?>` for REST responses with proper HTTP status codes
- Keep `@Service` methods transactional with `@Transactional` where needed
- Use Spring Data JPA repositories; write JPQL/native queries only when necessary
- Secure endpoints via Spring Security config — do not leave `permitAll()` without intent
- JWT is used for stateless auth — do not mix with session-based auth
- Reports are generated via JasperReports — `.jrxml` files go under `src/main/resources/reports/`

## Security Notes

- Never log passwords, JWT secrets, or PII
- JWT secret and DB credentials must come from `application.properties` or env vars — never hardcode
- CSRF: verify whether it is disabled for REST APIs and enabled for Thymeleaf views

---

## Workflow Orchestration

### 1. Plan Mode Default
- Enter plan mode for ANY non-trivial task (3+ steps or architectural decisions)
- If something goes sideways, STOP and re-plan immediately
- Use plan mode for verification steps, not just building
- Write detailed specs upfront to reduce ambiguity

### 2. Subagent Strategy
- Use subagents liberally to keep main context window clean
- Offload research, exploration, and parallel analysis to subagents
- For complex problems, throw more compute at it via subagents
- One task per subagent for focused execution

### 3. Self-Improvement Loop
- After ANY correction from the user: update `tasks/lessons.md` with the pattern
- Write rules that prevent the same mistake
- Ruthlessly iterate on these lessons until mistake rate drops
- Review lessons at session start for relevant context

### 4. Verification Before Done
- Never mark a task complete without proving it works
- Diff behavior between main and your changes when relevant
- Ask yourself: "Would a staff engineer approve this?"
- Run tests, check logs, demonstrate correctness

### 5. Demand Elegance (Balanced)
- For non-trivial changes: pause and ask "is there a more elegant way?"
- If a fix feels hacky: "Knowing everything I know now, implement the elegant solution"
- Skip this for simple, obvious fixes — don't over-engineer
- Challenge your own work before presenting it

### 6. Autonomous Bug Fixing
- When given a bug report: just fix it. Don't ask for hand-holding
- Point at logs, errors, failing tests — then resolve them
- Zero context switching required from the user
- Go fix failing CI tests without being told how

---

## Task Management

1. **Plan First** — Write plan to `tasks/todo.md` with checkable items
2. **Verify Plan** — Check in before starting implementation
3. **Track Progress** — Mark items complete as you go
4. **Explain Changes** — High-level summary at each step
5. **Document Results** — Add review section to `tasks/todo.md`
6. **Capture Lessons** — Update `tasks/lessons.md` after corrections

---

## Core Principles

- **Simplicity First** — Make every change as simple as possible. Minimal code impact.
- **No Laziness** — Find root causes. No temporary fixes. Senior developer standards.
- **Minimal Impact** — Only touch what's necessary. No side effects, no new bugs.

---

## Frontend UI/UX Standards

> Reference priority: **Legacy Firefly** → **IBCMS v2** → **Firefly v2**

### Button Color Standards

| Action Type | Class | Usage |
|---|---|---|
| Primary action | `btn-primary` | Save, Create, Confirm, Submit |
| Search / Browse / Print / Export | `btn-success` | Search, Browse, Print, Export |
| Cancel / Back / Close | `btn-light` | Cancel, Back, Close modal |
| Destructive / Reset | `btn-danger` | Delete, Remove, Reset filters |

- All action buttons: add `fw-bold`
- Icon-only buttons (table row actions): `btn-light btn-icon btn-sm rounded-circle` — NO `btn-sm` on text buttons
- Always pair icons with `ng-icon` — use `tablerSearch`, `tablerPlus`, `tablerPrinter`, `tablerRefresh`, `tablerEdit`, `tablerEye`, `tablerTrash`, etc.

### Table Standards

Follow **IBCMS v2 PMES** table pattern:
- `table table-custom table-centered table-select table-hover w-100 mb-0`
- `thead`: `bg-light align-middle bg-opacity-25 thead-sm`
- `tr` in thead: `text-uppercase fs-xxs`
- Empty row: single `<td colspan="N">` with centered muted text + spinner when loading

### Browse Input Pattern

```html
<div class="input-group">
  <input type="text" class="form-control" [value]="selectedItem?.description" readonly placeholder="..."/>
  <button type="button" class="btn btn-success fw-bold" (click)="openBrowse()">
    <ng-icon name="tablerSearch" class="ps-0 pe-2 fw-bold"></ng-icon>Browse
  </button>
</div>
```

### Label / Value Standards

- Form labels: `fw-bold` (bold)
- Values / inputs: normal weight
- Section headers inside modals or cards: `fw-semibold` or `text-uppercase fs-xs`

### Component Placement

| Element | Position |
|---|---|
| Create button | Upper-right of filter row (`col-md-auto ms-auto`) |
| Save / Submit | Lower-right of form footer |
| Cancel / Back | Lower-left or adjacent-left of Save |
| Filters (date, status, search) | Upper-left |
| Print / Export | Adjacent to Save in detail view footer |

### Alerts & Confirmations

- Use **SweetAlert2** (`Swal.fire(...)`) for ALL alerts, confirmations, and success/error messages
- No native `alert()` or `confirm()` calls
- No Bootstrap toast for user-facing feedback — use SweetAlert

### Date Inputs

- Always use `mwlFlatpickr` with `[options]="flatpickrOptions"`
- Standard options: `{ dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' }`
- Provide `FlatpickrDefaults` in component `providers`

### Modal Standards

- Always open NgbModal with `{ size: 'lg', centered: true }`
- Browse/lookup modals: `size: 'lg'` or `size: 'xl'` depending on content
- Confirmation modals: `size: 'sm'` or use SweetAlert instead

### Add-Edit Form Layout

- Prefer a **single `app-ui-card`** with `<hr>` between logical sections rather than multiple stacked cards.
- Multiple cards cause excessive vertical scrolling — keep forms compact so users don't have to scroll.
- Use `<hr/>` between sections, with a section label (`fs-xs text-uppercase fw-semibold text-muted`) above each group.
- Only use separate cards when sections are truly independent (e.g., a line-items table that needs its own header action button).

### Save/Cancel Pattern (add-edit forms)

Use these 2 as exact patterns standard add-edit cancel/back and save, this should still be part of the card/container not separated. Choose only one of them. 

```html
<div class="d-flex gap-2 justify-content-end align-items-center pt-3 border-top mt-3">
    <a [routerLink]="['/' + menuLink]" class="btn btn-light fw-bold">
        <ng-icon name="tablerArrowLeft" class="ps-0 pe-3 fw-bold"></ng-icon>Back
    </a>
    <button type="submit" class="btn btn-primary fw-bold" [disabled]="formSubmit">
        <ng-icon name="tablerCheck" class="ps-0 pe-3 fw-bold"></ng-icon>
        {{ editMode ? 'Update' : 'Save ' + module}}
    </button>
</div>

OR

<div class="col-xl-12">
    <div class="d-flex gap-2 justify-content-end align-items-center pt-3 border-top mt-3">
        <a [routerLink]="['/' + menuLink]" class="btn btn-light fw-bold">
            <ng-icon name="tablerArrowLeft" class="ps-0 pe-3 fw-bold"></ng-icon>Back
        </a>
        <button type="submit" class="btn btn-primary fw-bold" [disabled]="isLoading()">
            <ng-icon name="tablerCheck" class="ps-0 pe-3 fw-bold"></ng-icon>
            {{ editMode ? 'Update' : 'Save ' + module}}
        </button>
    </div>
</div>
```

- Use `<a [routerLink]>` (not `<button>`) for Back/Cancel
- Icon padding: `ps-0 pe-3` (wider gap for form buttons vs `pe-2` for compact buttons)
- Always `justify-content-end` — Save is on the right, Back is to its left
