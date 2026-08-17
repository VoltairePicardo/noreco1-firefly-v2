import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { BrowseSupplierModalComponent } from '@/app/shared/modals/browse-supplier-modal/browse-supplier-modal.component';
import { SubSupplierService } from '../sub-supplier.service';

@Component({
    selector: 'app-sub-supplier-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './sub-supplier-main.component.html'
})
export class SubSupplierMainComponent {
    module    = 'Sub-Supplier';
    subModule = '';
    menuLink  = 'sub-supplier';

    subSuppliers  = signal<any[]>([]);
    isLoading     = signal(false);
    pageNumber    = signal(0);
    totalElements = signal(0);
    pageSize = 10;
    searchText    = '';
    selectedSupplier: any = null;

    private service      = inject(SubSupplierService);
    private alertService = inject(AlertService);
    private modalService = inject(NgbModal);

    ngOnInit(): void { this.load(); }

    load(page = 0): void {
        this.isLoading.set(true);
        this.service.list(this.searchText, this.selectedSupplier?.id ?? null, page).subscribe({
            next: (data) => {
                this.subSuppliers.set(data.content ?? []);
                this.pageNumber.set(data.page?.number ?? 0);
                this.totalElements.set(data.page?.totalElements ?? 0);
                this.isLoading.set(false);
            },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    search():      void { this.load(0); }
    clearSearch(): void { this.searchText = ''; this.selectedSupplier = null; this.load(0); }
    onPageChange(p: number): void { this.load(p - 1); }

    openSupplierBrowse(): void {
        const ref = this.modalService.open(BrowseSupplierModalComponent, { size: 'lg', centered: true });
        ref.result.then((result) => {
            if (result?.action === 'select') {
                this.selectedSupplier = result.data;
            }
        }).catch(() => {});
    }

    clearSupplier(): void {
        this.selectedSupplier = null;
    }
}
