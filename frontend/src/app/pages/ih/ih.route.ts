import { Routes } from '@angular/router';

const mainPath = '/ih';

export const IH_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./ih-main/ih-main.component').then(m => m.IhMainComponent),
        data: { title: 'Item History', mainPath }
    },
];
