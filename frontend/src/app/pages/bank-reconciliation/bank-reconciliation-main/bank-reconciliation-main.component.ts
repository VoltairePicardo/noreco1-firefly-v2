import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { BankReconciliationService } from '../bank-reconciliation.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseCOAModalComponent } from '@/app/shared/modals/browse-coa-modal/browse-coa-modal.component';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import Swal from 'sweetalert2';
import { provideIcons } from '@ng-icons/core';
import { tablerChevronLeft, tablerChevronRight } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-bank-reconciliation-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults(), provideIcons({ tablerChevronLeft, tablerChevronRight })],
    templateUrl: './bank-reconciliation-main.component.html'
})
export class BankReconciliationMainComponent {
    module    = 'Bank Reconciliation';
    subModule = 'Released Checks & Other Deposits';
    menuLink  = 'bank-reconciliation';

    records   = signal<any[]>([]);
    isLoading = signal(false);

    searchQuery      = '';
    clearedFilter    = '';   // '' = All, 'cleared', 'not-cleared'
    typeFilter       = '';   // '' = All, 'RC', 'DT', 'OD'
    selectedAccount: any = null;
    dateFilter       = '';

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    page     = 1;
    pageSize = 10;

    private service      = inject(BankReconciliationService);
    private alertService = inject(AlertService);
    private modalService = inject(ModalService);

    ngOnInit(): void {
        this.load();
    }

    load(): void {
        this.isLoading.set(true);
        this.service.getList().subscribe({
            next: (data) => {
                this.records.set(data || []);
                this.page = 1;
                this.isLoading.set(false);
            },
            error: () => {
                this.alertService.error(this.module, 'Load', '');
                this.isLoading.set(false);
            }
        });
    }

    /** Returns true when the record is in a cleared/reflected state */
    isCleared(rec: any): boolean {
        if (rec.statusId === 1) return true;
        const s = (rec.status || '').toLowerCase();
        return s === 'cleared' || s === 'reflected';
    }

    get filteredRecords(): any[] {
        let data = this.records();
        const q  = this.searchQuery.trim().toLowerCase();
        if (q) {
            data = data.filter(r =>
                (r.localCode   || '').toLowerCase().includes(q) ||
                (r.checkNo     || '').toLowerCase().includes(q) ||
                (r.account     || '').toLowerCase().includes(q) ||
                (r.particulars || '').toLowerCase().includes(q)
            );
        }
        if (this.typeFilter) {
            data = data.filter(r => r.type === this.typeFilter);
        }
        if (this.clearedFilter === 'cleared') {
            data = data.filter(r => this.isCleared(r));
        } else if (this.clearedFilter === 'not-cleared') {
            data = data.filter(r => !this.isCleared(r));
        }
        if (this.selectedAccount) {
            data = data.filter(r => r.accountId === this.selectedAccount.id);
        }
        if (this.dateFilter) {
            data = data.filter(r => (r.voucherDate || '').startsWith(this.dateFilter));
        }
        return data;
    }

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.filteredRecords.slice(start, start + this.pageSize);
    }

    onSearch(): void {
        this.page = 1;
    }

    onTypeChange(): void {
        // Mirror old behavior: selecting a type defaults status filter to "Not Cleared"
        this.clearedFilter = this.typeFilter ? 'not-cleared' : '';
        this.page = 1;
    }

    reset(): void {
        this.searchQuery      = '';
        this.clearedFilter    = '';
        this.typeFilter       = '';
        this.selectedAccount  = null;
        this.dateFilter       = '';
        this.page             = 1;
        this.load();
    }

    async openAccBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseCOAModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this.selectedAccount = result.data;
                this.page = 1;
            }
        } catch { }
    }

    clearAccount(): void {
        this.selectedAccount = null;
        this.page = 1;
    }

    /** Type-aware status label: OD uses Reflected/Not Reflected; RC+DT use Cleared/Not Cleared */
    statusLabel(rec: any, cleared: boolean): string {
        return rec.type === 'OD'
            ? (cleared ? 'Reflected'    : 'Not Reflected')
            : (cleared ? 'Cleared'      : 'Not Cleared');
    }

    toggleCleared(rec: any): void {
        const willClear    = !this.isCleared(rec);
        const actionLabel  = this.statusLabel(rec, willClear);
        const currentLabel = this.statusLabel(rec, !willClear);

        Swal.fire({
            title: `Mark as ${actionLabel}?`,
            text:  willClear
                ? `This will mark the record as ${actionLabel.toLowerCase()}.`
                : `This will remove the ${currentLabel.toLowerCase()} status.`,
            icon: 'question',
            showCancelButton: true,
            confirmButtonText: actionLabel,
            cancelButtonText: 'Cancel',
            buttonsStyling: false,
            customClass: {
                confirmButton: 'btn btn-primary me-2',
                cancelButton: 'btn btn-light text-dark'
            }
        }).then(result => {
            if (!result.isConfirmed) return;

            const req$ = rec.type === 'RC'
                ? this.service.processRc(rec.documentId, willClear)
                : rec.type === 'DT'
                    ? this.service.processDt(rec.documentId, willClear)
                    : this.service.processOd(rec.documentId, willClear);

            req$.subscribe({
                next: (res) => {
                    if (res?.success) {
                        this.alertService.success(this.module, actionLabel, '');
                        this.load();
                    } else {
                        this.alertService.error(this.module, actionLabel, res?.failureMessage || '');
                    }
                },
                error: () => {
                    this.alertService.error(this.module, actionLabel, '');
                }
            });
        });
    }
}
