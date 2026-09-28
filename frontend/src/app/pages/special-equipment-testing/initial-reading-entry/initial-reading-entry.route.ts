import { Routes } from '@angular/router';

const mainPath = '/initial-reading-entry';

export const INITIAL_READING_ENTRY_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./initial-reading-entry-main/initial-reading-entry-main.component')
            .then(m => m.InitialReadingEntryMainComponent),
        data: { title: 'Initial Reading Entry', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./initial-reading-entry-add-edit/initial-reading-entry-add-edit.component')
            .then(m => m.InitialReadingEntryAddEditComponent),
        data: { title: 'Initial Reading Entry — Create', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./initial-reading-entry-add-edit/initial-reading-entry-add-edit.component')
            .then(m => m.InitialReadingEntryAddEditComponent),
        data: { title: 'Initial Reading Entry — Edit', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./initial-reading-entry-detail/initial-reading-entry-detail.component')
            .then(m => m.InitialReadingEntryDetailComponent),
        data: { title: 'Initial Reading Entry — Detail', mainPath }
    },
];
