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

@Component({
    selector: 'app-pcv-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective
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
    attachments: { file: File; name: string; url: string }[] = [];

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

    // ─── Validation & save ────────────────────────────────────────────

    isValid(): boolean {
        return !!(this.voucherDate && this.payee?.trim() && this.pettyCashFund && this.approvingOfficer);
    }

    save(): void {
        this.submit = true;
        if (!this.isValid()) return;

        this.formSubmit = true;
        const payload: any = {
            id:           this.editMode ? this.id : null,
            pettyCashDate: this.voucherDate,
            voucherDate:   this.voucherDate,
            payee:         this.payee.trim(),
            request:       this.request,
            amount:        this.totalAmount,
            office:               this.selectedOffice?.id ? { id: this.selectedOffice.id } : null,
            pettyCashFund:        { id: this.pettyCashFund.id },
            budgetLineItemDetail: this.budgetLineItemDetail?.id ? { id: this.budgetLineItemDetail.id } : null,
            approvingOfficer:     this.approvingOfficer?.accountNo ? { accountNo: this.approvingOfficer.accountNo } : null,
            checker:              this.checker?.accountNo          ? { accountNo: this.checker.accountNo }          : null,
            releasingOfficer:     this.releasingOfficer?.accountNo ? { accountNo: this.releasingOfficer.accountNo } : null,
            pettyCashTransDetails: this.details
                .filter(d => d.remarks?.trim())
                .map(d => ({ remarks: d.remarks.trim(), amount: Number(d.amount) || 0 }))
        };

        const files = this.attachments.map(a => a.file);
        const req$ = this.editMode
            ? this.service.update(payload, files, [])
            : this.service.create(payload, files);

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
