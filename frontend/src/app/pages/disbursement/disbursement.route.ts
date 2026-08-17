import { Routes } from '@angular/router';

const mainPath = '/disbursement';

export const DISBURSEMENT_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./disbursement-main/disbursement-main.component').then(m => m.DisbursementMainComponent),
        data: { title: 'Disbursement', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./disbursement-add-edit/disbursement-add-edit.component').then(m => m.DisbursementAddEditComponent),
        data: { title: 'Create Disbursement', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./disbursement-add-edit/disbursement-add-edit.component').then(m => m.DisbursementAddEditComponent),
        data: { title: 'Edit Disbursement', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./disbursement-detail/disbursement-detail.component').then(m => m.DisbursementDetailComponent),
        data: { title: 'Disbursement Detail', mainPath }
    },
];
