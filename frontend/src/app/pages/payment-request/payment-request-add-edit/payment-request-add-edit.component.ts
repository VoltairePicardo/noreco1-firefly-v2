import { Component, inject, signal, ViewChild, ElementRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { LaddaModule } from 'angular2-ladda';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { PaymentRequestService } from '../payment-request.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseBudgetLineItemModalComponent } from '@/app/shared/modals/browse-budget-line-item-modal/browse-budget-line-item-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerPlus, tablerX, tablerCheck, tablerArrowLeft, tablerPaperclip, tablerPhoto, tablerFile, tablerTrash } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-payment-request-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        LaddaModule,
        FlatpickrDirective
    ],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS,
        provideIcons({ tablerSearch, tablerPlus, tablerX, tablerCheck, tablerArrowLeft, tablerPaperclip, tablerPhoto, tablerFile, tablerTrash })],
    templateUrl: './payment-request-add-edit.component.html'
})
export class PaymentRequestAddEditComponent {
    @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

    module    = 'Payment Request';
    subModule = 'Create';
    menuLink  = 'payment-request';

    id: any    = null;
    editMode   = false;
    formSubmit = false;
    isLoading  = signal(false);

    // Form fields
    voucherDate   = '';
    invoiceDate   = '';
    invoiceNumber = '';
    dueDate       = '';
    vendor: any   = null;

    // Line items (payment request details)
    lineItems: any[] = [];

    // Budget line items
    budgetLineItems     = signal<any[]>([]);   // available from API
    budgetLineItemRows: any[] = [];            // selected rows

    // Budget sub items
    budgetSubItemRows: { description: string, amount: number }[] = [];

    // Attachments (staged for upload)
    stagedFiles: File[]  = [];
    uploadingFiles       = false;

    flatpickrOptions = {
        dateFormat: 'Y-m-d',
        altInput: true,
        altFormat: 'F j, Y'
    };

    private service      = inject(PaymentRequestService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private modalService = inject(ModalService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.setDefaultDates();

        this.service.getBudgetLineItems().subscribe({
            next: (items) => this.budgetLineItems.set(items || []),
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
                this.addLineItem();
            }
        });
    }

    private setDefaultDates(): void {
        const today = new Date();
        const fmt = (d: Date) =>
            `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
        const todayStr = fmt(today);
        this.voucherDate = todayStr;
        this.invoiceDate = todayStr;
        this.dueDate     = todayStr;
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    const d = data;
                    this.voucherDate   = d.voucherDate   ? new Date(d.voucherDate).toISOString().substring(0, 10)   : '';
                    this.invoiceDate   = d.invoiceDate   ? new Date(d.invoiceDate).toISOString().substring(0, 10)   : '';
                    this.dueDate       = d.dueDate       ? new Date(d.dueDate).toISOString().substring(0, 10)       : '';
                    this.invoiceNumber = d.invoiceNumber || '';
                    this.vendor        = d.vendor        || null;
                    this.lineItems     = (d.paymentRequestDetails || []).map((item: any) => ({
                        description: item.description || '',
                        amount:      item.amount      || 0
                    }));
                    if (this.lineItems.length === 0) this.addLineItem();
                    this.budgetLineItemRows = (d.budgetLineItemDetails || []).map((bli: any) => ({
                        id:    bli.id    || bli.budgetLineItemDetail?.id,
                        code:  bli.code  || bli.budgetLineItemDetail?.code  || '',
                        title: bli.title || bli.budgetLineItemDetail?.title || ''
                    }));
                    this.budgetSubItemRows = (d.budgetDetails || []).map((bs: any) => ({
                        description: bs.description || '',
                        amount:      bs.amount      || 0
                    }));
                } else {
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Error loading data', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    // ── Line items ──────────────────────────────────────────────────────────
    addLineItem(): void {
        this.lineItems.push({ description: '', amount: 0 });
    }

    removeLineItem(index: number): void {
        this.lineItems.splice(index, 1);
    }

    get totalAmount(): number {
        return this.lineItems.reduce((sum, item) => sum + (Number(item.amount) || 0), 0);
    }

    // ── Budget line items ────────────────────────────────────────────────────
    async openBudgetLineItemBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseBudgetLineItemModalComponent,
                { items: this.budgetLineItems() },
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const item = result.data;
                const exists = this.budgetLineItemRows.some(r => r.id === item.id);
                if (!exists) {
                    this.budgetLineItemRows.push({ id: item.id, code: item.code, title: item.title });
                }
            }
        } catch { }
    }

    removeBudgetLineItem(index: number): void {
        this.budgetLineItemRows.splice(index, 1);
    }

    // ── Budget sub items ─────────────────────────────────────────────────────
    addBudgetSubItem(): void {
        this.budgetSubItemRows.push({ description: '', amount: 0 });
    }

    removeBudgetSubItem(index: number): void {
        this.budgetSubItemRows.splice(index, 1);
    }

    get budgetSubTotal(): number {
        return this.budgetSubItemRows.reduce((sum, item) => sum + (Number(item.amount) || 0), 0);
    }

    // ── Vendor browse ────────────────────────────────────────────────────────
    async openVendorBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseEntityModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this.vendor = result.data;
            }
        } catch { }
    }

    clearVendor(): void {
        this.vendor = null;
    }

    // ── File attachments ─────────────────────────────────────────────────────
    openFilePicker(): void {
        this.fileInput.nativeElement.click();
    }

    onFileSelect(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files) {
            const allowed = ['image/jpeg', 'image/png', 'application/pdf'];
            Array.from(input.files).forEach(f => {
                if (allowed.includes(f.type)) {
                    this.stagedFiles.push(f);
                }
            });
        }
        input.value = '';
    }

    removeStagedFile(index: number): void {
        this.stagedFiles.splice(index, 1);
    }

    isImage(file: File): boolean {
        return file.type.startsWith('image/');
    }

    // ── Save ─────────────────────────────────────────────────────────────────
    save(): void {
        if (!this.voucherDate) {
            this.alertService.warning(this.module, 'Validation', 'Please enter a voucher date.');
            return;
        }
        if (!this.invoiceDate) {
            this.alertService.warning(this.module, 'Validation', 'Please enter an invoice/request date.');
            return;
        }
        if (!this.dueDate) {
            this.alertService.warning(this.module, 'Validation', 'Please enter a due date.');
            return;
        }
        if (!this.vendor) {
            this.alertService.warning(this.module, 'Validation', 'Please select a vendor/payee.');
            return;
        }
        if (this.lineItems.length === 0) {
            this.alertService.warning(this.module, 'Validation', 'Please add at least one line item.');
            return;
        }
        if (this.budgetLineItemRows.length === 0) {
            this.alertService.warning(this.module, 'Validation', 'Please select at least one budget line item.');
            return;
        }

        this.formSubmit = true;

        const payload: any = {
            id:            this.editMode ? this.id : null,
            voucherDate:   this.voucherDate,
            invoiceDate:   this.invoiceDate   || null,
            invoiceNumber: this.invoiceNumber || null,
            dueDate:       this.dueDate       || null,
            vendor:        { accountNo: this.vendor.accountNo },
            amount:        this.totalAmount,
            paymentRequestDetails: this.lineItems.map(item => ({
                description: item.description,
                amount:      item.amount
            })),
            budgetLineItemDetails: this.budgetLineItemRows.map(r => ({ id: r.id })),
            budgetDetails:         this.budgetSubItemRows.map(r => ({
                description: r.description,
                amount:      r.amount
            }))
        };

        const req$ = this.editMode ? this.service.update(payload) : this.service.create(payload);

        req$.subscribe({
            next: (res) => {
                if (res.success) {
                    const savedId = res.modelId || this.id;
                    if (this.stagedFiles.length > 0 && savedId) {
                        this.uploadAndNavigate(savedId);
                    } else {
                        this.formSubmit = false;
                        this.alertService.success(this.module, 'Saved', '');
                        this.router.navigate(['/' + this.menuLink, savedId, 'detail']);
                    }
                } else {
                    this.formSubmit = false;
                    this.alertService.error(this.module, 'Saving', res.failureMessage);
                }
            },
            error: () => {
                this.formSubmit = false;
                this.alertService.error(this.module, 'Saving', '');
            }
        });
    }

    private uploadAndNavigate(savedId: number): void {
        const formData = new FormData();
        this.stagedFiles.forEach(f => formData.append('files', f, f.name));
        this.service.uploadFiles(savedId, formData).subscribe({
            next: () => {
                this.formSubmit = false;
                this.alertService.success(this.module, 'Saved', '');
                this.router.navigate(['/' + this.menuLink, savedId, 'detail']);
            },
            error: () => {
                this.formSubmit = false;
                this.alertService.success(this.module, 'Saved', 'Record saved but file upload failed.');
                this.router.navigate(['/' + this.menuLink, savedId, 'detail']);
            }
        });
    }
}
