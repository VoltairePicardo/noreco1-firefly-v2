import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class CaLiquidationService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/cash-advance-liquidation/list`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/cash-advance-liquidation/${id}`);
    }

    create(form: any, files: File[] = []): Observable<any> {
        const fd = new FormData();
        fd.append('model', new Blob([JSON.stringify(form)], { type: 'application/json' }));
        files.forEach((f, i) => fd.append(`file_${i}`, f, f.name));
        return this.http.post(`${BASE_API}/cash-advance-liquidation/create`, fd);
    }

    update(form: any, files: File[] = [], filesToRemove: any[] = []): Observable<any> {
        const fd = new FormData();
        fd.append('model', new Blob([JSON.stringify(form)], { type: 'application/json' }));
        fd.append('filesToRemove', new Blob([JSON.stringify(filesToRemove)], { type: 'application/json' }));
        files.forEach((f, i) => fd.append(`file_${i}`, f, f.name));
        return this.http.post(`${BASE_API}/cash-advance-liquidation/update`, fd);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/cash-advance-liquidation/process`, payload, httpOptions);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/workflow-actions/${transId}`);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/cash-advance-liquidation/document-statuses`);
    }

    getDefaultSignatories(): Observable<any> {
        return this.http.get(`${BASE_API}/cash-advance-liquidation/default-signatories`);
    }

    getOffices(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/offices`);
    }

    getUserOffice(): Observable<any> {
        return this.http.get(`${BASE_API}/json/office-user`);
    }

    getCashAdvanceList(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/cash-advance/list`);
    }

    getParticularsForLiquidation(caId: number, calId = 0): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/cap/details-for-liquidation/${caId}/${calId}`);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/document-logs/${transId}`);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/cash-advance-liquidation/export/${id}`, { type: 'pdf' });
    }
}
