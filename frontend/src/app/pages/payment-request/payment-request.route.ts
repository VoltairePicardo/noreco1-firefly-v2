import { Routes } from '@angular/router';

export const PAYMENT_REQUEST_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./payment-request-main/payment-request-main.component').then(m => m.PaymentRequestMainComponent),
        data: { title: 'Payment Request' }
    },
    {
        path: 'create',
        loadComponent: () => import('./payment-request-add-edit/payment-request-add-edit.component').then(m => m.PaymentRequestAddEditComponent),
        data: { title: 'Create Payment Request' }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./payment-request-add-edit/payment-request-add-edit.component').then(m => m.PaymentRequestAddEditComponent),
        data: { title: 'Edit Payment Request' }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./payment-request-detail/payment-request-detail.component').then(m => m.PaymentRequestDetailComponent),
        data: { title: 'Payment Request Detail' }
    },
];
