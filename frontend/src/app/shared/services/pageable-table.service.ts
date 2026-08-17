import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { debounceTime, switchMap, tap } from 'rxjs/operators';
import { HttpClient, HttpParams } from '@angular/common/http';

export interface SearchResult<T> {
    items: T[];
    total: number;
}


@Injectable({
    providedIn: 'root'
})
export class PageableTableService<T> {
    private _items$ = new BehaviorSubject<T[]>([]);
    private _total$ = new BehaviorSubject<number>(0);
    private _loading$ = new BehaviorSubject<boolean>(true);
    private _search$ = new Subject<void>();

    private _state = {
        page: 1,
        pageSize: 10,
        searchTerm: '',
        sortColumn: '',
        sortDirection: '',
        filters: {} as Record<string, string>
    };

    private _apiUrl = '/api/default'; // default, can be overridden

    constructor(private http: HttpClient) {
        this._search$
            .pipe(
                tap(() => this._loading$.next(true)),
                debounceTime(150),
                switchMap(() => this._fetchPage()),
                tap(() => this._loading$.next(false))
            )
            .subscribe(result => {
                this._items$.next(result.items);
                this._total$.next(result.total);
            });

        this._search$.next();
    }

    /** Observables */
    get items$(): Observable<T[]> { return this._items$.asObservable(); }
    get total$(): Observable<number> { return this._total$.asObservable(); }
    get loading$(): Observable<boolean> { return this._loading$.asObservable(); }

    /** State setters */
    set page(page: number) { this._state.page = page; this._search$.next(); }
    set pageSize(size: number) { this._state.pageSize = size; this._search$.next(); }
    set searchTerm(term: string) { this._state.searchTerm = term; this._search$.next(); }

    setSort(column: string, direction: 'asc' | 'desc' | '') {
        this._state.sortColumn = column;
        this._state.sortDirection = direction;
        this._search$.next();
    }

    setFilter(column: string, value: string) {
        if (!value || value === 'All') delete this._state.filters[column];
        else this._state.filters[column] = value;
        this._search$.next();
    }

    /** Set the API URL dynamically */
    setApiUrl(url: string) {
        this._apiUrl = url;
        this._search$.next(); // fetch from new API
    }

    /** Internal fetch */
    private _fetchPage(): Observable<SearchResult<T>> {
        let params = new HttpParams()
            .set('page', this._state.page.toString())
            .set('pageSize', this._state.pageSize.toString());

        if (this._state.searchTerm) params = params.set('search', this._state.searchTerm);
        if (this._state.sortColumn) {
            params = params.set('sortColumn', this._state.sortColumn)
                .set('sortDirection', this._state.sortDirection);
        }

        Object.keys(this._state.filters).forEach(k => {
            params = params.set(k, this._state.filters[k]);
        });

        return this.http.get<SearchResult<T>>(this._apiUrl, { params });
    }
}
