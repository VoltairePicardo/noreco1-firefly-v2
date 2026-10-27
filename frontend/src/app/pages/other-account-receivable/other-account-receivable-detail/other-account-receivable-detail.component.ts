import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { OtherAccountReceivableService } from '../other-account-receivable.service';
import { AttachmentsComponent } from '@/app/shared/components/attachments/attachments.component';
import { provideIcons } from '@ng-icons/core';
import { tablerArrowLeft, tablerPrinter, tablerEdit, tablerHistory, tablerCheck, tablerEye, tablerEyeOff, tablerChevronRight, tablerChevronDown } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-other-account-receivable-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, AttachmentsComponent],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerArrowLeft, tablerPrinter, tablerEdit, tablerHistory, tablerCheck, tablerEye, tablerEyeOff, tablerChevronRight, tablerChevronDown })],
    templateUrl: './other-account-receivable-detail.component.html'
})
export class OtherAccountReceivableDetailComponent {
    module    = 'Other Account Receivable';
    subModule = 'Detail';
    menuLink  = 'other-account-receivable';
    id: any   = 0;
    data: any = {};
    journalEntries: any[] = [];
    attachments   : any[] = [];
    isLoading  = signal(false);
    formSubmit = false;

    workflowActions: any[] = [];
    selectedAction: any    = null;
    remarks = '';

    expandedSl = new Set<number>();

    logs        : any[] = [];
    showLogs    = false;
    logsLoading = false;

    private service      = inject(OtherAccountReceivableService);
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
                this.isLoading.set(false);
                if (data?.id) {
                    this.data = data;
                    this.expandedSl.clear();
                    this.loadWorkflowActions();
                    this.loadAttachments();
                    if (data.transId) this.loadJournalEntries(data.transId);
                } else {
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Error', ''); this.router.navigate(['/' + this.menuLink]); }
        });
    }

    loadJournalEntries(transId: number): void {
        this.service.getGLEntries(transId).subscribe({
            next: (entries) => { this.journalEntries = entries || []; },
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
        if (!this.data?.transId) return;
        this.service.getWorkflowActions(this.data.transId).subscribe({
            next: (actions) => { this.workflowActions = actions || []; this.selectedAction = null; this.remarks = ''; },
            error: () => { this.workflowActions = []; }
        });
    }

    processWorkflow(): void {
        if (!this.selectedAction) return;
        this.formSubmit = true;
        const payload = {
            documentId:         this.data.id,
            transId:            this.data.transId,
            workflowActionsDto: this.selectedAction,
            remarks:            this.remarks || ''
        };
        this.service.process(payload).subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res.success) { this.alertService.success(this.module, 'Processed', ''); this.loadData(); }
                else { this.alertService.error(this.module, 'Process', res.failureMessage || ''); }
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'Process', ''); }
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

    isSlExpanded(index: number): boolean { return this.expandedSl.has(index); }

    toggleSl(index: number): void {
        if (this.expandedSl.has(index)) this.expandedSl.delete(index);
        else this.expandedSl.add(index);
    }

    toggleLogs(): void {
        this.showLogs = !this.showLogs;
        if (this.showLogs && this.logs.length === 0 && !this.logsLoading) { this.loadLogs(); }
    }

    loadLogs(): void {
        if (!this.data?.transId || this.logsLoading) return;
        this.logsLoading = true;
        this.service.getDocumentLogs(this.data.transId).subscribe({
            next: (logs) => { this.logs = logs || []; this.logsLoading = false; },
            error: () => { this.logsLoading = false; }
        });
    }

    print(): void { this.service.print(this.id); }
}
