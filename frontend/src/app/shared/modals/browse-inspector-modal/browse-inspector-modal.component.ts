import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch } from '@ng-icons/tabler-icons';
import { JoAcceptanceService } from '@/app/pages/jo-acceptance/jo-acceptance.service';

@Component({
    selector: 'app-browse-inspector-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerSearch })],
    templateUrl: './browse-inspector-modal.component.html'
})
export class BrowseInspectorModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private service = inject(JoAcceptanceService);
    private cdr = inject(ChangeDetectorRef);

    entities: any[] = [];
    loading = false;
    searchText = '';

    ngOnInit(): void {
        this.loading = true;
        this.service.getEntities().subscribe({
            next: (e) => {
                this.entities = e || [];
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
        if (!q) return this.entities;
        return this.entities.filter(e => e.name?.toLowerCase().includes(q));
    }

    select(entity: any): void {
        this.activeModal.close({ action: 'select', data: entity });
    }
}
