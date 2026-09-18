import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { GeneralJournalService } from '../general-journal.service';
import { provideIcons } from '@ng-icons/core';
import { NgbCollapse } from '@ng-bootstrap/ng-bootstrap';
import { tablerArrowLeft, tablerPrinter, tablerEdit, tablerEye, tablerEyeOff, tablerCheck, tablerChevronDown, tablerChevronRight } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-general-journal-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, NgbCollapse],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerArrowLeft, tablerPrinter, tablerEdit, tablerEye, tablerEyeOff, tablerCheck, tablerChevronDown, tablerChevronRight })],
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
                    const rawEntries: any[] = data.journalEntries || data.details || [];
                    // SL sub-rows are flattened into the same list, right after their parent
                    // GL row, with "&nbsp;"-prefixed labels and amounts in slDebitAmount/
                    // slCreditAmount instead of glDebitAmount/glCreditAmount — regroup them
                    // under their parent so they can be shown in a collapsible breakdown
                    this.journalEntries = [];
                    for (const e of rawEntries) {
                        const isSlEntry = e.glDebitAmount == null && e.glCreditAmount == null;
                        const cleaned = {
                            ...e,
                            accountCode:  String(e.accountCode || '').replace(/(&nbsp;)+/g, '').trim(),
                            accountTitle: String(e.accountTitle || '').replace(/(&nbsp;)+/g, '').trim(),
                        };
                        if (isSlEntry) {
                            this.journalEntries[this.journalEntries.length - 1]?.slEntries.push(cleaned);
                        } else {
                            this.journalEntries.push({ ...cleaned, slEntries: [], expanded: false });
                        }
                    }
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
        if (!this.data?.id) return;
        this.service.getFiles(this.data.id).subscribe({
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
        return this.journalEntries.reduce((sum, e) => sum + (Number(e.glDebitAmount) || 0), 0);
    }

    get totalCredit(): number {
        return this.journalEntries.reduce((sum, e) => sum + (Number(e.glCreditAmount) || 0), 0);
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
