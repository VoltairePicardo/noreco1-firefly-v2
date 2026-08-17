import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch } from '@ng-icons/tabler-icons';
import { HttpClient } from '@angular/common/http';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');

@Component({
    selector: 'app-browse-purchase-order-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerSearch })],
    templateUrl: './browse-purchase-order-modal.component.html'
})
export class BrowsePurchaseOrderModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private http = inject(HttpClient);
    private cdr  = inject(ChangeDetectorRef);

    vendors: any[] = [];
    vendorsLoading = false;
    vendorSearch   = '';

    purchaseOrders: any[]    = [];
    purchaseOrdersLoading    = false;
    poSearch                 = '';
    selectedVendor: any      = null;

    ngOnInit(): void {
        this.vendorsLoading = true;
        this.http.get<any[]>(`${BASE_API}/purchase-order/approved-vendors`).subscribe({
            next: (v) => {
                this.vendors = v || [];
                this.vendorsLoading = false;
                this.cdr.markForCheck();
            },
            error: () => {
                this.vendorsLoading = false;
                this.cdr.markForCheck();
            }
        });
    }

    get filteredVendors(): any[] {
        const q = this.vendorSearch.trim().toLowerCase();
        if (!q) return this.vendors;
        return this.vendors.filter(v =>
            (v.name      || '').toLowerCase().includes(q) ||
            (v.accountNo || '').toString().includes(q)    ||
            (v.address   || '').toLowerCase().includes(q)
        );
    }

    get filteredPurchaseOrders(): any[] {
        const q = this.poSearch.trim().toLowerCase();
        if (!q) return this.purchaseOrders;
        return this.purchaseOrders.filter(po =>
            (po.localCode  || po.code || '').toLowerCase().includes(q) ||
            (po.supplier   || po.vendor?.name || '').toLowerCase().includes(q) ||
            (po.preparedBy || '').toLowerCase().includes(q)
        );
    }

    selectVendor(vendor: any): void {
        if (this.selectedVendor?.accountNo === vendor.accountNo) return;
        this.selectedVendor = vendor;
        this.purchaseOrders = [];
        this.poSearch = '';
        this.purchaseOrdersLoading = true;
        this.cdr.markForCheck();
        this.http.get<any[]>(`${BASE_API}/purchase-order/by-supplier/${vendor.accountNo}`).subscribe({
            next: (pos) => {
                this.purchaseOrders = pos || [];
                this.purchaseOrdersLoading = false;
                this.cdr.markForCheck();
            },
            error: () => {
                this.purchaseOrdersLoading = false;
                this.cdr.markForCheck();
            }
        });
    }

    select(po: any): void {
        this.activeModal.close({ action: 'select', data: po });
    }
}
