import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';
import { StockWithdrawal, StockWithdrawalPage } from '@/app/models/inventory-modules/stock-withdrawal.model';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class WithdrawalService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    listPaged(from: string, to: string, statusId: number | null, query: string, page: number, size: number): Observable<any> {
        let params = new HttpParams().set('from', from).set('to', to).set('page', page).set('size', size);
        if (statusId != null) params = params.set('statusId', statusId);
        if (query) params = params.set('query', query);
        return this.http.get(`${BASE_API}/withdrawal/list-paged`, { params });
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

    getItemStocksForWithdrawal(locationId: number, categoryId: number, q = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/item-stock/item-stock/list-paged-with-zero-quantity/inv-loc/inv-cat/${locationId}/${categoryId}`, { params });
    }

    getRVDetailsForWithdrawal(rvId: number, locationId: number, categoryId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/rv-detail/rvd/type/withdrawal/${rvId}/${locationId}/${categoryId}`);
    }

    getWorkOrderDetails(workOrderId: number, locationId: number, categoryId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/withdrawal/work-order-details/${workOrderId}/${locationId}/${categoryId}`);
    }

    getCostEstimateDetails(transactionId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/cost-estimate/details-for-withdrawal/${transactionId}`);
    }

    listRvForWithdrawal(locationId: number, q = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/purchase-request/list/stock-withdrawal/${locationId}`, { params });
    }

    listCostEstimateForWithdrawal(locationId: number, q = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/cost-estimate/list/stock-withdrawal/${locationId}`, { params });
    }

    getMemorandumReceiptVouchers(query: string, page: number, size: number, multipleEmployee?: boolean): Observable<StockWithdrawalPage<StockWithdrawal>> {
        let params = new HttpParams().set('page', page).set('size', size);
        if (query) params = params.set('q', query);
        if (multipleEmployee != null) params = params.set('f', multipleEmployee);
        return this.http.get<StockWithdrawalPage<StockWithdrawal>>(`${BASE_API}/withdrawal/memorandum-receipt-vouchers`, { params });
    }
}
