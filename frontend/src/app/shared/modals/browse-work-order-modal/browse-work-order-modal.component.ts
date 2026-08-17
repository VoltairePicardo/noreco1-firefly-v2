import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch } from '@ng-icons/tabler-icons';
import { WorkOrderService } from '@/app/pages/work-order/work-order.service';

@Component({
    selector: 'app-browse-work-order-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerSearch })],
    templateUrl: './browse-work-order-modal.component.html'
})
export class BrowseWorkOrderModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private service = inject(WorkOrderService);
    private cdr = inject(ChangeDetectorRef);

    workOrders: any[] = [];
    loading = false;
    searchText = '';

    ngOnInit(): void {
        this.loading = true;
        this.service.list(null, null, null, 0, 200).subscribe({
            next: (data) => {
                this.workOrders = data?.content ?? [];
                this.loading = false;
                this.cdr.markForCheck();
            },
            error: () => {
                this.loading = false;
                this.cdr.markForCheck();
            }
        });
    }

    get filtered(): any[] {
        const q = this.searchText.toLowerCase();
        if (!q) return this.workOrders;
        return this.workOrders.filter(wo =>
            wo.code?.toLowerCase().includes(q) ||
            wo.project?.name?.toLowerCase().includes(q) ||
            wo.description?.toLowerCase().includes(q)
        );
    }

    select(workOrder: any): void {
        this.activeModal.close({ action: 'select', data: workOrder });
    }
}
