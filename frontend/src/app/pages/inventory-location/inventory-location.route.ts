import { Routes } from '@angular/router';

const mainPath = '/inventory-location';

export const INVENTORY_LOCATION_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./inventory-location-main/inventory-location-main.component').then(m => m.InventoryLocationMainComponent),
        data: { title: 'Inventory Locations', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./inventory-location-add-edit/inventory-location-add-edit.component').then(m => m.InventoryLocationAddEditComponent),
        data: { title: 'Create Inventory Location', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./inventory-location-add-edit/inventory-location-add-edit.component').then(m => m.InventoryLocationAddEditComponent),
        data: { title: 'Edit Inventory Location', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./inventory-location-details/inventory-location-details.component').then(m => m.InventoryLocationDetailsComponent),
        data: { title: 'Inventory Location Details', mainPath }
    },
];
