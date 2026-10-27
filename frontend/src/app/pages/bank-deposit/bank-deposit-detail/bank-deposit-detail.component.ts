import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { BankDepositService } from '../bank-deposit.service';
import { DocumentLogsComponent } from '@/app/shared/components/document-logs/document-logs.component';
import { AttachmentsComponent } from '@/app/shared/components/attachments/attachments.component';
import { provideIcons } from '@ng-icons/core';
import { tablerArrowLeft, tablerPrinter, tablerEdit, tablerEye, tablerEyeOff } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-bank-deposit-detail',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS,
        DocumentLogsComponent, AttachmentsComponent
    ],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerArrowLeft, tablerPrinter, tablerEdit, tablerEye, tablerEyeOff })],
    templateUrl: './bank-deposit-detail.component.html'
})
export class BankDepositDetailComponent {
    module    = 'Bank Deposit';
    subModule = 'Detail';
    menuLink  = 'bank-deposit';
    id: any   = 0;
    data: any = {};
    attachments: any[] = [];
    showLogs  = false;
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
                    if (data.transId) { this.loadAttachments(data.transId); }
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

    loadAttachments(transId: number): void {
        this.service.getFiles(transId).subscribe({
            next: (files) => { this.attachments = files || []; },
            error: () => { this.attachments = []; }
        });
    }

    get totalAmount(): number {
        return (Number(this.data.cashAmount) || 0) + (Number(this.data.checkAmount) || 0);
    }

    fileUrl = (fileId: number): string => this.service.fileUrl(fileId);

    toggleLogs(): void { this.showLogs = !this.showLogs; }

    print(): void { this.service.print(this.id); }
}
