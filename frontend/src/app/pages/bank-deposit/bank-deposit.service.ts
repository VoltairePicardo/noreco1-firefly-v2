import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const BASE_URL = environment.get('baseUrl');

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

    getBankAccounts(q = '', page = 0, size = 10): Observable<any> {
        return this.http.get(`${BASE_API}/bank-account/list`, { params: { q, page, size } });
    }

    getDefaultDate(): Observable<any> {
        return this.http.get(`${BASE_API}/bank-deposit/default-date`);
    }

    getFiles(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/file/attachments/${transId}`);
    }

    uploadFiles(id: number, formData: FormData): Observable<any> {
        return this.http.post(`${BASE_API}/bank-deposit/${id}/upload`, formData);
    }

    fileUrl(fileId: number): string {
        return this.downloadService.printUrl(`${BASE_URL}/file/${fileId}`);
    }

    uploadDeposits(payload: { bankDepositLines: string[]; bankAccount: { id: number } }): Observable<any> {
        return this.http.post(`${BASE_API}/bank-deposit/upload-deposits`, payload, httpOptions);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/bank-deposit/export/${id}`, { type: 'pdf' });
    }
}
