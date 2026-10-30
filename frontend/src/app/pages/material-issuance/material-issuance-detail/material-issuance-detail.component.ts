import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { MaterialIssuanceService } from '../material-issuance.service';
import { AttachmentsComponent } from '@/app/shared/components/attachments/attachments.component';
import { provideIcons } from '@ng-icons/core';
import { tablerArrowLeft, tablerPrinter, tablerEdit, tablerEye, tablerEyeOff, tablerCheck, tablerDeviceFloppy, tablerChevronRight, tablerChevronDown } from '@ng-icons/tabler-icons';
import { JournalEntriesFormComponent, JournalEntry } from '@/app/shared/forms/journal-entries-form/journal-entries-form.component';

@Component({
    selector: 'app-material-issuance-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, JournalEntriesFormComponent, AttachmentsComponent],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerArrowLeft, tablerPrinter, tablerEdit, tablerEye, tablerEyeOff, tablerCheck, tablerDeviceFloppy, tablerChevronRight, tablerChevronDown })],
    templateUrl: './material-issuance-detail.component.html'
})
export class MaterialIssuanceDetailComponent {
    module    = 'Material Issuance';
    subModule = 'Detail';
    menuLink  = 'material-issuance';
    id: any   = 0;
    data: any = {};
    journalEntries: any[]    = [];
    inventoryDocItems: any[] = [];
    attachments: any[]       = [];
    isLoading          = signal(false);
    processingWorkflow = false;
    savingEntries      = false;

    editableJournalEntries: JournalEntry[] = [];

    workflowActions: any[] = [];
    selectedAction: any    = null;
    remarks = '';

    expandedSl = new Set<number>();

    logs        : any[] = [];
    showLogs    = false;
    logsLoading = false;

    private service      = inject(MaterialIssuanceService);
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
                    this.expandedSl.clear();
                    this.inventoryDocItems = data.inventoryDocItems || data.inventoryDocument?.details || data.items || [];
                    this.loadWorkflowActions();
                    this.loadAttachments();
                    if (data.transId) this.loadJournalEntries(data.transId);
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

    loadJournalEntries(transId: number): void {
        this.service.getGLEntries(transId).subscribe({
            next: (entries) => {
                this.journalEntries = entries || [];
                this.editableJournalEntries = this.journalEntries.map((e: any): JournalEntry => ({
                    account: { id: e.accountId, accountCode: e.code, accountTitle: e.description, hasSL: e.hasSL || false },
                    debit:   Number(e.debit ?? e.debitAmount) || null,
                    credit:  Number(e.credit ?? e.creditAmount) || null,
                    slentries: (e.slentries || []).map((sl: any) => ({
                        entity:    null,
                        accountNo: sl.accountNo,
                        name:      sl.name,
                        debit:     Number(sl.debit)  || null,
                        credit:    Number(sl.credit) || null
                    }))
                }));
                if (this.editableJournalEntries.length === 0) {
                    this.editableJournalEntries = [{ account: null, debit: null, credit: null }];
                }
            },
            error: () => { this.journalEntries = []; }
        });
    }

    loadAttachments(): void {
        if (!this.data?.transId) return;
        this.service.getFiles(this.data.transId).subscribe({
            next: (files) => { this.attachments = files || []; },
            error: () => { this.attachments = []; }
        });
    }

    fileUrl = (fileId: number): string => this.service.fileUrl(fileId);

    loadWorkflowActions(): void {
        if (!this.data?.transId) { this.isLoading.set(false); return; }
        this.service.getWorkflowActions(this.data.transId).subscribe({
            next: (actions) => { this.isLoading.set(false); this.workflowActions = actions || []; this.selectedAction = null; this.remarks = ''; },
            error: () => { this.isLoading.set(false); this.workflowActions = []; }
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
        return s.toLowerCase().includes('checking');
    }

    isSlExpanded(index: number): boolean { return this.expandedSl.has(index); }

    toggleSl(index: number): void {
        if (this.expandedSl.has(index)) this.expandedSl.delete(index);
        else this.expandedSl.add(index);
    }

    get totalDebit(): number {
        return this.journalEntries.reduce((sum, e) => sum + (Number(e.debit ?? e.debitAmount) || 0), 0);
    }

    get totalCredit(): number {
        return this.journalEntries.reduce((sum, e) => sum + (Number(e.credit ?? e.creditAmount) || 0), 0);
    }

    get totalDebitEditable(): number {
        return this.editableJournalEntries.reduce((sum, e) => sum + (Number(e.debit) || 0), 0);
    }

    get totalCreditEditable(): number {
        return this.editableJournalEntries.reduce((sum, e) => sum + (Number(e.credit) || 0), 0);
    }

    get isBalanced(): boolean {
        const d = this.totalDebitEditable, c = this.totalCreditEditable;
        return this.editableJournalEntries.length > 0 && Math.abs(d - c) < 0.001;
    }

    updateEntries(): void {
        if (!this.isBalanced) {
            this.alertService.warning(this.module, 'Validation', 'Debit and credit totals must be equal.');
            return;
        }
        this.savingEntries = true;
        const payload = {
            id:                 this.data.id,
            amount:             this.totalDebitEditable,
            generalLedgerLines: this.editableJournalEntries.map(e => ({
                code:        e.account?.accountCode  || '',
                description: e.account?.accountTitle || e.account?.accountDescription || '',
                accountId:   e.account?.id           || null,
                debit:       Number(e.debit)         || 0,
                credit:      Number(e.credit)        || 0
            })),
            subLedgerLines: this.editableJournalEntries
                .filter(e => (e.slentries || []).length > 0)
                .flatMap(e => (e.slentries || []).map(sl => ({
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
                    this.alertService.success(this.module, 'Entries updated successfully.', '');
                    this.loadData();
                } else {
                    this.alertService.error(this.module, 'Update Entries', res?.failureMessage || '');
                }
            },
            error: () => { this.savingEntries = false; this.alertService.error(this.module, 'Update Entries', ''); }
        });
    }

    toggleLogs(): void {
        this.showLogs = !this.showLogs;
        if (this.showLogs && this.logs.length === 0 && !this.logsLoading) {
            this.loadLogs();
        }
    }

    loadLogs(): void {
        if (!this.data?.transId || this.logsLoading) return;
        this.logsLoading = true;
        this.service.getDocumentLogs(this.data.transId).subscribe({
            next: (logs) => { this.logs = logs || []; this.logsLoading = false; },
            error: () => { this.logsLoading = false; }
        });
    }

    print(): void {
        this.service.print(this.id);
    }
}
