import { inject, Injectable, NgZone } from '@angular/core';
import Swal, {SweetAlertIcon} from 'sweetalert2';

@Injectable({
  providedIn: 'root'
})
export class AlertService {
    private ngZone = inject(NgZone);
    success(module: string, action: string, message: string) {
        return Swal.fire({
            icon: 'success',
            title: module + ' ' + action,
            text: message != "" ? message : (module + ' Successfully ' + action),
            timer: 3000,
            showConfirmButton: false,
            didOpen: () => {
                const handler = (e: KeyboardEvent) => {
                    if (e.key === 'Enter') {
                        e.preventDefault();
                        e.stopPropagation();
                        document.removeEventListener('keydown', handler, true);
                        Swal.close();
                    }
                };
                document.addEventListener('keydown', handler, true);
            }
        });
    }

    httpError(module: string, action: string, err: any): void {
        const msg = err?.error?.failureMessage
                 || (Array.isArray(err?.error?.messages) ? err.error.messages.join(', ') : '')
                 || err?.error?.message
                 || err?.message
                 || '';
        this.error(module, action, msg);
    }

    error(module: string, action: string, message: string) {
        Swal.fire({
            icon: 'error',
            title: action,
            text: message != "" ? message : ('Failed to '+ action + ' ' + module + '.'),
            timer: 3000,
            // timerProgressBar: true,
            showConfirmButton: false,
            didOpen: () => {
                const handler = (e: KeyboardEvent) => {
                    if (e.key === 'Enter') {
                        e.preventDefault();
                        e.stopPropagation();
                        document.removeEventListener('keydown', handler, true);
                        Swal.close();
                    }
                };
                document.addEventListener('keydown', handler, true);
            }
        });
    }

    errorHtml(module: string, action: string, message: string) {
        const html = message
            ? message.replace(/\n/g, '<br>')
            : 'Failed to ' + action + ' ' + module + '.';
        Swal.fire({
            icon: 'error',
            title: action,
            html,
            showConfirmButton: true,
            buttonsStyling: false,
            customClass: {
                confirmButton: 'btn btn-primary'
            }
        });
    }

    successConfirm(title: string, html: string, confirmText: string = 'OK') {
        return Swal.fire({
            icon: 'success',
            title,
            html,
            confirmButtonText: confirmText,
            cancelButtonText: 'Cancel',
            showCancelButton: true,
            buttonsStyling: false,
            customClass: {
                confirmButton: 'btn btn-primary me-2',
                cancelButton: 'btn btn-light text-dark'
            }
        });
    }

    confirm(text: string) {
        return Swal.fire({
            title: 'Are you sure?',
            html: text,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Confirm',
            cancelButtonText: 'Cancel',
            buttonsStyling: false,
            customClass: {
                confirmButton: 'btn btn-primary me-2',
                cancelButton: 'btn btn-light text-dark'
            }
        });
    }

    confirmWithCallback(title: string, text: string, callback: () => void): void {
        Swal.fire({
            title: title,
            html: text,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Confirm',
            cancelButtonText: 'Cancel',
            buttonsStyling: false,
            customClass: {
                confirmButton: 'btn btn-primary me-2',
                cancelButton: 'btn btn-light text-dark'
            }
        }).then((result) => {
            if (result.isConfirmed) {
                this.ngZone.run(() => callback());
            }
        });
    }

    warning(module: string, action: string, message: string) {
        Swal.fire({
            icon: 'warning',
            title: module + ' ' + action,
            text: message != "" ? message : (module + ' ' + action),
            timer: 3000,
            // timerProgressBar: true,
            showConfirmButton: false,
            didOpen: () => {
                const handler = (e: KeyboardEvent) => {
                    if (e.key === 'Enter') {
                        e.preventDefault();
                        e.stopPropagation();
                        document.removeEventListener('keydown', handler, true);
                        Swal.close();
                    }
                };
                document.addEventListener('keydown', handler, true);
            }
        });
    }

    fieldWarning(module: string, action: string, messages: string[]) {
        const listItems = messages.map((message: string) =>
            `<li style="text-align:left; margin-bottom: 4px;">${message}</li>`
        ).join('');

        Swal.fire({
            icon: 'warning',
            title: module + ' ' + action,
            html: `<ul style="padding-left: 1rem; margin: 0;">${listItems}</ul>`,
            timer: 5000,
            timerProgressBar: true,
            showConfirmButton: false
        });
    }

    showAlert(
        icon: SweetAlertIcon,
        title: string,
        message: string = '',
        timer?: number,
        timerProgressBar: boolean = false,
        showConfirmButton: boolean = true
    ) {
        Swal.fire({
            icon,
            title,
            text: message,
            timer,
            timerProgressBar,
            showConfirmButton
        });
    }

}
