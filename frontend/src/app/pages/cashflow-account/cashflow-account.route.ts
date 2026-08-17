import { Routes } from '@angular/router';

const mainPath = '/cashflow-account';

export const CASHFLOW_ACCOUNT_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./cashflow-account-main/cashflow-account-main.component').then(m => m.CashflowAccountMainComponent),
        data: { title: 'Cash Flow Accounts', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./cashflow-account-add-edit/cashflow-account-add-edit.component').then(m => m.CashflowAccountAddEditComponent),
        data: { title: 'Create Cash Flow Account', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./cashflow-account-add-edit/cashflow-account-add-edit.component').then(m => m.CashflowAccountAddEditComponent),
        data: { title: 'Edit Cash Flow Account', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./cashflow-account-details/cashflow-account-details.component').then(m => m.CashflowAccountDetailsComponent),
        data: { title: 'Cash Flow Account Details', mainPath }
    },
];
