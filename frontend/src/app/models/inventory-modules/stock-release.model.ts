import { User } from '@/app/models/user.model';
import { Transaction, InventoryLocation, SpecialEquipment } from '@/app/models/shared/reference.model';
import { DocumentBase, ItemLineRef } from '@/app/models/shared/document.model';

export interface ItemTransactionDetailDto extends ItemLineRef {
    searchText?: string;
    unitCost?: number;
    totalCost?: number;
    quantityReleased?: number;
    quantityReceived?: number;
    releaseQuantity?: number;
    receiveQuantity?: number;
    quantityOrdered?: number;
    inventoryLocationId?: number;
    itemStockId?: number;
    adjustment?: number;
    inventoryCategoryId?: number;
    oldQuantity?: number;
    isRMRTE?: boolean;
    isUsable?: boolean;
    deductFromStock?: boolean;
    newItemId?: number;
    deliveredQuantity?: number;
    newItemDescription?: string;
    serialNumbers?: SpecialEquipment[];
}

export interface StockRelease extends DocumentBase {
    description?: string;
    voucherDate?: string;
    year?: number;
    type?: number;
    documentTransaction?: Transaction;
    inventoryLocation?: InventoryLocation;
    receivedBy?: User;
    auditor?: User;
    details?: ItemTransactionDetailDto[];
    documentType?: number;
}
