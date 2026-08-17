import { Routes } from '@angular/router';

const mainPath = '/rr';

export const RR_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./rr-main/rr-main.component').then(m => m.RrMainComponent),
        data: { title: 'Receiving Reports', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./rr-add-edit/rr-add-edit.component').then(m => m.RrAddEditComponent),
        data: { title: 'Create Receiving Report', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./rr-add-edit/rr-add-edit.component').then(m => m.RrAddEditComponent),
        data: { title: 'Edit Receiving Report', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./rr-detail/rr-detail.component').then(m => m.RrDetailComponent),
        data: { title: 'Receiving Report Details', mainPath }
    },
];
