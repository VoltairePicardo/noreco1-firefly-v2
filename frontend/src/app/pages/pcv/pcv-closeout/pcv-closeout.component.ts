import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { AlertService } from '@/app/shared/services/alert.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { ReplenishCvModalComponent } from '@/app/shared/modals/replenish-cv-modal/replenish-cv-modal.component';
import { PcvService } from '../pcv.service';
import Swal from 'sweetalert2';

@Component({
    selector: 'app-pcv-closeout',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './pcv-closeout.component.html'
})
export class PcvCloseoutComponent {
    module    = 'Petty Cash Voucher';
    subModule = 'Closeout';
    menuLink  = 'pcv';

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // Filters
    fromDate       = '';
    toDate         = '';
    offices        : any[] = [];
    selectedOffice : any   = null;
    documentStatuses   : any[] = [];
    selectedStatus     : any   = null;

    // Records
    records   = signal<any[]>([]);
    isLoading = signal(false);

    // Batch management
    batches       : any[] = [];
    selectedBatch : any   = null;
    activeOnly            = true;

    // Officers
    checkedBy    : any = null;
    replenishedBy: any = null;

    private service      = inject(PcvService);
    private alertService = inject(AlertService);
    private modalService = inject(ModalService);

    ngOnInit(): void {
        this.loadBatches();
        this.loadOffices();
        this.loadDocumentStatuses();
    }

    // ─── Filters ─────────────────────────────────────────────────────

    loadOffices(): void {
        this.service.getOffices().subscribe({
            next: (data) => { this.offices = data || []; },
            error: () => {}
        });
    }

    loadDocumentStatuses(): void {
        this.service.getDocumentStatuses().subscribe({
            next: (data) => { this.documentStatuses = data || []; },
            error: () => {}
        });
    }

    search(): void {
        this.isLoading.set(true);
        const params: any = {};
        if (this.fromDate)       params['from']     = this.fromDate;
        if (this.toDate)         params['to']       = this.toDate;
        if (this.selectedOffice) params['officeId'] = this.selectedOffice.id;
        if (this.selectedStatus) params['statusId'] = this.selectedStatus.id;
        this.service.getCloseoutVouchers(params).subscribe({
            next: (data) => { this.records.set(data || []); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Failed to load records.', ''); }
        });
    }

    printSummary(): void {
        const params: any = {};
        if (this.fromDate)        params['from']             = this.fromDate;
        if (this.toDate)          params['to']               = this.toDate;
        if (this.selectedOffice)  params['officeId']         = this.selectedOffice.id;
        if (this.selectedStatus)  params['statusId']         = this.selectedStatus.id;
        if (this.checkedBy)       params['checkedById']      = this.checkedBy.accountNo;
        if (this.replenishedBy)   params['replenishedById']  = this.replenishedBy.accountNo;
        this.service.printCloseout(params);
    }

    // ─── Officers ────────────────────────────────────────────────────

    async openSignatoryBrowse(field: 'checkedBy' | 'replenishedBy'): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseEntityModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this[field] = result.data;
            }
        } catch { }
    }

    clearSignatory(field: 'checkedBy' | 'replenishedBy'): void {
        this[field] = null;
    }

    // ─── Batch management ─────────────────────────────────────────────

    loadBatches(): void {
        this.service.getBatches().subscribe({
            next: (data) => { this.batches = data || []; },
            error: () => {}
        });
    }

    get filteredBatches(): any[] {
        const status = this.activeOnly ? 'active' : 'closed';
        return this.batches.filter(b => (b.status || '').toLowerCase() === status);
    }

    compareBatch(a: any, b: any): boolean {
        return a && b ? a.id === b.id : a === b;
    }

    onActiveOnlyChange(): void {
        this.selectedBatch = null;
    }

    createBatch(): void {
        this.service.createBatch().subscribe({
            next: (res) => {
                if (res?.success) {
                    this.alertService.success(this.module, 'Batch created.', '');
                    this.loadBatches();
                } else {
                    this.alertService.error(this.module, 'Failed to create batch.', res?.failureMessage || '');
                }
            },
            error: () => this.alertService.error(this.module, 'Error creating batch.', '')
        });
    }

    async closeBatch(): Promise<void> {
        if (!this.selectedBatch) return;
        const result = await Swal.fire({
            title: 'Close Batch',
            text: `Are you sure you want to close Batch ${this.selectedBatch.id} (${this.selectedBatch.status})?`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Confirm',
            cancelButtonText: 'Cancel'
        });
        if (!result.isConfirmed) return;
        this.service.closeBatch(this.selectedBatch.id).subscribe({
            next: (res) => {
                if (res?.success) {
                    this.alertService.success(this.module, 'Batch closed.', '');
                    this.selectedBatch = null;
                    this.loadBatches();
                } else {
                    this.alertService.error(this.module, 'Failed to close batch.', res?.failureMessage || '');
                }
            },
            error: () => this.alertService.error(this.module, 'Error closing batch.', '')
        });
    }

    // ─── Replenish ───────────────────────────────────────────────────

    async openReplenish(): Promise<void> {
        try {
            const result = await this.modalService.openModal(ReplenishCvModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'replenished') {
                this.alertService.success(this.module, 'Replenished successfully.', '');
            }
        } catch { }
    }
}
