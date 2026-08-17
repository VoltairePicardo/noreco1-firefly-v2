import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch } from '@ng-icons/tabler-icons';
import { MctService } from '@/app/pages/mct/mct.service';

@Component({
    selector: 'app-browse-mct-stock-release-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerSearch })],
    templateUrl: './browse-mct-stock-release-modal.component.html'
})
export class BrowseMctStockReleaseModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private service = inject(MctService);
    private cdr = inject(ChangeDetectorRef);

    items: any[] = [];
    loading = false;
    searchText = '';

    ngOnInit(): void {
        this.loading = true;
        this.service.getStockReleasesForMct().subscribe({
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

    get filtered(): any[] {
        const q = this.searchText.toLowerCase();
        if (!q) return this.items;
        return this.items.filter(sr =>
            sr.code?.toLowerCase().includes(q) ||
            sr.inventoryLocation?.description?.toLowerCase().includes(q) ||
            sr.inventoryLocation?.name?.toLowerCase().includes(q) ||
            sr.createdBy?.fullName?.toLowerCase().includes(q)
        );
    }

    select(stockRelease: any): void {
        this.activeModal.close({ action: 'select', data: stockRelease });
    }
}
