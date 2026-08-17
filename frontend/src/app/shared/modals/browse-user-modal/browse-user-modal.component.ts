import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerChevronLeft, tablerChevronRight, tablerSearch } from '@ng-icons/tabler-icons';
import { HttpClient } from '@angular/common/http';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');

@Component({
    selector: 'app-browse-user-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerChevronLeft, tablerChevronRight, tablerSearch })],
    templateUrl: './browse-user-modal.component.html'
})
export class BrowseUserModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private http = inject(HttpClient);
    private cdr  = inject(ChangeDetectorRef);

    items: any[] = [];
    total      = 0;
    page       = 1;
    pageSize   = 10;
    searchText = '';
    loading    = false;

    ngOnInit(): void {
        this.loadData();
    }

    loadData(): void {
        if (this.loading) return;
        this.loading = true;
        this.http.get<any>(`${BASE_API}/user/signatories`, {
            params: { searchText: this.searchText, page: this.page - 1, size: this.pageSize }
        }).subscribe({
            next: (res) => {
                this.items = res.content ?? [];
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

    select(user: any): void {
        this.activeModal.close({ action: 'select', data: { accountNo: user.accountNo, name: user.fullName } });
    }
}
