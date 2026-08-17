import { Component, inject, Input, signal } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { ApproveVouchersService } from '@/app/pages/approve-vouchers/approve-vouchers.service';
import { provideIcons } from '@ng-icons/core';
import { tablerCheck, tablerX } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-approve-voucher-modal',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerCheck, tablerX })],
    templateUrl: './approve-voucher-modal.component.html'
})
export class ApproveVoucherModalComponent {
    @Input() voucher: any = null;

    remarks      = '';
    isSubmitting = signal(false);

    activeModal        = inject(NgbActiveModal);
    private service      = inject(ApproveVouchersService);
    private alertService = inject(AlertService);

    approve(): void {
        if (!this.voucher) return;
        this.isSubmitting.set(true);
        const payload = {
            documentId:   this.voucher.id,
            remarks:      this.remarks,
            documentType: this.voucher.documentCode || ''
        };
        this.service.process(payload).subscribe({
            next: (res: any) => {
                this.isSubmitting.set(false);
                if (res?.success) {
                    this.activeModal.close({ action: 'approved' });
                } else {
                    this.alertService.error('Approve Vouchers', 'Approve', res?.failureMessage || '');
                }
            },
            error: () => {
                this.isSubmitting.set(false);
                this.alertService.error('Approve Vouchers', 'Approve', '');
            }
        });
    }
}
