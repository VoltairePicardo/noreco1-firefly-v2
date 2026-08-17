import { FormsModule } from '@angular/forms';
import { NgForOf } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgIcon } from '@ng-icons/core';
import { NgbTooltip } from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'app-age-filter',
    standalone: true,
    imports: [FormsModule, NgForOf, NgIcon, NgbTooltip],
    host: { '[class]': 'colClass' },
    template: `
        <div class="app-search">
            <select class="form-select form-control my-1 my-md-0"
                    [(ngModel)]="value"
                    ngbTooltip="Age"
                    (change)="onChange()">
                <option [ngValue]="0">Select Age</option>
                <option *ngFor="let option of ageOptions" [ngValue]="option.id">
                    {{ option.description }}
                </option>
            </select>
            <ng-icon name="tablerCalendarStats" class="app-search-icon text-muted"></ng-icon>
        </div>
    `
})
export class AgeFilterComponent {

    @Input() value: number = 0;
    @Input() colClass = 'col-md-2';
    @Output() valueChange = new EventEmitter<number>();

    ageOptions: { id: number; description: string }[] = [
        { id: 1, description: '12 Months and Below' },
        { id: 2, description: '13-24 Months' },
        { id: 3, description: '25-36 Months' },
        { id: 4, description: '37-48 Months' },
        { id: 5, description: '49-60 Months' },
        { id: 6, description: 'Above 60 Months' }
    ];

    onChange(): void {
        this.valueChange.emit(this.value);
    }
}
