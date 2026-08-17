import { Routes } from '@angular/router';

const mainPath = '/inventory-reports';

export const INVENTORY_REPORTS_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./inventory-reports-main/inventory-reports-main.component').then(m => m.InventoryReportsMainComponent),
        data: { title: 'Inventory Reports', mainPath }
    },
    {
        path: 'bin-card',
        loadComponent: () => import('./reports/bin-card/bin-card.component').then(m => m.BinCardComponent),
        data: { title: 'Bin Card', mainPath }
    },
    {
        path: 'stock-card',
        loadComponent: () => import('./reports/stock-card/stock-card.component').then(m => m.StockCardComponent),
        data: { title: 'Stock Card', mainPath }
    },
    {
        path: 'inventory-balance',
        loadComponent: () => import('./reports/inventory-balance/inventory-balance.component').then(m => m.InventoryBalanceComponent),
        data: { title: 'Inventory Balance', mainPath }
    },
    {
        path: 'withdrawal-summary',
        loadComponent: () => import('./reports/withdrawal-summary/withdrawal-summary.component').then(m => m.WithdrawalSummaryComponent),
        data: { title: 'Summary of Stock Withdrawal', mainPath }
    },
    {
        path: 'release-summary',
        loadComponent: () => import('./reports/release-summary/release-summary.component').then(m => m.ReleaseSummaryComponent),
        data: { title: 'Summary of Stock Release', mainPath }
    },
    {
        path: 'mcrt-summary',
        loadComponent: () => import('./reports/mcrt-summary/mcrt-summary.component').then(m => m.McrtSummaryComponent),
        data: { title: 'Summary of MCRT', mainPath }
    },
    {
        path: 'adjustment-summary',
        loadComponent: () => import('./reports/adjustment-summary/adjustment-summary.component').then(m => m.AdjustmentSummaryComponent),
        data: { title: 'Summary of Stock Adjustment', mainPath }
    },
    {
        path: 'mst-summary',
        loadComponent: () => import('./reports/mst-summary/mst-summary.component').then(m => m.MstSummaryComponent),
        data: { title: 'Summary of MST', mainPath }
    },
    {
        path: 'transfer-summary',
        loadComponent: () => import('./reports/transfer-summary/transfer-summary.component').then(m => m.TransferSummaryComponent),
        data: { title: 'Summary of Stock Transfer', mainPath }
    },
    {
        path: 'receive-summary',
        loadComponent: () => import('./reports/receive-summary/receive-summary.component').then(m => m.ReceiveSummaryComponent),
        data: { title: 'Summary of Received Stock Transfers', mainPath }
    },
    {
        path: 'material-issuance-summary',
        loadComponent: () => import('./reports/material-issuance-summary/material-issuance-summary.component').then(m => m.MaterialIssuanceSummaryComponent),
        data: { title: 'Summary of Material Periodic Issuance', mainPath }
    },
    {
        path: 'ideal-quantity',
        loadComponent: () => import('./reports/ideal-quantity/ideal-quantity.component').then(m => m.IdealQuantityComponent),
        data: { title: 'Ideal Quantity or Reorder Point', mainPath }
    },
    {
        path: 'mrte-ledger',
        loadComponent: () => import('./reports/mrte-ledger/mrte-ledger.component').then(m => m.MrteLedgerComponent),
        data: { title: 'MRTE Ledger', mainPath }
    },
    {
        path: 'special-equipment-release-summary',
        loadComponent: () => import('./reports/special-equipment-release-summary/special-equipment-release-summary.component').then(m => m.SpecialEquipmentReleaseSummaryComponent),
        data: { title: 'Summary of Special Equipment Releasing', mainPath }
    },
    {
        path: 'special-equipment-pending-summary',
        loadComponent: () => import('./reports/special-equipment-pending-summary/special-equipment-pending-summary.component').then(m => m.SpecialEquipmentPendingSummaryComponent),
        data: { title: 'Summary of Special Equipment Pending', mainPath }
    },
];
