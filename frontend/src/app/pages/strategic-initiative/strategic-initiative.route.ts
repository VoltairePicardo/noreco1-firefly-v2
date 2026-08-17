import { Routes } from '@angular/router';

const mainPath = '/strategic-initiative';

export const STRATEGIC_INITIATIVE_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./strategic-initiative-main/strategic-initiative-main.component').then(m => m.StrategicInitiativeMainComponent),
        data: { title: 'Strategic Initiatives', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./strategic-initiative-add-edit/strategic-initiative-add-edit.component').then(m => m.StrategicInitiativeAddEditComponent),
        data: { title: 'Create Strategic Initiative', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./strategic-initiative-add-edit/strategic-initiative-add-edit.component').then(m => m.StrategicInitiativeAddEditComponent),
        data: { title: 'Edit Strategic Initiative', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./strategic-initiative-details/strategic-initiative-details.component').then(m => m.StrategicInitiativeDetailsComponent),
        data: { title: 'Strategic Initiative Details', mainPath }
    },
];
