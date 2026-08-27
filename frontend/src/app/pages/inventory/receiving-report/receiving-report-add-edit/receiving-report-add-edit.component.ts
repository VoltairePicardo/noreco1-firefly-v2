import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { ReceivingReportService } from '../receiving-report.service';
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

const DOC_TYPE_PO     = 1;
const DOC_TYPE_IFR    = 2;
const DOC_TYPE_RV     = 3;
const DOC_TYPE_JO     = 4;
const DOC_TYPE_TESTED = 5;

@Component({
    selector: 'app-receiving-report-add-edit',
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
    templateUrl: './receiving-report-add-edit.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReceivingReportAddEditComponent implements OnInit {
    module    = 'Receiving Report';
    subModule = 'Create';
    menuLink  = 'receiving-report';

    id: any  = null;
    editMode = false;
    isLoading = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    deliveryDate   = '';
    deliveryNumber = '';
    invoiceDate    = '';
    invoiceNumber  = '';
    remarks        = '';

    inventoryLocations        = signal<any[]>([]);
    selectedInventoryLocation = signal<any>(null);

    docType = signal(DOC_TYPE_PO);

    selectedPO       = signal<any>(null);
    selectedJO       = signal<any>(null);
    selectedIFR      = signal<any>(null);
    selectedRV       = signal<any>(null);
    selectedSupplier = signal<any>(null);

    poDesc  = signal('');
    joDesc  = signal('');
    ifrDesc = signal('');
    rvDesc  = signal('');

    rrDetails = signal<any[]>([]);

    totals = { qty: 0, cost: 0, qtyReceived: 0, adjustment: 0, netAmount: 0 };

    notedBy    = signal<any>(null);
    approvedBy = signal<any>(null);

    private service      = inject(ReceivingReportService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    isPO     = computed(() => this.docType() === DOC_TYPE_PO);
    isIFR    = computed(() => this.docType() === DOC_TYPE_IFR);
    isRV     = computed(() => this.docType() === DOC_TYPE_RV);
    isJO     = computed(() => this.docType() === DOC_TYPE_JO);
    isTested = computed(() => this.docType() === DOC_TYPE_TESTED);

    ngOnInit(): void {
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
                const today = new Date().toISOString().substring(0, 10);
                this.deliveryDate = today;
                this.invoiceDate  = today;
            }
        });
    }

    loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (data) => {
                if (data) {
                    this.notedBy.set(data.checkedBy   || null);
                    this.approvedBy.set(data.approvedBy || null);
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
                this.notedBy.set(data.checker || null);
                this.approvedBy.set(data.approvingOfficer || null);

                if (data.inventoryLocation?.id) {
                    const found = this.inventoryLocations().find(
                        l => l.id === data.inventoryLocation.id
                    );
                    this.selectedInventoryLocation.set(found ?? data.inventoryLocation);
                }

                if (data.isJO) {
                    this.docType.set(DOC_TYPE_JO);
                    this.selectedJO.set(data.jobOrder);
                    this.joDesc.set(data.jobOrder?.joDesc || data.jobOrder?.localCode || '');
                } else if (data.isRV) {
                    this.docType.set(DOC_TYPE_RV);
                    this.selectedRV.set(data.requisitionVoucher);
                    this.rvDesc.set(data.requisitionVoucher?.rvDesc || data.requisitionVoucher?.code || '');
                    this.selectedSupplier.set(data.supplier);
                } else if (data.isRepairedItems) {
                    this.docType.set(DOC_TYPE_IFR);
                    this.selectedIFR.set(data.itemsForRepair);
                    this.ifrDesc.set(data.itemsForRepair?.ifrDesc || data.itemsForRepair?.code || '');
                } else {
                    this.docType.set(data.isTestedItem ? DOC_TYPE_TESTED : DOC_TYPE_PO);
                    this.selectedPO.set(data.purchaseOrder);
                    this.poDesc.set(data.purchaseOrder?.poDesc || data.purchaseOrder?.localCode || '');
                }

                this.rrDetails.set(data.rrDetails || []);
                this.updateTotals();
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load record.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    selectDocType(type: number): void {
        this.docType.set(type);
        this.rrDetails.set([]);
        this.selectedPO.set(null);
        this.selectedJO.set(null);
        this.selectedIFR.set(null);
        this.selectedRV.set(null);
        this.selectedSupplier.set(null);
        this.poDesc.set('');
        this.joDesc.set('');
        this.ifrDesc.set('');
        this.rvDesc.set('');
        this.updateTotals();
    }

    async openPOBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowsePurchaseOrderModalComponent, {}, { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const po = result.data;
                this.selectedPO.set(po);
                this.poDesc.set((po.localCode || po.code || '') + ' : ' + (po.vendor?.name || po.supplier || ''));
                this.rrDetails.set([]);
                const loader$ = this.isTested()
                    ? this.service.getPurchaseOrderDetailsWithItemTesting(po.id)
                    : this.service.getPurchaseOrderDetailsForRR(po.id);
                loader$.subscribe({
                    next: (items) => {
                        this.rrDetails.set(items || []);
                        this.initAllQuantitiesReceived();
                        this.updateTotals();
                    },
                    error: () => this.alertService.error(this.module, 'Failed to load PO items.', '')
                });
            }
        } catch { }
    }

    async openJOBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseJobOrderModalComponent, {}, { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const jo = result.data;
                this.selectedJO.set(jo);
                this.joDesc.set((jo.localCode || jo.code || '') + ' : ' + (jo.vendor?.name || jo.supplier || ''));
                this.rrDetails.set([]);
                this.service.getJobOrderDetailsForRR(jo.id).subscribe({
                    next: (items) => {
                        this.rrDetails.set(items || []);
                        this.initAllQuantitiesReceived();
                        this.updateTotals();
                    },
                    error: () => this.alertService.error(this.module, 'Failed to load JO items.', '')
                });
            }
        } catch { }
    }

    async openSupplierBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseSupplierModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.selectedSupplier.set(result.data);
            }
        } catch { }
    }

    async openSignatoryBrowse(field: 'notedBy' | 'approvedBy'): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                if (field === 'notedBy') this.notedBy.set(result.data);
                else this.approvedBy.set(result.data);
            }
        } catch { }
    }

    async openItemBrowse(index: number): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseItemModalComponent, {}, { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const item = result.data;
                const isDuplicate = this.rrDetails().some(
                    (r, i) => i !== index && r.itemId === item.id
                );
                if (isDuplicate) {
                    this.alertService.warning(this.module, 'Validation', 'Duplicate item.');
                    return;
                }
                const row = this.rrDetails()[index];
                row.itemId          = item.id;
                row.itemDescription = item.description;
                row.unitCode        = item.unit?.code || '';
                row.hasItem         = true;
                this.rrDetails.update(list => [...list]);
                this.updateNetAmount(index, 'quantityReceived');
            }
        } catch { }
    }

    initAllQuantitiesReceived(): void {
        for (const row of this.rrDetails()) {
            if (row.quantityReceived == null || row.quantityReceived === 0) {
                row.quantityReceived = row.remainingQuantity ?? row.quantity ?? 0;
            }
            this.recalcRowNetAmount(row);
        }
    }

    private recalcRowNetAmount(row: any): void {
        const qty  = Number(row.quantityReceived) || 0;
        const cost = Number(row.unitPrice)         || 0;
        const adj  = Number(row.adjustment)        || 0;
        row.netAmount = (qty * cost) + adj;
    }

    updateNetAmount(index: number, _field: string): void {
        const details = this.rrDetails();
        if (index < 0 || index >= details.length) return;
        this.recalcRowNetAmount(details[index]);
        this.rrDetails.update(list => [...list]);
        this.updateTotals();
    }

    updateTotals(): void {
        let qty = 0, cost = 0, qtyReceived = 0, adjustment = 0, netAmount = 0;
        for (const row of this.rrDetails()) {
            qty         += Number(row.quantity)         || 0;
            cost        += Number(row.unitPrice)         || 0;
            qtyReceived += Number(row.quantityReceived) || 0;
            adjustment  += Number(row.adjustment)       || 0;
            netAmount   += Number(row.netAmount)        || 0;
        }
        this.totals = { qty, cost, qtyReceived, adjustment, netAmount };
    }

    addIFRRow(): void {
        this.rrDetails.update(list => [...list, {
            itemId:           null,
            itemDescription:  '',
            unitCode:         '',
            quantity:         0,
            quantityReceived: 0,
            unitPrice:        0,
            adjustment:       0,
            netAmount:        0,
            hasItem:          false
        }]);
    }

    removeRow(index: number): void {
        this.rrDetails.update(list => list.filter((_, i) => i !== index));
        this.updateTotals();
    }

    compareById(a: any, b: any): boolean {
        return a && b ? a.id === b.id : a === b;
    }

    save(): void {
        if (!this.deliveryDate) {
            this.alertService.warning(this.module, 'Validation', 'Delivery Date is required.');
            return;
        }
        if ((this.isPO() || this.isTested()) && !this.selectedPO()) {
            this.alertService.warning(this.module, 'Validation', 'Please select a Purchase Order.');
            return;
        }
        if (this.isJO() && !this.selectedJO()) {
            this.alertService.warning(this.module, 'Validation', 'Please select a Job Order.');
            return;
        }
        if (this.isRV() && !this.selectedSupplier()) {
            this.alertService.warning(this.module, 'Validation', 'Please select a Supplier for the Requisition Voucher.');
            return;
        }
        if (this.rrDetails().length === 0) {
            this.alertService.warning(this.module, 'Validation', 'Please add at least one item.');
            return;
        }

        this.isLoading.set(true);

        const selectedInventoryLocation = this.selectedInventoryLocation();
        const selectedPO                = this.selectedPO();
        const selectedJO                = this.selectedJO();
        const selectedRV                = this.selectedRV();
        const selectedIFR               = this.selectedIFR();
        const selectedSupplier          = this.selectedSupplier();
        const notedBy                   = this.notedBy();
        const approvedBy                = this.approvedBy();
        const rrDetails                 = this.rrDetails();

        const payload: any = {
            deliveryDate:   this.deliveryDate,
            invoiceDate:    this.invoiceDate    || null,
            deliveryNumber: this.deliveryNumber || null,
            invoiceNumber:  this.invoiceNumber  || null,
            remarks:        this.remarks        || null,

            inventoryLocation: selectedInventoryLocation?.id
                ? { id: selectedInventoryLocation.id }
                : null,

            isJO:            this.isJO(),
            isRV:            this.isRV(),
            isRepairedItems: this.isIFR(),
            isTestedItem:    this.isTested(),

            purchaseOrder: (this.isPO() || this.isTested()) && selectedPO
                ? { id: selectedPO.id }
                : null,
            jobOrder: this.isJO() && selectedJO
                ? { id: selectedJO.id }
                : null,
            requisitionVoucher: this.isRV() && selectedRV
                ? { id: selectedRV.id }
                : null,
            itemsForRepair: this.isIFR() && selectedIFR
                ? { id: selectedIFR.id }
                : null,
            supplier: this.isRV() && selectedSupplier
                ? { accountNo: selectedSupplier.accountNo }
                : null,

            checker:          notedBy?.accountNo    ? { accountNo: notedBy.accountNo }    : null,
            approvingOfficer: approvedBy?.accountNo ? { accountNo: approvedBy.accountNo } : null,

            rrDetails: rrDetails.map(row => ({
                id:               row.id               || null,
                poDetailId:       row.poDetailId        || null,
                joDetailId:       row.joDetailId        || null,
                rvDetailId:       row.rvDetailId        || null,
                itemId:           row.itemId            || null,
                itemDescription:  row.itemDescription   || '',
                unitCode:         row.unitCode          || '',
                quantity:         Number(row.quantity)         || 0,
                quantityReceived: Number(row.quantityReceived) || 0,
                unitPrice:        Number(row.unitPrice)         || 0,
                adjustment:       Number(row.adjustment)       || 0,
                netAmount:        Number(row.netAmount)        || 0
            })),

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
