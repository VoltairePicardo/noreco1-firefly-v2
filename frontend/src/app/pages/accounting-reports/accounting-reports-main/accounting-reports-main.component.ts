import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';

interface ReportOption {
    label: string;
    route: string;
}

interface ReportCategory {
    title: string;
    icon: string;
    options: ReportOption[];
    selected: string;
}

@Component({
    selector: 'app-accounting-reports-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './accounting-reports-main.component.html'
})
export class AccountingReportsMainComponent {
    module   = 'Accounting Reports';
    menuLink = 'accounting-reports';

    private router = inject(Router);

    categories: ReportCategory[] = [
        {
            title: 'Financial Statements (NEA)',
            icon: 'tablerFileAnalytics',
            selected: '',
            options: [
                { label: 'Trial Balance',                  route: 'trial-balance-nea' },
                { label: 'Statement of Operations',        route: 'income-statement-nea' },
                { label: 'Balance Sheet',                  route: 'balance-sheet-nea' },
                { label: 'Cash Flow Statement',            route: 'cashflow-statement-nea' },
                { label: 'Trial Balance - Audited',        route: 'trial-balance-nea-audited' },
            ]
        },
        {
            title: 'Financial Statements (BSUP)',
            icon: 'tablerReportMoney',
            selected: '',
            options: [
                { label: 'Trial Balance DET',              route: 'trial-balance' },
                { label: 'Transaction Summary',            route: 'transaction-summary' },
                { label: 'Balance Sheet',                  route: 'balance-sheet' },
                { label: 'Income Statement',               route: 'income-statement' },
                { label: 'Cash Flow Statement (BSUP)',     route: 'cashflow-statement-bsup' },
            ]
        },
        {
            title: 'Registers',
            icon: 'tablerList',
            selected: '',
            options: [
                { label: 'APV Register',                   route: 'apv-register' },
                { label: 'CV Register',                    route: 'cv-register' },
                { label: 'JV Register',                    route: 'jv-register' },
                { label: 'AJ Register',                    route: 'aj-register' },
                { label: 'Material Issue Register',        route: 'mir-register' },
                { label: 'Sales Register',                 route: 'sales-register' },
                { label: 'Cash Receipts Register',         route: 'cash-register' },
            ]
        },
        {
            title: 'Summaries',
            icon: 'tablerLayoutList',
            selected: '',
            options: [
                { label: 'RV Summary',                     route: 'rv-summary' },
                { label: 'PO Summary',                     route: 'po-summary' },
                { label: 'JO Summary',                     route: 'jo-summary' },
                { label: 'JOA Summary',                    route: 'joa-summary' },
                { label: 'Canvass Summary',                route: 'canvass-summary' },
                { label: 'PR Summary',                     route: 'pr-summary' },
                { label: 'Prepayment Expense Summary',     route: 'pe-summary' },
                { label: 'Depreciation Summary',           route: 'depreciation-summary' },
                { label: 'Depreciation Schedule',          route: 'depreciation-schedule' },
                { label: 'Accounts Payable Aging',         route: 'accounts-payable-aging' },
                { label: 'Pending Voucher List',           route: 'pending-voucher-list' },
                { label: 'Quotation Summary',              route: 'quotation-summary' },
                { label: 'Summary of Petty Cash Vouchers', route: 'pcv-summary' },
                { label: 'Check Listing',                  route: 'check-list' },
                { label: 'Cash Flow Detail',               route: 'cash-flow-detail' },
                { label: 'Pending Purchase Requests',      route: 'pending-purchase-request' },
            ]
        },
        {
            title: 'Support Module Reports',
            icon: 'tablerTool',
            selected: '',
            options: [
                { label: 'Summary of Construction Work In Progress', route: 'work-in-progress' },
                { label: 'Aging of Work Order',                      route: 'work-order' },
                { label: 'Work Order Transaction',         route: 'work-order-transaction' },
                { label: 'PCF Ledger',                     route: 'pcf-ledger' },
                { label: 'Unliquidated Cash Advance',      route: 'unliquidated-ca' },
            ]
        },
        {
            title: 'BIR',
            icon: 'tablerReceipt2',
            selected: '',
            options: [
                { label: 'BIR Alphalist',                  route: 'bir-alphalist' },
                { label: 'BIR Form 1601E',                 route: 'bir-form-1601e' },
            ]
        },
        {
            title: 'Asset Management',
            icon: 'tablerBuildingWarehouse',
            selected: '',
            options: [
                { label: 'Maintenance Record Summary',     route: 'maintenance-record-summary' },
                { label: 'Asset Ledger',                   route: 'asset-ledger' },
                { label: 'Asset Monitoring Sheet',         route: 'asset-monitoring-sheet' },
            ]
        },
    ];

    onSelectionChange(category: ReportCategory): void {
        if (category.selected) {
            this.router.navigate(['/', this.menuLink, category.selected]);
        }
    }
}
