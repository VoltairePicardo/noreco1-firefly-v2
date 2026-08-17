import { Component, inject, OnInit, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { InventoryReportsService } from '../../inventory-reports.service';

const RELEASING_DOC_TYPES = [
    { id: 0, desc: 'All' },
    { id: 1, desc: 'Material Charge Ticket' },
    { id: 2, desc: 'Office Furniture and Equipment Acquired or Issued' },
    { id: 3, desc: 'Office Supplies or Spare Parts Issuance Slip' },
    { id: 4, desc: 'Stock Transfer Release' }
];

@Component({
    selector: 'app-withdrawal-summary',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './withdrawal-summary.component.html'
})
export class WithdrawalSummaryComponent implements OnInit {
    module   = 'Summary of Stock Withdrawal';
    menuLink = 'inventory-reports';

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    fromDate   = '';
    toDate     = '';
    statusId   = 0;
    locationId = 0;
    docTypeId  = 0;
    statuses   = signal<any[]>([]);
    locations  = signal<any[]>([]);
    docTypes   = RELEASING_DOC_TYPES;

    rows      = signal<any[]>([]);
    isLoading = signal(false);

    expandedRows    = new Map<number, boolean>();
    rowItems        = new Map<number, any[]>();
    loadingRowItems = new Set<number>();

    private downloadSvc  = inject(DownloadService);
    private service      = inject(InventoryReportsService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.setDefaultDates();
        this.service.getDocumentStatuses().subscribe({
            next: (data) => this.statuses.set([{ id: 0, status: 'All' }, ...(data || [])])
        });
        this.service.getInventoryLocations().subscribe({
            next: (data) => this.locations.set([{ id: 0, description: 'All' }, ...(data || [])])
        });
    }

    setDefaultDates(): void {
        const now = new Date(), first = new Date(now.getFullYear(), now.getMonth(), 1);
        this.fromDate = first.toISOString().substring(0, 10);
        this.toDate   = now.toISOString().substring(0, 10);
    }

    search(): void {
        if (!this.fromDate || !this.toDate) {
            this.alertService.error(this.module, 'Validation', 'Please select a date range.');
            return;
        }
        this.rows.set([]);
        this.expandedRows.clear();
        this.rowItems.clear();
        this.loadingRowItems.clear();
        this.isLoading.set(true);
        this.service.getWithdrawalSummary(this.fromDate, this.toDate, this.docTypeId, this.locationId, this.statusId).subscribe({
            next: (data) => { this.rows.set(data ?? []); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); }
        });
    }

    toggleRow(index: number, row: any): void {
        const isExpanded = this.expandedRows.get(index) ?? false;
        this.expandedRows.set(index, !isExpanded);
        if (!isExpanded && !this.rowItems.has(index)) {
            this.loadingRowItems.add(index);
            this.service.getWithdrawalItems(row.id).subscribe({
                next: (items) => { this.rowItems.set(index, items ?? []); this.loadingRowItems.delete(index); },
                error: () => { this.loadingRowItems.delete(index); }
            });
        }
    }

    export(type: 'pdf' | 'xls'): void {
        if (!this.fromDate || !this.toDate) {
            this.alertService.error(this.module, 'Validation', 'Please select a date range.');
            return;
        }
        this.downloadSvc.print(
            `/reports/export/withdrawal-summary/${this.fromDate}/${this.toDate}/${this.statusId}`,
            { type, docType: this.docTypeId, location: this.locationId }
        );
    }
}
