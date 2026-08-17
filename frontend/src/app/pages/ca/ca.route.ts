import { Routes } from '@angular/router';

const mainPath = '/ca';

export const CA_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./ca-main/ca-main.component').then(m => m.CaMainComponent),
        data: { title: 'Cash Advance', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./ca-add-edit/ca-add-edit.component').then(m => m.CaAddEditComponent),
        data: { title: 'Create Cash Advance', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./ca-add-edit/ca-add-edit.component').then(m => m.CaAddEditComponent),
        data: { title: 'Edit Cash Advance', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./ca-detail/ca-detail.component').then(m => m.CaDetailComponent),
        data: { title: 'Cash Advance Detail', mainPath }
    },
];
