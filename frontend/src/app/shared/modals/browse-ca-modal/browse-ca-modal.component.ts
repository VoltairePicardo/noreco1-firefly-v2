import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { CaService } from '@/app/pages/ca/ca.service';

@Component({
    selector: 'app-browse-ca-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './browse-ca-modal.component.html'
})
export class BrowseCaModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private service = inject(CaService);
    private cdr = inject(ChangeDetectorRef);

    items: any[] = [];
    loading = false;

    ngOnInit(): void {
        this.loading = true;
        this.service.list().subscribe({
            next: (data) => { this.items = data || []; this.loading = false; this.cdr.markForCheck(); },
            error: () => { this.loading = false; this.cdr.markForCheck(); }
        });
    }

    select(item: any): void {
        this.activeModal.close({ action: 'select', data: item });
    }
}
