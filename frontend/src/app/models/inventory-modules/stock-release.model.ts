export interface User {
    id: number;
    fullName?: string;
    username?: string;
    accountNo?: number;
}

export interface Transaction {
    id: number;
    createdBy?: User;
    createdAt?: string;
}

export interface Workflow {
    id: number;
    name?: string;
    enabled?: boolean;
}

export interface DocumentStatus {
    id: number;
    status?: string;
}

export interface Office {
    id: number;
    name?: string;
    acronym?: string;
}

export interface InventoryLocation {
    id: number;
    description?: string;
    name?: string;
}

export interface SpecialEquipment {
    id: number;
    serialNo?: string;
}

export interface ItemTransactionDetailDto {
    id?: number;
    itemId?: number;
    itemCode?: string;
    unitId?: number;
    unitCode?: string;
    itemDescription?: string;
    searchText?: string;
    quantity?: number;
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

export interface StockRelease {
    id: number;
    code?: string;
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

    transaction?: Transaction;
    workflow?: Workflow;
    createdAt?: string;
    updatedAt?: string;
    createdBy?: User;
    documentStatus?: DocumentStatus;
    postedBy?: User;
    office?: Office;
}
