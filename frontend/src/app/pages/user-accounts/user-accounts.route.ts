import { Routes } from '@angular/router';

const mainPath = '/user-accounts';

export const USER_ACCOUNTS_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./user-accounts-main/user-accounts-main.component').then(m => m.UserAccountsMainComponent),
        data: { title: 'User Accounts', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./user-accounts-add-edit/user-accounts-add-edit.component').then(m => m.UserAccountsAddEditComponent),
        data: { title: 'Create User Account', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./user-accounts-add-edit/user-accounts-add-edit.component').then(m => m.UserAccountsAddEditComponent),
        data: { title: 'Edit User Account', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./user-accounts-details/user-accounts-details.component').then(m => m.UserAccountsDetailsComponent),
        data: { title: 'User Account Details', mainPath }
    },
];
