import {Component, inject, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {NgIcon} from '@ng-icons/core';
import {NgbCollapse} from '@ng-bootstrap/ng-bootstrap';
import {NavigationEnd, Router, RouterLink} from '@angular/router';
import {filter} from 'rxjs';
import {scrollToElement} from '@/app/utils/layout-utils';
import {LayoutStoreService} from '@core/services/layout-store.service';
import {AuthService} from '@/app/pages/auth/auth.service';
import {Title} from '@angular/platform-browser';
import {appTitle} from '@/app/constants';

@Component({
    selector: 'app-menu',
    imports: [CommonModule, NgIcon, NgbCollapse, RouterLink],
    templateUrl: './app-menu.component.html'
})
export class AppMenuComponent implements OnInit {

    router = inject(Router)
    layout = inject(LayoutStoreService)
    authService = inject(AuthService);
    private titleService = inject(Title);

    @ViewChild('MenuItemWithChildren', {static: true})
    menuItemWithChildren!: TemplateRef<{ item: any }>;

    @ViewChild('MenuItem', {static: true})
    menuItem!: TemplateRef<{ item: any }>;

    menuItems: any[] = [];

    private readonly iconMap: Record<string, string> = {
        'fa fa-tachometer':         'tablerLayoutDashboard',
        'fa fa-dashboard':          'tablerLayoutDashboard',
        'fa fa-book':               'tablerReceipt2',
        'fa fa-file-text':          'tablerReceipt2',
        'fa fa-shopping-cart':      'tablerShoppingCart',
        'fa fa-archive':            'tablerPackage',
        'fa fa-cubes':              'tablerPackage',
        'fa fa-university':         'tablerBuildingBank',
        'fa fa-bank':               'tablerBuildingBank',
        'fa fa-industry':           'tablerBuildingFactory',
        'fa fa-cogs':               'tablerBuildingFactory',
        'fa fa-clipboard':          'tablerClipboardList',
        'fa fa-list':               'tablerClipboardList',
        'fa fa-bar-chart':          'tablerReportAnalytics',
        'fa fa-line-chart':         'tablerReportAnalytics',
        'fa fa-gear':               'tablerSettings2',
        'fa fa-wrench':             'tablerSettings2',
        'fa fa-cog':                'tablerSettings2',
        'fa fa-shield':             'tablerShieldCog',
        'fa fa-user-secret':        'tablerShieldCog',
        'fa fa-users':              'tablerUsers',
    };

    ngOnInit(): void {
        this.menuItems = this.buildMenuTree(this.authService.getMenus());

        this.router.events
            .pipe(filter(event => event instanceof NavigationEnd))
            .subscribe(() => {
                this.expandActivePaths(this.menuItems);
                setTimeout(() => this.scrollToActiveLink(), 50);
                const active = this.findActiveLeaf(this.menuItems);
                if (active?.text) {
                    this.titleService.setTitle(`${active.text} | ${appTitle}`);
                }
            });

        this.expandActivePaths(this.menuItems);
        setTimeout(() => this.scrollToActiveLink(), 100);
    }

    buildMenuTree(menus: any[]): any[] {
        const topLevel = menus.filter(m => !m.parentMenu);

        if (topLevel.length > 0) {
            // Parent menus are in the result — normal path
            return topLevel.map(m => {
                const children = (m.subMenus || []).map((c: any) => ({
                    text: c.title,
                    link: c.viewRoute?.url || null,
                    icon: this.resolveIcon(c.iconClass),
                }));
                return {
                    text: m.title,
                    icon: this.resolveIcon(m.iconClass),
                    link: children.length === 0 ? (m.viewRoute?.url || null) : null,
                    isCollapsed: true,
                    children,
                };
            });
        }

        // Fallback: only child menus returned — group by parent using embedded parentMenu data
        const parentMap = new Map<number, any>();
        for (const m of menus) {
            if (!m.parentMenu) continue;
            const pid = m.parentMenu.id ?? m.parentMenu.FK_parentMenuId ?? 0;
            if (!parentMap.has(pid)) {
                parentMap.set(pid, {
                    text: m.parentMenu.title || `Group ${pid}`,
                    icon: this.resolveIcon(m.parentMenu.iconClass),
                    link: null,
                    isCollapsed: true,
                    children: [],
                    _order: m.parentMenu.order ?? pid,
                });
            }
            parentMap.get(pid).children.push({
                text: m.title,
                link: m.viewRoute?.url || null,
                icon: this.resolveIcon(m.iconClass),
            });
        }
        console.log('[Menu] Fallback groups:', parentMap.size);
        return Array.from(parentMap.values()).sort((a, b) => a._order - b._order);
    }

    resolveIcon(iconClass: string): string {
        if (!iconClass) return 'tablerCircleDot';
        return this.iconMap[iconClass.trim()] || 'tablerCircleDot';
    }

    hasSubMenu(item: any): boolean {
        return item.children?.length > 0;
    }

    expandActivePaths(items: any[]) {
        for (const item of items) {
            if (this.hasSubMenu(item)) {
                item.isCollapsed = !this.isChildActive(item);
                this.expandActivePaths(item.children || []);
            }
        }
    }

    isChildActive(item: any): boolean {
        if (item.link && this.isActive(item)) return true;
        if (!item.children) return false;
        return item.children.some((child: any) => this.isChildActive(child));
    }

    findActiveLeaf(items: any[]): any {
        for (const item of items) {
            if (item.children?.length > 0) {
                const found = this.findActiveLeaf(item.children);
                if (found) return found;
            } else if (this.isActive(item)) {
                return item;
            }
        }
        return null;
    }

    isActive(item: any): boolean {
        if (!item.link) return false;
        return this.router.isActive(item.link, {
            paths: 'subset',
            queryParams: 'ignored',
            fragment: 'ignored',
            matrixParams: 'ignored',
        });
    }

    scrollToActiveLink(): void {
        const activeItem = document.querySelector('[data-active-link="true"]') as HTMLElement;
        const scrollContainer = document.querySelector("#sidenav .simplebar-content-wrapper") as HTMLElement;

        if (activeItem && scrollContainer) {
            const containerRect = scrollContainer.getBoundingClientRect();
            const itemRect = activeItem.getBoundingClientRect();

            const offset = itemRect.top - containerRect.top - window.innerHeight * 0.4;

            scrollToElement(scrollContainer, scrollContainer.scrollTop + offset, 500);
        }
    }

    hideBackDrop(url: string): void {
        if (this.layout.sidenavSize === 'offcanvas' && this.router.url !== url) {
            this.layout.hideBackdrop();
        }
    }
}
