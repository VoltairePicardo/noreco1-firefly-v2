import { Routes } from '@angular/router';

const mainPath = '/voucher-cashflow';

export const VOUCHER_CASHFLOW_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./voucher-cashflow-main/voucher-cashflow-main.component').then(m => m.VoucherCashflowMainComponent),
        data: { title: 'CashFlow Account Setting', mainPath }
    },
    {
        path: ':documentTypeCode/:voucherId/setup',
        loadComponent: () => import('./voucher-cashflow-setup/voucher-cashflow-setup.component').then(m => m.VoucherCashflowSetupComponent),
        data: { title: 'CashFlow Setup', mainPath }
    },
];
