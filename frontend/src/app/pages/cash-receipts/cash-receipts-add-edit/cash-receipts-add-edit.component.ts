import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { CashReceiptsService } from '../cash-receipts.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseTempGLModalComponent } from '@/app/shared/modals/browse-temp-gl-modal/browse-temp-gl-modal.component';
import { JournalEntriesFormComponent, JournalEntry } from '@/app/shared/forms/journal-entries-form/journal-entries-form.component';

@Component({
    selector: 'app-cash-receipts-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective,
        JournalEntriesFormComponent
    ],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './cash-receipts-add-edit.component.html'
})
export class CashReceiptsAddEditComponent {
    module    = 'Cash Receipts';
    subModule = 'Create';
    menuLink  = 'cash-receipts';

    id: any    = null;
    editMode   = false;
    formSubmit = false;
    isLoading  = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    voucherDate = '';
    particulars = '';

    journalEntries: JournalEntry[] = [];
    tempBatch: any = null;

    approvingOfficer: any = null;

    private service      = inject(CashReceiptsService);
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
                this.subModule = 'Create';
                this.loadDefaultSignatories();
            }
        });
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.voucherDate      = data.voucherDate ? new Date(data.voucherDate).toISOString().substring(0, 10) : '';
                    this.particulars      = data.particulars || '';
                    this.approvingOfficer = data.approvingOfficer || null;
                    this.journalEntries   = (data.journalEntries || data.details || []).map((e: any): JournalEntry => ({
                        account:         e.account || (e.code ? { accountCode: e.code, accountTitle: e.description, id: e.accountId ?? null } : null),
                        debit:           Number(e.debit ?? e.debitAmount) || null,
                        credit:          Number(e.credit ?? e.creditAmount) || null,
                        applyAllocation: e.applyAllocation || false,
                        allocationPct:   e.allocationPct   ?? null,
                        applyWht:        e.applyWht        || false,
                        wht:             e.wht             || null
                    }));
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

    loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (data) => { if (data) this.approvingOfficer = data.approvingOfficer || null; },
            error: () => {}
        });
    }

    get totalDebit(): number {
        return this.journalEntries.reduce((sum, e) => sum + (Number(e.debit) || 0), 0);
    }

    get totalCredit(): number {
        return this.journalEntries.reduce((sum, e) => sum + (Number(e.credit) || 0), 0);
    }

    get isBalanced(): boolean {
        return Math.abs(this.totalDebit - this.totalCredit) < 0.001;
    }

    async openTempGLBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseTempGLModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                const { batch, entries } = result.data;
                this.tempBatch   = batch;
                this.particulars = batch.remarks || '';
                this.journalEntries = (entries as any[]).map((e): JournalEntry => ({
                    account:         e.account || { accountCode: e.code, accountTitle: e.description, id: e.accountId ?? null },
                    debit:           Number(e.debit)  || null,
                    credit:          Number(e.credit) || null,
                    applyAllocation: false,
                    allocationPct:   null,
                    applyWht:        false,
                    wht:             null
                }));
            }
        } catch { }
    }

    clearTempGL(): void {
        this.tempBatch      = null;
        this.journalEntries = [];
        this.particulars    = '';
    }

    async openEntityBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseEntityModalComponent, { entityTypes: [1] }, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this.approvingOfficer = result.data;
            }
        } catch { }
    }

    save(): void {
        if (!this.voucherDate) {
            this.alertService.warning(this.module, 'Validation', 'Please enter a voucher date.');
            return;
        }
        if (!this.particulars.trim()) {
            this.alertService.warning(this.module, 'Validation', 'Please enter particulars.');
            return;
        }
        if (this.journalEntries.length === 0) {
            this.alertService.warning(this.module, 'Validation', 'Please add at least one journal entry.');
            return;
        }
        if (!this.isBalanced) {
            this.alertService.warning(this.module, 'Validation', 'Debit and credit totals must be equal.');
            return;
        }

        this.formSubmit = true;

        const payload: any = {
            id:          this.editMode ? this.id : null,
            voucherDate: this.voucherDate,
            particulars: this.particulars,
            amount:      this.totalDebit,
            generalLedgerLines: this.journalEntries.map(e => ({
                code:        e.account?.accountCode || '',
                description: e.account?.accountTitle || '',
                accountId:   e.account?.id || null,
                debit:       Number(e.debit)  || 0,
                credit:      Number(e.credit) || 0
            })),
            approvingOfficer: this.approvingOfficer ? { accountNo: this.approvingOfficer.accountNo } : null
        };

        const req$ = this.editMode ? this.service.update(payload) : this.service.create(payload);
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
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'An error occurred.', ''); }
        });
    }
}
