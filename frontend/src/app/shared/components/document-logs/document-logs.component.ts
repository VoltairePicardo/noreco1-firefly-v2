import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import {AnyJSONService, DocumentLog} from '@/app/shared/services/any-json.service';

/** Log keys whose timestamps are shown with the time of day. */
const DATETIME_LOG_KEYS = new Set(['createdAt', 'updatedAt']);

/** Log keys that are not shown in the history. */
const HIDDEN_LOG_KEYS = new Set<string>();

/** Quotation log keys rendered as dedicated item / terms tables instead of plain log values. */
const QUOTATION_LOG_KEYS = new Set(['suppliers', 'quotationItems', 'terms']);

interface LedgerColumn {
    header: string;
    field: string;
    numeric?: boolean;
}

/** Log keys shown as their own table under a section heading, instead of as a plain log value. */
const LEDGER_TABLES: Record<string, { section: string; title: string; columns: LedgerColumn[] }> = {
    generalLedgerLines: {
        section: 'Ledger Entries',
        title: 'General Entries',
        columns: [
            { header: 'Code', field: 'code' },
            { header: 'Title', field: 'description' },
            { header: 'Debit', field: 'debit', numeric: true },
            { header: 'Credit', field: 'credit', numeric: true },
        ],
    },
    subLedgerLines: {
        section: 'Ledger Entries',
        title: 'Sub Entries',
        columns: [
            { header: 'Account', field: 'accountNo' },
            { header: 'Name', field: 'name' },
            { header: 'Amount', field: 'amount', numeric: true },
        ],
    },
    files: {
        section: 'Files',
        title: '',
        columns: [
            { header: 'File Name', field: 'originalFilename' },
            { header: 'Type', field: 'mimeType' },
            { header: 'Uploaded', field: 'updatedAt' },
        ],
    },
};

interface LogEntry {
    label: string;
    value: string;
}

interface LedgerTable {
    title: string;
    columns: LedgerColumn[];
    rows: string[][];
}

interface LedgerSection {
    heading: string;
    tables: LedgerTable[];
}

interface QuotationItemCell {
    awarded: boolean;
    brand: string;
    price: string;
}

interface QuotationItemRow {
    no: number;
    rvNo: string;
    description: string;
    unitCode: string;
    quantity: string;
    cells: QuotationItemCell[];
    available: boolean;
}

interface QuotationTermRow {
    label: string;
    values: string[];
}

interface QuotationLog {
    suppliers: string[];
    items: QuotationItemRow[];
    total: string;
    terms: QuotationTermRow[];
}

interface LogRow {
    id: number;
    entries: LogEntry[];
    sections: LedgerSection[];
    quotation: QuotationLog | null;
    userName: string;
    createdAt: string;
}

@Component({
    selector: 'app-document-logs',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [DatePipe],
    providers: [DatePipe],
    template: `
        @if (loading()) {
            <div class="text-center py-4">
                <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                Loading history…
            </div>
        } @else if (error()) {
            <div class="alert alert-danger mb-0" role="alert">{{ error() }}</div>
        } @else if (rows().length === 0) {
            <p class="text-muted mb-0">No history yet.</p>
        } @else {
            <div class="table-responsive">
                <table class="table table-sm table-striped align-top mb-0">
                    <thead class="bg-light align-middle bg-opacity-25 thead-sm">
                    <tr class="text-uppercase fs-xxs">
                        <th scope="col">Log</th>
                        <th scope="col" class="text-nowrap">User</th>
                        <th scope="col" class="text-nowrap">Timestamp</th>
                    </tr>
                    </thead>
                    <tbody>
                        @for (row of rows(); track row.id) {
                            <tr>
                                <td>
                                    <dl class="row gy-1 mb-0">
                                        @for (entry of row.entries; track entry.label) {
                                            <dt class="col-4 col-sm-4 fs-xs fw-normal">{{ entry.label }}</dt>
                                            <dd class="col-8 col-sm-8 fs-xs mb-0 fw-semibold">{{ entry.value }}</dd>
                                        }
                                    </dl>
                                    @if (row.quotation; as q) {
                                        @if (q.items.length > 0) {
                                            <div class="fs-xs text-uppercase fw-semibold mt-3 mb-1">Items For Quotation</div>
                                            <div class="table-responsive ps-3">
                                                <table class="table table-sm table-bordered fs-xs mb-2 align-middle">
                                                    <thead class="bg-light bg-opacity-25">
                                                    <tr class="text-uppercase fs-xxs">
                                                        <th>#</th>
                                                        <th>PR No.</th>
                                                        <th>Description of Materials</th>
                                                        <th>Unit</th>
                                                        <th class="text-end">Qty</th>
                                                        @for (s of q.suppliers; track $index) {
                                                            <th class="text-center">Award</th>
                                                            <th>Brand</th>
                                                            <th class="text-end">Price</th>
                                                        }
                                                        <th class="text-center">Available</th>
                                                    </tr>
                                                    <tr class="fs-xxs text-muted">
                                                        <th colspan="5"></th>
                                                        @for (s of q.suppliers; track $index) {
                                                            <th colspan="3" class="fw-normal">{{ s }}</th>
                                                        }
                                                        <th></th>
                                                    </tr>
                                                    </thead>
                                                    <tbody>
                                                        @for (it of q.items; track it.no) {
                                                            <tr>
                                                                <td class="text-muted">{{ it.no }}</td>
                                                                <td class="fw-semibold text-nowrap">{{ it.rvNo }}</td>
                                                                <td>{{ it.description }}</td>
                                                                <td>{{ it.unitCode }}</td>
                                                                <td class="text-end fw-semibold">{{ it.quantity }}</td>
                                                                @for (c of it.cells; track $index) {
                                                                    <td class="text-center text-success fw-bold">{{ c.awarded ? '✓' : '' }}</td>
                                                                    <td class="text-uppercase">{{ c.brand }}</td>
                                                                    <td class="text-end" [class.fw-bold]="c.awarded" [class.text-success]="c.awarded">{{ c.price }}</td>
                                                                }
                                                                <td class="text-center">
                                                                    <span class="badge" [class]="it.available ? 'bg-success-subtle text-success' : 'bg-secondary-subtle text-secondary'">{{ it.available ? 'Yes' : 'No' }}</span>
                                                                </td>
                                                            </tr>
                                                        }
                                                    </tbody>
                                                    <tfoot>
                                                    <tr>
                                                        <td [attr.colspan]="5 + q.suppliers.length * 3" class="text-end fw-bold">Total (Awarded Price × Qty):</td>
                                                        <td class="fw-bold text-end">{{ q.total }}</td>
                                                    </tr>
                                                    </tfoot>
                                                </table>
                                            </div>
                                        }
                                        @if (q.terms.length > 0) {
                                            <div class="fs-xs text-uppercase fw-semibold mt-3 mb-1">Terms and Conditions</div>
                                            <div class="table-responsive ps-3">
                                                <table class="table table-sm table-bordered fs-xs mb-2">
                                                    <thead class="bg-light bg-opacity-25">
                                                    <tr class="text-uppercase fs-xxs">
                                                        <th>Term</th>
                                                        @for (s of q.suppliers; track $index) {
                                                            <th>{{ s }}</th>
                                                        }
                                                    </tr>
                                                    </thead>
                                                    <tbody>
                                                        @for (t of q.terms; track t.label) {
                                                            <tr>
                                                                <td class="fw-semibold">{{ t.label }}</td>
                                                                @for (v of t.values; track $index) {
                                                                    <td>{{ v }}</td>
                                                                }
                                                            </tr>
                                                        }
                                                    </tbody>
                                                </table>
                                            </div>
                                        }
                                    }
                                    @for (s of row.sections; track s.heading; let lastSection = $last; let firstSection = $first) {
                                        <div class="fs-xs text-uppercase fw-semibold mb-1" [class.mt-3]="firstSection" [class.mt-0]="!firstSection">{{ s.heading }}</div>
                                        @for (t of s.tables; track t.title; let lastTable = $last) {
                                            @if (t.title) {
                                                <div class="fs-xs fw-semibold mb-1 ps-3">{{ t.title }}</div>
                                            }
                                            <div class="table-responsive ps-3">
                                                <table class="table table-sm table-borderless fs-xs mb-2">
                                                    <thead class="bg-light bg-opacity-25">
                                                    <tr class="text-uppercase fs-xxs">
                                                        @for (c of t.columns; track c.header) {
                                                            <th [class.text-end]="c.numeric">{{ c.header }}</th>
                                                        }
                                                    </tr>
                                                    </thead>
                                                    <tbody>
                                                        @for (r of t.rows; track $index) {
                                                            <tr>
                                                                @for (cell of r; track $index; let ci = $index) {
                                                                    <td [class.text-end]="t.columns[ci].numeric">{{ cell }}</td>
                                                                }
                                                            </tr>
                                                        }
                                                    </tbody>
                                                </table>
                                                @if (!(lastSection && lastTable)) {
                                                    <hr class="mt-0"/>
                                                }
                                            </div>
                                        }
                                    }
                                </td>
                                <td class="text-nowrap fs-xs">{{ row.userName }}</td>
                                <td class="text-nowrap fs-xs">{{ row.createdAt | date:'MMM dd, yyyy hh:mm:ss a' }}</td>
                            </tr>
                        }
                    </tbody>
                </table>
            </div>
        }
    `,
})
export class DocumentLogsComponent {
    private anyJsonService = inject(AnyJSONService);
    private datePipe = inject(DatePipe);

    transactionId = input<number | null | undefined>(null);

    private logs = signal<DocumentLog[]>([]);
    loading = signal(false);
    error = signal('');

    rows = computed(() => this.logs().map((log) => this.buildRow(log)));

    constructor() {
        effect(() => {
            const id = this.transactionId();
            if (!id) {
                this.logs.set([]);
                return;
            }
            this.load(id);
        });
    }

    private buildRow(log: DocumentLog): LogRow {
        let entries: LogEntry[] = [];
        const sections: LedgerSection[] = [];
        let quotation: QuotationLog | null = null;

        if (log.newValue) {
            try {
                const parsed = JSON.parse(log.newValue) as Record<string, unknown>;
                entries = Object.entries(parsed)
                    .filter(([key]) => !HIDDEN_LOG_KEYS.has(key) && !QUOTATION_LOG_KEYS.has(key) && !(key in LEDGER_TABLES))
                    .map(([key, value]) => ({
                        label: this.humanizeKey(key),
                        value: this.formatValue(value, key),
                    }));

                quotation = this.buildQuotation(parsed);

                for (const [key, spec] of Object.entries(LEDGER_TABLES)) {
                    const lines = parsed[key];
                    if (!Array.isArray(lines) || lines.length === 0) continue;
                    let section = sections.find((s) => s.heading === spec.section);
                    if (!section) {
                        section = { heading: spec.section, tables: [] };
                        sections.push(section);
                    }
                    section.tables.push({
                        title: spec.title,
                        columns: spec.columns,
                        rows: lines.map((line: Record<string, unknown>) =>
                            spec.columns.map((c) => c.numeric ? this.formatAmount(line?.[c.field]) : this.formatValue(line?.[c.field], c.field))),
                    });
                }
            } catch {
                entries = [{ label: 'Value', value: log.newValue }];
            }
        }

        return {
            id: log.id,
            entries,
            sections,
            quotation,
            userName: log.loggedBy?.fullName || log.loggedBy?.username || 'System',
            createdAt: log.createdAt,
        };
    }

    private buildQuotation(parsed: Record<string, unknown>): QuotationLog | null {
        const rawItems = Array.isArray(parsed['quotationItems']) ? parsed['quotationItems'] as any[] : [];
        const rawTerms = Array.isArray(parsed['terms']) ? parsed['terms'] as any[] : [];
        if (rawItems.length === 0 && rawTerms.length === 0) return null;

        const topSuppliers = Array.isArray(parsed['suppliers']) ? parsed['suppliers'] as any[] : [];
        const slotCount = Math.max(
            topSuppliers.length,
            rawTerms.length,
            ...rawItems.map((item) => (item.details?.length ?? 0)),
        );
        const suppliers: string[] = Array.from({ length: slotCount }, (_, i) =>
            topSuppliers[i]?.name
            || rawItems.map((item) => item.details?.[i]?.supplier?.name).find(Boolean)
            || rawTerms[i]?.supplier?.name
            || `Supplier ${i + 1}`);

        let total = 0;
        const items: QuotationItemRow[] = rawItems.map((item, idx) => {
            const qty = Number(item.quantity || 0);
            const available = item.available !== false;
            const cells: QuotationItemCell[] = suppliers.map((_, i) => {
                const d = item.details?.[i];
                const price = Number(d?.price || 0);
                if (available && d?.awarded) total += price * qty;
                return { awarded: !!d?.awarded, brand: d?.brand?.name || '—', price: this.formatAmount(price) };
            });
            return {
                no: idx + 1,
                rvNo: item.rvNo || '—',
                description: (item.isChildItem ? item.newItemDescription : item.itemDescription) || '—',
                unitCode: item.unitCode || '—',
                quantity: String(qty),
                cells,
                available,
            };
        });

        const termFields: { label: string; field: string }[] = [
            { label: 'Delivery Time & Completion', field: 'deliveryTimeAndCompletion' },
            { label: 'Warranty Period', field: 'warrantyPeriod' },
            { label: 'Terms of Payment (Days)', field: 'termsOfPayment' },
            { label: 'Place of Delivery', field: 'placeOfDelivery' },
        ];
        const terms: QuotationTermRow[] = rawTerms.length === 0 ? [] : termFields.map((f) => ({
            label: f.label,
            values: suppliers.map((_, i) => this.formatValue(rawTerms[i]?.[f.field])),
        }));

        return { suppliers, items, total: this.formatAmount(total), terms };
    }

    private formatAmount(value: unknown): string {
        const n = Number(value);
        if (value === null || value === undefined || value === '' || Number.isNaN(n)) return '—';
        return n.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }

    private humanizeKey(key: string): string {
        const spaced = key.replace(/([a-z0-9])([A-Z])/g, '$1 $2');
        return spaced.charAt(0).toUpperCase() + spaced.slice(1);
    }

    private formatValue(value: unknown, key?: string): string {
        if (value === null || value === undefined || value === '') return '—';
        if (typeof value === 'boolean') return value ? 'Yes' : 'No';
        if (typeof value === 'number') {
            // 13-digit numbers are epoch-millisecond timestamps in this API.
            if (value > 1e12) {
                const format = key && DATETIME_LOG_KEYS.has(key) ? 'MMM dd, yyyy hh:mm a' : 'MMM dd, yyyy';
                return this.datePipe.transform(value, format) ?? String(value);
            }
            return String(value);
        }
        if (typeof value === 'string') return value;
        if (Array.isArray(value)) {
            return value.length === 0 ? '—' : value.map((item) => this.formatValue(item)).join(', ');
        }
        if (typeof value === 'object') return this.extractLabel(value as Record<string, unknown>);
        return String(value);
    }

    private extractLabel(obj: Record<string, unknown>): string {
        for (const field of ['fullName', 'name', 'description', 'status', 'code']) {
            const val = obj[field];
            if (typeof val === 'string' && val) return val;
        }
        return obj['id'] !== undefined && obj['id'] !== null ? `# ${obj['id']}` : '—';
    }

    private load(transactionId: number): void {
        this.loading.set(true);
        this.error.set('');
        this.anyJsonService.getLogs(transactionId).subscribe({
            next: (logs) => {
                this.logs.set(logs);
                this.loading.set(false);
            },
            error: () => {
                this.error.set('Unable to load history. Please try again.');
                this.loading.set(false);
            },
        });
    }
}
