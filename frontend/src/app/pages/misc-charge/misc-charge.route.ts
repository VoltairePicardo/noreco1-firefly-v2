import { Routes } from '@angular/router';

const mainPath = '/misc-charge';

export const MISC_CHARGE_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./misc-charge-main/misc-charge-main.component').then(m => m.MiscChargeMainComponent),
        data: { title: 'Miscellaneous Charges', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./misc-charge-add-edit/misc-charge-add-edit.component').then(m => m.MiscChargeAddEditComponent),
        data: { title: 'Create Miscellaneous Charge', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./misc-charge-add-edit/misc-charge-add-edit.component').then(m => m.MiscChargeAddEditComponent),
        data: { title: 'Edit Miscellaneous Charge', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./misc-charge-details/misc-charge-details.component').then(m => m.MiscChargeDetailsComponent),
        data: { title: 'Miscellaneous Charge Details', mainPath }
    },
];
