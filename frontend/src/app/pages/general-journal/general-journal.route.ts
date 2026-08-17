import { Routes } from '@angular/router';

const mainPath = '/general-journal';

export const GENERAL_JOURNAL_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./general-journal-main/general-journal-main.component').then(m => m.GeneralJournalMainComponent),
        data: { title: 'General Journal', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./general-journal-add-edit/general-journal-add-edit.component').then(m => m.GeneralJournalAddEditComponent),
        data: { title: 'Create General Journal', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./general-journal-add-edit/general-journal-add-edit.component').then(m => m.GeneralJournalAddEditComponent),
        data: { title: 'Edit General Journal', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./general-journal-detail/general-journal-detail.component').then(m => m.GeneralJournalDetailComponent),
        data: { title: 'General Journal Detail', mainPath }
    },
];
