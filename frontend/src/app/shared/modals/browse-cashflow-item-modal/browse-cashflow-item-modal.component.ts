import { Component, inject, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-browse-cashflow-item-modal',
    imports: [...COMMON_ALL_PAGE_IMPORTS, FormsModule],
    providers: [provideIcons({ tablerSearch })],
    templateUrl: './browse-cashflow-item-modal.component.html'
})
export class BrowseCashflowItemModalComponent {
    activeModal = inject(NgbActiveModal);

    @Input() items: any[] = [];
    searchText = '';

    get filtered(): any[] {
        const q = this.searchText.trim().toLowerCase();
        if (!q) return this.items;
        return this.items.filter(i =>
            (i.name                       || '').toLowerCase().includes(q) ||
            (i.cashflowItemType?.name     || '').toLowerCase().includes(q) ||
            (i.parentCashflowItem?.name   || '').toLowerCase().includes(q)
        );
    }

    select(item: any): void {
        this.activeModal.close({ action: 'select', data: item });
    }
}
