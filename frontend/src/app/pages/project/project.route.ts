import { Routes } from '@angular/router';

const mainPath = '/project';

export const PROJECT_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./project-main/project-main.component').then(m => m.ProjectMainComponent),
        data: { title: 'Projects', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./project-add-edit/project-add-edit.component').then(m => m.ProjectAddEditComponent),
        data: { title: 'Create Project', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./project-add-edit/project-add-edit.component').then(m => m.ProjectAddEditComponent),
        data: { title: 'Edit Project', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./project-detail/project-detail.component').then(m => m.ProjectDetailComponent),
        data: { title: 'Project Details', mainPath }
    },
];
