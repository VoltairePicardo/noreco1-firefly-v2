import { ChangeDetectionStrategy, Component, ElementRef, inject, OnDestroy, signal, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, debounceTime, takeUntil } from 'rxjs';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { InitialReadingEntryService } from '../initial-reading-entry.service';

@Component({
    selector: 'app-initial-reading-entry-add-edit',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, FlatpickrDirective],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './initial-reading-entry-add-edit.component.html'
})
export class InitialReadingEntryAddEditComponent implements OnDestroy {
    module    = 'Initial Reading Entry';
    menuLink  = 'initial-reading-entry';

    id          = signal<number | null>(null);
    editMode    = signal(false);
    subModule   = signal('Create');

    serialNo        = signal('');
    meter           = signal<any>(null);
    searchResults   = signal<any[]>([]);
    presentReading  = signal<number | null>(null);
    readingDate     = signal<string>(new Date().toISOString().split('T')[0]);
    isLookingUp     = signal(false);
    isSaving        = signal(false);
    isLoadingEdit   = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    readonly serialNo$ = new Subject<string>();
    private readonly destroy$  = new Subject<void>();

    @ViewChild('serialNoInput') serialNoInput!: ElementRef<HTMLInputElement>;

    private service      = inject(InitialReadingEntryService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.serialNo$.pipe(
            debounceTime(300),
            takeUntil(this.destroy$)
        ).subscribe(value => {
            if (value.trim() && !this.meter() && !this.isLookingUp()) {
                this.search();
            }
        });

        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            const isEdit  = idParam != null && /^\d+$/.test(idParam);
            this.id.set(isEdit ? Number(idParam) : null);
            this.editMode.set(isEdit);
            this.subModule.set(isEdit ? 'Edit' : 'Create');
            if (isEdit) { this.loadForEdit(Number(idParam)); }
        });
    }

    loadForEdit(id: number): void {
        this.isLoadingEdit.set(true);
        this.service.getById(id).subscribe({
            next: (data) => {
                this.isLoadingEdit.set(false);
                if (data?.id) {
                    this.meter.set(data);
                    this.serialNo.set(data.serialNo ?? '');
                    this.presentReading.set(data.presentReading ?? null);
                    this.readingDate.set(data.readingDate ? data.readingDate.split('T')[0] : new Date().toISOString().split('T')[0]);
                } else {
                    this.alertService.error(this.module, 'Not Found', 'Meter record not found.');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoadingEdit.set(false);
                this.alertService.error(this.module, 'Error', 'Failed to load meter.');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }

    onSerialNoKeydown(event: KeyboardEvent): void {
        if (event.key === 'Enter') { event.preventDefault(); this.search(); }
    }

    search(): void {
        const sn = this.serialNo().trim();
        if (!sn) return;
        this.isLookingUp.set(true);
        this.meter.set(null);
        this.searchResults.set([]);
        this.service.list(sn, 0, 20).subscribe({
            next: (data) => {
                this.isLookingUp.set(false);
                const results = data.content ?? [];
                if (results.length === 0) {
                    this.alertService.error(this.module, 'Not Found', `No meter found matching "${sn}".`);
                } else if (results.length === 1) {
                    this.selectMeter(results[0].serialNo);
                } else {
                    this.searchResults.set(results);
                }
            },
            error: (err) => {
                this.isLookingUp.set(false);
                this.alertService.error(this.module, 'Search Failed', err.error?.message || 'Search failed.');
            }
        });
    }

    selectMeter(serialNo: string): void {
        this.isLookingUp.set(true);
        this.searchResults.set([]);
        this.service.getMeterBySerialNo(serialNo).subscribe({
            next: (data) => { this.meter.set(data); this.isLookingUp.set(false); },
            error: (err) => {
                this.isLookingUp.set(false);
                this.alertService.error(this.module, 'Cannot Select', err.error?.message || 'Meter cannot accept an initial reading.');
            }
        });
    }

    clearLookup(): void {
        this.serialNo.set('');
        this.meter.set(null);
        this.searchResults.set([]);
        this.presentReading.set(null);
        this.readingDate.set(new Date().toISOString().split('T')[0]);
        setTimeout(() => this.serialNoInput?.nativeElement?.focus(), 50);
    }

    save(): void {
        if (!this.meter()) { this.alertService.warning(this.module, 'Required', 'Please look up a meter first.'); return; }
        if (this.presentReading() === null || this.presentReading() === undefined) {
            this.alertService.warning(this.module, 'Required', 'Please enter the present reading.');
            return;
        }

        this.isSaving.set(true);
        const payload = {
            serialNo: this.meter().serialNo,
            presentReading: this.presentReading(),
            readingDate: this.readingDate()
        };

        this.service.saveReading(payload).subscribe({
            next: (res) => {
                this.isSaving.set(false);
                if (res.success) {
                    this.alertService.savedWithActions(
                        res.successMessage || 'Initial reading successfully saved.',
                        () => this.router.navigate(['/' + this.menuLink, res.modelId, 'detail']),
                        () => this.clearLookup()
                    );
                } else {
                    this.alertService.error(this.module, 'Error', res.failureMessage || 'Failed to save.');
                }
            },
            error: (err) => {
                this.isSaving.set(false);
                this.alertService.error(this.module, 'Error', err.error?.message || 'Something went wrong.');
            }
        });
    }
}
