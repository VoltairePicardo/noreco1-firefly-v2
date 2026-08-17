import { Routes } from '@angular/router';

const mainPath = '/coa';

export const COA_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./coa-main/coa-main.component').then(m => m.CoaMainComponent),
        data: { title: 'Chart of Accounts', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./coa-add-edit/coa-add-edit.component').then(m => m.CoaAddEditComponent),
        data: { title: 'Create Account', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./coa-add-edit/coa-add-edit.component').then(m => m.CoaAddEditComponent),
        data: { title: 'Edit Account', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./coa-details/coa-details.component').then(m => m.CoaDetailsComponent),
        data: { title: 'Account Details', mainPath }
    },
];
