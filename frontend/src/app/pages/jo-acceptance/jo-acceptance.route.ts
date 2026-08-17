import { Routes } from '@angular/router';

export const JO_ACCEPTANCE_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./jo-acceptance-main/jo-acceptance-main.component').then(m => m.JoAcceptanceMainComponent),
        data: { title: 'JO Certifications / Accomplishments' }
    },
    {
        path: 'create',
        loadComponent: () => import('./jo-acceptance-add-edit/jo-acceptance-add-edit.component').then(m => m.JoAcceptanceAddEditComponent),
        data: { title: 'Create JO Certification' }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./jo-acceptance-add-edit/jo-acceptance-add-edit.component').then(m => m.JoAcceptanceAddEditComponent),
        data: { title: 'Edit JO Certification' }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./jo-acceptance-detail/jo-acceptance-detail.component').then(m => m.JoAcceptanceDetailComponent),
        data: { title: 'JO Certification Details' }
    }
];
