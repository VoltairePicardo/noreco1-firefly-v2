import {Component, inject, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {MODAL_BUTTON_CANCEL, MODAL_BUTTON_CONTINUE} from '@/app/constants/app.constants';
import {AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {NgClass} from '@angular/common';

@Component({
  selector: 'app-add-check-modal',
    imports: [
        ReactiveFormsModule,
        NgClass
    ],
  templateUrl: './add-check-modal.component.html',
  styleUrl: './add-check-modal.component.scss'
})
export class AddCheckModalComponent implements OnInit {

    @Input() title: string = '';

    form!: FormGroup;

    private activeModal = inject(NgbActiveModal);
    private fb = inject(FormBuilder);

    ngOnInit(): void {
        this.form = this.fb.group({
            checkNumber: ['', Validators.required],
            bankName: ['', Validators.required],
            checkAmount: [
                null,
                [
                    Validators.required,
                    Validators.min(0.01),
                    this.twoDecimalValidator
                ]
            ],
            checkDate: ['', Validators.required]
        });
    }

    // Custom validator for max 2 decimals
    twoDecimalValidator(control: AbstractControl): { [key: string]: any } | null {
        const value = control.value;
        if (value === null || value === undefined || value === '') return null;

        // Allow numbers with max 2 decimals
        const regex = /^\d+(\.\d{1,2})?$/;
        return regex.test(value) ? null : { maxTwoDecimals: true };
    }

    continue(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.activeModal.close({
            action: MODAL_BUTTON_CONTINUE,
            data: this.form.value
        });
    }

    cancel(): void {
        this.activeModal.dismiss(MODAL_BUTTON_CANCEL);
    }

}
