import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { GeneralJournalService } from '../general-journal.service';
import { provideIcons } from '@ng-icons/core';
import {
    tablerArrowLeft, tablerPrinter, tablerEdit, tablerEye, tablerEyeOff, tablerCheck,
    tablerChevronDown, tablerChevronRight
} from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-general-journal-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({
        tablerArrowLeft, tablerPrinter, tablerEdit, tablerEye, tablerEyeOff, tablerCheck,
        tablerChevronDown, tablerChevronRight
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
    attachments   : any[] = [];
    expandedSl    = new Set<number>();
    isLoading   = signal(false);
    processingWorkflow = false;

    workflowActions: any[] = [];
    selectedAction: any    = null;
    remarks = '';

    logs        : any[] = [];
    showLogs    = false;
    logsLoading = false;

    private service      = inject(GeneralJournalService);
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
                this.isLoading.set(false);
                if (data?.id) {
                    this.data = data;
                    this.journalEntries = data.generalLedgerLines || [];
                    this.expandedSl.clear();
                    this.loadWorkflowActions();
                    this.loadAttachments();
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

    loadWorkflowActions(): void {
        if (!this.data?.transId) return;
        this.service.getWorkflowActions(this.data.transId).subscribe({
            next: (actions) => { this.workflowActions = actions || []; this.selectedAction = null; this.remarks = ''; },
            error: () => { this.workflowActions = []; }
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

    fileUrl(fileId: number): string {
        return this.service.fileUrl(fileId);
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
