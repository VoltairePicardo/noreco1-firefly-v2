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
import { MaterialIssuanceService } from '../material-issuance.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseInventoryDocModalComponent } from '@/app/shared/modals/browse-inventory-doc-modal/browse-inventory-doc-modal.component';
import { JournalEntriesFormComponent, JournalEntry } from '@/app/shared/forms/journal-entries-form/journal-entries-form.component';

@Component({
    selector: 'app-material-issuance-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective,
        JournalEntriesFormComponent
    ],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './material-issuance-add-edit.component.html'
})
export class MaterialIssuanceAddEditComponent {
    module    = 'Material Issuance';
    subModule = 'Create';
    menuLink  = 'material-issuance';

    id: any    = null;
    editMode   = false;
    formSubmit = false;
    isLoading  = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    voucherDate = '';
    particulars = '';

    selectedInventoryDoc: any = null;
    inventoryDocItems: any[]  = [];

    journalEntries: JournalEntry[] = [];

    signatories: { [key: string]: any } = {
        checker:              null,
        recommendingApproval: null,
        approvingOfficer:     null
    };

    signatoryLabels: { [key: string]: string } = {
        checker:              'Checker',
        recommendingApproval: 'Rec. Officer',
        approvingOfficer:     'Approving Officer'
    };

    signatoryKeys = ['checker', 'recommendingApproval', 'approvingOfficer'];

    private service      = inject(MaterialIssuanceService);
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
                this.journalEntries = [{ account: null, debit: null, credit: null }];
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
                    this.voucherDate          = data.voucherDate ? new Date(data.voucherDate).toISOString().substring(0, 10) : '';
                    this.particulars          = data.particulars || '';
                    this.selectedInventoryDoc = data.inventoryDoc || null;
                    this.inventoryDocItems    = data.inventoryDocItems || data.items || [];
                    this.journalEntries = (data.journalEntries || data.details || []).map((e: any): JournalEntry => ({
                        account:         e.account || (e.code ? { accountCode: e.code, accountTitle: e.description, id: e.accountId } : null),
                        debit:           Number(e.debit ?? e.debitAmount) || null,
                        credit:          Number(e.credit ?? e.creditAmount) || null,
                        applyAllocation: e.applyAllocation || false,
                        allocationPct:   e.allocationPct   ?? null,
                        applyWht:        e.applyWht        || false,
                        wht:             e.wht             || null
                    }));
                    if (this.journalEntries.length === 0) {
                        this.journalEntries = [{ account: null, debit: null, credit: null }];
                    }
                    this.signatories = {
                        checker:              data.checker              || null,
                        recommendingApproval: data.recommendingApproval || null,
                        approvingOfficer:     data.approvingOfficer     || null
                    };
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
            next: (data) => {
                if (data) {
                    this.signatories = {
                        checker:              data.checker              || data.checkedBy   || null,
                        recommendingApproval: data.recommendingApproval || data.recApprovedBy || null,
                        approvingOfficer:     data.approvingOfficer     || data.approvedBy  || null
                    };
                }
            },
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
        return this.journalEntries.length > 0 && Math.abs(this.totalDebit - this.totalCredit) < 0.001;
    }

    async openInvDocBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseInventoryDocModalComponent, {}, { size: 'xl', centered: true });
            if (result?.action === 'select' && result?.data) {
                const doc = result.data;
                this.selectedInventoryDoc = doc;
                // auto-fill particulars from the document's purpose
                if (doc?.purpose) {
                    this.particulars = doc.purpose;
                }
                if (doc?.id) {
                    this.service.getInventoryDocItems(doc.id).subscribe({
                        next: (items) => { this.inventoryDocItems = items || []; },
                        error: () => { this.inventoryDocItems = []; }
                    });
                }
            }
        } catch { }
    }

    clearInventoryDoc(): void {
        this.selectedInventoryDoc = null;
        this.inventoryDocItems    = [];
        this.particulars          = '';
    }

    async openEntityBrowse(key: string): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseEntityModalComponent, { entityTypes: [1] }, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this.signatories[key] = result.data;
            }
        } catch { }
    }

    clearSignatory(key: string): void {
        this.signatories[key] = null;
    }

    save(): void {
        if (!this.voucherDate) {
            this.alertService.warning(this.module, 'Validation', 'Please enter a voucher date.');
            return;
        }
       /* if (!this.selectedInventoryDoc) {
            this.alertService.warning(this.module, 'Validation', 'Please select an inventory document.');
            return;
        }*/
        if (this.journalEntries.length === 0) {
            this.alertService.warning(this.module, 'Validation', 'Please add at least one journal entry.');
            return;
        }
        if (!this.isBalanced) {
            this.alertService.warning(this.module, 'Validation', 'Debit and credit totals must be equal.');
            return;
        }

        this.formSubmit = true;

        const sigRef = (key: string) => this.signatories[key]
            ? { accountNo: this.signatories[key].accountNo } : null;

        const payload: any = {
            id:                  this.editMode ? this.id : null,
            voucherDate:         this.voucherDate,
            particulars:         this.particulars || null,
            invDocTransactionId: this.selectedInventoryDoc?.id || null,
            amount:              this.totalDebit,
            generalLedgerLines:  this.journalEntries.map(e => ({
                code:        e.account?.accountCode  || '',
                description: e.account?.accountTitle || e.account?.accountDescription || '',
                accountId:   e.account?.id           || null,
                debit:       Number(e.debit)         || 0,
                credit:      Number(e.credit)        || 0
            })),
            checker:             sigRef('checker'),
            recommendingOfficer: sigRef('recommendingApproval'),
            approvingOfficer:    sigRef('approvingOfficer')
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
            error: () => {
                this.formSubmit = false;
                this.alertService.error(this.module, 'An error occurred while saving.', '');
            }
        });
    }
}
