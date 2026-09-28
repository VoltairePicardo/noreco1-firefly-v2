import { ChangeDetectionStrategy, Component, inject, Input } from '@angular/core';
import { LowerCasePipe } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'app-workflow-action-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [LowerCasePipe, ReactiveFormsModule],
    templateUrl: './workflow-action-modal.component.html',
})
export class WorkflowActionModalComponent {
    activeModal = inject(NgbActiveModal);

    @Input() actionLabel = '';
    @Input() isDenyAction = false;

    remarks = new FormControl('', { nonNullable: true });

    confirm(): void {
        this.activeModal.close({ remarks: this.remarks.value.trim() });
    }
}
