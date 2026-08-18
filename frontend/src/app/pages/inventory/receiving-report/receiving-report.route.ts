import { Routes } from '@angular/router';

const mainPath = '/receiving-report';

export const RECEIVING_REPORT_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./receiving-report-main/receiving-report-main.component').then(m => m.ReceivingReportMainComponent),
        data: { title: 'Receiving Reports', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./receiving-report-add-edit/receiving-report-add-edit.component').then(m => m.ReceivingReportAddEditComponent),
        data: { title: 'Create Receiving Report', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./receiving-report-add-edit/receiving-report-add-edit.component').then(m => m.ReceivingReportAddEditComponent),
        data: { title: 'Edit Receiving Report', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./receiving-report-detail/receiving-report-detail.component').then(m => m.ReceivingReportDetailComponent),
        data: { title: 'Receiving Report Details', mainPath }
    },
];
