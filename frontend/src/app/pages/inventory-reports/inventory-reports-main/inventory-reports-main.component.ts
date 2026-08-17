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
    selector: 'app-inventory-reports-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './inventory-reports-main.component.html'
})
export class InventoryReportsMainComponent {
    module   = 'Inventory Reports';
    menuLink = 'inventory-reports';

    private router = inject(Router);

    categories: ReportCategory[] = [
        {
            title: 'Item Tracking',
            icon: 'tablerFileDescription',
            selected: '',
            options: [
                { label: 'Bin Card',                              route: 'bin-card' },
                { label: 'Stock Card',                            route: 'stock-card' },
                { label: 'MRTE Ledger',                           route: 'mrte-ledger' },
                { label: 'Ideal Quantity / Reorder Point',        route: 'ideal-quantity' },
            ]
        },
        {
            title: 'Inventory Summary',
            icon: 'tablerChartHistogram',
            selected: '',
            options: [
                { label: 'Inventory Balance',                     route: 'inventory-balance' },
                { label: 'Summary of Material Periodic Issuance', route: 'material-issuance-summary' },
            ]
        },
        {
            title: 'Withdrawals & Releases',
            icon: 'tablerArrowBarUp',
            selected: '',
            options: [
                { label: 'Summary of Stock Withdrawal',           route: 'withdrawal-summary' },
                { label: 'Summary of Stock Release',              route: 'release-summary' },
                { label: 'Summary of Special Equipment Releasing',route: 'special-equipment-release-summary' },
                { label: 'Summary of Special Equipment Pending',  route: 'special-equipment-pending-summary' },
            ]
        },
        {
            title: 'Transfers & Adjustments',
            icon: 'tablerArrowsTransferDown',
            selected: '',
            options: [
                { label: 'Summary of MCRT',                       route: 'mcrt-summary' },
                { label: 'Summary of Stock Adjustment',           route: 'adjustment-summary' },
                { label: 'Summary of MST',                        route: 'mst-summary' },
                { label: 'Summary of Stock Transfer',             route: 'transfer-summary' },
                { label: 'Summary of Received Stock Transfers',   route: 'receive-summary' },
            ]
        },
    ];

    onSelectionChange(category: ReportCategory): void {
        if (category.selected) {
            this.router.navigate(['/', this.menuLink, category.selected]);
        }
    }
}
