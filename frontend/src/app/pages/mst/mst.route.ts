import { Routes } from '@angular/router';

const mainPath = '/mst';

export const MST_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./mst-main/mst-main.component').then(m => m.MstMainComponent),
        data: { title: 'Material Salvage Ticket', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./mst-add-edit/mst-add-edit.component').then(m => m.MstAddEditComponent),
        data: { title: 'Create Material Salvage Ticket', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./mst-add-edit/mst-add-edit.component').then(m => m.MstAddEditComponent),
        data: { title: 'Edit Material Salvage Ticket', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./mst-detail/mst-detail.component').then(m => m.MstDetailComponent),
        data: { title: 'Material Salvage Ticket Details', mainPath }
    },
];
