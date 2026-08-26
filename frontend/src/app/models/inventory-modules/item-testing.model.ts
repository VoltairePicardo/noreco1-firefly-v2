import { InventoryLocation } from '@/app/models/inventory-modules/item-stock.model';
import { User } from '@/app/models/user.model';

export interface Supplier {
    id?: number;
    accountNumber?: number;
    name?: string;
    address?: string;
}

export interface PurchaseOrderVendor {
    accountNo?: number;
    name?: string;
}

export interface PurchaseOrderSummary {
    id: number;
    code?: string;
    localCode?: string;
    supplier?: string;
    vendor?: PurchaseOrderVendor;
}

export interface PoDetailForTesting {
    id: number;
    itemId?: number;
    itemCode?: string;
    unitCode?: string;
    itemDescription?: string;
    quantity?: number;
    sentForTestingQuantity?: number;
    deliveredQuantity?: number;
}

export interface ItemTestingDetailRow {
    id?: number;
    item?: { id: number };
    poDetail?: { id: number };
    itemCode?: string;
    unitCode: string;
    itemDescription: string;
    quantity: number;
    deliveredQuantity: number;
    quantityReceived: number;
    unitsReceivedQuantity: number;
    unitsRejectedQuantity?: number;
    remarks: string;
    balance?: number;
}

export interface ItemTestingDto {
    id: number;
    date?: string;
    inventoryLocation?: InventoryLocation;
    createdBy?: User;
    itemTestingDetails?: ItemTestingDetailRow[];
    createdAt?: string;
    updatedAt?: string;
    supplier?: Supplier;
    purchaseOrder?: PurchaseOrderSummary;
}
