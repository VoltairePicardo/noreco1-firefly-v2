import { Component, ElementRef, ViewChild, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { FormsModule } from '@angular/forms';
import { FlatpickrDefaults, FlatpickrModule } from 'angularx-flatpickr';
import { AlertService } from '@/app/shared/services/alert.service';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerX, tablerPaperclip, tablerPhoto, tablerFile, tablerArrowLeft, tablerCheck } from '@ng-icons/tabler-icons';
import { forkJoin } from 'rxjs';
import { JoAcceptanceService } from '../jo-acceptance.service';
import { JobOrderService } from '../../job-order/job-order.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseJobOrderModalComponent } from '@/app/shared/modals/browse-job-order-modal/browse-job-order-modal.component';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';

@Component({
    selector: 'app-jo-acceptance-add-edit',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, FormsModule, FlatpickrModule, RouterLink],
    providers: [FlatpickrDefaults, provideIcons({ tablerSearch, tablerX, tablerPaperclip, tablerPhoto, tablerFile, tablerArrowLeft, tablerCheck })],
    templateUrl: './jo-acceptance-add-edit.component.html'
})
export class JoAcceptanceAddEditComponent {
    module    = 'JO Certification / Acceptance';
    subModule = 'Create';
    menuLink  = 'jo-acceptance';

    @ViewChild('fileInput') fileInputRef!: ElementRef<HTMLInputElement>;

    id: any    = null;
    editMode   = false;
    formSubmit = false;
    isLoading  = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // Header fields
    voucherDate   = '';
    invoiceDate   = '';
    invoiceNumber = '';
    joaType       = 1; // 1 = Payment, 2 = Liquidation (default: Payment)
    selectedJobOrder: any = null;
    inspectedBy: any      = null;
    lineItems: any[]      = [];

    // Staged attachments
    attachments: { file: File; name: string; url: string }[] = [];

    private service      = inject(JoAcceptanceService);
    private joService    = inject(JobOrderService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.setDefaultDates();

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

    private setDefaultDates(): void {
        const today = new Date();
        const yyyy  = today.getFullYear();
        const mm    = String(today.getMonth() + 1).padStart(2, '0');
        const dd    = String(today.getDate()).padStart(2, '0');
        const todayStr = `${yyyy}-${mm}-${dd}`;
        this.voucherDate = todayStr;
        this.invoiceDate = todayStr;
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
                    this.voucherDate      = this.formatDate(header.voucherDate);
                    this.selectedJobOrder = header.jobOrder;
                    this.inspectedBy      = header.inspectedBy || null;
                    this.invoiceNumber    = header.invoiceNumber || '';
                    this.joaType          = header.type || 1;

                    if (header.invoiceDate) {
                        this.invoiceDate = this.formatDate(header.invoiceDate);
                    }

                    this.lineItems = (details || []).map((d: any) => ({
                        id:                  d.id,
                        joDetailId:          d.joDetailId,
                        itemDescription:     d.itemDescription || '',
                        joDescription:       d.joDescription || '',
                        unitCode:            d.unitCode || '',
                        quantity:            d.quantity || 0,
                        unitPrice:           d.unitPrice || 0,
                        remainingAmount:     d.remainingAmount || 0,
                        acceptedAmount:      d.acceptedAmount || 0,
                        // base = total accumulated minus what THIS JOA originally accepted
                        baseAcceptedAmount:  +(d.acceptedAmount || 0) - +(d.itemAmount || 0),
                        itemAmount:          d.itemAmount || 0,
                        adjustment:          d.adjustment || 0,
                        netAmount:           d.netAmount || 0,
                    }));
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    private formatDate(raw: any): string {
        const d = new Date(raw);
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    }

    // ─── Job Order Browse ─────────────────────────────────────────────────────

    async openJobOrderBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseJobOrderModalComponent,
                {},
                { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const jo = result.data;
                this.selectedJobOrder = jo;
                this.lineItems = [];
                this.joService.getDetailsForJoa(jo.id).subscribe({
                    next: (items) => {
                        this.lineItems = (items || []).map((d: any) => ({
                            joDetailId:          d.id,
                            itemDescription:     d.itemDescription || '',
                            joDescription:       d.joDescription || '',
                            unitCode:            d.unitCode || '',
                            quantity:            d.quantity || 0,
                            unitPrice:           d.itemAmount || 0, // JO item amount shown as "Price"
                            remainingAmount:     +(d.itemAmount || 0) - +(d.acceptedAmount || 0),
                            acceptedAmount:      d.acceptedAmount || 0,
                            // base = accumulated before this JOA (for new JOA, all prior JOAs)
                            baseAcceptedAmount:  d.acceptedAmount || 0,
                            itemAmount:          0,   // user fills in acceptance amount
                            adjustment:          0,
                            netAmount:           0,
                        }));
                        this.recalculateTotals();
                    },
                    error: () => {}
                });
            }
        } catch {
            // dismissed — no action
        }
    }

    clearJobOrder(): void {
        this.selectedJobOrder = null;
        this.lineItems = [];
    }

    // ─── Inspector Browse ─────────────────────────────────────────────────────

    async openInspectedByBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent,
                {},
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.inspectedBy = result.data;
            }
        } catch {
            // dismissed — no action
        }
    }

    clearInspectedBy(): void { this.inspectedBy = null; }

    // ─── Line Item Handlers ───────────────────────────────────────────────────

    onItemAmountChange(item: any): void {
        if (isNaN(item.itemAmount)) {
            item.itemAmount = 0;
        } else if (item.itemAmount > item.remainingAmount) {
            item.itemAmount = item.remainingAmount;
            this.alertService.warning(
                this.module,
                `Amount must be less than or equal to ${item.remainingAmount}.`,
                ''
            );
        }
        item.netAmount = (item.itemAmount || 0) + (item.adjustment || 0);
        this.recalculateTotals();
    }

    onAdjustmentChange(item: any): void {
        item.netAmount = (item.itemAmount || 0) + (item.adjustment || 0);
        this.recalculateTotals();
    }

    removeRow(index: number): void {
        this.lineItems.splice(index, 1);
        this.recalculateTotals();
    }

    // ─── Totals ──────────────────────────────────────────────────────────────

    totalAmount     = 0;
    totalAdjustment = 0;
    totalNetAmount  = 0;

    private recalculateTotals(): void {
        this.totalAmount     = this.lineItems.reduce((s, li) => s + (li.itemAmount   || 0), 0);
        this.totalAdjustment = this.lineItems.reduce((s, li) => s + (li.adjustment   || 0), 0);
        this.totalNetAmount  = this.totalAmount + this.totalAdjustment;
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

    private uploadStagedFiles(joaId: number): void {
        if (!this.attachments.length) return;
        const formData = new FormData();
        this.attachments.forEach(a => formData.append(`file_${a.name}`, a.file, a.name));
        this.service.uploadFiles(joaId, formData).subscribe({ error: () => {} });
    }

    // ─── Save ────────────────────────────────────────────────────────────────

    save(): void {
        if (!this.voucherDate) {
            this.alertService.warning(this.module, 'Please enter the voucher date.', '');
            return;
        }
        if (!this.selectedJobOrder) {
            this.alertService.warning(this.module, 'Please select a Job Order.', '');
            return;
        }
        if (!this.invoiceDate) {
            this.alertService.warning(this.module, 'Please enter the invoice date.', '');
            return;
        }
        if (!this.invoiceNumber?.trim()) {
            this.alertService.warning(this.module, 'Please enter the invoice number.', '');
            return;
        }
        if (this.lineItems.length === 0) {
            this.alertService.warning(this.module, 'No items to certify.', '');
            return;
        }

        this.formSubmit = true;

        const payload: any = {
            voucherDate:   this.voucherDate,
            jobOrder:      { id: this.selectedJobOrder.id },
            vendor:        { accountNo: this.selectedJobOrder.vendor?.accountNo || this.selectedJobOrder.vendorAccountNo },
            invoiceDate:   this.invoiceDate,
            invoiceNumber: this.invoiceNumber.trim(),
            inspectedBy:   this.inspectedBy ? { accountNo: this.inspectedBy.accountNo } : null,
            type:          this.joaType,
            amount:        this.totalAmount,
            netAmount:     this.totalNetAmount,
            adjustment:    this.totalAdjustment,
            joAcceptanceDetails: this.lineItems.map(li => ({
                joDetailId:     li.joDetailId,
                itemAmount:     li.itemAmount || 0,
                // new cumulative acceptedAmount = base (prior JOAs) + this JOA's acceptance
                acceptedAmount: (li.baseAcceptedAmount || 0) + (li.itemAmount || 0),
                adjustment:     li.adjustment || 0,
                netAmount:      li.netAmount || 0,
                quantity:       li.quantity,
                unitPrice:      li.unitPrice || 0,
            }))
        };

        if (this.editMode) payload.id = this.id;

        const req = this.editMode ? this.service.update(payload) : this.service.create(payload);

        req.subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.uploadStagedFiles(res.modelId);
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
