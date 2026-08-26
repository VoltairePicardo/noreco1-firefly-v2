import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';
import { MemorandumReceiptDto, MemorandumReceiptListRow, MemorandumReceiptPage, SlEntity } from '@/app/models/inventory-modules/memorandum-receipt.model';
import { WorkflowAction } from '@/app/models/workflow-action.model';
import { DocumentLog } from '@/app/shared/services/any-json.service';

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

    getData(id: number): Observable<MemorandumReceiptDto> {
        return this.http.get<MemorandumReceiptDto>(`${BASE_API}/memorandum-receipt/${id}`);
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

    getWorkflowActions(transId: number): Observable<WorkflowAction[]> {
        return this.http.get<WorkflowAction[]>(`${BASE_URL}/json/workflow-actions/${transId}`);
    }

    getDocumentLogs(transId: number): Observable<DocumentLog[]> {
        return this.http.post<DocumentLog[]>(`${BASE_URL}/document/${transId}/logs`, {}, httpOptions);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_URL}/memorandum-receipt/export/${id}`, { type: 'pdf' });
    }

    getDefaultSignatories(): Observable<{ approvedBy?: SlEntity; approvingOfficer?: SlEntity }> {
        return this.http.get<{ approvedBy?: SlEntity; approvingOfficer?: SlEntity }>(`${BASE_API}/memorandum-receipt/default-signatories`);
    }

    getStockWithdrawals(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/memorandum-receipt/stock-withdrawals`);
    }

    getStockWithdrawalBalance(detailId: number): Observable<{ assigned?: number }[]> {
        return this.http.get<{ assigned?: number }[]>(`${BASE_API}/memorandum-receipt/stock-withdrawal-balance/${detailId}`);
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
        return this.http.get<any[]>(`${BASE_API}/memorandum-receipt/stock-withdrawal-employees/${swId}`);
    }

    getReturnedMrsByEmployee(accountNo: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/memorandum-receipt/returned-memos/${accountNo}`);
    }

    listPaged(from: string, to: string, statusId: number | null, employeeAccountNo: number | null, query: string, page: number, size: number): Observable<MemorandumReceiptPage<MemorandumReceiptListRow>> {
        let params = new HttpParams().set('from', from).set('to', to).set('page', page).set('size', size);
        if (statusId != null) params = params.set('statusId', statusId);
        if (employeeAccountNo != null) params = params.set('em', employeeAccountNo);
        if (query) params = params.set('query', query);
        return this.http.get<MemorandumReceiptPage<MemorandumReceiptListRow>>(`${BASE_API}/memorandum-receipt/list-paged`, { params });
    }
}
