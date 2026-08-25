import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { CreditCardPurchaseRequestService } from '../credit-card-purchase-request.service';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { FormsModule } from '@angular/forms';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerRefresh, tablerPlus, tablerEye, tablerEdit } from '@ng-icons/tabler-icons';
import { monthStart, monthEnd } from '@/app/shared/utils/date.utils';

@Component({
    selector: 'app-credit-card-purchase-request-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective, FormsModule, RouterLink],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS,
        provideIcons({ tablerSearch, tablerRefresh, tablerPlus, tablerEye, tablerEdit })],
    templateUrl: './credit-card-purchase-request-main.component.html'
})
export class CreditCardPurchaseRequestMainComponent {
    module   = 'Credit Card Purchase Request';
    moduleAbbr   = 'CCPR';
    menuLink = 'credit-card-purchase-request';

    records  = signal<any[]>([]);
    isLoading = signal(false);

    page     = 1;
    pageSize = 10;

    documentStatuses = signal<any[]>([]);
    from             = '';
    to               = '';
    statusId: number | null = null;

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    private service      = inject(CreditCardPurchaseRequestService);
    private alertService = inject(AlertService);

    searchText = '';

    get filteredRecords(): any[] {
        if (!this.searchText.trim()) return this.records();
        const q = this.searchText.toLowerCase();
        return this.records().filter((r: any) => (r.code || r.localCode || '').toLowerCase().includes(q));
    }

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.filteredRecords.slice(start, start + this.pageSize);
    }

    ngOnInit(): void {
        this.service.getDocumentStatuses().subscribe({
            next: (data) => this.documentStatuses.set(data || []),
            error: () => {}
        });

        this.from = monthStart();
        this.to   = monthEnd();
        this.load();
    }

    load(): void {
        if (!this.from || !this.to) return;
        this.isLoading.set(true);
        this.page = 1;

        const obs$ = this.statusId != null
            ? this.service.listByStatus(this.from, this.to, this.statusId)
            : this.service.list(this.from, this.to);

        obs$.subscribe({
            next: (data) => {
                this.records.set(data || []);
                this.isLoading.set(false);
            },
            error: () => {
                this.alertService.error(this.module, 'Load', '');
                this.isLoading.set(false);
            }
        });
    }

    reset(): void {
        this.from     = monthStart();
        this.to       = monthEnd();
        this.statusId = null;
        this.searchText = '';
        this.records.set([]);
        this.page = 1;
        this.load();
    }

    createBatch(): void {
        this.service.createBatch().subscribe({
            next: (res) => {
                if (res.success) {
                    this.alertService.success(this.module, 'Batch Created', '');
                    this.load();
                } else {
                    this.alertService.error(this.module, 'Create Batch', res.failureMessage || '');
                }
            },
            error: () => { this.alertService.error(this.module, 'Create Batch', ''); }
        });
    }

    isEditable(rec: any): boolean {
        const s = rec?.documentStatus?.status || '';
        return s === 'Document Created' || s === 'For Revision';
    }
}
