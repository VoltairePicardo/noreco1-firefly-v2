import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const BASE_URL = environment.get('baseUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class WithdrawalService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/withdrawal/list`);
    }

    listByDateRange(from: string, to: string, statusId?: number | null): Observable<any[]> {
        const url = statusId
            ? `${BASE_API}/withdrawal/list/${from}/${to}/${statusId}`
            : `${BASE_API}/withdrawal/list/${from}/${to}`;
        return this.http.get<any[]>(url);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/withdrawal/document-statuses`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/withdrawal/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/withdrawal/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/withdrawal/update`, form, httpOptions);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/withdrawal/process`, payload, httpOptions);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/json/workflow-actions/${transId}`);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.post<any[]>(`${BASE_URL}/document/${transId}/logs`, {}, httpOptions);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_URL}/withdrawal/export/${id}`, { type: 'pdf' });
    }

    getInventoryLocations(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/withdrawal/inventory-locations`);
    }

    getInventoryCategories(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/withdrawal/inventory-categories`);
    }

    getPurposes(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/withdrawal/purposes`);
    }

    getDefaultSignatories(): Observable<any> {
        return this.http.get(`${BASE_API}/withdrawal/default-signatories`);
    }

    getItemStocksForWithdrawal(locationId: number, categoryId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/withdrawal/item-stocks/${locationId}/${categoryId}`);
    }

    getRVDetailsForWithdrawal(rvId: number, locationId: number, categoryId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/withdrawal/rv-details/${rvId}/${locationId}/${categoryId}`);
    }

    getWorkOrderDetails(workOrderId: number, locationId: number, categoryId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/withdrawal/work-order-details/${workOrderId}/${locationId}/${categoryId}`);
    }

    getCostEstimateDetails(transactionId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/withdrawal/cost-estimate-details/${transactionId}`);
    }
}
