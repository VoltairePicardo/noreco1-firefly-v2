import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { TransformerTestingService } from '../transformer-testing.service';
import { TransformerTestingData } from '@/app/models/special-equipment-testing/transformer-testing.model';
import { HelperService } from '@/app/helpers/util';
import { WorkflowService } from '@/app/shared/workflow/workflow.service';
import { DocumentActionsComponent } from '@/app/shared/workflow/document-actions/document-actions.component';
import { documentStatusBadgeClass } from '@/app/shared/workflow/document-status-badge.util';
import { buildProcessPayload, WorkflowActionOption } from '@/app/models/shared/workflow.model';
import { provideIcons } from '@ng-icons/core';
import { tablerPrinter } from '@ng-icons/tabler-icons';
import { NgbCollapse } from '@ng-bootstrap/ng-bootstrap';
import { DocumentLogsComponent } from '@/app/shared/components/document-logs/document-logs.component';

@Component({
    selector: 'app-transformer-testing-detail',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, DocumentActionsComponent, NgbCollapse, DocumentLogsComponent],
    templateUrl: './transformer-testing-detail.component.html',
    providers: [provideIcons({ tablerPrinter })]
})
export class TransformerTestingDetailComponent implements OnInit {
    module    = 'Transformer Testing';
    subModule = 'Details';
    menuLink  = 'transformer-testing';

    data = signal<TransformerTestingData | null>(null);
    isLoading = signal(false);
    actions = signal<WorkflowActionOption[]>([]);
    processing = signal(false);
    historyCollapsed = signal(true);

    readonly statusBadgeClass = documentStatusBadgeClass;

    private route          = inject(ActivatedRoute);
    private router         = inject(Router);
    private service        = inject(TransformerTestingService);
    private alertService   = inject(AlertService);
    private workflowService = inject(WorkflowService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            const id = idParam != null && /^\d+$/.test(idParam) ? Number(idParam) : null;

            if (id !== null) {
                this.load(id);
            }
        });
    }

    onProcess(event: { actionMapId: number; remarks: string }): void {
        const data = this.data();
        if (!data?.id) { return; }

        this.processing.set(true);
        this.service.process(buildProcessPayload(data.id, event.actionMapId, event.remarks)).subscribe({
            next: (response) => {
                this.processing.set(false);
                if (response.success) {
                    this.alertService.success(this.module, 'Processed', response.successMessage || '');
                    this.load(data.id);
                } else {
                    this.alertService.error(this.module, 'Process', response.failureMessage || response.messages?.[0] || '');
                }
            },
            error: (err) => {
                this.processing.set(false);
                this.alertService.httpError(this.module, 'Process', err);
            }
        });
    }

    print(): void {
        const id = this.data()?.id;
        if (id != null) { this.service.print(id); }
    }

    toPercentForm(value: number | null | undefined): number {
        return HelperService.toPercentForm(value);
    }

    toTimeDate(time: string | null | undefined): Date | null {
        if (!time) { return null; }
        const [hours, minutes] = time.split(':').map(Number);
        const date = new Date(0);
        date.setHours(hours, minutes, 0, 0);
        return date;
    }

    private load(id: number): void {
        this.isLoading.set(true);
        this.service.getData(id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.data.set(data);
                    if (data.transaction?.id) {
                        this.workflowService.getAvailableActionsForTransaction(data.transaction.id).subscribe({
                            next: (actions) => this.actions.set(actions)
                        });
                    }
                } else {
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Error', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }
}
