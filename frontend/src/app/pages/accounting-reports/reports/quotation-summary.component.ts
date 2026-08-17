import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { AccountingReportsService } from '../accounting-reports.service';

@Component({
    selector: 'app-quotation-summary',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './quotation-summary.component.html'
})
export class QuotationSummaryComponent {
    module   = 'Summary of Quotations';
    menuLink = 'accounting-reports';

    rivId       = '';
    validator:  any = null;
    approvedBy: any = null;

    validatorError   = '';
    approvedByError  = '';

    items     = signal<any[]>([]);
    isLoading = signal(false);

    private downloadSvc  = inject(DownloadService);
    private alertService = inject(AlertService);
    private modalService = inject(NgbModal);
    private service      = inject(AccountingReportsService);

    browseValidator(): void {
        const ref = this.modalService.open(BrowseEntityModalComponent, { size: 'lg', centered: true });
        ref.componentInstance.types = ['ENTITY_EMPLOYEE'];
        ref.result.then((result) => {
            if (result) { this.validator = result; this.validatorError = ''; }
        }, () => {});
    }

    browseApprovedBy(): void {
        const ref = this.modalService.open(BrowseEntityModalComponent, { size: 'lg', centered: true });
        ref.componentInstance.types = ['ENTITY_EMPLOYEE'];
        ref.result.then((result) => {
            if (result) { this.approvedBy = result; this.approvedByError = ''; }
        }, () => {});
    }

    search(): void {
        if (!this.rivId || isNaN(Number(this.rivId))) {
            this.alertService.error(this.module, 'Validation', 'Please enter a valid RV ID.');
            return;
        }
        this.isLoading.set(true);
        this.items.set([]);
        this.service.getQuotationSummaryItems(Number(this.rivId)).subscribe({
            next: (data) => { this.items.set(data ?? []); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); }
        });
    }

    export(type: 'pdf' | 'xls'): void {
        if (!this.rivId || isNaN(Number(this.rivId))) {
            this.alertService.error(this.module, 'Validation', 'Please enter a valid RV ID.');
            return;
        }
        this.validatorError  = '';
        this.approvedByError = '';
        let valid = true;
        if (!this.validator)  { this.validatorError  = 'Please select a validating officer.';  valid = false; }
        if (!this.approvedBy) { this.approvedByError = 'Please select an approving officer.'; valid = false; }
        if (!valid) {
            this.alertService.error(this.module, 'Validation', 'Please select all required officers.');
            return;
        }
        this.downloadSvc.print(
            `/reports/export/quotation-summary/${this.rivId}/${this.validator.accountNo}/${this.approvedBy.accountNo}`,
            { type }
        );
    }
}
