import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { IfrService } from '../ifr.service';

@Component({
    selector: 'app-ifr-add-edit',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './ifr-add-edit.component.html'
})
export class IfrAddEditComponent {
    module = 'Items For Repair'; subModule = 'Create'; menuLink = 'ifr';
    id: any = null; editMode = false; isLoading = signal(false);
    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };
    voucherDate = ''; remarks = '';

    private service = inject(IfrService);
    private route = inject(ActivatedRoute); private router = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.editMode = idParam != null && /^\d+$/.test(idParam);
            if (this.editMode) { this.id = Number(idParam); this.subModule = 'Edit'; this.loadForEdit(); }
            else { this.subModule = 'Create'; }
        });
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.voucherDate = data.date || data.voucherDate ? new Date(data.date || data.voucherDate).toISOString().substring(0, 10) : '';
                    this.remarks = data.remarks || data.description || '';
                } else { this.alertService.error(this.module, 'Not found.', ''); this.router.navigate(['/' + this.menuLink]); }
            },
            error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Error.', ''); this.router.navigate(['/' + this.menuLink]); }
        });
    }

    save(): void {
        if (!this.voucherDate) { this.alertService.warning(this.module, 'Validation', 'Date is required.'); return; }
        this.isLoading.set(true);
        const payload: any = { date: this.voucherDate, remarks: this.remarks || null };
        if (this.editMode) payload.id = this.id;
        const req$ = this.editMode ? this.service.update(payload) : this.service.create(payload);
        req$.subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.success === false) { this.alertService.error(this.module, 'Save', data.failureMessage || ''); }
                else { this.alertService.success(this.module, this.editMode ? 'Updated' : 'Created', ''); this.router.navigate(['/' + this.menuLink, data?.modelId ?? data?.id ?? this.id, 'detail']); }
            },
            error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Save', ''); }
        });
    }
}
