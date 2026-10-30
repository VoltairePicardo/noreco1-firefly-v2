import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FormsModule } from '@angular/forms';
import { FlatpickrDefaults, FlatpickrModule } from 'angularx-flatpickr';
import { provideIcons } from '@ng-icons/core';
import { tablerPrinter, tablerEdit, tablerArrowLeft, tablerPhoto, tablerFile, tablerCheck, tablerEye, tablerEyeOff, tablerChevronDown, tablerChevronUp, tablerSearch } from '@ng-icons/tabler-icons';
import { AlertService } from '@/app/shared/services/alert.service';
import { forkJoin } from 'rxjs';
import { PurchaseOrderService } from '../purchase-order.service';
import { SharedModule } from '@/app/shared/shared.module';
import { DocumentLogsComponent } from '@/app/shared/components/document-logs/document-logs.component';
import { AttachmentsComponent } from '@/app/shared/components/attachments/attachments.component';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseBudgetLineItemModalComponent } from '@/app/shared/modals/browse-budget-line-item-modal/browse-budget-line-item-modal.component';

const TERMINAL_STATUSES = ['Approved', 'Denied', 'Cancelled'];

@Component({
    selector: 'app-purchase-order-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, SharedModule, FormsModule, FlatpickrModule, RouterLink, DocumentLogsComponent, AttachmentsComponent],
    providers: [...SHARED_PROVIDERS, FlatpickrDefaults, provideIcons({ tablerPrinter, tablerEdit, tablerArrowLeft, tablerPhoto, tablerFile, tablerCheck, tablerEye, tablerEyeOff, tablerChevronDown, tablerChevronUp, tablerSearch })],
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

    showLogs = false;

    // Budget line item assignment (forAddingBudgetDetail workflow step)
    budgetLineItems       : any[] = [];
    budgetLineItemsLoaded = false;
    selectedBudgetLineItem: any   = null;
    savingBudget = false;

    private modalService = inject(ModalService);
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
                if (header?.id) {
                    this.data      = header;
                    this.lineItems = details || [];
                    this.showLogs  = false;
                    this.selectedBudgetLineItem = null;
                    if (header.forAddingBudgetDetail && !this.budgetLineItemsLoaded) {
                        this.service.getBudgetLineItems().subscribe({
                            next: (items) => { this.budgetLineItems = items || []; this.budgetLineItemsLoaded = true; },
                            error: () => {}
                        });
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
                this.alertService.error(this.module, 'Error loading purchase order.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    async openBudgetLineItemBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseBudgetLineItemModalComponent,
                { items: this.budgetLineItems },
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.selectedBudgetLineItem = result.data;
            }
        } catch { }
    }

    saveBudgetAssignment(): void {
        if (!this.selectedBudgetLineItem?.id) {
            this.alertService.warning(this.module, 'Validation', 'Please select a Budget Line Item.');
            return;
        }
        this.savingBudget = true;
        this.service.assignBudgetLineItem({
            transactionId:          this.data.transId,
            budgetLineItemDetailId: this.selectedBudgetLineItem.id
        }).subscribe({
            next: (res) => {
                this.savingBudget = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Budget line item assigned.', '');
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

    loadWorkflowActions(): void {
        if (!this.data?.transId || this.isTerminal()) {
            this.isLoading.set(false);
            this.workflowActions = [];
            this.selectedAction  = null;
            return;
        }
        this.service.getWorkflowActions(this.data.transId).subscribe({
            next: (actions) => { this.isLoading.set(false); this.workflowActions = actions || []; this.selectedAction = null; this.remarks = ''; },
            error: () => { this.isLoading.set(false); this.workflowActions = []; }
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
    }

    // ─── Attachments ────────────────────────────────────────────────────────

    loadAttachments(): void {
        if (!this.data?.id) return;
        this.service.getFiles(this.data.id).subscribe({
            next:  (files) => { this.attachments = files || []; },
            error: () => { this.attachments = []; }
        });
    }

    fileUrl = (fileId: number): string => this.service.fileUrl(fileId);

    print(): void {
        this.service.print(this.data.id);
    }

    get lineTotal(): number {
        return this.lineItems.reduce((sum: number, li: any) => sum + (li.itemAmount || li.amount || 0), 0);
    }
}
