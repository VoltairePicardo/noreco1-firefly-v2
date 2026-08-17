import { Routes } from '@angular/router';

const mainPath = '/canvass';

export const CANVASS_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./canvass-main/canvass-main.component').then(m => m.CanvassMainComponent),
        data: { title: 'Canvass', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./canvass-add-edit/canvass-add-edit.component').then(m => m.CanvassAddEditComponent),
        data: { title: 'Create Canvass', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./canvass-add-edit/canvass-add-edit.component').then(m => m.CanvassAddEditComponent),
        data: { title: 'Edit Canvass', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./canvass-detail/canvass-detail.component').then(m => m.CanvassDetailComponent),
        data: { title: 'Canvass Details', mainPath }
    },
];
