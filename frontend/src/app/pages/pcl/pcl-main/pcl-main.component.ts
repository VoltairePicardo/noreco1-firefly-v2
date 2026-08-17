import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { PclService } from '../pcl.service';

@Component({
    selector: 'app-pcl-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './pcl-main.component.html'
})
export class PclMainComponent {
    module    = 'Petty Cash Liquidation';
    subModule = '';
    menuLink  = 'pcl';

    records   = signal<any[]>([]);
    isLoading = signal(false);

    searchQuery = '';
    page        = 1;
    pageSize    = 20;

    private service      = inject(PclService);
    private alertService = inject(AlertService);

    get filteredRecords(): any[] {
        const q = this.searchQuery.toLowerCase();
        return this.records().filter(r =>
            !q ||
            (r.code || '').toLowerCase().includes(q) ||
            (r.pcvCode || r.pettyCashTransCode || '').toLowerCase().includes(q) ||
            (r.payee || '').toLowerCase().includes(q) ||
            (r.status || r.documentStatus || '').toLowerCase().includes(q)
        );
    }

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.filteredRecords.slice(start, start + this.pageSize);
    }

    ngOnInit(): void {
        this.load();
    }

    load(): void {
        this.isLoading.set(true);
        this.service.list().subscribe({
            next: (data) => { this.records.set(data || []); this.page = 1; this.isLoading.set(false); },
            error: () => { this.alertService.error(this.module, 'Failed to load records.', ''); this.isLoading.set(false); }
        });
    }

    onSearch(): void {
        this.page = 1;
    }

    reset(): void {
        this.searchQuery = '';
        this.page        = 1;
        this.load();
    }

    isEditable(rec: any): boolean {
        const s = (rec?.status || rec?.documentStatus || '').toString();
        return s === 'Document Created' || s === 'Returned to Creator' || s === '1' || s === '11';
    }
}
