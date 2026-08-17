import { Routes } from '@angular/router';

const mainPath = '/allocation-factor';

export const ALLOCATION_FACTOR_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./allocation-factor-main/allocation-factor-main.component').then(m => m.AllocationFactorMainComponent),
        data: { title: 'Allocation Factors', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./allocation-factor-add-edit/allocation-factor-add-edit.component').then(m => m.AllocationFactorAddEditComponent),
        data: { title: 'Create Allocation Factor', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./allocation-factor-add-edit/allocation-factor-add-edit.component').then(m => m.AllocationFactorAddEditComponent),
        data: { title: 'Edit Allocation Factor', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./allocation-factor-details/allocation-factor-details.component').then(m => m.AllocationFactorDetailsComponent),
        data: { title: 'Allocation Factor Details', mainPath }
    },
    {
        path: ':factorId/validity/:validityId/edit',
        loadComponent: () => import('./allocation-factor-edit-validity/allocation-factor-edit-validity.component').then(m => m.AllocationFactorEditValidityComponent),
        data: { title: 'Edit Allocation Factor Validity', mainPath }
    },
];
