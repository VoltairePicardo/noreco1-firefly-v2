import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { AccountingReportsService } from '../accounting-reports.service';

@Component({
    selector: 'app-cashflow-statement-bsup',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './cashflow-statement-bsup.component.html'
})
export class CashflowStatementBsupComponent {
    module   = 'Cash Flow Statement (BSUP)';
    menuLink = 'accounting-reports';
    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    fromDate    = '';
    toDate      = '';
    checkedBy: any = null;
    notedBy: any   = null;

    checkedByError = '';
    notedByError   = '';

    rows      = signal<any[]>([]);
    isLoading = signal(false);

    private downloadSvc  = inject(DownloadService);
    private alertService = inject(AlertService);
    private modalService = inject(NgbModal);
    private service      = inject(AccountingReportsService);

    ngOnInit(): void { this.setDefaultDates(); }

    setDefaultDates(): void {
        const now = new Date(), first = new Date(now.getFullYear(), now.getMonth(), 1);
        this.fromDate = first.toISOString().substring(0, 10);
        this.toDate   = now.toISOString().substring(0, 10);
    }

    browseCheckedBy(): void {
        const ref = this.modalService.open(BrowseEntityModalComponent, { size: 'lg', centered: true });
        ref.componentInstance.types = ['ENTITY_EMPLOYEE'];
        ref.result.then((entity) => { if (entity) this.checkedBy = entity; }, () => {});
    }

    browseNotedBy(): void {
        const ref = this.modalService.open(BrowseEntityModalComponent, { size: 'lg', centered: true });
        ref.componentInstance.types = ['ENTITY_EMPLOYEE'];
        ref.result.then((entity) => { if (entity) this.notedBy = entity; }, () => {});
    }

    private sameYearValid(): boolean {
        const y1 = this.fromDate?.substring(0, 4);
        const y2 = this.toDate?.substring(0, 4);
        if (y1 && y2 && y1 !== y2) {
            this.alertService.error(this.module, 'Validation', 'Date range must be within the same year.');
            return false;
        }
        return true;
    }

    search(): void {
        if (!this.fromDate || !this.toDate) {
            this.alertService.error(this.module, 'Validation', 'Please select a date range.');
            return;
        }
        if (!this.sameYearValid()) return;
        this.isLoading.set(true);
        this.rows.set([]);
        this.service.getCashFlowStatementBsup(this.fromDate, this.toDate).subscribe({
            next: (data) => { this.rows.set(data ?? []); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); }
        });
    }

    export(type: 'pdf' | 'xls'): void {
        if (!this.fromDate || !this.toDate) {
            this.alertService.error(this.module, 'Validation', 'Please select a date range.');
            return;
        }
        if (!this.sameYearValid()) return;
        this.checkedByError = '';
        this.notedByError   = '';
        let valid = true;
        if (!this.checkedBy) { this.checkedByError = 'Please select an officer.'; valid = false; }
        if (!this.notedBy)   { this.notedByError   = 'Please select an officer.'; valid = false; }
        if (!valid) {
            this.alertService.error(this.module, 'Validation', 'Please select all required officers.');
            return;
        }
        this.downloadSvc.print(
            `/reports/export/cashflow-statement-bsup/${this.fromDate}/${this.toDate}/${this.checkedBy.accountNo}/${this.notedBy.accountNo}`,
            { type }
        );
    }
}
