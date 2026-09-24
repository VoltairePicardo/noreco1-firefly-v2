import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import {MeterTestingResult} from '@/app/models/special-equipment-testing/meter-testing.model';
import {MeterTestingService} from '@/app/pages/special-equipment-testing/meter/meter-testing.service';

@Component({
    selector: 'app-meter-testing-upload',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './meter-testing-upload.component.html'
})
export class MeterTestingUploadComponent {
    module    = 'Meter Testing';
    subModule = 'Multiple Meter Testing';
    menuLink  = 'meter-testing';

    selectedFile: File | null = null;
    fileName = '';
    submit = false;
    isExtracting = signal(false);
    isSaving = signal(false);
    result = signal<MeterTestingResult | null>(null);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    meterModels = signal<any[]>([]);
    meterModelId: number | null = null;
    actualDateOfTesting = '';
    multiplier: number | null = null;
    temperature: string | null = null;
    relativeHumidity: string | null = null;

    private service      = inject(MeterTestingService);
    private alertService = inject(AlertService);
    private router       = inject(Router);

    ngOnInit(): void {
        this.service.listMeterModels().subscribe({ next: d => this.meterModels.set(d), error: () => {} });
    }

    onFileChange(event: Event): void {
        const input = event.target as HTMLInputElement;
        this.selectedFile = input.files?.[0] ?? null;
        this.fileName     = this.selectedFile?.name ?? '';
        this.result.set(null);
    }

    extract(): void {
        this.submit = true;
        if (!this.selectedFile) { return; }

        this.isExtracting.set(true);
        this.service.extract(this.selectedFile).subscribe({
            next: (data) => {
                this.isExtracting.set(false);
                this.result.set(data);
                this.temperature = data?.temperature ?? null;
                this.relativeHumidity    = data?.relativeHumidity    ?? null;
                if (!data?.records?.length) {
                    this.alertService.warning(this.module, 'No Records', 'No meter testing records were found in this file.');
                }
            },
            error: () => {
                this.isExtracting.set(false);
                this.alertService.error(this.module, 'Extract', 'Failed to extract data from the uploaded file.');
            }
        });
    }

    reset(): void {
        this.selectedFile = null;
        this.fileName     = '';
        this.submit       = false;
        this.meterModelId = null;
        this.actualDateOfTesting = '';
        this.multiplier = null;
        this.temperature = null;
        this.relativeHumidity = null;
        this.result.set(null);
    }

    save(): void {
        this.submit = true;

        if (!this.meterModelId) {
            this.alertService.warning(this.module, 'Meter Model', 'Please select a meter model.');
            return;
        }

        const data = this.result();
        if (!data?.records?.length) {
            this.alertService.warning(this.module, 'No Records', 'Please extract a file with meter testing records first.');
            return;
        }

        const payload = {
            date: this.actualDateOfTesting || null,
            meterModel: { id: this.meterModelId },
            temperature: this.temperature,
            relativeHumidity: this.relativeHumidity,
            testedBy: data.tester || null,
            multiplier: this.multiplier || null,
            details: data.records.map(rec => ({
                meterSerialNo: rec.meterSerialNo,
                error: rec.error,
                sta: !!rec.sta,
                crp: !!rec.crp,
                voltageTest: !!rec.voltageTest,
                result: !!rec.result,
            })),
        };

        this.isSaving.set(true);
        this.service.create(payload).subscribe({
            next: (res) => {
                this.isSaving.set(false);
                if (res.success) {
                    this.alertService.success(this.module, 'Saved', '');
                    this.router.navigate(['/' + this.menuLink]);
                } else {
                    this.alertService.error(this.module, 'Saving', res.failureMessage);
                }
            },
            error: () => {
                this.isSaving.set(false);
                this.alertService.error(this.module, 'Saving', '');
            }
        });
    }

}
