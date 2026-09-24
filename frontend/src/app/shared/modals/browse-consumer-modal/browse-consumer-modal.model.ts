export interface ConsumerSummary {
    id: number;
    accountNo: number | null;
    accountName: string | null;
    oldAccountNo: string | null;
    address: string | null;
}

export interface MeterSummary {
    id: number;
    serialNo: string | null;
    presentReading: number | null;
}

export interface ConsumerMeterRow {
    id: number;
    consumer: ConsumerSummary | null;
    meter: MeterSummary | null;
}

export interface MeterModel {
    id: number;
    
}

export interface ConsumerMeterSelection {
    consumerId: number | null;
    accountNo: number | null;
    accountName: string | null;
    oldAccountNo: string | null;
    address: string | null;
    meterId: number | null;
    meterSerialNo: string | null;
    presentReading: number | null;
}

export interface ConsumerMeterPage {
    content?: ConsumerMeterRow[];
    totalElements?: number;
    page?: { totalElements?: number };
}
