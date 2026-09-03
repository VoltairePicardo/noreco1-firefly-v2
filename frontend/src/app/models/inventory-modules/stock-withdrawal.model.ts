import { Department } from '@/app/models/shared/reference.model';
import { DocumentBase } from '@/app/models/shared/document.model';
import { InventoryCategoryEntity, InventoryLocationEntity, Item, UnitMeasure } from '@/app/models/inventory-modules/item-stock.model';
import { Purpose } from '@/app/models/dropdown.model';

export interface WorkOrder {
    id: number;
    code?: string;
    description?: string;
}

export interface CostEstimate {
    id: number;
    code?: string;
}

export interface PurchaseRequest {
    id: number;
    code?: string;
}

export interface StockWithdrawalDetail {
    id: number;
    item?: Item;
    unit?: UnitMeasure;
    quantity?: number;
    quantityReleased?: number;
    isSpecialEquipment?: boolean;
}

export interface StockWithdrawalDetailDto {
    itemId?: number;
    itemCode?: string;
    unitId?: number;
    unitCode?: string;
    itemDescription?: string;
    quantity?: number;
    quantityReleased?: number;
    isSpecialEquipment?: boolean;
    itemStockId?: number;
    inventoryBalance?: number;
    rvBalance?: number;
    insertedQuantity?: number;
}

export interface TurnOnOrderWithdrawalDto {
    id?: number;
    date?: string;
    totalTurnOnOrders?: number;
}

export interface EmployeeDto {
    name?: string;
    accountNo?: number;
}

export interface StockWithdrawal extends DocumentBase {
    description?: string;
    voucherDate?: string;
    year?: number;
    department?: Department;
    inventoryLocation?: InventoryLocationEntity;
    inventoryCategory?: InventoryCategoryEntity;
    workOrder?: WorkOrder;
    costEstimate?: CostEstimate;
    details?: StockWithdrawalDetailDto[];
    items?: StockWithdrawalDetail[];
    type?: number;
    turnOnOrderWithdrawalId?: number;
    purchaseRequest?: PurchaseRequest;
    purpose?: Purpose;
    turnOnOrderWithdrawal?: TurnOnOrderWithdrawalDto;
    employees?: EmployeeDto[];
}
