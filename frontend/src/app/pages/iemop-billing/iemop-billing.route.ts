import { Routes } from '@angular/router';

const mainPath = '/iemop-billing';

export const IEMOP_BILLING_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./iemop-billing-main/iemop-billing-main.component').then(m => m.IemopBillingMainComponent),
        data: { title: 'Upload IEMOP Billing', mainPath }
    },
    {
        path: 'upload',
        loadComponent: () => import('./iemop-billing-upload/iemop-billing-upload.component').then(m => m.IemopBillingUploadComponent),
        data: { title: 'Upload IEMOP Billing', mainPath }
    },
    {
        path: ':id/details',
        loadComponent: () => import('./iemop-billing-details/iemop-billing-details.component').then(m => m.IemopBillingDetailsComponent),
        data: { title: 'IEMOP Billing Details', mainPath }
    },
];
