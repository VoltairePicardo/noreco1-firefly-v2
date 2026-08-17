import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { InventoryReportsService } from '../../inventory-reports.service';

@Component({
    selector: 'app-mrte-ledger',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './mrte-ledger.component.html'
})
export class MrteLedgerComponent {
    module   = 'MRTE Ledger';
    menuLink = 'inventory-reports';

    selectedEmployee: any = null;

    rows      = signal<any[]>([]);
    isLoading = signal(false);

    private downloadSvc  = inject(DownloadService);
    private modalService = inject(NgbModal);
    private alertService = inject(AlertService);
    private service      = inject(InventoryReportsService);

    browseEmployee(): void {
        const ref = this.modalService.open(BrowseEntityModalComponent, { size: 'lg', centered: true });
        ref.result.then((result) => {
            if (result?.action === 'select') {
                this.selectedEmployee = result.data;
            }
        }, () => {});
    }

    clearEmployee(): void {
        this.selectedEmployee = null;
    }

    search(): void {
        this.isLoading.set(true);
        this.rows.set([]);
        this.service.getMrteLedgerData(this.selectedEmployee?.accountNo ?? 0).subscribe({
            next: (data) => { this.rows.set(data); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); }
        });
    }

    export(type: 'pdf' | 'xls'): void {
        const params: any = { type };
        if (this.selectedEmployee?.accountNo) {
            params['acctNo'] = this.selectedEmployee.accountNo;
        }
        this.downloadSvc.print(`/reports/export/mrte-ledger`, params);
    }
}
