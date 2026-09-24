import { Routes } from '@angular/router';

const mainPath = '/meter-testing';

export const METER_TESTING_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./meter-testing-main/meter-testing-main.component').then(m => m.MeterTestingMainComponent),
        data: { title: 'Meter Testing', mainPath }
    },
    {
        path: 'upload',
        loadComponent: () => import('./meter-testing-upload/meter-testing-upload.component').then(m => m.MeterTestingUploadComponent),
        data: { title: 'Multiple Meter Testing', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./meter-testing-add-edit/meter-testing-add-edit.component').then(m => m.MeterTestingAddEditComponent),
        data: { title: 'Single Meter Testing', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./meter-testing-add-edit/meter-testing-add-edit.component').then(m => m.MeterTestingAddEditComponent),
        data: { title: 'Edit Meter Testing', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./meter-testing-detail/meter-testing-detail.component').then(m => m.MeterTestingDetailComponent),
        data: { title: 'Meter Testing Details', mainPath }
    },
];
