import { Routes } from '@angular/router';

const mainPath = '/bank-account';

export const BANK_ACCOUNT_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./bank-account-main/bank-account-main.component').then(m => m.BankAccountMainComponent),
        data: { title: 'Bank Accounts', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./bank-account-add-edit/bank-account-add-edit.component').then(m => m.BankAccountAddEditComponent),
        data: { title: 'Create Bank Account', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./bank-account-add-edit/bank-account-add-edit.component').then(m => m.BankAccountAddEditComponent),
        data: { title: 'Edit Bank Account', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./bank-account-details/bank-account-details.component').then(m => m.BankAccountDetailsComponent),
        data: { title: 'Bank Account Details', mainPath }
    },
];
