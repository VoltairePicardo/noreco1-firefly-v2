import { Routes } from '@angular/router';

const mainPath = '/cpr';

export const CPR_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./cpr-main/cpr-main.component').then(m => m.CprMainComponent),
        data: { title: 'Asset Records (CPR)', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./cpr-add-edit/cpr-add-edit.component').then(m => m.CprAddEditComponent),
        data: { title: 'Create CPR', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./cpr-add-edit/cpr-add-edit.component').then(m => m.CprAddEditComponent),
        data: { title: 'Edit CPR', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./cpr-detail/cpr-detail.component').then(m => m.CprDetailComponent),
        data: { title: 'CPR Detail', mainPath }
    },
    {
        path: ':id/link',
        loadComponent: () => import('./cpr-link-voucher/cpr-link-voucher.component').then(m => m.CprLinkVoucherComponent),
        data: { title: 'Link Voucher to CPR', mainPath }
    },
];
