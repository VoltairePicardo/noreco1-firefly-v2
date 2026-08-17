import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { ItemService } from '../item.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseCOAModalComponent } from '@/app/shared/modals/browse-coa-modal/browse-coa-modal.component';
import Swal from 'sweetalert2';

@Component({
    selector: 'app-item-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './item-main.component.html'
})
export class ItemMainComponent {
    module    = 'Item';
    subModule = '';
    menuLink  = 'item';

    items         = signal<any[]>([]);
    isLoading     = signal(false);
    pageNumber    = signal(0);
    totalPages    = signal(0);
    totalElements = signal(0);
    pageSize      = 10;
    searchText    = '';
    selectedAccount: any = null;

    private service      = inject(ItemService);
    private alertService = inject(AlertService);
    private modalService = inject(ModalService);

    ngOnInit(): void { this.load(); }

    load(page = 0): void {
        this.isLoading.set(true);
        this.service.list(this.searchText, this.selectedAccount?.id ?? null, page).subscribe({
            next: (data) => {
                this.items.set(data.content);
                this.pageNumber.set(data.page.number);
                this.totalPages.set(data.page.totalPages);
                this.totalElements.set(data.page.totalElements);
                this.isLoading.set(false);
            },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    search():      void { this.load(0); }
    clearSearch(): void { this.searchText = ''; this.selectedAccount = null; this.load(0); }
    onPageChange(p: number): void { this.load(p - 1); }

    async openAccountBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseCOAModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this.selectedAccount = result.data;
            }
        } catch { }
    }

    clearAccount(): void { this.selectedAccount = null; }

    delete(id: number, description: string): void {
        Swal.fire({
            title: 'Delete Item?',
            text: `Are you sure you want to delete "${description}"?`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Yes, delete',
            confirmButtonColor: '#d33',
            cancelButtonText: 'Cancel'
        }).then(result => {
            if (result.isConfirmed) {
                this.service.remove(id).subscribe({
                    next: (res) => {
                        if (res.success) {
                            Swal.fire({ title: 'Deleted!', text: res.successMessage, icon: 'success', timer: 2000, showConfirmButton: false });
                            this.load(this.pageNumber());
                        } else {
                            Swal.fire({ title: 'Error', text: res.failureMessage, icon: 'error' });
                        }
                    },
                    error: () => Swal.fire({ title: 'Error', text: 'Failed to delete item.', icon: 'error' })
                });
            }
        });
    }
}
