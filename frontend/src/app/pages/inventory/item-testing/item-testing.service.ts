import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';
import { InventoryLocationEntity } from '@/app/models/inventory-modules/item-stock.model';
import { ItemTestingDto, ItemTestingListRow, PoDetailForTesting } from '@/app/models/inventory-modules/item-testing.model';
import { InventoryPage } from '@/app/models/shared/page.model';

const BASE_API = environment.get('baseApiUrl');
const BASE_URL = environment.get('baseUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class ItemTestingService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/item-testing/list`);
    }

    listByDateRange(from: string, to: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/item-testing/list/${from}/${to}`);
    }

    listPaged(from: string, to: string, page: number, size: number): Observable<InventoryPage<ItemTestingListRow>> {
        const params = new HttpParams().set('from', from).set('to', to).set('page', page).set('size', size);
        return this.http.get<InventoryPage<ItemTestingListRow>>(`${BASE_API}/item-testing/list-paged`, { params });
    }

    getData(id: number): Observable<ItemTestingDto> {
        return this.http.get<ItemTestingDto>(`${BASE_API}/item-testing/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/item-testing/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/item-testing/update`, form, httpOptions);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/item-testing/process`, payload, httpOptions);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/workflow-actions/${transId}`);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/document-logs/${transId}`);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/item-testing/export/${id}`, { type: 'pdf' });
    }

    getInventoryLocations(): Observable<InventoryLocationEntity[]> {
        return this.http.get<InventoryLocationEntity[]>(`${BASE_API}/item-testing/inventory-locations`);
    }

    getPurchaseOrderDetailsForItemTesting(poId: number): Observable<PoDetailForTesting[]> {
        return this.http.get<PoDetailForTesting[]>(`${BASE_API}/po-detail/pod-for-item-testing/${poId}`);
    }

    delete(id: number): Observable<any> {
        return this.http.post(`${BASE_API}/item-testing/delete/${id}`, {}, httpOptions);
    }
}
