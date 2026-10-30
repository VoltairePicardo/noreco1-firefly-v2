import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';

@Component({
    selector: 'app-summary-of-taxes-withheld',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './summary-of-taxes-withheld.component.html'
})
export class SummaryOfTaxesWithheldComponent {
    module   = 'Summary of Taxes Withheld';
    menuLink = 'accounting-reports';

    selectedYear     = signal<number>(new Date().getFullYear());
    selectedMonth    = signal<number>(new Date().getMonth() + 1);

    years: number[] = [];
    months = [
        { id: 1,  name: 'January'   }, { id: 2,  name: 'February'  }, { id: 3,  name: 'March'     },
        { id: 4,  name: 'April'     }, { id: 5,  name: 'May'       }, { id: 6,  name: 'June'      },
        { id: 7,  name: 'July'      }, { id: 8,  name: 'August'    }, { id: 9,  name: 'September' },
        { id: 10, name: 'October'   }, { id: 11, name: 'November'  }, { id: 12, name: 'December'  },
    ];

    notedBy: any    = null;
    approvedBy: any = null;
    notedByError    = '';
    approvedByError = '';

    private downloadSvc  = inject(DownloadService);
    private alertService = inject(AlertService);
    private modalService = inject(NgbModal);

    ngOnInit(): void {
        const currentYear = new Date().getFullYear();
        for (let y = currentYear - 5; y <= currentYear + 2; y++) this.years.push(y);
    }

    browseNotedBy(): void {
        const ref = this.modalService.open(BrowseEntityModalComponent, { size: 'lg', centered: true });
        ref.componentInstance.entityTypes = [1];
        ref.result.then((result) => { if (result?.action === 'select' && result?.data) this.notedBy = result.data; }, () => {});
    }

    browseApprovedBy(): void {
        const ref = this.modalService.open(BrowseEntityModalComponent, { size: 'lg', centered: true });
        ref.componentInstance.entityTypes = [1];
        ref.result.then((result) => { if (result?.action === 'select' && result?.data) this.approvedBy = result.data; }, () => {});
    }

    export(type: 'pdf' | 'xls'): void {
        const year  = this.selectedYear();
        const month = this.selectedMonth();
        if (!year || !month) {
            this.alertService.error(this.module, 'Validation', 'Please select year and month.');
            return;
        }
        this.notedByError    = this.notedBy    ? '' : 'Please select an officer.';
        this.approvedByError = this.approvedBy ? '' : 'Please select an officer.';
        if (this.notedByError || this.approvedByError) {
            this.alertService.error(this.module, 'Validation', 'Please select all required officers.');
            return;
        }
        this.downloadSvc.print(
            `/reports/export/summary-of-taxes-withheld/${year}/${month}/${this.notedBy.accountNo}/${this.approvedBy.accountNo}`,
            { type }
        );
    }
}
