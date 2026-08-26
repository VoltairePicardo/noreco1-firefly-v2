import {
    Transaction,
    Workflow,
    DocumentStatus,
    Office,
    InventoryLocation,
    ItemTransactionDetailDto
} from './stock-release.model';
import {User} from '@/app/models/user.model';

export interface Department {
    id: number;
    name?: string;
    abbreviation?: string;
}

export interface MaterialSalvageTicket {
    id: number;
    code?: string;
    transaction?: Transaction;
    transId?: number;
    workflow?: Workflow;
    createdAt?: string;
    updatedAt?: string;
    createdBy?: User;
    documentStatus?: DocumentStatus;
    postedBy?: User;
    office?: Office;

    voucherDate?: string;
    purpose?: string;
    year?: number;
    department?: Department;
    inventoryLocation?: InventoryLocation;
    returnedBy?: User;
    receivedBy?: User;
    details?: ItemTransactionDetailDto[];
}
