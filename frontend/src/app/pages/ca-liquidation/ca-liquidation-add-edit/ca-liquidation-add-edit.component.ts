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
import { CaLiquidationService } from '../ca-liquidation.service';

@Component({
    selector: 'app-ca-liquidation-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective
    ],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './ca-liquidation-add-edit.component.html'
})
export class CaLiquidationAddEditComponent {
    module    = 'CA Liquidation';
    subModule = 'Create';
    menuLink  = 'ca-liquidation';

    id: any    = null;
    editMode   = false;
    formSubmit = false;
    submit     = false;
    isLoading  = signal(false);
    loadingItems = false;

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    voucherDate         = '';
    remarks             = '';
    totalReturnedAmount = 0;

    cashAdvance: any    = null;
    cashAdvances: any[] = [];

    items: { cashAdvanceParticularId: number; particular: string; orNumber: string; amount: number }[] = [];

    private service      = inject(CaLiquidationService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.loadCashAdvances();
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

    loadCashAdvances(): void {
        this.service.getCashAdvanceList().subscribe({
            next: (data) => { this.cashAdvances = (data || []).filter((ca: any) => !ca.isLiquidated); },
            error: () => { this.cashAdvances = []; }
        });
    }

    onCashAdvanceChange(): void {
        this.items = [];
        if (this.cashAdvance?.id) {
            this.loadingItems = true;
            const calId = this.editMode ? (this.id || 0) : 0;
            this.service.getParticularsForLiquidation(this.cashAdvance.id, calId).subscribe({
                next: (data) => {
                    this.loadingItems = false;
                    this.items = (data || []).map((p: any) => ({
                        cashAdvanceParticularId: p.id,
                        particular: p.particular || '',
                        orNumber:   '',
                        amount:     p.amount || 0
                    }));
                },
                error: () => { this.loadingItems = false; }
            });
        }
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.voucherDate         = data.voucherDate ? new Date(data.voucherDate).toISOString().substring(0, 10) : '';
                    this.remarks             = data.remarks || '';
                    this.totalReturnedAmount = data.totalReturnedAmount || 0;
                    this.cashAdvance         = data.cashAdvance || null;
                    if (this.cashAdvance?.id) {
                        this.loadingItems = true;
                        this.service.getParticularsForLiquidation(this.cashAdvance.id, this.id).subscribe({
                            next: (particulars) => {
                                this.loadingItems = false;
                                const existingItems = data.cashAdvanceLiquidationItems || [];
                                this.items = (particulars || []).map((p: any) => {
                                    const existing = existingItems.find((item: any) =>
                                        item.cashAdvanceParticular?.id === p.id);
                                    return {
                                        cashAdvanceParticularId: p.id,
                                        particular: p.particular || '',
                                        orNumber:   existing?.orNumber || '',
                                        amount:     existing?.amount   || p.amount || 0
                                    };
                                });
                            },
                            error: () => { this.loadingItems = false; }
                        });
                    }
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

    get totalAmount(): number {
        return this.items.reduce((sum, item) => sum + (Number(item.amount) || 0), 0);
    }

    isValid(): boolean {
        return !!(this.voucherDate && this.cashAdvance);
    }

    save(): void {
        this.submit = true;
        if (!this.isValid()) return;

        this.formSubmit = true;
        const payload: any = {
            id:          this.editMode ? this.id : null,
            voucherDate: this.voucherDate,
            remarks:     this.remarks?.trim() || null,
            cashAdvance: { id: this.cashAdvance.id },
            totalReturnedAmount: Number(this.totalReturnedAmount) || 0,
            cashAdvanceLiquidationItems: this.items.map(item => ({
                cashAdvanceParticular: { id: item.cashAdvanceParticularId },
                orNumber: item.orNumber?.trim() || null,
                amount:   Number(item.amount) || 0
            }))
        };

        const req$ = this.editMode ? this.service.update(payload) : this.service.create(payload);
        req$.subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Saved successfully.', '');
                    this.router.navigate(['/' + this.menuLink]);
                } else {
                    this.alertService.error(this.module, 'Save failed.', res?.failureMessage || '');
                }
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'An error occurred.', ''); }
        });
    }

    cancel(): void {
        this.router.navigate(['/' + this.menuLink]);
    }

    compareFn(a: any, b: any): boolean {
        return a && b ? a.id === b.id : a === b;
    }
}
