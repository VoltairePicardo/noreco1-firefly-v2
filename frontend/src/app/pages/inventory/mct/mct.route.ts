import { Routes } from '@angular/router';

const mainPath = '/mct';

export const MCT_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./mct-main/mct-main.component').then(m => m.MctMainComponent),
        data: { title: 'Material Credit Ticket', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./mct-add-edit/mct-add-edit.component').then(m => m.MctAddEditComponent),
        data: { title: 'Create Material Credit Ticket', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./mct-add-edit/mct-add-edit.component').then(m => m.MctAddEditComponent),
        data: { title: 'Edit Material Credit Ticket', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./mct-detail/mct-detail.component').then(m => m.MctDetailComponent),
        data: { title: 'Material Credit Ticket Details', mainPath }
    },
];
