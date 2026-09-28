export interface DocumentStatusOption {
    id: number;
    status: string;
}

export interface WorkflowActionOption {
    actionMapId: number;
    action: string;
    actionId: number;
    sequence: number;
}

export interface ProcessDocumentPayload {
    documentId: number;
    remarks: string;
    workflowActionsDto: { actionMapId: number };
}

export function buildProcessPayload(documentId: number, actionMapId: number, remarks: string): ProcessDocumentPayload {
    return { documentId, remarks, workflowActionsDto: { actionMapId } };
}
