import { Routes } from '@angular/router';

const mainPath = '/unit-measure';

export const UNIT_MEASURE_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./unit-measure-main/unit-measure-main.component').then(m => m.UnitMeasureMainComponent),
        data: { title: 'Units of Measure', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./unit-measure-add-edit/unit-measure-add-edit.component').then(m => m.UnitMeasureAddEditComponent),
        data: { title: 'Create Unit of Measure', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./unit-measure-add-edit/unit-measure-add-edit.component').then(m => m.UnitMeasureAddEditComponent),
        data: { title: 'Edit Unit of Measure', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./unit-measure-details/unit-measure-details.component').then(m => m.UnitMeasureDetailsComponent),
        data: { title: 'Unit of Measure Details', mainPath }
    },
];
