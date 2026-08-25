import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { NgbModal, NgbNavModule } from '@ng-bootstrap/ng-bootstrap';
import { GlAccountInquiryService } from '../gl-account-inquiry.service';
import { DownloadService } from '@/app/services/download.service';
import { BrowseCOAModalComponent } from '@/app/shared/modals/browse-coa-modal/browse-coa-modal.component';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { AlertService } from '@/app/shared/services/alert.service';
import { monthStart, monthEnd } from '@/app/shared/utils/date.utils';

@Component({
    selector: 'app-gl-account-inquiry-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective, NgbNavModule],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './gl-account-inquiry-main.component.html'
})
export class GlAccountInquiryMainComponent {
    module    = 'GL Account Inquiry';
    subModule = '';
    menuLink  = 'gl-account-inquiry';

    private service       = inject(GlAccountInquiryService);
    private downloadSvc   = inject(DownloadService);
    private modalService  = inject(NgbModal);
    private alertService  = inject(AlertService);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    fromDate = '';
    toDate   = '';

    selectedAccount: any = null;
    selectedDocStat: any = { id: 0, status: 'All' };
    checkedBy: any = null;

    documentStatuses = signal<any[]>([]);
    detailRows = signal<any[]>([]);
    summaryRows = signal<any[]>([]);
    isLoading = signal(false);

    activeTab = 'detail';

    get detailTotal(): { debit: number; credit: number; balance: number } {
        return this.detailRows().reduce(
            (acc, r) => ({ debit: acc.debit + (r.debit || 0), credit: acc.credit + (r.credit || 0), balance: r.balance || 0 }),
            { debit: 0, credit: 0, balance: 0 }
        );
    }

    get summaryTotal(): { debit: number; credit: number; balance: number } {
        return this.summaryRows().reduce(
            (acc, r) => ({ debit: acc.debit + (r.debit || 0), credit: acc.credit + (r.credit || 0), balance: r.balance || 0 }),
            { debit: 0, credit: 0, balance: 0 }
        );
    }

    ngOnInit(): void {
        this.fromDate = monthStart();
        this.toDate   = monthEnd();

        this.service.getDocumentStatuses().subscribe({
            next: (data) => {
                this.documentStatuses.set([{ id: 0, status: 'All' }, ...data]);
            }
        });

        this.service.getDefaultSignatories().subscribe({
            next: (data) => {
                if (data?.checkedBy) this.checkedBy = data.checkedBy;
            }
        });
    }

    browseAccount(): void {
        const ref = this.modalService.open(BrowseCOAModalComponent, { size: 'xl', centered: true });
        ref.result.then((account) => {
            if (account) this.selectedAccount = account;
        }, () => {});
    }

    browseCheckedBy(): void {
        const ref = this.modalService.open(BrowseEntityModalComponent, { size: 'lg', centered: true });
        ref.componentInstance.types = ['ENTITY_EMPLOYEE'];
        ref.result.then((entity) => {
            if (entity) this.checkedBy = entity;
        }, () => {});
    }

    search(): void {
        if (!this.selectedAccount) {
            this.alertService.error(this.module, 'Validation', 'Please select an account.');
            return;
        }

        this.isLoading.set(true);
        const statusId = this.selectedDocStat?.id ?? 0;

        this.service.getDetail(this.selectedAccount.id, this.fromDate, this.toDate, statusId).subscribe({
            next: (data) => { this.detailRows.set(data || []); this.isLoading.set(false); },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });

        this.service.getSummary(this.selectedAccount.id, this.fromDate, this.toDate, statusId).subscribe({
            next: (data) => { this.summaryRows.set(data || []); }
        });
    }

    export(type: 'pdf' | 'xls'): void {
        if (!this.selectedAccount) {
            this.alertService.error(this.module, 'Validation', 'Please select an account.');
            return;
        }

        const statusId = this.selectedDocStat?.id ?? 0;

        if (this.activeTab === 'detail') {
            this.downloadSvc.print(
                `/reports/export/gl-account-inquiry/${this.selectedAccount.id}/${this.fromDate}/${this.toDate}/${statusId}`,
                { type }
            );
        } else {
            if (!this.checkedBy) {
                this.alertService.error(this.module, 'Validation', 'Please select the Checked By officer.');
                return;
            }
            this.downloadSvc.print(
                `/reports/export/gl-account-inquiry/summary/${this.selectedAccount.id}/${this.fromDate}/${this.toDate}/${this.checkedBy.accountNo}/${statusId}`,
                { type }
            );
        }
    }
}
