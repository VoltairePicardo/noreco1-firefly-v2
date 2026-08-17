import { Routes } from '@angular/router';

const mainPath = '/sl-entity';

export const SL_ENTITY_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./sl-entity-main/sl-entity-main.component').then(m => m.SlEntityMainComponent),
        data: { title: 'SL Entities', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./sl-entity-add-edit/sl-entity-add-edit.component').then(m => m.SlEntityAddEditComponent),
        data: { title: 'Create SL Entity', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./sl-entity-add-edit/sl-entity-add-edit.component').then(m => m.SlEntityAddEditComponent),
        data: { title: 'Edit SL Entity', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./sl-entity-details/sl-entity-details.component').then(m => m.SlEntityDetailsComponent),
        data: { title: 'SL Entity Details', mainPath }
    },
];
