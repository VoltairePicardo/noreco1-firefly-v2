import { Routes } from '@angular/router';

const mainPath = '/adjustment-journal';

export const ADJUSTMENT_JOURNAL_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./adjustment-journal-main/adjustment-journal-main.component').then(m => m.AdjustmentJournalMainComponent),
        data: { title: 'Adjustment Journal', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./adjustment-journal-add-edit/adjustment-journal-add-edit.component').then(m => m.AdjustmentJournalAddEditComponent),
        data: { title: 'Create Adjustment Journal', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./adjustment-journal-add-edit/adjustment-journal-add-edit.component').then(m => m.AdjustmentJournalAddEditComponent),
        data: { title: 'Edit Adjustment Journal', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./adjustment-journal-detail/adjustment-journal-detail.component').then(m => m.AdjustmentJournalDetailComponent),
        data: { title: 'Adjustment Journal Detail', mainPath }
    },
];
