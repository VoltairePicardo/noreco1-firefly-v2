import { Routes } from '@angular/router';

const mainPath = '/transformer-testing';

export const TRANSFORMER_TESTING_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./transformer-testing-main/transformer-testing-main.component').then(m => m.TransformerTestingMainComponent),
        data: { title: 'Transformer Testing', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./transformer-testing-add-edit/transformer-testing-add-edit.component').then(m => m.TransformerTestingAddEditComponent),
        data: { title: 'Add Transformer Testing', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./transformer-testing-add-edit/transformer-testing-add-edit.component').then(m => m.TransformerTestingAddEditComponent),
        data: { title: 'Edit Transformer Testing', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./transformer-testing-detail/transformer-testing-detail.component').then(m => m.TransformerTestingDetailComponent),
        data: { title: 'Transformer Testing Details', mainPath }
    },
];
