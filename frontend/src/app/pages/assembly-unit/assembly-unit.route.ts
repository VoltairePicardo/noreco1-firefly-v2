import { Routes } from '@angular/router';

const mainPath = '/assembly-unit';

export const ASSEMBLY_UNIT_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./assembly-unit-main/assembly-unit-main.component').then(m => m.AssemblyUnitMainComponent),
        data: { title: 'Assembly Units', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./assembly-unit-add-edit/assembly-unit-add-edit.component').then(m => m.AssemblyUnitAddEditComponent),
        data: { title: 'Create Assembly Unit', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./assembly-unit-add-edit/assembly-unit-add-edit.component').then(m => m.AssemblyUnitAddEditComponent),
        data: { title: 'Edit Assembly Unit', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./assembly-unit-details/assembly-unit-details.component').then(m => m.AssemblyUnitDetailsComponent),
        data: { title: 'Assembly Unit Details', mainPath }
    },
    {
        path: 'types',
        loadComponent: () => import('./assembly-unit-types-main/assembly-unit-types-main.component').then(m => m.AssemblyUnitTypesMainComponent),
        data: { title: 'Assembly Types', mainPath }
    },
    {
        path: 'types/create',
        loadComponent: () => import('./assembly-unit-types-add-edit/assembly-unit-types-add-edit.component').then(m => m.AssemblyUnitTypesAddEditComponent),
        data: { title: 'Create Assembly Type', mainPath }
    },
    {
        path: 'types/:id/edit',
        loadComponent: () => import('./assembly-unit-types-add-edit/assembly-unit-types-add-edit.component').then(m => m.AssemblyUnitTypesAddEditComponent),
        data: { title: 'Edit Assembly Type', mainPath }
    },
];
