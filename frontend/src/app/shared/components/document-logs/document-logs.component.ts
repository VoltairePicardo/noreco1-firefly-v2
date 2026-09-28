import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import {AnyJSONService, DocumentLog} from '@/app/shared/services/any-json.service';

interface LogEntry {
    label: string;
    value: string;
}

interface LogRow {
    id: number;
    entries: LogEntry[];
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
                <table class="table table-sm align-middle mb-0">
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
                                            <dt class="col-5 col-sm-4 fs-xs fw-normal">{{ entry.label }}</dt>
                                            <dd class="col-7 col-sm-8 fs-xs mb-0 fw-semibold">{{ entry.value }}</dd>
                                        }
                                    </dl>
                                </td>
                                <td class="text-nowrap fs-xs fw-semibold">{{ row.userName }}</td>
                                <td class="text-nowrap fs-xs fw-semibold">{{ row.createdAt | date:'MMM dd, yyyy hh:mm:ss a' }}</td>
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

    /** The document's workflow transaction id (e.g. `loan.transaction.id`). */
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

        if (log.newValue) {
            try {
                const parsed = JSON.parse(log.newValue) as Record<string, unknown>;
                entries = Object.entries(parsed).map(([key, value]) => ({
                    label: this.humanizeKey(key),
                    value: this.formatValue(value),
                }));
            } catch {
                entries = [{ label: 'Value', value: log.newValue }];
            }
        }

        return {
            id: log.id,
            entries,
            userName: log.loggedBy?.fullName || log.loggedBy?.username || 'System',
            createdAt: log.createdAt,
        };
    }

    private humanizeKey(key: string): string {
        const spaced = key.replace(/([a-z0-9])([A-Z])/g, '$1 $2');
        return spaced.charAt(0).toUpperCase() + spaced.slice(1);
    }

    private formatValue(value: unknown): string {
        if (value === null || value === undefined || value === '') return '—';
        if (typeof value === 'boolean') return value ? 'Yes' : 'No';
        if (typeof value === 'number') {
            // 13-digit numbers are epoch-millisecond timestamps in this API.
            if (value > 1e12) return this.datePipe.transform(value, 'MMM dd, yyyy') ?? String(value);
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
