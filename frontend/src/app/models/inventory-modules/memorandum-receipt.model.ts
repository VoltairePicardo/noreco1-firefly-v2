import { User } from '@/app/models/user.model';
import { Office, Transaction, Workflow, DocumentStatus } from '@/app/models/shared/reference.model';
import { SlEntity } from '@/app/models/shared/party.model';
import { StockWithdrawal, StockWithdrawalDetail } from '@/app/models/inventory-modules/stock-withdrawal.model';

export type { SlEntity };

export interface ReturnMemorandumReceipt {
    id: number;
    code?: string;
}

export interface ReturnMemorandumReceiptListRow {
    id: number;
    code?: string;
    date?: string;
    office?: Office;
    employee?: SlEntity;
    status?: string;
}

/** One row of `returnMemorandumReceiptDetails` — mirrors `model/ReturnMemorandumReceiptDetail.java`. */
export interface ReturnMemorandumReceiptDetail {
    id?: number;
    stockWithdrawalDetail?: StockWithdrawalDetail;
    quantity?: number;
    returnedQuantity?: number;
    returnedToInventoryQuantity?: number;
    usable?: boolean;
    reassignedQuantity?: number;
}

/** Mirrors `controller/response/ReturnMemorandumReceiptDto.java` — the shape returned by `GET /return-memorandum-receipt/{id}`. */
export interface ReturnMemorandumReceiptDto {
    id: number;
    code?: string;
    date?: string;
    employee?: SlEntity;
    office?: Office;
    stockWithdrawal?: StockWithdrawalDto;
    user?: User;
    returnMemorandumReceiptDetails?: ReturnMemorandumReceiptDetail[];
    transaction?: Transaction;
    documentStatus?: DocumentStatus;
    workflow?: Workflow;
    memorandumReceipt?: MemorandumReceipt;
    remarks?: string;
}

export interface StockWithdrawalDto {
    id: number;
    code?: string;
    description?: string;
    voucherDate?: string;
    year?: number;
    type?: number;
    details?: StockWithdrawalDetail[];
}

export interface MemorandumReceiptDetail {
    id?: number;
    stockWithdrawalDetail?: StockWithdrawalDetail;
    quantity?: number;
    reassignedQuantity?: number;
    returned?: number;
}

export interface MemorandumReceipt {
    id: number;
    code?: string;
    date?: string;
    employee?: SlEntity;
    office?: Office;
    stockWithdrawal?: StockWithdrawalDto;
    returnMemorandumReceipt?: ReturnMemorandumReceipt;
    user?: User;
    approvingOfficer?: User;
    memorandumReceiptDetails?: MemorandumReceiptDetail[];
    hasMst?: boolean;
    transaction?: Transaction;
    documentStatus?: DocumentStatus;
    workflow?: Workflow;
}

export type MemorandumReceiptSource = StockWithdrawal | StockWithdrawalDto;

export interface AssignedItemRow {
    stockWithdrawalDetail?: StockWithdrawalDetail;
    quantity: number;
    reassignedQuantity: number;
    oldQuantity: number;
    itemCode: string;
    itemDescription: string;
    unitCode: string;
}

export interface AvailableItemRow {
    stockWithdrawalDetail?: StockWithdrawalDetail;
    itemCode: string;
    itemDescription: string;
    unitCode: string;
    quantity: number;
    quantityReleased?: number;
    totalAssigned: number;
    remaining: number;
    oldRemainingBalance: number;
    oldTotalAssigned: number;
    insufficientBalance: boolean;
    assigned: boolean;
}

export interface MemorandumReceiptListRow {
    id: number;
    code?: string;
    date?: string;
    status?: string;
    employeeName?: string;
    officeName?: string;
    returnedMR?: string;
    preparedBy?: string;
}
