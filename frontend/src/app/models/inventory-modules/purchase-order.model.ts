import { User } from '@/app/models/user.model';
import { Department } from '@/app/models/shared/reference.model';
import { DocumentBase } from '@/app/models/shared/document.model';
import { SpringPage } from '@/app/models/shared/page.model';

export interface Supplier {
    id?: number;
    accountNumber?: number;
    name?: string;
    address?: string;
}

export interface PurchaseOrderVendorEntity {
    accountNo: number;
    name?: string;
    address?: string;
    slEntityClassification?: string;
    marker?: number;
    vatable?: boolean;
}

export type PurchaseOrderVendor = Pick<PurchaseOrderVendorEntity, 'accountNo' | 'name'>;

export interface PurchaseOrderSummary {
    id: number;
    code?: string;
    localCode?: string;
    supplier?: string;
    vendor?: PurchaseOrderVendor;
}

export interface VehicleRef {
    id: number;
    name?: string;
}

export interface CashAdvanceRef {
    id: number;
    code?: string;
}

export interface BudgetLineItemDetailRef {
    id: number;
}

export interface PurchaseOrderBudgetDetail {
    id?: number;
}

export interface PurchaseOrderDetail {
    id?: number;
    purchaseOrderId?: number;
    itemId?: number;
    rvDetailId?: number;
    itemCode?: string;
    unitCode?: string;
    itemDescription?: string;
    quantity?: number;
    vat?: number;
    discount?: number;
    unitPrice?: number;
    itemAmount?: number;
    rvdQuantity?: number;
    remainingQuantity?: number;
    poQuantity?: number;
    requisitionVoucherCode?: string;
    sentForTestingQuantity?: number;
    deliveredQuantity?: number;
}

export interface PurchaseOrder extends DocumentBase {
    approvingOfficer?: User;

    voucherDate?: string;
    year?: number;
    term?: number;
    vendor?: PurchaseOrderVendorEntity;
    amount?: number;
    checkedBy?: User;
    budgetCheckedBy?: User;
    poDetails?: PurchaseOrderDetail[];
    deliveryTerm?: string;
    deliveryAddress?: string;
    paymentTerm?: number;
    purpose?: string;
    vehicle?: VehicleRef;
    cashAdvance?: CashAdvanceRef;
    useCreditCard?: boolean;
    department?: Department;
    budgetLineItemDetail?: BudgetLineItemDetailRef;
    budgetLineItemBalancePOJORFP?: number;
    budgetLineItemBalanceCV?: number;
    cashFlowItemBalancePOJORFP?: number;
    cashFlowItemBalanceCV?: number;
    cashFlowItemTotal?: number;
    budgetDetails?: PurchaseOrderBudgetDetail[];
    deliveryTimeAndCompletion?: string;
    receivedDate?: string;
    receivedBy?: string;
    expectedDeliveryDate?: string;
}

export type PurchaseOrderForItemTestingPage = SpringPage<PurchaseOrder>;
