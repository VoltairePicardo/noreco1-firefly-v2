import {Directive, ElementRef, inject} from '@angular/core';

@Directive({
    selector: 'input[appSelectOnFocus]',
    host: {
        '(focus)': 'selectAll()'
    }
})
export class SelectOnFocusDirective {
    private readonly elementRef = inject(ElementRef<HTMLInputElement>);

    selectAll(): void {
        this.elementRef.nativeElement.select();
    }
}