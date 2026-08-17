import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, signal } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { HttpClient } from '@angular/common/http';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch } from '@ng-icons/tabler-icons';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');

@Component({
    selector: 'app-browse-temp-gl-modal',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerSearch })],
    changeDetection: ChangeDetectionStrategy.OnPush,
    templateUrl: './browse-temp-gl-modal.component.html'
})
export class BrowseTempGLModalComponent {
    searchText  = '';
    batches     = signal<any[]>([]);
    isLoading   = signal(false);
    isSelecting = signal(false);

    private activeModal = inject(NgbActiveModal);
    private http        = inject(HttpClient);
    private cdr         = inject(ChangeDetectorRef);

    ngOnInit(): void {
        this.load();
    }

    load(): void {
        this.isLoading.set(true);
        this.http.get<any[]>(`${BASE_API}/ledger/temp/all`).subscribe({
            next: (data) => { this.batches.set(data || []); this.isLoading.set(false); this.cdr.markForCheck(); },
            error: () => { this.batches.set([]); this.isLoading.set(false); this.cdr.markForCheck(); }
        });
    }

    get filtered(): any[] {
        if (!this.searchText.trim()) return this.batches();
        const q = this.searchText.toLowerCase();
        return this.batches().filter(b =>
            (b.docTypeDesc || '').toLowerCase().includes(q) ||
            (b.remarks     || '').toLowerCase().includes(q)
        );
    }

    select(batch: any): void {
        this.isSelecting.set(true);
        this.http.get<any[]>(`${BASE_API}/ledger/temp/gl/${batch.tempBatchId}`).subscribe({
            next: (entries) => {
                this.isSelecting.set(false);
                this.activeModal.close({ action: 'select', data: { batch, entries: entries || [] } });
            },
            error: () => {
                this.isSelecting.set(false);
                this.cdr.markForCheck();
            }
        });
    }

    close(): void { this.activeModal.dismiss(); }
}
