import { Routes } from '@angular/router';

const mainPath = '/ifr';

export const IFR_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./ifr-main/ifr-main.component').then(m => m.IfrMainComponent),
        data: { title: 'Items For Repair', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./ifr-add-edit/ifr-add-edit.component').then(m => m.IfrAddEditComponent),
        data: { title: 'Create Items For Repair', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./ifr-add-edit/ifr-add-edit.component').then(m => m.IfrAddEditComponent),
        data: { title: 'Edit Items For Repair', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./ifr-detail/ifr-detail.component').then(m => m.IfrDetailComponent),
        data: { title: 'Items For Repair Details', mainPath }
    },
];
