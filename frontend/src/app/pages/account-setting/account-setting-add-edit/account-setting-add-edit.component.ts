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
import { FlatpickrDefaults, FlatpickrModule } from 'angularx-flatpickr';
import { AccountSettingService } from '../account-setting.service';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerX, tablerArrowLeft, tablerCheck } from '@ng-icons/tabler-icons';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseAccountSettingDocModalComponent } from '@/app/shared/modals/browse-account-setting-doc-modal/browse-account-setting-doc-modal.component';
import { BrowseCOAModalComponent } from '@/app/shared/modals/browse-coa-modal/browse-coa-modal.component';

export const DOC_TYPE_OPTIONS = [
    { label: 'Receiving Report',        value: 'rr',           field: 'receivingReport' },
    { label: 'Receive Stock Transfer',   value: 'stockReceive', field: 'stockReceive' },
    { label: 'Material Credit Ticket',  value: 'mct',          field: 'materialCreditTicket' },
    { label: 'Stock Release',           value: 'stockRelease', field: 'stockRelease' },
    { label: 'Material Salvage Ticket', value: 'mst',          field: 'materialSalvageTicket' },
    { label: 'Stock Adjustment',        value: 'stockAdjust',  field: 'stockAdjustment' },
];

@Component({
    selector: 'app-account-setting-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        LaddaModule,
        FlatpickrModule
    ],
    providers: [...SHARED_PROVIDERS, FlatpickrDefaults, provideIcons({ tablerSearch, tablerX, tablerArrowLeft, tablerCheck })],
    templateUrl: './account-setting-add-edit.component.html'
})
export class AccountSettingAddEditComponent {
    module    = 'Account Setting';
    menuLink  = 'account-setting';
    id: any   = null;
    editMode  = false;
    subModule = 'Create';

    isLoading  = signal(false);
    formSubmit = false;
    submitted  = false;

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // Header
    date    = '';
    remarks = '';

    // Document selection
    docTypeOptions     = DOC_TYPE_OPTIONS;
    selectedDocType    = DOC_TYPE_OPTIONS[0].value;
    selectedDocument: any = null;
    rrLinkedData: any     = null;
    rrConfirmedForJv      = false;

    // Supplier account (default credit for RR type)
    supplierAccount: any = null;

    // Line items
    lineItems: any[] = [];

    // Batch apply accounts
    batchDebitAccount: any  = null;
    batchCreditAccount: any = null;

    private service      = inject(AccountSettingService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private modalService = inject(ModalService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.date = new Date().toISOString().substring(0, 10);
        this.loadSupplierAccount();

        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.editMode = idParam != null && /^\d+$/.test(idParam ?? '');
            if (this.editMode) {
                this.id        = idParam;
                this.subModule = 'Edit';
                this.getData();
            }
        });
    }

    // ── Init ───────────────────────────────────────────────────────────────

    loadSupplierAccount(): void {
        this.service.getSupplierAccount().subscribe({
            next: (data) => { this.supplierAccount = data || null; },
            error: () => {}
        });
    }

    getData(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (header) => {
                this.isLoading.set(false);
                if (header?.id) {
                    this.date             = header.date ? new Date(header.date).toISOString().substring(0, 10) : '';
                    this.remarks          = header.remarks || '';
                    this.rrConfirmedForJv = header.rrConfirmedForJv || false;

                    for (const opt of DOC_TYPE_OPTIONS) {
                        if (header[opt.field]) {
                            this.selectedDocType  = opt.value;
                            this.selectedDocument = header.documentDetail || header[opt.field];
                            break;
                        }
                    }

                    if (this.selectedDocType === 'rr' && header.receivingReport?.id) {
                        this.service.getRrLinkedDetails(header.receivingReport.id).subscribe({
                            next: (rrData) => {
                                this.rrLinkedData     = rrData;
                                this.rrConfirmedForJv = !!(rrData?.cvCode || rrData?.calId || rrData?.useCreditCard);
                            },
                            error: () => {}
                        });
                    }

                    this.lineItems = (header.accountSettingDetails || []).map((d: any) => ({
                        itemStockDetailId: d.itemStockDetailId,
                        itemCode:          d.itemCode,
                        itemDescription:   d.itemDescription,
                        unitCode:          d.unitCode,
                        debitAccount:      d.debitAccount  || null,
                        creditAccount:     d.creditAccount || null,
                        isSelected:        false
                    }));
                } else {
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Error', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    // ── Document type ──────────────────────────────────────────────────────

    onDocTypeChange(): void {
        this.selectedDocument = null;
        this.lineItems        = [];
        this.rrLinkedData     = null;
        this.rrConfirmedForJv = false;
    }

    getDocTypeLabel(): string {
        return DOC_TYPE_OPTIONS.find(o => o.value === this.selectedDocType)?.label || '';
    }

    // ── Document browse ────────────────────────────────────────────────────

    async openDocBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseAccountSettingDocModalComponent,
                { docType: this.selectedDocType },
                { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const doc = result.data;
                this.selectedDocument = doc;
                this.rrLinkedData     = null;
                this.rrConfirmedForJv = false;
                this.lineItems        = [];
                this.loadDocumentLineItems(doc);
            }
        } catch { }
    }

    private loadDocumentLineItems(doc: any): void {
        const transactionId = doc.transactionId || doc.id;
        this.isLoading.set(true);
        this.service.getDetails(transactionId).subscribe({
            next: (details: any[]) => {
                this.isLoading.set(false);
                const isRR       = this.selectedDocType === 'rr';
                const isMctOrMst = ['mct', 'mst'].includes(this.selectedDocType);

                this.lineItems = (details || []).map((d: any) => ({
                    itemStockDetailId: d.itemStockDetailId,
                    itemCode:          d.itemCode,
                    itemDescription:   d.itemDescription,
                    unitCode:          d.unitCode,
                    debitAccount:      isRR ? (d.debitAccount || null) : null,
                    creditAccount:     isRR       ? (this.supplierAccount || null)
                                     : isMctOrMst ? (d.creditAccount || null)
                                     : null,
                    isSelected: false
                }));

                if (isRR) {
                    this.service.getRrLinkedDetails(doc.id).subscribe({
                        next: (rrData) => {
                            this.rrLinkedData     = rrData;
                            this.rrConfirmedForJv = !!(rrData?.cvCode || rrData?.calId || rrData?.useCreditCard);
                            if (rrData?.remarks) this.remarks = rrData.remarks;
                        },
                        error: () => {}
                    });
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Load Details', 'Failed to load document details.');
            }
        });
    }

    clearDocument(): void {
        this.selectedDocument = null;
        this.lineItems        = [];
        this.rrLinkedData     = null;
        this.rrConfirmedForJv = false;
    }

    // ── Account browse ─────────────────────────────────────────────────────

    async openAccBrowse(rowIndex: number, col: 'debit' | 'credit'): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseCOAModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                if (col === 'debit') {
                    this.lineItems[rowIndex].debitAccount = result.data;
                } else {
                    this.lineItems[rowIndex].creditAccount = result.data;
                }
            }
        } catch { }
    }

    async openBatchAccBrowse(col: 'debit' | 'credit'): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseCOAModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                if (col === 'debit') {
                    this.batchDebitAccount = result.data;
                } else {
                    this.batchCreditAccount = result.data;
                }
            }
        } catch { }
    }

    applyBatchDebit(): void {
        if (!this.batchDebitAccount) return;
        this.lineItems.forEach(item => {
            if (item.isSelected) item.debitAccount = { ...this.batchDebitAccount };
        });
    }

    applyBatchCredit(): void {
        if (!this.batchCreditAccount) return;
        this.lineItems.forEach(item => {
            if (item.isSelected) item.creditAccount = { ...this.batchCreditAccount };
        });
    }

    get anySelected(): boolean {
        return this.lineItems.some(i => i.isSelected);
    }

    get allSelected(): boolean {
        return this.lineItems.length > 0 && this.lineItems.every(i => i.isSelected);
    }

    toggleAll(checked: boolean): void {
        this.lineItems.forEach(i => i.isSelected = checked);
    }

    getAccountLabel(account: any): string {
        if (!account) return '';
        return `${account.accountCode || account.code || ''} — ${account.accountTitle || account.title || ''}`.trim();
    }

    // ── Save ───────────────────────────────────────────────────────────────

    save(): void {
        this.submitted = true;

        if (!this.date) {
            this.alertService.warning(this.module, 'Validation', 'Please provide a date.');
            return;
        }
        if (!this.selectedDocument) {
            this.alertService.warning(this.module, 'Validation', 'Please select a document.');
            return;
        }

        this.formSubmit = true;

        const opt = DOC_TYPE_OPTIONS.find(o => o.value === this.selectedDocType)!;
        const payload: any = {
            date:              this.date,
            remarks:           this.remarks,
            rrConfirmedForJv:  this.rrConfirmedForJv,
            [opt.field]:       { id: this.selectedDocument.id },
            accountSettingDetails: this.lineItems.map(li => ({
                itemStockDetailId: li.itemStockDetailId,
                debitAccount:      li.debitAccount  ? { id: li.debitAccount.id,  hasSL: li.debitAccount.hasSL  ? 1 : 0 } : null,
                creditAccount:     li.creditAccount ? { id: li.creditAccount.id, hasSL: li.creditAccount.hasSL ? 1 : 0 } : null,
            }))
        };

        for (const o of DOC_TYPE_OPTIONS) {
            if (o.field !== opt.field) payload[o.field] = null;
        }

        if (this.editMode) payload.id = this.id;

        const req = this.editMode ? this.service.update(payload) : this.service.create(payload);
        req.subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res.success) {
                    this.alertService.success(this.module, 'Saved', '');
                    this.router.navigate(['/' + this.menuLink, res.modelId, 'detail']);
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
