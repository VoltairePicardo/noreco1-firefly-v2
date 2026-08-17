import { Routes } from '@angular/router';

const mainPath = '/requisition-voucher';

export const REQUISITION_VOUCHER_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./requisition-voucher-main/requisition-voucher-main.component').then(m => m.RequisitionVoucherMainComponent),
        data: { title: 'Purchase/Work Requests', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./requisition-voucher-add-edit/requisition-voucher-add-edit.component').then(m => m.RequisitionVoucherAddEditComponent),
        data: { title: 'Create Purchase/Work Request', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./requisition-voucher-add-edit/requisition-voucher-add-edit.component').then(m => m.RequisitionVoucherAddEditComponent),
        data: { title: 'Edit Purchase/Work Request', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./requisition-voucher-details/requisition-voucher-details.component').then(m => m.RequisitionVoucherDetailsComponent),
        data: { title: 'Purchase/Work Request Details', mainPath }
    },
];
