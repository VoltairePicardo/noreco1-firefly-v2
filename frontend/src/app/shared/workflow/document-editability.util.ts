export const DOCUMENT_STATUS = {
    CREATED: 'Document Created',
    FOR_REVISION: 'For Revision',
    RETURNED_TO_CREATOR: 'Returned to Creator',
} as const;

export const EDITABLE_DOCUMENT_STATUSES: readonly string[] = [
    DOCUMENT_STATUS.CREATED,
    DOCUMENT_STATUS.FOR_REVISION,
    DOCUMENT_STATUS.RETURNED_TO_CREATOR,
];

export function getDocumentStatusName(doc: any): string {
    const ds = doc?.documentStatus;
    const raw = (typeof ds === 'string' ? ds : ds?.status) ?? doc?.status ?? '';
    return String(raw).trim();
}


export function isDocumentEditable(doc: any, extraStatuses: readonly string[] = []): boolean {
    const status = getDocumentStatusName(doc);
    return EDITABLE_DOCUMENT_STATUSES.includes(status) || extraStatuses.includes(status);
}
