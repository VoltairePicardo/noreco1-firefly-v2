import { Routes } from '@angular/router';

const mainPath = '/effectivity-date';

export const EFFECTIVITY_DATE_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./effectivity-date-main/effectivity-date-main.component').then(m => m.EffectivityDateMainComponent),
        data: { title: 'Effectivity Dates', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./effectivity-date-add-edit/effectivity-date-add-edit.component').then(m => m.EffectivityDateAddEditComponent),
        data: { title: 'Create Effectivity Date', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./effectivity-date-add-edit/effectivity-date-add-edit.component').then(m => m.EffectivityDateAddEditComponent),
        data: { title: 'Edit Effectivity Date', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./effectivity-date-details/effectivity-date-details.component').then(m => m.EffectivityDateDetailsComponent),
        data: { title: 'Effectivity Date Details', mainPath }
    },
];
