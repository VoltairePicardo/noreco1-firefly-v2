import { ChangeDetectionStrategy, Component, inject, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {ModalService} from '@/app/shared/modals/modal-service';
import {WorkflowActionOption} from '@/app/models/shared/workflow.model';
import {WorkflowActionModalComponent} from '@/app/shared/modals/workflow-action-modal/workflow-action-modal.component';

@Component({
    selector: 'app-document-actions',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [FormsModule],
    template: `
        @if (actions().length > 0) {
            @for (currentStatus of [status()]; track currentStatus) {
                <select
                    class="form-select"
                    [ngModel]="selectedAction()"
                    (ngModelChange)="onActionSelect($event)"
                    [disabled]="processing()">
                    <option [ngValue]="null">{{ currentStatus || 'Select action' }}</option>
                    @for (action of actions(); track action.actionMapId) {
                        <option [ngValue]="action" [class.text-danger]="isDenyAction(action.action)">{{ action.action }}</option>
                    }
                </select>
            }
        }
    `,
})
export class DocumentActionsComponent {
    private modalService = inject(ModalService);

    actions = input<WorkflowActionOption[]>([]);
    processing = input(false);
    status = input<string | null | undefined>(null);

    process = output<{ actionMapId: number; remarks: string }>();

    selectedAction = signal<WorkflowActionOption | null>(null);

    isDenyAction(action: string): boolean {
        return /deny|disapprove|reject/i.test(action);
    }

    async onActionSelect(action: WorkflowActionOption | null): Promise<void> {
        this.selectedAction.set(null);
        if (!action) return;

        try {
            const result = await this.modalService.openModal(
                WorkflowActionModalComponent,
                { actionLabel: action.action, isDenyAction: this.isDenyAction(action.action) },
                { centered: true },
            );
            this.process.emit({ actionMapId: action.actionMapId, remarks: result.remarks });
        } catch {
            // Modal dismissed/cancelled — no action taken.
        }
    }
}
