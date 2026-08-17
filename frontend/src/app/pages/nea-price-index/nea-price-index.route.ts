import { Routes } from '@angular/router';

const mainPath = '/nea-price-index';

export const NEA_PRICE_INDEX_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./nea-price-index-main/nea-price-index-main.component').then(m => m.NeaPriceIndexMainComponent),
        data: { title: 'NEA Price Indices', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./nea-price-index-add-edit/nea-price-index-add-edit.component').then(m => m.NeaPriceIndexAddEditComponent),
        data: { title: 'Create NEA Price Index', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./nea-price-index-add-edit/nea-price-index-add-edit.component').then(m => m.NeaPriceIndexAddEditComponent),
        data: { title: 'Edit NEA Price Index', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./nea-price-index-details/nea-price-index-details.component').then(m => m.NeaPriceIndexDetailsComponent),
        data: { title: 'NEA Price Index Details', mainPath }
    },
];
