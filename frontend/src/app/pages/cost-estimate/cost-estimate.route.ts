import { Routes } from '@angular/router';

const mainPath = '/cost-estimate';

export const COST_ESTIMATE_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./cost-estimate-main/cost-estimate-main.component').then(m => m.CostEstimateMainComponent),
        data: { title: 'Cost Estimates', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./cost-estimate-add-edit/cost-estimate-add-edit.component').then(m => m.CostEstimateAddEditComponent),
        data: { title: 'Create Cost Estimate', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./cost-estimate-add-edit/cost-estimate-add-edit.component').then(m => m.CostEstimateAddEditComponent),
        data: { title: 'Edit Cost Estimate', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./cost-estimate-detail/cost-estimate-detail.component').then(m => m.CostEstimateDetailComponent),
        data: { title: 'Cost Estimate Details', mainPath }
    },
];
