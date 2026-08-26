import { InventoryLocationEntity } from '@/app/models/inventory-modules/item-stock.model';
import { User } from '@/app/models/user.model';
import { PurchaseOrderVendorEntity } from '@/app/models/inventory-modules/purchase-order.model';
import { DocumentBase } from '@/app/models/shared/document.model';
import { DocumentStatus } from '@/app/models/shared/reference.model';

export interface Supplier {
    id?: number;
    accountNumber?: number;
    name?: string;
    address?: string;
}

export type PurchaseOrderVendor = Pick<PurchaseOrderVendorEntity, 'accountNo' | 'name'>;

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
    item?: { id: number; code?: string; description?: string };
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

/** One row of `GET /item-testing/list-paged` — mirrors the map keys built in `ItemTestingServiceImpl.getItemTestingPaged`. */
export interface ItemTestingListRow {
    id: number;
    date?: string;
    inventoryLocation?: InventoryLocationEntity;
    purchaseOrder?: PurchaseOrderSummary;
    totalItems?: number;
    createdBy?: User;
    supplier?: Supplier;
    documentStatus?: DocumentStatus;
}

export interface ItemTestingDto extends DocumentBase {
    date?: string;
    transId?: number;
    totalItems?: number;
    preparedBy?: string;
    inventoryLocation?: InventoryLocationEntity;
    itemTestingDetails?: ItemTestingDetailRow[];
    supplier?: Supplier;
    purchaseOrder?: PurchaseOrderSummary;
}
