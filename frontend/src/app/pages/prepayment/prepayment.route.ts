import { Routes } from '@angular/router';

const mainPath = '/prepayment';

export const PREPAYMENT_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./prepayment-main/prepayment-main.component').then(m => m.PrepaymentMainComponent),
        data: { title: 'Prepayments and Other Amortizations', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./prepayment-add-edit/prepayment-add-edit.component').then(m => m.PrepaymentAddEditComponent),
        data: { title: 'Create Prepayment', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./prepayment-add-edit/prepayment-add-edit.component').then(m => m.PrepaymentAddEditComponent),
        data: { title: 'Edit Prepayment', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./prepayment-detail/prepayment-detail.component').then(m => m.PrepaymentDetailComponent),
        data: { title: 'Prepayment Detail', mainPath }
    },
    {
        path: 'process-monthly',
        loadComponent: () => import('./prepayment-process-monthly/prepayment-process-monthly.component').then(m => m.PrepaymentProcessMonthlyComponent),
        data: { title: 'Process Monthly Expense', mainPath }
    },
];
