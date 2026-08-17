import { Injectable } from '@angular/core';
import {NgbModal, NgbModalOptions} from '@ng-bootstrap/ng-bootstrap';

// modal-config.ts
export interface ModalConfig {
    title: string;
    message: string;
    confirmButtonText?: string;
    cancelButtonText?: string;
    data?: any; // Optional: for passing complex objects
}

@Injectable({
  providedIn: 'root'
})
export class SharedModalService { // universal shared open-modal function (tailored from browse-users-modal-service)

    constructor(private modalService: NgbModal) {}

    openModal(component: any, inputs?: any, options?: NgbModalOptions): Promise<any> {
        const modalRef = this.modalService.open(component, {
            backdrop: 'static',
            keyboard: false,
            ...options
        });

        if (inputs) {
            Object.keys(inputs).forEach(key => {
                modalRef.componentInstance[key] = inputs[key];
            });
        }

        return modalRef.result;
    }
}
