import { Component, inject } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { AlertService } from '@/app/shared/services/alert.service';
import { PcvService } from '@/app/pages/pcv/pcv.service';

@Component({
    selector: 'app-replenish-cv-modal',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './replenish-cv-modal.component.html'
})
export class ReplenishCvModalComponent {
    activeModal    = inject(NgbActiveModal);
    private service      = inject(PcvService);
    private alertService = inject(AlertService);

    from = '';
    to   = '';
    code = '';

    items      : any[] = [];
    loading            = false;
    selectedCv : any   = null;
    processing         = false;

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y', static: true };

    search(): void {
        this.loading    = true;
        this.selectedCv = null;
        this.service.getCheckVouchers(this.from, this.to, this.code).subscribe({
            next: (data) => { this.items = data || []; this.loading = false; },
            error: ()     => { this.loading = false; }
        });
    }

    select(item: any): void {
        this.selectedCv = this.selectedCv?.id === item.id ? null : item;
    }

    replenish(): void {
        if (!this.selectedCv || this.processing) return;
        this.processing = true;
        this.service.replenish({ transId: this.selectedCv.transactionId, checkAmount: this.selectedCv.checkAmount }).subscribe({
            next: (res) => {
                this.processing = false;
                if (res?.success) {
                    this.activeModal.close({ action: 'replenished' });
                } else {
                    this.alertService.error('Replenish', 'Replenish failed.', res?.failureMessage || '');
                }
            },
            error: () => {
                this.processing = false;
                this.alertService.error('Replenish', 'An error occurred.', '');
            }
        });
    }
}
