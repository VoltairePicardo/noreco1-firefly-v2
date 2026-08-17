import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { PrepaymentService } from '../prepayment.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseCOAModalComponent } from '@/app/shared/modals/browse-coa-modal/browse-coa-modal.component';

@Component({
    selector: 'app-prepayment-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective
    ],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './prepayment-add-edit.component.html'
})
export class PrepaymentAddEditComponent {
    module    = 'Prepayments and Other Amortizations';
    subModule = 'Create';
    menuLink  = 'prepayment';

    id: any    = null;
    editMode   = false;
    formSubmit = false;
    isLoading  = signal(false);
    hasPpd     = false;

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // Form fields
    datePaid           = '';
    description        = '';
    startMonth         = 1;
    startYear          = new Date().getFullYear();
    totalCost          = 0;
    noOfMonths         = 0;
    monthlyCost        = 0;
    appliedCost        = 0;
    balance            = 0;
    prepaymentAccount: any = null;
    expenseAccount: any    = null;

    readonly months = [
        { value: 1,  label: 'January'   },
        { value: 2,  label: 'February'  },
        { value: 3,  label: 'March'     },
        { value: 4,  label: 'April'     },
        { value: 5,  label: 'May'       },
        { value: 6,  label: 'June'      },
        { value: 7,  label: 'July'      },
        { value: 8,  label: 'August'    },
        { value: 9,  label: 'September' },
        { value: 10, label: 'October'   },
        { value: 11, label: 'November'  },
        { value: 12, label: 'December'  },
    ];

    private service      = inject(PrepaymentService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private modalService = inject(ModalService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.editMode = idParam != null && /^\d+$/.test(idParam);
            if (this.editMode) {
                this.id        = Number(idParam);
                this.subModule = 'Edit';
                this.loadForEdit();
            } else {
                this.subModule = 'Create';
            }
        });
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.datePaid           = data.datePaid ? new Date(data.datePaid).toISOString().substring(0, 10) : '';
                    this.description        = data.description || '';
                    this.startMonth         = (data.startMonth ?? 0) + 1;
                    this.startYear          = data.startYear  || new Date().getFullYear();
                    this.totalCost          = data.totalCost  || 0;
                    this.noOfMonths         = data.noOfMonths || 0;
                    this.monthlyCost        = data.monthlyCost || 0;
                    this.appliedCost        = data.appliedCost || 0;
                    this.balance            = data.balance    || 0;
                    this.prepaymentAccount  = data.prepaymentAccount || null;
                    this.expenseAccount     = data.expenseAccount    || null;
                    this.hasPpd             = !!data.hasPpd;
                } else {
                    this.alertService.error(this.module, 'Record not found.', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load record.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    onCostChange(): void {
        const total   = Number(this.totalCost)   || 0;
        const months  = Number(this.noOfMonths)  || 0;
        const applied = Number(this.appliedCost) || 0;
        this.monthlyCost = months > 0 ? total / months : 0;
        this.balance     = total - applied;
    }

    async openAccBrowse(field: string): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseCOAModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                if (field === 'prepaymentAccount') this.prepaymentAccount = result.data;
                else if (field === 'expenseAccount') this.expenseAccount = result.data;
            }
        } catch { }
    }

    save(): void {
        if (!this.datePaid) {
            this.alertService.warning(this.module, 'Validation', 'Please enter a date paid.');
            return;
        }
        if (!this.description?.trim()) {
            this.alertService.warning(this.module, 'Validation', 'Please enter a description.');
            return;
        }
        if (!(Number(this.totalCost) > 0)) {
            this.alertService.warning(this.module, 'Validation', 'Total cost must be greater than 0.');
            return;
        }
        if (!(Number(this.noOfMonths) > 0)) {
            this.alertService.warning(this.module, 'Validation', 'Number of months must be greater than 0.');
            return;
        }
        if (!this.prepaymentAccount) {
            this.alertService.warning(this.module, 'Validation', 'Please select a Prepayment Account.');
            return;
        }
        if (!this.expenseAccount) {
            this.alertService.warning(this.module, 'Validation', 'Please select an Expense Account.');
            return;
        }

        this.formSubmit = true;

        const total   = Number(this.totalCost)   || 0;
        const months  = Number(this.noOfMonths)  || 0;
        const applied = Number(this.appliedCost) || 0;

        const payload: any = {
            id:                 this.editMode ? this.id : null,
            datePaid:           this.datePaid,
            description:        this.description,
            prepaymentAccount:  { id: this.prepaymentAccount.id },
            expenseAccount:     { id: this.expenseAccount.id },
            totalCost:          total,
            noOfMonths:         months,
            monthlyCost:        months > 0 ? total / months : 0,
            appliedCost:        applied,
            balance:            total - applied,
            startMonth:         (Number(this.startMonth) || 1) - 1,
            startYear:          Number(this.startYear)  || 0
        };

        const req$ = this.editMode ? this.service.update(payload) : this.service.create(payload);
        req$.subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Saved successfully.', '');
                    this.router.navigate(['/' + this.menuLink, res.modelId, 'detail']);
                } else {
                    this.alertService.error(this.module, 'Save failed.', res?.failureMessage || '');
                }
            },
            error: () => {
                this.formSubmit = false;
                this.alertService.error(this.module, 'An error occurred while saving.', '');
            }
        });
    }
}
