import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DocumentStatusOption, WorkflowActionOption } from '@/app/models/shared/workflow.model';

const BASE_API = environment.get('baseApiUrl');

/**
 * Read side of the shared approval-workflow engine (backend/service/WorkflowService).
 * Each document module's own service still owns its `/process` write endpoint —
 * this only fetches the status list (for the list filter) and the actions
 * available from a document's current status (for the detail page).
 */
@Injectable({ providedIn: 'root' })
export class WorkflowService {
    private http = inject(HttpClient);

    getDocumentStatuses(workflowId: number): Observable<DocumentStatusOption[]> {
        return this.http.get<DocumentStatusOption[]>(`${BASE_API}/workflow/${workflowId}/document-statuses`);
    }

    getAvailableActions(workflowId: number, documentStatusId: number): Observable<WorkflowActionOption[]> {
        return this.http.get<WorkflowActionOption[]>(`${BASE_API}/workflow/${workflowId}/actions/${documentStatusId}`);
    }

    /**
     * Backend counterpart: AnyJsonController#getWorkflowActions (GET /api/json/workflow-actions/{transId}).
     * Unlike getAvailableActions above (which targets an endpoint that isn't implemented by
     * WorkflowController), this resolves the actions available to the *current user* for a
     * document's transaction by walking its workflow log -- the actual mechanism the backend
     * implements for every document module.
     */
    getAvailableActionsForTransaction(transactionId: number): Observable<WorkflowActionOption[]> {
        return this.http.get<WorkflowActionOption[]>(`${BASE_API}/json/workflow-actions/${transactionId}`);
    }
}
