import { Component, ElementRef, ViewChild, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FormsModule } from '@angular/forms';
import { provideIcons } from '@ng-icons/core';
import { tablerPrinter, tablerEdit, tablerPaperclip, tablerPhoto, tablerFile, tablerTrash, tablerArrowLeft, tablerCheck, tablerEye, tablerEyeOff } from '@ng-icons/tabler-icons';
import { AlertService } from '@/app/shared/services/alert.service';
import { forkJoin } from 'rxjs';
import { JoAcceptanceService } from '../jo-acceptance.service';
import { SharedModule } from '@/app/shared/shared.module';

const TERMINAL_STATUSES = ['Approved', 'Denied', 'Cancelled'];

@Component({
    selector: 'app-jo-acceptance-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, SharedModule, FormsModule, RouterLink],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerPrinter, tablerEdit, tablerPaperclip, tablerPhoto, tablerFile, tablerTrash, tablerArrowLeft, tablerCheck, tablerEye, tablerEyeOff })],
    templateUrl: './jo-acceptance-detail.component.html'
})
export class JoAcceptanceDetailComponent {
    module    = 'JO Certification / Acceptance';
    subModule = 'Details';
    menuLink  = 'jo-acceptance';

    @ViewChild('fileInput') fileInputRef!: ElementRef<HTMLInputElement>;

    id: any   = 0;
    data: any = {};
    lineItems: any[] = [];
    isLoading = signal(false);

    // Attachments
    attachments:   any[] = [];
    uploadingFiles = false;

    workflowActions:   any[] = [];
    selectedAction:    any   = null;
    remarks            = '';
    processingWorkflow = false;

    logs        : any[] = [];
    showLogs    = false;
    logsLoading = false;

    private service      = inject(JoAcceptanceService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.id = params.get('id');
            if (this.id && /^\d+$/.test(String(this.id))) this.loadData();
        });
    }

    loadData(): void {
        this.isLoading.set(true);
        forkJoin({
            header:  this.service.getData(this.id),
            details: this.service.getDetails(this.id)
        }).subscribe({
            next: ({ header, details }) => {
                this.isLoading.set(false);
                if (header?.id) {
                    this.data           = header;
                    this.lineItems      = details || [];
                    this.logs           = [];
                    this.showLogs       = false;
                    this.selectedAction = null;
                    this.remarks        = '';
                    this.loadWorkflowActions();
                    this.loadAttachments();
                } else {
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Error loading record.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    loadWorkflowActions(): void {
        if (!this.data?.transId || this.isTerminal()) {
            this.workflowActions = [];
            this.selectedAction  = null;
            return;
        }
        this.service.getWorkflowActions(this.data.transId).subscribe({
            next: (actions) => { this.workflowActions = actions || []; this.selectedAction = null; this.remarks = ''; },
            error: () => { this.workflowActions = []; }
        });
    }

    isTerminal(): boolean {
        return TERMINAL_STATUSES.includes(this.data?.documentStatus?.status || '');
    }

    isEditable(): boolean {
        const s = this.data?.documentStatus?.status || '';
        return s === 'Document Created' || s === 'For Revision';
    }

    processWorkflow(): void {
        if (!this.selectedAction) return;
        this.processingWorkflow = true;

        this.service.process({
            documentId:         this.data.id,
            remarks:            this.remarks,
            workflowActionsDto: { actionMapId: this.selectedAction.actionMapId }
        }).subscribe({
            next: (res) => {
                this.processingWorkflow = false;
                if (res?.success) {
                    this.workflowActions = [];
                    this.selectedAction  = null;
                    this.remarks         = '';
                    this.alertService.success(this.module, res.successMessage || 'Processed.', '');
                    this.loadData();
                } else {
                    this.alertService.error(this.module, res?.failureMessage || 'Processing failed.', '');
                }
            },
            error: () => {
                this.processingWorkflow = false;
                this.alertService.error(this.module, 'An error occurred.', '');
            }
        });
    }

    toggleLogs(): void {
        this.showLogs = !this.showLogs;
        if (this.showLogs && this.logs.length === 0) {
            this.logsLoading = true;
            this.service.getDocumentLogs(this.data.transId).subscribe({
                next: (logs) => { this.logs = logs || []; this.logsLoading = false; },
                error: () => { this.logsLoading = false; }
            });
        }
    }

    // ─── Attachments ────────────────────────────────────────────────────────

    loadAttachments(): void {
        if (!this.data?.id) return;
        this.service.getFiles(this.data.id).subscribe({
            next:  (files) => { this.attachments = files || []; },
            error: () => { this.attachments = []; }
        });
    }

    openFilePicker(): void {
        this.fileInputRef?.nativeElement.click();
    }

    onFileSelect(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (!input.files?.length) return;

        const formData = new FormData();
        for (const file of Array.from(input.files)) {
            const allowed = ['image/jpeg', 'image/png', 'application/pdf'];
            if (!allowed.includes(file.type)) {
                this.alertService.warning(this.module, 'Invalid file type', `${file.name} must be JPG, PNG, or PDF.`);
                continue;
            }
            formData.append(`file_${file.name}`, file, file.name);
        }
        input.value = '';

        this.uploadingFiles = true;
        this.service.uploadFiles(this.data.id, formData).subscribe({
            next:  () => { this.uploadingFiles = false; this.loadAttachments(); },
            error: () => {
                this.uploadingFiles = false;
                this.alertService.error(this.module, 'Upload failed.', '');
            }
        });
    }

    deleteAttachment(file: any): void {
        this.alertService.confirm(`Remove <strong>${file.originalFilename}</strong>?`).then(result => {
            if (!result.isConfirmed) return;
            this.service.deleteFile(file.id).subscribe({
                next:  () => { this.loadAttachments(); },
                error: () => { this.alertService.error(this.module, 'Delete failed.', ''); }
            });
        });
    }

    isImage(file: any): boolean {
        return /\.(jpg|jpeg|png)$/i.test(file.originalFilename || '') || (file.mimeType || '').startsWith('image/');
    }

    fileUrl(fileId: number): string {
        return this.service.fileUrl(fileId);
    }

    get joaTypeName(): string {
        return this.data?.type === 2 ? 'Liquidation' : 'Payment';
    }

    get lineTotal(): number {
        return this.lineItems.reduce((s: number, li: any) => s + (li.netAmount || 0), 0);
    }

    getLogField(value: string, key: string): string {
        try { return JSON.parse(value)?.[key] || '—'; } catch { return '—'; }
    }

    print(): void { this.service.print(this.data.id); }
}
