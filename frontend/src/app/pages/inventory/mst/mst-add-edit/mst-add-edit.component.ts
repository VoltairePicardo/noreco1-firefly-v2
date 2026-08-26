import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { MstService } from '../mst.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { SelectOnFocusDirective } from '@/app/core/directive/select-on-focus.directive';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerTrash, tablerArrowLeft, tablerCheck, tablerPlus } from '@ng-icons/tabler-icons';
import {InventoryLocation, SpecialEquipment} from '@/app/models/shared/reference.model';
import {InventoryLocationService} from '@/app/pages/inventory-location/inventory-location.service';
import {
    BrowseItemStockModalComponent
} from '@/app/shared/modals/browse-item-stock-modal/browse-item-stock-modal.component';
import {ItemStock} from '@/app/models/inventory-modules/item-stock.model';
import {ItemTransactionDetailDto} from '@/app/models/inventory-modules/stock-release.model';
import {SerialNumbersModalComponent} from '@/app/shared/modals/serial-numbers-modal/serial-numbers-modal.component';

const EMPLOYEE_CLASSIFICATION_ID = 1;

@Component({
    selector: 'app-mst-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective,
        RouterLink,
        SelectOnFocusDirective
    ],
    providers: [
        provideFlatpickrDefaults(),
        ...SHARED_PROVIDERS,
        provideIcons({ tablerSearch, tablerTrash, tablerArrowLeft, tablerCheck, tablerPlus })
    ],
    templateUrl: './mst-add-edit.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class MstAddEditComponent implements OnInit {
    module    = 'Material Salvage Ticket';
    subModule = 'Create';
    menuLink  = 'mst';

    id: any  = null;
    editMode = false;
    isLoading = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    voucherDate = '';
    purpose     = '';

    departments        = signal<any[]>([]);
    inventoryLocations = signal<InventoryLocation[]>([]);
    selectedDepartment = signal<any>(null);
    selectedLocation   = signal<InventoryLocation | null>(null);

    returnedBy = signal<any>(null);
    receivedBy = signal<any>(null);

    details = signal<ItemTransactionDetailDto[]>([]);

    private service      = inject(MstService);
    private locationService      = inject(InventoryLocationService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.loadDepartments();
        this.loadInventoryLocations();

        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.editMode = idParam != null && /^\d+$/.test(idParam);
            if (this.editMode) {
                this.id = Number(idParam);
                this.subModule = 'Edit';
                this.loadForEdit();
            } else {
                this.subModule = 'Create';
                const today = new Date().toISOString().substring(0, 10);
                this.voucherDate = today;
                this.loadDefaultSignatories();
            }
        });
    }

    private loadDepartments(): void {
        this.service.getDepartments().subscribe({
            next: (d) => this.departments.set(d || []),
            error: () => {}
        });
    }

    private loadInventoryLocations(): void {
        this.locationService.getAllLocations().subscribe({
            next: (d) => this.inventoryLocations.set(d || []),
            error: () => {}
        });
    }

    private loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (data) => {
                if (data) {
                    this.returnedBy.set(data.returnedBy || null);
                    this.receivedBy.set(data.receivedBy || null);
                }
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
                    this.alertService.error(this.module, 'Record not found.', '');
                    this.router.navigate(['/' + this.menuLink]);
                    return;
                }
                const toYmd = (v: any) => v ? new Date(v).toISOString().substring(0, 10) : '';
                this.voucherDate = toYmd(data.voucherDate);
                this.purpose     = data.purpose || data.remarks || '';
                this.returnedBy.set(data.returnedBy || null);
                this.receivedBy.set(data.receivedBy || null);
                this.details.set((data.details || []).map((d: any) => ({ ...d })));

                const tryMatchDept = () => {
                    if (data.department?.id) {
                        this.selectedDepartment.set(this.departments().find(d => d.id === data.department.id) ?? data.department);
                    }
                };
                tryMatchDept();
                setTimeout(tryMatchDept, 400);

                const tryMatchLoc = () => {
                    if (data.inventoryLocation?.id) {
                        this.selectedLocation.set(this.inventoryLocations().find(l => l.id === data.inventoryLocation.id) ?? data.inventoryLocation);
                    }
                };
                tryMatchLoc();
                setTimeout(tryMatchLoc, 400);
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load record.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    async openItemStockBrowse(): Promise<void> {
        const locationId = this.selectedLocation()?.id;
        if (!locationId) {
            this.alertService.warning(this.module, 'Validation', 'Please select an Inventory Location first.');
            return;
        }
        try {
            const result = await this.modalService.openModal(
                BrowseItemStockModalComponent,
                { locationId, type: 'INV_LOCATION' },
                { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.onItemStockSelected(result.data as ItemStock);
            }
        } catch { }
    }

    private onItemStockSelected(itemStock: ItemStock): void {
        const itemStockId = itemStock.id;
        const isDuplicate = this.details().some(d => d.itemStockId === itemStockId);
        if (isDuplicate) {
            this.alertService.error(this.module, 'Duplicate Item', 'This item is already in the list.');
            return;
        }
        const location = this.selectedLocation();
        this.details.update(list => [...list, {
            itemStockId:         itemStockId,
            itemId:              itemStock.item?.id            ?? undefined,
            itemCode:            itemStock.item?.code          ?? '',
            unitCode:            itemStock.item?.unit?.code    ?? '',
            unitCost:            Number(itemStock.unitCost)    || 0,
            itemDescription:     itemStock.item?.description   ?? '',
            quantity:            0,
            inventoryLocationId: itemStock.inventoryLocation?.id ?? location?.id ?? undefined,
            isUsable:            true,
            serialNumbers:       []
        }]);
    }

    async openSerialNumbersModal(index: number): Promise<void> {
        const row = this.details()[index];
        if (!row) return;
        try {
            const result = await this.modalService.openModal(
                SerialNumbersModalComponent,
                {
                    itemDescription: row.itemDescription,
                    quantity:        Number(row.quantity) || 0,
                    serialNumbers:   row.serialNumbers ?? []
                },
                { size: 'lg', centered: true }
            );
            if (result?.action === 'save') {
                this.details.update(list => list.map((d, i) =>
                    i === index ? { ...d, serialNumbers: result.data as SpecialEquipment[] } : d));
            }
        } catch { }
    }

    onQuantityChange(index: number): void {
        const list = this.details();
        const row = list[index];
        if (!row) return;
        const qty = Number(row.quantity) || 0;
        if (qty < 0) {
            list[index] = { ...row, quantity: 0 };
            this.details.update(l => [...l]);
        }
    }

    removeRow(index: number): void {
        this.details.update(list => list.filter((_, i) => i !== index));
    }

    async openReturnedByBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, { defaultClassificationId: EMPLOYEE_CLASSIFICATION_ID }, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const e = result.data;
                this.returnedBy.set({ accountNo: e.accountNo, fullName: e.fullName || e.name });
            }
        } catch { }
    }

    async openReceivedByBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, { defaultClassificationId: EMPLOYEE_CLASSIFICATION_ID }, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const e = result.data;
                this.receivedBy.set({ accountNo: e.accountNo, fullName: e.fullName || e.name });
            }
        } catch { }
    }

    save(): void {
        const selectedDepartment = this.selectedDepartment();
        const selectedLocation = this.selectedLocation();
        const returnedBy = this.returnedBy();
        const receivedBy = this.receivedBy();
        const details = this.details();

        if (!this.voucherDate) {
            this.alertService.warning(this.module, 'Validation', 'Date is required.');
            return;
        }
        if (!this.purpose?.trim()) {
            this.alertService.warning(this.module, 'Validation', 'Purpose is required.');
            return;
        }
        if (!selectedDepartment?.id) {
            this.alertService.warning(this.module, 'Validation', 'Department is required.');
            return;
        }
        if (!selectedLocation?.id) {
            this.alertService.warning(this.module, 'Validation', 'Inventory Location is required.');
            return;
        }
        if (details.length === 0) {
            this.alertService.warning(this.module, 'Validation', 'No items selected. Please add at least one item.');
            return;
        }
        const invalidQty = details.find(d => !Number(d.quantity) || Number(d.quantity) <= 0);
        if (invalidQty) {
            this.alertService.warning(this.module, 'Validation',
                `Quantity for item "${invalidQty.itemCode}" must be greater than zero.`);
            return;
        }
        if (!returnedBy?.accountNo) {
            this.alertService.warning(this.module, 'Validation', 'Returned By is required.');
            return;
        }
        if (!receivedBy?.accountNo) {
            this.alertService.warning(this.module, 'Validation', 'Received By is required.');
            return;
        }

        this.isLoading.set(true);

        const payload: any = {
            voucherDate:       this.voucherDate,
            purpose:           this.purpose.trim(),
            inventoryLocation: { id: selectedLocation.id },
            department:        { id: selectedDepartment.id },
            returnedBy:        { accountNo: returnedBy.accountNo, fullName: returnedBy.fullName },
            receivedBy:        { accountNo: receivedBy.accountNo, fullName: receivedBy.fullName },
            details:           details.map(d => ({
                itemStockId:         d.itemStockId         || null,
                itemId:              d.itemId              || null,
                itemCode:            d.itemCode            || '',
                unitCode:            d.unitCode            || '',
                unitCost:            Number(d.unitCost)    || 0,
                itemDescription:     d.itemDescription     || '',
                quantity:            Number(d.quantity)    || 0,
                inventoryLocationId: d.inventoryLocationId || null,
                isUsable:            d.isUsable !== false,
                serialNumbers:       d.serialNumbers || []
            }))
        };
        if (this.editMode) payload.id = this.id;

        const request$ = this.editMode ? this.service.update(payload) : this.service.create(payload);

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

    compareById(a: any, b: any): boolean {
        return a && b ? a.id === b.id : a === b;
    }
}
