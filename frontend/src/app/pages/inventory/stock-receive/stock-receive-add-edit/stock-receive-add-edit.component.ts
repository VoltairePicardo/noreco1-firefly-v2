import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { StockReceiveService } from '../stock-receive.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseStockReceiveDocModalComponent } from '@/app/shared/modals/browse-stock-receive-doc-modal/browse-stock-receive-doc-modal.component';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerArrowLeft, tablerCheck } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-stock-receive-add-edit',
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
    templateUrl: './stock-receive-add-edit.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class StockReceiveAddEditComponent implements OnInit {
    module    = 'Receive Stock Transfer';
    subModule = 'Create';
    menuLink  = 'stock-receive';

    id: any  = null;
    editMode = false;
    isLoading = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    voucherDate      = '';
    description      = '';
    inventoryLocation   = signal<any>(null);
    inventoryLocations  = signal<any[]>([]);
    documentTransaction = signal<any>(null);
    selectedDocCode      = signal('');
    selectedDocDate      = signal('');
    selectedDocCreatedBy = signal('');
    documentType         = signal<any>(null);
    details              = signal<any[]>([]);
    checkedBy            = signal<any>(null);

    private service      = inject(StockReceiveService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.loadInventoryLocations();
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
                this.voucherDate = new Date().toISOString().substring(0, 10);
            }
        });
    }

    private loadInventoryLocations(): void {
        this.service.getInventoryLocations().subscribe({
            next: (d) => { this.inventoryLocations.set(d || []); },
            error: () => {}
        });
    }

    private loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (data) => { this.checkedBy.set(data?.checker || null); },
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
                this.voucherDate         = data.voucherDate ? new Date(data.voucherDate).toISOString().substring(0, 10) : '';
                this.description         = data.description || data.remarks || '';
                this.inventoryLocation.set(data.inventoryLocation || null);
                this.documentTransaction.set(data.documentTransaction || null);
                this.selectedDocCode.set(data.documentTransaction?.code || '');
                this.checkedBy.set(data.checkedBy || this.checkedBy());
                this.details.set((data.details || []).map((d: any) => ({ ...d })));
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Error.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    async openDocumentBrowse(): Promise<void> {
        const location = this.inventoryLocation();
        if (!location?.id) {
            this.alertService.warning(this.module, 'Validation', 'Select an Inventory Location first.');
            return;
        }
        try {
            const result = await this.modalService.openModal(
                BrowseStockReceiveDocModalComponent,
                { locationId: location.id },
                { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const doc = result.data;
                this.documentTransaction.set({ id: doc.transId || doc.id });
                this.selectedDocCode.set(doc.code || '');
                this.selectedDocDate.set(doc.date || doc.voucherDate || '');
                this.selectedDocCreatedBy.set(doc.createdBy || '');
                this.documentType.set(doc.type || null);
                this.details.set((doc.details || []).map((d: any) => ({
                    itemId:              d.itemId,
                    itemCode:            d.itemCode,
                    unitId:              d.unitId,
                    unitCode:            d.unitCode,
                    itemDescription:     d.itemDescription,
                    quantityOrdered:     d.quantityOrdered,
                    quantityReleased:    d.quantityReleased,
                    quantityReceived:    d.quantityReceived,
                    receiveQuantity:     d.quantityReleased - d.quantityReceived,
                    unitCost:            d.unitCost,
                    inventoryLocationId: d.inventoryLocationId || location.id
                })));
            }
        } catch { }
    }

    onReceiveQtyChange(item: any, value: string): void {
        const max = item.quantityReleased - item.quantityReceived;
        let qty = parseFloat(value) || 0;
        if (qty < 0)   qty = 0;
        if (qty > max) qty = max;
        item.receiveQuantity = qty;
        this.details.update(list => [...list]);
    }

    async openSignatoryBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.checkedBy.set(result.data);
            }
        } catch { }
    }

    save(): void {
        const location = this.inventoryLocation();
        const documentTransaction = this.documentTransaction();
        const details = this.details();
        const checkedBy = this.checkedBy();

        if (!this.voucherDate)
            { this.alertService.warning(this.module, 'Validation', 'Date is required.'); return; }
        if (!location?.id)
            { this.alertService.warning(this.module, 'Validation', 'Inventory Location is required.'); return; }
        if (!this.description.trim())
            { this.alertService.warning(this.module, 'Validation', 'Description is required.'); return; }
        if (!documentTransaction?.id)
            { this.alertService.warning(this.module, 'Validation', 'Please browse and select a receiving document.'); return; }
        if (details.length === 0)
            { this.alertService.warning(this.module, 'Validation', 'No items to receive.'); return; }
        if (details.some(d => !(d.receiveQuantity > 0)))
            { this.alertService.warning(this.module, 'Validation', 'All receive quantities must be greater than 0.'); return; }
        if (!checkedBy)
            { this.alertService.warning(this.module, 'Validation', 'Please select a Noted By (noting officer).'); return; }

        this.isLoading.set(true);

        const payload: any = {
            voucherDate:         this.voucherDate,
            description:         this.description.trim(),
            documentTransaction: { id: documentTransaction.id },
            documentType:        this.documentType(),
            approvingOfficer:    { accountNo: 0, fullName: '' },
            checkedBy:           {
                accountNo: checkedBy?.accountNo || 0,
                fullName:  checkedBy?.fullName || checkedBy?.name || ''
            },
            inventoryLocation:   { id: location.id },
            details:             details.map(d => ({
                itemId:              d.itemId,
                itemCode:            d.itemCode,
                unitId:              d.unitId,
                unitCode:            d.unitCode,
                itemDescription:     d.itemDescription,
                quantityOrdered:     d.quantityOrdered,
                quantityReleased:    d.quantityReleased,
                quantityReceived:    d.quantityReceived,
                receiveQuantity:     d.receiveQuantity,
                unitCost:            d.unitCost,
                inventoryLocationId: d.inventoryLocationId
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
