import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Router } from '@angular/router';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { WorkOrderService } from '../work-order.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseWorkOrderModalComponent } from '@/app/shared/modals/browse-work-order-modal/browse-work-order-modal.component';
import { BrowseVoucherModalComponent } from '@/app/shared/modals/browse-voucher-modal/browse-voucher-modal.component';

const VAT_RATE = 0.12;

@Component({
    selector: 'app-work-order-posting',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS
    ],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './work-order-posting.component.html'
})
export class WorkOrderPostingComponent {
    module    = 'Work Order';
    subModule = 'Posting';
    menuLink  = 'work-order';

    isLoading = signal(false);

    workOrder: any = null;
    voucher:   any = null;

    detail = {
        materials:      0,
        labor:          0,
        overhead:       0,
        houseConnection: 0
    };

    calMaterialsTax = false;
    calLaborTax     = false;
    calOverheadTax  = false;

    totalCharge = 0;
    inputTax    = 0;

    private service      = inject(WorkOrderService);
    private alertService = inject(AlertService);
    private modalService = inject(ModalService);
    private router       = inject(Router);

    async browseWorkOrder(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseWorkOrderModalComponent,
                {},
                { size: 'xl', centered: true }
            );
            if (result?.action === 'select') {
                this.workOrder = result.data;
                this.voucher   = null;
                this.resetDetail();
            }
        } catch (_) {}
    }

    async browseVoucher(): Promise<void> {
        if (!this.workOrder) return;
        try {
            const result = await this.modalService.openModal(
                BrowseVoucherModalComponent,
                { accountNo: this.workOrder.accountNumber || '' },
                { size: 'xl', centered: true }
            );
            if (result?.action === 'select') {
                this.voucher = result.data;
                this.resetDetail();
            }
        } catch (_) {}
    }

    resetDetail(): void {
        this.detail = { materials: 0, labor: 0, overhead: 0, houseConnection: 0 };
        this.calMaterialsTax = false;
        this.calLaborTax     = false;
        this.calOverheadTax  = false;
        this.recalculate();
    }

    recalculate(): void {
        const m = +(this.detail.materials      || 0);
        const l = +(this.detail.labor          || 0);
        const o = +(this.detail.overhead       || 0);
        const h = +(this.detail.houseConnection || 0);

        this.totalCharge = m + l + o + h;

        const mTax = this.calMaterialsTax ? (m / (1 + VAT_RATE)) * VAT_RATE : 0;
        const lTax = this.calLaborTax     ? (l / (1 + VAT_RATE)) * VAT_RATE : 0;
        const oTax = this.calOverheadTax  ? (o / (1 + VAT_RATE)) * VAT_RATE : 0;

        this.inputTax = mTax + lTax + oTax;
    }

    save(): void {
        if (!this.workOrder) {
            this.alertService.warning(this.module, 'Validation', 'Please select a work order.');
            return;
        }
        if (!this.voucher) {
            this.alertService.warning(this.module, 'Validation', 'Please select a voucher.');
            return;
        }

        this.isLoading.set(true);

        const payload = {
            workOrderId: this.workOrder.id,
            voucherId:   this.voucher.id ?? this.voucher.transactionId,
            materials:       +(this.detail.materials       || 0),
            labor:           +(this.detail.labor           || 0),
            overhead:        +(this.detail.overhead        || 0),
            houseConnection: +(this.detail.houseConnection || 0),
            inputTax:        this.inputTax,
            totalCharge:     this.totalCharge,
            calMaterialsTax: this.calMaterialsTax,
            calLaborTax:     this.calLaborTax,
            calOverheadTax:  this.calOverheadTax
        };

        this.service.postWorkOrder(payload).subscribe({
            next: (res) => {
                this.isLoading.set(false);
                if (res?.success === false) {
                    this.alertService.error(this.module, 'Posting', res.failureMessage || '');
                } else {
                    this.alertService.success(this.module, 'Posted successfully.', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Posting failed.', '');
            }
        });
    }
}
