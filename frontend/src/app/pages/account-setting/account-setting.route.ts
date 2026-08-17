import { Routes } from '@angular/router';

const mainPath = '/account-setting';

export const ACCOUNT_SETTING_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./account-setting-main/account-setting-main.component').then(m => m.AccountSettingMainComponent),
        data: { title: 'Account Settings', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./account-setting-add-edit/account-setting-add-edit.component').then(m => m.AccountSettingAddEditComponent),
        data: { title: 'Create Account Setting', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./account-setting-add-edit/account-setting-add-edit.component').then(m => m.AccountSettingAddEditComponent),
        data: { title: 'Edit Account Setting', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./account-setting-detail/account-setting-detail.component').then(m => m.AccountSettingDetailComponent),
        data: { title: 'Account Setting Detail', mainPath }
    },
];
