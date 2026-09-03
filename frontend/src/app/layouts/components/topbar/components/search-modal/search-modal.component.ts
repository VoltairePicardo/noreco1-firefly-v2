import {
    AfterViewInit,
    ChangeDetectionStrategy,
    Component,
    computed,
    ElementRef,
    inject,
    signal,
    ViewChild,
} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {NgIcon} from '@ng-icons/core';
import {AuthService} from '@/app/pages/auth/auth.service';
import {CommonModule} from '@angular/common';

interface MenuSearchResult {
    text: string;
    parentMenuText: string;
    link: string;
    icon?: string;
}

interface MenuSearchGroup {
    label: string;
    items: Array<{ result: MenuSearchResult; index: number }>;
}

@Component({
    selector: 'app-search-modal',
    standalone: true,
    imports: [CommonModule, RouterLink, NgIcon],
    changeDetection: ChangeDetectionStrategy.OnPush,
    styles: `
        :host {
            --ins-table-hover-bg: rgba(var(--ins-light-rgb), 0.70);
        }

        .search-input {
            border: none;
            box-shadow: none !important;
            font-size: 1.1rem;
        }

        .search-input:focus {
            outline: none;
        }

        .group-label {
            padding: 6px 10px 2px;
            font-size: 0.72rem;
            font-weight: 500;
            letter-spacing: 0.04em;
            text-transform: uppercase;
            color: var(--ins-secondary-color);
        }

        .result-item {
            display: flex;
            align-items: center;
            gap: 6px;
            padding: 8px 10px;
            text-decoration: none;
            color: var(--ins-body-color);
            border-radius: 6px;
            cursor: pointer;
            border: 1px solid transparent;
        }

        .result-item:hover,
        .result-item:focus,
        .result-item.active {
            background: var(--ins-table-hover-bg);
            border-color: var(--ins-border-color);
            outline: none;
        }

        .result-list {
            scrollbar-width: none;
            -ms-overflow-style: none;
        }

        .result-list::-webkit-scrollbar {
            display: none;
        }

        .result-icon {
            width: 32px;
            height: 32px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 8px;
            flex-shrink: 0;
        }
    `,
    template: `
        <div class="modal-header align-items-center border-bottom py-2 px-3">
            <ng-icon name="tablerSearch" class="text-muted me-2 flex-shrink-0 fs-18"/>
            <input #searchInput
                   type="text"
                   class="form-control search-input flex-grow-1"
                   placeholder="Search modules..."
                   [value]="searchQuery()"
                   (input)="onQueryChange($any($event.target).value)"
                   (keydown.escape)="modal.dismiss()"
                   (keydown.arrowdown)="moveActive(1)"
                   (keydown.arrowup)="moveActive(-1)"
                   (keydown.enter)="selectActive()"
                   autocomplete="off"
                   aria-label="Search modules"
                   aria-autocomplete="list">
            @if (searchQuery()) {
                <button class="btn btn-sm btn-link text-muted p-1 ms-1" (click)="searchQuery.set('')"
                        aria-label="Clear search">
                    <ng-icon name="tablerX" class="fs-18"/>
                </button>
            }
        </div>

        <div #resultList class="modal-body result-list p-2" style="min-height: 200px; max-height: 420px; overflow-y: auto;">
            @if (searchQuery().trim() && searchGroups().length === 0) {
                <p class="text-muted text-center py-4 mb-0">
                    No modules found for "<strong>{{ searchQuery() }}</strong>".
                </p>
            } @else {
                @for (group of searchGroups(); track group.label) {
                    <div class="group-label">{{ group.label }}</div>
                    @for (item of group.items; track item.result.link) {
                        <a class="result-item"
                           role="option"
                           [class.active]="activeIndex() === item.index"
                           [routerLink]="item.result.link"
                           (mousedown)="navigate(item.result.link)"
                           (mouseenter)="activeIndex.set(item.index)">
                            <span class="result-icon">
                                <ng-icon name="tablerArrowRight" class="fs-18 text-muted"/>
                            </span>
                            <div class="overflow-hidden flex-grow-1">
                                <div class="fw-medium fs-14 text-truncate">{{ item.result.text }}</div>
                            </div>
                        </a>
                    }
                }
            }
        </div>
    `
})
export class SearchModalComponent implements AfterViewInit {
    modal = inject(NgbActiveModal);
    private auth = inject(AuthService);
    private router = inject(Router);

    @ViewChild('searchInput') searchInput!: ElementRef<HTMLInputElement>;
    @ViewChild('resultList') resultList!: ElementRef<HTMLElement>;

    searchQuery = signal('');
    activeIndex = signal(0);

    private allMenus = computed<MenuSearchResult[]>(() => {
        const menus: any[] = this.auth.getMenus();
        return menus
            .filter(m => m.url)
            .map(m => ({
                text: m.title,
                parentMenuText: this.buildParentText(m),
                link: m.url,
                icon: m.iconClass,
            }));
    });

    private searchResults = computed<MenuSearchResult[]>(() => {
        const q = this.searchQuery().trim().toLowerCase();
        if (!q) return this.allMenus();
        return this.allMenus()
            .filter(m => m.text.toLowerCase().includes(q) || m.parentMenuText.toLowerCase().includes(q));
    });

    searchGroups = computed<MenuSearchGroup[]>(() => {
        const map = new Map<string, Array<{ result: MenuSearchResult; index: number }>>();
        this.searchResults().forEach((result, index) => {
            const label = result.parentMenuText.split('|')[0] || result.text;
            if (!map.has(label)) map.set(label, []);
            map.get(label)!.push({ result, index });
        });
        return Array.from(map.entries()).map(([label, items]) => ({ label, items }));
    });

    ngAfterViewInit() {
        this.searchInput.nativeElement.focus();
    }

    onQueryChange(value: string) {
        this.searchQuery.set(value);
        this.activeIndex.set(0);
    }

    moveActive(delta: number) {
        const total = this.searchResults().length;
        if (total === 0) return;
        this.activeIndex.update(i => (i + delta + total) % total);
        setTimeout(() => this.scrollActiveIntoView());
    }

    selectActive() {
        const result = this.searchResults()[this.activeIndex()];
        if (result) this.navigate(result.link);
    }

    navigate(link: string) {
        void this.router.navigateByUrl(link);
        this.modal.dismiss();
    }

    private scrollActiveIntoView() {
        const el = this.resultList?.nativeElement.querySelector('.result-item.active');
        el?.scrollIntoView({ block: 'nearest' });
    }

    private buildParentText(menu: any): string {
        const parts: string[] = [];
        let p = menu.parentMenu;
        while (p) {
            parts.unshift(p.title ?? '');
            p = p.parentMenu;
        }
        return parts.join('|');
    }
}

