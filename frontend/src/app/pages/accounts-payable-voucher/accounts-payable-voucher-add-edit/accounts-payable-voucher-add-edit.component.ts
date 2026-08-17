import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { LaddaModule } from 'angular2-ladda';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { AccountsPayableVoucherService } from '../accounts-payable-voucher.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseIemopBillingModalComponent } from '@/app/shared/modals/browse-iemop-billing-modal/browse-iemop-billing-modal.component';
import { JournalEntriesFormComponent, JournalEntry } from '@/app/shared/forms/journal-entries-form/journal-entries-form.component';
import { provideIcons } from '@ng-icons/core';
import { tablerCheck, tablerArrowLeft, tablerSearch, tablerPlus, tablerTrash, tablerX, tablerPaperclip } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-accounts-payable-voucher-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        LaddaModule,
        FlatpickrDirective,
        JournalEntriesFormComponent
    ],
    providers: [
        provideFlatpickrDefaults(),
        ...SHARED_PROVIDERS,
        provideIcons({ tablerCheck, tablerArrowLeft, tablerSearch, tablerPlus, tablerTrash, tablerX, tablerPaperclip })
    ],
    templateUrl: './accounts-payable-voucher-add-edit.component.html'
})
export class AccountsPayableVoucherAddEditComponent {
    module    = 'Accounts Payable Voucher';
    subModule = 'Create';
    menuLink  = 'accounts-payable-voucher';

    id: any    = null;
    editMode   = false;
    formSubmit = false;
    isLoading  = signal(false);

    flatpickrOptions = {
        dateFormat: 'Y-m-d',
        altInput: true,
        altFormat: 'F j, Y'
    };

    // Form fields
    voucherDate       = '';
    invoiceDate       = '';
    dueDate           = '';
    particulars       = '';
    paymentTerm: number | null = null;
    forInstallment    = false;
    numberOfPayments: number | null = null;
    vendor: any       = null;

    // Signatories
    checker: any          = null;
    approvingOfficer: any = null;

    // IEMOP Billings
    iemopBillings: any[] = [];

    // Journal Entries
    journalEntries: JournalEntry[] = [];

    // Attachments
    newFiles: File[] = [];

    private service      = inject(AccountsPayableVoucherService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private modalService = inject(ModalService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
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
            }
        });
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getById(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.voucherDate    = data.voucherDate   ? new Date(data.voucherDate).toISOString().substring(0, 10)   : '';
                    this.invoiceDate    = data.invoiceDate   ? new Date(data.invoiceDate).toISOString().substring(0, 10)   : '';
                    this.dueDate        = data.dueDate       ? new Date(data.dueDate).toISOString().substring(0, 10)       : '';
                    this.particulars    = data.particulars   || '';
                    this.paymentTerm    = data.paymentTerm   ?? null;
                    this.forInstallment = data.forInstallment ?? false;
                    this.numberOfPayments = data.numberOfPayments ?? null;
                    this.vendor         = data.vendor        || null;
                    this.checker        = data.checker       || null;
                    this.approvingOfficer = data.approvingOfficer || null;
                    this.iemopBillings = data.iemopBillings || [];
                    if (data.transId) { this.loadJournalEntries(data.transId); }
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

    loadJournalEntries(transId: number): void {
        this.service.getJournalEntries(transId).subscribe({
            next: (data) => {
                this.journalEntries = (data || []).map((e: any) => ({
                    account: { accountCode: e.code || e.accountCode, accountTitle: e.description || e.accountTitle, id: e.accountId },
                    debit:   e.debit   || null,
                    credit:  e.credit  || null
                }));
            },
            error: () => { this.journalEntries = []; }
        });
    }

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

    async openIemopBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseIemopBillingModalComponent, {}, { size: 'xl', centered: true });
            if (result?.action === 'select' && result?.data) {
                const bill = result.data;
                const alreadyLinked = this.iemopBillings.some((b: any) => b.id === bill.id);
                if (alreadyLinked) {
                    this.alertService.warning(this.module, 'Already Added', 'This IEMOP billing is already linked.');
                    return;
                }
                this.iemopBillings.push(bill);
            }
        } catch { }
    }

    removeIemop(index: number): void {
        this.iemopBillings.splice(index, 1);
    }

    async openSignatoryBrowse(field: 'checker' | 'approvingOfficer'): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseEntityModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this[field] = result.data;
            }
        } catch { }
    }

    clearSignatory(field: 'checker' | 'approvingOfficer'): void {
        this[field] = null;
    }

    onFileChange(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files) {
            Array.from(input.files).forEach(f => this.newFiles.push(f));
            input.value = '';
        }
    }

    removeFile(index: number): void {
        this.newFiles.splice(index, 1);
    }

    save(): void {
        if (!this.voucherDate) {
            this.alertService.warning(this.module, 'Validation', 'Please enter a voucher date.');
            return;
        }
        if (!this.vendor) {
            this.alertService.warning(this.module, 'Validation', 'Please select a vendor/supplier.');
            return;
        }

        this.formSubmit = true;

        const payload: any = {
            id:               this.editMode ? this.id : null,
            voucherDate:      this.voucherDate,
            invoiceDate:      this.invoiceDate       || null,
            dueDate:          this.dueDate           || null,
            particulars:      this.particulars       || null,
            paymentTerm:      this.paymentTerm       ?? null,
            forInstallment:   this.forInstallment,
            numberOfPayments: this.forInstallment ? (this.numberOfPayments ?? null) : null,
            vendor:           { accountNo: this.vendor.accountNo },
            checker:          this.checker?.accountNo          ? { accountNo: this.checker.accountNo }          : null,
            approvingOfficer: this.approvingOfficer?.accountNo ? { accountNo: this.approvingOfficer.accountNo } : null,
            iemopBillings:    this.iemopBillings.map(b => ({ id: b.id })),
            generalLedgerLines: this.journalEntries
                .filter(e => e.account)
                .map(e => ({
                    accountId: e.account?.id,
                    code:      e.account?.accountCode,
                    debit:     e.debit  || 0,
                    credit:    e.credit || 0
                }))
        };

        const req$ = this.editMode
            ? this.service.update(payload, this.newFiles)
            : this.service.create(payload, this.newFiles);

        req$.subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Saved successfully.', '');
                    this.router.navigate(['/' + this.menuLink, res.modelId, 'detail']);
                } else {
                    this.alertService.error(this.module, 'Save failed.', res?.failureMessage || '');
                }
            },
            error: () => {
                this.formSubmit = false;
                this.alertService.error(this.module, 'An error occurred while saving.', '');
            }
        });
    }
}
