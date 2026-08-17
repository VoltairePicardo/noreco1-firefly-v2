import {Component, Input} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';

@Component({
  selector: 'app-template-select-modal',
    imports: [ ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
  templateUrl: './template-select-modal.component.html',
  styleUrl: './template-select-modal.component.scss'
})
export class TemplateSelectModalComponent {

    @Input() title: string = 'Select Template';
    @Input() text: string = 'Select template where to print the billing invoice.';

    templateOption: number = 1;

    options: { id: number; label: string }[] = [
        { id: 1, label: 'Template 1 (Individual Consumer 2025)' },
        { id: 2, label: 'Template 2 (BAPA 2025)' },
        { id: 3, label: 'Template 3 (NORECO1 Style 2025)' },
        { id: 4, label: 'Template 4 (Bill of Collection Invoice)' }
    ];

    constructor(public activeModal: NgbActiveModal) {}

    ngOnInit(): void {}

    confirm(): void {
        this.activeModal.close({
            action: 'confirm',
            template: this.templateOption
        });
    }

    close(): void {
        this.activeModal.dismiss('cancel');
    }

}
