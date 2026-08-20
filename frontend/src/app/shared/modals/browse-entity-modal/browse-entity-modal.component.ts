import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, Input, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerChevronLeft, tablerChevronRight, tablerSearch } from '@ng-icons/tabler-icons';
import { GeneralJournalService } from '@/app/pages/general-journal/general-journal.service';

@Component({
    selector: 'app-browse-entity-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerChevronLeft, tablerChevronRight, tablerSearch })],
    templateUrl: './browse-entity-modal.component.html'
})
export class BrowseEntityModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private service = inject(GeneralJournalService);
    private cdr = inject(ChangeDetectorRef);

    @Input() entityTypes?: number[];
    @Input() defaultClassificationId?: number;

    items: any[] = [];
    classifications: any[] = [];
    selectedClassification: string | null = null;
    total = 0;
    page = 1;
    pageSize = 10;
    searchText = '';
    loading = false;

    ngOnInit(): void {
        this.service.getEntityClassifications().subscribe({
            next: (res) => {
                this.classifications = res;
                if (this.defaultClassificationId != null) {
                    const match = res.find(c => c.id === this.defaultClassificationId);
                    if (match) {
                        this.selectedClassification = match.description;
                    }
                }
                this.loadData();
                this.cdr.markForCheck();
            },
            error: () => {
                this.loadData();
            }
        });
    }

    onClassificationChange(): void {
        this.page = 1;
        this.loadData();
    }

    loadData(): void {
        if (this.loading) return;
        this.loading = true;
        this.service.getEntities(this.searchText, this.page - 1, this.pageSize, this.entityTypes, this.selectedClassification).subscribe({
            next: (res) => {
                this.items = res.content ?? res ?? [];
                this.total = res.totalElements ?? res.page?.totalElements ?? this.items.length;
                this.loading = false;
                if (this.items.length === 1) { this.select(this.items[0]); return; }
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
}
