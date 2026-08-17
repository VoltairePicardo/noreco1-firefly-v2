import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { FormsModule } from '@angular/forms';
import { FlatpickrDefaults, FlatpickrModule } from 'angularx-flatpickr';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerPlus, tablerTrash, tablerX, tablerArrowLeft, tablerDeviceFloppy } from '@ng-icons/tabler-icons';
import { AlertService } from '@/app/shared/services/alert.service';
import { LaddaModule } from 'angular2-ladda';
import { forkJoin } from 'rxjs';
import { CanvassService } from '../canvass.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseSupplierModalComponent } from '@/app/shared/modals/browse-supplier-modal/browse-supplier-modal.component';
import { BrowseRvItemsModalComponent } from '@/app/shared/modals/browse-rv-items-modal/browse-rv-items-modal.component';

@Component({
    selector: 'app-canvass-add-edit',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, FormsModule, FlatpickrModule, LaddaModule, RouterLink],
    providers: [FlatpickrDefaults, provideIcons({ tablerSearch, tablerPlus, tablerTrash, tablerX, tablerArrowLeft, tablerDeviceFloppy })],
    templateUrl: './canvass-add-edit.component.html'
})
export class CanvassAddEditComponent {
    module    = 'Canvass';
    subModule = 'Create';
    menuLink  = 'canvass';

    id: any    = null;
    editMode   = false;
    formSubmit = false;
    isLoading  = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    voucherDate = '';
    suppliers: (any | null)[] = [null, null, null];
    lineItems: any[] = [];

    private service      = inject(CanvassService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            const id = params.get('id');
            if (id && /^\d+$/.test(id)) {
                this.id       = +id;
                this.editMode = true;
                this.subModule = 'Edit';
                this.loadForEdit();
            }
        });
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        forkJoin({
            header:  this.service.getData(this.id),
            details: this.service.getDetails(this.id)
        }).subscribe({
            next: ({ header, details }) => {
                this.isLoading.set(false);
                if (header?.id) {
                    const d = new Date(header.voucherDate);
                    const mm = String(d.getMonth() + 1).padStart(2, '0');
                    const dd = String(d.getDate()).padStart(2, '0');
                    this.voucherDate = `${d.getFullYear()}-${mm}-${dd}`;

                    // restore suppliers from header.suppliers list
                    this.suppliers = [null, null, null];
                    if (header.suppliers?.length > 0) {
                        header.suppliers.forEach((s: any, i: number) => {
                            if (i < 3) this.suppliers[i] = s;
                        });
                    }

                    // restore line items from details
                    this.lineItems = (details || []).map((d: any) => ({
                        rvDetailId:       d.rvDetailId,
                        rvNumber:         d.rvNumber,
                        itemCode:         d.itemCode,
                        itemDescription:  d.itemDescription,
                        unitCode:         d.unitCode,
                        quantity:         d.quantity,
                        priceSupplier1:   d.priceSupplier1 ?? 0,
                        priceSupplier2:   d.priceSupplier2 ?? 0,
                        priceSupplier3:   d.priceSupplier3 ?? 0,
                    }));
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load canvass.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    async openSupplierBrowse(index: number): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseSupplierModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this.suppliers[index] = result.data;
            }
        } catch { }
    }

    removeSupplier(index: number): void {
        this.suppliers[index] = null;
    }

    async openRvBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseRvItemsModalComponent, {}, { size: 'xl', centered: true });
            if (result?.action === 'select' && result?.data?.length) {
                const existing = new Set(this.lineItems.map((li: any) => li.rvDetailId));
                result.data.filter((item: any) => !existing.has(item.id)).forEach((item: any) => {
                    this.lineItems.push({
                        rvDetailId:      item.id,
                        rvNumber:        item.rvCode || item.purchaseRequestCode || '',
                        itemCode:        item.itemCode || item.code || '',
                        itemDescription: item.itemDescription || item.description || '',
                        unitCode:        item.unitCode || item.unit?.code || '',
                        quantity:        item.quantity || 0,
                        priceSupplier1:  0,
                        priceSupplier2:  0,
                        priceSupplier3:  0,
                    });
                });
            }
        } catch { }
    }

    removeLineItem(index: number): void {
        this.lineItems.splice(index, 1);
    }

    // ─── Save ────────────────────────────────────────────────────────────────

    save(): void {
        if (!this.voucherDate) {
            this.alertService.warning(this.module, 'Please enter the voucher date.', '');
            return;
        }
        if (this.lineItems.length === 0) {
            this.alertService.warning(this.module, 'Please add at least one item.', '');
            return;
        }

        this.formSubmit = true;

        const payload: any = {
            voucherDate:    this.voucherDate,
            suppliers:      this.suppliers.filter(s => s !== null),
            canvassDetails: this.lineItems.map(li => ({
                rvDetailId:      li.rvDetailId,
                rvNumber:        li.rvNumber,
                itemCode:        li.itemCode,
                itemDescription: li.itemDescription,
                unitCode:        li.unitCode,
                quantity:        li.quantity,
                priceSupplier1:  li.priceSupplier1 || 0,
                priceSupplier2:  li.priceSupplier2 || 0,
                priceSupplier3:  li.priceSupplier3 || 0,
            }))
        };

        if (this.editMode) {
            payload.id = this.id;
        }

        const req = this.editMode
            ? this.service.update(payload)
            : this.service.create(payload);

        req.subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, res.successMessage || 'Saved successfully.', '');
                    this.router.navigate(['/' + this.menuLink, res.modelId, 'detail']);
                } else {
                    const msgs = res?.messages?.join('\n') || res?.failureMessage || 'Save failed.';
                    this.alertService.error(this.module, msgs, '');
                }
            },
            error: () => {
                this.formSubmit = false;
                this.alertService.error(this.module, 'An error occurred.', '');
            }
        });
    }
}
