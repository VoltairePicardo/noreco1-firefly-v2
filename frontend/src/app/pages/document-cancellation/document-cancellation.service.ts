import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class DocumentCancellationService {
    private http = inject(HttpClient);

    getDocumentTypes(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/document-cancellation/document-types`);
    }

    search(docType: string, from: string, to: string, cancelledOnly: boolean): Observable<any[]> {
        const cancelledParam = cancelledOnly ? '?cancelled' : '';
        return this.http.get<any[]>(`${BASE_API}/document-cancellation/${docType}/${from}/${to}${cancelledParam}`);
    }

    cancel(documentType: string, transId: number, remarks: string): Observable<any> {
        const params = new HttpParams().set('remarks', remarks);
        return this.http.post(`${BASE_API}/document-cancellation/cancel/${documentType}/${transId}`, {}, { params });
    }

    restore(documentType: string, transId: number): Observable<any> {
        return this.http.post(`${BASE_API}/document-cancellation/restore/${documentType}/${transId}`, {}, httpOptions);
    }

    getCancellationDetails(transId: number): Observable<any> {
        return this.http.get(`${BASE_API}/document-cancellation/details/${transId}`);
    }
}
