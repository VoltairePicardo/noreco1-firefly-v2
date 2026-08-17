import { Routes } from '@angular/router';

const mainPath = '/sub-supplier';

export const SUB_SUPPLIER_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./sub-supplier-main/sub-supplier-main.component').then(m => m.SubSupplierMainComponent),
        data: { title: 'Sub-Suppliers', mainPath }
    },
    {
        path: 'upload',
        loadComponent: () => import('./sub-supplier-upload/sub-supplier-upload.component').then(m => m.SubSupplierUploadComponent),
        data: { title: 'Upload Sub-Suppliers', mainPath }
    },
    {
        path: ':id/detail',
        loadComponent: () => import('./sub-supplier-details/sub-supplier-details.component').then(m => m.SubSupplierDetailsComponent),
        data: { title: 'Sub-Supplier Details', mainPath }
    },
];
