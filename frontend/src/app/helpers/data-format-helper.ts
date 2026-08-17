import moment from 'moment';

export class DataFormatHelper {

    public static buildAddress(data: any): string {
        const parts = [
            data?.houseNo ? `House ${data.houseNo}` : null,
            data?.lotNo ? `Lot ${data.lotNo}` : null,
            data?.blockNo ? `Block ${data.blockNo}` : null,
            data?.street,
            data?.barangay?.name,
            data?.town?.name
        ];

        return parts.filter(Boolean).join(', ');
    }

}
