import { Component, computed, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { SectionService } from '../section.service';

@Component({
    selector: 'app-section-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './section-main.component.html'
})
export class SectionMainComponent {
    module    = 'Section';
    subModule = '';
    menuLink  = 'section';

    allData = signal<any[]>([]);

    page     = 1;
    pageSize = 10;

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.filteredData().slice(start, start + this.pageSize);
    }

    searchText = '';
    isLoading  = signal(false);

    filteredData = computed(() => {
        const q = this.searchText.toLowerCase();
        return q
            ? this.allData().filter(d =>
                d.name?.toLowerCase().includes(q) ||
                d.division?.name?.toLowerCase().includes(q))
            : this.allData();
    });

    private service      = inject(SectionService);
    private alertService = inject(AlertService);

    ngOnInit(): void { this.load(); }

    load(): void {
        this.isLoading.set(true);
        this.service.list().subscribe({
            next:  (data) => { this.allData.set(data);
            this.page = 1; this.isLoading.set(false); },
            error: ()     => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    search():      void { this.allData.set([...this.allData()]);
            this.page = 1; }
    clearSearch(): void { this.searchText = ''; this.allData.set([...this.allData()]);
            this.page = 1; }
}
