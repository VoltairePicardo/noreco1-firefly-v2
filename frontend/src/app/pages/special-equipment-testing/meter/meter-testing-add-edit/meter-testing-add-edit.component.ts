import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
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
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { MeterTestingService } from '../meter-testing.service';
import { MeterTestingOption, MeterTestingOptionGroup } from '@/app/models/special-equipment-testing/meter-testing.model';
import {NG_ICON_DIRECTIVES} from "@ng-icons/core";

interface ConsumerMeterModalResult {
    action?: string;
    data?: ConsumerMeterSelection;
}

interface EntitySummary {
    accountNo: number | null;
    name: string | null;
}

interface EntityModalResult {
    action?: string;
    data?: EntitySummary;
}

interface MeterTestingCreateResponse {
    success: boolean;
    failureMessage?: string;
}

const OTHERS_DESCRIPTION = 'others';

@Component({
    selector: 'app-meter-testing-add-edit',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective, NG_ICON_DIRECTIVES],
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
    meterId = signal<number | null>(null);
    meterModelId = signal<number | null>(null);
    modelName = signal('');
    accuracyClassDescription = signal('');
    presentReading = signal<number | null>(null);
    meterCalibratorAccountNo = signal<number | null>(null);
    meterCalibratorName = signal('');
    actualDateOfTesting = signal(new Date().toISOString().substring(0, 10));
    error = signal<number | null>(null);
    sta = signal<boolean | null>(true);
    crp = signal<boolean | null>(true);
    voltageTest = signal<boolean | null>(true);
    result = signal<boolean | null>(true);

    optionGroups = signal<MeterTestingOptionGroup[]>([]);
    selectedOptionIds = signal<Set<number>>(new Set());
    otherTexts = signal<Partial<Record<number, string>>>({});

    private route            = inject(ActivatedRoute);
    private router           = inject(Router);
    private alertService     = inject(AlertService);
    private modalService     = inject(SharedModalService);
    private meterTestingService = inject(MeterTestingService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.id.set(idParam != null && /^\d+$/.test(idParam) ? Number(idParam) : null);
        });

        this.meterTestingService.listOptions().subscribe(options => {
            this.optionGroups.set(this.groupOptions(options));
        });
    }

    isOthersOption(option: MeterTestingOption): boolean {
        return (option.description ?? '').trim().toLowerCase() === OTHERS_DESCRIPTION;
    }

    isOptionSelected(id: number): boolean {
        return this.selectedOptionIds().has(id);
    }

    toggleOption(id: number): void {
        const next = new Set(this.selectedOptionIds());
        if (next.has(id)) { next.delete(id); } else { next.add(id); }
        this.selectedOptionIds.set(next);
    }

    setOtherText(id: number, value: string): void {
        this.otherTexts.update(texts => ({ ...texts, [id]: value }));
    }

    private groupOptions(options: MeterTestingOption[]): MeterTestingOptionGroup[] {
        const typeIds: number[] = [];
        const optionsByTypeId = new Map<number, MeterTestingOption[]>();
        const typeDescriptionById = new Map<number, string>();

        for (const option of options) {
            const typeId = option.optionType?.id ?? 0;
            if (!optionsByTypeId.has(typeId)) {
                typeIds.push(typeId);
                optionsByTypeId.set(typeId, []);
                typeDescriptionById.set(typeId, option.optionType?.description ?? '');
            }
            optionsByTypeId.get(typeId)!.push(option);
        }

        const ROWS_PER_COLUMN = 3;

        return typeIds.map(typeId => {
            const typeOptions = optionsByTypeId.get(typeId)!;
            const regularOptions = typeOptions.filter(o => !this.isOthersOption(o));
            const othersOption = typeOptions.find(o => this.isOthersOption(o)) ?? null;

            const columns: MeterTestingOption[][] = [];
            regularOptions.forEach((option, index) => {
                const columnIndex = Math.floor(index / ROWS_PER_COLUMN);
                if (!columns[columnIndex]) { columns[columnIndex] = []; }
                columns[columnIndex].push(option);
            });

            return { typeId, typeDescription: typeDescriptionById.get(typeId) ?? '', columns, othersOption };
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
                this.meterId.set(result.data.meterId);
                this.meterModelId.set(result.data.meterModelId);
                this.presentReading.set(result.data.presentReading);
                this.modelName.set(result.data.modelName ?? '');
                this.accuracyClassDescription.set(result.data.accuracyClassDescription ?? '');
            }
        } catch {
            // modal dismissed — no action needed
        }
    }

    async openMeterCalibratorBrowse(): Promise<void> {
        try {
            const result: EntityModalResult = await this.modalService.openModal(
                BrowseEntityModalComponent,
                {},
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.meterCalibratorAccountNo.set(result.data.accountNo);
                this.meterCalibratorName.set(result.data.name ?? '');
            }
        } catch {
            // modal dismissed — no action needed
        }
    }

    save(): void {
        this.submit.set(true);

        if (!this.meterSerialNo().trim()) { return; }

        if (!this.meterModelId()) {
            this.alertService.warning(this.module, 'Meter Model', 'The selected meter has no meter model on record — please browse and select again.');
            return;
        }

        const payload = {
            date: this.actualDateOfTesting() || null,
            meterModel: { id: this.meterModelId() },
            meter: this.meterId() ? { id: this.meterId() } : null,
            accountNo: this.accountNo(),
            presentReading: this.presentReading(),
            meterCalibrator: this.meterCalibratorAccountNo() ? { accountNo: this.meterCalibratorAccountNo() } : null,
            details: [{
                meterSerialNo: this.meterSerialNo(),
                error: this.error(),
                sta: !!this.sta(),
                crp: !!this.crp(),
                voltageTest: !!this.voltageTest(),
                result: !!this.result()
            }],
            optionDetails: Array.from(this.selectedOptionIds()).map(id => ({
                meterTestingOption: { id: id },
                otherRemarks: this.otherTexts()[id] ?? null
            }))
        };

        this.isLoading.set(true);
        this.meterTestingService.createIndividual(payload).subscribe({
            next: (res: MeterTestingCreateResponse) => {
                this.isLoading.set(false);
                if (res.success) {
                    this.alertService.success(this.module, 'Saved', '');
                    this.router.navigate(['/' + this.menuLink]);
                } else {
                    this.alertService.error(this.module, 'Saving', res.failureMessage ?? '');
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Saving', '');
            }
        });
    }
}
