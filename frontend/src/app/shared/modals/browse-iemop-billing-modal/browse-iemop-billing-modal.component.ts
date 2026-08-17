import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, signal } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch } from '@ng-icons/tabler-icons';
import { IemopBillingService } from '@/app/pages/iemop-billing/iemop-billing.service';

@Component({
    selector: 'app-browse-iemop-billing-modal',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerSearch })],
    changeDetection: ChangeDetectionStrategy.OnPush,
    templateUrl: './browse-iemop-billing-modal.component.html'
})
export class BrowseIemopBillingModalComponent {
    searchText  = '';
    items       = signal<any[]>([]);
    page        = 1;
    pageSize    = 10;
    totalItems  = 0;
    isLoading   = signal(false);

    private activeModal = inject(NgbActiveModal);
    private service     = inject(IemopBillingService);
    private cdr         = inject(ChangeDetectorRef);

    ngOnInit(): void { this.search(); }

    search(): void {
        this.page = 1;
        this.isLoading.set(true);
        this.service.list(0, this.pageSize, this.searchText).subscribe({
            next: (res: any) => {
                this.items.set(res?.content || (Array.isArray(res) ? res : []));
                this.totalItems = res?.totalElements ?? (Array.isArray(res) ? res.length : 0);
                this.isLoading.set(false);
                this.cdr.markForCheck();
            },
            error: () => { this.items.set([]); this.isLoading.set(false); this.cdr.markForCheck(); }
        });
    }

    onPageChange(): void {
        this.isLoading.set(true);
        this.service.list(this.page - 1, this.pageSize, this.searchText).subscribe({
            next: (res: any) => {
                this.items.set(res?.content || (Array.isArray(res) ? res : []));
                this.totalItems = res?.totalElements ?? 0;
                this.isLoading.set(false);
                this.cdr.markForCheck();
            },
            error: () => { this.items.set([]); this.isLoading.set(false); this.cdr.markForCheck(); }
        });
    }

    select(item: any): void { this.activeModal.close({ action: 'select', data: item }); }
    close(): void { this.activeModal.dismiss(); }
}
