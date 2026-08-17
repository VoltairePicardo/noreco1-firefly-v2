import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { forkJoin } from 'rxjs';
import { COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { LaddaModule } from 'angular2-ladda';
import { AllocationFactorService } from '../allocation-factor.service';

@Component({
    selector: 'app-allocation-factor-add-edit',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, LaddaModule],
    templateUrl: './allocation-factor-add-edit.component.html'
})
export class AllocationFactorAddEditComponent {
    module    = 'Allocation Factor';
    subModule = 'Create';
    menuLink  = 'allocation-factor';

    factorId: any = null;
    editMode      = false;
    submit        = false;
    formSubmit    = false;
    isLoading     = signal(false);

    code           = '';
    description    = '';
    factorData: any = null;

    selectedValidity: any = null;
    segmentRows: { segmentId: number; description: string; value: number }[] = [];
    dateRanges = signal<any[]>([]);

    private service      = inject(AllocationFactorService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.factorId  = params.get('id') ? Number(params.get('id')) : null;
            this.editMode  = this.factorId != null;
            this.subModule = this.editMode ? 'Edit' : 'Create';
            this.loadAll();
        });
    }

    loadAll(): void {
        this.isLoading.set(true);

        const calls: any = {
            segments:   this.service.getBusinessSegments(),
            dateRanges: this.service.getDateRanges()
        };
        if (this.editMode) {
            calls['factor'] = this.service.getById(this.factorId);
        }

        forkJoin(calls).subscribe({
            next: (results: any) => {
                this.isLoading.set(false);
                const segments: any[] = results.segments || [];
                this.dateRanges.set(results.dateRanges || []);
                this.segmentRows = segments.map(s => ({ segmentId: s.id, description: s.description, value: 0 }));

                if (this.editMode) {
                    const data = results.factor;
                    if (!data) { this.alertService.error(this.module, 'Not Found', ''); this.router.navigate(['/' + this.menuLink]); return; }
                    this.code        = data.code || '';
                    this.description = data.description || '';
                    this.factorData  = data;
                }
            },
            error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Error', ''); this.router.navigate(['/' + this.menuLink]); }
        });
    }

    get total(): number {
        return this.segmentRows.reduce((sum, r) => sum + (Number(r.value) || 0), 0);
    }

    validityEntries(): [string, any[]][] {
        if (!this.factorData?.factorPercentageDistroSetByValidity) return [];
        return Object.entries(this.factorData.factorPercentageDistroSetByValidity);
    }

    validSubmit(): void {
        this.submit = true;

        if (!this.code.trim())        { this.alertService.error(this.module, 'Validation', 'Code is required.'); return; }
        if (!this.description.trim()) { this.alertService.error(this.module, 'Validation', 'Description is required.'); return; }
        if (!this.selectedValidity)   { this.alertService.error(this.module, 'Validation', 'Validity date is required.'); return; }
        if (this.total !== 100)       { this.alertService.error(this.module, 'Validation', `Segment percentages must total 100%. Current: ${this.total}%`); return; }

        this.formSubmit = true;

        const payload = {
            id:          this.editMode ? this.factorId : null,
            code:        this.code.trim().toUpperCase(),
            description: this.description.trim(),
            validityDate: { id: this.selectedValidity.id },
            factorPercentageDistroSet: this.segmentRows.map(r => ({
                businessSegment: { id: r.segmentId },
                percentage:      r.value
            }))
        };

        this.service.create(payload).subscribe({
            next: (res: any) => {
                if (res?.success) {
                    this.alertService.success(this.module, 'Saved', '');
                    const id = res.modelId || this.factorId;
                    this.router.navigate(['/' + this.menuLink, id, 'detail']);
                } else {
                    this.formSubmit = false;
                    this.alertService.error(this.module, 'Saving', res?.failureMessage || '');
                }
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'Saving', ''); }
        });
    }
}
