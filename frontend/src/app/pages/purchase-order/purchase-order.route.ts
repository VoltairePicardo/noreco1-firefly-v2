import { Routes } from '@angular/router';

export const PURCHASE_ORDER_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./purchase-order-main/purchase-order-main.component').then(m => m.PurchaseOrderMainComponent),
        data: { title: 'Purchase Orders' }
    },
    {
        path: 'create',
        loadComponent: () => import('./purchase-order-add-edit/purchase-order-add-edit.component').then(m => m.PurchaseOrderAddEditComponent),
        data: { title: 'Create Purchase Order' }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./purchase-order-add-edit/purchase-order-add-edit.component').then(m => m.PurchaseOrderAddEditComponent),
        data: { title: 'Edit Purchase Order' }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./purchase-order-detail/purchase-order-detail.component').then(m => m.PurchaseOrderDetailComponent),
        data: { title: 'Purchase Order Details' }
    }
];
