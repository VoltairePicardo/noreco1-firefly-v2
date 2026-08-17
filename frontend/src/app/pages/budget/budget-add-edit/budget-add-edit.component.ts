import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { BudgetService } from '../budget.service';

@Component({
    selector: 'app-budget-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
    ],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './budget-add-edit.component.html'
})
export class BudgetAddEditComponent {
    module    = 'Cash Flow Budget';
    subModule = 'Create';
    menuLink  = 'budget';

    id: any    = null;
    editMode   = false;
    formSubmit = false;
    isLoading  = signal(false);

    // Form fields
    year = '';

    // Budget details: { cashflowItem: any, amount: number }
    budgetDetails: any[] = [];

    // Lookup data
    cashflowItems = signal<any[]>([]);

    private service      = inject(BudgetService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.loadCashflowItems();
        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.editMode = idParam != null && /^\d+$/.test(idParam);
            if (this.editMode) {
                this.id        = Number(idParam);
                this.subModule = 'Edit';
                this.loadForEdit();
            } else {
                this.subModule = 'Create';
                this.addDetail();
            }
        });
    }

    loadCashflowItems(): void {
        this.service.getCashflowItems().subscribe({
            next: (data) => this.cashflowItems.set(data || []),
            error: () => {}
        });
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                if (data?.id) {
                    this.year = data.year ? String(data.year) : '';
                    this.loadDetailsForEdit();
                } else {
                    this.isLoading.set(false);
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

    loadDetailsForEdit(): void {
        this.service.getDetails(this.id).subscribe({
            next: (details) => {
                this.isLoading.set(false);
                this.budgetDetails = (details || []).map((d: any) => ({
                    cashflowItem: d.cashflowItem || null,
                    amount: d.amount || 0
                }));
                if (this.budgetDetails.length === 0) this.addDetail();
            },
            error: () => {
                this.isLoading.set(false);
                this.addDetail();
            }
        });
    }

    addDetail(): void {
        this.budgetDetails.push({ cashflowItem: null, amount: 0 });
    }

    removeDetail(index: number): void {
        this.budgetDetails.splice(index, 1);
    }

    get totalAmount(): number {
        return this.budgetDetails.reduce((sum, d) => sum + (Number(d.amount) || 0), 0);
    }

    compareFn(a: any, b: any): boolean {
        return a && b ? a.id === b.id : a === b;
    }

    trackByIndex(index: number): number {
        return index;
    }

    save(): void {
        if (!this.year || isNaN(Number(this.year))) {
            this.alertService.warning(this.module, 'Validation', 'Please enter a valid year.');
            return;
        }
        if (this.budgetDetails.length === 0) {
            this.alertService.warning(this.module, 'Validation', 'Please add at least one budget detail.');
            return;
        }
        const invalidDetail = this.budgetDetails.find(d => !d.cashflowItem || Number(d.amount) <= 0);
        if (invalidDetail) {
            this.alertService.warning(this.module, 'Validation', 'All details must have a cashflow item and an amount greater than 0.');
            return;
        }

        this.formSubmit = true;

        const payload: any = {
            id:            this.editMode ? this.id : null,
            year:          Number(this.year),
            budgetDetails: this.budgetDetails.map(d => ({
                cashflowItem: { id: d.cashflowItem?.id },
                amount:       Number(d.amount) || 0
            }))
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
