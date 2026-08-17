import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class MaintenanceRecordService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    list(q: string = '', from: string = '', to: string = '', page = 0, size = 10): Observable<any> {
        let params = new HttpParams()
            .set('page', page)
            .set('size', size);
        if (q)    params = params.set('q', q);
        if (from) params = params.set('s', from);
        if (to)   params = params.set('e', to);
        return this.http.get<any>(`${BASE_API}/maintenance-record/list`, { params });
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/maintenance-record/${id}`);
    }

    getStockReleasesForMaintenance(q: string = '', page = 0, size = 10): Observable<any> {
        let params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/stock-release/for-maintenance-record`, { params });
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/maintenance-record/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/maintenance-record/update`, form, httpOptions);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/maintenance-record/export/${id}`, { type: 'pdf' });
    }
}
