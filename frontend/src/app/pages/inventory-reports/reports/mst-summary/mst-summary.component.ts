import { Component, inject, OnInit, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { InventoryReportsService } from '../../inventory-reports.service';

@Component({
    selector: 'app-mst-summary',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './mst-summary.component.html'
})
export class MstSummaryComponent implements OnInit {
    module   = 'Summary of MST';
    menuLink = 'inventory-reports';

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    fromDate   = '';
    toDate     = '';
    statusId   = 0;
    locationId = 0;
    statuses   = signal<any[]>([]);
    locations  = signal<any[]>([]);

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
        this.service.getMstSummary(this.fromDate, this.toDate, this.locationId, this.statusId).subscribe({
            next: (data) => { this.rows.set(data ?? []); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); }
        });
    }

    toggleRow(index: number, row: any): void {
        const isExpanded = this.expandedRows.get(index) ?? false;
        this.expandedRows.set(index, !isExpanded);
        if (!isExpanded && !this.rowItems.has(index)) {
            this.loadingRowItems.add(index);
            const transId = row.transaction?.id ?? row.id;
            this.service.getMstItems(transId).subscribe({
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
            `/reports/export/mst-summary/${this.fromDate}/${this.toDate}/${this.statusId}`,
            { type, location: this.locationId }
        );
    }
}
