import { User } from '../user.model';
import {
    Transaction,
    Workflow,
    DocumentStatus,
    Office,
    InventoryLocation,
    ItemTransactionDetailDto
} from './stock-release.model';

export interface StockAdjustment {
    id: number;
    code?: string;
    transaction?: Transaction;
    approvingOfficer?: User;
    workflow?: Workflow;
    createdAt?: string;
    updatedAt?: string;
    createdBy?: User;
    documentStatus?: DocumentStatus;
    postedBy?: User;
    office?: Office;

    remarks?: string;
    voucherDate?: string;
    year?: number;
    inventoryLocation?: InventoryLocation;
    checker?: User;
    details?: ItemTransactionDetailDto[];
}

export interface SignatoryRef {
    accountNo?: number;
    fullName?: string;
    name?: string;
}

export interface StockAdjustmentDefaultSignatories {
    checker?: SignatoryRef;
    approvedBy?: SignatoryRef;
}
