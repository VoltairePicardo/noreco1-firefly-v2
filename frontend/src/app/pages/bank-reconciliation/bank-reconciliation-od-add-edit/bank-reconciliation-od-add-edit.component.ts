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
import { BankReconciliationService } from '../bank-reconciliation.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseCOAModalComponent } from '@/app/shared/modals/browse-coa-modal/browse-coa-modal.component';

@Component({
    selector: 'app-bank-reconciliation-od-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective
    ],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './bank-reconciliation-od-add-edit.component.html'
})
export class BankReconciliationOdAddEditComponent {
    module    = 'Bank Reconciliation';
    subModule = 'Create Other Deposit';
    menuLink  = 'bank-reconciliation';

    id: any   = null;
    editMode  = false;
    formSubmit = false;
    submit     = false;
    isLoading  = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // Form fields
    voucherDate  = '';
    particulars  = '';
    amount       = 0;
    account: any = null;

    private service      = inject(BankReconciliationService);
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
                this.subModule = 'Edit Other Deposit';
                this.loadForEdit();
            }
        });
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getOd(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.voucherDate = data.voucherDate ? new Date(data.voucherDate).toISOString().substring(0, 10) : '';
                    this.particulars = data.particulars || '';
                    this.amount      = data.amount   || 0;
                    this.account     = data.account  || null;
                } else {
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Error', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    async openAccBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseCOAModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this.account = result.data;
            }
        } catch { }
    }

    isValid(): boolean {
        return !!(this.voucherDate && this.particulars?.trim() && this.account);
    }

    save(): void {
        this.submit = true;
        if (!this.isValid()) return;

        this.formSubmit = true;
        const payload: any = {
            id:          this.editMode ? this.id : null,
            voucherDate: this.voucherDate,
            particulars: this.particulars.trim(),
            amount:      Number(this.amount) || 0,
            account:     { id: this.account.id },
            checkNumber: ''
        };

        const req$ = this.editMode ? this.service.updateOd(payload) : this.service.createOd(payload);
        req$.subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Saved', '');
                    this.router.navigate(['/' + this.menuLink]);
                } else {
                    this.alertService.error(this.module, 'Save Failed', res?.failureMessage || '');
                }
            },
            error: () => {
                this.formSubmit = false;
                this.alertService.error(this.module, 'Save Failed', '');
            }
        });
    }

    cancel(): void {
        this.router.navigate(['/' + this.menuLink]);
    }
}
