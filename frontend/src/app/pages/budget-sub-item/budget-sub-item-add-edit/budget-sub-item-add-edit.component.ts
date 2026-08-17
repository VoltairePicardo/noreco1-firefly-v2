import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { BudgetSubItemService } from '../budget-sub-item.service';

@Component({
    selector: 'app-budget-sub-item-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS
    ],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './budget-sub-item-add-edit.component.html'
})
export class BudgetSubItemAddEditComponent {
    module    = 'Budget Sub Item';
    subModule = 'Manage Sub Items';
    menuLink  = 'budget-sub-item';

    budgetLineItemDetailId: any = null;
    parentInfo: any = null;
    items: { id?: number; description: string; amount: number }[] = [];

    submit     = false;
    formSubmit = false;
    isLoading  = signal(false);

    private service      = inject(BudgetSubItemService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        // Read parent info from router state (passed from main page)
        this.parentInfo = history.state || null;

        this.route.paramMap.subscribe(params => {
            const idParam = params.get('budgetLineItemDetailId');
            if (idParam && /^\d+$/.test(idParam)) {
                this.budgetLineItemDetailId = Number(idParam);
                this.loadSubItems();
            } else {
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    loadSubItems(): void {
        this.isLoading.set(true);
        this.service.getSubItems(this.budgetLineItemDetailId).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                this.items = (data || []).map((item: any) => ({
                    id:          item.id,
                    description: item.description || '',
                    amount:      Number(item.amount) || 0
                }));
                if (this.items.length === 0) {
                    this.addRow();
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.addRow();
            }
        });
    }

    addRow(): void {
        this.items.push({ description: '', amount: 0 });
    }

    removeRow(index: number): void {
        if (this.items.length > 1) {
            this.items.splice(index, 1);
        }
    }

    get totalAmount(): number {
        return this.items.reduce((sum, item) => sum + (Number(item.amount) || 0), 0);
    }

    get projectCost(): number {
        return Number(this.parentInfo?.projectCost) || 0;
    }

    get totalMatchesProjectCost(): boolean {
        return Math.abs(this.totalAmount - this.projectCost) < 0.005;
    }

    get hasEmptyDescriptions(): boolean {
        return this.items.some(item => !item.description?.trim());
    }

    isValid(): boolean {
        if (this.items.length === 0) return false;
        const hasEmpty = this.items.some(item => !item.description?.trim());
        if (hasEmpty) return false;
        if (this.projectCost > 0 && !this.totalMatchesProjectCost) return false;
        return true;
    }

    save(): void {
        this.submit = true;
        if (!this.isValid()) return;

        this.formSubmit = true;
        const payload = {
            budgetLineItemDetail: { id: this.budgetLineItemDetailId },
            budgetSubItems: this.items.map(item => ({
                id:          item.id || null,
                description: item.description.trim(),
                amount:      Number(item.amount) || 0
            }))
        };

        this.service.create(payload).subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Budget sub items saved successfully.', '');
                    this.router.navigate(['/' + this.menuLink]);
                } else {
                    this.alertService.error(this.module, 'Save failed.', res?.failureMessage || '');
                }
            },
            error: () => {
                this.formSubmit = false;
                this.alertService.error(this.module, 'An error occurred.', '');
            }
        });
    }

    cancel(): void {
        this.router.navigate(['/' + this.menuLink]);
    }
}
