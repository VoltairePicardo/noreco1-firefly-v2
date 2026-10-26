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
export class JobOrderService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    listByDateRange(from: string, to: string, statusId?: number | null): Observable<any[]> {
        const id = statusId ?? 0;
        return this.http.get<any[]>(`${BASE_API}/job-order/list/${from}/${to}/${id}`);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/job-order/document-statuses`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/job-order/${id}`);
    }

    getDetails(id: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/job-order/detail/${id}`);
    }

    /** Vendors that have APPROVED JOs with remaining acceptance. */
    getApprovedVendors(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/job-order/approved-vendors`);
    }

    /** APPROVED JOs for a specific vendor (for JOA form). */
    getJobOrdersBySupplier(accountNo: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/job-order/by-supplier/${accountNo}`);
    }

    /** Non-fully-accepted items of a JO (for JOA form). */
    getDetailsForJoa(joId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/job-order/detail-for-joa/${joId}`);
    }

    getEntities(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/entities`);
    }

    getRvDetailsForJo(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/rv-detail/rvd/type/jo`);
    }

    getPurchaseRequestsForJo(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/rv-detail/purchase-request/for-jo`);
    }

    getPurchaseRequestItems(prId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/rv-detail/rvd/${prId}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/job-order/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/job-order/update`, form, httpOptions);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/job-order/process`, payload, httpOptions);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/workflow-actions/${transId}`);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/document-logs/${transId}`);
    }

    getDefaultSignatories(): Observable<any> {
        return this.http.get(`${BASE_API}/default-signatory/jo`);
    }

    getQuotationPrice(vendorAccountNo: number, rvDetailId: number): Observable<any> {
        return this.http.get(`${BASE_API}/quotation-detail/quotation-price/${vendorAccountNo}/${rvDetailId}`);
    }

    getPrBudgetBalance(budgetLineItemDetailId: number): Observable<any> {
        return this.http.get(`${BASE_API}/json/budget-line-item-balance/${budgetLineItemDetailId}`);
    }

    isDocumentForCashFlowItemAssignment(transId: number): Observable<boolean> {
        return this.http.get<boolean>(`${BASE_API}/job-order/is-document-for-cash-flow-item-assignment/${transId}`);
    }

    saveCashFlowItem(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/job-order/saveCashFlowItem`, payload, httpOptions);
    }

    getBudgetDetails(joId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/job-order/budget-details/${joId}`);
    }

    getBudgetSubItems(budgetLineItemDetailId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/budget-sub-items/${budgetLineItemDetailId}`);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/job-order/export/${id}`, { type: 'pdf' });
    }
}
