import { Routes } from '@angular/router';

const mainPath = '/item';

export const ITEM_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./item-main/item-main.component').then(m => m.ItemMainComponent),
        data: { title: 'Items', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./item-add-edit/item-add-edit.component').then(m => m.ItemAddEditComponent),
        data: { title: 'Create Item', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./item-add-edit/item-add-edit.component').then(m => m.ItemAddEditComponent),
        data: { title: 'Edit Item', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./item-details/item-details.component').then(m => m.ItemDetailsComponent),
        data: { title: 'Item Details', mainPath }
    },
];
