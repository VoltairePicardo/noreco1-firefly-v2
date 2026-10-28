import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const BASE_URL = environment.get('baseUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class PcvService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/pcv/list`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/pcv/${id}`);
    }

    create(form: any, files: File[] = []): Observable<any> {
        const fd = new FormData();
        fd.append('model', new Blob([JSON.stringify(form)], { type: 'application/json' }));
        files.forEach((f, i) => fd.append(`file_${i}`, f, f.name));
        return this.http.post(`${BASE_API}/pcv/create`, fd);
    }

    update(form: any, files: File[] = [], filesToRemove: any[] = []): Observable<any> {
        const fd = new FormData();
        fd.append('model', new Blob([JSON.stringify(form)], { type: 'application/json' }));
        fd.append('filesToRemove', new Blob([JSON.stringify(filesToRemove)], { type: 'application/json' }));
        files.forEach((f, i) => fd.append(`file_${i}`, f, f.name));
        return this.http.post(`${BASE_API}/pcv/update`, fd);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/pcv/process`, payload, httpOptions);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/workflow-actions/${transId}`);
    }

    getDefaultSignatories(): Observable<any> {
        return this.http.get(`${BASE_API}/pcv/default-signatories`);
    }

    getPettyCashFunds(): Observable<any[]> {
        return this.http.get<any>(`${BASE_API}/petty-cash-fund/list?size=200`).pipe(
            map((res: any) => res?.content ?? res ?? [])
        );
    }

    getBudgetLineItems(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/budget-line-items`);
    }

    getLogs(transId: number | string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/document-logs/${transId}`);
    }

    getFiles(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/file/attachments/${transId}`);
    }

    fileUrl(fileId: number): string {
        return this.downloadService.printUrl(`${BASE_URL}/file/${fileId}`);
    }

    getBatches(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/pcv/batches`);
    }

    createBatch(): Observable<any> {
        return this.http.post(`${BASE_API}/pcv/batch/create`, { status: 1 }, httpOptions);
    }

    closeBatch(batchId: number): Observable<any> {
        return this.http.post(`${BASE_API}/pcv/batch/close`, { id: batchId }, httpOptions);
    }

    getCheckVouchers(from: string, to: string, code: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/pcv/check-vouchers`, { params: { from, to, code } });
    }

    replenish(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/pcv/replenish`, payload, httpOptions);
    }

    getBudgetBalances(budgetLineItemDetailId: number): Observable<any> {
        return this.http.get(`${BASE_API}/pcv/budget-balances/${budgetLineItemDetailId}`);
    }

    getCashFlowItems(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/cashflow-item/list`);
    }

    getCashFlowBalances(budgetLineItemDetailId: number): Observable<any> {
        return this.http.get(`${BASE_API}/pcv/cashflow-balances/${budgetLineItemDetailId}`);
    }

    saveCashFlowItems(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/pcv/saveCashFlowItem`, payload, httpOptions);
    }

    getPcvCashFlowDetails(pcvId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/pcv/cash-flow/${pcvId}`);
    }

    isDocumentForCashFlowAssignment(transactionId: number): Observable<boolean> {
        return this.http.get<boolean>(`${BASE_API}/pcv/is-document-for-cash-flow-item-assignment/${transactionId}`);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/petty-cash-voucher/export/${id}`, { type: 'pdf' });
    }

    getOffices(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/offices`);
    }

    getUserOffice(): Observable<any> {
        return this.http.get(`${BASE_API}/json/office-user`);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/document-statuses`);
    }

    getCloseoutVouchers(params: any): Observable<any[]> {
        const p: any = {};
        if (params.from)             p['from']             = params.from;
        if (params.to)               p['to']               = params.to;
        if (params.officeId)         p['officeId']         = params.officeId;
        if (params.documentStatusId) p['documentStatusId'] = params.documentStatusId;
        return this.http.get<any[]>(`${BASE_API}/pcv/closeout-vouchers`, { params: p });
    }

    printCloseout(checkedByAcctNo: number, replenishedByAcctNo: number, params: Record<string, any>): void {
        this.downloadService.print(
            `${BASE_API}/petty-cash-voucher/print/${checkedByAcctNo}/${replenishedByAcctNo}`,
            { type: 'pdf', ...params }
        );
    }
}
