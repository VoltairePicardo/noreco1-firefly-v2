import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { PrepaymentService } from '../prepayment.service';
import { provideIcons } from '@ng-icons/core';
import { tablerArrowLeft, tablerEdit, tablerLink, tablerSearch } from '@ng-icons/tabler-icons';
import Swal from 'sweetalert2';

@Component({
    selector: 'app-prepayment-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerArrowLeft, tablerEdit, tablerLink, tablerSearch })],
    templateUrl: './prepayment-detail.component.html'
})
export class PrepaymentDetailComponent {
    module    = 'Prepayments and Other Amortizations';
    subModule = 'Detail';
    menuLink  = 'prepayment';
    id: any   = 0;
    data: any = {};
    isLoading = signal(false);

    // Voucher linking
    linkMode       = false;
    voucherQuery   = '';
    voucherPage    = 0;
    voucherResult: any  = null;
    selectedVoucher: any = null;
    calcCost: any       = null;
    linkSubmit      = false;

    private service      = inject(PrepaymentService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.id = params.get('id');
            if (this.id && /^\d+$/.test(String(this.id))) {
                this.loadData();
            }
        });
    }

    loadData(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.data = data;
                } else {
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Error', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    getStatus(): string {
        return (Number(this.data.balance) || 0) > 0 ? 'OPEN' : 'CLOSED';
    }

    isEditable(): boolean {
        return !this.data?.hasPpd;
    }

    // ─── Voucher Linking ────────────────────────────────────────────

    openLinkVoucher(): void {
        this.linkMode       = true;
        this.voucherQuery   = '';
        this.voucherPage    = 0;
        this.selectedVoucher = null;
        this.calcCost        = null;
        this.loadVouchers();
    }

    loadVouchers(): void {
        this.service.getVouchersForLinking(this.data.accountNo, this.voucherQuery, this.voucherPage).subscribe({
            next: (res) => this.voucherResult = res,
            error: () => {}
        });
    }

    searchVouchers(): void {
        this.voucherPage = 0;
        this.loadVouchers();
    }

    voucherPageChange(p: number): void {
        this.voucherPage = p - 1;
        this.loadVouchers();
    }

    selectVoucher(v: any): void {
        this.selectedVoucher = v;
        this.calcCost        = null;
        // v row: [0:transId, 1:code, 2:documentTypeId, 3:voucherDate, 4:amount, 5:particulars]
        const transId    = v[0];
        const accountId  = this.data.prepaymentAccount?.id;
        if (!accountId) return;
        this.service.calculateCost(this.id, transId, accountId).subscribe({
            next: (cost) => this.calcCost = cost,
            error: () => {}
        });
    }

    confirmLink(): void {
        if (!this.selectedVoucher || !this.calcCost) return;
        Swal.fire({
            title: 'Confirm Voucher Link',
            html: `Total Cost: <b>${Number(this.calcCost.totalCost).toFixed(2)}</b><br>
                   Monthly Cost: <b>${Number(this.calcCost.monthlyCost).toFixed(2)}</b><br>
                   Balance: <b>${Number(this.calcCost.balance).toFixed(2)}</b>`,
            icon: 'question',
            showCancelButton: true,
            confirmButtonText: 'Confirm',
            cancelButtonText: 'Cancel',
            buttonsStyling: false,
            customClass: { confirmButton: 'btn btn-primary me-2', cancelButton: 'btn btn-light text-dark' }
        }).then(result => {
            if (!result.isConfirmed) return;
            this.linkSubmit = true;
            const payload = {
                prepayment:         { id: this.id },
                totalCost:          this.calcCost.totalCost,
                monthlyCost:        this.calcCost.monthlyCost,
                balance:            this.calcCost.balance,
                voucherTransaction: { id: this.selectedVoucher[0] },
                documentType:       this.selectedVoucher[2] ? { id: this.selectedVoucher[2] } : null
            };
            this.service.linkVoucher(payload).subscribe({
                next: (res) => {
                    this.linkSubmit = false;
                    if (res?.success !== false) {
                        this.alertService.success(this.module, 'Voucher Linked', '');
                        this.linkMode = false;
                        this.loadData();
                    } else {
                        this.alertService.error(this.module, 'Link Failed', res?.failureMessage || '');
                    }
                },
                error: () => {
                    this.linkSubmit = false;
                    this.alertService.error(this.module, 'Link Failed', '');
                }
            });
        });
    }

    getMonthName(month: number): string {
        const months = [
            'January', 'February', 'March', 'April', 'May', 'June',
            'July', 'August', 'September', 'October', 'November', 'December'
        ];
        return months[month] || String(month);
    }
}
