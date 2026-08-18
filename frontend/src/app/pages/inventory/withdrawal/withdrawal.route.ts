import { Routes } from '@angular/router';

const mainPath = '/withdrawal';

export const WITHDRAWAL_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./withdrawal-main/withdrawal-main.component').then(m => m.WithdrawalMainComponent),
        data: { title: 'Stock Withdrawals', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./withdrawal-add-edit/withdrawal-add-edit.component').then(m => m.WithdrawalAddEditComponent),
        data: { title: 'Create Stock Withdrawal', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./withdrawal-add-edit/withdrawal-add-edit.component').then(m => m.WithdrawalAddEditComponent),
        data: { title: 'Edit Stock Withdrawal', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./withdrawal-detail/withdrawal-detail.component').then(m => m.WithdrawalDetailComponent),
        data: { title: 'Stock Withdrawal Details', mainPath }
    },
];
