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
import {StockWithdrawal, StockWithdrawalDetail} from '@/app/models/inventory-modules/stock-withdrawal.model';
import { AssignedItemRow, AvailableItemRow, SlEntity } from '@/app/models/inventory-modules/memorandum-receipt.model';

interface MrEmployee {
    accountNo: number;
    name?: string;
    fullName?: string;
}

interface MultipleMrEntry {
    employee: MrEmployee;
    assignedItems: AssignedItemRow[];
}

interface MultipleMrPayload {
    date: string;
    stockWithdrawal: { id: number };
    employee: { accountNo: number; name: string };
    approvingOfficer: { accountNo: number; fullName: string };
    memorandumReceiptDetails: { stockWithdrawalDetail: StockWithdrawalDetail; quantity: number; reassignedQuantity: number }[];
}

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
    readonly module    = 'Memorandum Receipt';
    readonly subModule = 'Create Multiple Employee MR';
    readonly menuLink  = 'memorandum-receipt';
    readonly flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    isLoading = signal(false);

    date             = '';
    selectedSW       = signal<StockWithdrawal | null>(null);
    selectedSWCode   = signal('');
    availableItems   = signal<AvailableItemRow[]>([]);
    employees        = signal<MrEmployee[]>([]);
    multipleMR       = signal<MultipleMrEntry[]>([]);
    approvingOfficer = signal<SlEntity | null>(null);

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
                BrowseMrStockWithdrawalModalComponent, { multipleEmployee: true }, { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const doc = result.data as StockWithdrawal;
                this.selectedSW.set(doc);
                this.selectedSWCode.set(doc.code || '');
                this.multipleMR.set([]);
                this.availableItems.set((doc.items || []).map((item): AvailableItemRow => ({
                    stockWithdrawalDetail: item,
                    itemCode:              item.item?.code || '',
                    itemDescription:       item.item?.description || '',
                    unitCode:              item.unit?.code || '',
                    quantity:              item.quantity ?? 0,
                    quantityReleased:      item.quantityReleased,
                    totalAssigned:         0,
                    remaining:             item.quantity ?? 0,
                    oldRemainingBalance:   item.quantity ?? 0,
                    oldTotalAssigned:      0,
                    insufficientBalance:   false,
                    assigned:              false
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
                    item.totalAssigned       = data[0]?.assigned ?? 0;
                    item.remaining           = item.quantity - item.totalAssigned;
                    item.oldRemainingBalance = item.remaining;
                    item.oldTotalAssigned    = item.totalAssigned;
                    item.insufficientBalance = item.remaining <= 0;
                    this.availableItems.update(list => [...list]);
                },
                error: () => {}
            });
        });
    }

    private loadEmployees(swId: number): void {
        this.service.getStockWithdrawalEmployees(swId).subscribe({
            next: (employees: MrEmployee[]) => {
                this.employees.set(employees || []);
                this.multipleMR.set(this.employees().map(emp => ({ employee: emp, assignedItems: [] })));
            },
            error: () => { this.employees.set([]); this.multipleMR.set([]); }
        });
    }

    async openEmployeeItemsModal(entry: MultipleMrEntry): Promise<void> {
        const multipleMR = this.multipleMR();
        const itemsForModal = this.availableItems().map(item => {
            const detailId = item.stockWithdrawalDetail?.id;
            const alreadyAssignedToOthers = multipleMR
                .filter(e => e.employee.accountNo !== entry.employee.accountNo)
                .reduce((sum, e) => {
                    const found = e.assignedItems.find(a => a.stockWithdrawalDetail?.id === detailId);
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
                const rows = result.data as Omit<AssignedItemRow, 'reassignedQuantity' | 'oldQuantity'>[];
                entry.assignedItems = rows.map((row): AssignedItemRow => ({
                    ...row,
                    reassignedQuantity: 0,
                    oldQuantity:        0
                }));
                this.multipleMR.update(list => [...list]);
            }
        } catch { }
    }

    hasEmployeeAssignment(entry: MultipleMrEntry): boolean {
        return entry.assignedItems.length > 0;
    }

    async openApprovingOfficerBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const e = result.data as SlEntity;
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
                const found = e.assignedItems.find(a => a.stockWithdrawalDetail?.id === detailId);
                return sum + (found?.quantity || 0);
            }, 0);
            return totalQty < item.remaining;
        });
        if (unassigned.length > 0)
            { this.alertService.warning(this.module, 'Validation', 'All items must be fully distributed to employees.'); return; }

        const officer = { accountNo: approvingOfficer.accountNo, fullName: approvingOfficer.fullName || approvingOfficer.name || '' };
        const forms: MultipleMrPayload[] = multipleMR
            .filter(e => e.assignedItems.length > 0)
            .map(e => ({
                date:             this.date,
                stockWithdrawal:  { id: selectedSW.id },
                employee:         { accountNo: e.employee.accountNo, name: e.employee.name || e.employee.fullName || '' },
                approvingOfficer: officer,
                memorandumReceiptDetails: e.assignedItems.map(d => ({
                    stockWithdrawalDetail: d.stockWithdrawalDetail!,
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
