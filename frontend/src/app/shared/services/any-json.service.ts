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
    transaction: { id: number };
    oldValue: string | null;
    newValue: string | null;
    loggedBy: DocumentLogUser;
    createdAt: string;
}


@Injectable({ providedIn: 'root' })
export class AnyJSONService {
    private http = inject(HttpClient);

    getLogs(transactionId: number): Observable<DocumentLog[]> {
        return this.http.get<DocumentLog[]>(`${BASE_API}/json/v2/document-logs/${transactionId}`);
    }

    getWorkflowActions(transactionId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/workflow-actions/${transactionId}`);
    }
}
