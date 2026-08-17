import {Component, EventEmitter, Input, Output} from '@angular/core';
import { FormsModule } from '@angular/forms';
import {FlatpickrDirective, provideFlatpickrDefaults} from 'angularx-flatpickr';
import { DateHelper } from '@/app/helpers/date-helper';
import {NgIcon} from '@ng-icons/core';
import {DateRange} from '@/app/shared/report-filters/models/report-filter.model';
import {NgbTooltip} from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'app-date-range-filter',
    standalone: true,
    imports: [FormsModule, FlatpickrDirective, NgIcon, NgbTooltip],
    host: { '[class]': 'colClass' },
    providers: [provideFlatpickrDefaults()],
    template: `
    <div class="d-flex gap-2">
        <div class="app-search flex-fill">
            <input
                class="form-control"
                type="text"
                mwlFlatpickr
                [dateFormat]="'F j, Y'"
                ngbTooltip="Start Date"
                [placeholder]="'Start Date'"
                [(ngModel)]="value.startDate"
                (ngModelChange)="onDateChange('start', $event)" />
            <ng-icon name="tablerCalendarMinus" class="app-search-icon text-muted"></ng-icon>
        </div>
        <div class="app-search flex-fill">
            <input
                class="form-control"
                type="text"
                mwlFlatpickr
                [dateFormat]="'F j, Y'"
                [placeholder]="'End Date'"
                ngbTooltip="End Date"
                [(ngModel)]="value.endDate"
                (ngModelChange)="onDateChange('end', $event)" />
            <ng-icon name="tablerCalendarMinus" class="app-search-icon text-muted"></ng-icon>
        </div>
    </div>
  `
})
export class DateRangeFilterComponent {
    @Input() value: DateRange = {
        startDate: DateHelper.startOfMonth(),
        endDate: DateHelper.endOfMonth()
    };

    @Input() colClass = 'col-md-4';

    @Output() valueChange = new EventEmitter<DateRange>();

    internalStart!: Date;
    internalEnd!: Date;

    ngOnInit() {
        this.internalStart = new Date(this.value.startDate);
        this.internalEnd = new Date(this.value.endDate);
    }

    onDateChange(type: 'start' | 'end', date: Date | string) {
        if (!date) return;

        const pickedDate = date instanceof Date ? date : new Date(date);

        if (type === 'start') this.internalStart = pickedDate;
        if (type === 'end') this.internalEnd = pickedDate;

        const newRange: DateRange = {
            startDate: this.internalStart,
            endDate: this.internalEnd
        };

        this.valueChange.emit(newRange);
    }

}
