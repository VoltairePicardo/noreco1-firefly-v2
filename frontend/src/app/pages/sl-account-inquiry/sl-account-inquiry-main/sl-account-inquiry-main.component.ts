import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { SlAccountInquiryService } from '../sl-account-inquiry.service';
import { DownloadService } from '@/app/services/download.service';
import { BrowseCOAModalComponent } from '@/app/shared/modals/browse-coa-modal/browse-coa-modal.component';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { AlertService } from '@/app/shared/services/alert.service';

@Component({
    selector: 'app-sl-account-inquiry-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './sl-account-inquiry-main.component.html'
})
export class SlAccountInquiryMainComponent {
    module    = 'SL Account Inquiry';
    subModule = '';
    menuLink  = 'sl-account-inquiry';

    private service      = inject(SlAccountInquiryService);
    private downloadSvc  = inject(DownloadService);
    private modalService = inject(NgbModal);
    private alertService = inject(AlertService);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    fromDate = '';
    toDate   = '';

    selectedAccount: any  = null;
    selectedSLEntity: any = null;

    rows      = signal<any[]>([]);
    isLoading = signal(false);

    get totals(): { debit: number; credit: number } {
        return this.rows().reduce(
            (acc, r) => ({ debit: acc.debit + (r.debit || 0), credit: acc.credit + (r.credit || 0) }),
            { debit: 0, credit: 0 }
        );
    }

    ngOnInit(): void {
        const now = new Date();
        const first = new Date(now.getFullYear(), now.getMonth(), 1);
        this.fromDate = first.toISOString().substring(0, 10);
        this.toDate   = now.toISOString().substring(0, 10);
    }

    browseAccount(): void {
        const ref = this.modalService.open(BrowseCOAModalComponent, { size: 'xl', centered: true });
        ref.result.then((account) => {
            if (account) this.selectedAccount = account;
        }, () => {});
    }

    browseSLEntity(): void {
        const ref = this.modalService.open(BrowseEntityModalComponent, { size: 'lg', centered: true });
        ref.result.then((entity) => {
            if (entity) this.selectedSLEntity = entity;
        }, () => {});
    }

    search(): void {
        if (!this.selectedAccount) {
            this.alertService.error(this.module, 'Validation', 'Please select an account.');
            return;
        }
        if (!this.selectedSLEntity) {
            this.alertService.error(this.module, 'Validation', 'Please select an SL Entity.');
            return;
        }

        this.isLoading.set(true);
        this.service.getData(this.selectedAccount.id, this.selectedSLEntity.accountNo, this.fromDate, this.toDate).subscribe({
            next: (data) => { this.rows.set(data || []); this.isLoading.set(false); },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    export(type: 'pdf' | 'xls'): void {
        if (!this.selectedAccount || !this.selectedSLEntity) {
            this.alertService.error(this.module, 'Validation', 'Please select an account and SL Entity.');
            return;
        }
        this.downloadSvc.print(
            `/reports/export/sl-account-inquiry/${this.selectedAccount.id}/${this.selectedSLEntity.accountNo}/${this.fromDate}/${this.toDate}`,
            { type }
        );
    }
}
