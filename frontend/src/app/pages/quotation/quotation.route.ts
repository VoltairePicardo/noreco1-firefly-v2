import { Routes } from '@angular/router';

const mainPath = '/quotation';

export const QUOTATION_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('@/app/pages/quotation/quotation-main/quotation-main.component').then(m => m.QuotationMainComponent),
        data: { title: 'Quotations', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./quotation-add-edit/quotation-add-edit.component').then(m => m.QuotationAddEditComponent),
        data: { title: 'Create Quotation', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./quotation-add-edit/quotation-add-edit.component').then(m => m.QuotationAddEditComponent),
        data: { title: 'Edit Quotation', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./quotation-detail/quotation-detail.component').then(m => m.QuotationDetailComponent),
        data: { title: 'Quotation Detail', mainPath }
    },
];
