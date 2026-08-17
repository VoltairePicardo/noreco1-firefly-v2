import {Component, Input} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import {LaddaModule} from 'angular2-ladda';
import {TagifyModule} from 'ngx-tagify';
import {provideFlatpickrDefaults} from 'angularx-flatpickr';
import {NgIcon, provideIcons} from '@ng-icons/core';
import {tablerCheck, tablerX} from '@ng-icons/tabler-icons';

@Component({
  selector: 'app-enter-remarks-modal',
    imports: [...COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, LaddaModule, TagifyModule, NgIcon],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS, provideIcons({
        tablerCheck, tablerX
    })],
  templateUrl: './enter-remarks-modal.component.html',
  styleUrl: './enter-remarks-modal.component.scss'
})
export class EnterRemarksModalComponent {

    @Input() title: string = 'Enter Remarks';

    remarks: string = '';

    constructor(public activeModal: NgbActiveModal) {}

    close() {
        this.activeModal.close();
    }

    confirm() {
        this.activeModal.close({
            action: 'confirm',
            data: this.remarks
        });
    }

}
