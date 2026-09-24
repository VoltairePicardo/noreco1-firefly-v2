export interface MeterTestingRecord {
    no: number | null;
    actualDateOfTesting: string | null;
    meterSerialNo: string | null;
    sealNo: string | null;
    error: number | null;
    sta: boolean | null;
    crp: boolean | null;
    voltageTest: boolean | null;
    result: boolean | null;
}

export interface MeterTestingResult {
    reportTitle: string | null;
    specifications: string | null;
    tester: string | null;
    dateTested: string | null;
    temperature: string | null;
    relativeHumidity: string | null;
    totalCount: number;
    passedCount: number;
    failedCount: number;
    records: MeterTestingRecord[];
}

export interface MeterTestingOptionType {
    id: number;
    description: string | null;
}

export interface MeterTestingOption {
    id: number;
    optionType: MeterTestingOptionType | null;
    description: string | null;
    active: boolean | null;
    order: number | null;
}

export interface MeterTestingOptionGroup {
    typeId: number;
    typeDescription: string;
    columns: MeterTestingOption[][];
    othersOption: MeterTestingOption | null;
}

export interface MeterTestingDetailRow {
    id: number;
    meterSerialNo: string | null;
    error: number | null;
    sta: boolean | null;
    crp: boolean | null;
    voltageTest: boolean | null;
    result: boolean | null;
}

export interface MeterTestingOptionDetailRow {
    id: number;
    meterTestingOption: MeterTestingOption | null;
    otherRemarks: string | null;
}

export interface MeterTestingOptionDetailGroup {
    typeId: number;
    typeDescription: string;
    options: MeterTestingOptionDetailRow[];
}

export interface MeterTestingData {
    id: number;
    date: string | null;
    meterModel: { modelName: string | null } | null;
    testedBy: string | null;
    multiplier: number | null;
    temperature: string | null;
    relativeHumidity: string | null;
    createdBy: { fullName: string | null } | null;
    createdAt: string | null;
    details: MeterTestingDetailRow[];
    optionDetails: MeterTestingOptionDetailRow[];
}
