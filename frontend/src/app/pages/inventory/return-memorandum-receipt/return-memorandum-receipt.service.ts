import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';
import { MemorandumReceipt, ReturnMemorandumReceiptDto, ReturnMemorandumReceiptListRow } from '@/app/models/inventory-modules/memorandum-receipt.model';
import { InventoryPage } from '@/app/models/shared/page.model';
import { Office } from '@/app/models/shared/reference.model';

const BASE_API = environment.get('baseApiUrl');
const BASE_URL = environment.get('baseUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class ReturnMemorandumReceiptService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    listPaged(from: string, to: string, employeeAccountNo: number | null, query: string, page: number, size: number): Observable<InventoryPage<ReturnMemorandumReceiptListRow>> {
        let params = new HttpParams().set('s', from).set('e', to).set('page', page).set('size', size);
        if (employeeAccountNo != null) params = params.set('em', employeeAccountNo);
        if (query) params = params.set('q', query);
        return this.http.get<InventoryPage<ReturnMemorandumReceiptListRow>>(`${BASE_API}/return-memorandum-receipt/list`, { params });
    }

    getData(id: number): Observable<ReturnMemorandumReceiptDto> {
        return this.http.get<ReturnMemorandumReceiptDto>(`${BASE_API}/return-memorandum-receipt/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/return-memorandum-receipt/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/return-memorandum-receipt/update`, form, httpOptions);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/return-memorandum-receipt/process`, payload, httpOptions);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_URL}/return-memorandum-receipt/export/${id}`, { type: 'pdf' });
    }

    getOffices(): Observable<Office[]> {
        return this.http.get<Office[]>(`${BASE_API}/json/offices`);
    }
}
