import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');

@Injectable({ providedIn: 'root' })
export class InventoryReportsService {
    private http = inject(HttpClient);

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/document-statuses`);
    }

    getInventoryLocations(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/inventory-locations`);
    }

    getInventoryCategories(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/inventory-categories`);
    }

    getSpecialEquipmentTypes(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/special-equipment-type`);
    }

    getBinCard(from: string, to: string, itemStockId: number): Observable<any[]> {
        const params = new HttpParams()
            .set('from', from)
            .set('to', to)
            .set('itemStockId', itemStockId);
        return this.http.get<any[]>(`${BASE_API}/reports/data/bin-card`, { params });
    }

    getStockCard(from: string, to: string, itemStockId: number): Observable<any[]> {
        const params = new HttpParams()
            .set('from', from)
            .set('to', to)
            .set('itemStockId', itemStockId);
        return this.http.get<any[]>(`${BASE_API}/reports/data/stock-card`, { params });
    }

    getInventoryBalance(inventoryLocationId: number, inventoryCategoryId: number): Observable<any[]> {
        const params = new HttpParams()
            .set('inventoryLocationId', inventoryLocationId)
            .set('inventoryCategoryId', inventoryCategoryId);
        return this.http.get<any[]>(`${BASE_API}/reports/data/inventory-balance`, { params });
    }

    getBinCardData(from: string, to: string, itemStockId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/data/bin-card`, {
            params: new HttpParams().set('from', from).set('to', to).set('itemStockId', itemStockId)
        });
    }

    getStockCardData(from: string, to: string, itemStockId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/data/stock-card`, {
            params: new HttpParams().set('from', from).set('to', to).set('itemStockId', itemStockId)
        });
    }

    getMaterialIssuanceSummaryPaged(from: string, to: string, categoryId: number | null, locationId: number | null, page = 0, size = 20): Observable<any> {
        let params = new HttpParams().set('s', from).set('e', to).set('page', page).set('size', size);
        if (categoryId != null) params = params.set('c', categoryId);
        if (locationId != null) params = params.set('l', locationId);
        return this.http.get<any>(`${BASE_API}/reports/inventory/material-issuance-summary-list-paged`, { params });
    }

    getSpecialEquipmentReleaseSummaryPaged(from: string, to: string, typeId: number | null, page = 0, size = 20): Observable<any> {
        let params = new HttpParams().set('s', from).set('e', to).set('page', page).set('size', size);
        if (typeId != null) params = params.set('t', typeId);
        return this.http.get<any>(`${BASE_API}/reports/inventory/special-equipment-release-summary-list-paged`, { params });
    }

    getSpecialEquipmentPendingSummaryPaged(typeId: number | null, page = 0, size = 20): Observable<any> {
        let params = new HttpParams().set('page', page).set('size', size);
        if (typeId != null) params = params.set('t', typeId);
        return this.http.get<any>(`${BASE_API}/reports/inventory/special-equipment-pending-summary-list-paged`, { params });
    }

    getMrteLedgerData(acctNo: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/accounting/mrte-ledger`, {
            params: new HttpParams().set('acctNo', acctNo)
        });
    }

    getIdealQuantityData(locationId: number, reportTypeId: number, categoryId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/data/ideal-quantity-reorder-point`, {
            params: new HttpParams()
                .set('inventoryLocationId', locationId)
                .set('reportTypeId', reportTypeId)
                .set('inventoryCategoryId', categoryId)
        });
    }

    getMcrtSummary(from: string, to: string, locationId: number, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/api/mct/list/${from}/${to}/${statusId}`);
    }

    getMstSummary(from: string, to: string, locationId: number, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/api/mst/list/${from}/${to}/${statusId}`);
    }

    getWithdrawalSummary(from: string, to: string, docTypeId: number, locationId: number, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/api/withdrawal/list/${from}/${to}/${statusId}`);
    }

    getReleaseSummary(from: string, to: string, docTypeId: number, locationId: number, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/api/stock-release/list/${from}/${to}/${statusId}`);
    }

    getAdjustmentSummary(from: string, to: string, locationId: number, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/api/stock-adjustment/list/${from}/${to}/${statusId}`);
    }

    getTransferSummary(from: string, to: string, locationId: number, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/api/stock-transfer/list/${from}/${to}/${statusId}`);
    }

    getReceiveSummary(from: string, to: string, locationId: number, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/api/stock-receive/list/${from}/${to}/${statusId}`);
    }

    getMcrtItems(transactionId: number): Observable<any[]> {
        return this.http.get<any>(`${BASE_API}/api/mct/${transactionId}`).pipe(map((r: any) => r?.details ?? []));
    }

    getMstItems(transactionId: number): Observable<any[]> {
        return this.http.get<any>(`${BASE_API}/api/mst/${transactionId}`).pipe(map((r: any) => r?.details ?? []));
    }

    getWithdrawalItems(id: number): Observable<any[]> {
        return this.http.get<any>(`${BASE_API}/api/withdrawal/${id}`).pipe(map((r: any) => r?.details ?? []));
    }

    getReleaseItems(documentTransactionId: number): Observable<any[]> {
        return this.http.get<any>(`${BASE_API}/api/stock-release/${documentTransactionId}`).pipe(map((r: any) => r?.details ?? []));
    }
}
