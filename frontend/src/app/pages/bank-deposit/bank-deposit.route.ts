import { Routes } from '@angular/router';

export const BANK_DEPOSIT_ROUTES: Routes = [
    {
        path: '',
        children: [
            {
                path: '',
                loadComponent: () => import('./bank-deposit-main/bank-deposit-main.component').then(m => m.BankDepositMainComponent),
                data: { title: 'Bank Deposits' }
            },
            {
                path: 'create',
                loadComponent: () => import('./bank-deposit-add-edit/bank-deposit-add-edit.component').then(m => m.BankDepositAddEditComponent),
                data: { title: 'Create Bank Deposit' }
            },
            {
                path: ':id/edit',
                loadComponent: () => import('./bank-deposit-add-edit/bank-deposit-add-edit.component').then(m => m.BankDepositAddEditComponent),
                data: { title: 'Edit Bank Deposit' }
            },
            {
                path: ':id/detail',
                loadComponent: () => import('./bank-deposit-detail/bank-deposit-detail.component').then(m => m.BankDepositDetailComponent),
                data: { title: 'Bank Deposit Detail' }
            }
        ]
    }
];
