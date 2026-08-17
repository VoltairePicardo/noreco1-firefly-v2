import { Routes } from '@angular/router';

const mainPath = '/check-config';

export const CHECK_CONFIG_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./check-config-main/check-config-main.component').then(m => m.CheckConfigMainComponent),
        data: { title: 'Check Configs', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./check-config-add-edit/check-config-add-edit.component').then(m => m.CheckConfigAddEditComponent),
        data: { title: 'Create Check Config', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./check-config-add-edit/check-config-add-edit.component').then(m => m.CheckConfigAddEditComponent),
        data: { title: 'Edit Check Config', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./check-config-details/check-config-details.component').then(m => m.CheckConfigDetailsComponent),
        data: { title: 'Check Config Details', mainPath }
    },
];
