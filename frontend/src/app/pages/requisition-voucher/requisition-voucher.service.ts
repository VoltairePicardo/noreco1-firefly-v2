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
        return this.http.get<any[]>(`${BASE_API}/requisition-voucher/list`);
    }

    listByDateRange(from: string, to: string, statusId?: number | null): Observable<any[]> {
        const url = statusId
            ? `${BASE_API}/requisition-voucher/list/${from}/${to}/${statusId}`
            : `${BASE_API}/requisition-voucher/list/${from}/${to}`;
        return this.http.get<any[]>(url);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/requisition-voucher/document-statuses`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/requisition-voucher/${id}`);
    }

    getDetails(id: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/rv-detail/rvd/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/requisition-voucher/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/requisition-voucher/update`, form, httpOptions);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/workflow-actions/${transId}`);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/requisition-voucher/process`, payload, httpOptions);
    }

    getModesOfProcurement(rvId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/requisition-voucher/modes-of-procurement/${rvId}`);
    }

    setModeOfProcurement(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/requisition-voucher/set-mode-of-procurement`, payload, httpOptions);
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
     * Opens the RV export in a new tab. The export endpoint is determined by
     * RV type: For PO → /export, For IT → /export1, For REP → /export2, For LAB → /export3.
     */
    print(id: number, rvType: string): void {
        let path = '/requisition-voucher/export';
        if      (rvType.includes('REP')) path = '/requisition-voucher/export2';
        else if (rvType.includes('LAB')) path = '/requisition-voucher/export3';
        else if (rvType.includes('IT'))  path = '/requisition-voucher/export1';
        this.downloadService.print(`${BASE_API}${path}/${id}`, { type: 'pdf' });
    }
}
