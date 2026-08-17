import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { AccountingReportsService } from '../accounting-reports.service';

@Component({
    selector: 'app-bir-form-1601e',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './bir-form-1601e.component.html'
})
export class BirForm1601EComponent {
    module   = 'BIR Form 1601E';
    menuLink = 'accounting-reports';

    selectedYear  = signal<number>(new Date().getFullYear());
    selectedMonth = signal<number>(new Date().getMonth() + 1);

    years: number[] = [];
    months = [
        { id: 1,  name: 'January'   }, { id: 2,  name: 'February'  }, { id: 3,  name: 'March'     },
        { id: 4,  name: 'April'     }, { id: 5,  name: 'May'       }, { id: 6,  name: 'June'      },
        { id: 7,  name: 'July'      }, { id: 8,  name: 'August'    }, { id: 9,  name: 'September' },
        { id: 10, name: 'October'   }, { id: 11, name: 'November'  }, { id: 12, name: 'December'  },
    ];

    authorizedRep: any = null;
    rows      = signal<any[]>([]);
    isLoading = signal(false);

    private downloadSvc  = inject(DownloadService);
    private alertService = inject(AlertService);
    private modalService = inject(NgbModal);
    private service      = inject(AccountingReportsService);

    ngOnInit(): void {
        const currentYear = new Date().getFullYear();
        for (let y = currentYear - 5; y <= currentYear + 2; y++) this.years.push(y);
    }

    browseAuthorizedRep(): void {
        const ref = this.modalService.open(BrowseEntityModalComponent, { size: 'lg', centered: true });
        ref.componentInstance.types = ['ENTITY_EMPLOYEE'];
        ref.result.then((entity) => { if (entity) this.authorizedRep = entity; }, () => {});
    }

    search(): void {
        const year  = this.selectedYear();
        const month = this.selectedMonth();
        if (!year || !month) {
            this.alertService.error(this.module, 'Validation', 'Please select year and month.');
            return;
        }
        this.isLoading.set(true);
        this.rows.set([]);
        this.service.getForm1601ESchedule(year, month).subscribe({
            next: (data) => { this.rows.set(data ?? []); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); }
        });
    }

    exportForm(): void {
        const year  = this.selectedYear();
        const month = this.selectedMonth();
        if (!year || !month) {
            this.alertService.error(this.module, 'Validation', 'Please select year and month.');
            return;
        }
        if (!this.authorizedRep) {
            this.alertService.error(this.module, 'Validation', 'Please select the authorized agent signatory.');
            return;
        }
        this.downloadSvc.print(`/reports/export/form-1601E/${year}/${month}`, { an: this.authorizedRep.accountNo });
    }

    exportSchedule(type: 'pdf' | 'xls'): void {
        const year  = this.selectedYear();
        const month = this.selectedMonth();
        if (!year || !month) {
            this.alertService.error(this.module, 'Validation', 'Please select year and month.');
            return;
        }
        this.downloadSvc.print(`/reports/export/form-1601E-schedule/${year}/${month}`, { type });
    }
}
