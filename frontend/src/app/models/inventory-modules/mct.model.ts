import { User } from '@/app/models/user.model';
import { InventoryLocation } from '@/app/models/shared/reference.model';
import { DocumentBase } from '@/app/models/shared/document.model';
import { StockRelease, ItemTransactionDetailDto } from '@/app/models/inventory-modules/stock-release.model';

export interface MaterialCreditTicket extends DocumentBase {
    approvingOfficer?: User;

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
