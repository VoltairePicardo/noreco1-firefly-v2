import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { forkJoin } from 'rxjs';
import { COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { LaddaModule } from 'angular2-ladda';
import { AllocationFactorService } from '../allocation-factor.service';

@Component({
    selector: 'app-allocation-factor-edit-validity',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, LaddaModule],
    templateUrl: './allocation-factor-edit-validity.component.html'
})
export class AllocationFactorEditValidityComponent {
    module    = 'Allocation Factor';
    subModule = 'Edit Validity';
    menuLink  = 'allocation-factor';

    factorId:   any = null;
    validityId: any = null;
    factor: any     = null;
    isLoading       = signal(false);
    submit          = false;
    formSubmit      = false;

    segmentRows: { segmentId: number; description: string; value: number }[] = [];

    private service      = inject(AllocationFactorService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.factorId   = params.get('factorId') ? Number(params.get('factorId')) : null;
            this.validityId = params.get('validityId') ? Number(params.get('validityId')) : null;
            if (this.factorId && this.validityId) { this.loadAll(); }
        });
    }

    loadAll(): void {
        this.isLoading.set(true);
        forkJoin({
            factor:   this.service.getByIdAndValidity(this.factorId, this.validityId),
            segments: this.service.getBusinessSegments()
        }).subscribe({
            next: (results: any) => {
                this.isLoading.set(false);
                const factor = results.factor;
                if (!factor) { this.alertService.error(this.module, 'Not Found', ''); this.router.navigate(['/' + this.menuLink]); return; }
                this.factor = factor;

                const segments: any[] = results.segments || [];
                const distros: any[]  = factor.factorPercentageDistroSet || [];

                this.segmentRows = segments.map(s => {
                    const existing = distros.find((d: any) => d.businessSegment?.id === s.id);
                    return {
                        segmentId:   s.id,
                        description: s.description,
                        value:       existing ? Number((existing.percentage * 100).toFixed(2)) : 0
                    };
                });
            },
            error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Error', ''); this.router.navigate(['/' + this.menuLink]); }
        });
    }

    get total(): number {
        return this.segmentRows.reduce((sum, r) => sum + (Number(r.value) || 0), 0);
    }

    validSubmit(): void {
        this.submit = true;
        if (this.total !== 100) { this.alertService.error(this.module, 'Validation', `Total must be 100%. Current: ${this.total}%`); return; }

        this.formSubmit = true;

        const payload = {
            id:          this.factorId,
            validityDate: { id: this.validityId },
            factorPercentageDistroSet: this.segmentRows.map(r => ({
                businessSegment: { id: r.segmentId },
                percentage:      r.value
            }))
        };

        this.service.updateByValidity(payload).subscribe({
            next: (res: any) => {
                if (res?.success) {
                    this.alertService.success(this.module, 'Updated', '');
                    this.router.navigate(['/' + this.menuLink, this.factorId, 'detail']);
                } else {
                    this.formSubmit = false;
                    this.alertService.error(this.module, 'Update', res?.failureMessage || '');
                }
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'Update', ''); }
        });
    }
}
