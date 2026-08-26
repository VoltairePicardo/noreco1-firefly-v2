import { User } from '@/app/models/user.model';
import { Office, Transaction, Workflow, DocumentStatus } from '@/app/models/shared/reference.model';
import { SlEntity } from '@/app/models/shared/party.model';
import { StockWithdrawal, StockWithdrawalDetail } from '@/app/models/inventory-modules/stock-withdrawal.model';

export type { SlEntity };

export interface ReturnMemorandumReceipt {
    id: number;
    code?: string;
}

/** Mirrors `controller/response/StockWithdrawalDto.java` — the shape `MemorandumReceiptDto.stockWithdrawal` is returned as. */
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

/** Mirrors `controller/response/MemorandumReceiptDto.java` — the shape returned by `GET /memorandum-receipt/{id}`. */
export interface MemorandumReceiptDto {
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

/** Voucher picked from the Browse Stock Withdrawal modal — a full `StockWithdrawal` (its `items`/`details` populate the assignable rows). */
export type MemorandumReceiptSource = StockWithdrawal | StockWithdrawalDto;

/** One item already assigned to an employee — shared row shape between the single- and multiple-employee MR create/edit screens. */
export interface AssignedItemRow {
    stockWithdrawalDetail?: StockWithdrawalDetail;
    quantity: number;
    reassignedQuantity: number;
    oldQuantity: number;
    itemCode: string;
    itemDescription: string;
    unitCode: string;
}

/** One item from the selected Stock Withdrawal, with its remaining/assigned balances — shared row shape between the single- and multiple-employee MR create/edit screens. */
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

/** One row of `GET /memorandum-receipt/list-paged` — a flat native-query projection, mirrors the column aliases in `MemorandumReceiptRepo.getMemorandumReceiptPaged`. */
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
