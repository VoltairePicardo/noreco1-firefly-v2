import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { SharedModule } from '@/app/shared/shared.module';
import { provideIcons } from '@ng-icons/core';
import { tablerArrowLeft, tablerPrinter, tablerEdit, tablerCheck, tablerEye, tablerEyeOff, tablerAlertTriangle } from '@ng-icons/tabler-icons';
import { QuotationService } from '../quotation.service';
import { forkJoin } from 'rxjs';

const TERMINAL_STATUSES = ['Approved', 'Denied', 'Cancelled'];

@Component({
    selector: 'app-quotation-detail',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        SharedModule
    ],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerArrowLeft, tablerPrinter, tablerEdit, tablerCheck, tablerEye, tablerEyeOff, tablerAlertTriangle })],
    templateUrl: './quotation-detail.component.html'
})
export class QuotationDetailComponent {
    module    = 'Quotation';
    subModule = 'Detail';
    menuLink  = 'quotation';
    id: any   = 0;

    data: any        = {};
    lineItems: any[] = [];
    isLoading        = signal(false);

    workflowActions:   any[] = [];
    selectedAction:    any   = null;
    remarks            = '';
    processingWorkflow = false;

    logs:       any[] = [];
    showLogs    = false;
    logsLoading = false;

    readonly supplierSlots = [0, 1, 2];

    private service      = inject(QuotationService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.id = params.get('id');
            if (this.id && /^\d+$/.test(String(this.id))) {
                this.loadData();
            } else {
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    loadData(): void {
        this.isLoading.set(true);
        forkJoin({
            header:  this.service.getData(this.id),
            details: this.service.getDetails(this.id)
        }).subscribe({
            next: ({ header, details }: any) => {
                this.isLoading.set(false);
                if (header?.id) {
                    this.data      = header;
                    this.lineItems = details || [];
                    this.logs      = [];
                    this.showLogs  = false;
                    this.loadWorkflowActions();
                } else {
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Error loading quotation.', '');
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
        return TERMINAL_STATUSES.includes(this.data?.documentStatus || '');
    }

    isEditable(): boolean {
        const s = this.data?.documentStatus || '';
        return s === 'Document Created' || s === 'For Revision';
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
            next: (res: any) => {
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
                next: (logs) => {
                    this.logs = (logs || []).map((log: any) => ({
                        ...log,
                        parsed: this.parseLog(log)
                    }));
                    this.logsLoading = false;
                },
                error: () => { this.logsLoading = false; }
            });
        }
    }

    parseLog(log: any): any {
        try {
            const raw = log.newValue || log.oldValue;
            if (!raw) return null;
            return JSON.parse(raw);
        } catch { return null; }
    }

    logSupplierNames(parsed: any): string[] {
        const items: any[] = parsed?.quotationItems || [];
        if (!items.length) return [];
        const details: any[] = items[0]?.details || [];
        return details.map((d: any) => d?.supplier?.name || '').filter((n: string) => n);
    }

    print(): void {
        this.service.print(this.data.id);
    }

    get suppliersList(): any[] {
        return this.data?.suppliers || [];
    }

    get termsList(): any[] {
        return this.data?.terms || [];
    }

    get awardedTotal(): number {
        let total = 0;
        for (const item of this.lineItems) {
            if (item.available === false) continue;
            for (const detail of (item.details || [])) {
                if (detail?.awarded) total += Number(detail.price || 0) * Number(item.quantity || 0);
            }
        }
        return total;
    }

    get awardedSummary(): { supplierName: string; items: { no: number; description: string; unitCode: string; quantity: number; price: number; brand: string; amount: number }[]; total: number }[] {
        const result: { supplierName: string; items: { no: number; description: string; unitCode: string; quantity: number; price: number; brand: string; amount: number }[]; total: number }[] = [];
        this.suppliersList.forEach((sup: any, si: number) => {
            let total = 0;
            const items: { no: number; description: string; unitCode: string; quantity: number; price: number; brand: string; amount: number }[] = [];
            this.lineItems.forEach((item: any, idx: number) => {
                if (item.available === false) return;
                const detail = item.details?.[si];
                if (detail?.awarded && detail?.price) {
                    const amount = Number(detail.price) * Number(item.quantity || 0);
                    total += amount;
                    items.push({
                        no:          idx + 1,
                        description: item.itemDescription || '—',
                        unitCode:    item.unitCode || '—',
                        quantity:    Number(item.quantity || 0),
                        price:       Number(detail.price || 0),
                        brand:       detail.brand?.name || '',
                        amount,
                    });
                }
            });
            if (items.length > 0) {
                result.push({ supplierName: sup.name, items, total });
            }
        });
        return result;
    }

    get notChildItems(): any[] {
        return this.lineItems.filter((item: any) => !item.isChildItem);
    }

    get notApprovedItems(): any[] {
        return this.lineItems.filter((item: any) => item.isChildItem && !item.isApprovedItem);
    }

    get processingNotAllowed(): boolean {
        return this.notChildItems.length > 0 || this.notApprovedItems.length > 0;
    }
}
