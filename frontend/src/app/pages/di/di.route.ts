import { Routes } from '@angular/router';

export const DI_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./di-main/di-main.component').then(m => m.DiMainComponent)
    }
];
