import { Routes } from '@angular/router';

const mainPath = '/meter-model';

export const METER_MODEL_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./meter-model-main/meter-model-main.component').then(m => m.MeterModelMainComponent),
        data: { title: 'Meter Models', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./meter-model-add-edit/meter-model-add-edit.component').then(m => m.MeterModelAddEditComponent),
        data: { title: 'Create Meter Model', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./meter-model-add-edit/meter-model-add-edit.component').then(m => m.MeterModelAddEditComponent),
        data: { title: 'Edit Meter Model', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./meter-model-details/meter-model-details.component').then(m => m.MeterModelDetailsComponent),
        data: { title: 'Meter Model Details', mainPath }
    },
];
