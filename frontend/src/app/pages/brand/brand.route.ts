import { Routes } from '@angular/router';

const mainPath = '/brand';

export const BRAND_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./brand-main/brand-main.component').then(m => m.BrandMainComponent),
        data: { title: 'Brands', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./brand-add-edit/brand-add-edit.component').then(m => m.BrandAddEditComponent),
        data: { title: 'Create Brand', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./brand-add-edit/brand-add-edit.component').then(m => m.BrandAddEditComponent),
        data: { title: 'Edit Brand', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./brand-details/brand-details.component').then(m => m.BrandDetailsComponent),
        data: { title: 'Brand Details', mainPath }
    },
];
