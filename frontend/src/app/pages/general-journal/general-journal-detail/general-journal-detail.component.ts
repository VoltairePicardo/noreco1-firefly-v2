import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { GeneralJournalService } from '../general-journal.service';
import { JournalEntriesFormComponent, JournalEntry } from '@/app/shared/forms/journal-entries-form/journal-entries-form.component';
import { BrowseCashflowItemModalComponent } from '@/app/shared/modals/browse-cashflow-item-modal/browse-cashflow-item-modal.component';
import { BrowseBudgetLineItemModalComponent } from '@/app/shared/modals/browse-budget-line-item-modal/browse-budget-line-item-modal.component';
import { ModalService } from '@/app/shared/modals/modal-service';
import { DocumentLogsComponent } from '@/app/shared/components/document-logs/document-logs.component';
import { AttachmentsComponent } from '@/app/shared/components/attachments/attachments.component';
import { provideIcons } from '@ng-icons/core';
import {
    tablerArrowLeft, tablerPrinter, tablerEdit, tablerEye, tablerEyeOff, tablerCheck,
    tablerChevronDown, tablerChevronRight, tablerDeviceFloppy, tablerPlus, tablerX
} from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-general-journal-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, JournalEntriesFormComponent, DocumentLogsComponent, AttachmentsComponent],
    providers: [...SHARED_PROVIDERS, provideIcons({
        tablerArrowLeft, tablerPrinter, tablerEdit, tablerEye, tablerEyeOff, tablerCheck,
        tablerChevronDown, tablerChevronRight, tablerDeviceFloppy, tablerPlus, tablerX
    })],
    templateUrl: './general-journal-detail.component.html'
})
export class GeneralJournalDetailComponent {
    module    = 'General Journal';
    subModule = 'Detail';
    menuLink  = 'general-journal';
    id: any   = 0;
    data: any = {};
    journalEntries: any[] = [];
    editableEntries: JournalEntry[] = [];
    savingEntries = false;
    attachments   : any[] = [];
    expandedSl    = new Set<number>();
    isLoading   = signal(false);
    processingWorkflow = false;

    workflowActions: any[] = [];
    selectedAction: any    = null;
    remarks = '';

    showLogs = false;

    // Budget detail assignment (forAddingBudgetDetail)
    cashflowBudgetDetails      : any[] = [];
    budgetLineItemsList        : any[] = [];
    cashflowBudgetDetailsLoaded = false;
    budgetLineItemsLoaded       = false;
    selectedCashflowItems  : any[] = [];
    selectedBudgetLineItems: any[] = [];
    savingBudgetDetails = false;

    private service      = inject(GeneralJournalService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.id = params.get('id');
            if (this.id && /^\d+$/.test(String(this.id))) {
                this.loadData();
            }
        });
    }

    loadData(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                if (data?.id) {
                    this.data = data;
                    this.journalEntries = data.generalLedgerLines || [];
                    this.editableEntries = (data.generalLedgerLines || []).map((e: any): JournalEntry => ({
                        account: { id: e.accountId, accountCode: e.code, accountTitle: e.description, hasSL: e.hasSL || false },
                        debit:   Number(e.debit)  || null,
                        credit:  Number(e.credit) || null,
                        slentries: (e.slentries || []).map((sl: any) => ({
                            entity: null, accountNo: sl.accountNo, name: sl.name,
                            debit: Number(sl.debit) || null, credit: Number(sl.credit) || null
                        }))
                    }));
                    this.expandedSl.clear();
                    // Pre-populate editable budget items from saved data
                    this.selectedCashflowItems   = (data.journalVoucherCashFlowBudgetDetails || []).map((item: any) => ({
                        id:           item.budgetDetail?.id,
                        name:         item.budgetDetail?.cashflowItem?.name || '—',
                        totalBalance: item.totalBalance
                    }));
                    this.selectedBudgetLineItems = (data.journalVoucherBudgetLineItemDetails || []).map((item: any) => ({
                        id:                        item.budgetLineItemDetail?.id,
                        title:                     item.budgetLineItemDetail?.title || '—',
                        code:                      item.budgetLineItemDetail?.code,
                        budgetAmountBalanceCV:      item.budgetAmountBalanceCV,
                        budgetAmountBalancePOJORFP: item.budgetAmountBalancePOJORFP
                    }));
                    if (data.forAddingBudgetDetail) {
                        if (!this.cashflowBudgetDetailsLoaded) {
                            this.service.getCashflowBudgetDetails().subscribe({
                                next: (items) => { this.cashflowBudgetDetails = items || []; this.cashflowBudgetDetailsLoaded = true; },
                                error: () => {}
                            });
                        }
                        if (!this.budgetLineItemsLoaded) {
                            this.service.getBudgetLineItems().subscribe({
                                next: (items) => { this.budgetLineItemsList = items || []; this.budgetLineItemsLoaded = true; },
                                error: () => {}
                            });
                        }
                    }
                    this.loadWorkflowActions();
                    this.loadAttachments();
                } else {
                    this.isLoading.set(false);
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

    loadWorkflowActions(): void {
        if (!this.data?.transId) { this.isLoading.set(false); return; }
        this.service.getWorkflowActions(this.data.transId).subscribe({
            next: (actions) => { this.isLoading.set(false); this.workflowActions = actions || []; this.selectedAction = null; this.remarks = ''; },
            error: () => { this.isLoading.set(false); this.workflowActions = []; }
        });
    }

    loadAttachments(): void {
        if (!this.data?.transId) return;
        this.service.getFiles(this.data.transId).subscribe({
            next: (files) => { this.attachments = files || []; },
            error: () => { this.attachments = []; }
        });
    }

    processWorkflow(): void {
        if (!this.selectedAction) return;
        this.processingWorkflow = true;
        const payload = {
            documentId:         this.data.id,
            transId:            this.data.transId,
            workflowActionsDto: this.selectedAction,
            remarks:            this.remarks || ''
        };
        this.service.process(payload).subscribe({
            next: (res) => {
                this.processingWorkflow = false;
                if (res.success) {
                    this.alertService.success(this.module, 'Processed', '');
                    this.loadData();
                } else {
                    this.alertService.error(this.module, 'Process', res.failureMessage || '');
                }
            },
            error: () => { this.processingWorkflow = false; this.alertService.error(this.module, 'Process', ''); }
        });
    }

    isEditable(): boolean {
        const s = this.data?.documentStatus?.status || this.data?.status || '';
        return s === 'Document Created' || s === 'Returned to Creator';
    }

    isForChecking(): boolean {
        const s = this.data?.documentStatus?.status || this.data?.status || '';
        return s === 'For Checking';
    }

    get editableDebit(): number {
        return this.editableEntries.reduce((sum, e) => sum + (Number(e.debit) || 0), 0);
    }

    saveEntries(): void {
        if (this.savingEntries) return;
        this.savingEntries = true;
        const payload = {
            id:     this.data.id,
            amount: this.editableDebit,
            generalLedgerLines: this.editableEntries.map(e => ({
                code:        e.account?.accountCode  || '',
                description: e.account?.accountTitle || e.account?.accountDescription || '',
                accountId:   e.account?.id           || null,
                debit:       Number(e.debit)         || 0,
                credit:      Number(e.credit)        || 0,
                hasSL:       !!e.account?.hasSL,
                wTaxEntry:   e.wTaxEntry || null,
                vatEntry:    e.vatEntry  || null
            })),
            subLedgerLines: this.editableEntries
                .filter(e => (e.slentries || []).length > 0)
                .flatMap(e => (e.slentries || []).map((sl: any) => ({
                    accountId: e.account?.id || null,
                    accountNo: sl.accountNo,
                    name:      sl.name,
                    debit:     Number(sl.debit)  || 0,
                    credit:    Number(sl.credit) || 0
                })))
        };
        this.service.updateEntries(payload).subscribe({
            next: (res) => {
                this.savingEntries = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Entries saved successfully.', '');
                    this.loadData();
                } else {
                    this.alertService.error(this.module, 'Save failed.', res?.failureMessage || '');
                }
            },
            error: () => {
                this.savingEntries = false;
                this.alertService.error(this.module, 'An error occurred while saving entries.', '');
            }
        });
    }

    get totalDebit(): number {
        return this.journalEntries.reduce((sum, e) => sum + (Number(e.debit) || 0), 0);
    }

    get totalCredit(): number {
        return this.journalEntries.reduce((sum, e) => sum + (Number(e.credit) || 0), 0);
    }

    get sourceDocumentType(): string | null {
        if (this.data.cashAdvanceLiquidation) return 'Cash Advance Liquidation';
        if (this.data.document?.extensionUrl === 'receiving-report') return 'Receiving Report';
        if (this.data.document) return 'Stock Receive / Stock Adjustment';
        return null;
    }

    get sourceDocument(): { code: string; amount: number; voucherDate: string; particulars: string } | null {
        const cal = this.data.cashAdvanceLiquidation;
        if (cal) {
            return { code: cal.code, amount: cal.amount, voucherDate: cal.voucherDate, particulars: cal.remarks };
        }
        const doc = this.data.document;
        if (doc) {
            return { code: doc.localCode, amount: doc.netAmount, voucherDate: doc.voucherDate, particulars: doc.particulars };
        }
        return null;
    }

    isSlExpanded(index: number): boolean {
        return this.expandedSl.has(index);
    }

    toggleSl(index: number): void {
        if (this.expandedSl.has(index)) {
            this.expandedSl.delete(index);
        } else {
            this.expandedSl.add(index);
        }
    }

    fileUrl = (fileId: number): string => this.service.fileUrl(fileId);

    toggleLogs(): void {
        this.showLogs = !this.showLogs;
    }

    async openCashflowItemBrowse(): Promise<void> {
        if (!this.cashflowBudgetDetailsLoaded) {
            this.service.getCashflowBudgetDetails().subscribe({
                next: (items) => {
                    this.cashflowBudgetDetails = items || [];
                    this.cashflowBudgetDetailsLoaded = true;
                    this.openCashflowItemBrowse();
                },
                error: () => {}
            });
            return;
        }
        try {
            const result = await this.modalService.openModal(
                BrowseCashflowItemModalComponent,
                { items: this.cashflowBudgetDetails },
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const item = result.data;
                if (!this.selectedCashflowItems.some(i => i.id === item.id)) {
                    this.selectedCashflowItems.push({ id: item.id, name: item.name, totalBalance: item.totalBalance });
                }
            }
        } catch { }
    }

    removeCashflowItem(index: number): void {
        this.selectedCashflowItems.splice(index, 1);
    }

    async openBudgetLineItemBrowse(): Promise<void> {
        if (!this.budgetLineItemsLoaded) {
            this.service.getBudgetLineItems().subscribe({
                next: (items) => {
                    this.budgetLineItemsList = items || [];
                    this.budgetLineItemsLoaded = true;
                    this.openBudgetLineItemBrowse();
                },
                error: () => {}
            });
            return;
        }
        try {
            const result = await this.modalService.openModal(
                BrowseBudgetLineItemModalComponent,
                { items: this.budgetLineItemsList },
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const item = result.data;
                if (!this.selectedBudgetLineItems.some(i => i.id === item.id)) {
                    this.selectedBudgetLineItems.push({
                        id:    item.id,
                        code:  item.code,
                        title: item.title,
                        budgetAmountBalanceCV:      null,
                        budgetAmountBalancePOJORFP: null
                    });
                }
            }
        } catch { }
    }

    removeBudgetLineItem(index: number): void {
        this.selectedBudgetLineItems.splice(index, 1);
    }

    saveBudgetDetails(): void {
        if (this.savingBudgetDetails) return;
        if (this.selectedCashflowItems.length === 0 && this.selectedBudgetLineItems.length === 0) {
            this.alertService.warning(this.module, 'Validation', 'Please add at least one cash flow item or budget line item.');
            return;
        }
        this.savingBudgetDetails = true;
        const payload = {
            transactionId:        this.data.transId,
            budgetDetails:        this.selectedCashflowItems.map(i => ({ id: i.id, totalBalance: i.totalBalance })),
            budgetLineItemDetails: this.selectedBudgetLineItems.map(i => ({
                id:                        i.id,
                budgetAmountBalanceCV:      i.budgetAmountBalanceCV,
                budgetAmountBalancePOJORFP: i.budgetAmountBalancePOJORFP
            }))
        };
        this.service.saveAdditionalBudgetDetails(payload).subscribe({
            next: (res) => {
                this.savingBudgetDetails = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Budget details saved.', '');
                    this.loadData();
                } else {
                    this.alertService.error(this.module, 'Save failed.', res?.failureMessage || '');
                }
            },
            error: () => { this.savingBudgetDetails = false; this.alertService.error(this.module, 'Save failed.', ''); }
        });
    }

    print(): void {
        this.service.print(this.id);
    }
}
