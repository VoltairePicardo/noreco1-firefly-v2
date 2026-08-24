import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const BASE_URL = environment.get('baseUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class MemorandumReceiptService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/memorandum-receipt/list`);
    }

    listByDateRange(from: string, to: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/memorandum-receipt/list/${from}/${to}`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/memorandum-receipt/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/memorandum-receipt/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/memorandum-receipt/update`, form, httpOptions);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/memorandum-receipt/process`, payload, httpOptions);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/workflow-actions/${transId}`);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/document-logs/${transId}`);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_URL}/memorandum-receipt/export/${id}`, { type: 'pdf' });
    }

    getDefaultSignatories(): Observable<any> {
        return this.http.get(`${BASE_API}/memorandum-receipt/default-signatories`);
    }

    getStockWithdrawals(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/memorandum-receipt/stock-withdrawals`);
    }

    getStockWithdrawalBalance(detailId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/memorandum-receipt/stock-withdrawal-balance/${detailId}`);
    }

    listByEmployee(from: string, to: string, accountNo: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/memorandum-receipt/list/${from}/${to}?em=${accountNo}`);
    }

    createMultiple(forms: any[]): Observable<any> {
        return this.http.post(`${BASE_API}/memorandum-receipt/create-multiple`, forms, httpOptions);
    }

    createReturnedMr(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/memorandum-receipt/create-returned-mr`, form, httpOptions);
    }

    getStockWithdrawalEmployees(swId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/memorandum-receipt/sw-employees/${swId}`);
    }

    getReturnedMrsByEmployee(accountNo: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/memorandum-receipt/returned-memos/${accountNo}`);
    }
}
