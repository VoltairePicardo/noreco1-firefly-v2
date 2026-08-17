import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerChevronLeft, tablerChevronRight, tablerSearch } from '@ng-icons/tabler-icons';
import { MiscChargeService } from '@/app/pages/misc-charge/misc-charge.service';

@Component({
    selector: 'app-browse-misc-charge-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [
        ...SHARED_PROVIDERS,
        provideIcons({ tablerChevronLeft, tablerChevronRight, tablerSearch })
    ],
    templateUrl: './browse-misc-charge-modal.component.html'
})
export class BrowseMiscChargeModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private service = inject(MiscChargeService);
    private cdr = inject(ChangeDetectorRef);

    items: any[] = [];
    total = 0;
    page = 1;
    pageSize = 10;
    searchText = '';
    loading = false;

    ngOnInit(): void {
        this.loadData();
    }

    loadData(): void {
        if (this.loading) return;
        this.loading = true;
        this.service.list(this.searchText, this.page - 1, this.pageSize).subscribe({
            next: (res) => {
                this.items = res.content ?? res ?? [];
                this.total = res.totalElements ?? res.page?.totalElements ?? this.items.length;
                this.loading = false;
                this.cdr.markForCheck();
            },
            error: () => {
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
        this.activeModal.close({ action: 'select', data: item });
    }

    close(): void {
        this.activeModal.dismiss('closed');
    }
}
