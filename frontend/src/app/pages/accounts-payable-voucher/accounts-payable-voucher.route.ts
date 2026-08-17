import { Routes } from '@angular/router';

export const ACCOUNTS_PAYABLE_VOUCHER_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () =>
            import('./accounts-payable-voucher-main/accounts-payable-voucher-main.component').then(
                m => m.AccountsPayableVoucherMainComponent
            ),
        data: { title: 'Accounts Payable Voucher' }
    },
    {
        path: 'create',
        loadComponent: () =>
            import('./accounts-payable-voucher-add-edit/accounts-payable-voucher-add-edit.component').then(
                m => m.AccountsPayableVoucherAddEditComponent
            ),
        data: { title: 'Create Accounts Payable Voucher' }
    },
    {
        path: ':id/edit',
        loadComponent: () =>
            import('./accounts-payable-voucher-add-edit/accounts-payable-voucher-add-edit.component').then(
                m => m.AccountsPayableVoucherAddEditComponent
            ),
        data: { title: 'Edit Accounts Payable Voucher' }
    },
    {
        path: ':id/detail',
        loadComponent: () =>
            import('./accounts-payable-voucher-detail/accounts-payable-voucher-detail.component').then(
                m => m.AccountsPayableVoucherDetailComponent
            ),
        data: { title: 'Accounts Payable Voucher Detail' }
    }
];
