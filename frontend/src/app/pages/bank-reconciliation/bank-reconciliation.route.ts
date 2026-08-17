import { Routes } from '@angular/router';

const mainPath = '/bank-reconciliation';

export const BANK_RECONCILIATION_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./bank-reconciliation-main/bank-reconciliation-main.component').then(m => m.BankReconciliationMainComponent),
        data: { title: 'Bank Reconciliation', mainPath }
    },
    {
        path: 'od/create',
        loadComponent: () => import('./bank-reconciliation-od-add-edit/bank-reconciliation-od-add-edit.component').then(m => m.BankReconciliationOdAddEditComponent),
        data: { title: 'Create Other Deposit', mainPath }
    },
    {
        path: 'od/:id/edit',
        loadComponent: () => import('./bank-reconciliation-od-add-edit/bank-reconciliation-od-add-edit.component').then(m => m.BankReconciliationOdAddEditComponent),
        data: { title: 'Edit Other Deposit', mainPath }
    },
    {
        path: 'od/:id/detail',
        loadComponent: () => import('./bank-reconciliation-od-detail/bank-reconciliation-od-detail.component').then(m => m.BankReconciliationOdDetailComponent),
        data: { title: 'Other Deposit Detail', mainPath }
    },
];
