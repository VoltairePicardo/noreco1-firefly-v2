import { Component, inject, OnInit, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { AccountingReportsService } from '../accounting-reports.service';

@Component({
    selector: 'app-pcv-summary',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './pcv-summary.component.html'
})
export class PcvSummaryComponent implements OnInit {
    module   = 'PCV Summary';
    menuLink = 'accounting-reports';
    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    fromDate      = '';
    toDate        = '';
    officeId      = 0;
    pcfId         = 0;
    docStatId     = 0;
    checkedBy: any     = null;
    replenishedBy: any = null;

    offices          = signal<any[]>([]);
    pcfFunds         = signal<any[]>([]);
    documentStatuses = signal<any[]>([]);

    private downloadSvc  = inject(DownloadService);
    private alertService = inject(AlertService);
    private modalService = inject(NgbModal);
    private service      = inject(AccountingReportsService);

    ngOnInit(): void {
        this.setDefaultDates();
        this.service.getDocumentStatuses().subscribe({
            next: (data) => this.documentStatuses.set([{ id: 0, status: 'All' }, ...(data || [])])
        });
        this.service.getOffices().subscribe({
            next: (data) => this.offices.set([{ id: 0, name: 'All' }, ...(data || [])])
        });
        this.loadBatches();
    }

    setDefaultDates(): void {
        const now = new Date(), first = new Date(now.getFullYear(), now.getMonth() - 1, 1);
        this.fromDate = first.toISOString().substring(0, 10);
        this.toDate   = now.toISOString().substring(0, 10);
    }

    loadBatches(): void {
        if (!this.fromDate || !this.toDate) return;
        this.pcfFunds.set([]);
        this.pcfId = 0;
        this.service.getPcvBatches(this.fromDate, this.toDate, this.officeId).subscribe({
            next: (data) => this.pcfFunds.set(data ?? [])
        });
    }

    onOfficeChange(): void {
        this.loadBatches();
    }

    browseCheckedBy(): void {
        const ref = this.modalService.open(BrowseEntityModalComponent, { size: 'lg', centered: true });
        ref.componentInstance.types = ['ENTITY_EMPLOYEE'];
        ref.result.then((entity) => { if (entity) this.checkedBy = entity; }, () => {});
    }

    browseReplenishedBy(): void {
        const ref = this.modalService.open(BrowseEntityModalComponent, { size: 'lg', centered: true });
        ref.componentInstance.types = ['ENTITY_EMPLOYEE'];
        ref.result.then((entity) => { if (entity) this.replenishedBy = entity; }, () => {});
    }

    export(type: 'pdf' | 'xls'): void {
        if (!this.fromDate || !this.toDate) {
            this.alertService.error(this.module, 'Validation', 'Please select a date range.');
            return;
        }
        if (!this.pcfId) {
            this.alertService.error(this.module, 'Validation', 'Please select a batch.');
            return;
        }
        if (!this.checkedBy) {
            this.alertService.error(this.module, 'Validation', 'Please select the checked-by officer.');
            return;
        }
        if (!this.replenishedBy) {
            this.alertService.error(this.module, 'Validation', 'Please select the replenished-by officer.');
            return;
        }
        this.downloadSvc.print(
            `/petty-cash-voucher/print/${this.checkedBy.accountNo}/${this.replenishedBy.accountNo}`,
            {
                from:             this.fromDate,
                to:               this.toDate,
                batch:            this.pcfId,
                documentStatusId: this.docStatId || '',
                officeId:         this.officeId || '',
                type
            }
        );
    }
}
