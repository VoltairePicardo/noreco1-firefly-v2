import { Routes } from '@angular/router';

export const ENERGY_SALES_ROUTES: Routes = [
    {
        path: '',
        children: [
            {
                path: '',
                loadComponent: () => import('./energy-sales-main/energy-sales-main.component').then(m => m.EnergySalesMainComponent),
                data: { title: 'Energy Sales' }
            },
            {
                path: 'create',
                loadComponent: () => import('./energy-sales-add-edit/energy-sales-add-edit.component').then(m => m.EnergySalesAddEditComponent),
                data: { title: 'Create Energy Sales' }
            },
            {
                path: ':id/edit',
                loadComponent: () => import('./energy-sales-add-edit/energy-sales-add-edit.component').then(m => m.EnergySalesAddEditComponent),
                data: { title: 'Edit Energy Sales' }
            },
            {
                path: ':id/detail',
                loadComponent: () => import('./energy-sales-detail/energy-sales-detail.component').then(m => m.EnergySalesDetailComponent),
                data: { title: 'Energy Sales Detail' }
            }
        ]
    }
];
