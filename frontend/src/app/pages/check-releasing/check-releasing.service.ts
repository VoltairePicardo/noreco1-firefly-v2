import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class CheckReleasingService {
    private http = inject(HttpClient);

    getUnreleased(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/check-releasing/unreleased`);
    }

    getReleased(from?: string, to?: string): Observable<any[]> {
        let params = new HttpParams();
        if (from) params = params.set('from', from);
        if (to)   params = params.set('to', to);
        return this.http.get<any[]>(`${BASE_API}/check-releasing/released`, { params });
    }

    getData(id: number | string): Observable<any> {
        return this.http.get(`${BASE_API}/check-releasing/${id}`);
    }

    release(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/check-releasing/release-json`, form, httpOptions);
    }

    releaseWithFiles(formData: FormData): Observable<any> {
        // No Content-Type header — browser sets multipart/form-data with boundary automatically
        return this.http.post(`${BASE_API}/check-releasing/release`, formData);
    }

    getFiles(id: number | string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/check-releasing/${id}/files`);
    }

    downloadFileUrl(fileId: number): string {
        return `${BASE_API}/check-releasing/file/${fileId}`;
    }

    cancel(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/check-releasing/cancel`, form, httpOptions);
    }

    getLogs(transId: number | string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/document-logs/${transId}`);
    }
}
