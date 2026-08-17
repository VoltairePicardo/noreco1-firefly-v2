import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class JoAcceptanceService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    listByDateRange(from: string, to: string, statusId?: number | null): Observable<any[]> {
        const url = statusId
            ? `${BASE_API}/jo-acceptance/list/${from}/${to}/${statusId}`
            : `${BASE_API}/jo-acceptance/list/${from}/${to}`;
        return this.http.get<any[]>(url);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/jo-acceptance/document-statuses`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/jo-acceptance/${id}`);
    }

    getDetails(id: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/jo-acceptance/detail/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/jo-acceptance/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/jo-acceptance/update`, form, httpOptions);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/jo-acceptance/process`, payload, httpOptions);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/workflow-actions/${transId}`);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.post<any[]>(`${BASE_API}/document/${transId}/logs`, {}, httpOptions);
    }

    getEntities(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/entities`);
    }

    getFiles(id: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/jo-acceptance/${id}/files`);
    }

    uploadFiles(id: number, formData: FormData): Observable<any> {
        return this.http.post(`${BASE_API}/jo-acceptance/${id}/upload`, formData);
    }

    deleteFile(fileId: number): Observable<any> {
        return this.http.delete(`${BASE_API}/jo-acceptance/file/${fileId}`);
    }

    fileUrl(fileId: number): string {
        return `${BASE_API}/jo-acceptance/file/${fileId}`;
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/jo-acceptance/export/${id}`, { type: 'pdf' });
    }
}
