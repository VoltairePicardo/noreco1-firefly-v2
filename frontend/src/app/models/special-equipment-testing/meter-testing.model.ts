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
