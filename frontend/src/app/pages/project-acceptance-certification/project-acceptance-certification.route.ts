import { Routes } from '@angular/router';

const mainPath = '/project-acceptance-certification';

export const PROJECT_ACCEPTANCE_CERTIFICATION_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./project-acceptance-certification-main/project-acceptance-certification-main.component').then(m => m.ProjectAcceptanceCertificationMainComponent),
        data: { title: 'Project Acceptance Certifications', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./project-acceptance-certification-add-edit/project-acceptance-certification-add-edit.component').then(m => m.ProjectAcceptanceCertificationAddEditComponent),
        data: { title: 'Create Project Acceptance Certification', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./project-acceptance-certification-add-edit/project-acceptance-certification-add-edit.component').then(m => m.ProjectAcceptanceCertificationAddEditComponent),
        data: { title: 'Edit Project Acceptance Certification', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./project-acceptance-certification-detail/project-acceptance-certification-detail.component').then(m => m.ProjectAcceptanceCertificationDetailComponent),
        data: { title: 'Project Acceptance Certification Details', mainPath }
    },
];
