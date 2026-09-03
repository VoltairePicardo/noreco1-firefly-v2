import { Routes } from '@angular/router';

const mainPath = '/stock-adjustment';

export const STOCK_ADJUSTMENT_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./stock-adjustment-main/stock-adjustment-main.component').then(m => m.StockAdjustmentMainComponent),
        data: { title: 'Stock Adjustment', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./stock-adjustment-add-edit/stock-adjustment-add-edit.component').then(m => m.StockAdjustmentAddEditComponent),
        data: { title: 'Create Stock Adjustment', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./stock-adjustment-add-edit/stock-adjustment-add-edit.component').then(m => m.StockAdjustmentAddEditComponent),
        data: { title: 'Edit Stock Adjustment', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./stock-adjustment-detail/stock-adjustment-detail.component').then(m => m.StockAdjustmentDetailComponent),
        data: { title: 'Stock Adjustment Details', mainPath }
    },
];
