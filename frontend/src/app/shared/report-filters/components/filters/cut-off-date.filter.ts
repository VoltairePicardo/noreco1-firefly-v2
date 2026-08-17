import {Component, EventEmitter, Input, Output} from '@angular/core';
import { FormsModule } from '@angular/forms';
import {FlatpickrDirective, provideFlatpickrDefaults} from 'angularx-flatpickr';
import { DateHelper } from '@/app/helpers/date-helper';
import {NgIcon} from '@ng-icons/core';
import {NgbTooltip} from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'app-cutoff-date-filter',
    standalone: true,
    imports: [FormsModule, FlatpickrDirective, NgIcon, NgbTooltip],
    host: { '[class]': 'colClass' },
    providers: [provideFlatpickrDefaults()],
    template: `
    <div class="app-search">
        <input
            class="form-control"
            type="text"
            mwlFlatpickr
            [options]="{ dateFormat: 'F j, Y' }"
            [placeholder]="'Select '+tooltip"
            [(ngModel)]="value"
            [ngbTooltip]="tooltip"
            (ngModelChange)="onDateChange($event)" />
        <ng-icon name="tablerCalendarMinus" class="app-search-icon text-muted"></ng-icon>
    </div>
  `
})
export class CutOffDateFilterComponent {

    @Input() value: Date = DateHelper.dateToday();
    @Input() tooltip = 'Cut-off Date' ;
    @Input() colClass = 'col-md-3';

    @Output() valueChange = new EventEmitter<Date>();

    onDateChange(date: Date | string) {
        const pickedDate = date instanceof Date ? date : new Date(date);
        this.valueChange.emit(pickedDate);
    }
}
