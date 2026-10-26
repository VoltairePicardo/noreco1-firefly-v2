import { Directive, ElementRef, HostListener } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Directive({
    selector: 'input[appNumericFormat]',
    standalone: true,
    providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: NumericFormatDirective, multi: true }]
})
export class NumericFormatDirective implements ControlValueAccessor {
    private onChange: (v: number | null) => void = () => {};
    private onTouched: () => void = () => {};

    constructor(private el: ElementRef<HTMLInputElement>) {}

    @HostListener('input')
    onInput(): void {
        let raw = this.el.nativeElement.value.replace(/[^0-9.]/g, '');
        // Only one decimal point allowed
        const dot = raw.indexOf('.');
        if (dot !== -1) raw = raw.slice(0, dot + 1) + raw.slice(dot + 1).replace(/\./g, '');

        const parts = raw.split('.');
        parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',');
        this.el.nativeElement.value = parts.join('.');

        this.onChange(raw === '' ? null : parseFloat(raw));
    }

    @HostListener('blur')
    onBlur(): void {
        this.onTouched();
        const raw = this.el.nativeElement.value.replace(/[^0-9.]/g, '');
        const num = parseFloat(raw);
        if (!isNaN(num)) {
            this.el.nativeElement.value = num.toLocaleString('en-US', { minimumFractionDigits: 0, maximumFractionDigits: 2 });
        } else {
            this.el.nativeElement.value = '';
        }
    }

    writeValue(value: number | null): void {
        if (value != null && !isNaN(Number(value))) {
            this.el.nativeElement.value = Number(value).toLocaleString('en-US', { minimumFractionDigits: 0, maximumFractionDigits: 2 });
        } else {
            this.el.nativeElement.value = '';
        }
    }

    registerOnChange(fn: (v: number | null) => void): void { this.onChange = fn; }
    registerOnTouched(fn: () => void): void { this.onTouched = fn; }
}
