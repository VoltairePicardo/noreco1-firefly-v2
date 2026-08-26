import { User } from '@/app/models/user.model';
import { InventoryLocation, Department } from '@/app/models/shared/reference.model';
import { DocumentBase } from '@/app/models/shared/document.model';
import { ItemTransactionDetailDto } from '@/app/models/inventory-modules/stock-release.model';

export interface MaterialSalvageTicket extends DocumentBase {
    transId?: number;

    voucherDate?: string;
    purpose?: string;
    year?: number;
    department?: Department;
    inventoryLocation?: InventoryLocation;
    returnedBy?: User;
    receivedBy?: User;
    details?: ItemTransactionDetailDto[];
}
