import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { AssemblyUnitService } from '../assembly-unit.service';

@Component({
    selector: 'app-assembly-unit-types-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './assembly-unit-types-main.component.html'
})
export class AssemblyUnitTypesMainComponent {
    module    = 'Assembly Unit';
    subModule = 'Assembly Types';
    menuLink  = 'assembly-unit';

    assemblyTypes  = signal<any[]>([]);
    isLoading      = signal(false);
    searchText     = '';

    private service      = inject(AssemblyUnitService);
    private alertService = inject(AlertService);

    ngOnInit(): void { this.load(); }

    load(): void {
        this.isLoading.set(true);
        this.service.getTypes().subscribe({
            next: (data) => {
                const list: any[] = Array.isArray(data) ? data : (data?.content ?? []);
                const q = this.searchText.toLowerCase().trim();
                this.assemblyTypes.set(q ? list.filter(t => t.description?.toLowerCase().includes(q)) : list);
                this.isLoading.set(false);
            },
            error: () => { this.alertService.error(this.module, 'Load Types', ''); this.isLoading.set(false); }
        });
    }

    search(): void { this.load(); }
    clearSearch(): void { this.searchText = ''; this.load(); }
}
