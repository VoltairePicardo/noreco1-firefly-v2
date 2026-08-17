import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class ApproveVouchersService {
    private http = inject(HttpClient);

    getVouchers(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/approve-vouchers/vouchers`);
    }

    process(payload: { documentId: number; remarks: string; documentType: string }): Observable<any> {
        return this.http.post(`${BASE_API}/approve-vouchers/process`, payload, httpOptions);
    }

    processAll(payloads: any[]): Observable<any> {
        return this.http.post(`${BASE_API}/approve-vouchers/process-all`, payloads, httpOptions);
    }
}
