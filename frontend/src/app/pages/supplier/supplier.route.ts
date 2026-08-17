import { Routes } from '@angular/router';

const mainPath = '/supplier';

export const SUPPLIER_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./supplier-main/supplier-main.component').then(m => m.SupplierMainComponent),
        data: { title: 'Suppliers', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./supplier-add-edit/supplier-add-edit.component').then(m => m.SupplierAddEditComponent),
        data: { title: 'Create Supplier', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./supplier-add-edit/supplier-add-edit.component').then(m => m.SupplierAddEditComponent),
        data: { title: 'Edit Supplier', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./supplier-details/supplier-details.component').then(m => m.SupplierDetailsComponent),
        data: { title: 'Supplier Details', mainPath }
    },
];
