import { Routes } from '@angular/router';

const mainPath = '/funding-source';

export const FUNDING_SOURCE_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./funding-source-main/funding-source-main.component').then(m => m.FundingSourceMainComponent),
        data: { title: 'Funding Sources', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./funding-source-add-edit/funding-source-add-edit.component').then(m => m.FundingSourceAddEditComponent),
        data: { title: 'Create Funding Source', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./funding-source-add-edit/funding-source-add-edit.component').then(m => m.FundingSourceAddEditComponent),
        data: { title: 'Edit Funding Source', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./funding-source-details/funding-source-details.component').then(m => m.FundingSourceDetailsComponent),
        data: { title: 'Funding Source Details', mainPath }
    },
];
