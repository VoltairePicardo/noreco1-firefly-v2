import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { AccountingReportsService } from '../accounting-reports.service';

@Component({
    selector: 'app-unliquidated-ca',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './unliquidated-ca.component.html'
})
export class UnliquidatedCaComponent {
    module   = 'Unliquidated Cash Advance';
    menuLink = 'accounting-reports';

    statusDescription = 'All';
    statusOptions     = ['All', 'Overdue', 'Not Overdue'];

    rows      = signal<any[]>([]);
    isLoading = signal(false);

    private downloadSvc  = inject(DownloadService);
    private alertService = inject(AlertService);
    private service      = inject(AccountingReportsService);

    search(): void {
        this.isLoading.set(true);
        this.rows.set([]);
        this.service.getUnliquidatedCA(this.statusDescription).subscribe({
            next: (data) => { this.rows.set(data); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); }
        });
    }

    export(type: 'pdf' | 'xls'): void {
        this.downloadSvc.print(`/reports/export/unliquidated-ca/${this.statusDescription}`, { type });
    }
}
