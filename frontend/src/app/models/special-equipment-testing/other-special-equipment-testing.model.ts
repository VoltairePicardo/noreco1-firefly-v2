export interface OtherSpecialEquipmentTestingBrand {
    id: number;
    name: string | null;
}

export interface OtherSpecialEquipmentTestingType {
    id: number;
    description: string | null;
}

export interface OtherSpecialEquipmentTestingSpecialEquipment {
    id: number;
    serialNo: string | null;
    owner: string | null;
    address: string | null;
    specialEquipmentType: OtherSpecialEquipmentTestingType | null;
    brand: OtherSpecialEquipmentTestingBrand | null;
}

export interface OtherSpecialEquipmentTestingUserSummary {
    accountNo: number | null;
    fullName: string | null;
}

export interface OtherSpecialEquipmentTestingData {
    id: number;
    date: string | null;
    specialEquipment: OtherSpecialEquipmentTestingSpecialEquipment | null;
    passed: boolean;
    remarks: string | null;
    verifiedBy: OtherSpecialEquipmentTestingUserSummary | null;
    checkedBy: OtherSpecialEquipmentTestingUserSummary | null;
    approvedBy: OtherSpecialEquipmentTestingUserSummary | null;
    createdBy: OtherSpecialEquipmentTestingUserSummary | null;
    createdAt: string | null;
    updatedAt: string | null;
}
