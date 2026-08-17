import { Routes } from '@angular/router';

const mainPath = '/position';

export const POSITION_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./position-main/position-main.component').then(m => m.PositionMainComponent),
        data: { title: 'Positions', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./position-add-edit/position-add-edit.component').then(m => m.PositionAddEditComponent),
        data: { title: 'Create Position', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./position-add-edit/position-add-edit.component').then(m => m.PositionAddEditComponent),
        data: { title: 'Edit Position', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./position-details/position-details.component').then(m => m.PositionDetailsComponent),
        data: { title: 'Position Details', mainPath }
    },
];
