import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { CreditCardPurchaseRequestService } from '../credit-card-purchase-request.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowsePurchaseOrderModalComponent } from '@/app/shared/modals/browse-purchase-order-modal/browse-purchase-order-modal.component';
import { BrowseJobOrderModalComponent } from '@/app/shared/modals/browse-job-order-modal/browse-job-order-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerCheck, tablerArrowLeft } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-credit-card-purchase-request-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective
    ],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS,
        provideIcons({ tablerSearch, tablerCheck, tablerArrowLeft })],
    templateUrl: './credit-card-purchase-request-add-edit.component.html'
})
export class CreditCardPurchaseRequestAddEditComponent {
    module    = 'Credit Card Purchase Request';
    subModule = 'Create';
    menuLink  = 'credit-card-purchase-request';

    id: any    = null;
    editMode   = false;
    formSubmit = false;
    isLoading  = signal(false);

    // Form fields
    voucherDate = '';
    purpose     = '';

    // PO / JO selection
    isPurchaseOrder = false;
    isJobOrder      = false;
    selectedPO: any = null;
    selectedJO: any = null;
    itemDetails: any[]     = [];
    itemsLoading           = false;

    // Officers
    recommendedBy: any = null;
    approvedBy: any    = null;

    // Lookup data
    fundingSources = signal<any[]>([]);
    modes          = signal<any[]>([]);
    fundingSource: any = null;
    mode: any          = null;

    flatpickrOptions = {
        dateFormat: 'Y-m-d',
        altInput: true,
        altFormat: 'F j, Y'
    };

    private service      = inject(CreditCardPurchaseRequestService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);
    private modalService = inject(ModalService);

    ngOnInit(): void {
        this.setDefaultDate();

        this.service.getFundingSources().subscribe({
            next: (data) => this.fundingSources.set(data || []),
            error: () => {}
        });
        this.service.getModes().subscribe({
            next: (data) => this.modes.set(data || []),
            error: () => {}
        });

        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.editMode = idParam != null && /^\d+$/.test(idParam);
            if (this.editMode) {
                this.id        = Number(idParam);
                this.subModule = 'Edit';
                this.loadForEdit();
            } else {
                this.subModule = 'Create';
            }
        });
    }

    private setDefaultDate(): void {
        const today = new Date();
        const fmt = (d: Date) =>
            `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
        this.voucherDate = fmt(today);
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.voucherDate   = data.voucherDate ? new Date(data.voucherDate).toISOString().substring(0, 10) : '';
                    this.purpose       = data.purpose             || '';
                    this.recommendedBy = data.recommendingOfficer || null;
                    this.approvedBy    = data.approvingOfficer    || null;
                    this.fundingSource = data.fundingSource       || null;
                    this.mode          = data.mode                || null;

                    if (data.purchaseOrder?.id) {
                        this.isPurchaseOrder = true;
                        this.selectedPO = data.purchaseOrder;
                        this.loadPoItems(data.purchaseOrder.id);
                    }
                    if (data.jobOrder?.id) {
                        this.isJobOrder = true;
                        this.selectedJO = data.jobOrder;
                        if (!this.isPurchaseOrder) {
                            this.loadJoItems(data.jobOrder.id);
                        }
                    }
                } else {
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Load', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    // ── PO / JO type selection (mutually exclusive) ──────────────────────────
    onTypeChange(type: 'po' | 'jo' | null): void {
        this.isPurchaseOrder = type === 'po';
        this.isJobOrder      = type === 'jo';
        this.selectedPO      = null;
        this.selectedJO      = null;
        this.itemDetails     = [];
    }

    // ── Browse PO ────────────────────────────────────────────────────────────
    async openPoBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowsePurchaseOrderModalComponent, {}, { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.selectedPO = result.data;
                this.loadPoItems(result.data.id);
            }
        } catch { }
    }

    private loadPoItems(poId: number): void {
        this.itemsLoading = true;
        this.service.getPurchaseOrderItems(poId).subscribe({
            next: (items) => { this.itemDetails = items || []; this.itemsLoading = false; },
            error: () => { this.itemsLoading = false; }
        });
    }

    // ── Browse JO ────────────────────────────────────────────────────────────
    async openJoBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseJobOrderModalComponent, {}, { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.selectedJO = result.data;
                if (!this.isPurchaseOrder) {
                    this.loadJoItems(result.data.id);
                }
            }
        } catch { }
    }

    private loadJoItems(joId: number): void {
        this.itemsLoading = true;
        this.service.getJobOrderItems(joId).subscribe({
            next: (items) => { this.itemDetails = items || []; this.itemsLoading = false; },
            error: () => { this.itemsLoading = false; }
        });
    }

    // ── Entity browse ────────────────────────────────────────────────────────
    async openEntityBrowse(field: string): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                switch (field) {
                    case 'recommendedBy': this.recommendedBy = result.data; break;
                    case 'approvedBy':    this.approvedBy    = result.data; break;
                }
            }
        } catch { }
    }

    // ── Save ─────────────────────────────────────────────────────────────────
    save(): void {
        if (!this.voucherDate) {
            this.alertService.warning(this.module, 'Validation', 'Date is required.');
            return;
        }
        if (!this.mode) {
            this.alertService.warning(this.module, 'Validation', 'Mode is required.');
            return;
        }
        if (!this.fundingSource) {
            this.alertService.warning(this.module, 'Validation', 'Funding Source is required.');
            return;
        }
        if (!this.recommendedBy) {
            this.alertService.warning(this.module, 'Validation', 'Recommended By is required.');
            return;
        }
        if (!this.approvedBy) {
            this.alertService.warning(this.module, 'Validation', 'Approved By is required.');
            return;
        }

        const payload: any = {
            voucherDate:         this.voucherDate,
            purpose:             this.purpose,
            recommendingOfficer: { accountNo: this.recommendedBy.accountNo },
            approvingOfficer:    { accountNo: this.approvedBy.accountNo },
            fundingSource:       { id: this.fundingSource.id },
            mode:                { id: this.mode.id },
            purchaseOrder:       (this.isPurchaseOrder && this.selectedPO) ? { id: this.selectedPO.id } : null,
            jobOrder:            (this.isJobOrder      && this.selectedJO) ? { id: this.selectedJO.id } : null,
        };

        if (this.editMode) payload.id = this.id;

        this.formSubmit = true;
        const req$ = this.editMode ? this.service.update(payload) : this.service.create(payload);

        req$.subscribe({
            next: (res) => {
                if (res.success) {
                    this.alertService.success(this.module, 'Saved', '');
                    this.router.navigate(['/' + this.menuLink, res.modelId, 'detail']);
                } else {
                    this.formSubmit = false;
                    this.alertService.error(this.module, 'Saving', res.failureMessage || '');
                }
            },
            error: () => {
                this.formSubmit = false;
                this.alertService.error(this.module, 'Saving', '');
            }
        });
    }
}
