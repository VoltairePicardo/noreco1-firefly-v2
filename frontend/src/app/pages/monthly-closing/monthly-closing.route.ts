import { Routes } from '@angular/router';

const mainPath = '/monthly-closing';

export const MONTHLY_CLOSING_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./monthly-closing-main/monthly-closing-main.component').then(m => m.MonthlyClosingMainComponent),
        data: { title: 'Monthly Closing', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./monthly-closing-add-edit/monthly-closing-add-edit.component').then(m => m.MonthlyClosingAddEditComponent),
        data: { title: 'Create Monthly Closing', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./monthly-closing-add-edit/monthly-closing-add-edit.component').then(m => m.MonthlyClosingAddEditComponent),
        data: { title: 'Edit Monthly Closing', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./monthly-closing-detail/monthly-closing-detail.component').then(m => m.MonthlyClosingDetailComponent),
        data: { title: 'Monthly Closing Detail', mainPath }
    },
];
