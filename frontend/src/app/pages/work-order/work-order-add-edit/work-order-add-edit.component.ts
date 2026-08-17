import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { WorkOrderService } from '../work-order.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseProjectModalComponent } from '@/app/shared/modals/browse-project-modal/browse-project-modal.component';
import { TownService } from '@/app/pages/town/town.service';

@Component({
    selector: 'app-work-order-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective
    ],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './work-order-add-edit.component.html'
})
export class WorkOrderAddEditComponent {
    module    = 'Work Order';
    subModule = 'Create';
    menuLink  = 'work-order';

    id: any    = null;
    editMode   = false;
    isLoading  = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // Form fields
    date        = '';
    type        = '';
    description = '';
    location    = '';
    startDate   = '';
    endDate     = '';

    // Lookups
    project: any      = null;
    selectedTown: any = null;
    towns: any[]      = [];

    readonly workOrderTypes = ['MATERIALS', 'LABOR', 'BOTH'];

    private service      = inject(WorkOrderService);
    private townService  = inject(TownService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);
    private modalService = inject(ModalService);

    ngOnInit(): void {
        this.loadTowns();
        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.editMode = idParam != null && /^\d+$/.test(idParam);
            if (this.editMode) {
                this.id        = Number(idParam);
                this.subModule = 'Edit';
                this.loadForEdit();
            } else {
                this.subModule = 'Create';
            }
        });
    }

    loadTowns(): void {
        this.townService.list().subscribe({
            next: (data) => { this.towns = data || []; },
            error: () => {}
        });
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.date         = data.date               ? new Date(data.date).toISOString().substring(0, 10) : '';
                    this.startDate    = data.periodCoveredFrom  ? new Date(data.periodCoveredFrom).toISOString().substring(0, 10) : '';
                    this.endDate      = data.periodCoveredTo    ? new Date(data.periodCoveredTo).toISOString().substring(0, 10) : '';
                    this.type         = data.type               || '';
                    this.description  = data.description        || '';
                    this.location     = data.location           || '';
                    this.project      = data.project            || null;
                    this.selectedTown = data.town               || null;
                } else {
                    this.alertService.error(this.module, 'Record not found.', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load record.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    async browseProject(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseProjectModalComponent,
                {},
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select') {
                this.project = result.data;
            }
        } catch (_) {}
    }

    save(): void {
        if (!this.date) {
            this.alertService.warning(this.module, 'Validation', 'Date is required.');
            return;
        }
        if (!this.selectedTown) {
            this.alertService.warning(this.module, 'Validation', 'Town is required.');
            return;
        }
        if (!this.type) {
            this.alertService.warning(this.module, 'Validation', 'Work Order Type is required.');
            return;
        }

        this.isLoading.set(true);

        const payload: any = {
            date:               this.date,
            type:               this.type,
            description:        this.description        || null,
            location:           this.location           || null,
            periodCoveredFrom:  this.startDate          || null,
            periodCoveredTo:    this.endDate            || null,
            project:            this.project            ? { id: this.project.id }      : null,
            town:               this.selectedTown       ? { id: this.selectedTown.id } : null
        };

        if (this.editMode) payload.id = this.id;

        const request$ = this.editMode
            ? this.service.update(payload)
            : this.service.create(payload);

        request$.subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.success === false) {
                    this.alertService.error(this.module, 'Save', data.failureMessage || '');
                } else {
                    this.alertService.success(this.module, this.editMode ? 'Updated' : 'Created', '');
                    const id = data?.modelId ?? data?.id ?? this.id;
                    this.router.navigate(['/' + this.menuLink, id, 'detail']);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Save', '');
            }
        });
    }
}
