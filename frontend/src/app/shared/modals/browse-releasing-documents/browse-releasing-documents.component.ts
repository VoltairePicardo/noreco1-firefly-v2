import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { NG_ICON_DIRECTIVES, provideIcons } from '@ng-icons/core';
import { tablerChevronLeft, tablerChevronRight, tablerSearch } from '@ng-icons/tabler-icons';
import { StockReleaseService } from '@/app/pages/inventory/stock-release/stock-release.service';
import { InventoryDocumentDto, ReleasingDocumentType } from '@/app/models/inventory-document.model';

const DOCUMENT_TYPE_LABELS: Record<ReleasingDocumentType, string> = {
    ST: 'Stock Transfer',
    SW: 'Stock Withdrawal',
    MR: 'Memorandum Receipt',
};

const DOCUMENT_TYPE_OPTIONS: { value: ReleasingDocumentType; label: string }[] =
    (Object.keys(DOCUMENT_TYPE_LABELS) as ReleasingDocumentType[]).map(value => ({ value, label: DOCUMENT_TYPE_LABELS[value] }));

@Component({
    selector: 'app-browse-releasing-documents-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, ReactiveFormsModule, NG_ICON_DIRECTIVES],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerChevronLeft, tablerChevronRight, tablerSearch })],
    templateUrl: './browse-releasing-documents.component.html',
})
export class BrowseReleasingDocumentsModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private service = inject(StockReleaseService);
    private destroyRef = inject(DestroyRef);

    documentType: ReleasingDocumentType = 'SW';

    protected readonly typeOptions = DOCUMENT_TYPE_OPTIONS;
    protected readonly typeControl = new FormControl<ReleasingDocumentType>('SW', { nonNullable: true });
    protected readonly searchControl = new FormControl('', { nonNullable: true });

    protected readonly documents = signal<InventoryDocumentDto[]>([]);
    protected readonly total = signal(0);
    protected readonly page = signal(1);
    protected readonly pageSize = signal(10);
    protected readonly loading = signal(false);

    protected readonly hasResults = computed(() => this.documents().length > 0);

    protected title(): string {
        return `Browse ${DOCUMENT_TYPE_LABELS[this.typeControl.value]}`;
    }

    ngOnInit(): void {
        this.typeControl.setValue(this.documentType, { emitEvent: false });
        this.typeControl.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
            this.page.set(1);
            this.loadData();
        });
        this.loadData();
    }

    protected onSearch(): void {
        this.page.set(1);
        this.loadData();
    }

    protected onPageChange(page: number): void {
        this.page.set(page);
        this.loadData();
    }

    protected select(document: InventoryDocumentDto): void {
        this.activeModal.close({ action: 'select', data: document, documentType: this.typeControl.value });
    }

    private loadData(): void {
        this.loading.set(true);
        this.service.getReleasingDocuments(this.typeControl.value, this.searchControl.value.trim(), this.page() - 1, this.pageSize())
            .subscribe({
                next: ({ content, totalElements }) => {
                    this.documents.set(content);
                    this.total.set(totalElements);
                    this.loading.set(false);
                },
                error: () => {
                    this.documents.set([]);
                    this.total.set(0);
                    this.loading.set(false);
                },
            });
    }
}
