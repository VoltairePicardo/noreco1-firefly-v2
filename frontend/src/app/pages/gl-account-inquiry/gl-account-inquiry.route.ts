import { Routes } from '@angular/router';

export const GL_ACCOUNT_INQUIRY_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./gl-account-inquiry-main/gl-account-inquiry-main.component').then(m => m.GlAccountInquiryMainComponent),
    }
];
