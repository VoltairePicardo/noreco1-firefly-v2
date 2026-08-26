import { User } from '@/app/models/user.model';
import { InventoryLocation } from '@/app/models/shared/reference.model';
import { DocumentBase } from '@/app/models/shared/document.model';
import { SlEntity } from '@/app/models/shared/party.model';
import { ItemTransactionDetailDto } from '@/app/models/inventory-modules/stock-release.model';

export interface StockAdjustment extends DocumentBase {
    approvingOfficer?: User;

    remarks?: string;
    voucherDate?: string;
    year?: number;
    inventoryLocation?: InventoryLocation;
    checker?: User;
    details?: ItemTransactionDetailDto[];
}

export type SignatoryRef = SlEntity;

export interface StockAdjustmentDefaultSignatories {
    checker?: SignatoryRef;
    approvedBy?: SignatoryRef;
}
