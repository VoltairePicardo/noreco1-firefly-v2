import {Component, inject, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {FormsModule} from '@angular/forms';
import {MODAL_BUTTON_CANCEL, MODAL_BUTTON_CONTINUE} from '@/app/constants/app.constants';

@Component({
  selector: 'app-remarks-modal',
    imports: [
        FormsModule
    ],
  templateUrl: './remarks-modal.component.html',
  styleUrl: './remarks-modal.component.scss'
})
export class RemarksModalComponent implements OnInit {

    @Input() title: string = '';
    @Input() type: number = 0;

    public remarks: string = '';

    private activeModal = inject(NgbActiveModal);

    ngOnInit(): void {
        if(this.type == 2){
            this.title = "Send for Cancellation Approval"
        } else if(this.type == 3){
            this.title = "Approve Cancellation"
        } else if(this.type == 6){
            this.title = "Send for Approval"
        } else if(this.type == 7){
            this.title = "Approve"
        } else if(this.type == 8){
            this.title = "Return to Creator"
        } else if(this.type == 11){
            this.title = "Deny"
        }
    }

    continue(): void {
        this.activeModal.close({
            action: MODAL_BUTTON_CONTINUE,
            data: this.remarks
        });
    }

    cancel(): void {
        this.activeModal.dismiss(MODAL_BUTTON_CANCEL);
    }

}

