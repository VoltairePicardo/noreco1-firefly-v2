import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { SharedModalService } from '@/app/shared/modals/shared-modal-service/shared-modal.service';
import { BrowseConsumerModalComponent } from '@/app/shared/modals/browse-consumer-modal/browse-consumer-modal.component';
import { ConsumerMeterSelection } from '@/app/shared/modals/browse-consumer-modal/browse-consumer-modal.model';

interface ConsumerMeterModalResult {
    action?: string;
    data?: ConsumerMeterSelection;
}

@Component({
    selector: 'app-meter-testing-add-edit',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './meter-testing-add-edit.component.html'
})
export class MeterTestingAddEditComponent implements OnInit {
    module    = 'Meter Testing';
    menuLink  = 'meter-testing';

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    id = signal<number | null>(null);
    editMode = computed(() => this.id() !== null);
    subModule = computed(() => this.editMode() ? 'Edit' : 'Create');

    isLoading = signal(false);
    submit = signal(false);

    accountNo = signal<number | null>(null);
    accountName = signal('');
    oldAccountNo = signal('');
    address = signal('');
    meterSerialNo = signal('');
    presentReading = signal<number | null>(null);
    sealNo = signal('');
    actualDateOfTesting = signal('');
    error = signal<number | null>(null);
    sta = signal<boolean | null>(null);
    crp = signal<boolean | null>(null);
    voltageTest = signal<boolean | null>(null);
    result = signal<boolean | null>(null);

    private route        = inject(ActivatedRoute);
    private alertService = inject(AlertService);
    private modalService = inject(SharedModalService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.id.set(idParam != null && /^\d+$/.test(idParam) ? Number(idParam) : null);
        });
    }

    async openConsumerBrowse(): Promise<void> {
        try {
            const result: ConsumerMeterModalResult = await this.modalService.openModal(
                BrowseConsumerModalComponent,
                {},
                { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.accountNo.set(result.data.accountNo);
                this.accountName.set(result.data.accountName ?? '');
                this.oldAccountNo.set(result.data.oldAccountNo ?? '');
                this.address.set(result.data.address ?? '');
                this.meterSerialNo.set(result.data.meterSerialNo ?? '');
                this.presentReading.set(result.data.presentReading);
            }
        } catch {
            // modal dismissed — no action needed
        }
    }

    save(): void {
        this.submit.set(true);
        if (!this.meterSerialNo().trim()) { return; }

        this.alertService.warning(this.module, 'Not Yet Available', 'Saving a single meter testing record is not yet implemented.');
    }
}
