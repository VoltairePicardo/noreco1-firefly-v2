import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FormsModule } from '@angular/forms';
import { FlatpickrDefaults, FlatpickrModule } from 'angularx-flatpickr';
import { provideIcons } from '@ng-icons/core';
import { tablerPrinter, tablerEdit, tablerArrowLeft, tablerPhoto, tablerFile, tablerCheck, tablerEye, tablerEyeOff } from '@ng-icons/tabler-icons';
import { AlertService } from '@/app/shared/services/alert.service';
import { forkJoin } from 'rxjs';
import { PurchaseOrderService } from '../purchase-order.service';
import { SharedModule } from '@/app/shared/shared.module';

const TERMINAL_STATUSES = ['Approved', 'Denied', 'Cancelled'];

@Component({
    selector: 'app-purchase-order-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, SharedModule, FormsModule, FlatpickrModule, RouterLink],
    providers: [...SHARED_PROVIDERS, FlatpickrDefaults, provideIcons({ tablerPrinter, tablerEdit, tablerArrowLeft, tablerPhoto, tablerFile, tablerCheck, tablerEye, tablerEyeOff })],
    templateUrl: './purchase-order-detail.component.html'
})
export class PurchaseOrderDetailComponent {
    module    = 'Purchase Order';
    subModule = 'Details';
    menuLink  = 'purchase-order';

    id: any   = 0;
    data: any = {};
    lineItems: any[] = [];
    isLoading = signal(false);

    // Attachments
    attachments: any[] = [];

    workflowActions:   any[] = [];
    selectedAction:    any   = null;
    remarks            = '';
    processingWorkflow = false;

    // Supplier received
    showSupplierReceived = false;
    receivedDate  = '';
    receivedBy    = '';
    expectedDeliveryDate = '';
    processingReceived   = false;
    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    logs        : any[] = [];
    showLogs    = false;
    logsLoading = false;

    private service      = inject(PurchaseOrderService);
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
        forkJoin({
            header:  this.service.getData(this.id),
            details: this.service.getDetails(this.id)
        }).subscribe({
            next: ({ header, details }) => {
                this.isLoading.set(false);
                if (header?.id) {
                    this.data      = header;
                    this.lineItems = details || [];
                    this.logs      = [];
                    this.showLogs  = false;
                    this.loadWorkflowActions();
                    this.loadAttachments();
                } else {
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Error loading purchase order.', '');
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

    isApproved(): boolean {
        return this.data?.documentStatus?.status === 'Approved';
    }

    processWorkflow(): void {
        if (!this.selectedAction) return;
        this.processingWorkflow = true;

        const payload = {
            documentId:         this.data.id,
            remarks:            this.remarks,
            workflowActionsDto: { actionMapId: this.selectedAction.actionMapId }
        };

        this.service.process(payload).subscribe({
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

    saveSupplierReceived(): void {
        if (!this.receivedDate || !this.receivedBy) {
            this.alertService.warning(this.module, 'Please fill in received date and received by.', '');
            return;
        }
        this.processingReceived = true;

        const payload = {
            id:                   this.data.id,
            receivedDate:         this.receivedDate,
            receivedBy:           this.receivedBy,
            expectedDeliveryDate: this.expectedDeliveryDate || null,
        };

        this.service.supplierReceived(payload).subscribe({
            next: (res) => {
                this.processingReceived = false;
                if (res?.success) {
                    this.alertService.success(this.module, res.successMessage || 'Saved.', '');
                    this.showSupplierReceived = false;
                    this.loadData();
                } else {
                    this.alertService.error(this.module, res?.failureMessage || 'Failed.', '');
                }
            },
            error: () => {
                this.processingReceived = false;
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

    isImage(file: any): boolean {
        return /\.(jpg|jpeg|png)$/i.test(file.originalFilename || '') || (file.mimeType || '').startsWith('image/');
    }

    fileUrl(fileId: number): string {
        return this.service.fileUrl(fileId);
    }

    print(): void {
        this.service.print(this.data.id);
    }

    get lineTotal(): number {
        return this.lineItems.reduce((sum: number, li: any) => sum + (li.itemAmount || li.amount || 0), 0);
    }
}
