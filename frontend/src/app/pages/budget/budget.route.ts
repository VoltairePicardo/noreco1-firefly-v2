import { Routes } from '@angular/router';

const mainPath = '/budget';

export const BUDGET_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./budget-main/budget-main.component').then(m => m.BudgetMainComponent),
        data: { title: 'Cash Flow Budget', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./budget-add-edit/budget-add-edit.component').then(m => m.BudgetAddEditComponent),
        data: { title: 'Create Cash Flow Budget', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./budget-add-edit/budget-add-edit.component').then(m => m.BudgetAddEditComponent),
        data: { title: 'Edit Cash Flow Budget', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./budget-detail/budget-detail.component').then(m => m.BudgetDetailComponent),
        data: { title: 'Cash Flow Budget Detail', mainPath }
    },
];
