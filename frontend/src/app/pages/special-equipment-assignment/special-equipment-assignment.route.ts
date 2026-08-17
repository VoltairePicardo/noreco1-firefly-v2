import { Routes } from '@angular/router';

const mainPath = '/special-equipment-assignment';

export const SPECIAL_EQUIPMENT_ASSIGNMENT_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./special-equipment-assignment-main/special-equipment-assignment-main.component').then(m => m.SpecialEquipmentAssignmentMainComponent),
        data: { title: 'Special Equipment Assignment', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./special-equipment-assignment-add-edit/special-equipment-assignment-add-edit.component').then(m => m.SpecialEquipmentAssignmentAddEditComponent),
        data: { title: 'Create Special Equipment Assignment', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./special-equipment-assignment-add-edit/special-equipment-assignment-add-edit.component').then(m => m.SpecialEquipmentAssignmentAddEditComponent),
        data: { title: 'Edit Special Equipment Assignment', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./special-equipment-assignment-detail/special-equipment-assignment-detail.component').then(m => m.SpecialEquipmentAssignmentDetailComponent),
        data: { title: 'Special Equipment Assignment Details', mainPath }
    },
];
