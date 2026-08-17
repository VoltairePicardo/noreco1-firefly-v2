import { Routes } from '@angular/router';

const mainPath = '/division';

export const DIVISION_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./division-main/division-main.component').then(m => m.DivisionMainComponent),
        data: { title: 'Divisions', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./division-add-edit/division-add-edit.component').then(m => m.DivisionAddEditComponent),
        data: { title: 'Create Division', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./division-add-edit/division-add-edit.component').then(m => m.DivisionAddEditComponent),
        data: { title: 'Edit Division', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./division-details/division-details.component').then(m => m.DivisionDetailsComponent),
        data: { title: 'Division Details', mainPath }
    },
];
