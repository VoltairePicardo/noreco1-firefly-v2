import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_URL = environment.get('baseUrl');

@Injectable({ providedIn: 'root' })
export class IemopBillingService {
    private http = inject(HttpClient);

    list(page = 0, size = 20, q = ''): Observable<any> {
        let params = new HttpParams().set('page', page).set('size', size);
        if (q) params = params.set('q', q);
        return this.http.get(`${BASE_URL}/iemop-billing/list`, { params });
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_URL}/iemop-billing/${id}`);
    }

    upload(file: File, date: string, referenceNumber: string): Observable<any> {
        const fd = new FormData();
        fd.append('file', file);
        fd.append('date', date);
        fd.append('referenceNumber', referenceNumber);
        return this.http.post(`${BASE_URL}/iemop-billing/upload`, fd);
    }
}
