import {FormsModule} from '@angular/forms';
import {NgForOf} from '@angular/common';
import {Component, EventEmitter, Input, Output} from '@angular/core';
import {NgIcon} from '@ng-icons/core';
import {HelperService} from '@/app/helpers/util';
import {NgbTooltip} from '@ng-bootstrap/ng-bootstrap';

type ReportType = { id: number; description: string };
type optionLabel = 'All' | 'SALES_REPORT_FOR_ACCOUNTING' | 'SALES_REPORT_FOR_BILLING' | 'SUMMARY_OF_ADJUSTMENTS';

@Component({
    selector:'app-report-type-filter',
    standalone:true,
    imports: [FormsModule, NgForOf, NgIcon, NgbTooltip],
    host: { '[class]': 'colClass' },
    template:`
 <div class="app-search">
     <select class="form-select form-control my-1 my-md-0"
             [(ngModel)]="value"
             ngbTooltip="Report Type"
             (change)="change()">
         <option [value]="0">Choose Report Type</option>
         <option *ngFor="let a of options" [value]="a.id">
             {{a.description}}
         </option>
     </select>
     <ng-icon name="tablerMap" class="app-search-icon text-muted"></ng-icon>
 </div>`
})
export class ReportTypeFilterComponent{

    @Input() value: number = 0;
    @Input() colClass = 'col-md-3';
    @Output() valueChange=new EventEmitter<number>();
    @Input() option: optionLabel = 'All';

    options: ReportType[] = [];

    private allTypes(): ReportType[] {
        return HelperService.billingReportTypes();
    }

    private salesReportForAccountingSet(): ReportType[] {
        return this.allTypes().filter(r => [1, 2, 3].includes(r.id));
    }

    private salesReportForBillingSet(): ReportType[] {
        return this.allTypes().filter(r => [1, 4, 5, 6].includes(r.id));
    }

    private summaryOfAdjustmentSet(): ReportType[] {
        return this.allTypes().filter(r => [2,6].includes(r.id));
    }

    ngOnInit() {
        switch (this.option) {
            case 'All':
                this.options = this.allTypes();
                break;
            case 'SALES_REPORT_FOR_ACCOUNTING':
                this.options = this.salesReportForAccountingSet();
                break;
            case 'SALES_REPORT_FOR_BILLING':
                this.options = this.salesReportForBillingSet();
                break;
            case 'SUMMARY_OF_ADJUSTMENTS':
                this.options = this.summaryOfAdjustmentSet();
                break;
        }

        if (!this.value && this.options.length) {
            this.value = this.options[0].id;
            this.valueChange.emit(this.value);
        }
    }

    change(){ this.valueChange.emit(this.value); }

}
