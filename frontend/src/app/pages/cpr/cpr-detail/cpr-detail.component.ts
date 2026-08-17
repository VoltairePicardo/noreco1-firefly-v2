import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { CprService } from '../cpr.service';
import { forkJoin } from 'rxjs';
import Swal from 'sweetalert2';

const MONTHS = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
];

@Component({
    selector: 'app-cpr-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './cpr-detail.component.html'
})
export class CprDetailComponent {
    module    = 'Asset Records (CPR)';
    subModule = 'Detail';
    menuLink  = 'cpr';
    id: any   = 0;

    data: any           = {};
    assetDetails: any[] = [];
    assetItems: any[]   = [];
    isLoading   = signal(false);
    formSubmit  = false;

    private service      = inject(CprService);
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
                if (!data?.id) {
                    this.isLoading.set(false);
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                    return;
                }
                this.data = data;
                forkJoin({
                    details: this.service.getDetails(this.id),
                    items:   this.service.getItems(this.id),
                }).subscribe({
                    next: ({ details, items }) => {
                        this.assetDetails = details || [];
                        this.assetItems   = items   || [];
                        this.isLoading.set(false);
                    },
                    error: () => { this.isLoading.set(false); }
                });
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Error', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    isActive(): boolean {
        return this.data?.status === 'ACTIVE';
    }

    monthName(val: any): string {
        if (!val) return '—';
        if (typeof val === 'string' && isNaN(Number(val))) return val;
        const n = Number(val);
        return MONTHS[n - 1] || String(val);
    }

    get totalValue(): number {
        return this.assetDetails.reduce((s, r) => s + (Number(r.value) || 0), 0);
    }
    get totalMonthlyDep(): number {
        return this.assetDetails.reduce((s, r) => s + (Number(r.monthlyDepreciation) || 0), 0);
    }
    get totalDepreciatedValue(): number {
        return this.assetDetails.reduce((s, r) => s + (Number(r.depreciatedValue) || 0), 0);
    }
    get totalRemainingValue(): number {
        return this.assetDetails.reduce((s, r) => s + (Number(r.remainingValue) || 0), 0);
    }

    retireAsset(): void {
        Swal.fire({
            title: 'Retire Asset',
            html: '<p>Are you sure you want to retire this asset?</p><p>Please enter retirement remarks:</p>',
            input: 'textarea',
            inputPlaceholder: 'Enter retirement remarks...',
            inputAttributes: { 'aria-label': 'Retirement remarks' },
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Retire',
            cancelButtonText: 'Cancel',
            buttonsStyling: false,
            customClass: {
                confirmButton: 'btn btn-danger me-2',
                cancelButton: 'btn btn-light text-dark'
            },
            inputValidator: (value) => {
                if (!value?.trim()) return 'Retirement remarks are required.';
                return null;
            }
        }).then((result) => {
            if (result.isConfirmed) {
                const remarks = result.value?.trim() || '';
                this.performRetire(remarks);
            }
        });
    }

    private performRetire(remarks: string): void {
        this.formSubmit = true;
        const payload = {
            id:                this.data.id,
            retirementRemarks: remarks,
            assetDetails:      this.assetDetails
        };
        this.service.retire(payload).subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Asset Retired', '');
                    this.loadData();
                } else {
                    this.alertService.error(this.module, 'Retire Asset', res?.failureMessage || '');
                }
            },
            error: () => {
                this.formSubmit = false;
                this.alertService.error(this.module, 'Retire Asset', '');
            }
        });
    }

    accountLabel(account: any): string {
        if (!account) return '—';
        return (account.code || account.accountCode || '') + ' - ' + (account.title || account.accountTitle || account.accountDescription || '');
    }
}
