import { Routes } from '@angular/router';

const mainPath = '/stock-release';

export const STOCK_RELEASE_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./stock-release-main/stock-release-main.component').then(m => m.StockReleaseMainComponent),
        data: { title: 'Stock Releases', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./stock-release-add-edit/stock-release-add-edit.component').then(m => m.StockReleaseAddEditComponent),
        data: { title: 'Create Stock Release', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./stock-release-add-edit/stock-release-add-edit.component').then(m => m.StockReleaseAddEditComponent),
        data: { title: 'Edit Stock Release', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./stock-release-detail/stock-release-detail.component').then(m => m.StockReleaseDetailComponent),
        data: { title: 'Stock Release Details', mainPath }
    },
];
