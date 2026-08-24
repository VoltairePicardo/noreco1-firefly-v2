import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { FormsModule } from '@angular/forms';
import { AlertService } from '@/app/shared/services/alert.service';
import { AccountsPayableVoucherService } from '../accounts-payable-voucher.service';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { monthStart, monthEnd } from '@/app/shared/utils/date.utils';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerRefresh, tablerPlus, tablerEye, tablerEdit, tablerThumbUp } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-accounts-payable-voucher-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective, FormsModule, RouterLink],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS,
        provideIcons({ tablerSearch, tablerRefresh, tablerPlus, tablerEye, tablerEdit, tablerThumbUp })],
    templateUrl: './accounts-payable-voucher-main.component.html'
})
export class AccountsPayableVoucherMainComponent {
    module   = 'Accounts Payable Voucher';
    menuLink = 'accounts-payable-voucher';

    isLoading    = signal(false);
    processingAll = false;
    items        = signal<any[]>([]);
    statuses     = signal<any[]>([]);

    page     = 1;
    pageSize = 10;

    selectedStatusId = 0;
    dateFrom = '';
    dateTo   = '';

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    private service      = inject(AccountsPayableVoucherService);
    private alertService = inject(AlertService);

    private fmt(d: Date): string {
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    }

    ngOnInit(): void {
        this.service.getDocumentStatuses().subscribe({
            next: (data) => this.statuses.set(data || []),
            error: () => {}
        });
        this.dateFrom = monthStart();
        this.dateTo   = monthEnd();
        this.load();
    }

    load(): void {
        if (!this.dateFrom || !this.dateTo) return;
        this.isLoading.set(true);
        this.page = 1;
        this.service.getList(this.dateFrom, this.dateTo, this.selectedStatusId).subscribe({
            next: (data) => {
                this.items.set((data || []).map((d: any) => ({ ...d, selected: false })));
                this.isLoading.set(false);
            },
            error: () => {
                this.alertService.error(this.module, 'Load', '');
                this.isLoading.set(false);
            }
        });
    }

    reset(): void {
        this.dateFrom        = monthStart();
        this.dateTo          = monthEnd();
        this.selectedStatusId = 0;
        this.searchText = '';
        this.items.set([]);
        this.page = 1;
        this.load();
    }

    searchText = '';

    get filteredItems(): any[] {
        if (!this.searchText.trim()) return this.items();
        const q = this.searchText.toLowerCase();
        return this.items().filter((r: any) => (r.localCode || r.code || '').toLowerCase().includes(q));
    }

    get pagedItems(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.filteredItems.slice(start, start + this.pageSize);
    }

    isEditable(item: any): boolean {
        const s = item?.status || '';
        return s === 'Document Created' || s === 'For Revision';
    }

    get selectedIds(): number[] {
        return this.items().filter(i => i.selected).map(i => i.id);
    }

    approveAll(): void {
        const ids = this.selectedIds;
        if (ids.length === 0) {
            this.alertService.warning(this.module, 'Approve All', 'Please select at least one record.');
            return;
        }
        this.processingAll = true;
        this.service.approveAll(ids).subscribe({
            next: (res) => {
                this.processingAll = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Approved', '');
                    this.load();
                } else {
                    this.alertService.error(this.module, 'Approve All', res?.failureMessage || '');
                }
            },
            error: () => {
                this.processingAll = false;
                this.alertService.error(this.module, 'Approve All', '');
            }
        });
    }
}
