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
import { AttachmentFile, AttachmentsComponent } from '@/app/shared/components/attachments/attachments.component';
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
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FormsModule, FlatpickrModule, LaddaModule, RouterLink, AttachmentsComponent],
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

    // Attachments
    newFiles: File[]        = [];   // staged, uploaded after save
    existingFiles: any[]    = [];
    filesToRemove: any[]    = [];   // deleted after save
    attachmentItems: AttachmentFile[] = [];   // existing + staged, shown by <app-attachments>
    private tempIds    = new Map<File, number>();
    private nextTempId = -1;

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
    approvedBy: any      = null;
    checkedBy: any       = null;
    budgetCheckedBy: any = null;
    auditedBy: any       = null;
    recommendingOfficer: any = null;

    // Budget line item (auto-populated from PR)
    budgetLineItemDetail: any       = null;
    budgetAmountBalanceCV: number | null   = null;
    budgetAmountBalancePOJO: number | null = null;

    vatRate = 12; // overwritten from INPUT_TAX_RATE setting on init

    // Reference data
    deliveryTerms = signal<any[]>(DELIVERY_TERMS_FALLBACK);
    brands: any[]    = [];
    lineItems: any[] = [];

    private service      = inject(PurchaseOrderService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    compareBrand(a: any, b: any): boolean { return a?.id === b?.id; }

    ngOnInit(): void {
        this.service.getDeliveryTerms().subscribe({
            next: (terms) => { if (terms?.length) this.deliveryTerms.set(terms); },
            error: () => {}
        });
        this.service.getBrands().subscribe({
            next: (res: any) => { this.brands = res.content ?? res ?? []; },
            error: () => {}
        });
        this.service.getSetting('INPUT_TAX_RATE').subscribe({
            next: (v: any) => { if (v != null) this.vatRate = Number(v) || 12; },
            error: () => {}
        });

        this.route.paramMap.subscribe(params => {
            const id = params.get('id');
            if (id && /^\d+$/.test(id)) {
                this.id       = +id;
                this.editMode = true;
                this.subModule = 'Edit';
                this.loadForEdit();
            } else {
                // Create mode: default voucherDate to today
                const today = new Date();
                const yyyy = today.getFullYear();
                const mm = String(today.getMonth() + 1).padStart(2, '0');
                const dd = String(today.getDate()).padStart(2, '0');
                this.voucherDate = `${yyyy}-${mm}-${dd}`;
                this.loadDefaultSignatories();
            }
        });
    }

    loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (data) => {
                if (!data) return;
                this.budgetCheckedBy     = data.budgetCheckedBy     || null;
                this.checkedBy           = data.checkedBy           || null;
                this.recommendingOfficer = data.recommendingOfficer || null;
                this.auditedBy           = data.auditedBy           || null;
                this.approvedBy          = data.approvedBy          || null;
            },
            error: () => {}
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
                    this.loadAttachments();
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
                    this.purchaseRequest      = header.purchaseRequest      || null;
                    this.useCreditCard        = header.useCreditCard        || false;
                    this.cashAdvance          = header.cashAdvance          || null;
                    this.approvedBy           = header.approvedBy           || null;
                    this.checkedBy            = header.checkedBy            || null;
                    this.budgetCheckedBy      = header.budgetCheckedBy      || null;
                    this.auditedBy            = header.auditedBy            || null;
                    this.recommendingOfficer  = header.recommendingOfficer  || null;
                    this.budgetLineItemDetail = header.budgetLineItemDetail  || null;

                    this.lineItems = (details || []).map((d: any) => ({
                        rvDetailId:      d.rvDetailId,
                        rvNumber:        d.requisitionVoucherCode || d.rvNumber || '',
                        itemCode:        d.itemCode || '',
                        itemDescription: d.itemDescription || '',
                        unitCode:        d.unitCode || '',
                        quantity:        d.quantity || 0,
                        unitPrice:       d.unitPrice || 0,
                        vat:             d.vatPercentage ?? 0,
                        discount:        d.discountPercentage ?? 0,
                        brand:           d.brand || null,
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

                // 7a: copy purpose
                if (pr.purpose) this.purpose = pr.purpose;

                // 7b: budget line item + balances
                this.budgetLineItemDetail    = pr.budgetLineItemDetail ?? null;
                this.budgetAmountBalanceCV   = null;
                this.budgetAmountBalancePOJO = null;
                if (this.budgetLineItemDetail?.id) {
                    this.service.getBudgetLineItemBalance(this.budgetLineItemDetail.id).subscribe({
                        next: (bal) => {
                            this.budgetAmountBalanceCV   = bal?.amountBalanceCV   ?? null;
                            this.budgetAmountBalancePOJO = bal?.amountBalancePOJO ?? null;
                        },
                        error: () => {}
                    });
                }

                // 7c: awarded SOQ terms + awarded supplier (AOQ), then load items so
                // the supplier / VAT status is known before prices are fetched
                this.service.getAwardedSoqTerms(pr.id).subscribe({
                    next: (terms: any) => {
                        if (terms) {
                            if (terms.termsOfPayment           != null) this.paymentTerm               = terms.termsOfPayment;
                            if (terms.placeOfDelivery)                  this.deliveryAddress           = terms.placeOfDelivery;
                            if (terms.deliveryTimeAndCompletion)        this.deliveryTimeAndCompletion = terms.deliveryTimeAndCompletion;
                            if (terms.supplier) {
                                this.vendor = terms.supplier;
                                this.applyVatToLineItems();
                            }
                        }
                        this.loadPurchaseRequestItems(pr);
                    },
                    error: () => this.loadPurchaseRequestItems(pr)
                });
            }
        } catch { }
    }

    private loadPurchaseRequestItems(pr: any): void {
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
                        vat:             this.vendorVatRate,
                        discount:        0,
                        brand:           null,
                    });
                });
                if (this.vendorAccountNo && toAdd.length > 0) {
                    this.fetchSoqDataForItems(this.vendorAccountNo, toAdd.map((i: any) => i.id));
                }
            },
            error: () => this.alertService.error(this.module, 'Failed to load PR items.', '')
        });
    }

    // Supplier (browse modal) exposes accountNumber; SlEntity (loaded PO / awarded terms) exposes accountNo
    private get vendorAccountNo(): number | null {
        return this.vendor?.accountNumber ?? this.vendor?.accountNo ?? null;
    }

    private get vendorVatRate(): number {
        return this.vendor?.vatable ? this.vatRate : 0;
    }

    private applyVatToLineItems(): void {
        const vatPct = this.vendorVatRate;
        this.lineItems.forEach(li => li.vat = vatPct);
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
                // Auto-set VAT% on all line items based on supplier vatable status
                this.applyVatToLineItems();
                if (this.lineItems.length > 0 && result.data?.accountNumber) {
                    this.fetchSoqData(result.data.accountNumber);
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

    async openSignatoryBrowse(field: 'approvedBy' | 'checkedBy' | 'budgetCheckedBy' | 'auditedBy' | 'recommendingOfficer'): Promise<void> {
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

    fetchSoqData(supplierAccountNo: number): void {
        this.lineItems.forEach(li => {
            if (li.rvDetailId) {
                this.service.getSoqData(supplierAccountNo, li.rvDetailId).subscribe({
                    next: (data: any) => {
                        if (data?.price != null) li.unitPrice = data.price;
                        if (data?.brand != null) li.brand = data.brand;
                    },
                    error: () => {}
                });
            }
        });
    }

    fetchSoqDataForItems(supplierAccountNo: number, rvDetailIds: number[]): void {
        const targets = this.lineItems.filter(li => rvDetailIds.includes(li.rvDetailId));
        targets.forEach(li => {
            this.service.getSoqData(supplierAccountNo, li.rvDetailId).subscribe({
                next: (data: any) => {
                    if (data?.price != null) li.unitPrice = data.price;
                    if (data?.brand != null) li.brand = data.brand;
                },
                error: () => {}
            });
        });
    }

    removeLineItem(index: number): void {
        this.lineItems.splice(index, 1);
    }

    // ─── Computed ────────────────────────────────────────────────────────────

    lineItemAmount(li: any): number {
        const price = +(li.unitPrice || 0);  // VAT-inclusive SOQ price
        const qty   = +(li.quantity  || 0);
        const disc  = +(li.discount  || 0);
        return price * qty * (1 - disc / 100);
    }

    get totalAmount(): number {
        return this.lineItems.reduce((sum, li) => sum + this.lineItemAmount(li), 0);
    }

    // Prices are VAT-inclusive: net = gross / (1 + rate), vat = gross - net
    lineItemNet(li: any): number {
        return this.lineItemAmount(li) / (1 + (+(li.vat || 0)) / 100);
    }

    get totalNetOfVat(): number {
        return this.lineItems.reduce((sum, li) => sum + this.lineItemNet(li), 0);
    }

    get totalVat(): number {
        return this.totalAmount - this.totalNetOfVat;
    }

    // ─── File Attachments ────────────────────────────────────────────────────

    openFilePicker(): void {
        this.fileInputRef?.nativeElement.click();
    }

    onFileSelect(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (!input.files?.length) return;
        const allowed = ['image/jpeg', 'image/png', 'application/pdf'];
        const duplicates: string[] = [];
        for (const file of Array.from(input.files)) {
            if (!allowed.includes(file.type)) {
                this.alertService.warning(this.module, 'Invalid file type', `${file.name} must be JPG, PNG, or PDF.`);
                continue;
            }
            if (this.isDuplicateFile(file)) {
                duplicates.push(file.name);
                continue;
            }
            this.newFiles.push(file);
        }
        this.refreshAttachments();
        if (duplicates.length > 0) {
            this.alertService.warning(this.module, 'Duplicate file skipped', duplicates.join(', '));
        }
        input.value = '';
    }

    // same file already picked (name + size + last modified), or a saved attachment with the same name
    private isDuplicateFile(file: File): boolean {
        return this.newFiles.some(s => s.name === file.name && s.size === file.size && s.lastModified === file.lastModified)
            || this.existingFiles.some(e => e.originalFilename === file.name);
    }

    loadAttachments(): void {
        this.service.getFiles(this.id).subscribe({
            next: (files) => { this.existingFiles = files || []; this.refreshAttachments(); },
            error: () => { this.existingFiles = []; this.refreshAttachments(); }
        });
    }

    fileUrl = (fileId: number): string => this.service.fileUrl(fileId);

    onRemoveAttachment(item: AttachmentFile): void {
        if (item.local) {
            const [removed] = this.newFiles.splice(this.newFiles.indexOf(item.local), 1);
            if (removed) this.tempIds.delete(removed);
        } else {
            const [removed] = this.existingFiles.splice(this.existingFiles.findIndex(f => f.id === item.id), 1);
            if (removed) this.filesToRemove.push(removed);
        }
        this.refreshAttachments();
    }

    // staged files get a negative temp id so they never clash with saved file ids
    private refreshAttachments(): void {
        this.attachmentItems = [
            ...this.existingFiles.map(f => ({ id: f.id, originalFilename: f.originalFilename, mimeType: f.mimeType })),
            ...this.newFiles.map(f => {
                if (!this.tempIds.has(f)) this.tempIds.set(f, this.nextTempId--);
                return { id: this.tempIds.get(f)!, originalFilename: f.name, mimeType: f.type, local: f };
            })
        ];
    }

    private syncFiles(poId: number): void {
        this.filesToRemove.forEach(f => this.service.deleteFile(f.id).subscribe({ error: () => {} }));
        if (!this.newFiles.length) return;
        const formData = new FormData();
        this.newFiles.forEach(f => formData.append(`file_${f.name}`, f, f.name));
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
        if (!this.approvedBy) {
            this.alertService.warning(this.module, 'Please select an Approved By officer.', '');
            return;
        }

        this.formSubmit = true;

        const payload: any = {
            voucherDate:             this.voucherDate,
            vendor:                  { accountNo: this.vendorAccountNo },
            deliveryTerm:            this.deliveryTerm || null,
            deliveryAddress:         this.deliveryAddress || null,
            deliveryTimeAndCompletion: this.deliveryTimeAndCompletion || null,
            paymentTerm:             this.paymentTerm || null,
            purpose:                 this.purpose || null,
            amount:                  this.totalAmount,
            useCreditCard:           this.useCreditCard,
            purchaseRequest:         this.purchaseRequest?.id ? { id: this.purchaseRequest.id } : null,
            cashAdvance:             this.cashAdvance?.id     ? { id: this.cashAdvance.id }     : null,
            approvingOfficer:        this.approvedBy?.accountNo      ? { accountNo: this.approvedBy.accountNo }      : null,
            checkedBy:               this.checkedBy?.accountNo       ? { accountNo: this.checkedBy.accountNo }       : null,
            budgetCheckedBy:         this.budgetCheckedBy?.accountNo ? { accountNo: this.budgetCheckedBy.accountNo } : null,
            auditedBy:               this.auditedBy?.accountNo       ? { accountNo: this.auditedBy.accountNo }       : null,
            recommendingOfficer:     this.recommendingOfficer?.accountNo ? { accountNo: this.recommendingOfficer.accountNo } : null,
            budgetLineItemDetail:    this.budgetLineItemDetail?.id   ? { id: this.budgetLineItemDetail.id }          : null,
            poDetails: this.lineItems.map(li => ({
                rvDetailId:      li.rvDetailId,
                rvNumber:        li.rvNumber,
                itemCode:        li.itemCode,
                itemDescription: li.itemDescription,
                unitCode:        li.unitCode,
                quantity:        li.quantity,
                unitPrice:          li.unitPrice || 0,  // VAT-inclusive SOQ price; backend divides out VAT
                vatPercentage:      +(li.vat     || 0),  // percentage e.g. 12
                discountPercentage: +(li.discount || 0),  // percentage e.g. 5
                vat:                0,  // backend computes amount
                discount:           0,  // backend computes amount
                brand:           li.brand?.id ? { id: li.brand.id } : null,
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
                    this.syncFiles(poId);
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
