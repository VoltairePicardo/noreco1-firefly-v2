import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import Swal from 'sweetalert2';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { DisbursementService } from '../disbursement.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseBudgetLineItemModalComponent } from '@/app/shared/modals/browse-budget-line-item-modal/browse-budget-line-item-modal.component';
import { BrowseCashflowItemModalComponent } from '@/app/shared/modals/browse-cashflow-item-modal/browse-cashflow-item-modal.component';
import { DocumentLogsComponent } from '@/app/shared/components/document-logs/document-logs.component';
import { AttachmentsComponent } from '@/app/shared/components/attachments/attachments.component';
import { JournalEntriesFormComponent, JournalEntry } from '@/app/shared/forms/journal-entries-form/journal-entries-form.component';
import { provideIcons } from '@ng-icons/core';
import {
    tablerArrowLeft, tablerPrinter, tablerEdit, tablerCheck, tablerEye, tablerEyeOff,
    tablerChevronDown, tablerChevronRight, tablerFile, tablerSearch, tablerX, tablerDeviceFloppy
} from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-disbursement-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, DocumentLogsComponent, AttachmentsComponent, JournalEntriesFormComponent],
    providers: [...SHARED_PROVIDERS, provideIcons({
        tablerArrowLeft, tablerPrinter, tablerEdit, tablerCheck, tablerEye, tablerEyeOff,
        tablerChevronDown, tablerChevronRight, tablerFile, tablerSearch, tablerX, tablerDeviceFloppy
    })],
    templateUrl: './disbursement-detail.component.html'
})
export class DisbursementDetailComponent {
    module    = 'Disbursement';
    subModule = 'Detail';
    menuLink  = 'disbursement';
    id: any   = 0;
    data: any = {};
    journalEntries  : any[] = [];
    editableEntries : JournalEntry[] = [];
    savingEntries     = false;
    iemopBillings : any[] = [];
    attachments   : any[] = [];
    expandedSl    = new Set<number>();
    printingCheckRows = new Set<any>();
    isLoading          = signal(false);
    processingWorkflow = false;

    workflowActions: any[] = [];
    selectedAction: any    = null;
    remarks = '';

    showLogs = false;

    // Budget assignment (forAddingBudgetDetail workflow step)
    budgetLineItems       : any[] = [];
    budgetLineItemsLoaded = false;
    selectedBudgetLineItem: any   = null;
    cashflowItems         : any[] = [];
    cashflowItemsLoaded   = false;
    selectedBudgetDetail  : any   = null;
    savingBudget = false;

    private service      = inject(DisbursementService);
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
                this.processingWorkflow = false;
                if (data?.id) {
                    this.data = data;
                    this.iemopBillings  = data.iemopBillings  || [];
                    this.expandedSl.clear();
                    this.selectedBudgetLineItem = null;
                    this.selectedBudgetDetail   = null;
                    this.loadAttachments();
                    if (data.transId) { this.loadJournalEntries(data.transId); }
                    if (data.forAddingBudgetDetail) {
                        if (!this.budgetLineItemsLoaded) {
                            this.service.getBudgetLineItems().subscribe({
                                next: (items) => { this.budgetLineItems = items || []; this.budgetLineItemsLoaded = true; },
                                error: () => {}
                            });
                        }
                        if (!this.cashflowItemsLoaded) {
                            this.service.getCashflowBudgetDetails().subscribe({
                                next: (items) => { this.cashflowItems = items || []; this.cashflowItemsLoaded = true; },
                                error: () => { this.alertService.error(this.module, 'Failed to load cash flow items.', ''); }
                            });
                        }
                    }
                    this.loadWorkflowActions(); // sets isLoading(false) on completion
                } else {
                    this.isLoading.set(false);
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.processingWorkflow = false;
                this.alertService.error(this.module, 'Error', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    loadJournalEntries(transId: number): void {
        this.service.getGLEntries(transId).subscribe({
            next: (entries) => {
                this.journalEntries = entries || [];
                this.editableEntries = (entries || []).map((e: any): JournalEntry => ({
                    account: { id: e.accountId, accountCode: e.code, accountTitle: e.description, hasSL: e.hasSL || false },
                    debit:   Number(e.debit)  || null,
                    credit:  Number(e.credit) || null,
                    wTaxEntry: e.wTaxEntry || null,
                    slentries: (e.slentries || []).map((sl: any) => ({
                        entity: null, accountNo: sl.accountNo, name: sl.name,
                        debit: Number(sl.debit) || null, credit: Number(sl.credit) || null
                    }))
                }));
            },
            error: () => { this.journalEntries = []; this.editableEntries = []; }
        });
    }

    isForCheckingOrReviewing(): boolean {
        const s = this.data?.documentStatus?.status || '';
        return s === 'For Checking' || s === 'For Reviewing';
    }

    saveEntries(): void {
        if (this.savingEntries) return;
        this.savingEntries = true;
        const payload = {
            id: this.data.id,
            generalLedgerLines: this.editableEntries.map(e => ({
                code:        e.account?.accountCode  || '',
                description: e.account?.accountTitle || '',
                accountId:   e.account?.id           || null,
                debit:       Number(e.debit)         || 0,
                credit:      Number(e.credit)        || 0,
                wTaxEntry:   e.wTaxEntry             || null
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

    loadWorkflowActions(): void {
        if (!this.data?.transId) { this.isLoading.set(false); return; }
        this.workflowActions = [];
        this.service.getWorkflowActions(this.data.transId).subscribe({
            next: (actions) => {
                this.isLoading.set(false);
                this.workflowActions = actions || [];
                this.selectedAction = null;
                this.remarks = '';
            },
            error: () => {
                this.isLoading.set(false);
                this.workflowActions = [];
            }
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
                if (res.success) {
                    this.alertService.success(this.module, 'Processed', '');
                    this.loadData();
                } else {
                    this.processingWorkflow = false;
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

    get totalDebit(): number {
        return this.journalEntries.reduce((sum, e) => sum + (Number(e.debit ?? e.debitAmount) || 0), 0);
    }

    get totalCredit(): number {
        return this.journalEntries.reduce((sum, e) => sum + (Number(e.credit ?? e.creditAmount) || 0), 0);
    }

    get iemopBaseTotal(): number {
        return this.iemopBillings.reduce((s, b) => s + ((b.vatablePurchases || 0) + (b.zeroRatedEcoPurchases || 0)), 0);
    }

    get iemopWtaxTotal(): number {
        return this.iemopBillings.reduce((s, b) => s + (b.ewtPurchases || 0), 0);
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

    get sourceVoucherType(): string | null {
        if (this.data.apvDto) return 'Account Payable Voucher';
        if (this.data.jvDto)  return 'Journal Voucher';
        if (this.data.rrDto)  return 'Receiving Report';
        if (this.data.joaDto) return 'JO Acceptance';
        if (this.data.rfpDto) return 'Request for Payment';
        return null;
    }

    get sourceVoucher(): any {
        return this.data.apvDto || this.data.jvDto || this.data.rrDto || this.data.joaDto || this.data.rfpDto || null;
    }

    get checkNumberRows(): any[] {
        return this.data.bankAccountDetailsMap || [];
    }

    get hasWtax(): boolean {
        return !!this.data.hasTax;
    }

    loadAttachments(): void {
        if (!this.data?.transId) return;
        this.service.getFiles(this.data.transId).subscribe({
            next: (files) => { this.attachments = files || []; },
            error: () => { this.attachments = []; }
        });
    }

    fileUrl = (fileId: number): string => this.service.fileUrl(fileId);

    toggleLogs(): void {
        this.showLogs = !this.showLogs;
    }

    async openBudgetLineItemBrowse(): Promise<void> {
        if (!this.budgetLineItemsLoaded) {
            this.service.getBudgetLineItems().subscribe({
                next: (items) => {
                    this.budgetLineItems = items || [];
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
                { items: this.budgetLineItems },
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.selectedBudgetLineItem = result.data;
                this.selectedBudgetDetail   = null; // mutually exclusive
            }
        } catch { }
    }

    async openCashflowItemBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseCashflowItemModalComponent,
                { items: this.cashflowItems },
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.selectedBudgetDetail   = result.data;
                this.selectedBudgetLineItem = null;
            }
        } catch { }
    }

    saveBudgetAssignment(): void {
        const hasBudgetLineItem = this.selectedBudgetLineItem || this.data?.budgetLineItemDetail?.id;
        const hasBudgetDetail   = this.selectedBudgetDetail   || this.data?.budgetDetail?.id;
        if (!hasBudgetLineItem && !hasBudgetDetail) {
            this.alertService.warning(this.module, 'Validation', 'Please select a Budget Line Item or Cash Flow Item.');
            return;
        }
        this.savingBudget = true;
        this.service.assignBudget({
            transactionId:          this.data.transId,
            budgetLineItemDetailId: this.selectedBudgetLineItem?.id ?? this.data?.budgetLineItemDetail?.id ?? null,
            budgetDetailId:         this.selectedBudgetDetail?.id   ?? this.data?.budgetDetail?.id        ?? null
        }).subscribe({
            next: (res) => {
                this.savingBudget = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Budget assigned.', '');
                    this.loadData();
                } else {
                    this.alertService.error(this.module, 'Save failed.', res?.failureMessage || '');
                }
            },
            error: () => {
                this.savingBudget = false;
                this.alertService.error(this.module, 'An error occurred.', '');
            }
        });
    }

    print(): void {
        this.service.print(this.id);
    }

    print2307(): void {
        if (!this.data?.payee?.accountNo || !this.data?.transId) return;
        this.service.print2307(this.data.payee.accountNo, this.data.transId);
    }

    print2307SubSuppliers(): void {
        if (!this.data?.transId) return;
        this.service.print2307SubSuppliers(this.data.transId);
    }

    async printCheck(row: any): Promise<void> {
        const checkNumber = (row.checkNumber || '').trim();
        if (!checkNumber || this.printingCheckRows.has(row)) return;

        if (row.printed) {
            const result = await Swal.fire({
                title: 'Reprint Check?',
                text: 'This check has already been printed. Are you sure you want to reprint?',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonText: 'Yes, reprint',
                cancelButtonText: 'Cancel'
            });
            if (!result.isConfirmed) return;
        }

        this.service.printCheck(row.transId, row.bankAccount.id);

        this.printingCheckRows.add(row);
        const payload = {
            checkNumber,
            transaction: { id: row.transId },
            bankAccount: { id: row.bankAccount.id },
            account:     { id: row.bankAccount.account?.id }
        };
        this.service.updateCheckNumber(payload).subscribe({
            next: (res) => {
                this.printingCheckRows.delete(row);
                if (res?.success) {
                    this.alertService.success(this.module, 'Check Printed', '');
                    this.loadData();
                } else {
                    this.alertService.error(this.module, 'Error', res?.failureMessage || '');
                }
            },
            error: () => {
                this.printingCheckRows.delete(row);
                this.alertService.error(this.module, 'Error', '');
            }
        });
    }
}
