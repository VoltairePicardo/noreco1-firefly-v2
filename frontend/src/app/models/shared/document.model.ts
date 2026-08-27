import { User } from '@/app/models/user.model';
import { Transaction, Workflow, DocumentStatus, Office } from '@/app/models/shared/reference.model';

export interface DocumentBase {
    id: number;
    code?: string;
    transaction?: Transaction;
    workflow?: Workflow;
    createdAt?: string;
    updatedAt?: string;
    createdBy?: User;
    documentStatus?: DocumentStatus;
    postedBy?: User;
    office?: Office;
}

export interface DocumentLog {
    id: number;
    createdAt?: string;
    action?: string;
    createdBy?: User;
    remarks?: string;
}

export interface ItemLineRef {
    itemId?: number;
    itemCode?: string;
    unitId?: number;
    unitCode?: string;
    itemDescription?: string;
    quantity?: number;
}
