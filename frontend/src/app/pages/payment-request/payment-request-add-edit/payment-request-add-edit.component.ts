import { Component, inject, signal, ViewChild, ElementRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
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
import { AttachmentFile, AttachmentsComponent } from '@/app/shared/components/attachments/attachments.component';
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
        FlatpickrDirective,
        AttachmentsComponent
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
    budgetLineItemRows: any[] = [];            // selected rows (with CV / PO-JO-RFP balances)

    // Budget sub items
    subItemOptions: { id: number, description: string, lineItemId: number }[] = [];  // from selected line items
    budgetSubItemRows: { budgetSubItemId: number | null, description: string, amount: number, balanceCV: number, balancePOJO: number }[] = [];

    // Cash flow balances (kept as loaded on edit, as in the old system)
    cashFlowAmountBalancePOJORFP: number | null = null;
    cashFlowAmountBalanceCV: number | null      = null;

    // Attachments
    stagedFiles: File[]  = [];     // new files, sent as file_<i>
    existingFiles: any[] = [];     // already saved (edit mode)
    filesToRemove: any[] = [];     // [{ id: <fileId> }] sent as filesToRemove
    attachmentItems: AttachmentFile[] = [];   // existing + staged, shown by <app-attachments>
    private tempIds = new Map<File, number>();
    private nextTempId = -1;
    uploadingFiles       = false;

    flatpickrOptions = {
        dateFormat: 'Y-m-d',
        altInput: true,
        altFormat: 'F j, Y'
    };

    readonlyFlatpickrOptions = { ...this.flatpickrOptions, clickOpens: false };

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
                    this.cashFlowAmountBalancePOJORFP = d.cashFlowAmountBalancePOJORFP ?? null;
                    this.cashFlowAmountBalanceCV      = d.cashFlowAmountBalanceCV      ?? null;
                    this.lineItems     = (d.paymentRequestDetails || []).map((item: any) => ({
                        description: item.description || '',
                        amount:      item.amount      || 0
                    }));
                    if (this.lineItems.length === 0) this.addLineItem();
                    this.budgetLineItemRows = (d.paymentRequestBudgetLineItemDetails || []).map((bli: any) => ({
                        id:          bli.budgetLineItemDetail?.id,
                        code:        bli.budgetLineItemDetail?.code        || '',
                        title:       bli.budgetLineItemDetail?.title       || '',
                        hasSubItems: !!bli.budgetLineItemDetail?.hasSubItems,
                        budgetAmountBalanceCV:       bli.budgetAmountBalanceCV       ?? 0,
                        budgetAmountBalancePOJORFP:  bli.budgetAmountBalancePOJORFP  ?? 0
                    }));
                    this.budgetLineItemRows.filter(r => r.hasSubItems).forEach(r => this.loadSubItemOptions(r.id));

                    this.service.getBudgetDetails(this.id).subscribe({
                        next: (rows) => {
                            this.budgetSubItemRows = (rows || []).map((bs: any) => ({
                                budgetSubItemId: bs.budgetSubItemId ?? null,
                                description:     bs.description || '',
                                amount:          bs.amount      || 0,
                                balanceCV:       0,
                                balancePOJO:     0
                            }));
                            this.budgetSubItemRows.forEach(r => this.loadSubItemBalances(r));
                        },
                        error: () => this.alertService.warning(this.module, 'Budget sub items not loaded', '')
                    });
                    this.service.getFiles(this.id).subscribe({
                        next: (files) => { this.existingFiles = files || []; this.refreshAttachments(); },
                        error: () => {}
                    });
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
                    const row = {
                        id: item.id, code: item.code, title: item.title,
                        hasSubItems: !!item.hasSubItems,
                        budgetAmountBalanceCV: 0, budgetAmountBalancePOJORFP: 0
                    };
                    this.budgetLineItemRows.push(row);
                    this.loadBalances(row);
                    if (row.hasSubItems) this.loadSubItemOptions(row.id);
                }
            }
        } catch { }
    }

    removeBudgetLineItem(index: number): void {
        const [removed] = this.budgetLineItemRows.splice(index, 1);
        if (removed) {
            this.subItemOptions = this.subItemOptions.filter(o => o.lineItemId !== removed.id);
            this.budgetSubItemRows = this.budgetSubItemRows.filter(r =>
                r.budgetSubItemId == null || this.subItemOptions.some(o => o.id === r.budgetSubItemId));
        }
    }

    // balances are saved with the document (same as the old system)
    private loadBalances(row: any): void {
        forkJoin({
            cv:    this.service.getBudgetLineItemDetailBalance(row.id, 'CV'),
            pojo:  this.service.getBudgetLineItemDetailBalance(row.id)
        }).subscribe({
            next: ({ cv, pojo }) => {
                row.budgetAmountBalanceCV      = cv   ?? 0;
                row.budgetAmountBalancePOJORFP = pojo ?? 0;
            },
            error: () => this.alertService.warning(this.module, 'Failed to load budget amount balance', '')
        });
    }

    private loadSubItemOptions(lineItemId: number): void {
        this.service.getBudgetSubItems(lineItemId).subscribe({
            next: (items) => {
                const others = this.subItemOptions.filter(o => o.lineItemId !== lineItemId);
                this.subItemOptions = [...others, ...(items || []).map((i: any) => ({
                    id: i.id, description: i.description, lineItemId
                }))];
            },
            error: () => {}
        });
    }

    // ── Budget sub items ─────────────────────────────────────────────────────
    addBudgetSubItem(): void {
        this.budgetSubItemRows.push({ budgetSubItemId: null, description: '', amount: 0, balanceCV: 0, balancePOJO: 0 });
    }

    onSubItemSelect(index: number): void {
        const row = this.budgetSubItemRows[index];
        const opt = this.subItemOptions.find(o => o.id === Number(row.budgetSubItemId));
        row.budgetSubItemId = opt ? opt.id : null;
        row.description     = opt ? opt.description : '';
        row.balanceCV       = 0;
        row.balancePOJO     = 0;
        if (opt) this.loadSubItemBalances(row);
    }

    private loadSubItemBalances(row: { budgetSubItemId: number | null, balanceCV: number, balancePOJO: number }): void {
        if (!row.budgetSubItemId) return;
        forkJoin({
            cv:   this.service.getBudgetSubItemBalance(row.budgetSubItemId, 'CV'),
            pojo: this.service.getBudgetSubItemBalance(row.budgetSubItemId, 'POJO')
        }).subscribe({
            next: ({ cv, pojo }) => {
                row.balanceCV   = cv   ?? 0;
                row.balancePOJO = pojo ?? 0;
            },
            error: () => this.alertService.warning(this.module, 'Failed to load budget amount balance', '')
        });
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
            const duplicates: string[] = [];
            Array.from(input.files).forEach(f => {
                if (!allowed.includes(f.type)) return;
                if (this.isDuplicateFile(f)) {
                    duplicates.push(f.name);
                    return;
                }
                this.stagedFiles.push(f);
            });
            this.refreshAttachments();
            if (duplicates.length > 0) {
                this.alertService.warning(this.module, 'Duplicate file skipped', duplicates.join(', '));
            }
        }
        input.value = '';
    }

    // same file already picked (name + size + last modified), or a saved attachment with the same name
    private isDuplicateFile(file: File): boolean {
        return this.stagedFiles.some(s => s.name === file.name && s.size === file.size && s.lastModified === file.lastModified)
            || this.existingFiles.some(e => e.originalFilename === file.name);
    }

    removeStagedFile(index: number): void {
        const [removed] = this.stagedFiles.splice(index, 1);
        if (removed) this.tempIds.delete(removed);
        this.refreshAttachments();
    }

    removeExistingFile(index: number): void {
        const [f] = this.existingFiles.splice(index, 1);
        if (f) this.filesToRemove.push({ id: f.fileId });
        this.refreshAttachments();
    }

    fileUrl = (fileId: number): string => this.service.fileUrl(fileId);

    onRemoveAttachment(item: AttachmentFile): void {
        if (item.local) {
            this.removeStagedFile(this.stagedFiles.indexOf(item.local));
        } else {
            this.removeExistingFile(this.existingFiles.findIndex(f => f.id === item.id));
        }
    }

    // staged files get a negative temp id so they never clash with saved file ids
    private refreshAttachments(): void {
        this.attachmentItems = [
            ...this.existingFiles.map(f => ({ id: f.id, originalFilename: f.originalFilename, mimeType: f.mimeType })),
            ...this.stagedFiles.map(f => {
                if (!this.tempIds.has(f)) this.tempIds.set(f, this.nextTempId--);
                return { id: this.tempIds.get(f)!, originalFilename: f.name, mimeType: f.type, local: f };
            })
        ];
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

        // a sub item row must always have a sub item selected
        if (this.budgetSubItemRows.some(r => !r.budgetSubItemId)) {
            this.alertService.warning(this.module, 'Validation', 'Please select a budget sub item on every sub item row, or remove the empty row.');
            return;
        }

        // same validation as the old system: line items with sub items require sub item details
        if (this.budgetLineItemRows.some(r => r.hasSubItems)) {
            if (this.budgetSubItemRows.length === 0) {
                this.alertService.warning(this.module, 'Validation', 'Please add budget line sub item.');
                return;
            }
            if (this.budgetSubTotal <= 0) {
                this.alertService.warning(this.module, 'Validation', 'Please input complete budget sub item details.');
                return;
            }
        }

        this.formSubmit = true;

        // payload follows the old system (pr.js): multipart with model + filesToRemove + file_<i>
        const model: any = {
            id:            this.editMode ? this.id : null,
            voucherDate:   this.voucherDate,
            invoiceDate:   this.invoiceDate   || null,
            invoiceNumber: this.invoiceNumber || null,
            dueDate:       this.dueDate       || null,
            vendor:        { accountNo: this.vendor.accountNo },
            budgetLineItemDetails: this.budgetLineItemRows.map(r => ({
                id:                         r.id,
                budgetAmountBalanceCV:      r.budgetAmountBalanceCV,
                budgetAmountBalancePOJORFP: r.budgetAmountBalancePOJORFP
            })),
            amount:        this.totalAmount,
            paymentRequestDetails: this.lineItems.map(item => ({
                description: item.description,
                amount:      item.amount
            })),
            budgetDetails: this.budgetSubItemRows.map(r => ({
                budgetSubItem: { id: r.budgetSubItemId },
                description:   r.description,
                amount:        r.amount
            })),
            cashFlowAmountBalancePOJORFP: this.cashFlowAmountBalancePOJORFP,
            cashFlowAmountBalanceCV:      this.cashFlowAmountBalanceCV
        };

        const formData = new FormData();
        this.stagedFiles.forEach((f, i) => formData.append(`file_${i}`, f, f.name));
        formData.append('model', new Blob([JSON.stringify(model)], { type: 'application/json' }));
        formData.append('filesToRemove', new Blob([JSON.stringify(this.filesToRemove)], { type: 'application/json' }));

        const req$ = this.editMode ? this.service.update(formData) : this.service.create(formData);

        req$.subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res.success) {
                    this.alertService.success(this.module, 'Saved', '');
                    this.router.navigate(['/' + this.menuLink, res.modelId || this.id, 'detail']);
                } else {
                    this.alertService.error(this.module, 'Saving', res.failureMessage);
                }
            },
            error: () => {
                this.formSubmit = false;
                this.alertService.error(this.module, 'Saving', '');
            }
        });
    }
}
