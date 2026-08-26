export interface AccountRef {
    id: number;
    code?: string;
    title?: string;
}

export interface UnitMeasure {
    id: number;
    code: string;
    description: string;
    plural?: string;
}

export interface InventoryCategoryEntity {
    id: number;
    description?: string;
    type?: number;
}

export interface InventoryLocationEntity {
    id: number;
    description: string;
    account?: AccountRef;
    isSubsidy?: boolean;
    createdAt?: string;
    updatedAt?: string;
    createdBy?: { id: number };
}

export interface Item {
    id: number;
    code?: string;
    description: string;
    matId?: number;
    matCode?: string;
    matDescription?: string;
    matUnit?: string;
    matACode?: string;
    unit?: UnitMeasure;
    reorderPoint?: number;
    idealQty?: number;
    location?: string;
    isActive?: boolean;
    assetAccount?: AccountRef;
    expenseAccount?: AccountRef;
    inventoryCategory?: InventoryCategoryEntity;
    fileName?: string;
    hasSerialNumbers?: boolean;
    barcode?: string;
    base64Image?: string;
}

export interface ItemStock {
    id: number;
    item?: Item;
    inventoryLocation?: InventoryLocationEntity;
    quantity?: number;
    unitCost?: number;
    totalQuantity?: number;
    totalItemCost?: number;
    createdAt?: string;
    updatedAt?: string;
    base64Image?: string;
    balance?: number;
}

export interface ItemStockDto {
    id: number;
    item?: Item;
    inventoryLocation?: InventoryLocationEntity;
    totalQuantity?: number;
    totalItemCost?: number;
    createdAt?: string;
    updatedAt?: string;
}
