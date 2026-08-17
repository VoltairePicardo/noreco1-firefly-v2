import { Component, TemplateRef, ViewChild, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { AlertService } from '@/app/shared/services/alert.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { PcvService } from '../pcv.service';
import Swal from 'sweetalert2';

@Component({
    selector: 'app-pcv-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './pcv-main.component.html'
})
export class PcvMainComponent {
    module    = 'Petty Cash Voucher';
    subModule = '';
    menuLink  = 'pcv';

    records   = signal<any[]>([]);
    isLoading = signal(false);

    searchQuery  = '';
    statusFilter = '';
    page         = 1;
    pageSize     = 10;

    // Batch management
    batches       : any[]    = [];
    selectedBatch : any      = null;
    activeOnly               = false;

    // Replenish modal state
    @ViewChild('replenishModal') replenishModalRef!: TemplateRef<any>;
    cvs               : any[]    = [];
    cvsLoading                   = false;
    selectedCv        : any      = null;
    replenishFrom                = '';
    replenishTo                  = '';
    replenishCode                = '';
    replenishProcessing          = false;

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    private service      = inject(PcvService);
    private alertService = inject(AlertService);
    private ngbModal     = inject(NgbModal);

    get filteredBatches(): any[] {
        if (!this.activeOnly) return this.batches;
        const s = (b: any) => (b.status || '').toLowerCase();
        return this.batches.filter(b => s(b).includes('open') || s(b).includes('active'));
    }

    get filteredRecords(): any[] {
        const q = this.searchQuery.toLowerCase();
        return this.records().filter(r => {
            const matchQuery = !q ||
                (r.code   || '').toLowerCase().includes(q) ||
                (r.payee  || '').toLowerCase().includes(q) ||
                (r.particulars || '').toLowerCase().includes(q);
            const matchStatus = !this.statusFilter ||
                String(r.documentStatusId) === this.statusFilter ||
                (r.status || r.documentStatus || '').toLowerCase().includes(this.statusFilter.toLowerCase());
            const matchBatch = !this.selectedBatch ||
                String(r.batchId || r.pettyCashBatch?.id) === String(this.selectedBatch.id);
            return matchQuery && matchStatus && matchBatch;
        });
    }

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.filteredRecords.slice(start, start + this.pageSize);
    }

    ngOnInit(): void {
        this.load();
        this.loadBatches();
    }

    load(): void {
        this.isLoading.set(true);
        this.service.list().subscribe({
            next: (data) => { this.records.set(data || []); this.page = 1; this.isLoading.set(false); },
            error: () => { this.alertService.error(this.module, 'Failed to load records.', ''); this.isLoading.set(false); }
        });
    }

    onSearch(): void { this.page = 1; }

    reset(): void {
        this.searchQuery   = '';
        this.statusFilter  = '';
        this.selectedBatch = null;
        this.activeOnly    = false;
        this.page          = 1;
        this.load();
    }

    isEditable(rec: any): boolean {
        const s = (rec?.status || rec?.documentStatus?.status || '').toString();
        return s === 'Document Created' || s === 'Returned to Creator' || s === '1';
    }

    compareBatch(a: any, b: any): boolean {
        return a && b ? a.id === b.id : a === b;
    }

    // ─── Batch management ────────────────────────────────────────────

    loadBatches(): void {
        this.service.getBatches().subscribe({
            next: (data) => { this.batches = data || []; },
            error: () => {}
        });
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
                    this.load();
                } else {
                    this.alertService.error(this.module, 'Failed to close batch.', res?.failureMessage || '');
                }
            },
            error: () => this.alertService.error(this.module, 'Error closing batch.', '')
        });
    }

    // ─── Replenish modal ─────────────────────────────────────────────

    openReplenish(): void {
        this.replenishFrom       = '';
        this.replenishTo         = '';
        this.replenishCode       = '';
        this.cvs                 = [];
        this.selectedCv          = null;
        this.replenishProcessing = false;
        this.ngbModal.open(this.replenishModalRef, { size: 'lg', centered: true });
    }

    loadCheckVouchers(): void {
        this.cvsLoading = true;
        this.service.getCheckVouchers(this.replenishFrom, this.replenishTo, this.replenishCode).subscribe({
            next: (data) => { this.cvs = data || []; this.cvsLoading = false; },
            error: () => { this.cvsLoading = false; }
        });
    }

    selectCv(cv: any): void {
        this.selectedCv = cv;
    }

    async confirmReplenish(modal: any): Promise<void> {
        if (!this.selectedCv) return;
        this.replenishProcessing = true;
        this.service.replenish({ cvId: this.selectedCv.id, batchId: this.selectedBatch?.id }).subscribe({
            next: (res) => {
                this.replenishProcessing = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Replenished successfully.', '');
                    modal.close();
                    this.load();
                } else {
                    this.alertService.error(this.module, 'Replenish failed.', res?.failureMessage || '');
                }
            },
            error: () => { this.replenishProcessing = false; this.alertService.error(this.module, 'Error.', ''); }
        });
    }
}
