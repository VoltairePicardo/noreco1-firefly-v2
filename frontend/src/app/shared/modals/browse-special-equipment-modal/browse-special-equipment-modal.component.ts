import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerChevronLeft, tablerChevronRight, tablerSearch } from '@ng-icons/tabler-icons';
import { OtherSpecialEquipmentTestingService } from '@/app/pages/special-equipment-testing/other-special-equipment/other-special-equipment-testing.service';
import { OtherSpecialEquipmentTestingSpecialEquipment } from '@/app/models/special-equipment-testing/other-special-equipment-testing.model';

@Component({
    selector: 'app-browse-special-equipment-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerChevronLeft, tablerChevronRight, tablerSearch })],
    templateUrl: './browse-special-equipment-modal.component.html'
})
export class BrowseSpecialEquipmentModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private service = inject(OtherSpecialEquipmentTestingService);
    private cdr = inject(ChangeDetectorRef);

    items: OtherSpecialEquipmentTestingSpecialEquipment[] = [];
    total = 0;
    page = 1;
    pageSize = 10;
    searchText = '';
    loading = false;

    ngOnInit(): void {
        this.loadData();
    }

    loadData(): void {
        if (this.loading) return;
        this.loading = true;
        this.service.searchSpecialEquipment(this.searchText, this.page - 1, this.pageSize).subscribe({
            next: (res: any) => {
                this.items = res.content ?? res ?? [];
                this.total = res.totalElements ?? this.items.length;
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

    select(item: OtherSpecialEquipmentTestingSpecialEquipment): void {
        this.activeModal.close({ action: 'select', data: item });
    }
}
