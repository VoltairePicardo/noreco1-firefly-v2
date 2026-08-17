import { Routes } from '@angular/router';
import { roleGuard } from '@/app/pages/auth/role.guard';

export const PAGES_ROUTES: Routes = [
    {
        path: '',
        redirectTo: 'home',
        pathMatch: 'full',
    },
    {
        path: 'home',
        loadChildren: () => import('./home/home.route').then((mod) => mod.HOME_ROUTES),
        data: { title: 'Dashboard' },
    },
    {
        path: 'profile/change-password',
        loadComponent: () => import('./auth/change-password/change-password.component').then((m) => m.ChangePasswordComponent),
        data: { title: 'Change Password' },
    },
    {
        path: 'bank',
        loadChildren: () => import('./bank/bank.route').then(m => m.BANK_ROUTES),
        data: { title: 'Banks' },
    },
    {
        path: 'department',
        loadChildren: () => import('./department/department.route').then(m => m.DEPARTMENT_ROUTES),
        data: { title: 'Departments' },
    },
    {
        path: 'division',
        loadChildren: () => import('./division/division.route').then(m => m.DIVISION_ROUTES),
        data: { title: 'Divisions' },
    },
    {
        path: 'section',
        loadChildren: () => import('./section/section.route').then(m => m.SECTION_ROUTES),
        data: { title: 'Sections' },
    },
    {
        path: 'town',
        loadChildren: () => import('./town/town.route').then(m => m.TOWN_ROUTES),
        data: { title: 'Towns' },
    },
    {
        path: 'unit-measure',
        loadChildren: () => import('./unit-measure/unit-measure.route').then(m => m.UNIT_MEASURE_ROUTES),
        data: { title: 'Units of Measure' },
    },
    {
        path: 'position',
        loadChildren: () => import('./position/position.route').then(m => m.POSITION_ROUTES),
        data: { title: 'Positions' },
    },
    {
        path: 'user-accounts',
        loadChildren: () => import('./user-accounts/user-accounts.route').then(m => m.USER_ACCOUNTS_ROUTES),
        data: { title: 'User Accounts' },
    },
    {
        path: 'roles',
        loadChildren: () => import('./roles/roles.route').then(m => m.ROLES_ROUTES),
        data: { title: 'Roles' },
    },
    {
        path: 'brand',
        loadChildren: () => import('./brand/brand.route').then(m => m.BRAND_ROUTES),
        data: { title: 'Brands' },
    },
    {
        path: 'supplier',
        loadChildren: () => import('./supplier/supplier.route').then(m => m.SUPPLIER_ROUTES),
        data: { title: 'Suppliers' },
    },
    {
        path: 'sub-supplier',
        loadChildren: () => import('./sub-supplier/sub-supplier.route').then(m => m.SUB_SUPPLIER_ROUTES),
        data: { title: 'Sub-Suppliers' },
    },
    {
        path: 'item',
        loadChildren: () => import('./item/item.route').then(m => m.ITEM_ROUTES),
        data: { title: 'Items' },
    },
    {
        path: 'general-classification',
        loadChildren: () => import('./general-classification/general-classification.route').then(m => m.GENERAL_CLASSIFICATION_ROUTES),
        data: { title: 'General Classifications' },
    },
    {
        path: 'budget-item-classification',
        loadChildren: () => import('./budget-item-classification/budget-item-classification.route').then(m => m.BUDGET_ITEM_CLASSIFICATION_ROUTES),
        data: { title: 'Budget Item Classifications' },
    },
    {
        path: 'bank-account',
        loadChildren: () => import('./bank-account/bank-account.route').then(m => m.BANK_ACCOUNT_ROUTES),
        data: { title: 'Bank Accounts' },
    },
    {
        path: 'funding-source',
        loadChildren: () => import('./funding-source/funding-source.route').then(m => m.FUNDING_SOURCE_ROUTES),
        data: { title: 'Funding Sources' },
    },
    {
        path: 'strategic-initiative',
        loadChildren: () => import('./strategic-initiative/strategic-initiative.route').then(m => m.STRATEGIC_INITIATIVE_ROUTES),
        data: { title: 'Strategic Initiatives' },
    },
    {
        path: 'nea-price-index',
        loadChildren: () => import('./nea-price-index/nea-price-index.route').then(m => m.NEA_PRICE_INDEX_ROUTES),
        data: { title: 'NEA Price Indices' },
    },
    {
        path: 'effectivity-date',
        loadChildren: () => import('./effectivity-date/effectivity-date.route').then(m => m.EFFECTIVITY_DATE_ROUTES),
        data: { title: 'Effectivity Dates' },
    },
    {
        path: 'sl-entity',
        loadChildren: () => import('./sl-entity/sl-entity.route').then(m => m.SL_ENTITY_ROUTES),
        data: { title: 'SL Entities' },
    },
    {
        path: 'coa',
        loadChildren: () => import('./coa/coa.route').then(m => m.COA_ROUTES),
        data: { title: 'Chart of Accounts' },
    },
    {
        path: 'assembly-unit',
        loadChildren: () => import('./assembly-unit/assembly-unit.route').then(m => m.ASSEMBLY_UNIT_ROUTES),
        data: { title: 'Assembly Units' },
    },
    {
        path: 'check-config',
        loadChildren: () => import('./check-config/check-config.route').then(m => m.CHECK_CONFIG_ROUTES),
        data: { title: 'Check Configuration' },
    },
    {
        path: 'allocation-factor',
        loadChildren: () => import('./allocation-factor/allocation-factor.route').then(m => m.ALLOCATION_FACTOR_ROUTES),
        data: { title: 'Allocation Factors' },
    },
    {
        path: 'asset-type',
        loadChildren: () => import('./asset-type/asset-type.route').then(m => m.ASSET_TYPE_ROUTES),
        data: { title: 'Asset Types' },
    },
    {
        path: 'misc-charge',
        loadChildren: () => import('./misc-charge/misc-charge.route').then(m => m.MISC_CHARGE_ROUTES),
        data: { title: 'Miscellaneous Charges' },
    },
    {
        path: 'inventory-location',
        loadChildren: () => import('./inventory-location/inventory-location.route').then(m => m.INVENTORY_LOCATION_ROUTES),
        data: { title: 'Inventory Locations' },
    },
    {
        path: 'petty-cash-fund',
        loadChildren: () => import('./petty-cash-fund/petty-cash-fund.route').then(m => m.PETTY_CASH_FUND_ROUTES),
        data: { title: 'Petty Cash Funds' },
    },
    {
        path: 'cashflow-account',
        loadChildren: () => import('./cashflow-account/cashflow-account.route').then(m => m.CASHFLOW_ACCOUNT_ROUTES),
        data: { title: 'Cash Flow Accounts' },
    },
    {
        path: 'requisition-voucher',
        loadChildren: () => import('./requisition-voucher/requisition-voucher.route').then(m => m.REQUISITION_VOUCHER_ROUTES),
        data: { title: 'Purchase/Work Requests' },
    },
    {
        path: 'canvass',
        loadChildren: () => import('./canvass/canvass.route').then(m => m.CANVASS_ROUTES),
        data: { title: 'Canvass' },
    },
    {
        path: 'purchase-order',
        loadChildren: () => import('./purchase-order/purchase-order.route').then(m => m.PURCHASE_ORDER_ROUTES),
        data: { title: 'Purchase Orders' },
    },
    {
        path: 'job-order',
        loadChildren: () => import('./job-order/job-order.route').then(m => m.JOB_ORDER_ROUTES),
        data: { title: 'Job Orders' },
    },
    {
        path: 'jo-acceptance',
        loadChildren: () => import('./jo-acceptance/jo-acceptance.route').then(m => m.JO_ACCEPTANCE_ROUTES),
        data: { title: 'JO Certifications / Accomplishments' },
    },
    {
        path: 'payment-request',
        loadChildren: () => import('./payment-request/payment-request.route').then(m => m.PAYMENT_REQUEST_ROUTES),
        data: { title: 'Request for Payment' },
    },
    {
        path: 'quotation',
        loadChildren: () => import('./quotation/quotation.route').then(m => m.QUOTATION_ROUTES),
        data: { title: 'Quotation' },
    },
    {
        path: 'credit-card-purchase-request',
        loadChildren: () => import('./credit-card-purchase-request/credit-card-purchase-request.route').then(m => m.CREDIT_CARD_PURCHASE_REQUEST_ROUTES),
        data: { title: 'Credit Card Purchase Request' },
    },
    {
        path: 'account-setting',
        loadChildren: () => import('./account-setting/account-setting.route').then(m => m.ACCOUNT_SETTING_ROUTES),
        data: { title: 'Account Settings' },
    },
    {
        path: 'accounts-payable-voucher',
        loadChildren: () => import('./accounts-payable-voucher/accounts-payable-voucher.route').then(m => m.ACCOUNTS_PAYABLE_VOUCHER_ROUTES),
        data: { title: 'Accounts Payable Voucher' },
    },
    {
        path: 'disbursement',
        loadChildren: () => import('./disbursement/disbursement.route').then(m => m.DISBURSEMENT_ROUTES),
        data: { title: 'Disbursement' },
    },
    {
        path: 'general-journal',
        loadChildren: () => import('./general-journal/general-journal.route').then(m => m.GENERAL_JOURNAL_ROUTES),
        data: { title: 'General Journal' },
    },
    {
        path: 'adjustment-journal',
        loadChildren: () => import('./adjustment-journal/adjustment-journal.route').then(m => m.ADJUSTMENT_JOURNAL_ROUTES),
        data: { title: 'Adjustment Journal' },
    },
    {
        path: 'material-issuance',
        loadChildren: () => import('./material-issuance/material-issuance.route').then(m => m.MATERIAL_ISSUANCE_ROUTES),
        data: { title: 'Material Issuance' },
    },
    {
        path: 'other-account-receivable',
        loadChildren: () => import('./other-account-receivable/other-account-receivable.route').then(m => m.OTHER_ACCOUNT_RECEIVABLE_ROUTES),
        data: { title: 'Other Account Receivable' },
    },
    {
        path: 'monthly-closing',
        loadChildren: () => import('./monthly-closing/monthly-closing.route').then(m => m.MONTHLY_CLOSING_ROUTES),
        data: { title: 'Monthly Closing' },
    },
    {
        path: 'document-cancellation',
        loadChildren: () => import('./document-cancellation/document-cancellation.route').then(m => m.DOCUMENT_CANCELLATION_ROUTES),
        data: { title: 'Document Cancellation' },
    },
    {
        path: 'approve-vouchers',
        loadChildren: () => import('./approve-vouchers/approve-vouchers.route').then(m => m.APPROVE_VOUCHERS_ROUTES),
        data: { title: 'Approve Vouchers' },
    },
    {
        path: 'energy-sales',
        loadChildren: () => import('./energy-sales/energy-sales.route').then(m => m.ENERGY_SALES_ROUTES),
        data: { title: 'Energy Sales' },
    },
    {
        path: 'cash-receipts',
        loadChildren: () => import('./cash-receipts/cash-receipts.route').then(m => m.CASH_RECEIPTS_ROUTES),
        data: { title: 'Cash Receipts' },
    },
    {
        path: 'bank-deposit',
        loadChildren: () => import('./bank-deposit/bank-deposit.route').then(m => m.BANK_DEPOSIT_ROUTES),
        data: { title: 'Bank Deposits' },
    },
    {
        path: 'prepayment',
        loadChildren: () => import('./prepayment/prepayment.route').then(m => m.PREPAYMENT_ROUTES),
        data: { title: 'Prepayments and Other Amortizations' },
    },
    {
        path: 'bank-reconciliation',
        loadChildren: () => import('./bank-reconciliation/bank-reconciliation.route').then(m => m.BANK_RECONCILIATION_ROUTES),
        data: { title: 'Bank Reconciliation' },
    },
    {
        path: 'budget',
        loadChildren: () => import('./budget/budget.route').then(m => m.BUDGET_ROUTES),
        data: { title: 'Cash Flow Budget' },
    },
    {
        path: 'check-releasing',
        loadChildren: () => import('./check-releasing/check-releasing.route').then(m => m.CHECK_RELEASING_ROUTES),
        data: { title: 'Check Releasing' },
    },
    {
        path: 'voucher-cashflow',
        loadChildren: () => import('./voucher-cashflow/voucher-cashflow.route').then(m => m.VOUCHER_CASHFLOW_ROUTES),
        data: { title: 'CashFlow Account Setting' },
    },
    {
        path: 'pcv',
        loadChildren: () => import('./pcv/pcv.route').then(m => m.PCV_ROUTES),
        data: { title: 'Petty Cash Voucher' },
    },
    {
        path: 'ca',
        loadChildren: () => import('./ca/ca.route').then(m => m.CA_ROUTES),
        data: { title: 'Cash Advance' },
    },
    {
        path: 'budget-line-item',
        loadChildren: () => import('./budget-line-item/budget-line-item.route').then(m => m.BUDGET_LINE_ITEM_ROUTES),
        data: { title: 'Budget Line Item' },
    },
    {
        path: 'ca-liquidation',
        loadChildren: () => import('./ca-liquidation/ca-liquidation.route').then(m => m.CA_LIQUIDATION_ROUTES),
        data: { title: 'CA Liquidation' },
    },
    {
        path: 'iemop-billing',
        loadChildren: () => import('./iemop-billing/iemop-billing.route').then(m => m.IEMOP_BILLING_ROUTES),
        data: { title: 'Upload IEMOP Billing' },
    },
    {
        path: 'budget-sub-item',
        loadChildren: () => import('./budget-sub-item/budget-sub-item.route').then(m => m.BUDGET_SUB_ITEM_ROUTES),
        data: { title: 'Budget Sub Item' },
    },
    {
        path: 'pcl',
        loadChildren: () => import('./pcl/pcl.route').then(m => m.PCL_ROUTES),
        data: { title: 'Petty Cash Liquidation' },
    },
    {
        path: 'cpr',
        loadChildren: () => import('./cpr/cpr.route').then(m => m.CPR_ROUTES),
        data: { title: 'Asset Records (CPR)' },
    },
    {
        path: 'process-monthly-depreciation',
        loadChildren: () => import('./process-monthly-depreciation/process-monthly-depreciation.route').then(m => m.PROCESS_MONTHLY_DEPRECIATION_ROUTES),
        data: { title: 'Process Monthly Depreciation' },
    },
    {
        path: 'maintenance-record',
        loadChildren: () => import('./maintenance-record/maintenance-record.route').then(m => m.MAINTENANCE_RECORD_ROUTES),
        data: { title: 'Maintenance Records' },
    },
    {
        path: 'project',
        loadChildren: () => import('./project/project.route').then(m => m.PROJECT_ROUTES),
        data: { title: 'Projects' },
    },
    {
        path: 'cost-estimate',
        loadChildren: () => import('./cost-estimate/cost-estimate.route').then(m => m.COST_ESTIMATE_ROUTES),
        data: { title: 'Cost Estimates' },
    },
    {
        path: 'work-order',
        loadChildren: () => import('./work-order/work-order.route').then(m => m.WORK_ORDER_ROUTES),
        data: { title: 'Work Orders' },
    },
    {
        path: 'site-inspection-report',
        loadChildren: () => import('./site-inspection-report/site-inspection-report.route').then(m => m.SITE_INSPECTION_REPORT_ROUTES),
        data: { title: 'Site Inspection Reports' },
    },
    {
        path: 'project-acceptance-report',
        loadChildren: () => import('./project-acceptance-report/project-acceptance-report.route').then(m => m.PROJECT_ACCEPTANCE_REPORT_ROUTES),
        data: { title: 'Project Acceptance Reports' },
    },
    {
        path: 'project-acceptance-certification',
        loadChildren: () => import('./project-acceptance-certification/project-acceptance-certification.route').then(m => m.PROJECT_ACCEPTANCE_CERTIFICATION_ROUTES),
        data: { title: 'Project Acceptance Certifications' },
    },
    {
        path: 'rr',
        loadChildren: () => import('./rr/rr.route').then(m => m.RR_ROUTES),
        data: { title: 'Receiving Reports' },
    },
    {
        path: 'withdrawal',
        loadChildren: () => import('./withdrawal/withdrawal.route').then(m => m.WITHDRAWAL_ROUTES),
        data: { title: 'Stock Withdrawals' },
    },
    {
        path: 'stock-release',
        loadChildren: () => import('./stock-release/stock-release.route').then(m => m.STOCK_RELEASE_ROUTES),
        data: { title: 'Stock Releases' },
    },
    {
        path: 'mct',
        loadChildren: () => import('./mct/mct.route').then(m => m.MCT_ROUTES),
        data: { title: 'Material Credit Tickets' },
    },
    {
        path: 'stock-adjustment',
        loadChildren: () => import('./stock-adjustment/stock-adjustment.route').then(m => m.STOCK_ADJUSTMENT_ROUTES),
        data: { title: 'Stock Adjustments' },
    },
    {
        path: 'mst',
        loadChildren: () => import('./mst/mst.route').then(m => m.MST_ROUTES),
        data: { title: 'Material Salvage Tickets' },
    },
    {
        path: 'stock-transfer',
        loadChildren: () => import('./stock-transfer/stock-transfer.route').then(m => m.STOCK_TRANSFER_ROUTES),
        data: { title: 'Stock Transfers' },
    },
    {
        path: 'stock-receive',
        loadChildren: () => import('./stock-receive/stock-receive.route').then(m => m.STOCK_RECEIVE_ROUTES),
        data: { title: 'Receive Stock Transfer' },
    },
    {
        path: 'memorandum-receipt',
        loadChildren: () => import('./memorandum-receipt/memorandum-receipt.route').then(m => m.MEMORANDUM_RECEIPT_ROUTES),
        data: { title: 'Memorandum Receipts' },
    },
    {
        path: 'item-testing',
        loadChildren: () => import('./item-testing/item-testing.route').then(m => m.ITEM_TESTING_ROUTES),
        data: { title: 'Item Testing' },
    },
    {
        path: 'special-equipment-assignment',
        loadChildren: () => import('./special-equipment-assignment/special-equipment-assignment.route').then(m => m.SPECIAL_EQUIPMENT_ASSIGNMENT_ROUTES),
        data: { title: 'Special Equipment Assignments' },
    },
    {
        path: 'ifr',
        loadChildren: () => import('./ifr/ifr.route').then(m => m.IFR_ROUTES),
        data: { title: 'Items For Repair' },
    },
    {
        path: 'ih',
        loadChildren: () => import('./ih/ih.route').then(m => m.IH_ROUTES),
        data: { title: 'Item History' },
    },
    {
        path: 'return-memorandum-receipt',
        loadChildren: () => import('./return-memorandum-receipt/return-memorandum-receipt.route').then(m => m.RETURN_MEMORANDUM_RECEIPT_ROUTES),
        data: { title: 'Return Memorandum Receipts' },
    },
    {
        path: 'di',
        loadChildren: () => import('./di/di.route').then(m => m.DI_ROUTES),
        data: { title: 'Document Inquiry' },
    },
    {
        path: 'gl-account-inquiry',
        loadChildren: () => import('./gl-account-inquiry/gl-account-inquiry.route').then(m => m.GL_ACCOUNT_INQUIRY_ROUTES),
        data: { title: 'GL Account Inquiry' },
    },
    {
        path: 'sl-account-inquiry',
        loadChildren: () => import('./sl-account-inquiry/sl-account-inquiry.route').then(m => m.SL_ACCOUNT_INQUIRY_ROUTES),
        data: { title: 'SL Account Inquiry' },
    },
    {
        path: 'inventory-reports',
        loadChildren: () => import('./inventory-reports/inventory-reports.route').then(m => m.INVENTORY_REPORTS_ROUTES),
        data: { title: 'Inventory Reports' },
    },
    {
        path: 'accounting-reports',
        loadChildren: () => import('./accounting-reports/accounting-reports.route').then(m => m.ACCOUNTING_REPORTS_ROUTES),
        data: { title: 'Accounting Reports' },
    },
    {
        path: '',
        loadChildren: () => import('./layouts/layout.routes').then((mod) => mod.LAYOUT_ROUTES),
        canActivateChild: [roleGuard],
    },
];
