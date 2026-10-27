import {Directive, ElementRef, forwardRef, inject} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';

const MAX_DIGITS = 12;

@Directive({
    selector: 'input[appAtmAmount]',
    providers: [{provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => AmountDirective), multi: true}],
    host: {
        inputmode: 'numeric',
        '(input)': 'onInput()',
        '(focus)': 'moveCaretToEnd()',
        '(mousedown)': 'onMouseDown($event)',
        '(keydown)': 'onKeyDown($event)',
        '(blur)': 'onTouched()',
    },
})
export class AmountDirective implements ControlValueAccessor {

    private readonly input = inject<ElementRef<HTMLInputElement>>(ElementRef).nativeElement;
    private onChange: (value: number) => void = () => undefined;
    protected onTouched: () => void = () => undefined;

    writeValue(value: number | string | null): void {
        this.input.value = this.format(Number(value) || 0);
    }

    registerOnChange(fn: (value: number) => void): void {
        this.onChange = fn;
    }

    registerOnTouched(fn: () => void): void {
        this.onTouched = fn;
    }

    setDisabledState(disabled: boolean): void {
        this.input.disabled = disabled;
    }

    protected onInput(): void {
        const digits = this.input.value.replace(/\D/g, '').slice(0, MAX_DIGITS);
        const amount = Number(digits || 0) / 100;
        this.input.value = this.format(amount);
        this.moveCaretToEnd();
        this.onChange(amount);
    }

    protected onMouseDown(event: MouseEvent): void {
        if (this.input.readOnly) return;
        event.preventDefault();
        this.input.focus();
        this.moveCaretToEnd();
    }

    protected onKeyDown(event: KeyboardEvent): void {
        if (this.input.readOnly) return;
        if (['ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown', 'Home', 'End'].includes(event.key)) {
            event.preventDefault();
            this.moveCaretToEnd();
        }
    }

    protected moveCaretToEnd(): void {
        const end = this.input.value.length;
        this.input.setSelectionRange(end, end);
    }

    private format(amount: number): string {
        return amount.toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2});
    }
}
