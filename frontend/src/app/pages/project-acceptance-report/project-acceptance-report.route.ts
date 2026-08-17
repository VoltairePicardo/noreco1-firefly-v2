import { Routes } from '@angular/router';

const mainPath = '/project-acceptance-report';

export const PROJECT_ACCEPTANCE_REPORT_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./project-acceptance-report-main/project-acceptance-report-main.component').then(m => m.ProjectAcceptanceReportMainComponent),
        data: { title: 'Project Acceptance Reports', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./project-acceptance-report-add-edit/project-acceptance-report-add-edit.component').then(m => m.ProjectAcceptanceReportAddEditComponent),
        data: { title: 'Create Project Acceptance Report', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./project-acceptance-report-add-edit/project-acceptance-report-add-edit.component').then(m => m.ProjectAcceptanceReportAddEditComponent),
        data: { title: 'Edit Project Acceptance Report', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./project-acceptance-report-detail/project-acceptance-report-detail.component').then(m => m.ProjectAcceptanceReportDetailComponent),
        data: { title: 'Project Acceptance Report Details', mainPath }
    },
];
