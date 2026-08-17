import { Routes } from '@angular/router';

const mainPath = '/petty-cash-fund';

export const PETTY_CASH_FUND_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./petty-cash-fund-main/petty-cash-fund-main.component').then(m => m.PettyCashFundMainComponent),
        data: { title: 'Petty Cash Funds', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./petty-cash-fund-add-edit/petty-cash-fund-add-edit.component').then(m => m.PettyCashFundAddEditComponent),
        data: { title: 'Create Petty Cash Fund', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./petty-cash-fund-add-edit/petty-cash-fund-add-edit.component').then(m => m.PettyCashFundAddEditComponent),
        data: { title: 'Edit Petty Cash Fund', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./petty-cash-fund-details/petty-cash-fund-details.component').then(m => m.PettyCashFundDetailsComponent),
        data: { title: 'Petty Cash Fund Details', mainPath }
    },
];
