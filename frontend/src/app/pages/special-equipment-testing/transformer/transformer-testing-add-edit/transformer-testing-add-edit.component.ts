import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { SharedModalService } from '@/app/shared/modals/shared-modal-service/shared-modal.service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseTransformerModalComponent } from '@/app/shared/modals/browse-transformer-modal/browse-transformer-modal.component';
import { NG_ICON_DIRECTIVES } from '@ng-icons/core';
import { TransformerTestingService } from '../transformer-testing.service';
import { TransformerLossTestRow, TransformerTestingBrand, TransformerTestingLookup, TransformerTestingTransformer, TransformerVoltageRatioTestRow } from '@/app/models/special-equipment-testing/transformer-testing.model';
import {HelperService} from '@/app/helpers/util';

type UserFieldKey = 'testedBy' | 'recommendingApprovalUser' | 'approvedBy';

interface EntitySummary {
    accountNo: number | null;
    name: string | null;
}

interface EntityModalResult {
    action?: string;
    data?: EntitySummary;
}

interface TransformerModalResult {
    action?: string;
    data?: TransformerTestingTransformer;
}

interface TransformerTestingCreateResponse {
    success: boolean;
    modelId?: number;
    failureMessage?: string;
}

const emptyVoltageRatioRow = (): TransformerVoltageRatioTestRow => ({
    primaryVoltageInduce: null, tap1: null, tap2: null, tap3: null, tap4: null, tap5: null
});

const emptyLossTestRow = (): TransformerLossTestRow => ({
    shortCircuitPrimaryCurrent: null, shortCircuitResult: null,
    openCircuitSecondaryVoltage: null, openCircuitResult: null,
    totalLoss: null, iex: null, iz: null, ir: null, ix: null, eff: null
});

const currentTimeString = (): string => {
    const now = new Date();
    return `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
};

const roundImpedance = (value: number | null): number | null =>
    value != null ? Math.round(value * 10000) / 10000 : null;

@Component({
    selector: 'app-transformer-testing-add-edit',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective, NG_ICON_DIRECTIVES],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './transformer-testing-add-edit.component.html'
})
export class TransformerTestingAddEditComponent implements OnInit {
    module    = 'Transformer Testing';
    subModule = 'Create';
    menuLink  = 'transformer-testing';

    dateFlatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };
    timeFlatpickrOptions = { enableTime: true, noCalendar: true, dateFormat: 'H:i', altInput: true, altFormat: 'h:i K' };

    isLoading = signal(false);
    submit    = signal(false);

    brands = signal<TransformerTestingBrand[]>([]);
    primaryVoltages = signal<TransformerTestingLookup[]>([]);
    secondaryVoltages = signal<TransformerTestingLookup[]>([]);
    transformerConditions = signal<TransformerTestingLookup[]>([]);

    transformerId = signal<number | null>(null);

    serialNo         = signal('');
    transformerOwner = signal('');
    ownerAddress     = signal('');
    brandId          = signal<number | null>(null);
    kva              = signal<number | null>(null);
    primaryVoltageId   = signal<number | null>(null);
    secondaryVoltageId = signal<number | null>(null);
    impedance        = signal<number | null>(null);
    polarity         = signal<string | null>(null);
    coreType         = signal<string | null>(null);
    bushing          = signal<string | null>(null);
    type             = signal<string | null>(null);

    dateTested      = signal(new Date().toISOString().substring(0, 10));
    timeTested       = signal(currentTimeString());
    weather          = signal('');
    transformerConditionId = signal<number | null>(null);
    remarks          = signal('');
    recommendation   = signal('');

    testedByAccountNo = signal<number | null>(null);
    testedByName      = signal('');
    recommendingApprovalUserAccountNo = signal<number | null>(null);
    recommendingApprovalUserName      = signal('');
    approvedByAccountNo = signal<number | null>(null);
    approvedByName      = signal('');

    voltageRatioTests = signal<TransformerVoltageRatioTestRow[]>([emptyVoltageRatioRow()]);
    lossTests         = signal<TransformerLossTestRow[]>([emptyLossTestRow()]);

    private router               = inject(Router);
    private alertService         = inject(AlertService);
    private modalService         = inject(SharedModalService);
    private transformerTestingService = inject(TransformerTestingService);

    ngOnInit(): void {
        this.transformerTestingService.listBrands().subscribe(brands => this.brands.set(brands));
        this.transformerTestingService.listPrimaryVoltages().subscribe(voltages => this.primaryVoltages.set(voltages));
        this.transformerTestingService.listSecondaryVoltages().subscribe(voltages => this.secondaryVoltages.set(voltages));
        this.transformerTestingService.listTransformerConditions().subscribe(conditions => this.transformerConditions.set(conditions));
    }

    async openTransformerBrowse(): Promise<void> {
        try {
            const result: TransformerModalResult = await this.modalService.openModal(
                BrowseTransformerModalComponent,
                {},
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.selectTransformer(result.data);
            }
        } catch {
            // modal dismissed — no action needed
        }
    }

    selectTransformer(transformer: TransformerTestingTransformer): void {
        this.transformerId.set(transformer.id);
        this.serialNo.set(transformer.serialNo ?? '');
        this.transformerOwner.set(transformer.owner ?? '');
        this.ownerAddress.set(transformer.ownerAddress ?? '');
        this.brandId.set(transformer.brand?.id ?? null);
        this.kva.set(transformer.kva);
        this.primaryVoltageId.set(transformer.primaryVoltage?.id ?? null);
        this.secondaryVoltageId.set(transformer.secondaryVoltage?.id ?? null);
        this.impedance.set(HelperService.toPercentForm(transformer.impedance));
        this.polarity.set(transformer.polarity);
        this.coreType.set(transformer.coreType);
        this.bushing.set(transformer.bushing);
        this.type.set(transformer.type);
    }

    clearSelectedTransformer(): void {
        this.transformerId.set(null);
        this.serialNo.set('');
        this.transformerOwner.set('');
        this.ownerAddress.set('');
        this.brandId.set(null);
        this.kva.set(null);
        this.primaryVoltageId.set(null);
        this.secondaryVoltageId.set(null);
        this.impedance.set(null);
        this.polarity.set(null);
        this.coreType.set(null);
        this.bushing.set(null);
        this.type.set(null);
    }

    addVoltageRatioRow(): void {
        this.voltageRatioTests.update(rows => [...rows, emptyVoltageRatioRow()]);
    }

    removeVoltageRatioRow(index: number): void {
        this.voltageRatioTests.update(rows => rows.filter((_, i) => i !== index));
    }

    updateVoltageRatioField(index: number, field: keyof TransformerVoltageRatioTestRow, value: string): void {
        const parsed = value === '' ? null : Number(value);
        this.voltageRatioTests.update(rows => rows.map((row, i) => i === index ? { ...row, [field]: parsed } : row));
    }

    addLossTestRow(): void {
        this.lossTests.update(rows => [...rows, emptyLossTestRow()]);
    }

    removeLossTestRow(index: number): void {
        this.lossTests.update(rows => rows.filter((_, i) => i !== index));
    }

    updateLossTestField(index: number, field: keyof TransformerLossTestRow, value: string): void {
        const parsed = value === '' ? null : Number(value);
        this.lossTests.update(rows => rows.map((row, i) => {
            if (i !== index) { return row; }
            const updated = { ...row, [field]: parsed };
            if (field === 'shortCircuitResult' || field === 'openCircuitResult') {
                updated.totalLoss = updated.shortCircuitResult == null && updated.openCircuitResult == null
                    ? null
                    : (updated.shortCircuitResult ?? 0) + (updated.openCircuitResult ?? 0);
            }
            return updated;
        }));
    }

    async openUserBrowse(target: UserFieldKey): Promise<void> {
        try {
            const result: EntityModalResult = await this.modalService.openModal(
                BrowseEntityModalComponent,
                {},
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.setUserField(target, result.data.accountNo, result.data.name ?? '');
            }
        } catch {
            // modal dismissed — no action needed
        }
    }

    private setUserField(target: UserFieldKey, accountNo: number | null, name: string): void {
        switch (target) {
            case 'testedBy':
                this.testedByAccountNo.set(accountNo);
                this.testedByName.set(name);
                break;
            case 'recommendingApprovalUser':
                this.recommendingApprovalUserAccountNo.set(accountNo);
                this.recommendingApprovalUserName.set(name);
                break;
            case 'approvedBy':
                this.approvedByAccountNo.set(accountNo);
                this.approvedByName.set(name);
                break;
        }
    }

    save(): void {
        this.submit.set(true);

        if (!this.serialNo().trim()) { return; }

        const impedanceValue = roundImpedance(this.impedance());

        const transformer = this.transformerId()
            ? { id: this.transformerId() }
            : {
                serialNo: this.serialNo().trim(),
                owner: this.transformerOwner() || null,
                ownerAddress: this.ownerAddress() || null,
                brand: this.brandId() ? { id: this.brandId() } : null,
                kva: this.kva(),
                primaryVoltage: this.primaryVoltageId() ? { id: this.primaryVoltageId() } : null,
                secondaryVoltage: this.secondaryVoltageId() ? { id: this.secondaryVoltageId() } : null,
                impedance: impedanceValue,
                polarity: this.polarity(),
                coreType: this.coreType(),
                bushing: this.bushing(),
                type: this.type()
            };

        const payload = {
            dateTested: this.dateTested() || null,
            timeTested: this.timeTested() || null,
            transformer,
            owner: this.transformerOwner() || null,
            ownerAddress: this.ownerAddress() || null,
            transformerCondition: this.transformerConditionId() ? { id: this.transformerConditionId() } : null,
            weather: this.weather() || null,
            remarks: this.remarks() || null,
            recommendation: this.recommendation() || null,
            testedBy: this.testedByAccountNo() ? { accountNo: this.testedByAccountNo() } : null,
            recommendingApprovalUser: this.recommendingApprovalUserAccountNo() ? { accountNo: this.recommendingApprovalUserAccountNo() } : null,
            approvedBy: this.approvedByAccountNo() ? { accountNo: this.approvedByAccountNo() } : null,
            voltageRatioTests: this.voltageRatioTests(),
            lossTests: this.lossTests()
        };

        this.isLoading.set(true);
        this.transformerTestingService.create(payload).subscribe({
            next: (res: TransformerTestingCreateResponse) => {
                this.isLoading.set(false);
                if (res.success) {
                    this.alertService.success(this.module, 'Saved', '');
                    this.router.navigate(['/' + this.menuLink, res.modelId, 'detail']);
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
