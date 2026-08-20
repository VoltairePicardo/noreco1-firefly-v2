import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { BudgetSubItemService } from '../budget-sub-item.service';

@Component({
    selector: 'app-budget-sub-item-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './budget-sub-item-main.component.html'
})
export class BudgetSubItemMainComponent {
    module    = 'Budget Sub Item';
    subModule = '';
    menuLink  = 'budget-sub-item';

    treeData  = signal<any[]>([]);
    isLoading = signal(false);

    searchQuery    = '';
    yearFilter     = new Date().getFullYear();
    divisionFilter: any = null;
    divisions: any[] = [];

    private collapsedRows = new Set<number>();

    private service      = inject(BudgetSubItemService);
    private alertService = inject(AlertService);
    private router       = inject(Router);

    ngOnInit(): void {
        this.loadDivisions();
        this.load();
    }

    loadDivisions(): void {
        this.service.getDivisions().subscribe({
            next: (data) => { this.divisions = data || []; },
            error: () => { this.divisions = []; }
        });
    }

    load(): void {
        this.isLoading.set(true);
        this.collapsedRows.clear();
        const divId = this.divisionFilter?.id || null;
        this.service.getTreeData(this.yearFilter, divId, this.searchQuery).subscribe({
            next: (data) => {
                // Enrich each child row with _parentId so collapse filtering works
                let currentParentId: number | null = null;
                const enriched = (data || []).map((row, idx) => {
                    if (row.isHeader) {
                        currentParentId = row.id;
                        return { ...row, _idx: idx };
                    }
                    return { ...row, _parentId: currentParentId, _idx: idx };
                });
                this.treeData.set(enriched);
                this.isLoading.set(false);
            },
            error: () => {
                this.alertService.error(this.module, 'Failed to load records.', '');
                this.isLoading.set(false);
            }
        });
    }

    get visibleRows(): any[] {
        return this.treeData().filter(row => {
            if (row.isHeader) return true;
            return !this.collapsedRows.has(row._parentId);
        });
    }

    toggleRow(parentId: number): void {
        if (this.collapsedRows.has(parentId)) {
            this.collapsedRows.delete(parentId);
        } else {
            this.collapsedRows.add(parentId);
        }
    }

    isCollapsed(parentId: number): boolean {
        return this.collapsedRows.has(parentId);
    }

    onSearch(): void {
        this.load();
    }

    reset(): void {
        this.searchQuery    = '';
        this.yearFilter     = new Date().getFullYear();
        this.divisionFilter = null;
        this.load();
    }

    editSubItems(row: any): void {
        this.router.navigate(['/' + this.menuLink, row.id, 'edit'], {
            state: {
                parentId:    row.id,
                code:        row.code,
                division:    row.division,
                title:       row.title,
                location:    row.location,
                projectCost: row.projectCost
            }
        });
    }

    async deleteSubItem(row: any): Promise<void> {
        const confirmed = await this.alertService.confirm('Delete this budget sub item?');
        if (!confirmed) return;

        this.service.delete(row.id).subscribe({
            next: (res) => {
                if (res?.success || !res?.failureMessage) {
                    this.alertService.success(this.module, 'Budget sub item deleted.', '');
                    this.load();
                } else {
                    this.alertService.error(this.module, res.failureMessage, '');
                }
            },
            error: () => { this.alertService.error(this.module, 'Failed to delete.', ''); }
        });
    }

    compareFn(a: any, b: any): boolean {
        return a && b ? a.id === b.id : a === b;
    }

    get years(): number[] {
        const current = new Date().getFullYear();
        return [current - 1, current, current + 1, current + 2];
    }
}
