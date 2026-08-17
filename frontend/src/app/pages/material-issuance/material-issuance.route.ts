import { Routes } from '@angular/router';

const mainPath = '/material-issuance';

export const MATERIAL_ISSUANCE_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./material-issuance-main/material-issuance-main.component').then(m => m.MaterialIssuanceMainComponent),
        data: { title: 'Material Issuance', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./material-issuance-add-edit/material-issuance-add-edit.component').then(m => m.MaterialIssuanceAddEditComponent),
        data: { title: 'Create Material Issuance', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./material-issuance-add-edit/material-issuance-add-edit.component').then(m => m.MaterialIssuanceAddEditComponent),
        data: { title: 'Edit Material Issuance', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./material-issuance-detail/material-issuance-detail.component').then(m => m.MaterialIssuanceDetailComponent),
        data: { title: 'Material Issuance Detail', mainPath }
    },
];
