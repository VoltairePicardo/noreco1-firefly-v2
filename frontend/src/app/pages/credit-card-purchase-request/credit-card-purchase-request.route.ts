import { Routes } from '@angular/router';

export const CREDIT_CARD_PURCHASE_REQUEST_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./credit-card-purchase-request-main/credit-card-purchase-request-main.component').then(m => m.CreditCardPurchaseRequestMainComponent),
        data: { title: 'Credit Card Purchase Request' }
    },
    {
        path: 'create',
        loadComponent: () => import('./credit-card-purchase-request-add-edit/credit-card-purchase-request-add-edit.component').then(m => m.CreditCardPurchaseRequestAddEditComponent),
        data: { title: 'Create Credit Card Purchase Request' }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./credit-card-purchase-request-add-edit/credit-card-purchase-request-add-edit.component').then(m => m.CreditCardPurchaseRequestAddEditComponent),
        data: { title: 'Edit Credit Card Purchase Request' }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./credit-card-purchase-request-detail/credit-card-purchase-request-detail.component').then(m => m.CreditCardPurchaseRequestDetailComponent),
        data: { title: 'Credit Card Purchase Request Detail' }
    },
];
