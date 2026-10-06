import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { ItemService } from '../item.service';

@Component({
    selector: 'app-item-approval',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    templateUrl: './item-approval.component.html'
})
export class ItemApprovalComponent {
    module    = 'Item Approval';
    subModule = '';
    menuLink  = 'item';

    items         = signal<any[]>([]);
    isLoading     = signal(false);
    pageNumber    = signal(0);
    totalPages    = signal(0);
    pageSize      = 20;
    searchText    = '';

    private service      = inject(ItemService);
    private alertService = inject(AlertService);

    ngOnInit(): void { this.load(); }

    load(page = 0): void {
        this.isLoading.set(true);
        // ponytail: filtering FOR_APPROVAL client-side; add statusId param to /api/item/list when needed
        this.service.list(this.searchText, null, page, this.pageSize).subscribe({
            next: (data) => {
                const forApproval = (data.content ?? []).filter((i: any) => i.documentStatus?.id === 5);
                this.items.set(forApproval);
                this.pageNumber.set(data.page?.number ?? 0);
                this.totalPages.set(data.page?.totalPages ?? 0);
                this.isLoading.set(false);
            },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    search():      void { this.load(0); }
    clearSearch(): void { this.searchText = ''; this.load(0); }
    onPageChange(p: number): void { this.load(p - 1); }
}
