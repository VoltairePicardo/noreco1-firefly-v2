import {Component, Input} from '@angular/core';
import {DateHelper} from '@/app/helpers/date-helper';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import {LaddaModule} from 'angular2-ladda';
import {TagifyModule} from 'ngx-tagify';
import {FlatpickrDirective, provideFlatpickrDefaults} from 'angularx-flatpickr';
import {provideIcons} from '@ng-icons/core';
import {tablerCalendar, tablerX, tablerAdjustmentsCog } from '@ng-icons/tabler-icons';

@Component({
  selector: 'app-new-due-date-modal',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, LaddaModule, TagifyModule, FlatpickrDirective],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS, provideIcons({
        tablerCalendar, tablerX, tablerAdjustmentsCog
    })],
  templateUrl: './new-due-date-modal.component.html',
  styleUrl: './new-due-date-modal.component.scss'
})
export class NewDueDateModalComponent {

    @Input() title: string = 'New Due Date';
    @Input() billNumber: number = 0;
    @Input() oldDueDate: string = '';
    newDueDate = DateHelper.dateToday();

    constructor(public activeModal: NgbActiveModal) {}

    ngOnInit(): void {
        if ((this as any).data) {
            const data = (this as any).data;
            this.billNumber = data.billNumber ?? 0;
            this.oldDueDate = data.oldDueDate ?? '';
            this.newDueDate = data.oldDueDate ? new Date(data.oldDueDate) : DateHelper.dateToday();
        }
    }

    onSend() {
        this.activeModal.close({
            billNumber: this.billNumber,
            oldDueDate: this.oldDueDate,
            newDueDate: DateHelper.matDateToSql(this.newDueDate)
        });
    }

    onClose() {
        this.activeModal.dismiss();
    }

}
