import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, Input, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { PurchaseOrderService } from '@/app/pages/purchase-order/purchase-order.service';
import { JobOrderService } from '@/app/pages/job-order/job-order.service';

@Component({
    selector: 'app-browse-rv-items-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './browse-rv-items-modal.component.html'
})
export class BrowseRvItemsModalComponent implements OnInit {
    activeModal   = inject(NgbActiveModal);
    private poSvc = inject(PurchaseOrderService);
    private joSvc = inject(JobOrderService);
    private cdr   = inject(ChangeDetectorRef);

    /** Pass 'jo' to load work request items, 'quotation' for canvassed items; defaults to PO/canvass items. */
    @Input() type: 'po' | 'jo' | 'quotation' = 'po';
    /** Override modal title. */
    @Input() title = 'Browse RV Items';

    items: any[] = [];
    loading = false;
    selected = new Set<number>();

    ngOnInit(): void {
        this.loading = true;
        const loader$ = this.type === 'jo'
            ? this.joSvc.getRvDetailsForJo()
            : this.type === 'quotation'
                ? this.poSvc.getRvDetailsForQuotation()
                : this.poSvc.getRvDetailsForPo();

        loader$.subscribe({
            next: (data) => {
                this.items = data || [];
                this.loading = false;
                this.cdr.markForCheck();
            },
            error: () => {
                this.loading = false;
                this.cdr.markForCheck();
            }
        });
    }

    toggle(id: number): void {
        if (this.selected.has(id)) {
            this.selected.delete(id);
        } else {
            this.selected.add(id);
        }
    }

    isSelected(id: number): boolean {
        return this.selected.has(id);
    }

    confirm(): void {
        const selectedItems = this.items.filter(item => this.selected.has(item.id));
        this.activeModal.close({ action: 'select', data: selectedItems });
    }
}
