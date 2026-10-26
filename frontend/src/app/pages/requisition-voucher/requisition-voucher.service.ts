import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class RequisitionVoucherService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/purchase-request/list`);
    }

    listByDateRange(from: string, to: string, statusId?: number | null): Observable<any[]> {
        const id = statusId ?? 0;   // 0 = all statuses
        return this.http.get<any[]>(`${BASE_API}/purchase-request/list/${from}/${to}/${id}`);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/purchase-request/document-statuses`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/purchase-request/${id}`);
    }

    getDetails(id: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/rv-detail/rvd/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/purchase-request/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/purchase-request/update`, form, httpOptions);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/workflow-actions/${transId}`);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/purchase-request/process`, payload, httpOptions);
    }

    getModesOfProcurement(rvId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/purchase-request/modes-of-procurement/${rvId}`);
    }

    setModeOfProcurement(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/purchase-request/set-mode-of-procurement`, payload, httpOptions);
    }

    getDefaultSignatories(): Observable<any> {
        return this.http.get(`${BASE_API}/default-signatory/rv`);
    }

    getEntities(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/entities`);
    }

    getUnits(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/unit-measure/all`);
    }

    searchItems(q: string = '', page = 0, size = 20): Observable<any> {
        return this.http.get(`${BASE_API}/item/list?q=${encodeURIComponent(q)}&page=${page}&size=${size}`);
    }

    getVehicles(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/vehicles`);
    }

    getOffices(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/offices`);
    }

    getBudgetLineItems(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/budget-line-items`);
    }

    getBudgetSubItems(budgetLineItemDetailId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/budget-sub-items/${budgetLineItemDetailId}`);
    }

    getBudgetBalance(budgetLineItemDetailId: number): Observable<any> {
        return this.http.get(`${BASE_API}/json/budget-line-item-balance/${budgetLineItemDetailId}`);
    }

    getBudgetSubItemBalance(budgetSubItemId: number): Observable<any> {
        return this.http.get(`${BASE_API}/json/budget-sub-item-balance/${budgetSubItemId}`);
    }

    getWorkOrders(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/work-orders`);
    }

    getCostEstimates(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/cost-estimates`);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/document-logs/${transId}`);
    }

    /**
     * Opens the RV export in a new tab. Routing is by rvTypeId (numeric):
     *   1 = Items or Materials → /export
     *   2 = For IT             → /export1
     *   3 = Labor and Materials → /export2
     *   4 = For Labor          → /export3
     */
    print(id: number, rvTypeId: number): void {
        let path = '/purchase-request/export';
        if      (rvTypeId === 3) path = '/purchase-request/export2';
        else if (rvTypeId === 4) path = '/purchase-request/export3';
        else if (rvTypeId === 2) path = '/purchase-request/export1';
        this.downloadService.print(`${BASE_API}${path}/${id}`, { type: 'pdf' });
    }
}
