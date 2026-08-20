import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { StockReleaseService } from '../stock-release.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseReleasingDocumentsModalComponent } from '@/app/shared/modals/browse-releasing-documents/browse-releasing-documents.component';
import { InventoryDocumentDto, ReleasingDocumentType } from '@/app/models/inventory-document.model';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerTrash, tablerArrowLeft, tablerCheck, tablerPlus } from '@ng-icons/tabler-icons';

const RELEASING_DOCUMENT_TYPE_CODE: Record<ReleasingDocumentType, number> = { SW: 1, ST: 2, MR: 3 };

@Component({
    selector: 'app-stock-release-add-edit',
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
        provideIcons({ tablerSearch, tablerTrash, tablerArrowLeft, tablerCheck, tablerPlus })
    ],
    templateUrl: './stock-release-add-edit.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class StockReleaseAddEditComponent implements OnInit {
    module    = 'Stock Release';
    subModule = 'Create';
    menuLink  = 'stock-release';

    id: any  = null;
    editMode = false;
    isLoading = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    voucherDate = '';
    description = '';

    selectedWithdrawal = signal<any>(null);

    releaseType: number | null = null;
    releaseDocumentType: number | null = null;

    receivedBy = signal<any>(null);

    details = signal<any[]>([]);

    totalReleaseQuantity = computed(() => this.details().reduce((s, r) => s + (Number(r.releaseQuantity) || 0), 0));

    private service      = inject(StockReleaseService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
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

    private loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (data) => {
                if (data) this.receivedBy.set(data.receivedBy || null);
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
                this.voucherDate  = toYmd(data.voucherDate);
                this.description  = data.description || '';
                this.receivedBy.set(data.receivedBy || null);
                this.details.set((data.details || []).map((d: any) => ({ ...d, releaseQuantity: Number(d.releaseQuantity ?? d.quantityReleased) || 0 })));
                this.releaseType = data.type ?? null;
                this.releaseDocumentType = data.documentType ?? null;
                if (data.documentTransaction) {
                    this.selectedWithdrawal.set({ id: data.documentTransaction.id, transId: data.documentTransaction.id, code: data.withdrawalCode || '—' });
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load record.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    async openWithdrawalBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseReleasingDocumentsModalComponent, { documentType: 'SW' }, { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const documentType = result.documentType as ReleasingDocumentType | undefined;
                this.releaseDocumentType = documentType ? RELEASING_DOCUMENT_TYPE_CODE[documentType] ?? null : null;
                this.onWithdrawalSelected(result.data);
            }
        } catch { }
    }

    private onWithdrawalSelected(document: InventoryDocumentDto): void {
        this.selectedWithdrawal.set(document);
        this.releaseType = document.inventoryCategoryTypeId ?? null;
        this.description = document.purpose || this.description;
        if (document.createdByUser) {
            this.receivedBy.set({ accountNo: document.createdByUser.accountNo, fullName: document.createdByUser.fullName });
        }
        this.details.set((document.withdrawalDetails || []).map(d => ({
            itemId:              d.itemId             || null,
            itemCode:            d.itemCode            || '',
            unitId:              d.unitId              || null,
            unitCode:            d.unitCode            || '',
            itemDescription:     d.itemDescription     || '',
            quantityOrdered:     Number(d.quantity)    || 0,
            quantityReleased:    Number(d.quantityReleased) || 0,
            releaseQuantity:     Math.max(0, (Number(d.quantity) || 0) - (Number(d.quantityReleased) || 0)),
            inventoryCategoryId: d.inventoryCategoryId || null
        })));
    }

    clearWithdrawal(): void {
        this.selectedWithdrawal.set(null);
        this.releaseType = null;
        this.releaseDocumentType = null;
        this.details.set([]);
    }

    async openReceivedByBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const entity = result.data;
                this.receivedBy.set({ accountNo: entity.accountNo, fullName: entity.fullName || entity.name });
            }
        } catch { }
    }

    onReleaseQuantityChange(index: number): void {
        const row = this.details()[index];
        if (!row) return;
        const qty = Number(row.releaseQuantity) || 0;
        const max = Math.max(0, (Number(row.quantityOrdered) || 0) - (Number(row.quantityReleased) || 0));
        if (qty > max) {
            row.releaseQuantity = max;
            this.alertService.warning(this.module, 'Validation', `Release quantity cannot exceed the remaining balance (${max}).`);
        }
        if (qty < 0) row.releaseQuantity = 0;
        this.details.update(list => [...list]);
    }

    save(): void {
        if (!this.voucherDate) {
            this.alertService.warning(this.module, 'Validation', 'Voucher Date is required.');
            return;
        }
        const selectedWithdrawal = this.selectedWithdrawal();
        if (!this.editMode) {
            if (!selectedWithdrawal?.transId) {
                this.alertService.warning(this.module, 'Validation', 'Please browse and select a Withdrawal Document.');
                return;
            }
            if (!this.releaseType) {
                this.alertService.warning(this.module, 'Validation', 'Selected withdrawal document has no inventory category type configured.');
                return;
            }
            if (!this.releaseDocumentType) {
                this.alertService.warning(this.module, 'Validation', 'Unable to determine the source document type. Please browse and select the document again.');
                return;
            }
        }
        const details = this.details();
        if (details.length === 0) {
            this.alertService.warning(this.module, 'Validation', 'No items loaded. Please select a Withdrawal Document first.');
            return;
        }
        const hasQty = details.some(d => (Number(d.releaseQuantity) || 0) > 0);
        if (!hasQty) {
            this.alertService.warning(this.module, 'Validation', 'Total release quantity is zero — enter quantities for at least one item.');
            return;
        }
        const receivedBy = this.receivedBy();
        if (!receivedBy?.accountNo) {
            this.alertService.warning(this.module, 'Validation', 'Received By is required.');
            return;
        }

        this.isLoading.set(true);

        const payload: any = {
            voucherDate:         this.voucherDate,
            description:         this.description.trim() || null,
            type:                this.releaseType,
            documentType:        this.releaseDocumentType,
            documentTransaction: { id: selectedWithdrawal?.transId ?? selectedWithdrawal?.id },
            receivedBy:          { accountNo: receivedBy.accountNo, fullName: receivedBy.fullName },
            details:             details.map(d => ({
                itemId:              d.itemId             || null,
                itemCode:            d.itemCode            || '',
                unitId:              d.unitId              || null,
                unitCode:            d.unitCode            || '',
                itemDescription:     d.itemDescription     || '',
                quantityOrdered:     Number(d.quantityOrdered)  || 0,
                releaseQuantity:     Number(d.releaseQuantity)  || 0,
                quantityReleased:    Number(d.quantityReleased) || 0,
                inventoryCategoryId: d.inventoryCategoryId      || null
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
}
