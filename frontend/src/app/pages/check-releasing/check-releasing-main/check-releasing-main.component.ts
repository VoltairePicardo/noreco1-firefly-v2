import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { NgbNavModule } from '@ng-bootstrap/ng-bootstrap';
import { CheckReleasingService } from '../check-releasing.service';
import Swal from 'sweetalert2';

@Component({
    selector: 'app-check-releasing-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective, NgbNavModule],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './check-releasing-main.component.html'
})
export class CheckReleasingMainComponent {
    module    = 'Check Releasing';
    subModule = '';
    menuLink  = 'check-releasing';

    activeTab = 1;

    // Not Released tab
    unreleased        = signal<any[]>([]);
    unreleasedLoading = signal(false);
    unreleasedPage    = 1;
    unreleasedPageSize = 10;
    unreleasedSearch  = '';

    get filteredUnreleased(): any[] {
        if (!this.unreleasedSearch.trim()) return this.unreleased();
        const q = this.unreleasedSearch.trim().toLowerCase();
        return this.unreleased().filter(r =>
            (r.code         || '').toLowerCase().includes(q) ||
            (r.checkNumber  || '').toLowerCase().includes(q) ||
            (r.accountTitle || '').toLowerCase().includes(q) ||
            (r.particulars  || '').toLowerCase().includes(q)
        );
    }

    get pagedUnreleased(): any[] {
        const start = (this.unreleasedPage - 1) * this.unreleasedPageSize;
        return this.filteredUnreleased.slice(start, start + this.unreleasedPageSize);
    }

    // Released tab
    released        = signal<any[]>([]);
    releasedLoading = signal(false);
    releasedPage    = 1;
    releasedPageSize = 10;
    releasedLoaded  = false;
    releasedSearch  = '';

    get filteredReleased(): any[] {
        if (!this.releasedSearch.trim()) return this.released();
        const q = this.releasedSearch.trim().toLowerCase();
        return this.released().filter(r =>
            (r.code        || '').toLowerCase().includes(q) ||
            (r.checkNumber || '').toLowerCase().includes(q) ||
            (r.receivedBy  || '').toLowerCase().includes(q)
        );
    }

    get pagedReleased(): any[] {
        const start = (this.releasedPage - 1) * this.releasedPageSize;
        return this.filteredReleased.slice(start, start + this.releasedPageSize);
    }

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    fromDate = '';
    toDate   = '';

    private service      = inject(CheckReleasingService);
    private alertService = inject(AlertService);
    private router       = inject(Router);

    ngOnInit(): void {
        this.setDefaultDates();
        this.loadUnreleased();
    }

    setDefaultDates(): void {
        const now   = new Date();
        const first = new Date(now.getFullYear(), now.getMonth(), 1);
        this.fromDate = this.toDateString(first);
        this.toDate   = this.toDateString(now);
    }

    toDateString(d: Date): string {
        return d.toISOString().substring(0, 10);
    }

    onTabChange(tabId: number): void {
        this.activeTab = tabId;
        if (tabId === 2 && !this.releasedLoaded) {
            this.loadReleased();
        }
    }

    loadUnreleased(): void {
        this.unreleasedLoading.set(true);
        this.service.getUnreleased().subscribe({
            next: (data) => {
                this.unreleased.set(data || []);
                this.unreleasedPage = 1;
                this.unreleasedLoading.set(false);
            },
            error: () => {
                this.alertService.error(this.module, 'Load', '');
                this.unreleasedLoading.set(false);
            }
        });
    }

    loadReleased(): void {
        this.releasedLoading.set(true);
        this.service.getReleased(this.fromDate, this.toDate).subscribe({
            next: (data) => {
                this.released.set(data || []);
                this.releasedPage = 1;
                this.releasedLoading.set(false);
                this.releasedLoaded = true;
            },
            error: () => {
                this.alertService.error(this.module, 'Load', '');
                this.releasedLoading.set(false);
            }
        });
    }

    searchReleased(): void {
        this.releasedLoaded = false;
        this.loadReleased();
    }

    resetReleased(): void {
        this.setDefaultDates();
        this.releasedSearch  = '';
        this.releasedLoaded  = false;
        this.loadReleased();
    }

    releaseCheck(item: any): void {
        this.router.navigate(['/' + this.menuLink, item.id, 'release'], { state: { check: item } });
    }

    viewDetail(item: any): void {
        this.router.navigate(['/' + this.menuLink, item.id, 'detail']);
    }

    cancelCheck(item: any): void {
        Swal.fire({
            title: 'Cancel Released Check',
            text: `Are you sure you want to cancel check ${item.checkNumber || item.code}?`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Yes, Cancel It',
            cancelButtonText: 'No',
            buttonsStyling: false,
            customClass: { confirmButton: 'btn btn-danger me-2', cancelButton: 'btn btn-light text-dark' },
        }).then(result => {
            if (result.isConfirmed) {
                this.service.cancel({ id: item.id }).subscribe({
                    next: (res) => {
                        if (res.success) {
                            this.alertService.success(this.module, 'Cancelled', '');
                            this.releasedLoaded = false;
                            this.loadReleased();
                        } else {
                            this.alertService.error(this.module, 'Cancel', res.failureMessage || '');
                        }
                    },
                    error: () => this.alertService.error(this.module, 'Cancel', '')
                });
            }
        });
    }
}
