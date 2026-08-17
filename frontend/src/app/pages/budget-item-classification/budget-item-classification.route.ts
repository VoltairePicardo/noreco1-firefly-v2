import { Routes } from '@angular/router';

const mainPath = '/budget-item-classification';

export const BUDGET_ITEM_CLASSIFICATION_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./budget-item-classification-main/budget-item-classification-main.component').then(m => m.BudgetItemClassificationMainComponent),
        data: { title: 'Budget Item Classifications', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./budget-item-classification-add-edit/budget-item-classification-add-edit.component').then(m => m.BudgetItemClassificationAddEditComponent),
        data: { title: 'Create Budget Item Classification', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./budget-item-classification-add-edit/budget-item-classification-add-edit.component').then(m => m.BudgetItemClassificationAddEditComponent),
        data: { title: 'Edit Budget Item Classification', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./budget-item-classification-details/budget-item-classification-details.component').then(m => m.BudgetItemClassificationDetailsComponent),
        data: { title: 'Budget Item Classification Details', mainPath }
    },
];
