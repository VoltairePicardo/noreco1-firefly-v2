import { Component, ElementRef, ViewChild, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { PcvService } from '../pcv.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseBudgetLineItemModalComponent } from '@/app/shared/modals/browse-budget-line-item-modal/browse-budget-line-item-modal.component';
import { AttachmentFile, AttachmentsComponent } from '@/app/shared/components/attachments/attachments.component';

@Component({
    selector: 'app-pcv-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective,
        AttachmentsComponent
    ],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './pcv-add-edit.component.html'
})
export class PcvAddEditComponent {
    module    = 'Petty Cash Voucher';
    subModule = 'Create';
    menuLink  = 'pcv';

    @ViewChild('fileInput') fileInputRef!: ElementRef<HTMLInputElement>;

    id: any    = null;
    editMode   = false;
    formSubmit = false;
    submit     = false;
    isLoading  = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // Form fields
    voucherDate    = '';
    request        = 'Reimbursement';
    payee          = '';
    selectedOffice: any   = null;
    offices:        any[] = [];

    pettyCashFund: any    = null;
    pettyCashFunds: any[] = [];

    budgetLineItemDetail: any  = null;
    budgetLineItems            = signal<any[]>([]);

    budgetUpdate      = false;
    budgetBalancePCL  : number | null = null;
    budgetBalancePOJO : number | null = null;
    budgetBalanceCV   : number | null = null;

    // Signatories
    approvingOfficer: any = null;
    checker: any          = null;
    releasingOfficer: any = null;

    // Expense details
    details: { remarks: string; amount: number }[] = [];

    // File attachments
    attachmentItems: AttachmentFile[] = [];
    private newFiles: File[]   = [];
    private nextTempId         = -1;
    existingFiles: any[]       = [];
    filesToRemove: any[]       = [];

    private service      = inject(PcvService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private modalService = inject(ModalService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.loadPettyCashFunds();
        this.loadBudgetLineItems();
        this.loadOffices();
        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.editMode = idParam != null && /^\d+$/.test(idParam);
            if (this.editMode) {
                this.id        = Number(idParam);
                this.subModule = 'Edit';
                this.loadForEdit();
            } else {
                this.subModule   = 'Create';
                this.voucherDate = new Date().toISOString().substring(0, 10);
                this.addDetail();
                this.loadDefaultSignatories();
                this.loadUserOffice();
            }
        });
    }

    loadOffices(): void {
        this.service.getOffices().subscribe({
            next: (data) => { this.offices = data || []; },
            error: () => { this.offices = []; }
        });
    }

    loadUserOffice(): void {
        this.service.getUserOffice().subscribe({
            next: (data) => { if (data?.id) this.selectedOffice = data; },
            error: () => {}
        });
    }

    loadPettyCashFunds(): void {
        this.service.getPettyCashFunds().subscribe({
            next: (data) => { this.pettyCashFunds = data || []; },
            error: () => { this.pettyCashFunds = []; }
        });
    }

    loadBudgetLineItems(): void {
        this.service.getBudgetLineItems().subscribe({
            next: (data) => this.budgetLineItems.set(data || []),
            error: () => {}
        });
    }

    loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (data) => {
                if (!this.approvingOfficer) this.approvingOfficer = data?.approvingOfficer || null;
                if (!this.checker)          this.checker          = data?.checker          || null;
                if (!this.releasingOfficer) this.releasingOfficer = data?.releasingOfficer || null;
            },
            error: () => {}
        });
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    const d = data.pettyCashDate || data.voucherDate;
                    this.voucherDate          = d ? new Date(d).toISOString().substring(0, 10) : '';
                    this.payee                = data.payee    || '';
                    this.request              = data.request  || 'Reimbursement';
                    this.selectedOffice       = data.office   || null;
                    this.pettyCashFund        = data.pettyCashFund          || null;
                    this.budgetLineItemDetail = data.budgetLineItemDetail    || null;
                    this.budgetUpdate = data.budgetUpdate === true;
                    if (this.budgetLineItemDetail?.id) {
                        this.loadBudgetBalances();
                    }
                    this.approvingOfficer     = data.approvedByUser  || data.approvingOfficer  || null;
                    this.checker              = data.checkedByUser   || data.checker            || null;
                    this.releasingOfficer     = data.releasedByUser  || data.releasingOfficer   || null;
                    this.details = (data.pettyCashTransDetails || []).map((item: any) => ({
                        remarks: item.remarks || '',
                        amount:  item.amount  || 0
                    }));
                    if (this.details.length === 0) this.addDetail();
                    if (data.transId) { this.loadExistingFiles(data.transId); }
                } else {
                    this.alertService.error(this.module, 'Record not found.', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load record.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    // ─── Budget Line Item ────────────────────────────────────────────

    async openBudgetLineItemBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseBudgetLineItemModalComponent,
                { items: this.budgetLineItems() },
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.budgetLineItemDetail = result.data;
                this.loadBudgetBalances();
            }
        } catch { }
    }

    clearBudgetLineItem(): void {
        this.budgetLineItemDetail = null;
        this.budgetBalancePCL     = null;
        this.budgetBalancePOJO    = null;
        this.budgetBalanceCV      = null;
    }

    loadBudgetBalances(): void {
        if (!this.budgetLineItemDetail?.id) return;
        this.service.getBudgetBalances(this.budgetLineItemDetail.id).subscribe({
            next: (data) => {
                this.budgetBalancePCL  = data?.budgetAmountBalancePCL        ?? null;
                this.budgetBalancePOJO = data?.budgetLineItemBalancePOJORFP  ?? null;
                this.budgetBalanceCV   = data?.budgetLineItemBalanceCV        ?? null;
            },
            error: () => {}
        });
    }

    loadExistingFiles(transId: number): void {
        this.service.getFiles(transId).subscribe({
            next: (files) => {
                this.existingFiles    = files || [];
                this.attachmentItems  = (files || []).map((f: any) => ({
                    id: f.id, originalFilename: f.originalFilename, mimeType: f.mimeType
                }));
            },
            error: () => {}
        });
    }

    // ─── Signatories ─────────────────────────────────────────────────

    async openSignatoryBrowse(field: 'approvingOfficer' | 'checker' | 'releasingOfficer'): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseEntityModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this[field] = result.data;
            }
        } catch { }
    }

    clearSignatory(field: 'approvingOfficer' | 'checker' | 'releasingOfficer'): void {
        this[field] = null;
    }

    // ─── Expense details ─────────────────────────────────────────────

    addDetail(): void {
        this.details.push({ remarks: '', amount: 0 });
    }

    removeDetail(index: number): void {
        if (this.details.length > 1) this.details.splice(index, 1);
    }

    get totalAmount(): number {
        return this.details.reduce((sum, d) => sum + (Number(d.amount) || 0), 0);
    }

    // ─── File attachments ─────────────────────────────────────────────

    fileUrl = (fileId: number): string => this.service.fileUrl(fileId);

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
            const tempId: AttachmentFile = { id: this.nextTempId--, originalFilename: file.name, mimeType: file.type, local: file };
            this.newFiles.push(file);
            this.attachmentItems = [...this.attachmentItems, tempId];
        }
        input.value = '';
    }

    onRemoveAttachment(file: AttachmentFile): void {
        if (file.local) {
            this.newFiles = this.newFiles.filter(f => f !== file.local);
        } else {
            this.filesToRemove.push(file);
            this.existingFiles = this.existingFiles.filter(f => f.id !== file.id);
        }
        this.attachmentItems = this.attachmentItems.filter(a => a.id !== file.id);
    }

    // ─── Validation & save ────────────────────────────────────────────

    isValid(): boolean {
        if (!this.voucherDate || !this.payee?.trim() || !this.pettyCashFund || !this.approvingOfficer) return false;
        if (!this.checker)          return false;
        if (!this.releasingOfficer) return false;
        if (!this.budgetLineItemDetail) return false;
        if (this.totalAmount > 1000) return false;
        if (this.pettyCashFund?.balance != null && this.totalAmount > this.pettyCashFund.balance) return false;
        if (this.details.filter(d => d.remarks?.trim()).length === 0) return false;
        return true;
    }

    save(): void {
        this.submit = true;
        if (!this.isValid()) {
            // Provide specific error feedback
            if (this.totalAmount > 1000) {
                this.alertService.error(this.module, 'Validation', 'Total amount must not exceed ₱1,000.00.');
                return;
            }
            if (this.pettyCashFund?.balance != null && this.totalAmount > this.pettyCashFund.balance) {
                this.alertService.error(this.module, 'Validation', 'Total amount exceeds the available PCF balance.');
                return;
            }
            return;
        }

        const filled = this.details.filter(d => d.remarks?.trim());
        const detailsTotal = filled.reduce((s, d) => s + (Number(d.amount) || 0), 0);
        if (Math.abs(detailsTotal - this.totalAmount) > 0.001) {
            this.alertService.error(this.module, 'Validation', 'All detail rows with amounts must have remarks filled in.');
            return;
        }

        this.formSubmit = true;
        const filledDetails = filled;
        const payload: any = {
            id:           this.editMode ? this.id : null,
            pettyCashDate: this.voucherDate,
            voucherDate:   this.voucherDate,
            payee:         this.payee.trim(),
            request:       this.request,
            amount:        this.totalAmount,
            budgetAmountBalancePCL:       this.budgetBalancePCL,
            budgetLineItemBalancePOJORFP: this.budgetBalancePOJO,
            budgetLineItemBalanceCV:      this.budgetBalanceCV,
            office:               this.selectedOffice?.id ? { id: this.selectedOffice.id } : null,
            pettyCashFund:        { id: this.pettyCashFund.id },
            budgetLineItemDetail: this.budgetLineItemDetail?.id ? { id: this.budgetLineItemDetail.id } : null,
            approvingOfficer:     this.approvingOfficer?.accountNo ? { accountNo: this.approvingOfficer.accountNo } : null,
            checker:              this.checker?.accountNo          ? { accountNo: this.checker.accountNo }          : null,
            releasingOfficer:     this.releasingOfficer?.accountNo ? { accountNo: this.releasingOfficer.accountNo } : null,
            pettyCashTransDetails: filledDetails
                .map(d => ({ remarks: d.remarks.trim(), amount: Number(d.amount) || 0 }))
        };

        const req$ = this.editMode
            ? this.service.update(payload, this.newFiles, this.filesToRemove)
            : this.service.create(payload, this.newFiles);

        req$.subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Saved successfully.', '');
                    this.router.navigate(['/' + this.menuLink]);
                } else {
                    this.alertService.error(this.module, 'Save failed.', res?.failureMessage || '');
                }
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'An error occurred.', ''); }
        });
    }

    compareFn(a: any, b: any): boolean {
        return a && b ? a.id === b.id : a === b;
    }
}
