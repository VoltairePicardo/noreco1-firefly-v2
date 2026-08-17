import { Routes } from '@angular/router';

const mainPath = '/department';

export const DEPARTMENT_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./department-main/department-main.component').then(m => m.DepartmentMainComponent),
        data: { title: 'Departments', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./department-add-edit/department-add-edit.component').then(m => m.DepartmentAddEditComponent),
        data: { title: 'Create Department', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./department-add-edit/department-add-edit.component').then(m => m.DepartmentAddEditComponent),
        data: { title: 'Edit Department', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./department-details/department-details.component').then(m => m.DepartmentDetailsComponent),
        data: { title: 'Department Details', mainPath }
    },
];
