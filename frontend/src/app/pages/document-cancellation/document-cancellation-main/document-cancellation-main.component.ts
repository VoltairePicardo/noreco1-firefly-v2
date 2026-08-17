import { Component, inject, signal, TemplateRef } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { FlatpickrDefaults, FlatpickrModule } from 'angularx-flatpickr';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { DocumentCancellationService } from '../document-cancellation.service';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerRefresh, tablerBan, tablerInfoCircle, tablerRotateClockwise, tablerX } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-document-cancellation-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrModule],
    providers: [...SHARED_PROVIDERS, FlatpickrDefaults, provideIcons({ tablerSearch, tablerRefresh, tablerBan, tablerInfoCircle, tablerRotateClockwise, tablerX })],
    templateUrl: './document-cancellation-main.component.html'
})
export class DocumentCancellationMainComponent {
    module    = 'Document Cancellation';
    subModule = '';
    menuLink  = 'document-cancellation';

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    documentTypes    = signal<any[]>([]);
    records          = signal<any[]>([]);
    isLoading        = signal(false);
    formSubmit       = false;

    fromDate         = '';
    toDate           = '';
    selectedDocType: any = null;
    cancelledOnly    = false;

    page     = 1;
    pageSize = 10;

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.records().slice(start, start + this.pageSize);
    }

    // Cancel modal
    cancelTarget: any = null;
    cancelRemarks = '';

    // Restore modal
    restoreTarget: any = null;

    // Remarks details modal
    remarksTarget: any = null;

    private service      = inject(DocumentCancellationService);
    private alertService = inject(AlertService);
    private modalService = inject(NgbModal);

    ngOnInit(): void {
        const now   = new Date();
        const first = new Date(now.getFullYear(), now.getMonth(), 1);
        this.fromDate = first.toISOString().substring(0, 10);
        this.toDate   = now.toISOString().substring(0, 10);
        this.loadDocumentTypes();
    }

    loadDocumentTypes(): void {
        this.service.getDocumentTypes().subscribe({
            next: (data) => this.documentTypes.set(data || []),
            error: () => {}
        });
    }

    load(): void {
        if (!this.selectedDocType) {
            this.alertService.warning(this.module, 'Validation', 'Please select a document type.');
            return;
        }
        if (!this.fromDate || !this.toDate) {
            this.alertService.warning(this.module, 'Validation', 'Please enter a date range.');
            return;
        }
        this.isLoading.set(true);
        this.records.set([]);
        this.page = 1;
        this.service.search(this.selectedDocType.name, this.fromDate, this.toDate, this.cancelledOnly).subscribe({
            next: (data) => {
                this.records.set(data || []);
                this.isLoading.set(false);
            },
            error: () => {
                this.alertService.error(this.module, 'Load', 'Failed to load documents.');
                this.isLoading.set(false);
            }
        });
    }

    reset(): void {
        const now   = new Date();
        const first = new Date(now.getFullYear(), now.getMonth(), 1);
        this.fromDate        = first.toISOString().substring(0, 10);
        this.toDate          = now.toISOString().substring(0, 10);
        this.selectedDocType = null;
        this.cancelledOnly   = false;
        this.records.set([]);
        this.page = 1;
    }

    // ── Cancel ──────────────────────────────────────────────
    openCancelModal(rec: any, tpl: TemplateRef<any>): void {
        this.cancelTarget  = rec;
        this.cancelRemarks = '';
        this.modalService.open(tpl, { size: 'md', centered: true });
    }

    confirmCancel(modal: any): void {
        if (!this.cancelTarget || !this.selectedDocType) return;
        this.formSubmit = true;
        this.service.cancel(this.selectedDocType.name, this.cancelTarget.transId, this.cancelRemarks).subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Document cancelled successfully.', '');
                    modal.close();
                    this.load();
                } else {
                    this.alertService.error(this.module, 'Cancel', res?.failureMessage || '');
                }
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'Cancel', ''); }
        });
    }

    // ── Restore ──────────────────────────────────────────────
    openRestoreModal(rec: any, tpl: TemplateRef<any>): void {
        this.restoreTarget = rec;
        this.modalService.open(tpl, { size: 'md', centered: true });
    }

    confirmRestore(modal: any): void {
        if (!this.restoreTarget || !this.selectedDocType) return;
        this.formSubmit = true;
        this.service.restore(this.selectedDocType.name, this.restoreTarget.transId).subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Document restored successfully.', '');
                    modal.close();
                    this.load();
                } else {
                    this.alertService.error(this.module, 'Restore', res?.failureMessage || '');
                }
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'Restore', ''); }
        });
    }

    // ── Remarks details ──────────────────────────────────────
    openRemarksModal(rec: any, tpl: TemplateRef<any>): void {
        this.remarksTarget = null;
        this.service.getCancellationDetails(rec.transId).subscribe({
            next: (data) => {
                this.remarksTarget = data;
                this.modalService.open(tpl, { size: 'md', centered: true });
            },
            error: () => { this.alertService.error(this.module, 'Load remarks', ''); }
        });
    }

    isCancelled(rec: any): boolean {
        return (rec?.status || '').toLowerCase().includes('cancel');
    }
}
