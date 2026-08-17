import { Component, inject, signal } from '@angular/core';
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
    templateUrl: './memorandum-receipt-add-edit.component.html'
})
export class MemorandumReceiptAddEditComponent {
    module    = 'Memorandum Receipt';
    subModule = 'Create';
    menuLink  = 'memorandum-receipt';

    id: any  = null;
    editMode = false;
    isLoading = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    date              = '';
    selectedSW:   any = null;
    selectedSWCode    = '';
    availableItems: any[] = [];   // items from stock withdrawal with balance
    assignedItems:  any[] = [];   // items being included in this MR
    approvingOfficer: any = null;

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
                if (data?.approvedBy) this.approvingOfficer = data.approvedBy;
            },
            error: () => {}
        });
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (!data?.id) {
                    this.alertService.error(this.module, 'Not found.', '');
                    this.router.navigate(['/' + this.menuLink]);
                    return;
                }
                const s = data.status || '';
                if (s !== 'Document Created' && s !== 'Returned to Creator') {
                    this.alertService.warning(this.module, 'Not Editable', 'This document cannot be edited.');
                    this.router.navigate(['/' + this.menuLink, this.id, 'detail']);
                    return;
                }
                this.date             = data.date ? new Date(data.date).toISOString().substring(0, 10) : '';
                this.approvingOfficer = data.approvingOfficer || null;
                this.selectedSW       = data.stockWithdrawal || null;
                this.selectedSWCode   = data.stockWithdrawal?.code || '';
                this.assignedItems    = (data.memorandumReceiptDetails || []).map((d: any) => ({
                    stockWithdrawalDetail: d.stockWithdrawalDetail,
                    quantity:              d.quantity,
                    reassignedQuantity:    d.reassignedQuantity || 0,
                    oldQuantity:           d.quantity,
                    itemCode:              d.stockWithdrawalDetail?.item?.code || '',
                    itemDescription:       d.stockWithdrawalDetail?.item?.description || '',
                    unitCode:              d.stockWithdrawalDetail?.item?.unit?.code || ''
                }));

                const swItems = data.stockWithdrawal?.items || data.stockWithdrawal?.details || [];
                this.availableItems = swItems.map((item: any) => {
                    const alreadyAssigned = this.assignedItems.some(
                        a => a.stockWithdrawalDetail?.id === item.id
                    );
                    return {
                        stockWithdrawalDetail: item,
                        itemCode:              item.item?.code || '',
                        itemDescription:       item.item?.description || '',
                        unitCode:              item.item?.unit?.code || '',
                        quantity:              item.quantity,
                        quantityReleased:      item.quantityReleased,
                        totalAssigned:         0,
                        remaining:             item.quantity,
                        oldRemainingBalance:   item.quantity,
                        oldTotalAssigned:      0,
                        insufficientBalance:   false,
                        assigned:              alreadyAssigned
                    };
                });
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
        this.availableItems.forEach(d => {
            const detailId = d.stockWithdrawalDetail?.id;
            if (!detailId) return;
            this.service.getStockWithdrawalBalance(detailId).subscribe({
                next: (data) => {
                    d.totalAssigned       = data[0]?.assigned ?? 0;
                    d.remaining           = d.quantity - d.totalAssigned;
                    d.oldRemainingBalance = d.remaining;
                    d.oldTotalAssigned    = d.totalAssigned;
                    d.insufficientBalance = d.remaining <= 0;
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
                const doc = result.data;
                this.selectedSW   = doc;
                this.selectedSWCode = doc.code || '';
                this.assignedItems  = [];
                this.availableItems = (doc.items || doc.details || []).map((item: any) => ({
                    stockWithdrawalDetail: item,
                    itemCode:              item.item?.code || item.itemCode || '',
                    itemDescription:       item.item?.description || item.itemDescription || '',
                    unitCode:              item.item?.unit?.code || item.unitCode || '',
                    quantity:              item.quantity,
                    quantityReleased:      item.quantityReleased,
                    totalAssigned:         0,
                    remaining:             item.quantity,
                    oldRemainingBalance:   item.quantity,
                    oldTotalAssigned:      0,
                    insufficientBalance:   false,
                    assigned:              false
                }));
                this.loadBalances();
            }
        } catch { }
    }

    assignItem(item: any): void {
        item.assigned = true;
        const qty = 1;
        item.remaining    = item.oldRemainingBalance - qty;
        item.totalAssigned = item.oldTotalAssigned + qty;
        this.assignedItems = [...this.assignedItems, {
            stockWithdrawalDetail: item.stockWithdrawalDetail,
            quantity:              qty,
            reassignedQuantity:    0,
            oldQuantity:           0,
            itemCode:              item.itemCode,
            itemDescription:       item.itemDescription,
            unitCode:              item.unitCode,
            _availableRef:         item    // internal reference for quantity tracking
        }];
    }

    removeItem(idx: number, swDetailId: number, quantity: number): void {
        this.assignedItems = this.assignedItems.filter((_, i) => i !== idx);
        const avail = this.availableItems.find(a => a.stockWithdrawalDetail?.id === swDetailId);
        if (avail) {
            avail.assigned     = false;
            avail.remaining    = avail.remaining + quantity;
            avail.totalAssigned = Math.max(0, avail.totalAssigned - quantity);
        }
    }

    onQtyChange(detail: any, value: string): void {
        const avail = this.availableItems.find(
            a => a.stockWithdrawalDetail?.id === detail.stockWithdrawalDetail?.id
        );
        if (!avail) return;

        let qty = parseFloat(value) || 1;
        if (qty <= 0) qty = 1;

        const newQty = qty - detail.oldQuantity;
        const newRemaining = avail.oldRemainingBalance - newQty;
        if (newQty >= avail.oldRemainingBalance) {
            detail.quantity   = detail.oldQuantity + avail.oldRemainingBalance;
            avail.remaining   = 0;
            avail.totalAssigned = avail.quantity;
        } else if (qty <= 0) {
            detail.quantity   = 1;
            avail.remaining   = detail.oldQuantity + avail.oldRemainingBalance - detail.quantity;
        } else {
            detail.quantity   = qty;
            avail.remaining   = newRemaining;
            avail.totalAssigned = avail.oldTotalAssigned + newQty;
        }
    }

    async openApprovingOfficerBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const e = result.data;
                this.approvingOfficer = { accountNo: e.accountNo, fullName: e.fullName || e.name };
            }
        } catch { }
    }

    save(): void {
        if (!this.date)
            { this.alertService.warning(this.module, 'Validation', 'Date is required.'); return; }
        if (!this.selectedSW?.id)
            { this.alertService.warning(this.module, 'Validation', 'Stock Withdrawal is required.'); return; }
        if (this.assignedItems.length === 0)
            { this.alertService.warning(this.module, 'Validation', 'Please assign at least one item.'); return; }
        if (!this.approvingOfficer?.accountNo)
            { this.alertService.warning(this.module, 'Validation', 'Noted By is required.'); return; }

        this.isLoading.set(true);
        const payload: any = {
            date:             this.date,
            stockWithdrawal:  { id: this.selectedSW.id },
            approvingOfficer: { accountNo: this.approvingOfficer.accountNo, fullName: this.approvingOfficer.fullName || this.approvingOfficer.name || '' },
            memorandumReceiptDetails: this.assignedItems.map(d => ({
                stockWithdrawalDetail: { id: d.stockWithdrawalDetail?.id },
                quantity:              d.quantity,
                reassignedQuantity:    0
            }))
        };
        if (this.editMode) payload.id = this.id;

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

    compareById(a: any, b: any): boolean {
        return a && b ? a.id === b.id : a === b;
    }
}
