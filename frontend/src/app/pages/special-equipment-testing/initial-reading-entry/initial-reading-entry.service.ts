import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class InitialReadingEntryService {
    private http = inject(HttpClient);

    list(q = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/initial-reading/list`, { params });
    }

    getById(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/initial-reading/${id}`);
    }

    getMeterBySerialNo(serialNo: string): Observable<any> {
        return this.http.get(`${BASE_API}/initial-reading/meter/${encodeURIComponent(serialNo)}`);
    }

    saveReading(meter: any): Observable<any> {
        return this.http.post(`${BASE_API}/initial-reading/save`, meter, httpOptions);
    }
}
