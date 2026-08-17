import { Routes } from '@angular/router';

const mainPath = '/site-inspection-report';

export const SITE_INSPECTION_REPORT_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./site-inspection-report-main/site-inspection-report-main.component').then(m => m.SiteInspectionReportMainComponent),
        data: { title: 'Site Inspection Reports', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./site-inspection-report-add-edit/site-inspection-report-add-edit.component').then(m => m.SiteInspectionReportAddEditComponent),
        data: { title: 'Create Site Inspection Report', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./site-inspection-report-add-edit/site-inspection-report-add-edit.component').then(m => m.SiteInspectionReportAddEditComponent),
        data: { title: 'Edit Site Inspection Report', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./site-inspection-report-detail/site-inspection-report-detail.component').then(m => m.SiteInspectionReportDetailComponent),
        data: { title: 'Site Inspection Report Details', mainPath }
    },
];
