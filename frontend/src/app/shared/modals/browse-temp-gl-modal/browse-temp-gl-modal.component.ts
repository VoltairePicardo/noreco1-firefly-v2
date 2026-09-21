import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { NG_ICON_DIRECTIVES, provideIcons } from '@ng-icons/core';
import { tablerSearch } from '@ng-icons/tabler-icons';
import { GeneralJournalService } from '@/app/pages/general-journal/general-journal.service';

export interface TempGLBatch {
    tempBatchId: number;
    docTypeDesc?: string;
    remarks?: string;
    amount?: number;
    tempBatchDate?: string;
}

@Component({
    selector: 'app-browse-temp-gl-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, ReactiveFormsModule, NG_ICON_DIRECTIVES],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerSearch })],
    templateUrl: './browse-temp-gl-modal.component.html',
})
export class BrowseTempGLModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private service = inject(GeneralJournalService);
    private destroyRef = inject(DestroyRef);

    protected readonly searchControl = new FormControl('', { nonNullable: true });

    private readonly searchTerm = signal('');
    protected readonly batches = signal<TempGLBatch[]>([]);
    protected readonly loading = signal(false);
    protected readonly selecting = signal(false);

    protected readonly filteredBatches = computed(() => {
        const q = this.searchTerm().trim().toLowerCase();
        if (!q) return this.batches();
        return this.batches().filter(b =>
            (b.docTypeDesc || '').toLowerCase().includes(q) ||
            (b.remarks || '').toLowerCase().includes(q));
    });

    protected readonly hasResults = computed(() => this.filteredBatches().length > 0);

    ngOnInit(): void {
        this.searchControl.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(value => this.searchTerm.set(value));
        this.loadData();
    }

    protected select(batch: TempGLBatch): void {
        this.selecting.set(true);
        this.service.getTempGLEntries(batch.tempBatchId).subscribe({
            next: (entries) => {
                this.selecting.set(false);
                this.activeModal.close({ action: 'select', data: batch, entries: entries || [] });
            },
            error: () => { this.selecting.set(false); },
        });
    }

    private loadData(): void {
        this.loading.set(true);
        this.service.getTempBatches().subscribe({
            next: (data) => { this.batches.set(data || []); this.loading.set(false); },
            error: () => { this.batches.set([]); this.loading.set(false); },
        });
    }
}
