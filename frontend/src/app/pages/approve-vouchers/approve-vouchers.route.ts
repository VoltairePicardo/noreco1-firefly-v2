import { Routes } from '@angular/router';

const mainPath = '/approve-vouchers';

export const APPROVE_VOUCHERS_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./approve-vouchers-main/approve-vouchers-main.component').then(m => m.ApproveVouchersMainComponent),
        data: { title: 'Approve Vouchers', mainPath }
    },
];
