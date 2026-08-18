import { Routes } from '@angular/router';

const mainPath = '/memorandum-receipt';

export const MEMORANDUM_RECEIPT_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./memorandum-receipt-main/memorandum-receipt-main.component').then(m => m.MemorandumReceiptMainComponent),
        data: { title: 'MR of Tools & Equipment', mainPath }
    },
    {
        path: 'create',
        loadComponent: () => import('./memorandum-receipt-add-edit/memorandum-receipt-add-edit.component').then(m => m.MemorandumReceiptAddEditComponent),
        data: { title: 'Create MR of Tools & Equipment', mainPath }
    },
    {
        path: ':id/edit',
        loadComponent: () => import('./memorandum-receipt-add-edit/memorandum-receipt-add-edit.component').then(m => m.MemorandumReceiptAddEditComponent),
        data: { title: 'Edit MR of Tools & Equipment', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./memorandum-receipt-detail/memorandum-receipt-detail.component').then(m => m.MemorandumReceiptDetailComponent),
        data: { title: 'MR of Tools & Equipment Details', mainPath }
    },
    {
        path: 'create-multiple',
        loadComponent: () => import('./memorandum-receipt-create-multiple/memorandum-receipt-create-multiple.component').then(m => m.MemorandumReceiptCreateMultipleComponent),
        data: { title: 'Create Multiple Employee MR', mainPath }
    },
    {
        path: 'reissue',
        loadComponent: () => import('./memorandum-receipt-reissue/memorandum-receipt-reissue.component').then(m => m.MemorandumReceiptReissueComponent),
        data: { title: 'Re-issue Returned MR', mainPath }
    },
];
