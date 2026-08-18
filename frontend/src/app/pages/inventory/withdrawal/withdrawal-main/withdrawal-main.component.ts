import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { WithdrawalService } from '../withdrawal.service';

@Component({
    selector: 'app-withdrawal-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './withdrawal-main.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class WithdrawalMainComponent implements OnInit {
    module    = 'Stock Withdrawal';
    subModule = '';
    menuLink  = 'withdrawal';

    records          = signal<any[]>([]);
    isLoading        = signal(false);
    documentStatuses = signal<any[]>([]);

    page     = signal(1);
    pageSize = 10;
    searchText = signal('');

    filteredTotal = computed(() => {
        const q = this.searchText().trim().toLowerCase();
        if (!q) return this.records().length;
        return this.records().filter(r =>
            (r.code        || '').toLowerCase().includes(q) ||
            (r.description || '').toLowerCase().includes(q) ||
            (r.createdBy?.fullName || r.preparedBy || '').toLowerCase().includes(q)
        ).length;
    });

    pagedRecords = computed(() => {
        const q = this.searchText().trim().toLowerCase();
        const filtered = q
            ? this.records().filter(r =>
                (r.code        || '').toLowerCase().includes(q) ||
                (r.description || '').toLowerCase().includes(q) ||
                (r.createdBy?.fullName || r.preparedBy || '').toLowerCase().includes(q)
              )
            : this.records();
        const start = (this.page() - 1) * this.pageSize;
        return filtered.slice(start, start + this.pageSize);
    });

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    fromDate       = '';
    toDate         = '';
    selectedStatus = signal<number | null>(null);

    private service      = inject(WithdrawalService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.setDefaultDates();
        this.loadStatuses();
        this.load();
    }

    setDefaultDates(): void {
        const now   = new Date();
        const first = new Date(now.getFullYear(), now.getMonth(), 1);
        this.fromDate = first.toISOString().substring(0, 10);
        this.toDate   = now.toISOString().substring(0, 10);
    }

    loadStatuses(): void {
        this.service.getDocumentStatuses().subscribe({
            next: (s) => this.documentStatuses.set(s || []),
            error: () => {}
        });
    }

    load(): void {
        this.isLoading.set(true);
        const obs = (this.fromDate && this.toDate)
            ? this.service.listByDateRange(this.fromDate, this.toDate, this.selectedStatus())
            : this.service.list();
        obs.subscribe({
            next: (data) => { this.records.set(data || []); this.page.set(1); this.isLoading.set(false); },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    reset(): void {
        this.setDefaultDates();
        this.selectedStatus.set(null);
        this.searchText.set('');
        this.load();
    }

    isEditable(rec: any): boolean {
        const s = rec?.documentStatus?.status || '';
        return s === 'Document Created' || s === 'Returned to Creator';
    }
}
