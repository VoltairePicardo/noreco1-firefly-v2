import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterLink} from '@angular/router';
import {NgIcon} from '@ng-icons/core';
import {LayoutStoreService} from '@core/services/layout-store.service';
import {LucideAngularModule, Search} from 'lucide-angular';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {SearchModalComponent} from './components/search-modal/search-modal.component';
import {ThemeTogglerComponent} from '@layouts/components/topbar/components/theme-toggler/theme-toggler.component';
import {
    CustomizerTogglerComponent
} from '@layouts/components/topbar/components/customizer-toggler/customizer-toggler.component';
import {UserProfileComponent} from '@layouts/components/topbar/components/user-profile/user-profile.component';

@Component({
    selector: 'app-topbar',
    host: {
        '(document:keydown.control.k)': 'openSearch($event)'
    },
    imports: [
        CommonModule,
        NgIcon,
        RouterLink,
        LucideAngularModule,
        CustomizerTogglerComponent,
        ThemeTogglerComponent,
        UserProfileComponent
    ],
    templateUrl: './topbar.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TopbarComponent {
    layout = inject(LayoutStoreService);
    private modalService = inject(NgbModal);

    Search = Search;
    private activeModal: NgbModalRef | null = null;

    openSearch(event?: KeyboardEvent) {
        event?.preventDefault();
        if (this.activeModal) return;
        this.activeModal = this.modalService.open(SearchModalComponent, {
            size: 'md',
            centered: true,
            animation: true,
            modalDialogClass: 'search-modal-dialog',
        });
        this.activeModal.hidden.subscribe(() => this.activeModal = null);
    }

    toggleSidebar() {
        const html = document.documentElement;
        const currentSize = html.getAttribute('data-sidenav-size');
        const savedSize = this.layout.sidenavSize;

        if (currentSize === 'offcanvas') {
            html.classList.toggle('sidebar-enable');
            this.layout.showBackdrop();
        } else if (savedSize === 'compact') {
            this.layout.setSidenavSize(currentSize === 'compact' ? 'condensed' : 'compact', false);
        } else {
            this.layout.setSidenavSize(currentSize === 'condensed' ? 'default' : 'condensed');
        }
    }
}
