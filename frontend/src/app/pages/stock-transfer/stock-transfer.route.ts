import { Routes } from '@angular/router';

const mainPath = '/stock-transfer';

export const STOCK_TRANSFER_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./stock-transfer-main/stock-transfer-main.component').then(m => m.StockTransferMainComponent),
        data: { title: 'Stock Transfer', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./stock-transfer-add-edit/stock-transfer-add-edit.component').then(m => m.StockTransferAddEditComponent),
        data: { title: 'Create Stock Transfer', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./stock-transfer-add-edit/stock-transfer-add-edit.component').then(m => m.StockTransferAddEditComponent),
        data: { title: 'Edit Stock Transfer', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./stock-transfer-detail/stock-transfer-detail.component').then(m => m.StockTransferDetailComponent),
        data: { title: 'Stock Transfer Details', mainPath }
    },
];
