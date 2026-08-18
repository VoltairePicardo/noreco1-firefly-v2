import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const BASE_URL = environment.get('baseUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class ReturnMemorandumReceiptService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/return-memorandum-receipt/list`);
    }

    listByDateRange(from: string, to: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/return-memorandum-receipt/list/${from}/${to}`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/return-memorandum-receipt/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/return-memorandum-receipt/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/return-memorandum-receipt/update`, form, httpOptions);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/return-memorandum-receipt/process`, payload, httpOptions);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/json/workflow-actions/${transId}`);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.post<any[]>(`${BASE_URL}/document/${transId}/logs`, {}, httpOptions);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_URL}/return-memorandum-receipt/export/${id}`, { type: 'pdf' });
    }

    getOffices(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/json/offices/`);
    }

    getEmployeeMemorandumReceipts(accountNo: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/return-memorandum-receipt/employee-mr/${accountNo}`);
    }
}
