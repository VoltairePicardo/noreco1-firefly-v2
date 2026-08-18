import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
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
        return this.http.get<any[]>(`${BASE_API}/json/workflow-actions/${transId}`);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.post<any[]>(`${BASE_API}/document/${transId}/logs`, {}, httpOptions);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/withdrawal/export/${id}`, { type: 'pdf' });
    }

    getInventoryLocations(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/inventory-location/all`);
    }

    getInventoryCategories(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/inventory-category/list`);
    }

    getPurposes(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/purposes`);
    }

    getDefaultSignatories(): Observable<any> {
        return this.http.get(`${BASE_API}/withdrawal/default-signatories`);
    }

    getItemStocksForWithdrawal(locationId: number, categoryId: number, q = ''): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', 0).set('size', 1000);
        return this.http.get(`${BASE_API}/item-stock/item-stock/list-paged-with-zero-quantity/inv-loc/inv-cat/${locationId}/${categoryId}`, { params });
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
