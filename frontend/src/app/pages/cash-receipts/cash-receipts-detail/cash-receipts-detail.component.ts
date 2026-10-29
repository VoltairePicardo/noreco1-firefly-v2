import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { CashReceiptsService } from '../cash-receipts.service';
import { JournalEntriesFormComponent, JournalEntry } from '@/app/shared/forms/journal-entries-form/journal-entries-form.component';
import { DocumentLogsComponent } from '@/app/shared/components/document-logs/document-logs.component';
import { AttachmentsComponent } from '@/app/shared/components/attachments/attachments.component';
import { provideIcons } from '@ng-icons/core';
import {
    tablerArrowLeft, tablerPrinter, tablerEdit, tablerEye, tablerEyeOff, tablerCheck,
    tablerChevronDown, tablerChevronRight, tablerDeviceFloppy
} from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-cash-receipts-detail',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS,
        JournalEntriesFormComponent, DocumentLogsComponent, AttachmentsComponent
    ],
    providers: [...SHARED_PROVIDERS, provideIcons({
        tablerArrowLeft, tablerPrinter, tablerEdit, tablerEye, tablerEyeOff, tablerCheck,
        tablerChevronDown, tablerChevronRight, tablerDeviceFloppy
    })],
    templateUrl: './cash-receipts-detail.component.html'
})
export class CashReceiptsDetailComponent {
    module    = 'Cash Receipts';
    subModule = 'Detail';
    menuLink  = 'cash-receipts';
    id: any   = 0;
    data: any = {};
    journalEntries : any[]        = [];
    editableEntries: JournalEntry[] = [];
    attachments    : any[]        = [];
    expandedSl     = new Set<number>();
    savingEntries      = false;
    isLoading          = signal(false);
    processingWorkflow = false;

    workflowActions: any[] = [];
    selectedAction: any    = null;
    remarks = '';

    showLogs = false;

    private service      = inject(CashReceiptsService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.id = params.get('id');
            if (this.id && /^\d+$/.test(String(this.id))) { this.loadData(); }
        });
    }

    loadData(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                if (data?.id) {
                    this.data = data;
                    const glLines: any[] = data.generalLedgerLines || [];

                    this.editableEntries = glLines.map((e: any): JournalEntry => ({
                        account: {
                            id:           e.accountId,
                            accountCode:  e.code,
                            accountTitle: e.description,
                            hasSL:        e.hasSL || false
                        },
                        debit:     Number(e.debit)  || null,
                        credit:    Number(e.credit) || null,
                        slentries: (e.slentries || []).map((sl: any) => ({
                            entity: null, accountNo: sl.accountNo, name: sl.name,
                            debit: Number(sl.debit) || null, credit: Number(sl.credit) || null
                        }))
                    }));
                    this.expandedSl.clear();
                    this.loadWorkflowActions();
                    this.loadAttachments();
                    if (data.transId) { this.loadJournalEntries(data.transId); }
                } else {
                    this.isLoading.set(false);
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Error', ''); this.router.navigate(['/' + this.menuLink]); }
        });
    }

    loadJournalEntries(transId: number): void {
        this.service.getJournalEntries(transId).subscribe({
            next: (data) => { this.journalEntries = data || []; },
            error: ()    => { this.journalEntries = []; }
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
                if (res.success) { this.alertService.success(this.module, 'Processed', ''); this.loadData(); }
                else { this.alertService.error(this.module, 'Process', res.failureMessage || ''); }
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

    get totalDebit(): number {
        return this.journalEntries.reduce((sum, e) => sum + (Number(e.debit) || 0), 0);
    }

    get totalCredit(): number {
        return this.journalEntries.reduce((sum, e) => sum + (Number(e.credit) || 0), 0);
    }

    get editableDebit(): number {
        return this.editableEntries.reduce((sum, e) => sum + (Number(e.debit) || 0), 0);
    }

    isSlExpanded(index: number): boolean { return this.expandedSl.has(index); }

    toggleSl(index: number): void {
        if (this.expandedSl.has(index)) { this.expandedSl.delete(index); }
        else { this.expandedSl.add(index); }
    }

    saveEntries(): void {
        if (this.savingEntries) return;
        this.savingEntries = true;
        const payload = {
            id:     this.data.id,
            amount: this.editableDebit,
            generalLedgerLines: this.editableEntries.map(e => ({
                code:        e.account?.accountCode  || '',
                description: e.account?.accountTitle || '',
                accountId:   e.account?.id           || null,
                debit:       Number(e.debit)          || 0,
                credit:      Number(e.credit)         || 0,
                hasSL:       !!e.account?.hasSL
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
            error: () => { this.savingEntries = false; this.alertService.error(this.module, 'An error occurred.', ''); }
        });
    }

    fileUrl = (fileId: number): string => this.service.fileUrl(fileId);

    toggleLogs(): void { this.showLogs = !this.showLogs; }

    print(): void { this.service.print(this.id); }
}
