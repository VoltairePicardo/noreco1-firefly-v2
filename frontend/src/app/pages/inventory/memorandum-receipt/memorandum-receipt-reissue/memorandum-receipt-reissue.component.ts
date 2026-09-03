import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { MemorandumReceiptService } from '../memorandum-receipt.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseReturnedMrModalComponent } from '@/app/shared/modals/browse-returned-mr-modal/browse-returned-mr-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerTrash, tablerArrowLeft, tablerCheck } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-memorandum-receipt-reissue',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective,
        RouterLink
    ],
    providers: [
        provideFlatpickrDefaults(),
        ...SHARED_PROVIDERS,
        provideIcons({ tablerSearch, tablerTrash, tablerArrowLeft, tablerCheck })
    ],
    templateUrl: './memorandum-receipt-reissue.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class MemorandumReceiptReissueComponent implements OnInit {
    module    = 'Memorandum Receipt';
    subModule = 'Re-issue Returned MR';
    menuLink  = 'memorandum-receipt';

    isLoading = signal(false);
    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    date                   = '';
    employee               = signal<any>(null);
    selectedReturnedMr     = signal<any>(null);
    selectedReturnedMrCode = signal('');
    returnMrItems          = signal<any[]>([]);
    reassignedItems        = signal<any[]>([]);
    approvingOfficer       = signal<any>(null);

    private service      = inject(MemorandumReceiptService);
    private modalService = inject(ModalService);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.date = new Date().toISOString().substring(0, 10);
        this.loadDefaultSignatories();
    }

    private loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (data) => { if (data?.approvedBy) this.approvingOfficer.set(data.approvedBy); },
            error: () => {}
        });
    }

    async openEmployeeBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.employee.set(result.data);
                this.selectedReturnedMr.set(null);
                this.selectedReturnedMrCode.set('');
                this.returnMrItems.set([]);
                this.reassignedItems.set([]);
            }
        } catch { }
    }

    async openReturnedMrBrowse(): Promise<void> {
        const employee = this.employee();
        if (!employee?.accountNo) {
            this.alertService.warning(this.module, 'Validation', 'Please select an employee first.');
            return;
        }
        try {
            const result = await this.modalService.openModal(
                BrowseReturnedMrModalComponent,
                { employeeAccountNo: employee.accountNo },
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const doc = result.data;
                this.selectedReturnedMr.set(doc);
                this.selectedReturnedMrCode.set(doc.code || '');
                this.returnMrItems.set((doc.memorandumReceiptDetails || [])
                    .map((d: any) => {
                        const returnedQty   = d.returnedQuantity || d.quantity || 0;
                        const alreadyReissued = d.reassignedQuantity || 0;
                        const maxReassign   = returnedQty - alreadyReissued;
                        return {
                            stockWithdrawalDetail: d.stockWithdrawalDetail,
                            itemCode:              d.stockWithdrawalDetail?.item?.code || d.itemCode || '',
                            itemDescription:       d.stockWithdrawalDetail?.item?.description || d.itemDescription || '',
                            unitCode:              d.stockWithdrawalDetail?.item?.unit?.code || d.unitCode || '',
                            returnedQuantity:      returnedQty,
                            reassignedQuantity:    alreadyReissued,
                            maxReassignQty:        maxReassign,
                            assigned:              false
                        };
                    })
                    .filter((item: any) => item.maxReassignQty > 0));
                this.reassignedItems.set([]);
            }
        } catch { }
    }

    assignItem(item: any): void {
        item.assigned = true;
        this.returnMrItems.update(list => [...list]);
        this.reassignedItems.update(list => [...list, {
            stockWithdrawalDetail: item.stockWithdrawalDetail,
            itemCode:              item.itemCode,
            itemDescription:       item.itemDescription,
            unitCode:              item.unitCode,
            returnedQuantity:      item.returnedQuantity,
            reassignedQuantity:    item.maxReassignQty,
            maxReassignQty:        item.maxReassignQty
        }]);
    }

    removeItem(idx: number, swDetailId: number): void {
        this.reassignedItems.update(list => list.filter((_, i) => i !== idx));
        const item = this.returnMrItems().find(r => r.stockWithdrawalDetail?.id === swDetailId);
        if (item) {
            item.assigned = false;
            this.returnMrItems.update(list => [...list]);
        }
    }

    onReassignQtyChange(detail: any, value: string): void {
        let qty = parseFloat(value) || 0;
        if (qty <= 0) qty = 1;
        if (qty > detail.maxReassignQty) qty = detail.maxReassignQty;
        detail.reassignedQuantity = qty;
        this.reassignedItems.update(list => [...list]);
    }

    async openApprovingOfficerBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const e = result.data;
                this.approvingOfficer.set({ accountNo: e.accountNo, fullName: e.fullName || e.name || '' });
            }
        } catch { }
    }

    save(): void {
        const employee = this.employee();
        const selectedReturnedMr = this.selectedReturnedMr();
        const reassignedItems = this.reassignedItems();
        const approvingOfficer = this.approvingOfficer();

        if (!this.date)
            { this.alertService.warning(this.module, 'Validation', 'Date is required.'); return; }
        if (!employee?.accountNo)
            { this.alertService.warning(this.module, 'Validation', 'Employee is required.'); return; }
        if (!selectedReturnedMr?.id)
            { this.alertService.warning(this.module, 'Validation', 'Returned MR is required.'); return; }
        if (reassignedItems.length === 0)
            { this.alertService.warning(this.module, 'Validation', 'Please assign at least one item.'); return; }
        if (!approvingOfficer?.accountNo)
            { this.alertService.warning(this.module, 'Validation', 'Noted By is required.'); return; }

        this.isLoading.set(true);
        const payload = {
            date:                     this.date,
            employee:                 { accountNo: employee.accountNo, name: employee.name || employee.fullName || '' },
            approvingOfficer:         { accountNo: approvingOfficer.accountNo, fullName: approvingOfficer.fullName || approvingOfficer.name || '' },
            returnMemorandumReceipt:  { id: selectedReturnedMr.id },
            memorandumReceiptDetails: reassignedItems.map(d => ({
                stockWithdrawalDetail: { id: d.stockWithdrawalDetail?.id },
                quantity:              d.returnedQuantity,
                returnedQuantity:      d.returnedQuantity,
                reassignedQuantity:    d.reassignedQuantity
            }))
        };

        this.service.createReturnedMr(payload).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.success === false) {
                    this.alertService.error(this.module, 'Save', data.failureMessage || '');
                } else {
                    this.alertService.success(this.module, 'Re-issued successfully.', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Save', 'An error occurred.');
            }
        });
    }
}
