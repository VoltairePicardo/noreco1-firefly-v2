import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { FormsModule } from '@angular/forms';
import { provideIcons } from '@ng-icons/core';
import { tablerPlus, tablerTrash, tablerArrowLeft, tablerCheck } from '@ng-icons/tabler-icons';
import { AlertService } from '@/app/shared/services/alert.service';
import { LaddaModule } from 'angular2-ladda';
import { forkJoin } from 'rxjs';
import { CanvassService } from '../canvass.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseRvItemsModalComponent } from '@/app/shared/modals/browse-rv-items-modal/browse-rv-items-modal.component';

@Component({
    selector: 'app-canvass-add-edit',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, FormsModule, LaddaModule, RouterLink],
    providers: [provideIcons({ tablerPlus, tablerTrash, tablerArrowLeft, tablerCheck })],
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

    voucherDate = '';
    lineItems: any[] = [];

    private service      = inject(CanvassService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.voucherDate = this.todayStr();
        this.route.paramMap.subscribe(params => {
            const id = params.get('id');
            if (id && /^\d+$/.test(id)) {
                this.id        = +id;
                this.editMode  = true;
                this.subModule = 'Edit';
                this.loadForEdit();
            }
        });
    }

    private todayStr(): string {
        const d = new Date();
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        forkJoin({
            header:  this.service.getData(this.id),
            details: this.service.getDetails(this.id)
        }).subscribe({
            next: ({ header, details }) => {
                this.isLoading.set(false);
                // Restore original voucher date (do NOT reset to today on edit)
                if (header?.voucherDate) {
                    const d = new Date(header.voucherDate);
                    this.voucherDate = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
                }
                // Deduplicate by rvDetailId (backend stores one row per supplier per item)
                const seen = new Set<number>();
                this.lineItems = (details || [])
                    .filter((d: any) => { if (seen.has(d.rvDetailId)) return false; seen.add(d.rvDetailId); return true; })
                    .map((d: any) => ({
                        rvDetailId:      d.rvDetailId,
                        rvNumber:        d.rvNumber,
                        itemCode:        d.itemCode,
                        itemDescription: d.itemDescription,
                        unitCode:        d.unitCode,
                        quantity:        d.quantity,
                    }));
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load canvass.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    async openRvBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseRvItemsModalComponent, {}, { size: 'xl', centered: true });
            if (result?.action === 'select' && result?.data?.length) {
                const existing = new Set(this.lineItems.map((li: any) => li.rvDetailId));
                result.data.filter((item: any) => !existing.has(item.id)).forEach((item: any) => {
                    this.lineItems.push({
                        rvDetailId:      item.id,
                        rvNumber:        item.rvNumber || '',
                        itemCode:        item.itemCode || '',
                        itemDescription: item.itemDescription || '',
                        unitCode:        item.unitCode || '',
                        quantity:        item.quantity || 0,
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
        if (this.lineItems.length === 0) {
            this.alertService.warning(this.module, 'Please add at least one item.', '');
            return;
        }
        const blankPr = this.lineItems.find(li => !li.rvNumber);
        if (blankPr) {
            this.alertService.warning(this.module, 'One or more items have a blank PR number. Please re-browse and select valid items.', '');
            return;
        }

        this.formSubmit = true;

        const payload: any = {
            voucherDate:    this.voucherDate,
            suppliers:      [],
            canvassDetails: this.lineItems.map(li => ({
                rvDetailId:      li.rvDetailId,
                rvNumber:        li.rvNumber,
                itemCode:        li.itemCode,
                itemDescription: li.itemDescription,
                unitCode:        li.unitCode,
                quantity:        li.quantity,
                priceSupplier1:  0,
                priceSupplier2:  0,
                priceSupplier3:  0,
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
