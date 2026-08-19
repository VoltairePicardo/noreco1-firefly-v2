import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FormsModule } from '@angular/forms';
import { FlatpickrDefaults, FlatpickrModule } from 'angularx-flatpickr';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerRefresh, tablerPlus, tablerEye, tablerEdit } from '@ng-icons/tabler-icons';
import { AccountSettingService } from '../account-setting.service';

@Component({
    selector: 'app-account-setting-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FormsModule, FlatpickrModule, RouterLink],
    providers: [...SHARED_PROVIDERS, FlatpickrDefaults, provideIcons({ tablerSearch, tablerRefresh, tablerPlus, tablerEye, tablerEdit })],
    templateUrl: './account-setting-main.component.html'
})
export class AccountSettingMainComponent {
    module    = 'Account Setting';
    subModule = '';
    menuLink  = 'account-setting';

    fromDate   = '';
    toDate     = '';
    searchText = '';
    records    = signal<any[]>([]);

    page     = 1;
    pageSize = 10;

    get filteredRecords(): any[] {
        if (!this.searchText.trim()) return this.records();
        const q = this.searchText.toLowerCase();
        return this.records().filter((r: any) => (r.code || '').toLowerCase().includes(q));
    }

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.filteredRecords.slice(start, start + this.pageSize);
    }

    isLoading = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    private service = inject(AccountSettingService);

    ngOnInit(): void {
        const now = new Date();
        this.fromDate = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10);
        this.toDate   = new Date(now.getFullYear(), now.getMonth() + 1, 0).toISOString().substring(0, 10);
        this.load();
    }

    load(): void {
        if (!this.fromDate || !this.toDate) return;
        this.isLoading.set(true);
        this.service.listByDateRange(this.fromDate, this.toDate).subscribe({
            next: (data) => { this.records.set(data || []); this.page = 1; this.isLoading.set(false); },
            error: () => { this.records.set([]); this.page = 1; this.isLoading.set(false); }
        });
    }

    reset(): void {
        const now = new Date();
        this.fromDate  = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10);
        this.toDate    = new Date(now.getFullYear(), now.getMonth() + 1, 0).toISOString().substring(0, 10);
        this.searchText = '';
        this.records.set([]);
        this.page = 1;
        this.load();
    }

    isEditable(rec: any): boolean {
        // Account settings have no workflow — always editable
        return true;
    }
}
