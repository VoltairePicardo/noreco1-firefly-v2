import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { FormsModule } from '@angular/forms';
import { FlatpickrDefaults, FlatpickrModule } from 'angularx-flatpickr';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerTrash, tablerArrowLeft, tablerCheck } from '@ng-icons/tabler-icons';
import { AlertService } from '@/app/shared/services/alert.service';
import { forkJoin } from 'rxjs';
import { JobOrderService } from '../job-order.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowsePurchaseRequestModalComponent } from '@/app/shared/modals/browse-purchase-request-modal/browse-purchase-request-modal.component';

@Component({
    selector: 'app-job-order-add-edit',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, FormsModule, FlatpickrModule, RouterLink],
    providers: [FlatpickrDefaults, provideIcons({ tablerSearch, tablerTrash, tablerArrowLeft, tablerCheck })],
    templateUrl: './job-order-add-edit.component.html'
})
export class JobOrderAddEditComponent {
    module    = 'Job Order';
    subModule = 'Create';
    menuLink  = 'job-order';

    id: any    = null;
    editMode   = false;
    formSubmit = false;
    isLoading  = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // Header fields
    voucherDate        = '';
    vendor: any        = null;
    description        = '';
    paymentTerm: number | null = null;
    paymentTermInWords = '';

    // Signatories
    budgetCheckedBy: any = null;
    checkedBy: any       = null;
    approvedBy: any      = null;

    // Line items
    lineItems: any[] = [];

    private service      = inject(JobOrderService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            const id = params.get('id');
            if (id && /^\d+$/.test(id)) {
                this.id        = +id;
                this.editMode  = true;
                this.subModule = 'Edit';
                this.loadForEdit();
            }
        });
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        forkJoin({
            header:  this.service.getData(this.id),
            details: this.service.getDetails(this.id)
        }).subscribe({
            next: ({ header, details }) => {
                this.isLoading.set(false);
                if (header?.id) {
                    const d = new Date(header.voucherDate);
                    this.voucherDate        = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
                    this.vendor             = header.vendor;
                    this.description        = header.description || '';
                    this.paymentTerm        = header.paymentTerm ?? null;
                    this.paymentTermInWords = header.paymentTermInWords || '';
                    this.budgetCheckedBy    = header.budgetCheckedBy || null;
                    this.checkedBy          = header.checkedBy || null;
                    this.approvedBy         = header.approvedBy || header.approvingOfficer || null;

                    this.lineItems = (details || []).map((d: any) => ({
                        rvDetailId:      d.rvDetailId,
                        itemCode:        d.itemCode || '',
                        itemDescription: d.itemDescription || '',
                        unitCode:        d.unitCode || '',
                        quantity:        d.quantity || 0,
                        unitPrice:       d.unitPrice || 0,
                        vat:             d.vat || 0,
                        discount:        d.discount || 0,
                        joDescription:   d.joDescription || '',
                        itemAmount:      d.itemAmount || 0,
                    }));
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load job order.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    // ─── Vendor Browse ────────────────────────────────────────────────────────

    async openVendorBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseEntityModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this.vendor = result.data;
            }
        } catch { }
    }

    // ─── Signatory Browse ─────────────────────────────────────────────────────

    async openSignatoryBrowse(field: 'budgetCheckedBy' | 'checkedBy' | 'approvedBy'): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseEntityModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this[field] = result.data;
            }
        } catch { }
    }

    // ─── Purchase Request Browse ──────────────────────────────────────────────

    async openRvBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowsePurchaseRequestModalComponent,
                { type: 'jo', title: 'Browse Purchase Request' },
                { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const pr = result.data;
                this.service.getPurchaseRequestItems(pr.id).subscribe({
                    next: (items) => {
                        const existing = new Set(this.lineItems.map((li: any) => li.rvDetailId));
                        (items || []).filter((item: any) => !existing.has(item.id)).forEach((item: any) => {
                            this.lineItems.push({
                                rvDetailId:      item.id,
                                itemCode:        item.itemCode || '',
                                itemDescription: item.itemDescription || '',
                                joDescription:   item.joDescription || '',
                                unitCode:        item.unitCode || '',
                                quantity:        item.quantity || 0,
                                unitPrice:       0,
                                vat:             0,
                                discount:        0,
                            });
                        });
                    },
                    error: () => this.alertService.error(this.module, 'Failed to load PR items.', '')
                });
            }
        } catch { }
    }

    removeLineItem(index: number): void { this.lineItems.splice(index, 1); }

    // ─── Computed ────────────────────────────────────────────────────────────

    lineItemAmount(li: any): number {
        const price = +(li.unitPrice || 0);
        const qty   = +(li.quantity  || 0);
        const vat   = +(li.vat       || 0);
        const disc  = +(li.discount  || 0);
        return (price * qty) * (1 + vat / 100) * (1 - disc / 100);
    }

    get totalAmount(): number {
        return this.lineItems.reduce((sum, li) => sum + this.lineItemAmount(li), 0);
    }

    // ─── Save ────────────────────────────────────────────────────────────────

    save(): void {
        if (!this.voucherDate) {
            this.alertService.warning(this.module, 'Please enter the date.', '');
            return;
        }
        if (!this.vendor) {
            this.alertService.warning(this.module, 'Please select a supplier.', '');
            return;
        }
        if (this.lineItems.length === 0) {
            this.alertService.warning(this.module, 'Please add at least one item.', '');
            return;
        }
        if (!this.budgetCheckedBy) {
            this.alertService.warning(this.module, 'Please select a Budget Checked By officer.', '');
            return;
        }
        if (!this.checkedBy) {
            this.alertService.warning(this.module, 'Please select a Checked By officer.', '');
            return;
        }
        if (!this.approvedBy) {
            this.alertService.warning(this.module, 'Please select an Approved By officer.', '');
            return;
        }

        this.formSubmit = true;

        const payload: any = {
            voucherDate:        this.voucherDate,
            vendor:             { accountNo: this.vendor.accountNo },
            description:        this.description || null,
            paymentTerm:        this.paymentTerm || null,
            paymentTermInWords: this.paymentTermInWords || null,
            amount:             this.totalAmount,
            budgetCheckedBy:    this.budgetCheckedBy?.accountNo ? { accountNo: this.budgetCheckedBy.accountNo } : null,
            checkedBy:          this.checkedBy?.accountNo       ? { accountNo: this.checkedBy.accountNo }       : null,
            approvingOfficer:   this.approvedBy?.accountNo      ? { accountNo: this.approvedBy.accountNo }      : null,
            joDetails: this.lineItems.map(li => ({
                rvDetailId:    li.rvDetailId,
                quantity:      li.quantity,
                unitPrice:     li.unitPrice || 0,
                vat:           li.vat || 0,
                discount:      li.discount || 0,
                joDescription: li.joDescription || null,
                itemAmount:    this.lineItemAmount(li),
            }))
        };

        if (this.editMode) payload.id = this.id;

        const req = this.editMode ? this.service.update(payload) : this.service.create(payload);

        req.subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, res.successMessage || 'Saved successfully.', '');
                    this.router.navigate(['/' + this.menuLink, res.modelId, 'detail']);
                } else {
                    const msgs = res?.messages?.join('\n') || res?.failureMessage || 'Save failed.';
                    this.alertService.error(this.module, msgs, '');
                }
            },
            error: () => {
                this.formSubmit = false;
                this.alertService.error(this.module, 'An error occurred.', '');
            }
        });
    }
}
