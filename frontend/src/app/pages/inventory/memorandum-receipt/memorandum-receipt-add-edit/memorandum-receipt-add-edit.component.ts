import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { MemorandumReceiptService } from '../memorandum-receipt.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseMrStockWithdrawalModalComponent } from '@/app/shared/modals/browse-mr-stock-withdrawal-modal/browse-mr-stock-withdrawal-modal.component';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerTrash, tablerArrowLeft, tablerCheck } from '@ng-icons/tabler-icons';
import { StockWithdrawal, StockWithdrawalDetail } from '@/app/models/inventory-modules/stock-withdrawal.model';
import { AssignedItemRow, AvailableItemRow, MemorandumReceiptDto, MemorandumReceiptSource, SlEntity } from '@/app/models/inventory-modules/memorandum-receipt.model';

interface MemorandumReceiptPayload {
    id?: number;
    date: string;
    stockWithdrawal: { id: number };
    approvingOfficer: { accountNo: number; fullName: string };
    memorandumReceiptDetails: { stockWithdrawalDetail: StockWithdrawalDetail; quantity?: number; reassignedQuantity: number }[];
}

@Component({
    selector: 'app-memorandum-receipt-add-edit',
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
    templateUrl: './memorandum-receipt-add-edit.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class MemorandumReceiptAddEditComponent implements OnInit {
    module    = 'Memorandum Receipt';
    subModule = 'Create';
    menuLink  = 'memorandum-receipt';

    id: number | null = null;
    editMode = false;
    isLoading = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    date              = '';
    selectedSW       = signal<MemorandumReceiptSource | null>(null);
    selectedSWCode   = signal('');
    availableItems   = signal<AvailableItemRow[]>([]);
    assignedItems    = signal<AssignedItemRow[]>([]);
    approvingOfficer = signal<SlEntity | null>(null);

    private service      = inject(MemorandumReceiptService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.loadDefaultSignatories();
        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.editMode = idParam != null && /^\d+$/.test(idParam);
            if (this.editMode) {
                this.id = Number(idParam);
                this.subModule = 'Edit';
                this.loadForEdit();
            } else {
                this.subModule = 'Create';
                this.date = new Date().toISOString().substring(0, 10);
            }
        });
    }

    private loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (data) => {
                if (data?.approvedBy) this.approvingOfficer.set(data.approvedBy);
            },
            error: () => {}
        });
    }

    loadForEdit(): void {
        if (this.id == null) return;
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data: MemorandumReceiptDto) => {
                this.isLoading.set(false);
                if (!data?.id) {
                    this.alertService.error(this.module, 'Not found.', '');
                    this.router.navigate(['/' + this.menuLink]);
                    return;
                }
                const s = data.documentStatus?.status || '';
                if (s !== 'Document Created' && s !== 'Returned to Creator') {
                    this.alertService.warning(this.module, 'Not Editable', 'This document cannot be edited.');
                    this.router.navigate(['/' + this.menuLink, this.id, 'detail']);
                    return;
                }
                this.date = data.date ? new Date(data.date).toISOString().substring(0, 10) : '';
                this.approvingOfficer.set(data.approvingOfficer || null);
                this.selectedSW.set(data.stockWithdrawal || null);
                this.selectedSWCode.set(data.stockWithdrawal?.code || '');
                const assignedItems: AssignedItemRow[] = (data.memorandumReceiptDetails || []).map(d => ({
                    stockWithdrawalDetail: d.stockWithdrawalDetail,
                    quantity:              d.quantity ?? 0,
                    reassignedQuantity:    d.reassignedQuantity || 0,
                    oldQuantity:           d.quantity ?? 0,
                    itemCode:              d.stockWithdrawalDetail?.item?.code || '',
                    itemDescription:       d.stockWithdrawalDetail?.item?.description || '',
                    unitCode:              d.stockWithdrawalDetail?.unit?.code || ''
                }));
                this.assignedItems.set(assignedItems);

                const swItems = data.stockWithdrawal?.details || [];
                this.availableItems.set(swItems.map((item): AvailableItemRow => {
                    const alreadyAssigned = assignedItems.some(a => a.stockWithdrawalDetail?.id === item.id);
                    return {
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
                        assigned:              alreadyAssigned
                    };
                }));
                this.loadBalances();
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Error.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    private loadBalances(): void {
        this.availableItems().forEach(d => {
            const detailId = d.stockWithdrawalDetail?.id;
            if (!detailId) return;
            this.service.getStockWithdrawalBalance(detailId).subscribe({
                next: (data) => {
                    d.totalAssigned       = data[0]?.assigned ?? 0;
                    d.remaining           = d.quantity - d.totalAssigned;
                    d.oldRemainingBalance = d.remaining;
                    d.oldTotalAssigned    = d.totalAssigned;
                    d.insufficientBalance = d.remaining <= 0;
                    this.availableItems.update(list => [...list]);
                },
                error: () => {}
            });
        });
    }

    async openStockWithdrawalBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseMrStockWithdrawalModalComponent, {}, { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const doc = result.data as StockWithdrawal;
                this.selectedSW.set(doc);
                this.selectedSWCode.set(doc.code || '');
                this.assignedItems.set([]);
                const swItems = doc.items || [];
                this.availableItems.set(swItems.map((item): AvailableItemRow => ({
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
            }
        } catch { }
    }

    assignItem(item: AvailableItemRow): void {
        item.assigned = true;
        const qty = 1;
        item.remaining     = item.oldRemainingBalance - qty;
        item.totalAssigned = item.oldTotalAssigned + qty;
        this.availableItems.update(list => [...list]);
        this.assignedItems.update(list => [...list, {
            stockWithdrawalDetail: item.stockWithdrawalDetail,
            quantity:              qty,
            reassignedQuantity:    0,
            oldQuantity:           0,
            itemCode:              item.itemCode,
            itemDescription:       item.itemDescription,
            unitCode:              item.unitCode
        }]);
    }

    removeItem(idx: number, swDetailId: number | undefined, quantity: number): void {
        this.assignedItems.update(list => list.filter((_, i) => i !== idx));
        const avail = this.availableItems().find(a => a.stockWithdrawalDetail?.id === swDetailId);
        if (avail) {
            avail.assigned      = false;
            avail.remaining     = avail.remaining + quantity;
            avail.totalAssigned = Math.max(0, avail.totalAssigned - quantity);
            this.availableItems.update(list => [...list]);
        }
    }

    onQtyChange(detail: AssignedItemRow, value: string): void {
        const avail = this.availableItems().find(
            a => a.stockWithdrawalDetail?.id === detail.stockWithdrawalDetail?.id
        );
        if (!avail) return;

        let qty = parseFloat(value) || 1;
        if (qty <= 0) qty = 1;

        const newQty = qty - detail.oldQuantity;
        const newRemaining = avail.oldRemainingBalance - newQty;
        if (newQty >= avail.oldRemainingBalance) {
            detail.quantity     = detail.oldQuantity + avail.oldRemainingBalance;
            avail.remaining     = 0;
            avail.totalAssigned = avail.quantity;
        } else if (qty <= 0) {
            detail.quantity = 1;
            avail.remaining = detail.oldQuantity + avail.oldRemainingBalance - detail.quantity;
        } else {
            detail.quantity     = qty;
            avail.remaining     = newRemaining;
            avail.totalAssigned = avail.oldTotalAssigned + newQty;
        }
        this.assignedItems.update(list => [...list]);
        this.availableItems.update(list => [...list]);
    }

    async openApprovingOfficerBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const e = result.data as SlEntity;
                this.approvingOfficer.set({ accountNo: e.accountNo, fullName: e.fullName || e.name });
            }
        } catch { }
    }

    save(): void {
        const selectedSW = this.selectedSW();
        const assignedItems = this.assignedItems();
        const approvingOfficer = this.approvingOfficer();

        if (!this.date)
            { this.alertService.warning(this.module, 'Validation', 'Date is required.'); return; }
        if (!selectedSW?.id)
            { this.alertService.warning(this.module, 'Validation', 'Stock Withdrawal is required.'); return; }
        if (assignedItems.length === 0)
            { this.alertService.warning(this.module, 'Validation', 'Please assign at least one item.'); return; }
        if (!approvingOfficer?.accountNo)
            { this.alertService.warning(this.module, 'Validation', 'Noted By is required.'); return; }

        this.isLoading.set(true);
        const payload: MemorandumReceiptPayload = {
            date:             this.date,
            stockWithdrawal:  { id: selectedSW.id },
            approvingOfficer: { accountNo: approvingOfficer.accountNo, fullName: approvingOfficer.fullName || approvingOfficer.name || '' },
            memorandumReceiptDetails: assignedItems.map(d => ({
                stockWithdrawalDetail: d.stockWithdrawalDetail!,
                quantity:              d.quantity,
                reassignedQuantity:    0
            }))
        };
        if (this.editMode && this.id != null) payload.id = this.id;

        const req$ = this.editMode ? this.service.update(payload) : this.service.create(payload);
        req$.subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.success === false) {
                    this.alertService.error(this.module, 'Save', data.failureMessage || '');
                } else {
                    this.alertService.success(this.module, this.editMode ? 'Updated successfully.' : 'Created successfully.', '');
                    this.router.navigate(['/' + this.menuLink, data?.modelId ?? data?.id ?? this.id, 'detail']);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Save', 'An error occurred.');
            }
        });
    }
}
