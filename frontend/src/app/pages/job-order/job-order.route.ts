import { Routes } from '@angular/router';

export const JOB_ORDER_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./job-order-main/job-order-main.component').then(m => m.JobOrderMainComponent),
        data: { title: 'Job Orders' }
    },
    {
        path: 'create',
        loadComponent: () => import('./job-order-add-edit/job-order-add-edit.component').then(m => m.JobOrderAddEditComponent),
        data: { title: 'Create Job Order' }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./job-order-add-edit/job-order-add-edit.component').then(m => m.JobOrderAddEditComponent),
        data: { title: 'Edit Job Order' }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./job-order-detail/job-order-detail.component').then(m => m.JobOrderDetailComponent),
        data: { title: 'Job Order Details' }
    }
];
