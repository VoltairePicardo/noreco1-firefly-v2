import { Routes } from '@angular/router';

export const ACCOUNTING_REPORTS_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./accounting-reports-main/accounting-reports-main.component').then(m => m.AccountingReportsMainComponent),
        data: { title: 'Accounting Reports' }
    },

    // Financial Statements (NEA)
    {
        path: 'trial-balance-nea',
        loadComponent: () => import('./reports/trial-balance-nea.component').then(m => m.TrialBalanceNeaComponent),
        data: { title: 'Trial Balance (NEA)' }
    },
    {
        path: 'income-statement-nea',
        loadComponent: () => import('./reports/income-statement-nea.component').then(m => m.IncomeStatementNeaComponent),
        data: { title: 'Statement of Operations (NEA)' }
    },
    {
        path: 'balance-sheet-nea',
        loadComponent: () => import('./reports/balance-sheet-nea.component').then(m => m.BalanceSheetNeaComponent),
        data: { title: 'Balance Sheet (NEA)' }
    },
    {
        path: 'cashflow-statement-nea',
        loadComponent: () => import('./reports/cashflow-statement-nea.component').then(m => m.CashflowStatementNeaComponent),
        data: { title: 'Cash Flow Statement (NEA)' }
    },
    {
        path: 'trial-balance-nea-audited',
        loadComponent: () => import('./reports/trial-balance-nea-audited.component').then(m => m.TrialBalanceNeaAuditedComponent),
        data: { title: 'Trial Balance - Audited (NEA)' }
    },

    // Financial Statements (BSUP)
    {
        path: 'trial-balance',
        loadComponent: () => import('./reports/trial-balance.component').then(m => m.TrialBalanceComponent),
        data: { title: 'Trial Balance DET (BSUP)' }
    },
    {
        path: 'transaction-summary',
        loadComponent: () => import('./reports/transaction-summary.component').then(m => m.TransactionSummaryComponent),
        data: { title: 'Transaction Summary (BSUP)' }
    },
    {
        path: 'balance-sheet',
        loadComponent: () => import('./reports/balance-sheet.component').then(m => m.BalanceSheetComponent),
        data: { title: 'Balance Sheet (BSUP)' }
    },
    {
        path: 'income-statement',
        loadComponent: () => import('./reports/income-statement.component').then(m => m.IncomeStatementComponent),
        data: { title: 'Income Statement (BSUP)' }
    },
    {
        path: 'cashflow-statement-bsup',
        loadComponent: () => import('./reports/cashflow-statement-bsup.component').then(m => m.CashflowStatementBsupComponent),
        data: { title: 'Cash Flow Statement (BSUP)' }
    },

    // Registers
    {
        path: 'apv-register',
        loadComponent: () => import('./reports/apv-register.component').then(m => m.ApvRegisterComponent),
        data: { title: 'APV Register' }
    },
    {
        path: 'cv-register',
        loadComponent: () => import('./reports/cv-register.component').then(m => m.CvRegisterComponent),
        data: { title: 'CV Register' }
    },
    {
        path: 'jv-register',
        loadComponent: () => import('./reports/jv-register.component').then(m => m.JvRegisterComponent),
        data: { title: 'JV Register' }
    },
    {
        path: 'aj-register',
        loadComponent: () => import('./reports/aj-register.component').then(m => m.AjRegisterComponent),
        data: { title: 'AJ Register' }
    },
    {
        path: 'mir-register',
        loadComponent: () => import('./reports/mir-register.component').then(m => m.MirRegisterComponent),
        data: { title: 'Material Issue Register' }
    },
    {
        path: 'sales-register',
        loadComponent: () => import('./reports/sales-register.component').then(m => m.SalesRegisterComponent),
        data: { title: 'Sales Register' }
    },
    {
        path: 'cash-register',
        loadComponent: () => import('./reports/cash-register.component').then(m => m.CashRegisterComponent),
        data: { title: 'Cash Receipts Register' }
    },

    // Summaries
    {
        path: 'rv-summary',
        loadComponent: () => import('./reports/rv-summary.component').then(m => m.RvSummaryComponent),
        data: { title: 'RV Summary' }
    },
    {
        path: 'po-summary',
        loadComponent: () => import('./reports/po-summary.component').then(m => m.PoSummaryComponent),
        data: { title: 'PO Summary' }
    },
    {
        path: 'jo-summary',
        loadComponent: () => import('./reports/jo-summary.component').then(m => m.JoSummaryComponent),
        data: { title: 'JO Summary' }
    },
    {
        path: 'joa-summary',
        loadComponent: () => import('./reports/joa-summary.component').then(m => m.JoaSummaryComponent),
        data: { title: 'JOA Summary' }
    },
    {
        path: 'canvass-summary',
        loadComponent: () => import('./reports/canvass-summary.component').then(m => m.CanvassSummaryComponent),
        data: { title: 'Canvass Summary' }
    },
    {
        path: 'pr-summary',
        loadComponent: () => import('./reports/pr-summary.component').then(m => m.PrSummaryComponent),
        data: { title: 'PR Summary' }
    },
    {
        path: 'pe-summary',
        loadComponent: () => import('./reports/pe-summary.component').then(m => m.PeSummaryComponent),
        data: { title: 'Prepayment Expense Summary' }
    },
    {
        path: 'depreciation-summary',
        loadComponent: () => import('./reports/depreciation-summary.component').then(m => m.DepreciationSummaryComponent),
        data: { title: 'Depreciation Summary' }
    },
    {
        path: 'depreciation-schedule',
        loadComponent: () => import('./reports/depreciation-schedule.component').then(m => m.DepreciationScheduleComponent),
        data: { title: 'Depreciation Schedule' }
    },
    {
        path: 'accounts-payable-aging',
        loadComponent: () => import('./reports/accounts-payable-aging.component').then(m => m.AccountsPayableAgingComponent),
        data: { title: 'Accounts Payable Aging' }
    },
    {
        path: 'pending-voucher-list',
        loadComponent: () => import('./reports/pending-voucher-list.component').then(m => m.PendingVoucherListComponent),
        data: { title: 'Pending Voucher List' }
    },
    {
        path: 'quotation-summary',
        loadComponent: () => import('./reports/quotation-summary.component').then(m => m.QuotationSummaryComponent),
        data: { title: 'Quotation Summary' }
    },
    {
        path: 'check-list',
        loadComponent: () => import('./reports/check-list.component').then(m => m.CheckListComponent),
        data: { title: 'Check Listing' }
    },
    {
        path: 'cash-flow-detail',
        loadComponent: () => import('./reports/cash-flow-detail.component').then(m => m.CashFlowDetailComponent),
        data: { title: 'Cash Flow Detail' }
    },
    {
        path: 'pending-purchase-request',
        loadComponent: () => import('./reports/pending-purchase-request.component').then(m => m.PendingPurchaseRequestComponent),
        data: { title: 'Pending Purchase Requests' }
    },
    {
        path: 'work-in-progress',
        loadComponent: () => import('./reports/work-in-progress.component').then(m => m.WorkInProgressComponent),
        data: { title: 'Work In Progress' }
    },
    {
        path: 'work-order',
        loadComponent: () => import('./reports/work-order.component').then(m => m.WorkOrderComponent),
        data: { title: 'Work Order Aging' }
    },

    // Support Module Reports
    {
        path: 'work-order-transaction',
        loadComponent: () => import('./reports/work-order-transaction.component').then(m => m.WorkOrderTransactionComponent),
        data: { title: 'Work Order Transaction' }
    },
    {
        path: 'pcf-ledger',
        loadComponent: () => import('./reports/pcf-ledger.component').then(m => m.PcfLedgerComponent),
        data: { title: 'PCF Ledger' }
    },
    {
        path: 'unliquidated-ca',
        loadComponent: () => import('./reports/unliquidated-ca.component').then(m => m.UnliquidatedCaComponent),
        data: { title: 'Unliquidated Cash Advance' }
    },
    {
        path: 'bir-alphalist',
        loadComponent: () => import('./reports/bir-alphalist.component').then(m => m.BirAlphalistComponent),
        data: { title: 'BIR Alphalist' }
    },
    {
        path: 'bir-form-1601e',
        loadComponent: () => import('./reports/bir-form-1601e.component').then(m => m.BirForm1601EComponent),
        data: { title: 'BIR Form 1601E' }
    },

    // Summaries (additional)
    {
        path: 'pcv-summary',
        loadComponent: () => import('./reports/pcv-summary.component').then(m => m.PcvSummaryComponent),
        data: { title: 'PCV Summary' }
    },

    // Asset Management
    {
        path: 'maintenance-record-summary',
        loadComponent: () => import('./reports/maintenance-record-summary.component').then(m => m.MaintenanceRecordSummaryComponent),
        data: { title: 'Maintenance Record Summary' }
    },
    {
        path: 'asset-ledger',
        loadComponent: () => import('./reports/asset-ledger.component').then(m => m.AssetLedgerComponent),
        data: { title: 'Asset Ledger' }
    },
    {
        path: 'asset-monitoring-sheet',
        loadComponent: () => import('./reports/asset-monitoring-sheet.component').then(m => m.AssetMonitoringSheetComponent),
        data: { title: 'Asset Monitoring Sheet' }
    },
];
