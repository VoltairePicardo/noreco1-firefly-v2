import { Routes } from '@angular/router';

const mainPath = '/return-memorandum-receipt';

export const RETURN_MEMORANDUM_RECEIPT_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./return-memorandum-receipt-main/return-memorandum-receipt-main.component').then(m => m.ReturnMemorandumReceiptMainComponent),
        data: { title: 'Return Memorandum Receipt', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./return-memorandum-receipt-add-edit/return-memorandum-receipt-add-edit.component').then(m => m.ReturnMemorandumReceiptAddEditComponent),
        data: { title: 'Create Return Memorandum Receipt', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./return-memorandum-receipt-add-edit/return-memorandum-receipt-add-edit.component').then(m => m.ReturnMemorandumReceiptAddEditComponent),
        data: { title: 'Edit Return Memorandum Receipt', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./return-memorandum-receipt-detail/return-memorandum-receipt-detail.component').then(m => m.ReturnMemorandumReceiptDetailComponent),
        data: { title: 'Return Memorandum Receipt Details', mainPath }
    },
];
