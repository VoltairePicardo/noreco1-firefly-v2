import { Routes } from '@angular/router';

const mainPath = '/budget-line-item';

export const BUDGET_LINE_ITEM_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./budget-line-item-main/budget-line-item-main.component').then(m => m.BudgetLineItemMainComponent),
        data: { title: 'Budget Line Item', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./budget-line-item-add-edit/budget-line-item-add-edit.component').then(m => m.BudgetLineItemAddEditComponent),
        data: { title: 'Create Budget Line Item', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./budget-line-item-add-edit/budget-line-item-add-edit.component').then(m => m.BudgetLineItemAddEditComponent),
        data: { title: 'Edit Budget Line Item', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./budget-line-item-detail/budget-line-item-detail.component').then(m => m.BudgetLineItemDetailComponent),
        data: { title: 'Budget Line Item Details', mainPath }
    },
];
