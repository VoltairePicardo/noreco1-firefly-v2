import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { ItemTestingService } from '../item-testing.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowsePurchaseOrderForTestingModalComponent } from '@/app/shared/modals/browse-purchase-order-for-testing-modal/browse-purchase-order-for-testing-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerTrash, tablerArrowLeft, tablerCheck } from '@ng-icons/tabler-icons';
import { ItemTestingDetailRow, ItemTestingDto, PoDetailForTesting, PurchaseOrderSummary, Supplier } from '@/app/models/inventory-modules/item-testing.model';
import { PurchaseOrder } from '@/app/models/inventory-modules/purchase-order.model';
import {InventoryLocationService} from '@/app/pages/inventory-location/inventory-location.service';
import {InventoryLocation} from '@/app/models/shared/reference.model';

interface ItemTestingPayload {
    id?: number;
    date: string;
    inventoryLocation: { id: number } | null;
    purchaseOrder: { id: number };
    supplier: Supplier | null;
    itemTestingDetails: {
        id: number | null;
        item: { id: number } | null;
        itemDescription: string;
        quantity: number;
        unitCode: string;
        deliveredQuantity: number;
        quantityReceived: number;
        poDetail: { id: number } | null;
        unitsReceivedQuantity: number;
        remarks: string;
    }[];
}

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
    readonly module   = 'Item Testing';
    readonly menuLink = 'item-testing';
    subModule = 'Create';

    id: number | null = null;
    editMode  = false;
    isLoading = signal(false);

    readonly flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    deliveryDate = '';

    inventoryLocations        = signal<InventoryLocation[]>([]);
    selectedInventoryLocation = signal<InventoryLocation | null>(null);

    selectedPO = signal<PurchaseOrderSummary | null>(null);
    poDesc     = signal('');

    supplierFromPO = signal<Supplier | null>(null);

    itemTestingDetails = signal<ItemTestingDetailRow[]>([]);

    totals = computed(() => {
        let qty = 0, qtyReceived = 0;
        for (const item of this.itemTestingDetails()) {
            qty         += Number(item.quantity)         || 0;
            qtyReceived += Number(item.quantityReceived) || 0;
        }
        return { qty, qtyReceived };
    });

    private service      = inject(ItemTestingService);
    private inventoryLocationService      = inject(InventoryLocationService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.inventoryLocationService.getAllLocations().subscribe({
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
        if (this.id == null) return;
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data: ItemTestingDto) => {
                this.isLoading.set(false);
                if (!data?.id) {
                    this.alertService.error(this.module, 'Record not found.', '');
                    this.router.navigate(['/' + this.menuLink]);
                    return;
                }

                this.deliveryDate = data.date ? new Date(data.date).toISOString().substring(0, 10) : '';

                if (data.inventoryLocation?.id) {
                    const found = this.inventoryLocations().find(l => l.id === data.inventoryLocation!.id);
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
                BrowsePurchaseOrderForTestingModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const po = result.data as PurchaseOrder;
                const summary: PurchaseOrderSummary = { id: po.id, code: po.code, vendor: po.vendor };
                this.selectedPO.set(summary);
                this.poDesc.set((po.code || '') + ' : ' + (po.vendor?.name || ''));
                this.supplierFromPO.set({
                    accountNumber: po.vendor?.accountNo,
                    name:          po.vendor?.name || ''
                });
                this.itemTestingDetails.set([]);

                this.service.getPurchaseOrderDetailsForItemTesting(po.id).subscribe({
                    next: (items: PoDetailForTesting[]) => {
                        const rows = (items || []).map((poDetail): ItemTestingDetailRow => {
                            const row: ItemTestingDetailRow = {
                                item:                  poDetail.itemId != null ? { id: poDetail.itemId } : undefined,
                                itemDescription:       poDetail.itemDescription || '',
                                quantity:              poDetail.quantity ?? 0,
                                unitCode:              poDetail.unitCode || '',
                                deliveredQuantity:     poDetail.sentForTestingQuantity ?? 0,
                                quantityReceived:      poDetail.deliveredQuantity ?? 0,
                                poDetail:              { id: poDetail.id },
                                unitsReceivedQuantity: poDetail.deliveredQuantity ?? 0,
                                remarks:               ''
                            };
                            this.initQuantityReceived(row);
                            return row;
                        });
                        this.itemTestingDetails.set(rows);
                    },
                    error: () => this.alertService.error(this.module, 'Failed to load PO details.', '')
                });
            }
        } catch { }
    }

    private initQuantityReceived(item: ItemTestingDetailRow): void {
        if (!(item.quantityReceived || item.quantityReceived > 0)) {
            item.quantityReceived = item.quantity - item.deliveredQuantity;
        }
        if (!(item.unitsReceivedQuantity || item.unitsReceivedQuantity > 0)) {
            item.unitsReceivedQuantity = item.quantityReceived;
        }
    }

    unitsReceivedQuantityChanged(index: number): void {
        const item = this.itemTestingDetails()[index];
        if (item.unitsReceivedQuantity < item.quantityReceived) {
            item.quantityReceived = item.unitsReceivedQuantity;
        }
        this.itemTestingDetails.update(list => [...list]);
    }

    updateNetAmount(index: number): void {
        void index;
        this.itemTestingDetails.update(list => [...list]);
    }

    removeRow(index: number): void {
        this.itemTestingDetails.update(list => list.filter((_, i) => i !== index));
    }

    compareById(a: { id?: unknown } | null, b: { id?: unknown } | null): boolean {
        return a && b ? a.id === b.id : a === b;
    }

    save(): void {
        if (!this.deliveryDate) {
            this.alertService.warning(this.module, 'Validation', 'Delivery Date is required.');
            return;
        }
        const selectedPO = this.selectedPO();
        if (!selectedPO) {
            this.alertService.warning(this.module, 'Validation', 'Please select a Purchase Order.');
            return;
        }
        if (this.itemTestingDetails().length === 0) {
            this.alertService.warning(this.module, 'Validation', 'Please add at least one item.');
            return;
        }

        this.isLoading.set(true);

        const selectedInventoryLocation = this.selectedInventoryLocation();

        const payload: ItemTestingPayload = {
            date: this.deliveryDate,
            inventoryLocation: selectedInventoryLocation?.id
                ? { id: selectedInventoryLocation.id }
                : null,
            purchaseOrder: { id: selectedPO.id },
            itemTestingDetails: this.itemTestingDetails().map(item => ({
                id:                    item.id ?? null,
                item:                  item.item ?? null,
                itemDescription:       item.itemDescription || '',
                quantity:              Number(item.quantity) || 0,
                unitCode:              item.unitCode || '',
                deliveredQuantity:     Number(item.deliveredQuantity) || 0,
                quantityReceived:      Number(item.quantityReceived) || 0,
                poDetail:              item.poDetail ?? null,
                unitsReceivedQuantity: Number(item.unitsReceivedQuantity) || 0,
                remarks:               item.remarks || ''
            })),
            supplier: this.supplierFromPO()
        };

        if (this.editMode && this.id != null) payload.id = this.id;

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
