import { Routes } from '@angular/router';

const mainPath = '/inventory-category';

export const INVENTORY_CATEGORY_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./inventory-category-main/inventory-category-main.component').then(m => m.InventoryCategoryMainComponent),
        data: { title: 'Inventory Categories', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./inventory-category-add-edit/inventory-category-add-edit.component').then(m => m.InventoryCategoryAddEditComponent),
        data: { title: 'Create Inventory Category', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./inventory-category-add-edit/inventory-category-add-edit.component').then(m => m.InventoryCategoryAddEditComponent),
        data: { title: 'Edit Inventory Category', mainPath }
    },
    {
        path: ':categoryId/sub-category/create',
        loadComponent: () => import('./inventory-sub-category-add-edit/inventory-sub-category-add-edit.component').then(m => m.InventorySubCategoryAddEditComponent),
        data: { title: 'Create Inventory Sub Category', mainPath }
    },
    {
        path: ':categoryId/sub-category/:id/edit',
        loadComponent: () => import('./inventory-sub-category-add-edit/inventory-sub-category-add-edit.component').then(m => m.InventorySubCategoryAddEditComponent),
        data: { title: 'Edit Inventory Sub Category', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./inventory-category-details/inventory-category-details.component').then(m => m.InventoryCategoryDetailsComponent),
        data: { title: 'Inventory Category Details', mainPath }
    },
];
