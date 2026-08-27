import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { ItemStock, ItemStockDto } from '@/app/models/inventory-modules/item-stock.model';
import { InventoryPage } from '@/app/models/shared/page.model';

const BASE_API = environment.get('baseApiUrl');

@Injectable({ providedIn: 'root' })
export class ItemStockService {
    private http = inject(HttpClient);

    private pageParams(query = '', page = 0, size = 10): HttpParams {
        return new HttpParams().set('q', query).set('page', page).set('size', size);
    }

    getItemStockNotInvLoc(invLocId: number, query = '', page = 0, size = 10): Observable<InventoryPage<ItemStock>> {
        return this.http.get<InventoryPage<ItemStock>>(
            `${BASE_API}/item-stock/item-stock/list-paged-not-inv-loc/${invLocId}`,
            { params: this.pageParams(query, page, size) }
        );
    }

    getItemStockInvLoc(invLocId: number, query = '', page = 0, size = 10): Observable<InventoryPage<ItemStock>> {
        return this.http.get<InventoryPage<ItemStock>>(
            `${BASE_API}/item-stock/item-stock/list-paged-inv-loc/${invLocId}`,
            { params: this.pageParams(query, page, size) }
        );
    }

    getItemStocks(query = '', page = 0, size = 10): Observable<InventoryPage<ItemStock>> {
        return this.http.get<InventoryPage<ItemStock>>(
            `${BASE_API}/item-stock/item-stock/list-paged`,
            { params: this.pageParams(query, page, size) }
        );
    }

    getItemStocksWithZeroQuantity(query = '', page = 0, size = 10): Observable<InventoryPage<ItemStock>> {
        return this.http.get<InventoryPage<ItemStock>>(
            `${BASE_API}/item-stock/item-stock/list-paged-with-zero-quantity`,
            { params: this.pageParams(query, page, size) }
        );
    }

    getItemStocksWithZeroQuantityInvLocInvCat(invLocId: number, invCatId: number, query = '', page = 0, size = 10): Observable<InventoryPage<ItemStock>> {
        return this.http.get<InventoryPage<ItemStock>>(
            `${BASE_API}/item-stock/item-stock/list-paged-with-zero-quantity/inv-loc/inv-cat/${invLocId}/${invCatId}`,
            { params: this.pageParams(query, page, size) }
        );
    }

    getItemStocksInvLocWithZeroQuantity(invLocId: number, query = '', page = 0, size = 10): Observable<InventoryPage<ItemStock>> {
        return this.http.get<InventoryPage<ItemStock>>(
            `${BASE_API}/item-stock/item-stock/list-paged-inv-loc-with-zero-quantity/${invLocId}`,
            { params: this.pageParams(query, page, size) }
        );
    }

    getItemStocksInvLocWithZeroQuantityNoStock(invLocId: number, query = '', page = 0, size = 10): Observable<InventoryPage<ItemStockDto>> {
        return this.http.get<InventoryPage<ItemStockDto>>(
            `${BASE_API}/item-stock/item-stock/list-paged-inv-loc-with-zero-quantity-no-stock/${invLocId}`,
            { params: this.pageParams(query, page, size) }
        );
    }
}
