import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Observable } from 'rxjs';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { NG_ICON_DIRECTIVES, provideIcons } from '@ng-icons/core';
import { tablerChevronLeft, tablerChevronRight, tablerSearch } from '@ng-icons/tabler-icons';
import { GeneralJournalService } from '@/app/pages/general-journal/general-journal.service';
import { JV_VOUCHER_TYPE } from '@/app/constants/app.constants';

export type JvVoucherTypeKey = keyof typeof JV_VOUCHER_TYPE;

export interface BrowseDocumentItem {
    id?: number;
    transactionId?: number;
    localCode?: string;
    code?: string;
    netAmount?: number;
    totalReturnedAmount?: number;
    amount?: number;
    voucherDate?: string;
    particulars?: string;
    remarks?: string;
    preparedBy?: string;
    createdBy?: { name?: string; fullName?: string };
}

const DOCUMENT_TYPE_OPTIONS: { value: JvVoucherTypeKey; label: string }[] =
    (Object.keys(JV_VOUCHER_TYPE) as JvVoucherTypeKey[]).map(value => ({ value, label: JV_VOUCHER_TYPE[value].description }));

@Component({
    selector: 'app-browse-document-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, ReactiveFormsModule, NG_ICON_DIRECTIVES],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerChevronLeft, tablerChevronRight, tablerSearch })],
    templateUrl: './browse-document-modal.component.html',
})
export class BrowseDocumentModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private service = inject(GeneralJournalService);
    private destroyRef = inject(DestroyRef);

    documentType: JvVoucherTypeKey = 'MCT';

    protected readonly typeOptions = DOCUMENT_TYPE_OPTIONS;
    protected readonly typeControl = new FormControl<JvVoucherTypeKey>('MCT', { nonNullable: true });
    protected readonly searchControl = new FormControl('', { nonNullable: true });

    protected readonly documents = signal<BrowseDocumentItem[]>([]);
    protected readonly total = signal(0);
    protected readonly page = signal(1);
    protected readonly pageSize = signal(10);
    protected readonly loading = signal(false);

    protected readonly hasResults = computed(() => this.documents().length > 0);

    protected title(): string {
        return `Browse ${JV_VOUCHER_TYPE[this.typeControl.value].description}`;
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

    protected select(document: BrowseDocumentItem): void {
        this.activeModal.close({ action: 'select', data: document, documentType: this.typeControl.value });
    }

    private loadData(): void {
        this.loading.set(true);
        this.requestFor(this.typeControl.value, this.searchControl.value.trim(), this.page() - 1, this.pageSize())
            .subscribe({
                next: ({ content, totalElements }) => {
                    this.documents.set(content ?? []);
                    this.total.set(totalElements ?? 0);
                    this.loading.set(false);
                },
                error: () => {
                    this.documents.set([]);
                    this.total.set(0);
                    this.loading.set(false);
                },
            });
    }

    private requestFor(type: JvVoucherTypeKey, q: string, page: number, size: number): Observable<any> {
        switch (type) {
            case 'MCT': return this.service.getMctForJv(q, page, size);
            case 'SA':  return this.service.getStockAdjustmentForJv(q, page, size);
            case 'CAL': return this.service.getCalForJv(q, page, size);
            case 'RR':  return this.service.getRrForJv(q, page, size);
        }
    }
}
