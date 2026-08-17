import { FormsModule } from '@angular/forms';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgIcon } from '@ng-icons/core';
import { NgbTooltip } from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'app-date-option-filter',
    imports: [FormsModule, NgIcon, NgbTooltip],
    host: { '[class]': 'colClass' },
    template: `
        <div class="app-search">
            <select class="form-select form-control my-1 my-md-0"
                    [(ngModel)]="value"
                    ngbTooltip="Date Option"
                    (change)="onChange()">
                @for (option of dateOptions; track option.id) {
                    <option [ngValue]="option.id">{{ option.name }}</option>
                }
            </select>
            <ng-icon name="tablerCalendar" class="app-search-icon text-muted"></ng-icon>
        </div>
    `
})
export class DateOptionFilterComponent {

    @Input() value: number = 1;
    @Input() colClass = 'col-md-2';
    @Output() valueChange = new EventEmitter<number>();

    dateOptions: { id: number; name: string }[] = [
        { id: 1, name: 'Posting Date' },
        { id: 2, name: 'Transaction Date' }
    ];

    onChange(): void {
        this.valueChange.emit(this.value);
    }
}
