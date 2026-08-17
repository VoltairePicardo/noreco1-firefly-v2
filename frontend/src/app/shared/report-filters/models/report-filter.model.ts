export interface ReportFilterValue {
    areaId?: number;
    townId?: number;
    barangayId?: number;
    precinctId?: number;
    statusId?: number;
    cutOffDate?: string | Date;
    complaintTypeId?: number;
    routeId?: number | number[];
    bookId?: number;
    feederId?: number;
    consumerClassId?: number;
    meterStatusId?: number;
    billingMonthId?: number;
    year?: number;
    optionId?: number;
    seniorCitizenType?: any;
    balanceTypeId?: any;
    itemId?: any;
    documentStatusId?: any;
    paymentDate?: string | Date;
    postingDate?: any;
    natureOfAdjId?: number;
    billOptionId?: number;
    selectedAccountTypes?: number[];
    ageSelected?: any;
    batchId?: number;
    accomplishmentStatusId?: number;
    discoStatusId?: number;
    memberStatusId?: number;
    accountId?: number;
    accountTypeId?: number;
    accountCategoryId?: number;
    accountClassificationId?: number;
    readingRemarkId?: number;
    printOptionId?: number;
}

export interface DateRange {
    startDate: string | Date;
    endDate: string | Date;
}

export interface YearMonth {
    year: number;
    month: number;
}

export class FilterValueHolder {
    areaId: number = 0;
    townId: number = 0;
    barangayId: number = 0;
    precinctId: number = 0;
    statusId: number = 0;
    cutOffDate = new Date;
}
