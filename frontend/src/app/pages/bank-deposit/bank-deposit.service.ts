import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');

const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class BankDepositService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/bank-deposit/list`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/bank-deposit/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/bank-deposit/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/bank-deposit/update`, form, httpOptions);
    }

    getBankAccounts(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/bank-account/list`);
    }

    getFiles(id: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/bank-deposit/${id}/files`);
    }

    uploadFiles(id: number, formData: FormData): Observable<any> {
        return this.http.post(`${BASE_API}/bank-deposit/${id}/upload`, formData);
    }

    fileUrl(fileId: number): string {
        return `${BASE_API}/bank-deposit/file/${fileId}`;
    }

    uploadDeposits(formData: FormData): Observable<any> {
        return this.http.post(`${BASE_API}/bank-deposit/upload-deposits`, formData);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/bank-deposit/export/${id}`, { type: 'pdf' });
    }
}
