import {User} from '@/app/models/user.model';

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

export interface InventoryDocumentDto {
    transId: number;
    date?: string;
    code?: string;
    purpose?: string;
    createdBy?: string;
    createdByUser?: User;
    departmentName?: string;
    inventoryCategoryTypeId?: number;
    details?: InventoryDocumentDetail[];
    withdrawalDetails?: InventoryDocumentDetail[];
}
