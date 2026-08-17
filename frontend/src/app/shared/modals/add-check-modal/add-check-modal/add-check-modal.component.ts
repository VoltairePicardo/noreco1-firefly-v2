import { Component } from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {DateHelper} from '@/app/helpers/date-helper';
import {
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import {LaddaModule} from 'angular2-ladda';
import {TagifyModule} from 'ngx-tagify';
import {FlatpickrDirective, provideFlatpickrDefaults} from 'angularx-flatpickr';
import {provideIcons} from '@ng-icons/core';
import {tablerPlus, tablerCalendar, tablerX} from '@ng-icons/tabler-icons';

@Component({
  selector: 'app-add-check-modal',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, LaddaModule, TagifyModule, FlatpickrDirective],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS, provideIcons({
        tablerCalendar, tablerX, tablerPlus
    })],
  templateUrl: './add-check-modal.component.html',
  styleUrl: './add-check-modal.component.scss'
})
export class AddCheckModalComponent {

    check = {
        checkNumber: '',
        bank: '',
        checkAmount: null as number | null,
        checkDate: DateHelper.dateToday()
    };

    constructor(public activeModal: NgbActiveModal) {}

    onClose(): void {
        this.activeModal.dismiss();
    }

    addCheck(): void {
        this.activeModal.close(this.check);
    }

}
