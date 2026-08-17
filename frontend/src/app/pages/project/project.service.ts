import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const BASE_URL = environment.get('baseUrl');
const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class ProjectService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    list(q: string = '', from: string = '', to: string = '', statusId: any = null, page = 0, size = 10): Observable<any> {
        let params = new HttpParams().set('page', page).set('size', size);
        if (q)        params = params.set('q', q);
        if (from)     params = params.set('from', from);
        if (to)       params = params.set('to', to);
        if (statusId) params = params.set('statusId', statusId);
        return this.http.get<any>(`${BASE_API}/project/list`, { params });
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/project/${id}`);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/project/document-statuses`);
    }

    getOffices(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/json/offices/`);
    }

    getDepartments(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/json/departments/`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/project/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/project/update`, form, httpOptions);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/project/process`, payload, httpOptions);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/json/workflow-actions/${transId}`);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.post<any[]>(`${BASE_URL}/document/${transId}/logs`, {}, httpOptions);
    }

    uploadFiles(id: number, formData: FormData): Observable<any> {
        return this.http.post(`${BASE_API}/project/${id}/upload`, formData);
    }

    getFiles(id: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/project/${id}/files`);
    }

    fileUrl(fileId: number): string {
        return `${BASE_API}/project/file/${fileId}`;
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_URL}/project/export/${id}`, { type: 'pdf' });
    }
}
