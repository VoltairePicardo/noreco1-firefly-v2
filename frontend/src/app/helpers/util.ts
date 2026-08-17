import Swal from 'sweetalert2';

// Interfaces
export interface ValidityChecker {
    value: any;
    default: any;
    message: string;
}

// Enums
export enum ACCOUNT_CLASSIFICATION {
    BIG_LOADS = 1,
    COOP_EMPLOYEE = 2,
    NON_EMPLOYEE = 3,
    SPECIAL = 4
}

export enum CONSTANT_ITEM {
    VAT_ITEM = 130
}

export enum ASSESSMENT_CHARGE_TYPE {
    APPLICATION = 1,
    CHANGE_OF_NAME = 2,
    APPREHENSION = 3,
    CONSUMER_REQUEST = 4
}

export class HelperService {

    // Constants
    static VAT_RATE = 0.12;

    // User Group Constants
    public static USER_GROUP_ALL = 0;
    public static USER_GROUP_READER = 1;
    public static USER_GROUP_CREW = 2;
    public static USER_GROUP_RECONN_CREW = 3;
    public static USER_GROUP_INTERNAL_AUDIT = 4;
    public static USER_GROUP_TELLER = 5;
    public static USER_GROUP_MANUAL_COLLECTOR = 6;
    public static USER_GROUP_COLLECTING_AGENT = 7;

    // Status Constants
    public static OPEN_STATUS_ID = 17;
    public static CLOSE_STATUS_ID = 18;

    // Approval Constants
    public static SEND_FOR_APPROVAL = {buttonId: 1, statuses:[5, 8]};
    public static RETURN_TO_CREATOR = {buttonId: 2, statuses:[6]};
    public static APPROVE = {buttonId: 3, statuses:[6]};
    public static DISAPPROVE = {buttonId: 4, statuses:[6]};

    // Error Handling
    public static set500Errors(err: any) {
        let errors = [];
        try {
            if (err.error.messages) {
                for (var x = 0; x < err.error.fields.length; x++) {
                    var field = err.error.fields[x];
                    field = field.charAt(0).toUpperCase() + field.slice(1);
                    errors.push(field + " " + err.error.messages[x]);
                }
            }
        } catch (e) {}
        return errors;
    }

    public static set200Errors(err: any) {
        let errors = [];
        try {
            if (err.messages) {
                for (var x = 0; x < err.messages.length; x++) {
                    errors.push(err.messages[x]);
                }
            }
        } catch (e) {}
        return errors;
    }

    // VAT Calculations
    public static extractVATFromInclusive(inclusiveAmount: number, vatRate: number): { baseAmount: number; vatAmount: number } {
        if (inclusiveAmount <= 0 || vatRate <= 0) {
            return { baseAmount: inclusiveAmount, vatAmount: 0 };
        }
        const baseAmount = Math.round((inclusiveAmount / (1 + vatRate)) * 100) / 100;
        const vatAmount = Math.round((inclusiveAmount - baseAmount) * 100) / 100;
        return { baseAmount, vatAmount };
    }

    public static getVATRateFromItem(item130Amount: number): number {
        if (!item130Amount || item130Amount <= 0) {
            return HelperService.VAT_RATE;
        }
        return item130Amount / 100;
    }

    public static calculateVAT(amount: number, isVatable: boolean): number {
        if (!isVatable) return 0;
        return parseFloat((amount * HelperService.VAT_RATE).toFixed(2));
    }

    public static calculateVATInclusive(amount: number, isVatable: boolean): number {
        if (!isVatable) return amount;
        const vat = HelperService.calculateVAT(amount, isVatable);
        return parseFloat((amount + vat).toFixed(2));
    }

    // Calculations
    public static calculateIncreaseDecrease(current?: number | null, avg?: number | null): number {
        const currentValue = Number(current ?? 0);
        const averageValue = Number(avg ?? 0);
        if (isNaN(currentValue) || isNaN(averageValue)) {
            return 0;
        }
        const divisor = averageValue === 0 ? 1 : averageValue;
        const result = Math.round(((currentValue - averageValue) / divisor) * 100);
        return Object.is(result, -0) ? 0 : result;
    }

    public static calculateConsumption(prev: any, pres: any, multiplier: any, isReset: boolean) {
        let consumption = 0;
        let previous = parseFloat(prev);
        let present = parseFloat(pres);
        let totalMultiplier = parseFloat(multiplier);

        if (pres < prev) {
            if (isReset) {
                let strPrev = prev.toString().length;
                consumption = (Math.pow(10, parseFloat(strPrev)) - previous) + present;
            } else {
                consumption = present - previous;
            }
        } else {
            consumption = present - previous;
        }
        return parseFloat((consumption * totalMultiplier).toFixed(4));
    }

    // Validation
    public static validateRequired(checks: ValidityChecker[]): boolean {
        for (const check of checks) {
            if (check.value === check.default) {
                Swal.fire({
                    icon: 'warning',
                    title: 'Warning',
                    text: check.message,
                    timer: 3000,
                    timerProgressBar: true,
                    showConfirmButton: false
                });
                return false;
            }
        }
        return true;
    }

    public static isFieldEmpty(field: string | null | undefined): boolean {
        return field == null || field.trim() === '';
    }

    // Number Formatting
    public static roundMoney(value: number): number {
        return Math.round(value * 100) / 100;
    }

    public static roundMoney4Decimals(value: number): number {
        return Math.round(value * 10000) / 10000;
    }

    // Date & Time
    public static months() {
        return [
            "January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December"
        ];
    }

    public static years() {
        let year = new Date().getFullYear();
        year++;
        let years: Array<any> = [];
        for (var x = 0; x < 9; x++) {
            years.push(year--);
        }
        return years;
    }

    // Data Helpers
    public static billingReportTypes(): {id: number, description: string}[] {
        return [
            {id: 4, description: "Current"},
            {id: 5, description: "Late Bills"},
            {id: 6, description: "Direct Adjustment"},
            {id: 1, description: "Gross Sales"},
            {id: 2, description: "Previous Month Adjustment"},
            {id: 3, description: "Net Sales"}
        ];
    }

    public static advancePaymentItem() {
        return {id: 14, desc: 'Advance Payment'};
    }

    public static advancePaymentItemConsumer() {
        return {id: 64, desc: 'Advance Payment'};
    }

    // Payment Types
    public static check() {
        return 'CHECK';
    }

    public static bill() {
        return 'BILL';
    }

    public static bankDeposit() {
        return 'BANK_DEPOSIT';
    }

    // External Services
    public static googleMap(latitude: number, longitude: number): void {
        const url = `https://www.google.com/maps?q=${latitude},${longitude}&z=15`;
        window.open(url, '_blank');
    }

    // Environment Variables
    public static clearEnvVars() {
        for (var key in window.localStorage) {
            if (key.startsWith('env.')) {
                window.localStorage.removeItem(key);
            }
        }
    }

    public static getEnvVar(key: any, env: any) {
        let val = window.localStorage.getItem('env.' + key);
        if (val) {
            return val;
        } else {
            for (let entry of env) {
                for (let k in entry) {
                    window.localStorage.setItem('env.' + k, entry[k]);
                }
            }
            return window.localStorage.getItem('env.' + key);
        }
    }

    // Array Utilities
    public static idInIds(id: number, ids: any): boolean {
        for (let i = 0; i < ids.length; i++) {
            if (id == ids[0]) {
                return true;
            }
        }
        return false;
    }
}
