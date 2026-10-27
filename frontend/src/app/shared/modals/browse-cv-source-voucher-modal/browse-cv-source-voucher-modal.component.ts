import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Observable } from 'rxjs';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { NG_ICON_DIRECTIVES, provideIcons } from '@ng-icons/core';
import { tablerSearch } from '@ng-icons/tabler-icons';
import { DisbursementService } from '@/app/pages/disbursement/disbursement.service';

export type CvSourceVoucherType = 'APV' | 'CA' | 'JV' | 'RR' | 'JOA' | 'RFP';

export interface CvVoucherInstallmentDetail {
    id: number;
    dueDate?: string;
    amount?: number;
}

export interface CvVoucherDto {
    id?: number;
    localCode?: string;
    amount?: number;
    particulars?: string;
    voucherDate?: string;
    preparedBy?: string;
    slentityAccountNo?: number;
    slentityName?: string;
    transId?: number;
    hasTax?: boolean;
    forInstallment?: boolean;
    numberOfPayments?: number;
    installmentDetails?: CvVoucherInstallmentDetail[];
}

const VOUCHER_TYPE_OPTIONS: { value: CvSourceVoucherType; label: string }[] = [
    { value: 'APV', label: 'Account Payable Voucher' },
    { value: 'CA', label: 'Cash Advance' },
    { value: 'JV', label: 'Journal Voucher' },
    { value: 'RR', label: 'Receiving Report' },
    { value: 'JOA', label: 'JO Acceptance' },
    { value: 'RFP', label: 'Request for Payment' },
];

@Component({
    selector: 'app-browse-cv-source-voucher-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, ReactiveFormsModule, NG_ICON_DIRECTIVES],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerSearch })],
    templateUrl: './browse-cv-source-voucher-modal.component.html',
})
export class BrowseCvSourceVoucherModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private service = inject(DisbursementService);
    private destroyRef = inject(DestroyRef);
    private cdr = inject(ChangeDetectorRef);

    voucherType: CvSourceVoucherType = 'APV';

    protected readonly typeOptions = VOUCHER_TYPE_OPTIONS;
    protected readonly typeControl = new FormControl<CvSourceVoucherType>('APV', { nonNullable: true });
    protected readonly searchControl = new FormControl('', { nonNullable: true });

    protected vouchers: CvVoucherDto[] = [];
    protected loading = false;

    protected title(): string {
        return `Browse ${this.typeOptions.find(o => o.value === this.typeControl.value)?.label}`;
    }

    ngOnInit(): void {
        this.typeControl.setValue(this.voucherType, { emitEvent: false });
        this.typeControl.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
            this.loadData();
        });
        this.loadData();
    }

    protected onSearch(): void {
        this.loadData();
    }

    protected select(voucher: CvVoucherDto): void {
        this.activeModal.close({ action: 'select', data: voucher, voucherType: this.typeControl.value });
    }

    protected loadData(): void {
        this.loading = true;
        this.cdr.markForCheck();
        const search = this.searchControl.value.trim();
        this.requestFor(this.typeControl.value, search, 0, 500)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: ({ content }) => {
                    this.vouchers = content ?? [];
                    this.loading  = false;
                    this.cdr.markForCheck();
                },
                error: () => {
                    this.vouchers = [];
                    this.loading  = false;
                    this.cdr.markForCheck();
                },
            });
    }

    private requestFor(type: CvSourceVoucherType, q: string, page: number, size: number): Observable<any> {
        switch (type) {
            case 'APV': return this.service.getApprovedApvForCv(q, page, size);
            case 'CA':  return this.service.getApprovedCaForCv(q, page, size);
            case 'JV':  return this.service.getApprovedJvForCv(q, page, size);
            case 'RR':  return this.service.getApprovedRrForCv(q, page, size);
            case 'JOA': return this.service.getApprovedJoaForCv(q, page, size);
            case 'RFP': return this.service.getApprovedRfpForCv(q, page, size);
        }
    }
}
