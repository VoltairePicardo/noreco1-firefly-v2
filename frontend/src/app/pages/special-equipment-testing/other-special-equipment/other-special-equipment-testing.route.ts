import { Routes } from '@angular/router';

const mainPath = '/other-special-equipment-testing';

export const OTHER_SPECIAL_EQUIPMENT_TESTING_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./other-special-equipment-testing-main/other-special-equipment-testing-main.component').then(m => m.OtherSpecialEquipmentTestingMainComponent),
        data: { title: 'Other Special Equipment Testing', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./other-special-equipment-testing-add-edit/other-special-equipment-testing-add-edit.component').then(m => m.OtherSpecialEquipmentTestingAddEditComponent),
        data: { title: 'Add Other Special Equipment Testing', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./other-special-equipment-testing-detail/other-special-equipment-testing-detail.component').then(m => m.OtherSpecialEquipmentTestingDetailComponent),
        data: { title: 'Other Special Equipment Testing Details', mainPath }
    },
];
