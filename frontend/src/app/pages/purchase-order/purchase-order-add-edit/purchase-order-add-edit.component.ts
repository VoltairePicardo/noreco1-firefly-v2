import { Component, ElementRef, ViewChild, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FormsModule } from '@angular/forms';
import { FlatpickrDefaults, FlatpickrModule } from 'angularx-flatpickr';
import { provideIcons } from '@ng-icons/core';
import { tablerPlus, tablerTrash, tablerSearch, tablerArrowLeft, tablerCheck, tablerPaperclip, tablerPhoto, tablerFile, tablerX } from '@ng-icons/tabler-icons';
import { AlertService } from '@/app/shared/services/alert.service';
import { LaddaModule } from 'angular2-ladda';
import { forkJoin } from 'rxjs';
import { PurchaseOrderService } from '../purchase-order.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseSupplierModalComponent } from '@/app/shared/modals/browse-supplier-modal/browse-supplier-modal.component';
import { BrowsePurchaseRequestModalComponent } from '@/app/shared/modals/browse-purchase-request-modal/browse-purchase-request-modal.component';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseCaModalComponent } from '@/app/shared/modals/browse-ca-modal/browse-ca-modal.component';

const DELIVERY_TERMS_FALLBACK = [
    { value: 'DELIVERED_TO_WAREHOUSE', label: 'Delivered to Warehouse' },
    { value: 'PICK_UP',                label: 'PICK-UP' },
    { value: 'DOOR_TO_DOOR',           label: 'DOOR TO DOOR' }
];

@Component({
    selector: 'app-purchase-order-add-edit',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FormsModule, FlatpickrModule, LaddaModule, RouterLink],
    providers: [FlatpickrDefaults, provideIcons({ tablerPlus, tablerTrash, tablerSearch, tablerArrowLeft, tablerCheck, tablerPaperclip, tablerPhoto, tablerFile, tablerX }), ...SHARED_PROVIDERS],
    templateUrl: './purchase-order-add-edit.component.html'
})
export class PurchaseOrderAddEditComponent {
    module    = 'Purchase Order';
    subModule = 'Create';
    menuLink  = 'purchase-order';

    @ViewChild('fileInput') fileInputRef!: ElementRef<HTMLInputElement>;

    id: any    = null;
    editMode   = false;
    formSubmit = false;
    isLoading  = signal(false);

    // Staged attachments (uploaded after save)
    attachments: { file: File; name: string; url: string }[] = [];

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // Header fields
    voucherDate               = '';
    vendor: any               = null;
    deliveryTerm              = '';
    deliveryAddress           = '';
    deliveryTimeAndCompletion = '';
    paymentTerm: number | null = null;
    purpose                   = '';

    // New fields
    purchaseRequest: any = null;
    useCreditCard        = false;
    cashAdvance: any     = null;
    budgetCheckedBy: any = null;
    checkedBy: any       = null;
    approvedBy: any      = null;

    // Reference data
    deliveryTerms = signal<any[]>(DELIVERY_TERMS_FALLBACK);
    lineItems: any[] = [];

    private service      = inject(PurchaseOrderService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.service.getDeliveryTerms().subscribe({
            next: (terms) => { if (terms?.length) this.deliveryTerms.set(terms); },
            error: () => {}
        });

        this.route.paramMap.subscribe(params => {
            const id = params.get('id');
            if (id && /^\d+$/.test(id)) {
                this.id       = +id;
                this.editMode = true;
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
                    const mm = String(d.getMonth() + 1).padStart(2, '0');
                    const dd = String(d.getDate()).padStart(2, '0');
                    this.voucherDate = `${d.getFullYear()}-${mm}-${dd}`;

                    this.vendor                   = header.vendor;
                    this.deliveryTerm             = header.deliveryTerm || '';
                    this.deliveryAddress          = header.deliveryAddress || '';
                    this.deliveryTimeAndCompletion = header.deliveryTimeAndCompletion || '';
                    this.paymentTerm              = header.paymentTerm ?? null;
                    this.purpose                  = header.purpose || '';

                    // New fields
                    this.purchaseRequest = header.purchaseRequest || null;
                    this.useCreditCard   = header.useCreditCard || false;
                    this.cashAdvance     = header.cashAdvance || null;
                    this.budgetCheckedBy = header.budgetCheckedBy || null;
                    this.checkedBy       = header.checkedBy || null;
                    this.approvedBy      = header.approvedBy || null;

                    this.lineItems = (details || []).map((d: any) => ({
                        rvDetailId:      d.rvDetailId,
                        rvNumber:        d.requisitionVoucherCode || d.rvNumber || '',
                        itemCode:        d.itemCode || '',
                        itemDescription: d.itemDescription || '',
                        unitCode:        d.unitCode || '',
                        quantity:        d.quantity || 0,
                        unitPrice:       d.unitPrice || 0,
                        vat:             d.vat || 0,
                        discount:        d.discount || 0,
                        brand:           d.brand?.name || d.brand || '',
                    }));
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load purchase order.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    // ─── Purchase Request Browse ──────────────────────────────────────────────

    async openPurchaseRequestBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowsePurchaseRequestModalComponent,
                { type: 'po', title: 'Browse Purchase Request' },
                { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const pr = result.data;
                this.purchaseRequest = { localCode: pr.code, id: pr.id };
                this.service.getPurchaseRequestItems(pr.id).subscribe({
                    next: (items) => {
                        const existing = new Set(this.lineItems.map((li: any) => li.rvDetailId));
                        const toAdd = (items || []).filter((item: any) => !existing.has(item.id));
                        toAdd.forEach((item: any) => {
                            this.lineItems.push({
                                rvDetailId:      item.id,
                                rvNumber:        pr.code,
                                itemCode:        item.itemCode || '',
                                itemDescription: item.itemDescription || '',
                                unitCode:        item.unitCode || '',
                                quantity:        item.quantity || 0,
                                unitPrice:       0,
                                vat:             0,
                                discount:        0,
                                brand:           '',
                            });
                        });
                        if (this.vendor?.accountNumber && toAdd.length > 0) {
                            this.fetchCanvassPricesForItems(this.vendor.accountNumber, toAdd.map((i: any) => i.id));
                        }
                    },
                    error: () => this.alertService.error(this.module, 'Failed to load PR items.', '')
                });
            }
        } catch { }
    }

    // ─── Vendor Browse ────────────────────────────────────────────────────────

    async openVendorBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseSupplierModalComponent,
                {},
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.vendor = result.data;
                if (this.lineItems.length > 0 && result.data?.accountNumber) {
                    this.fetchCanvassPrices(result.data.accountNumber);
                }
            }
        } catch {
            // dismissed — no action
        }
    }

    clearVendor(): void {
        this.vendor = null;
    }

    // ─── Cash Advance Browse ──────────────────────────────────────────────────

    async openCashAdvanceBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseCaModalComponent,
                {},
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.cashAdvance = result.data;
            }
        } catch { }
    }

    // ─── Signatory Browse ─────────────────────────────────────────────────────

    async openSignatoryBrowse(field: 'budgetCheckedBy' | 'checkedBy' | 'approvedBy'): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent,
                {},
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this[field] = result.data;
            }
        } catch { }
    }

    fetchCanvassPrices(supplierAccountNo: number): void {
        this.lineItems.forEach(li => {
            if (li.rvDetailId) {
                this.service.getCanvassPrice(supplierAccountNo, li.rvDetailId).subscribe({
                    next: (price) => { if (price) li.unitPrice = price; },
                    error: () => {}
                });
            }
        });
    }

    fetchCanvassPricesForItems(supplierAccountNo: number, rvDetailIds: number[]): void {
        const targets = this.lineItems.filter(li => rvDetailIds.includes(li.rvDetailId));
        targets.forEach(li => {
            this.service.getCanvassPrice(supplierAccountNo, li.rvDetailId).subscribe({
                next: (price) => { if (price) li.unitPrice = price; },
                error: () => {}
            });
        });
    }

    removeLineItem(index: number): void {
        this.lineItems.splice(index, 1);
    }

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

    // ─── File Attachments ────────────────────────────────────────────────────

    openFilePicker(): void {
        this.fileInputRef?.nativeElement.click();
    }

    onFileSelect(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (!input.files?.length) return;
        for (const file of Array.from(input.files)) {
            const allowed = ['image/jpeg', 'image/png', 'application/pdf'];
            if (!allowed.includes(file.type)) {
                this.alertService.warning(this.module, 'Invalid file type', `${file.name} must be JPG, PNG, or PDF.`);
                continue;
            }
            this.attachments.push({ file, name: file.name, url: URL.createObjectURL(file) });
        }
        input.value = '';
    }

    removeAttachment(index: number): void {
        URL.revokeObjectURL(this.attachments[index].url);
        this.attachments.splice(index, 1);
    }

    isImage(attachment: { name: string }): boolean {
        return /\.(jpg|jpeg|png)$/i.test(attachment.name);
    }

    private uploadStagedFiles(poId: number): void {
        if (!this.attachments.length) return;
        const formData = new FormData();
        this.attachments.forEach(a => formData.append(`file_${a.name}`, a.file, a.name));
        this.service.uploadFiles(poId, formData).subscribe({ error: () => {} });
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
            voucherDate:             this.voucherDate,
            vendor:                  { accountNo: this.vendor.accountNumber },
            deliveryTerm:            this.deliveryTerm || null,
            deliveryAddress:         this.deliveryAddress || null,
            deliveryTimeAndCompletion: this.deliveryTimeAndCompletion || null,
            paymentTerm:             this.paymentTerm || null,
            purpose:                 this.purpose || null,
            amount:                  this.totalAmount,
            useCreditCard:           this.useCreditCard,
            purchaseRequest:         this.purchaseRequest?.id ? { id: this.purchaseRequest.id } : null,
            cashAdvance:             this.cashAdvance?.id     ? { id: this.cashAdvance.id }     : null,
            budgetCheckedBy:         this.budgetCheckedBy?.accountNo ? { accountNo: this.budgetCheckedBy.accountNo } : null,
            checkedBy:               this.checkedBy?.accountNo       ? { accountNo: this.checkedBy.accountNo }       : null,
            approvingOfficer:        this.approvedBy?.accountNo      ? { accountNo: this.approvedBy.accountNo }      : null,
            poDetails: this.lineItems.map(li => ({
                rvDetailId:      li.rvDetailId,
                rvNumber:        li.rvNumber,
                itemCode:        li.itemCode,
                itemDescription: li.itemDescription,
                unitCode:        li.unitCode,
                quantity:        li.quantity,
                unitPrice:       li.unitPrice || 0,
                vat:             li.vat || 0,
                discount:        li.discount || 0,
                brand:           li.brand ? { name: li.brand } : null,
            }))
        };

        if (this.editMode) {
            payload.id = this.id;
        }

        const req = this.editMode
            ? this.service.update(payload)
            : this.service.create(payload);

        req.subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    const poId = res.modelId || this.id;
                    this.uploadStagedFiles(poId);
                    this.alertService.success(this.module, res.successMessage || 'Saved successfully.', '');
                    this.router.navigate(['/' + this.menuLink, poId, 'detail']);
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
