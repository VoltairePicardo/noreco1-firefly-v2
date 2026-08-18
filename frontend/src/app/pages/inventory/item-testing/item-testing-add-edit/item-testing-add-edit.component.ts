import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { ItemTestingService } from '../item-testing.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowsePurchaseOrderModalComponent } from '@/app/shared/modals/browse-purchase-order-modal/browse-purchase-order-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerTrash, tablerArrowLeft, tablerCheck } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-item-testing-add-edit',
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
    templateUrl: './item-testing-add-edit.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ItemTestingAddEditComponent implements OnInit {
    module    = 'Item Testing';
    subModule = 'Create';
    menuLink  = 'item-testing';

    id: any   = null;
    editMode  = false;
    isLoading = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    deliveryDate = '';

    inventoryLocations = signal<any[]>([]);
    selectedInventoryLocation = signal<any>(null);

    selectedPO = signal<any>(null);
    poDesc = signal('');

    supplierFromPO = signal<any>(null);

    itemTestingDetails = signal<any[]>([]);

    totals = computed(() => {
        let qty = 0, qtyReceived = 0;
        for (const item of this.itemTestingDetails()) {
            qty         += Number(item.quantity)         || 0;
            qtyReceived += Number(item.quantityReceived) || 0;
        }
        return { qty, qtyReceived };
    });

    private service      = inject(ItemTestingService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.service.getInventoryLocations().subscribe({
            next: (locs) => this.inventoryLocations.set(locs || []),
            error: () => {}
        });

        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.editMode = idParam != null && /^\d+$/.test(idParam);
            if (this.editMode) {
                this.id = Number(idParam);
                this.subModule = 'Edit';
                this.loadForEdit();
            } else {
                this.subModule = 'Create';
                this.deliveryDate = new Date().toISOString().substring(0, 10);
            }
        });
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (!data?.id) {
                    this.alertService.error(this.module, 'Record not found.', '');
                    this.router.navigate(['/' + this.menuLink]);
                    return;
                }

                this.deliveryDate = data.date ? new Date(data.date).toISOString().substring(0, 10) : '';

                if (data.inventoryLocation?.id) {
                    const found = this.inventoryLocations().find(l => l.id === data.inventoryLocation.id);
                    this.selectedInventoryLocation.set(found ?? data.inventoryLocation);
                }

                if (data.purchaseOrder?.id) {
                    this.selectedPO.set(data.purchaseOrder);
                    this.poDesc.set((data.purchaseOrder.code || data.purchaseOrder.localCode || '') +
                        ' : ' + (data.purchaseOrder.vendor?.name || ''));
                }

                if (data.supplier) {
                    this.supplierFromPO.set(data.supplier);
                }

                this.itemTestingDetails.set(data.itemTestingDetails || []);
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load record.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    async openPOBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowsePurchaseOrderModalComponent, {}, { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const po = result.data;
                this.selectedPO.set(po);
                this.poDesc.set((po.localCode || po.code || '') + ' : ' + (po.vendor?.name || po.supplier || ''));
                this.supplierFromPO.set({
                    accountNumber: po.vendor?.accountNo || '',
                    name: po.vendor?.name || po.supplier || ''
                });
                this.itemTestingDetails.set([]);

                this.service.getPurchaseOrderDetailsForItemTesting(po.id).subscribe({
                    next: (items) => {
                        const rows: any[] = [];
                        if (items && items.length > 0) {
                            for (const poDetail of items) {
                                const row: any = {
                                    item: { id: poDetail.itemId },
                                    itemDescription: poDetail.itemDescription,
                                    quantity: poDetail.quantity,
                                    unitCode: poDetail.unitCode,
                                    unitPrice: poDetail.unitPrice,
                                    itemAmount: poDetail.itemAmount,
                                    deliveredQuantity: poDetail.sentForTestingQuantity,
                                    quantityReceived: poDetail.deliveredQuantity,
                                    netAmount: poDetail.netAmount,
                                    poDetail: { id: poDetail.id },
                                    quantityOrdered: poDetail.quantity,
                                    amount: poDetail.itemAmount,
                                    unitsReceivedQuantity: poDetail.deliveredQuantity,
                                    remarks: ''
                                };
                                this.initQuantityReceived(row);
                                rows.push(row);
                            }
                        }
                        this.itemTestingDetails.set(rows);
                    },
                    error: () => this.alertService.error(this.module, 'Failed to load PO details.', '')
                });
            }
        } catch { }
    }

    initQuantityReceived(item: any): void {
        if (!(item.quantityReceived || item.quantityReceived > 0)) {
            item.quantityReceived = item.quantity - item.deliveredQuantity;
        }
        if (!(item.unitsReceivedQuantity || item.unitsReceivedQuantity > 0)) {
            item.unitsReceivedQuantity = item.quantityReceived;
        }
        item.quantityReceivedStatic = item.quantityReceived;
        item.unitsReceivedQuantityStatic = item.unitsReceivedQuantity;
    }

    unitsReceivedQuantityChanged(index: number): void {
        const item = this.itemTestingDetails()[index];
        if (item.unitsReceivedQuantity < item.quantityReceived) {
            item.quantityReceived = item.unitsReceivedQuantity;
        }
        item.netAmount  = (Number(item.quantityReceived) || 0) * (Number(item.unitPrice) || 0);
        item.itemAmount = item.netAmount;
        this.itemTestingDetails.update(list => [...list]);
    }

    updateNetAmount(index: number): void {
        const item = this.itemTestingDetails()[index];
        item.netAmount  = (Number(item.quantityReceived) || 0) * (Number(item.unitPrice) || 0);
        item.itemAmount = item.netAmount;
        this.itemTestingDetails.update(list => [...list]);
    }

    removeRow(index: number): void {
        this.itemTestingDetails.update(list => list.filter((_, i) => i !== index));
    }

    compareById(a: any, b: any): boolean {
        return a && b ? a.id === b.id : a === b;
    }

    save(): void {
        if (!this.deliveryDate) {
            this.alertService.warning(this.module, 'Validation', 'Delivery Date is required.');
            return;
        }
        if (!this.selectedPO()) {
            this.alertService.warning(this.module, 'Validation', 'Please select a Purchase Order.');
            return;
        }
        if (this.itemTestingDetails().length === 0) {
            this.alertService.warning(this.module, 'Validation', 'Please add at least one item.');
            return;
        }

        this.isLoading.set(true);

        const selectedInventoryLocation = this.selectedInventoryLocation();
        const selectedPO = this.selectedPO();

        const payload: any = {
            date: this.deliveryDate,
            inventoryLocation: selectedInventoryLocation?.id
                ? { id: selectedInventoryLocation.id }
                : null,
            purchaseOrder: { id: selectedPO.id },
            itemTestingDetails: this.itemTestingDetails().map(item => ({
                id:                   item.id                  || null,
                item:                 item.item                || null,
                itemDescription:      item.itemDescription     || '',
                quantity:             Number(item.quantity)             || 0,
                unitCode:             item.unitCode             || '',
                unitPrice:            Number(item.unitPrice)            || 0,
                itemAmount:           Number(item.itemAmount)           || 0,
                deliveredQuantity:    Number(item.deliveredQuantity)    || 0,
                quantityReceived:     Number(item.quantityReceived)     || 0,
                netAmount:            Number(item.netAmount)            || 0,
                poDetail:             item.poDetail             || null,
                quantityOrdered:      Number(item.quantityOrdered)      || 0,
                amount:               Number(item.amount)               || 0,
                unitsReceivedQuantity: Number(item.unitsReceivedQuantity) || 0,
                remarks:              item.remarks              || ''
            })),
            supplier: this.supplierFromPO() || null
        };

        if (this.editMode) payload.id = this.id;

        const request$ = this.editMode
            ? this.service.update(payload)
            : this.service.create(payload);

        request$.subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.success === false) {
                    this.alertService.error(this.module, 'Save', data.failureMessage || '');
                } else {
                    this.alertService.success(
                        this.module,
                        this.editMode ? 'Updated successfully.' : 'Created successfully.',
                        ''
                    );
                    const id = data?.modelId ?? data?.id ?? this.id;
                    this.router.navigate(['/' + this.menuLink, id, 'detail']);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Save', 'An error occurred.');
            }
        });
    }
}
