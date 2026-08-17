import { Routes } from '@angular/router';

const mainPath = '/pcv';

export const PCV_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./pcv-main/pcv-main.component').then(m => m.PcvMainComponent),
        data: { title: 'Petty Cash Voucher', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./pcv-add-edit/pcv-add-edit.component').then(m => m.PcvAddEditComponent),
        data: { title: 'Create Petty Cash Voucher', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./pcv-add-edit/pcv-add-edit.component').then(m => m.PcvAddEditComponent),
        data: { title: 'Edit Petty Cash Voucher', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./pcv-detail/pcv-detail.component').then(m => m.PcvDetailComponent),
        data: { title: 'Petty Cash Voucher Detail', mainPath }
    },
    {
        path: 'closeout',
        loadComponent: () => import('./pcv-closeout/pcv-closeout.component').then(m => m.PcvCloseoutComponent),
        data: { title: 'PCV Closeout', mainPath }
    },
];
