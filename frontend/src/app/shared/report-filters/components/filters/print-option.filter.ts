import { FormsModule } from '@angular/forms';
import { NgForOf } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgIcon } from '@ng-icons/core';
import { NgbTooltip } from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'app-print-option-filter',
    standalone: true,
    imports: [FormsModule, NgIcon, NgbTooltip],
    host: { '[class]': 'colClass' },
    template: `
        <div class="app-search">
            <select class="form-select form-control my-1 my-md-0"
                    [(ngModel)]="value"
                    ngbTooltip="Print Option"
                    (change)="onChange()">
                <option [ngValue]="0">Select Option</option>
                @for(option of options; track option.id) {
                    <option [ngValue]="option.id">
                        {{option.id}} - {{ option.description }}
                    </option>
                }
            </select>
            <ng-icon name="tablerPrinter" class="app-search-icon text-muted"></ng-icon>
        </div>
    `
})
export class PrintOptionFilterComponent {

    @Input() value: number = 0;
    @Input() colClass = 'col-md-2';
    @Output() valueChange = new EventEmitter<number>();

    options: { id: number; description: string }[] = [
        { id: 1, description: 'Printed' },
        { id: 2, description: 'Not Printed' },
    ];

    onChange(): void {
        this.valueChange.emit(this.value);
    }
}
