import { Component, EventEmitter, Input, Output } from '@angular/core';
import {LaddaModule} from 'angular2-ladda';
import {TagifyModule} from 'ngx-tagify';
import {NgbNavModule} from '@ng-bootstrap/ng-bootstrap';
import {provideFlatpickrDefaults} from 'angularx-flatpickr';
import {COMMON_ALL_PAGE_IMPORTS} from '@/app/shared/providers/shared-providers';

export type MessageType = 'success' | 'info' | 'error' | 'warning';

@Component({
  selector: 'app-message-prompt',
    imports: [LaddaModule, TagifyModule, NgbNavModule, COMMON_ALL_PAGE_IMPORTS],
    providers: [provideFlatpickrDefaults()],
    templateUrl: './message-prompt.component.html',
  styleUrl: './message-prompt.component.scss'
})
export class MessagePromptComponent {

    @Input() message: string = '';
    @Input() type: MessageType = 'info';
    @Input() dismissible: boolean = true;
    @Input() autoDismiss: boolean = false;
    @Input() dismissTimeout: number = 5000;

    @Output() dismissed = new EventEmitter<void>();

    ngOnInit(): void {
        if (this.autoDismiss) {
            setTimeout(() => this.dismiss(), this.dismissTimeout);
        }
    }

    dismiss(): void {
        this.dismissed.emit();
    }

    get icon(): string {
        switch (this.type) {
            case 'success': return 'tablerCircleCheck';
            case 'error': return 'tablerAlertCircle';
            case 'warning': return 'tablerAlertTriangle';
            default: return 'tablerInfoCircle';
        }
    }

    get title(): string {
        switch (this.type) {
            case 'success': return 'Success';
            case 'error': return 'Something went wrong';
            case 'warning': return 'Warning';
            default: return 'Information';
        }
    }

    get buttonClass(): string {
        return this.type === 'error'
            ? 'btn-outline-danger'
            : `btn-outline-${this.type}`;
    }

}
