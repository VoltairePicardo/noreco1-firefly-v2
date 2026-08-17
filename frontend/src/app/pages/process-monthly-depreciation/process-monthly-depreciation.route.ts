import { Routes } from '@angular/router';

const mainPath = '/process-monthly-depreciation';

export const PROCESS_MONTHLY_DEPRECIATION_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./process-monthly-depreciation-main/process-monthly-depreciation-main.component').then(m => m.ProcessMonthlyDepreciationMainComponent),
        data: { title: 'Process Monthly Depreciation', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./process-monthly-depreciation-detail/process-monthly-depreciation-detail.component').then(m => m.ProcessMonthlyDepreciationDetailComponent),
        data: { title: 'Process Monthly Depreciation Detail', mainPath }
    },
];
