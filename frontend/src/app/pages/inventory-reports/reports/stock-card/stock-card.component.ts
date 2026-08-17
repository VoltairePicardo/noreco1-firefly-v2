import { Component, inject, OnInit, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { BrowseItemModalComponent } from '@/app/shared/modals/browse-item-modal/browse-item-modal.component';
import { InventoryReportsService } from '../../inventory-reports.service';

@Component({
    selector: 'app-stock-card',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './stock-card.component.html'
})
export class StockCardComponent implements OnInit {
    module   = 'Stock Card';
    menuLink = 'inventory-reports';

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    fromDate           = '';
    toDate             = '';
    selectedLocationId = 0;
    selectedItem: any  = null;

    inventoryLocations = signal<any[]>([]);
    rows               = signal<any[]>([]);
    isLoading          = signal(false);

    private downloadSvc  = inject(DownloadService);
    private modalService = inject(NgbModal);
    private alertService = inject(AlertService);
    private service      = inject(InventoryReportsService);

    ngOnInit(): void {
        this.setDefaultDates();
        this.service.getInventoryLocations().subscribe({
            next: (data) => this.inventoryLocations.set(data ?? [])
        });
    }

    setDefaultDates(): void {
        const now   = new Date();
        const first = new Date(now.getFullYear(), now.getMonth(), 1);
        this.fromDate = first.toISOString().substring(0, 10);
        this.toDate   = now.toISOString().substring(0, 10);
    }

    onLocationChange(): void {
        this.selectedItem = null;
        this.rows.set([]);
    }

    browseItem(): void {
        if (!this.selectedLocationId) {
            this.alertService.error(this.module, 'Validation', 'Please select an inventory location first.');
            return;
        }
        const ref = this.modalService.open(BrowseItemModalComponent, { size: 'lg', centered: true });
        ref.componentInstance.inventoryLocationId = this.selectedLocationId;
        ref.result.then((result) => {
            if (result?.action === 'select') {
                this.selectedItem = result.data;   // id is itemStockId
                this.rows.set([]);
            }
        }, () => {});
    }

    search(): void {
        if (!this.fromDate) {
            this.alertService.error(this.module, 'Validation', 'Please select a from date.');
            return;
        }
        if (!this.toDate) {
            this.alertService.error(this.module, 'Validation', 'Please select a to date.');
            return;
        }
        if (!this.selectedItem) {
            this.alertService.error(this.module, 'Validation', 'Please select an item.');
            return;
        }
        const from = new Date(this.fromDate + 'T00:00:00').getTime().toString();
        const to   = new Date(this.toDate + 'T23:59:59').getTime().toString();
        this.isLoading.set(true);
        this.rows.set([]);
        this.service.getStockCardData(from, to, this.selectedItem.id).subscribe({
            next: (data) => { this.rows.set(data); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); }
        });
    }

    export(type: 'pdf' | 'xls'): void {
        if (!this.selectedItem) {
            this.alertService.error(this.module, 'Validation', 'Please select an item.');
            return;
        }
        this.downloadSvc.print(
            `/reports/export/stock-card/${this.fromDate}/${this.toDate}/${this.selectedItem.id}`,
            { type }
        );
    }
}
