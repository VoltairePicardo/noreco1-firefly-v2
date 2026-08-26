import {User} from '@/app/models/user.model';
import {InventoryCategory, InventoryLocation, Item, UnitMeasure} from '@/app/models/inventory-modules/item-stock.model';
import {Purpose} from '@/app/models/dropdown.model';

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

export interface Department {
    id: number;
    abbreviation?: string;
    name?: string;
}

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

export interface StockWithdrawal {
    id: number;
    code?: string;
    description?: string;
    voucherDate?: string;
    year?: number;
    department?: Department;
    inventoryLocation?: InventoryLocation;
    inventoryCategory?: InventoryCategory;
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

    transaction?: Transaction;
    workflow?: Workflow;
    createdAt?: string;
    updatedAt?: string;
    createdBy?: User;
    documentStatus?: DocumentStatus;
    postedBy?: User;
    office?: Office;
}

export interface StockWithdrawalPage<T> {
    content: T[];
    totalElements?: number;
    totalPages?: number;
    number?: number;
    size?: number;
    page?: {
        size?: number;
        number?: number;
        totalElements?: number;
        totalPages?: number;
    };
}
