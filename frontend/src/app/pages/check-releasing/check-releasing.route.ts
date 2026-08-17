import { Routes } from '@angular/router';

const mainPath = '/check-releasing';

export const CHECK_RELEASING_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./check-releasing-main/check-releasing-main.component').then(m => m.CheckReleasingMainComponent),
        data: { title: 'Check Releasing', mainPath }
    },
    {
        path: ':id/release',
        loadComponent: () => import('./check-releasing-add-edit/check-releasing-add-edit.component').then(m => m.CheckReleasingAddEditComponent),
        data: { title: 'Release Check', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./check-releasing-detail/check-releasing-detail.component').then(m => m.CheckReleasingDetailComponent),
        data: { title: 'Check Releasing Detail', mainPath }
    },
];
