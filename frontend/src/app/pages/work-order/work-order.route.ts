import { Routes } from '@angular/router';

const mainPath = '/work-order';

export const WORK_ORDER_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./work-order-main/work-order-main.component').then(m => m.WorkOrderMainComponent),
        data: { title: 'Work Orders', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./work-order-add-edit/work-order-add-edit.component').then(m => m.WorkOrderAddEditComponent),
        data: { title: 'Create Work Order', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./work-order-add-edit/work-order-add-edit.component').then(m => m.WorkOrderAddEditComponent),
        data: { title: 'Edit Work Order', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./work-order-detail/work-order-detail.component').then(m => m.WorkOrderDetailComponent),
        data: { title: 'Work Order Details', mainPath }
    },
    {
        path: 'posting',
        loadComponent: () => import('./work-order-posting/work-order-posting.component').then(m => m.WorkOrderPostingComponent),
        data: { title: 'Work Order Posting', mainPath }
    },
];
