import { Routes } from '@angular/router';

const mainPath = '/pcl';

export const PCL_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./pcl-main/pcl-main.component').then(m => m.PclMainComponent),
        data: { title: 'Petty Cash Liquidation', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./pcl-add-edit/pcl-add-edit.component').then(m => m.PclAddEditComponent),
        data: { title: 'Create Petty Cash Liquidation', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./pcl-add-edit/pcl-add-edit.component').then(m => m.PclAddEditComponent),
        data: { title: 'Edit Petty Cash Liquidation', mainPath }
    },
];
