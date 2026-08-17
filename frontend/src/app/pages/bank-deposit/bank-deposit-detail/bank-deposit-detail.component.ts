import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { BankDepositService } from '../bank-deposit.service';
import { provideIcons } from '@ng-icons/core';
import { tablerArrowLeft, tablerPrinter, tablerEdit, tablerPlus } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-bank-deposit-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerArrowLeft, tablerPrinter, tablerEdit, tablerPlus })],
    templateUrl: './bank-deposit-detail.component.html'
})
export class BankDepositDetailComponent {
    module    = 'Bank Deposit';
    subModule = 'Detail';
    menuLink  = 'bank-deposit';
    id: any   = 0;
    data: any = {};
    attachments: any[] = [];
    isLoading = signal(false);

    private service      = inject(BankDepositService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.id = params.get('id');
            if (this.id && /^\d+$/.test(String(this.id))) { this.loadData(); }
        });
    }

    loadData(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.data = data;
                    this.loadAttachments();
                } else {
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Error', ''); this.router.navigate(['/' + this.menuLink]); }
        });
    }

    loadAttachments(): void {
        this.service.getFiles(this.id).subscribe({
            next: (files) => { this.attachments = files || []; },
            error: () => { this.attachments = []; }
        });
    }

    fileUrl(fileId: number): string {
        return this.service.fileUrl(fileId);
    }

    isEditable(): boolean {
        const s = this.data?.documentStatus?.status || this.data?.status || '';
        return s === 'Document Created' || s === 'Returned to Creator';
    }

    get totalAmount(): number {
        return (Number(this.data.cashAmount) || 0) + (Number(this.data.checkAmount) || 0);
    }

    print(): void {
        this.service.print(this.id);
    }
}
