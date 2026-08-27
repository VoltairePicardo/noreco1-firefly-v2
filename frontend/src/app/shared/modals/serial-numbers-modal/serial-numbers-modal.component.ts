import { ChangeDetectionStrategy, Component, Input, OnInit, computed, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { AlertService } from '@/app/shared/services/alert.service';
import { SpecialEquipment } from '@/app/models/shared/reference.model';

@Component({
    selector: 'app-serial-numbers-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './serial-numbers-modal.component.html'
})
export class SerialNumbersModalComponent implements OnInit {
    /** Set by ModalService.openModal() inputs. */
    @Input() itemDescription = '';
    @Input() quantity        = 0;
    @Input() serialNumbers: SpecialEquipment[] = [];

    activeModal           = inject(NgbActiveModal);
    private alertService  = inject(AlertService);

    newSerialNo = '';
    rows        = signal<string[]>([]);
    total       = computed(() => this.rows().length);

    ngOnInit(): void {
        this.rows.set(this.serialNumbers.map(s => s.serialNo || '').filter(v => v));
    }

    addRow(): void {
        const value = this.newSerialNo.trim();
        if (!value) return;
        if (this.rows().some(r => r.trim() === value)) {
            this.alertService.warning('Serial Numbers', 'Validation', `Duplicate serial number: "${value}".`);
            return;
        }
        this.rows.update(list => [...list, value]);
        this.newSerialNo = '';
    }

    updateRow(index: number, value: string): void {
        this.rows.update(list => list.map((r, i) => i === index ? value : r));
    }

    removeRow(index: number): void {
        this.rows.update(list => list.filter((_, i) => i !== index));
    }

    isDuplicate(index: number): boolean {
        const list  = this.rows();
        const value = list[index]?.trim();
        if (!value) return false;
        return list.findIndex(r => r.trim() === value) !== index;
    }

    save(): void {
        const values = this.rows().map(r => r.trim()).filter(r => r.length > 0);

        if (values.length === 0) {
            this.alertService.warning('Serial Numbers', 'Validation', 'Enter at least one serial number.');
            return;
        }
        const duplicate = values.find((v, i) => values.indexOf(v) !== i);
        if (duplicate) {
            this.alertService.warning('Serial Numbers', 'Validation', `Duplicate serial number: "${duplicate}".`);
            return;
        }
        if (this.quantity > 0 && values.length !== this.quantity) {
            this.alertService.warning('Serial Numbers', 'Validation',
                `Number of serial numbers (${values.length}) must match the quantity (${this.quantity}).`);
            return;
        }

        const result: SpecialEquipment[] = values.map((serialNo, i) => ({
            id: this.serialNumbers[i]?.id ?? 0,
            serialNo
        }));
        this.activeModal.close({ action: 'save', data: result });
    }
}
