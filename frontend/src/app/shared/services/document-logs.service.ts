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

/**
 * Centralized client for the workflow/document audit trail exposed by
 * `AnyJsonController#logs` (`GET /api/json/document-logs/{transId}`).
 * Every module's document-detail page (stock release, vouchers, purchase
 * orders, etc.) should use this instead of redeclaring `getDocumentLogs`
 * on its own feature service.
 */
@Injectable({ providedIn: 'root' })
export class DocumentLogsService {
    private http = inject(HttpClient);

    getLogs(transId: number): Observable<DocumentLog[]> {
        return this.http.get<DocumentLog[]>(`${BASE_API}/json/document-logs/${transId}`);
    }
}
