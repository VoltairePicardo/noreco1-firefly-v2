import { Routes } from '@angular/router';

const mainPath = '/asset-type';

export const ASSET_TYPE_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./asset-type-main/asset-type-main.component').then(m => m.AssetTypeMainComponent),
        data: { title: 'Asset Types', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./asset-type-add-edit/asset-type-add-edit.component').then(m => m.AssetTypeAddEditComponent),
        data: { title: 'Create Asset Type', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./asset-type-add-edit/asset-type-add-edit.component').then(m => m.AssetTypeAddEditComponent),
        data: { title: 'Edit Asset Type', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./asset-type-details/asset-type-details.component').then(m => m.AssetTypeDetailsComponent),
        data: { title: 'Asset Type Details', mainPath }
    },
];
