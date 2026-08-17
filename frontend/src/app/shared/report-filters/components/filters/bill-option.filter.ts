import { FormsModule } from '@angular/forms';
import { NgForOf } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgIcon } from '@ng-icons/core';
import { NgbTooltip } from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'app-bill-option-filter',
    standalone: true,
    imports: [FormsModule, NgForOf, NgIcon, NgbTooltip],
    host: { '[class]': 'colClass' },
    template: `
        <div class="app-search">
            <select class="form-select form-control my-1 my-md-0"
                    [(ngModel)]="value"
                    ngbTooltip="Bill Option"
                    (change)="onChange()">
                <option *ngFor="let option of billOptions" [ngValue]="option.id">
                    {{ option.name }}
                </option>
            </select>
            <ng-icon name="tablerFileInvoice" class="app-search-icon text-muted"></ng-icon>
        </div>
    `
})
export class BillOptionFilterComponent {

    @Input() value: number = 1;
    @Input() colClass = 'col-md-2';
    @Output() valueChange = new EventEmitter<number>();

    billOptions: { id: number; name: string }[] = [
        { id: 1, name: 'Receivable' },
        { id: 2, name: 'Written Off' }
    ];

    onChange(): void {
        this.valueChange.emit(this.value);
    }
}
