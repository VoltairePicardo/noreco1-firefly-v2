import {Routes} from '@angular/router';

import {authGuard} from '@/app/pages/auth/auth.guard';
import {SignInComponent} from '@/app/pages/auth/version1/sign-in/sign-in.component';
import {MainLayoutComponent} from '@layouts/main-layout/main-layout.component';
import {loginGuard} from '@/app/pages/auth/login.guard';
import {roleGuard} from '@/app/pages/auth/role.guard';

export const routes: Routes = [
    { path: 'login', component: SignInComponent, canActivate: [loginGuard] },
    {
        path: '',
        component: MainLayoutComponent,
        canActivateChild: [authGuard],
        loadChildren: () => import('./pages/pages.route').then((mod) => mod.PAGES_ROUTES)
    },
    {
        path: '',
        loadChildren: () => import('./pages/error/error.route').then((mod) => mod.ERROR_PAGES_ROUTES)
    },
    { path: '**', redirectTo: '/error/404' },
];


