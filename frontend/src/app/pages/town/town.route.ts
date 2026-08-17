import { Routes } from '@angular/router';

const mainPath = '/town';

export const TOWN_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./town-main/town-main.component').then(m => m.TownMainComponent),
        data: { title: 'Towns', mainPath }
    },
];
