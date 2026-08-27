export interface HalPage<T> {
    _embedded?: Record<string, T[]>;
    page?: {
        size: number;
        totalElements: number;
        totalPages: number;
        number: number;
    };
}

export function toPagedResult<T>(res: HalPage<T> | null | undefined): { content: T[]; totalElements: number } {
    const embedded = res?._embedded ?? {};
    const [content] = Object.values(embedded);
    return {
        content: content ?? [],
        totalElements: res?.page?.totalElements ?? 0,
    };
}

export interface SpringPage<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
    numberOfElements: number;
    first: boolean;
    last: boolean;
    empty: boolean;
}

export interface InventoryPage<T> {
    content: T[];
    totalElements?: number;
    totalPages?: number;
    number?: number;
    size?: number;
    page?: {
        size?: number;
        number?: number;
        totalElements?: number;
        totalPages?: number;
    };
}
