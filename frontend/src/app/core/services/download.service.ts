import { Injectable } from '@angular/core';
import {AuthService} from '@/app/pages/auth/auth.service';

@Injectable({
  providedIn: 'root'
})
export class DownloadService {

    constructor(private authService: AuthService) {}

    print(url: string, params: Record<string, any> = {}) {
        if (!url) {
            console.error('URL cannot be null or empty');
            return; // Prevent further execution if the URL is not valid
        }

        const token =this.authService.getToken();

        if (!token) {
            console.error("Token missing — cannot print OR. Perform hard refresh.");
            return;
        }

        url = url + '?token=' + token;

        let qs = new URLSearchParams(params).toString(); // Use URLSearchParams to stringify params
        url = url + '&' + qs;

        const newTab = window.open(url, '_blank');
        if (newTab) {
            newTab.focus();
        } else {
            console.error('Failed to open new tab. Please allow pop-ups for this website.');
        }
    }

    printWithRequestBody(url: string, params: Record<string, any> = {}) {
        if (!url) {
            console.error('URL cannot be null or empty');
            return; // Prevent further execution if the URL is not valid
        }

        const token =this.authService.getToken();
        url = url + '?token=' + token;

        let qs = new URLSearchParams(params).toString(); // Use URLSearchParams to stringify params
        url = url + '&' + qs;

        var newTab = window.open(url, '_blank');
        newTab?.focus();
    }

    printUrl(url: string, params: any = {}) {

        const token =this.authService.getToken();
        if (!token) {
            console.error("Token missing — cannot print OR. Perform hard refresh.");
            return '';
        }

        url = url + '?token=' + token;

        let qs = new URLSearchParams(params).toString(); // Use URLSearchParams to stringify params
        url = url + '&' + qs;

        return url;
    }

    viewFile(url: string, params: any = {}) {

        const token =this.authService.getToken();
        url = url + '?token=' + token;

        let qs = new URLSearchParams(params).toString(); // Use URLSearchParams to stringify params
        url = url + '&' + qs;

        var newTab = window.open(url, '_blank');
        newTab?.focus();
    }


    downloadBlob(blob: Blob, filename: string) {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = filename;

        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);

        window.URL.revokeObjectURL(url);
    }
}
