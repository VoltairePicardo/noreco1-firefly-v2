import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class CreditCardPurchaseRequestService {
    private http = inject(HttpClient);
    private downloadService = inject(DownloadService);

    list(from: string, to: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/credit-card-purchase-request/list/${from}/${to}`);
    }

    listByStatus(from: string, to: string, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/credit-card-purchase-request/list/${from}/${to}/${statusId}`);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/credit-card-purchase-request/document-statuses`);
    }

    getData(id: number): Observable<any> {
        return this.http.get<any>(`${BASE_API}/credit-card-purchase-request/${id}`);
    }

    create(payload: any): Observable<any> {
        return this.http.post<any>(`${BASE_API}/credit-card-purchase-request/create`, payload, httpOptions);
    }

    createBatch(): Observable<any> {
        return this.http.post<any>(`${BASE_API}/credit-card-purchase-request/create-batch`, { status: 0 }, httpOptions);
    }

    update(payload: any): Observable<any> {
        return this.http.post<any>(`${BASE_API}/credit-card-purchase-request/update`, payload, httpOptions);
    }

    process(payload: any): Observable<any> {
        return this.http.post<any>(`${BASE_API}/credit-card-purchase-request/process`, payload, httpOptions);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/credit-card-purchase-request/export/${id}`, { type: 'pdf' });
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/workflow-actions/${transId}`);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/document-logs/${transId}`);
    }

    searchEntities(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/entities`);
    }

    getFundingSources(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/funding-sources`);
    }

    getModes(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/modes`);
    }

    getPurchaseOrderItems(poId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/purchase-order/detail/${poId}`);
    }

    getJobOrderItems(joId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/job-order/detail/${joId}`);
    }
}
