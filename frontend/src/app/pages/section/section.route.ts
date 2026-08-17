import { Routes } from '@angular/router';

const mainPath = '/section';

export const SECTION_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./section-main/section-main.component').then(m => m.SectionMainComponent),
        data: { title: 'Sections', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./section-add-edit/section-add-edit.component').then(m => m.SectionAddEditComponent),
        data: { title: 'Create Section', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./section-add-edit/section-add-edit.component').then(m => m.SectionAddEditComponent),
        data: { title: 'Edit Section', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./section-details/section-details.component').then(m => m.SectionDetailsComponent),
        data: { title: 'Section Details', mainPath }
    },
];
