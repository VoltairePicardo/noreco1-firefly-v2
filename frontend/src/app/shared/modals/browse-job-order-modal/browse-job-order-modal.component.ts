import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch } from '@ng-icons/tabler-icons';
import { JobOrderService } from '@/app/pages/job-order/job-order.service';

@Component({
    selector: 'app-browse-job-order-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerSearch })],
    templateUrl: './browse-job-order-modal.component.html'
})
export class BrowseJobOrderModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private service = inject(JobOrderService);
    private cdr = inject(ChangeDetectorRef);

    vendors: any[] = [];
    vendorsLoading = false;
    vendorSearch = '';

    jobOrders: any[] = [];
    jobOrdersLoading = false;
    joSearch = '';
    selectedVendor: any = null;

    ngOnInit(): void {
        this.vendorsLoading = true;
        this.service.getApprovedVendors().subscribe({
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

    get filteredJobOrders(): any[] {
        const q = this.joSearch.trim().toLowerCase();
        if (!q) return this.jobOrders;
        return this.jobOrders.filter(jo =>
            (jo.localCode  || '').toLowerCase().includes(q) ||
            (jo.supplier   || jo.vendor?.name || '').toLowerCase().includes(q) ||
            (jo.preparedBy || '').toLowerCase().includes(q)
        );
    }

    selectVendor(vendor: any): void {
        if (this.selectedVendor?.accountNo === vendor.accountNo) return;
        this.selectedVendor = vendor;
        this.jobOrders = [];
        this.joSearch = '';
        this.jobOrdersLoading = true;
        this.cdr.markForCheck();
        this.service.getJobOrdersBySupplier(vendor.accountNo).subscribe({
            next: (jos) => {
                this.jobOrders = jos || [];
                this.jobOrdersLoading = false;
                this.cdr.markForCheck();
            },
            error: () => {
                this.jobOrdersLoading = false;
                this.cdr.markForCheck();
            }
        });
    }

    select(jo: any): void {
        this.activeModal.close({
            action: 'select',
            data: { ...jo, vendorAccountNo: this.selectedVendor?.accountNo }
        });
    }
}
