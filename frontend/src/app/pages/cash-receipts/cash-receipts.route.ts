import { Routes } from '@angular/router';

export const CASH_RECEIPTS_ROUTES: Routes = [
    {
        path: '',
        children: [
            {
                path: '',
                loadComponent: () => import('./cash-receipts-main/cash-receipts-main.component').then(m => m.CashReceiptsMainComponent),
                data: { title: 'Cash Receipts' }
            },
            {
                path: 'create',
                loadComponent: () => import('./cash-receipts-add-edit/cash-receipts-add-edit.component').then(m => m.CashReceiptsAddEditComponent),
                data: { title: 'Create Cash Receipts' }
            },
            {
                path: ':id/edit',
                loadComponent: () => import('./cash-receipts-add-edit/cash-receipts-add-edit.component').then(m => m.CashReceiptsAddEditComponent),
                data: { title: 'Edit Cash Receipts' }
            },
            {
                path: ':id/detail',
                loadComponent: () => import('./cash-receipts-detail/cash-receipts-detail.component').then(m => m.CashReceiptsDetailComponent),
                data: { title: 'Cash Receipts Detail' }
            }
        ]
    }
];
