import { Component, inject, OnInit, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { BrowseCOAModalComponent } from '@/app/shared/modals/browse-coa-modal/browse-coa-modal.component';
import { BrowseAssetModalComponent } from '@/app/shared/modals/browse-asset-modal/browse-asset-modal.component';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { AccountingReportsService } from '../accounting-reports.service';

@Component({
    selector: 'app-asset-ledger',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './asset-ledger.component.html'
})
export class AssetLedgerComponent implements OnInit {
    module   = 'Asset Ledger';
    menuLink = 'accounting-reports';
    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    fromDate        = '';
    toDate          = '';
    selectedAccount: any = null;
    selectedAsset: any   = null;
    linkTypeId      = 0;

    linkTypeOptions = [
        { id: 0, displayName: 'All (Total Cost of Ownership)' },
        { id: 2, displayName: 'Major Repair (Capitalize)' },
        { id: 3, displayName: 'Minor Repair or Maintenance (Expense)' },
    ];

    rows      = signal<any[]>([]);
    isLoading = signal(false);

    private downloadSvc  = inject(DownloadService);
    private alertService = inject(AlertService);
    private modalService = inject(NgbModal);
    private service      = inject(AccountingReportsService);

    ngOnInit(): void {
        this.setDefaultDates();
    }

    setDefaultDates(): void {
        const now = new Date(), first = new Date(now.getFullYear(), now.getMonth(), 1);
        this.fromDate = first.toISOString().substring(0, 10);
        this.toDate   = now.toISOString().substring(0, 10);
    }

    browseAccount(): void {
        const ref = this.modalService.open(BrowseCOAModalComponent, { size: 'lg', centered: true });
        ref.result.then((result) => {
            if (result?.action === 'select') {
                this.selectedAccount = result.data;
                this.selectedAsset   = null;
            }
        }, () => {});
    }

    browseAsset(): void {
        const ref = this.modalService.open(BrowseAssetModalComponent, { size: 'lg', centered: true });
        ref.result.then((result) => {
            if (result?.action === 'select') this.selectedAsset = result.data;
        }, () => {});
    }

    search(): void {
        if (!this.selectedAccount) {
            this.alertService.error(this.module, 'Validation', 'Please select an account.');
            return;
        }
        const accountId    = this.selectedAccount.id;
        const assetAcctNo  = this.selectedAsset?.accountNo ?? 0;
        this.isLoading.set(true);
        this.rows.set([]);
        this.service.getAssetLedgerData(this.fromDate, this.toDate, accountId, assetAcctNo, this.linkTypeId).subscribe({
            next: (data) => { this.rows.set(data); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); }
        });
    }

    export(type: 'pdf' | 'xls'): void {
        if (!this.selectedAccount) {
            this.alertService.error(this.module, 'Validation', 'Please select an account.');
            return;
        }
        const accountId   = this.selectedAccount.id;
        const assetAcctNo = this.selectedAsset?.accountNo ?? 0;
        this.downloadSvc.print(
            `/reports/export/asset-ledger/${this.fromDate}/${this.toDate}/${accountId}/${assetAcctNo}/${this.linkTypeId}`,
            { type }
        );
    }
}
