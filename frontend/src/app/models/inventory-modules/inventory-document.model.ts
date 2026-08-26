import { User } from '@/app/models/user.model';
import { ItemLineRef } from '@/app/models/shared/document.model';

export type ReleasingDocumentType = 'ST' | 'SW' | 'MR';

export interface InventoryDocumentDetail extends ItemLineRef {
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
