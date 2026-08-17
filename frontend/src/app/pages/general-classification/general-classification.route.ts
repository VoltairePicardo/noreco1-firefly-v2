import { Routes } from '@angular/router';

const mainPath = '/general-classification';

export const GENERAL_CLASSIFICATION_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./general-classification-main/general-classification-main.component').then(m => m.GeneralClassificationMainComponent),
        data: { title: 'General Classifications', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./general-classification-add-edit/general-classification-add-edit.component').then(m => m.GeneralClassificationAddEditComponent),
        data: { title: 'Create General Classification', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./general-classification-add-edit/general-classification-add-edit.component').then(m => m.GeneralClassificationAddEditComponent),
        data: { title: 'Edit General Classification', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./general-classification-details/general-classification-details.component').then(m => m.GeneralClassificationDetailsComponent),
        data: { title: 'General Classification Details', mainPath }
    },
];
