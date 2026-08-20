/** Document type codes accepted by `GET /api/stock-release/documents?t=`. */
export type ReleasingDocumentType = 'ST' | 'SW' | 'MR';

export interface InventoryDocumentDetail {
    itemId?: number;
    itemCode?: string;
    unitId?: number;
    unitCode?: string;
    itemDescription?: string;
    quantity?: number;
    quantityReleased?: number;
    inventoryCategoryId?: number;
}

export interface CreatedByRef {
    id?: number;
    fullName?: string;
    accountNo?: string;
}

export interface InventoryDocumentDto {
    transId: number;
    date?: string;
    code?: string;
    purpose?: string;
    createdBy?: string;
    createdByUser?: CreatedByRef;
    departmentName?: string;
    inventoryCategoryTypeId?: number;
    details?: InventoryDocumentDetail[];
    withdrawalDetails?: InventoryDocumentDetail[];
}
