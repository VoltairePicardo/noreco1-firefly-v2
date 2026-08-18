import { Routes } from '@angular/router';

const mainPath = '/item-testing';

export const ITEM_TESTING_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./item-testing-main/item-testing-main.component').then(m => m.ItemTestingMainComponent),
        data: { title: 'Item Testing', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./item-testing-add-edit/item-testing-add-edit.component').then(m => m.ItemTestingAddEditComponent),
        data: { title: 'Create Item Testing', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./item-testing-add-edit/item-testing-add-edit.component').then(m => m.ItemTestingAddEditComponent),
        data: { title: 'Edit Item Testing', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./item-testing-detail/item-testing-detail.component').then(m => m.ItemTestingDetailComponent),
        data: { title: 'Item Testing Details', mainPath }
    },
];
