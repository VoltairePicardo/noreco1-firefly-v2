import { Component, ChangeDetectionStrategy, inject } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-browse-mr-employee-items-modal',
    templateUrl: './browse-mr-employee-items-modal.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, FormsModule]
})
export class BrowseMrEmployeeItemsModalComponent {
    employee: any  = null;
    items:    any[] = [];
    quantities: number[] = [];

    private activeModal = inject(NgbActiveModal);

    ngOnInit(): void {
        this.quantities = this.items.map(() => 0);
    }

    confirm(): void {
        const assigned = this.items
            .map((item, i) => ({ item, qty: this.quantities[i] }))
            .filter(x => x.qty > 0)
            .map(x => ({
                stockWithdrawalDetail: x.item.stockWithdrawalDetail,
                itemCode:              x.item.itemCode,
                itemDescription:       x.item.itemDescription,
                unitCode:              x.item.unitCode,
                quantity:              x.qty
            }));
        this.activeModal.close({ action: 'select', data: assigned });
    }

    dismiss(): void { this.activeModal.dismiss(); }
}
