export interface TransformerTestingBrand {
    id: number;
    name: string | null;
}

export interface TransformerTestingLookup {
    id: number;
    description: string | null;
}

export interface TransformerTestingTransformer {
    id: number;
    serialNo: string | null;
    owner: string | null;
    ownerAddress: string | null;
    brand: TransformerTestingBrand | null;
    kva: number | null;
    primaryVoltage: TransformerTestingLookup | null;
    secondaryVoltage: TransformerTestingLookup | null;
    impedance: number | null;
    polarity: string | null;
    coreType: string | null;
    bushing: string | null;
    type: string | null;
}

export interface TransformerTestingUserSummary {
    accountNo: number | null;
    fullName: string | null;
}

export interface TransformerVoltageRatioTestRow {
    id?: number;
    primaryVoltageInduce: number | null;
    tap1: number | null;
    tap2: number | null;
    tap3: number | null;
    tap4: number | null;
    tap5: number | null;
}

export interface TransformerLossTestRow {
    id?: number;
    shortCircuitPrimaryCurrent: number | null;
    shortCircuitResult: number | null;
    openCircuitSecondaryVoltage: number | null;
    openCircuitResult: number | null;
    totalLoss: number | null;
    iex: number | null;
    iz: number | null;
    ir: number | null;
    ix: number | null;
    eff: number | null;
}

export interface TransformerTestingDocumentStatus {
    id: number;
    status: string | null;
}

export interface TransformerTestingData {
    id: number;
    dateTested: string | null;
    timeTested: string | null;
    transformer: TransformerTestingTransformer | null;
    owner: string | null;
    ownerAddress: string | null;
    transformerCondition: TransformerTestingLookup | null;
    weather: string | null;
    remarks: string | null;
    recommendation: string | null;
    testedBy: TransformerTestingUserSummary | null;
    recommendingApprovalUser: TransformerTestingUserSummary | null;
    approvedBy: TransformerTestingUserSummary | null;
    createdBy: TransformerTestingUserSummary | null;
    createdAt: string | null;
    documentStatus: TransformerTestingDocumentStatus | null;
    transaction: { id: number } | null;
    voltageRatioTests: TransformerVoltageRatioTestRow[];
    lossTests: TransformerLossTestRow[];
}
