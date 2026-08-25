import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { FlatpickrDefaults, FlatpickrModule } from 'angularx-flatpickr';
import { VoucherCashflowService } from '../voucher-cashflow.service';
import { fmtDate, monthStart, monthEnd } from '@/app/shared/utils/date.utils';

@Component({
    selector: 'app-voucher-cashflow-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrModule],
    providers: [...SHARED_PROVIDERS, FlatpickrDefaults],
    templateUrl: './voucher-cashflow-main.component.html'
})
export class VoucherCashflowMainComponent {
    module    = 'Cash Flow Account Settings';
    subModule = 'Vouchers List';
    menuLink  = 'voucher-cashflow';

    records   = signal<any[]>([]);
    isLoading = signal(false);

    page     = 1;
    pageSize = 10;

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.records().slice(start, start + this.pageSize);
    }

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    fromDate = '';
    toDate   = '';

    private service      = inject(VoucherCashflowService);
    private alertService = inject(AlertService);
    private router       = inject(Router);

    ngOnInit(): void {
        this.setDefaultDates();
        this.load();
    }

    setDefaultDates(): void {
        this.fromDate = monthStart();
        this.toDate   = monthEnd();
    }

    toDateString(d: Date): string {
        return fmtDate(d);
    }

    load(): void {
        if (!this.fromDate || !this.toDate) return;
        this.isLoading.set(true);
        this.service.getAllVouchers(this.fromDate, this.toDate).subscribe({
            next: (data) => {
                this.records.set(data || []);
                this.page = 1;
                this.isLoading.set(false);
            },
            error: () => {
                this.alertService.error(this.module, 'Load', '');
                this.isLoading.set(false);
            }
        });
    }

    reset(): void {
        this.setDefaultDates();
        this.load();
    }

    goToSetup(rec: any): void {
        this.router.navigate(['/' + this.menuLink, rec.documentTypeCode, rec.voucherId, 'setup']);
    }
}
