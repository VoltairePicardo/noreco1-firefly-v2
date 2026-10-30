import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch } from '@ng-icons/tabler-icons';
import { HttpClient } from '@angular/common/http';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');

@Component({
    selector: 'app-browse-budget-line-item-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, FormsModule],
    providers: [provideIcons({ tablerSearch })],
    templateUrl: './browse-budget-line-item-modal.component.html'
})
export class BrowseBudgetLineItemModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private http = inject(HttpClient);
    private cdr  = inject(ChangeDetectorRef);

    items: any[] = [];
    searchText = '';
    loading = false;

    ngOnInit(): void {
        this.loadData();
    }

    loadData(): void {
        this.loading = true;
        this.http.get<any[]>(`${BASE_API}/json/budget-line-items`).subscribe({
            next: (res) => {
                this.items = res || [];
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
        const q = this.searchText.trim().toLowerCase();
        if (!q) return this.items;
        return this.items.filter(i =>
            (i.code  || '').toLowerCase().includes(q) ||
            (i.title || '').toLowerCase().includes(q)
        );
    }

    select(item: any): void {
        this.activeModal.close({ action: 'select', data: item });
    }
}
