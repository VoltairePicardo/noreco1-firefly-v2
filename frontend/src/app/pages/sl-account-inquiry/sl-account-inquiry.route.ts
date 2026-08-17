import { Routes } from '@angular/router';

export const SL_ACCOUNT_INQUIRY_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./sl-account-inquiry-main/sl-account-inquiry-main.component').then(m => m.SlAccountInquiryMainComponent),
    }
];
