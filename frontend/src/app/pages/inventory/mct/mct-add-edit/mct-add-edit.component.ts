import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { MctService } from '../mct.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseMctStockReleaseModalComponent } from '@/app/shared/modals/browse-mct-stock-release-modal/browse-mct-stock-release-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerArrowLeft, tablerCheck } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-mct-add-edit',
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
    templateUrl: './mct-add-edit.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class MctAddEditComponent implements OnInit {
    module    = 'Material Credit Ticket';
    subModule = 'Create';
    menuLink  = 'mct';

    id: any  = null;
    editMode = false;
    isLoading = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    voucherDate = '';
    remarks     = '';

    inventoryLocations = signal<any[]>([]);
    selectedLocation   = signal<any>(null);

    selectedStockRelease = signal<any>(null);

    approvingOfficer = signal<any>(null);

    details = signal<any[]>([]);

    totalQuantity = computed(() => this.details().reduce((s, r) => s + (Number(r.quantity) || 0), 0));

    private service      = inject(MctService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
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

    private loadInventoryLocations(): void {
        this.service.getInventoryLocations().subscribe({
            next: (d) => this.inventoryLocations.set(d || []),
            error: () => {}
        });
    }

    private loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (data) => {
                if (data) this.approvingOfficer.set(data.approvedBy || data.approvingOfficer || null);
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
                this.remarks     = data.remarks || '';
                this.approvingOfficer.set(data.approvingOfficer || null);
                this.details.set((data.details || []).map((d: any) => ({ ...d, quantity: Number(d.quantity) || 0 })));

                if (data.stockRelease) {
                    this.selectedStockRelease.set({ id: data.stockRelease.id, code: data.stockRelease.code || '—' });
                }

                const tryMatch = () => {
                    if (data.inventoryLocation?.id) {
                        this.selectedLocation.set(this.inventoryLocations().find(l => l.id === data.inventoryLocation.id) ?? data.inventoryLocation);
                    }
                };
                tryMatch();
                setTimeout(tryMatch, 400);
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load record.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    async openStockReleaseBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseMctStockReleaseModalComponent, {}, { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.onStockReleaseSelected(result.data);
            }
        } catch { }
    }

    private onStockReleaseSelected(sr: any): void {
        this.selectedStockRelease.set(sr);
        if (sr.inventoryLocation?.id) {
            this.selectedLocation.set(this.inventoryLocations().find(l => l.id === sr.inventoryLocation.id) ?? sr.inventoryLocation);
        }
        this.isLoading.set(true);
        this.service.getStockReleaseDetails(sr.id).subscribe({
            next: (items) => {
                this.isLoading.set(false);
                this.details.set((items || []).map((item: any) => ({
                    itemId:           item.itemId    || item.id    || null,
                    itemCode:         item.itemCode  || item.code  || '',
                    unitId:           item.unitId                  || null,
                    unitCode:         item.unitCode                || '',
                    itemDescription:  item.itemDescription || item.description || '',
                    unitCost:         Number(item.unitCost) || 0,
                    quantityReleased: Number(item.quantity || item.quantityReleased) || 0,
                    quantity:         0
                })));
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load stock release items.', '');
            }
        });
    }

    clearStockRelease(): void {
        this.selectedStockRelease.set(null);
        this.details.set([]);
    }

    async openApprovingOfficerBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                const entity = result.data;
                this.approvingOfficer.set({ accountNo: entity.accountNo, fullName: entity.fullName || entity.name });
            }
        } catch { }
    }

    onQuantityChange(index: number): void {
        const row = this.details()[index];
        if (!row) return;
        const qty = Number(row.quantity) || 0;
        const max = Number(row.quantityReleased) || 0;
        if (qty > max) {
            row.quantity = max;
            this.alertService.warning(this.module, 'Validation',
                `Return quantity cannot exceed quantity released (${max}).`);
        }
        if (qty < 0) row.quantity = 0;
        this.details.update(list => [...list]);
    }

    save(): void {
        const selectedStockRelease = this.selectedStockRelease();
        const selectedLocation     = this.selectedLocation();
        const details              = this.details();
        const approvingOfficer     = this.approvingOfficer();

        if (!this.voucherDate) {
            this.alertService.warning(this.module, 'Validation', 'Date is required.');
            return;
        }
        if (!selectedStockRelease?.id && !this.editMode) {
            this.alertService.warning(this.module, 'Validation', 'Please browse and select a Stock Release document.');
            return;
        }
        if (!selectedLocation?.id) {
            this.alertService.warning(this.module, 'Validation', 'Inventory Location is required.');
            return;
        }
        if (details.length === 0) {
            this.alertService.warning(this.module, 'Validation', 'No items loaded. Please select a Stock Release first.');
            return;
        }
        const hasQty = details.some(d => (Number(d.quantity) || 0) > 0);
        if (!hasQty) {
            this.alertService.warning(this.module, 'Validation', 'Total return quantity is zero — enter quantities for at least one item.');
            return;
        }
        if (!approvingOfficer?.accountNo) {
            this.alertService.warning(this.module, 'Validation', 'Received By is required.');
            return;
        }

        this.isLoading.set(true);

        const payload: any = {
            voucherDate:       this.voucherDate,
            remarks:           this.remarks.trim() || null,
            stockRelease:      { id: selectedStockRelease?.id },
            inventoryLocation: { id: selectedLocation.id },
            approvingOfficer:  { accountNo: approvingOfficer.accountNo, fullName: approvingOfficer.fullName },
            details:           details.map(d => ({
                itemId:           d.itemId           || null,
                itemCode:         d.itemCode          || '',
                unitId:           d.unitId            || null,
                unitCode:         d.unitCode           || '',
                itemDescription:  d.itemDescription   || '',
                quantity:         Number(d.quantity)  || 0,
                quantityReleased: Number(d.quantityReleased) || 0,
                unitCost:         Number(d.unitCost)  || 0
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
