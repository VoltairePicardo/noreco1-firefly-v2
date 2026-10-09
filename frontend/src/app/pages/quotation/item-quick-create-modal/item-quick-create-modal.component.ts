import { Component, ElementRef, inject, Input, OnInit, signal, ViewChild } from '@angular/core';
import { FormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import Swal from 'sweetalert2';
import { COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { ItemService } from '@/app/pages/item/item.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseItemModalComponent } from '@/app/shared/modals/browse-item-modal/browse-item-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerCheck, tablerArrowLeft, tablerSearch, tablerX } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-item-quick-create-modal',
    standalone: true,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS],
    providers: [provideIcons({ tablerCheck, tablerArrowLeft, tablerSearch, tablerX })],
    templateUrl: './item-quick-create-modal.component.html',
})
export class ItemQuickCreateModalComponent implements OnInit {
    @Input() parentItemId?: number;
    @Input() parentItemDescription?: string;

    @ViewChild('descInput') private descInput?: ElementRef<HTMLInputElement>;

    units     = signal<any[]>([]);
    brands    = signal<any[]>([]);
    isLoading = signal(false);
    duplicates         = signal<any[]>([]);
    descriptionDisplay = signal('');

    validationForm!: UntypedFormGroup;
    submit = false;
    selectedParentItem: any = null;

    private dupSearch$ = new Subject<string>();

    activeModal          = inject(NgbActiveModal);
    private service      = inject(ItemService);
    private modalService = inject(ModalService);
    private fb           = inject(FormBuilder);

    ngOnInit(): void {
        if (this.parentItemId) {
            this.selectedParentItem = { id: this.parentItemId, description: this.parentItemDescription };
        }

        this.service.listUnits().subscribe({ next: d => this.units.set(d ?? []), error: () => {} });
        this.service.getBrands().subscribe({ next: d => { this.brands.set(d); this.refreshDescription(); }, error: () => {} });

        this.dupSearch$.pipe(
            debounceTime(400),
            distinctUntilChanged(),
            switchMap(q => q.trim().length > 1 ? this.service.list(q, null, 0, 5) : [{ content: [] }])
        ).subscribe({ next: res => this.duplicates.set(res.content ?? []), error: () => {} });

        this.initForm();
    }

    private initForm(): void {
        this.validationForm = this.fb.group({
            code:          [''],
            description:   ['', Validators.required],
            unitId:        [null, Validators.required],
            genericName:   [''],
            size:          [''],
            rating:        [''],
            specification: [''],
            brandId:       [null],
            manufacturer:  [''],
            partNumber:    [''],
            remarks:       [''],
        });

        ['genericName', 'size', 'rating', 'specification', 'brandId'].forEach(name =>
            this.validationForm.get(name)?.valueChanges.subscribe(val =>
                this.refreshDescription({ [name]: val })));
    }

    get form(): UntypedFormGroup { return this.validationForm; }

    private refreshDescription(override: Record<string, any> = {}): void {
        const f = this.validationForm;
        if (!f) return;
        const v = { ...f.value, ...override };
        const brand = this.brands().find(b => b.id === v.brandId);
        const text = [
            this.selectedParentItem ? this.selectedParentItem.description : v.genericName,
            v.size, v.rating, v.specification, brand?.name
        ].map(p => (p ?? '').toString().trim()).filter(p => p).join(', ');
        if (text) {
            this.descriptionDisplay.set(text);
            if (this.descInput?.nativeElement) {
                this.descInput.nativeElement.value = text;
            }
            f.get('description')?.setValue(text, { emitEvent: false });
            this.dupSearch$.next(text);
        }
    }

    async openParentItemBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseItemModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.selectedParentItem = result.data;
                this.refreshDescription();
            }
        } catch {}
    }

    clearParentItem(): void {
        this.selectedParentItem = null;
        this.refreshDescription();
    }

    save(): void {
        this.submit = true;
        if (this.validationForm.invalid) return;

        const v = this.form.value;
        const payload = {
            id:             0,
            code:           v.code,
            description:    v.description,
            unit:           v.unitId ? { id: v.unitId } : null,
            parentItem:     this.selectedParentItem ? { id: this.selectedParentItem.id } : null,
            genericName:    v.genericName,
            size:           v.size,
            rating:         v.rating,
            specification:  v.specification,
            brand:          v.brandId ? { id: v.brandId } : null,
            manufacturer:   v.manufacturer,
            partNumber:     v.partNumber,
            remarks:        v.remarks,
            isActive:       true,
            assetAccount:   null,
            expenseAccount: null,
        };

        this.isLoading.set(true);
        this.service.create(payload).subscribe({
            next: (res: any) => {
                if (!res?.success) {
                    this.isLoading.set(false);
                    Swal.fire({ title: 'Error', text: res?.failureMessage || 'Failed to save item.', icon: 'error' });
                    return;
                }
                this.service.getData(res.modelId).subscribe({
                    next: (item: any) => {
                        this.isLoading.set(false);
                        this.activeModal.close({ id: item.id ?? res.modelId, description: item.description ?? v.description });
                    },
                    error: () => {
                        this.isLoading.set(false);
                        this.activeModal.close({ id: res.modelId, description: v.description });
                    }
                });
            },
            error: () => {
                this.isLoading.set(false);
                Swal.fire({ title: 'Error', text: 'Failed to save item.', icon: 'error' });
            }
        });
    }
}
