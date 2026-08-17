import { Routes } from '@angular/router';

const mainPath = '/other-account-receivable';

export const OTHER_ACCOUNT_RECEIVABLE_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./other-account-receivable-main/other-account-receivable-main.component').then(m => m.OtherAccountReceivableMainComponent),
        data: { title: 'Other Account Receivable', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./other-account-receivable-add-edit/other-account-receivable-add-edit.component').then(m => m.OtherAccountReceivableAddEditComponent),
        data: { title: 'Create Other Account Receivable', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./other-account-receivable-add-edit/other-account-receivable-add-edit.component').then(m => m.OtherAccountReceivableAddEditComponent),
        data: { title: 'Edit Other Account Receivable', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./other-account-receivable-detail/other-account-receivable-detail.component').then(m => m.OtherAccountReceivableDetailComponent),
        data: { title: 'Other Account Receivable Detail', mainPath }
    },
];
