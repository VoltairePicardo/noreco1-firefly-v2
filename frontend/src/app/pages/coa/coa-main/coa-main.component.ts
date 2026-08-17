import { Component, inject, signal, computed } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { DownloadService } from '@/app/core/services/download.service';
import { environment } from '@/environments/environment';
import { CoaService } from '../coa.service';

@Component({
    selector: 'app-coa-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './coa-main.component.html'
})
export class CoaMainComponent {
    module    = 'Chart of Accounts';
    subModule = '';
    menuLink  = 'coa';

    accounts     = signal<any[]>([]);
    isLoading    = signal(false);
    collapsedIds = signal<Set<number>>(new Set());
    searchText   = '';

    nodeMap        = new Map<number, any>();
    hasChildrenMap = new Map<number, boolean>();
    depthMap       = new Map<number, number>();

    private service         = inject(CoaService);
    private alertService    = inject(AlertService);
    private downloadService = inject(DownloadService);

    filtered = computed(() => {
        const q    = this.searchText.toLowerCase().trim();
        const data = this.accounts();
        if (q) {
            return data.filter(a =>
                (a.code  && a.code.toLowerCase().includes(q)) ||
                (a.title && a.title.toLowerCase().includes(q))
            );
        }
        const collapsed = this.collapsedIds();
        return data.filter(a => this.isVisibleNode(a, collapsed));
    });

    ngOnInit(): void { this.load(); }

    load(): void {
        this.isLoading.set(true);
        this.service.list().subscribe({
            next: (data) => {
                const arr = Array.isArray(data) ? data : [];
                this.buildMaps(arr);
                this.accounts.set(arr);
                this.isLoading.set(false);
            },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    buildMaps(data: any[]): void {
        this.nodeMap.clear();
        this.hasChildrenMap.clear();
        this.depthMap.clear();
        for (const node of data) {
            this.nodeMap.set(node.id, node);
            if (node.parentAccountId && node.parentAccountId > 0) {
                this.hasChildrenMap.set(node.parentAccountId, true);
            }
        }
        for (const node of data) {
            this.depthMap.set(node.id, this.computeDepth(node));
        }
    }

    computeDepth(node: any): number {
        let depth = 0;
        let parentId = node.parentAccountId;
        while (parentId && parentId > 0) {
            depth++;
            const parent = this.nodeMap.get(parentId);
            if (!parent) break;
            parentId = parent.parentAccountId;
        }
        return depth;
    }

    isVisibleNode(node: any, collapsed: Set<number>): boolean {
        let parentId = node.parentAccountId;
        while (parentId && parentId > 0) {
            if (collapsed.has(parentId)) return false;
            const parent = this.nodeMap.get(parentId);
            if (!parent) break;
            parentId = parent.parentAccountId;
        }
        return true;
    }

    toggleNode(node: any, event: Event): void {
        event.stopPropagation();
        const next = new Set(this.collapsedIds());
        next.has(node.id) ? next.delete(node.id) : next.add(node.id);
        this.collapsedIds.set(next);
    }

    expandAll(): void  { this.collapsedIds.set(new Set()); }
    collapseAll(): void {
        const all = new Set<number>();
        this.hasChildrenMap.forEach((_, id) => all.add(id));
        this.collapsedIds.set(all);
    }

    get isSearchMode(): boolean { return this.searchText.trim().length > 0; }

    search():      void { this.accounts.update(v => [...v]); }
    clearSearch(): void { this.searchText = ''; this.expandAll(); this.accounts.update(v => [...v]); }

    normalBalanceLabel(val: number): string {
        return val === 1 ? 'Debit' : val === 2 ? 'Credit' : '—';
    }

    printCoa(): void {
        const BASE_API = environment.get('baseApiUrl');
        this.downloadService.print(`${BASE_API}/accounting/accounts/export`, { type: 'pdf' });
    }
}
