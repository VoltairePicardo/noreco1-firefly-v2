import { Routes } from '@angular/router';

const mainPath = '/budget-sub-item';

export const BUDGET_SUB_ITEM_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./budget-sub-item-main/budget-sub-item-main.component').then(m => m.BudgetSubItemMainComponent),
        data: { title: 'Budget Sub Item', mainPath }
    },
    {
        path: ':budgetLineItemDetailId/edit',
        loadComponent: () => import('./budget-sub-item-add-edit/budget-sub-item-add-edit.component').then(m => m.BudgetSubItemAddEditComponent),
        data: { title: 'Manage Budget Sub Items', mainPath }
    },
];
