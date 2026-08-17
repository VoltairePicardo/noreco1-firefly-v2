import { Component, computed, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { RolesService } from '../roles.service';

@Component({
    selector: 'app-roles-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './roles-main.component.html'
})
export class RolesMainComponent {
    module    = 'Role';
    subModule = '';
    menuLink  = 'roles';

    roles      = signal<any[]>([]);
    isLoading  = signal(false);
    filterText = signal('');
    searchText = '';

    filteredRoles = computed(() => {
        const q = this.filterText().toLowerCase();
        return q ? this.roles().filter(r => r.name?.toLowerCase().includes(q)) : this.roles();
    });

    private service      = inject(RolesService);
    private alertService = inject(AlertService);

    ngOnInit(): void { this.load(); }

    load(): void {
        this.isLoading.set(true);
        this.service.list().subscribe({
            next:  (data) => { this.roles.set(data); this.isLoading.set(false); },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    search():      void { this.filterText.set(this.searchText); }
    clearSearch(): void { this.searchText = ''; this.filterText.set(''); }
}
