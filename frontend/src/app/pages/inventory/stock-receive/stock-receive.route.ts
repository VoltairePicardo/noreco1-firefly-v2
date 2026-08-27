import { Routes } from '@angular/router';

const mainPath = '/stock-receive';

export const STOCK_RECEIVE_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./stock-receive-main/stock-receive-main.component').then(m => m.StockReceiveMainComponent),
        data: { title: 'Receive Stock Transfer', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./stock-receive-add-edit/stock-receive-add-edit.component').then(m => m.StockReceiveAddEditComponent),
        data: { title: 'Create Receive Stock Transfer', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./stock-receive-add-edit/stock-receive-add-edit.component').then(m => m.StockReceiveAddEditComponent),
        data: { title: 'Edit Receive Stock Transfer', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./stock-receive-detail/stock-receive-detail.component').then(m => m.StockReceiveDetailComponent),
        data: { title: 'Receive Stock Transfer Details', mainPath }
    },
];
