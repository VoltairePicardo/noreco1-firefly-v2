import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { SharedModule } from '@/app/shared/shared.module';
import { RolesService } from '../roles.service';

@Component({
    selector: 'app-roles-details',
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule],
    templateUrl: './roles-details.component.html'
})
export class RolesDetailsComponent {
    module    = 'Role';
    subModule = 'Details';
    menuLink  = 'roles';
    id: any   = 0;
    data: any = {};
    isLoading = signal(false);

    private service      = inject(RolesService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.id = params.get('id');
            if (this.id && /^\d+$/.test(String(this.id))) {
                this.isLoading.set(true);
                this.service.getData(this.id).subscribe({
                    next: (data) => {
                        this.isLoading.set(false);
                        if (data?.id) { this.data = data; }
                        else { this.alertService.error(this.module, 'Not Found', ''); this.router.navigate(['/' + this.menuLink]); }
                    },
                    error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Error', ''); this.router.navigate(['/' + this.menuLink]); }
                });
            }
        });
    }

    get groupedMenus(): { parentId: number | null; items: any[] }[] {
        const menus: any[] = this.data?.menus || [];
        const map = new Map<number | null, any[]>();
        menus.forEach((m: any) => {
            const key = m.parentMenu?.id ?? null;
            if (!map.has(key)) map.set(key, []);
            map.get(key)!.push(m);
        });
        return Array.from(map.entries()).map(([parentId, items]) => ({ parentId, items }));
    }
}
