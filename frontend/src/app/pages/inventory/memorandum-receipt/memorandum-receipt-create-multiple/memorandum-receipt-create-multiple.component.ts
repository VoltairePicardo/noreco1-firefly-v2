import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { MemorandumReceiptService } from '../memorandum-receipt.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseMrStockWithdrawalModalComponent } from '@/app/shared/modals/browse-mr-stock-withdrawal-modal/browse-mr-stock-withdrawal-modal.component';
import { BrowseMrEmployeeItemsModalComponent } from '@/app/shared/modals/browse-mr-employee-items-modal/browse-mr-employee-items-modal.component';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerArrowLeft, tablerCheck } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-memorandum-receipt-create-multiple',
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
        provideIcons({ tablerSearch, tablerArrowLeft, tablerCheck })
    ],
    templateUrl: './memorandum-receipt-create-multiple.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class MemorandumReceiptCreateMultipleComponent implements OnInit {
    module    = 'Memorandum Receipt';
    subModule = 'Create Multiple Employee MR';
    menuLink  = 'memorandum-receipt';

    isLoading = signal(false);
    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    date             = '';
    selectedSW       = signal<any>(null);
    selectedSWCode   = signal('');
    availableItems   = signal<any[]>([]);
    employees        = signal<any[]>([]);
    multipleMR       = signal<{ employee: any; assignedItems: any[] }[]>([]);
    approvingOfficer = signal<any>(null);

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

    async openStockWithdrawalBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseMrStockWithdrawalModalComponent, {}, { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const doc = result.data;
                this.selectedSW.set(doc);
                this.selectedSWCode.set(doc.code || '');
                this.multipleMR.set([]);
                this.availableItems.set((doc.items || doc.details || []).map((item: any) => ({
                    stockWithdrawalDetail: item,
                    itemCode:              item.item?.code || item.itemCode || '',
                    itemDescription:       item.item?.description || item.itemDescription || '',
                    unitCode:              item.item?.unit?.code || item.unitCode || '',
                    quantity:              item.quantity,
                    remaining:             item.quantity,
                    oldRemainingBalance:   item.quantity
                })));
                this.loadBalances();
                this.loadEmployees(doc.id);
            }
        } catch { }
    }

    private loadBalances(): void {
        this.availableItems().forEach(item => {
            const detailId = item.stockWithdrawalDetail?.id;
            if (!detailId) return;
            this.service.getStockWithdrawalBalance(detailId).subscribe({
                next: (data) => {
                    const assigned = data[0]?.assigned ?? 0;
                    item.remaining           = item.quantity - assigned;
                    item.oldRemainingBalance = item.remaining;
                    this.availableItems.update(list => [...list]);
                },
                error: () => {}
            });
        });
    }

    private loadEmployees(swId: number): void {
        this.service.getStockWithdrawalEmployees(swId).subscribe({
            next: (employees) => {
                this.employees.set(employees || []);
                this.multipleMR.set(this.employees().map(emp => ({ employee: emp, assignedItems: [] })));
            },
            error: () => { this.employees.set([]); this.multipleMR.set([]); }
        });
    }

    async openEmployeeItemsModal(entry: { employee: any; assignedItems: any[] }): Promise<void> {
        const multipleMR = this.multipleMR();
        const itemsForModal = this.availableItems().map(item => {
            const detailId = item.stockWithdrawalDetail?.id;
            const alreadyAssignedToOthers = multipleMR
                .filter(e => e.employee.accountNo !== entry.employee.accountNo)
                .reduce((sum, e) => {
                    const found = e.assignedItems.find((a: any) => a.stockWithdrawalDetail?.id === detailId);
                    return sum + (found?.quantity || 0);
                }, 0);
            return { ...item, remaining: item.remaining - alreadyAssignedToOthers };
        }).filter(i => i.remaining > 0);

        try {
            const result = await this.modalService.openModal(
                BrowseMrEmployeeItemsModalComponent,
                { employee: entry.employee, items: itemsForModal },
                { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                entry.assignedItems = result.data;
                this.multipleMR.update(list => [...list]);
            }
        } catch { }
    }

    hasEmployeeAssignment(entry: { employee: any; assignedItems: any[] }): boolean {
        return entry.assignedItems.length > 0;
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
        const selectedSW = this.selectedSW();
        const approvingOfficer = this.approvingOfficer();
        const availableItems = this.availableItems();
        const multipleMR = this.multipleMR();

        if (!this.date)
            { this.alertService.warning(this.module, 'Validation', 'Date is required.'); return; }
        if (!selectedSW?.id)
            { this.alertService.warning(this.module, 'Validation', 'Stock Withdrawal is required.'); return; }
        if (!approvingOfficer?.accountNo)
            { this.alertService.warning(this.module, 'Validation', 'Noted By is required.'); return; }

        const unassigned = availableItems.filter(item => {
            const detailId = item.stockWithdrawalDetail?.id;
            const totalQty = multipleMR.reduce((sum, e) => {
                const found = e.assignedItems.find((a: any) => a.stockWithdrawalDetail?.id === detailId);
                return sum + (found?.quantity || 0);
            }, 0);
            return totalQty < item.remaining;
        });
        if (unassigned.length > 0)
            { this.alertService.warning(this.module, 'Validation', 'All items must be fully distributed to employees.'); return; }

        const forms = multipleMR
            .filter(e => e.assignedItems.length > 0)
            .map(e => ({
                date:             this.date,
                stockWithdrawal:  { id: selectedSW.id },
                employee:         { accountNo: e.employee.accountNo, name: e.employee.name || e.employee.fullName || '' },
                approvingOfficer: { accountNo: approvingOfficer.accountNo, fullName: approvingOfficer.fullName || approvingOfficer.name || '' },
                memorandumReceiptDetails: e.assignedItems.map((d: any) => ({
                    stockWithdrawalDetail: { id: d.stockWithdrawalDetail?.id },
                    quantity:              d.quantity,
                    reassignedQuantity:    0
                }))
            }));

        if (forms.length === 0)
            { this.alertService.warning(this.module, 'Validation', 'At least one employee must have items assigned.'); return; }

        this.isLoading.set(true);
        this.service.createMultiple(forms).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.success === false) {
                    this.alertService.error(this.module, 'Save', data.failureMessage || '');
                } else {
                    this.alertService.success(this.module, 'Multiple MRs created successfully.', '');
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
