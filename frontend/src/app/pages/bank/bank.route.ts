import { Routes } from '@angular/router';

const mainPath = '/bank';

export const BANK_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./bank-main/bank-main.component').then(m => m.BankMainComponent),
        data: { title: 'Banks', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./bank-add-edit/bank-add-edit.component').then(m => m.BankAddEditComponent),
        data: { title: 'Create Bank', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./bank-add-edit/bank-add-edit.component').then(m => m.BankAddEditComponent),
        data: { title: 'Edit Bank', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./bank-details/bank-details.component').then(m => m.BankDetailsComponent),
        data: { title: 'Bank Details', mainPath }
    },
];
