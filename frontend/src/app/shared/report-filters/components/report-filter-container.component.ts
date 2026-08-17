import { Component, Output, EventEmitter } from '@angular/core';
import { ReportFilterValue } from '../models/report-filter.model';

@Component({
    selector: 'app-report-filter-container',
    standalone: true,
    template: `<div class="row gy-2 align-items-center"><ng-content></ng-content></div>`
})
export class ReportFilterContainerComponent {

    private values: ReportFilterValue = {};

    @Output() valueChange = new EventEmitter<ReportFilterValue>();

    update(key: keyof ReportFilterValue, value: any): void {
        this.values[key] = value;
        this.valueChange.emit({ ...this.values });
    }

    reset(): void {
        this.values = {};
        this.valueChange.emit({});
    }
}
