import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { MonthlyClosingService } from '../monthly-closing.service';
import { provideIcons } from '@ng-icons/core';
import { tablerArrowLeft, tablerCheck } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-monthly-closing-add-edit',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerArrowLeft, tablerCheck })],
    templateUrl: './monthly-closing-add-edit.component.html'
})
export class MonthlyClosingAddEditComponent {
    module    = 'Monthly Closing';
    subModule = 'Create';
    menuLink  = 'monthly-closing';

    id: any    = null;
    editMode   = false;
    formSubmit = false;
    isLoading  = signal(false);

    year:   number | null = null;
    month:  number | null = null;
    status: string = 'OPEN';

    months = [
        { value: 1,  label: 'January'   },
        { value: 2,  label: 'February'  },
        { value: 3,  label: 'March'     },
        { value: 4,  label: 'April'     },
        { value: 5,  label: 'May'       },
        { value: 6,  label: 'June'      },
        { value: 7,  label: 'July'      },
        { value: 8,  label: 'August'    },
        { value: 9,  label: 'September' },
        { value: 10, label: 'October'   },
        { value: 11, label: 'November'  },
        { value: 12, label: 'December'  }
    ];

    private service      = inject(MonthlyClosingService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        const now = new Date();
        this.year  = now.getFullYear();
        this.month = now.getMonth() + 1;

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

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.year   = data.year   || null;
                    this.month  = data.month  || null;
                    this.status = data.status || 'OPEN';
                } else {
                    this.alertService.error(this.module, 'Record not found.', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Load error.', ''); this.router.navigate(['/' + this.menuLink]); }
        });
    }

    save(): void {
        if (!this.year || !this.month) {
            this.alertService.warning(this.module, 'Validation', 'Please select a year and month.');
            return;
        }

        this.formSubmit = true;
        const payload = { id: this.editMode ? this.id : null, year: this.year, month: this.month, status: this.status };
        const req$ = this.editMode ? this.service.update(payload) : this.service.create(payload);

        req$.subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Saved successfully.', '');
                    this.router.navigate(['/' + this.menuLink, res.modelId || this.id, 'detail']);
                } else {
                    this.alertService.error(this.module, 'Save failed.', res?.failureMessage || '');
                }
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'An error occurred.', ''); }
        });
    }
}
