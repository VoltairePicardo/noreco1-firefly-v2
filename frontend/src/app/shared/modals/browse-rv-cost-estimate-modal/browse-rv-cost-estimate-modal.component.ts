import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, Input, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerChevronLeft, tablerChevronRight, tablerSearch } from '@ng-icons/tabler-icons';
import { WithdrawalService } from '@/app/pages/inventory/withdrawal/withdrawal.service';

export type RvCostEstimateDocType = 'rv' | 'ce';

@Component({
    selector: 'app-browse-rv-cost-estimate-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerChevronLeft, tablerChevronRight, tablerSearch })],
    templateUrl: './browse-rv-cost-estimate-modal.component.html'
})
export class BrowseRvCostEstimateModalComponent implements OnInit {
    /** Inventory location the withdrawal is being created for — required to scope the RV list. */
    @Input() locationId!: number;

    activeModal      = inject(NgbActiveModal);
    private service  = inject(WithdrawalService);
    private cdr      = inject(ChangeDetectorRef);

    docType    : RvCostEstimateDocType = 'rv';
    items      : any[] = [];
    total      = 0;
    page       = 1;
    pageSize   = 10;
    searchText = '';
    loading    = false;

    ngOnInit(): void {
        this.loadData();
    }

    switchType(type: RvCostEstimateDocType): void {
        if (this.docType === type) return;
        this.docType    = type;
        this.searchText = '';
        this.page       = 1;
        this.loadData();
    }

    loadData(): void {
        this.loading = true;
        const loader$ = this.docType === 'rv'
            ? this.service.listRvForWithdrawal(this.locationId, this.searchText, this.page - 1, this.pageSize)
            : this.service.listCostEstimateForWithdrawal(this.locationId, this.searchText, this.page - 1, this.pageSize);

        loader$.subscribe({
            next: (res) => {
                this.items   = res?.content ?? [];
                this.total   = res?.totalElements ?? this.items.length;
                this.loading = false;
                this.cdr.markForCheck();
            },
            error: () => {
                this.items   = [];
                this.total   = 0;
                this.loading = false;
                this.cdr.markForCheck();
            }
        });
    }

    onSearchChange(): void {
        this.page = 1;
        this.loadData();
    }

    select(item: any): void {
        this.activeModal.close({ action: 'select', data: { type: this.docType, item } });
    }
}
