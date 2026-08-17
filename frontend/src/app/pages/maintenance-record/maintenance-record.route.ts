import { Routes } from '@angular/router';

const mainPath = '/maintenance-record';

export const MAINTENANCE_RECORD_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./maintenance-record-main/maintenance-record-main.component').then(m => m.MaintenanceRecordMainComponent),
        data: { title: 'Maintenance Record', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./maintenance-record-add-edit/maintenance-record-add-edit.component').then(m => m.MaintenanceRecordAddEditComponent),
        data: { title: 'Create Maintenance Record', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./maintenance-record-add-edit/maintenance-record-add-edit.component').then(m => m.MaintenanceRecordAddEditComponent),
        data: { title: 'Edit Maintenance Record', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./maintenance-record-detail/maintenance-record-detail.component').then(m => m.MaintenanceRecordDetailComponent),
        data: { title: 'Maintenance Record Detail', mainPath }
    },
];
