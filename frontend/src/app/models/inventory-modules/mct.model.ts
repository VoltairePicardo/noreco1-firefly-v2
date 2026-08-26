import { User } from '../user.model';
import {
    Transaction,
    Workflow,
    DocumentStatus,
    Office,
    InventoryLocation,
    ItemTransactionDetailDto,
    StockRelease
} from './stock-release.model';

export interface MaterialCreditTicket {
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
    amount?: number;
    stockRelease?: StockRelease;
    checker?: User;
    details?: ItemTransactionDetailDto[];
    requester?: User;
    inventoryLocation?: InventoryLocation;
}
