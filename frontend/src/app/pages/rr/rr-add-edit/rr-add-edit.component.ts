import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { RrService } from '../rr.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowsePurchaseOrderModalComponent } from '@/app/shared/modals/browse-purchase-order-modal/browse-purchase-order-modal.component';
import { BrowseJobOrderModalComponent } from '@/app/shared/modals/browse-job-order-modal/browse-job-order-modal.component';
import { BrowseSupplierModalComponent } from '@/app/shared/modals/browse-supplier-modal/browse-supplier-modal.component';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseItemModalComponent } from '@/app/shared/modals/browse-item-modal/browse-item-modal.component';
import { provideIcons } from '@ng-icons/core';
import {
    tablerSearch, tablerTrash, tablerPlus, tablerArrowLeft, tablerCheck
} from '@ng-icons/tabler-icons';

// Document type constants
const DOC_TYPE_PO     = 1;
const DOC_TYPE_IFR    = 2;
const DOC_TYPE_RV     = 3;
const DOC_TYPE_JO     = 4;
const DOC_TYPE_TESTED = 5;

@Component({
    selector: 'app-rr-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective,
        RouterLink
    ],
    providers: [
        provideFlatpickrDefaults(),
        ...SHARED_PROVIDERS,
        provideIcons({ tablerSearch, tablerTrash, tablerPlus, tablerArrowLeft, tablerCheck })
    ],
    templateUrl: './rr-add-edit.component.html'
})
export class RrAddEditComponent {
    module    = 'Receiving Report';
    subModule = 'Create';
    menuLink  = 'rr';

    id: any  = null;
    editMode = false;
    isLoading = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // Header fields
    deliveryDate   = '';
    deliveryNumber = '';
    invoiceDate    = '';
    invoiceNumber  = '';
    remarks        = '';

    // Inventory Location
    inventoryLocations = signal<any[]>([]);
    selectedInventoryLocation: any = null;

    // Document type (mutually exclusive)
    docType = DOC_TYPE_PO;  // default: Purchase Order

    // Reference documents (one per type)
    selectedPO:  any = null;  // for PO and Tested Items
    selectedJO:  any = null;  // for Job Order
    selectedIFR: any = null;  // for Items For Repair
    selectedRV:  any = null;  // for Request Voucher
    selectedSupplier: any = null;  // for RV type (separate supplier browse)

    // Display descriptions for browse inputs
    poDesc  = '';
    joDesc  = '';
    ifrDesc = '';
    rvDesc  = '';

    // Items table
    rrDetails: any[] = [];

    // Totals (recomputed on every change)
    totals = { qty: 0, cost: 0, qtyReceived: 0, adjustment: 0, netAmount: 0 };

    // Signatories
    notedBy: any    = null;
    approvedBy: any = null;

    private service      = inject(RrService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    // ─── Computed type getters ────────────────────────────────────────────────
    get isPO():     boolean { return this.docType === DOC_TYPE_PO; }
    get isIFR():    boolean { return this.docType === DOC_TYPE_IFR; }
    get isRV():     boolean { return this.docType === DOC_TYPE_RV; }
    get isJO():     boolean { return this.docType === DOC_TYPE_JO; }
    get isTested(): boolean { return this.docType === DOC_TYPE_TESTED; }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    ngOnInit(): void {
        // Load inventory locations
        this.service.getInventoryLocations().subscribe({
            next: (locs) => this.inventoryLocations.set(locs || []),
            error: () => {}
        });

        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.editMode = idParam != null && /^\d+$/.test(idParam);
            if (this.editMode) {
                this.id = Number(idParam);
                this.subModule = 'Edit';
                this.loadForEdit();
            } else {
                this.subModule = 'Create';
                this.loadDefaultSignatories();
                // default delivery/invoice dates to today
                const today = new Date().toISOString().substring(0, 10);
                this.deliveryDate = today;
                this.invoiceDate  = today;
            }
        });
    }

    // ─── Data Loading ─────────────────────────────────────────────────────────

    loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (data) => {
                if (data) {
                    this.notedBy    = data.checkedBy   || null;
                    this.approvedBy = data.approvedBy  || null;
                }
            },
            error: () => {}
        });
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (!data?.id) {
                    this.alertService.error(this.module, 'Record not found.', '');
                    this.router.navigate(['/' + this.menuLink]);
                    return;
                }
                const toYmd = (v: any) => v ? new Date(v).toISOString().substring(0, 10) : '';

                this.deliveryDate   = toYmd(data.deliveryDate);
                this.invoiceDate    = toYmd(data.invoiceDate);
                this.deliveryNumber = data.deliveryNumber || '';
                this.invoiceNumber  = data.invoiceNumber  || '';
                this.remarks        = data.remarks        || '';
                this.notedBy        = data.checker        || null;
                this.approvedBy     = data.approvingOfficer || null;

                // Inventory location
                if (data.inventoryLocation?.id) {
                    const found = this.inventoryLocations().find(
                        l => l.id === data.inventoryLocation.id
                    );
                    this.selectedInventoryLocation = found ?? data.inventoryLocation;
                }

                // Document type + reference
                if (data.isJO) {
                    this.docType   = DOC_TYPE_JO;
                    this.selectedJO = data.jobOrder;
                    this.joDesc    = data.jobOrder?.joDesc || data.jobOrder?.localCode || '';
                } else if (data.isRV) {
                    this.docType          = DOC_TYPE_RV;
                    this.selectedRV       = data.requisitionVoucher;
                    this.rvDesc           = data.requisitionVoucher?.rvDesc || data.requisitionVoucher?.code || '';
                    this.selectedSupplier = data.supplier;
                } else if (data.isRepairedItems) {
                    this.docType    = DOC_TYPE_IFR;
                    this.selectedIFR = data.itemsForRepair;
                    this.ifrDesc    = data.itemsForRepair?.ifrDesc || data.itemsForRepair?.code || '';
                } else {
                    // PO or Tested Items — both use purchaseOrder reference
                    this.docType    = data.isTestedItem ? DOC_TYPE_TESTED : DOC_TYPE_PO;
                    this.selectedPO = data.purchaseOrder;
                    this.poDesc     = data.purchaseOrder?.poDesc || data.purchaseOrder?.localCode || '';
                }

                this.rrDetails = data.rrDetails || [];
                this.updateTotals();
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load record.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    // ─── Document Type Selection ──────────────────────────────────────────────

    selectDocType(type: number): void {
        this.docType         = type;
        this.rrDetails       = [];
        this.selectedPO      = null;
        this.selectedJO      = null;
        this.selectedIFR     = null;
        this.selectedRV      = null;
        this.selectedSupplier = null;
        this.poDesc          = '';
        this.joDesc          = '';
        this.ifrDesc         = '';
        this.rvDesc          = '';
        this.updateTotals();
    }

    // ─── Browse: Purchase Order ───────────────────────────────────────────────

    async openPOBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowsePurchaseOrderModalComponent, {}, { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const po = result.data;
                this.selectedPO = po;
                this.poDesc = (po.localCode || po.code || '') + ' : ' + (po.vendor?.name || po.supplier || '');
                this.rrDetails = [];
                const loader$ = this.isTested
                    ? this.service.getPurchaseOrderDetailsWithItemTesting(po.id)
                    : this.service.getPurchaseOrderDetailsForRR(po.id);
                loader$.subscribe({
                    next: (items) => {
                        this.rrDetails = items || [];
                        this.initAllQuantitiesReceived();
                        this.updateTotals();
                    },
                    error: () => this.alertService.error(this.module, 'Failed to load PO items.', '')
                });
            }
        } catch { }
    }

    // ─── Browse: Job Order ────────────────────────────────────────────────────

    async openJOBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseJobOrderModalComponent, {}, { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const jo = result.data;
                this.selectedJO = jo;
                this.joDesc = (jo.localCode || jo.code || '') + ' : ' + (jo.vendor?.name || jo.supplier || '');
                this.rrDetails = [];
                this.service.getJobOrderDetailsForRR(jo.id).subscribe({
                    next: (items) => {
                        this.rrDetails = items || [];
                        this.initAllQuantitiesReceived();
                        this.updateTotals();
                    },
                    error: () => this.alertService.error(this.module, 'Failed to load JO items.', '')
                });
            }
        } catch { }
    }

    // ─── Browse: Supplier (for RV type) ──────────────────────────────────────

    async openSupplierBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseSupplierModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.selectedSupplier = result.data;
            }
        } catch { }
    }

    // ─── Browse: Noted By / Approved By ──────────────────────────────────────

    async openSignatoryBrowse(field: 'notedBy' | 'approvedBy'): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this[field] = result.data;
            }
        } catch { }
    }

    // ─── Browse: Item (for IFR / RV free-form rows) ───────────────────────────

    async openItemBrowse(index: number): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseItemModalComponent, {}, { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const item = result.data;
                // Avoid duplicate items
                const isDuplicate = this.rrDetails.some(
                    (r, i) => i !== index && r.itemId === item.id
                );
                if (isDuplicate) {
                    this.alertService.warning(this.module, 'Validation', 'Duplicate item.');
                    return;
                }
                const row = this.rrDetails[index];
                row.itemId          = item.id;
                row.itemDescription = item.description;
                row.unitCode        = item.unit?.code || '';
                row.hasItem         = true;
                this.updateNetAmount(index, 'quantityReceived');
            }
        } catch { }
    }

    // ─── Items Table Helpers ──────────────────────────────────────────────────

    /**
     * After loading PO/JO details, default quantityReceived to the full quantity
     * (or remaining quantity if available).
     */
    initAllQuantitiesReceived(): void {
        for (const row of this.rrDetails) {
            if (row.quantityReceived == null || row.quantityReceived === 0) {
                row.quantityReceived = row.remainingQuantity ?? row.quantity ?? 0;
            }
            this.recalcRowNetAmount(row);
        }
    }

    /**
     * Recompute a single row's net amount: unitPrice * qtyReceived + adjustment
     */
    private recalcRowNetAmount(row: any): void {
        const qty  = Number(row.quantityReceived) || 0;
        const cost = Number(row.unitPrice)          || 0;
        const adj  = Number(row.adjustment)        || 0;
        row.netAmount = (qty * cost) + adj;
    }

    /**
     * Called from template when quantityReceived or adjustment changes.
     * `field` param is for future use (currently recalc always runs).
     */
    updateNetAmount(index: number, _field: string): void {
        if (index < 0 || index >= this.rrDetails.length) return;
        this.recalcRowNetAmount(this.rrDetails[index]);
        this.updateTotals();
    }

    updateTotals(): void {
        let qty = 0, cost = 0, qtyReceived = 0, adjustment = 0, netAmount = 0;
        for (const row of this.rrDetails) {
            qty         += Number(row.quantity)         || 0;
            cost        += Number(row.unitPrice)         || 0;
            qtyReceived += Number(row.quantityReceived) || 0;
            adjustment  += Number(row.adjustment)       || 0;
            netAmount   += Number(row.netAmount)        || 0;
        }
        this.totals = { qty, cost, qtyReceived, adjustment, netAmount };
    }

    /**
     * Add a blank free-form row (used for IFR and RV document types).
     */
    addIFRRow(): void {
        this.rrDetails.push({
            itemId:           null,
            itemDescription:  '',
            unitCode:         '',
            quantity:         0,
            quantityReceived: 0,
            unitPrice:         0,
            adjustment:       0,
            netAmount:        0,
            hasItem:          false
        });
    }

    removeRow(index: number): void {
        this.rrDetails.splice(index, 1);
        this.updateTotals();
    }

    // ─── compareById for inventory location select ────────────────────────────

    compareById(a: any, b: any): boolean {
        return a && b ? a.id === b.id : a === b;
    }

    // ─── Validation & Save ────────────────────────────────────────────────────

    save(): void {
        // Basic validation
        if (!this.deliveryDate) {
            this.alertService.warning(this.module, 'Validation', 'Delivery Date is required.');
            return;
        }
        if ((this.isPO || this.isTested) && !this.selectedPO) {
            this.alertService.warning(this.module, 'Validation', 'Please select a Purchase Order.');
            return;
        }
        if (this.isJO && !this.selectedJO) {
            this.alertService.warning(this.module, 'Validation', 'Please select a Job Order.');
            return;
        }
        if (this.isRV && !this.selectedSupplier) {
            this.alertService.warning(this.module, 'Validation', 'Please select a Supplier for the Requisition Voucher.');
            return;
        }
        if (this.rrDetails.length === 0) {
            this.alertService.warning(this.module, 'Validation', 'Please add at least one item.');
            return;
        }

        this.isLoading.set(true);

        const payload: any = {
            deliveryDate:   this.deliveryDate,
            invoiceDate:    this.invoiceDate    || null,
            deliveryNumber: this.deliveryNumber || null,
            invoiceNumber:  this.invoiceNumber  || null,
            remarks:        this.remarks        || null,

            inventoryLocation: this.selectedInventoryLocation?.id
                ? { id: this.selectedInventoryLocation.id }
                : null,

            // Document type flags (back-end uses these booleans)
            isJO:           this.isJO,
            isRV:           this.isRV,
            isRepairedItems: this.isIFR,
            isTestedItem:   this.isTested,

            // Reference documents
            purchaseOrder: (this.isPO || this.isTested) && this.selectedPO
                ? { id: this.selectedPO.id }
                : null,
            jobOrder: this.isJO && this.selectedJO
                ? { id: this.selectedJO.id }
                : null,
            requisitionVoucher: this.isRV && this.selectedRV
                ? { id: this.selectedRV.id }
                : null,
            itemsForRepair: this.isIFR && this.selectedIFR
                ? { id: this.selectedIFR.id }
                : null,
            supplier: this.isRV && this.selectedSupplier
                ? { accountNo: this.selectedSupplier.accountNo }
                : null,

            // Signatories
            checker:           this.notedBy?.accountNo    ? { accountNo: this.notedBy.accountNo }    : null,
            approvingOfficer:  this.approvedBy?.accountNo ? { accountNo: this.approvedBy.accountNo } : null,

            // Items
            rrDetails: this.rrDetails.map(row => ({
                id:               row.id               || null,
                poDetailId:       row.poDetailId        || null,
                joDetailId:       row.joDetailId        || null,
                itemId:           row.itemId            || null,
                itemDescription:  row.itemDescription   || '',
                unitCode:         row.unitCode          || '',
                quantity:         Number(row.quantity)         || 0,
                quantityReceived: Number(row.quantityReceived) || 0,
                unitPrice:         Number(row.unitPrice)         || 0,
                adjustment:       Number(row.adjustment)       || 0,
                netAmount:        Number(row.netAmount)        || 0
            })),

            // Totals
            totalAmount: this.totals.netAmount
        };

        if (this.editMode) payload.id = this.id;

        const request$ = this.editMode
            ? this.service.update(payload)
            : this.service.create(payload);

        request$.subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.success === false) {
                    this.alertService.error(this.module, 'Save', data.failureMessage || '');
                } else {
                    this.alertService.success(
                        this.module,
                        this.editMode ? 'Updated successfully.' : 'Created successfully.',
                        ''
                    );
                    const id = data?.modelId ?? data?.id ?? this.id;
                    this.router.navigate(['/' + this.menuLink, id, 'detail']);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Save', 'An error occurred.');
            }
        });
    }
}
