import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
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
import { BrowseSpecialEquipmentModalComponent } from '@/app/shared/modals/browse-special-equipment-modal/browse-special-equipment-modal.component';
import { NG_ICON_DIRECTIVES } from '@ng-icons/core';
import { OtherSpecialEquipmentTestingService } from '../other-special-equipment-testing.service';
import { OtherSpecialEquipmentTestingSpecialEquipment } from '@/app/models/special-equipment-testing/other-special-equipment-testing.model';

type UserFieldKey = 'verifiedBy' | 'checkedBy' | 'approvedBy';

interface EntitySummary {
    accountNo: number | null;
    name: string | null;
}

interface EntityModalResult {
    action?: string;
    data?: EntitySummary;
}

interface SpecialEquipmentModalResult {
    action?: string;
    data?: OtherSpecialEquipmentTestingSpecialEquipment;
}

interface OtherSpecialEquipmentTestingCreateResponse {
    success: boolean;
    modelId?: number;
    failureMessage?: string;
}

@Component({
    selector: 'app-other-special-equipment-testing-add-edit',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrDirective, NG_ICON_DIRECTIVES],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './other-special-equipment-testing-add-edit.component.html'
})
export class OtherSpecialEquipmentTestingAddEditComponent {
    module    = 'Other Special Equipment Testing';
    subModule = 'Create';
    menuLink  = 'other-special-equipment-testing';

    dateFlatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    isLoading = signal(false);
    submit    = signal(false);

    date = signal(new Date().toISOString().substring(0, 10));

    specialEquipmentId       = signal<number | null>(null);
    specialEquipmentSerialNo = signal('');
    specialEquipmentType     = signal('');
    specialEquipmentBrand    = signal('');
    specialEquipmentOwner    = signal('');

    passed  = signal(false);
    remarks = signal('');

    verifiedByAccountNo = signal<number | null>(null);
    verifiedByName      = signal('');
    checkedByAccountNo  = signal<number | null>(null);
    checkedByName       = signal('');
    approvedByAccountNo = signal<number | null>(null);
    approvedByName      = signal('');

    private router  = inject(Router);
    private alertService = inject(AlertService);
    private modalService = inject(SharedModalService);
    private service = inject(OtherSpecialEquipmentTestingService);

    async openSpecialEquipmentBrowse(): Promise<void> {
        try {
            const result: SpecialEquipmentModalResult = await this.modalService.openModal(
                BrowseSpecialEquipmentModalComponent,
                {},
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.selectSpecialEquipment(result.data);
            }
        } catch {
            // modal dismissed — no action needed
        }
    }

    selectSpecialEquipment(specialEquipment: OtherSpecialEquipmentTestingSpecialEquipment): void {
        this.specialEquipmentId.set(specialEquipment.id);
        this.specialEquipmentSerialNo.set(specialEquipment.serialNo ?? '');
        this.specialEquipmentType.set(specialEquipment.specialEquipmentType?.description ?? '');
        this.specialEquipmentBrand.set(specialEquipment.brand?.name ?? '');
        this.specialEquipmentOwner.set(specialEquipment.owner ?? '');
    }

    clearSelectedSpecialEquipment(): void {
        this.specialEquipmentId.set(null);
        this.specialEquipmentSerialNo.set('');
        this.specialEquipmentType.set('');
        this.specialEquipmentBrand.set('');
        this.specialEquipmentOwner.set('');
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
            case 'verifiedBy':
                this.verifiedByAccountNo.set(accountNo);
                this.verifiedByName.set(name);
                break;
            case 'checkedBy':
                this.checkedByAccountNo.set(accountNo);
                this.checkedByName.set(name);
                break;
            case 'approvedBy':
                this.approvedByAccountNo.set(accountNo);
                this.approvedByName.set(name);
                break;
        }
    }

    save(): void {
        this.submit.set(true);

        if (!this.specialEquipmentId()) { return; }

        const payload = {
            date: this.date() || null,
            specialEquipment: { id: this.specialEquipmentId() },
            passed: this.passed(),
            remarks: this.remarks() || null,
            verifiedBy: this.verifiedByAccountNo() ? { accountNo: this.verifiedByAccountNo() } : null,
            checkedBy: this.checkedByAccountNo() ? { accountNo: this.checkedByAccountNo() } : null,
            approvedBy: this.approvedByAccountNo() ? { accountNo: this.approvedByAccountNo() } : null
        };

        this.isLoading.set(true);
        this.service.create(payload).subscribe({
            next: (res: OtherSpecialEquipmentTestingCreateResponse) => {
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
