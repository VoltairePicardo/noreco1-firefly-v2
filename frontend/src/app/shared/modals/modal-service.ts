import { Injectable } from '@angular/core';
import { NgbModal, NgbModalOptions } from '@ng-bootstrap/ng-bootstrap';

export interface ModalConfig {
    title: string;
    message: string;
    confirmButtonText?: string;
    cancelButtonText?: string;
    data?: any;
}

@Injectable({
  providedIn: 'root'
})
export class ModalService {
    constructor(private modalService: NgbModal) {}

    openModal(component: any, inputs?: any, options?: NgbModalOptions): Promise<any> {
        const modalRef = this.modalService.open(component, {
            backdrop: 'static',
            keyboard: false,
            container: 'body',
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
