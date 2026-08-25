import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');

export interface DocumentLogUser {
    fullName?: string;
    username?: string;
}

export interface DocumentLog {
    id: number;
    createdAt: string;
    createdBy: DocumentLogUser;
    action: string;
    remarks: string;
}


@Injectable({ providedIn: 'root' })
export class AnyJSONService {
    private http = inject(HttpClient);

    getLogs(transactionId: number): Observable<DocumentLog[]> {
        return this.http.get<DocumentLog[]>(`${BASE_API}/json/document-logs/${transactionId}`);
    }

    getWorkflowActions(transactionId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/workflow-actions/${transactionId}`);
    }
}
