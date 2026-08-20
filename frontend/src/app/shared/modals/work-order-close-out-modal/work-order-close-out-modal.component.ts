import { Component, inject, Input, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { AlertService } from '@/app/shared/services/alert.service';
import { WorkOrderService } from '@/app/pages/work-order/work-order.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseCOAModalComponent } from '@/app/shared/modals/browse-coa-modal/browse-coa-modal.component';
import { AssetTypeService } from '@/app/pages/asset-type/asset-type.service';

const MONTHS = [
    { id: 1,  description: 'January'   },
    { id: 2,  description: 'February'  },
    { id: 3,  description: 'March'     },
    { id: 4,  description: 'April'     },
    { id: 5,  description: 'May'       },
    { id: 6,  description: 'June'      },
    { id: 7,  description: 'July'      },
    { id: 8,  description: 'August'    },
    { id: 9,  description: 'September' },
    { id: 10, description: 'October'   },
    { id: 11, description: 'November'  },
    { id: 12, description: 'December'  }
];

@Component({
    selector: 'app-work-order-close-out-modal',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './work-order-close-out-modal.component.html'
})
export class WorkOrderCloseOutModalComponent implements OnInit {
    activeModal    = inject(NgbActiveModal);
    private service          = inject(WorkOrderService);
    private assetTypeService = inject(AssetTypeService);
    private alertService     = inject(AlertService);
    private modalService     = inject(ModalService);

    @Input() workOrder: any       = {};
    @Input() workOrderDetail: any = {};

    assetTypes:   any[] = [];
    assetDetails: any[] = [];
    submitting = false;

    readonly months = MONTHS;

    asset = {
        description:        '',
        assetTypeId:        null as number | null,
        depreciationYears:  0,
        depreciationMonths: 0,
        monthlyDepreciation: 0,
        startYear:  new Date().getFullYear(),
        startMonth: null as number | null
    };

    get totalAmount(): number {
        const d = this.workOrderDetail || {};
        return (+(d.labor || 0)) + (+(d.materials || 0)) + (+(d.overhead || 0)) + (+(d.houseConnection || 0));
    }

    get assetValue(): number {
        return this.assetDetails.reduce((sum, r) => sum + (+(r.value || 0)), 0);
    }

    get totals() {
        return {
            value:               this.assetDetails.reduce((s, r) => s + (+(r.value               || 0)), 0),
            depreciatedValue:    this.assetDetails.reduce((s, r) => s + (+(r.depreciatedValue    || 0)), 0),
            monthlyDepreciation: this.assetDetails.reduce((s, r) => s + (+(r.monthlyDepreciation || 0)), 0),
            remainingValue:      this.assetDetails.reduce((s, r) => s + (+(r.remainingValue      || 0)), 0)
        };
    }

    ngOnInit(): void {
        this.loadAssetTypes();
    }

    loadAssetTypes(): void {
        this.assetTypeService.list('', 0, 200).subscribe({
            next: (data) => { this.assetTypes = data?.content ?? data ?? []; },
            error: () => {}
        });
    }

    onDepreciationYearsChange(): void {
        this.asset.depreciationMonths  = (+(this.asset.depreciationYears || 0)) * 12;
        this.asset.monthlyDepreciation = this.asset.depreciationMonths > 0
            ? this.assetValue / this.asset.depreciationMonths
            : 0;
        this.recalculateRows();
    }

    onRowValueChange(row: any): void {
        row.monthlyDepreciation = this.asset.depreciationMonths > 0
            ? (+(row.value || 0)) / this.asset.depreciationMonths
            : 0;
        row.remainingValue = (+(row.value || 0)) - (+(row.depreciatedValue || 0));
        // Recalculate header monthly depreciation
        this.asset.monthlyDepreciation = this.asset.depreciationMonths > 0
            ? this.assetValue / this.asset.depreciationMonths
            : 0;
    }

    onRowDepreciatedValueChange(row: any): void {
        row.remainingValue = (+(row.value || 0)) - (+(row.depreciatedValue || 0));
    }

    recalculateRows(): void {
        for (const row of this.assetDetails) {
            row.monthlyDepreciation = this.asset.depreciationMonths > 0
                ? (+(row.value || 0)) / this.asset.depreciationMonths
                : 0;
            row.remainingValue = (+(row.value || 0)) - (+(row.depreciatedValue || 0));
        }
    }

    addRow(): void {
        this.assetDetails.push({
            assetAccount:       null,
            expenseAccount:     null,
            accumDepAccount:    null,
            value:              0,
            depreciatedValue:   0,
            monthlyDepreciation: 0,
            remainingValue:     0
        });
    }

    removeRow(index: number): void {
        this.assetDetails.splice(index, 1);
        this.asset.monthlyDepreciation = this.asset.depreciationMonths > 0
            ? this.assetValue / this.asset.depreciationMonths
            : 0;
    }

    async browseAccount(type: 'asset' | 'expense' | 'accumDep', index: number): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseCOAModalComponent,
                {},
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select') {
                const account = {
                    accountCode:  result.data.accountCode,
                    accountTitle: result.data.accountTitle || result.data.accountDescription
                };
                if (type === 'asset')    this.assetDetails[index].assetAccount    = account;
                if (type === 'expense')  this.assetDetails[index].expenseAccount  = account;
                if (type === 'accumDep') this.assetDetails[index].accumDepAccount = account;
            }
        } catch (_) {}
    }

    continueClosing(): void {
        if (!this.asset.assetTypeId) {
            this.alertService.warning('Close Out', 'Validation', 'Asset Type is required.');
            return;
        }
        if (!this.asset.depreciationYears || this.asset.depreciationYears <= 0) {
            this.alertService.warning('Close Out', 'Validation', 'Depreciation Years is required.');
            return;
        }
        if (!this.asset.startYear) {
            this.alertService.warning('Close Out', 'Validation', 'Start Year is required.');
            return;
        }
        if (!this.asset.startMonth) {
            this.alertService.warning('Close Out', 'Validation', 'Start Month is required.');
            return;
        }
        if (this.assetDetails.length === 0) {
            this.alertService.warning('Close Out', 'Validation', 'At least one account detail row is required.');
            return;
        }

        const payload = {
            workOrderId:     this.workOrder.id,
            assetTypeId:     this.asset.assetTypeId,
            description:     this.asset.description,
            depreciationYears:  this.asset.depreciationYears,
            depreciationMonths: this.asset.depreciationMonths,
            monthlyDepreciation: this.asset.monthlyDepreciation,
            startYear:       this.asset.startYear,
            startMonth:      this.asset.startMonth,
            assetDetails:    this.assetDetails.map(r => ({
                assetAccountCode:    r.assetAccount?.accountCode    || null,
                expenseAccountCode:  r.expenseAccount?.accountCode  || null,
                accumDepAccountCode: r.accumDepAccount?.accountCode || null,
                value:               +(r.value              || 0),
                depreciatedValue:    +(r.depreciatedValue    || 0),
                monthlyDepreciation: +(r.monthlyDepreciation || 0),
                remainingValue:      +(r.remainingValue      || 0)
            }))
        };

        this.submitting = true;
        this.service.closeOut(payload).subscribe({
            next: (res) => {
                this.submitting = false;
                if (res?.success === false) {
                    this.alertService.error('Close Out', res.failureMessage || 'Close out failed.', '');
                } else {
                    this.activeModal.close({ action: 'closed' });
                }
            },
            error: () => {
                this.submitting = false;
                this.alertService.error('Close Out', 'An error occurred.', '');
            }
        });
    }
}
