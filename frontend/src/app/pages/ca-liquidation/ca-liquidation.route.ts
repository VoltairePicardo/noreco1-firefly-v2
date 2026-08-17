import { Routes } from '@angular/router';

const mainPath = '/ca-liquidation';

export const CA_LIQUIDATION_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./ca-liquidation-main/ca-liquidation-main.component').then(m => m.CaLiquidationMainComponent),
        data: { title: 'CA Liquidation', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./ca-liquidation-add-edit/ca-liquidation-add-edit.component').then(m => m.CaLiquidationAddEditComponent),
        data: { title: 'Create CA Liquidation', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./ca-liquidation-add-edit/ca-liquidation-add-edit.component').then(m => m.CaLiquidationAddEditComponent),
        data: { title: 'Edit CA Liquidation', mainPath }
    },
];
