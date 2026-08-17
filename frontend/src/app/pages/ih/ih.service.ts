import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');

@Injectable({ providedIn: 'root' })
export class IhService {
    private http = inject(HttpClient);

    searchBySerial(serialNo: string): Observable<any[]> {
        const params = new HttpParams().set('serialNo', serialNo);
        return this.http.get<any[]>(`${BASE_API}/ih/history`, { params });
    }

    getItemBySerial(serialNo: string): Observable<any> {
        const params = new HttpParams().set('serialNo', serialNo);
        return this.http.get<any>(`${BASE_API}/ih/item`, { params });
    }
}
