import { Routes } from '@angular/router';

const mainPath = '/roles';

export const ROLES_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./roles-main/roles-main.component').then(m => m.RolesMainComponent),
        data: { title: 'Roles', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./roles-add-edit/roles-add-edit.component').then(m => m.RolesAddEditComponent),
        data: { title: 'Create Role', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./roles-add-edit/roles-add-edit.component').then(m => m.RolesAddEditComponent),
        data: { title: 'Edit Role', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./roles-details/roles-details.component').then(m => m.RolesDetailsComponent),
        data: { title: 'Role Details', mainPath }
    },
    {
        path: ':id/manage',
        loadComponent: () => import('./user-role-management/user-role-management.component').then(m => m.UserRoleManagementComponent),
        data: { title: 'Manage Role Users', mainPath }
    },
];
