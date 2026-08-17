import { Routes } from '@angular/router';

const mainPath = '/document-cancellation';

export const DOCUMENT_CANCELLATION_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./document-cancellation-main/document-cancellation-main.component').then(m => m.DocumentCancellationMainComponent),
        data: { title: 'Document Cancellation', mainPath }
    },
];
