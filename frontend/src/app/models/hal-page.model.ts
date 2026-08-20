/** Minimal shape of a Spring HATEOAS `PagedModel<EntityModel<T>>` response. */
export interface HalPage<T> {
    _embedded?: Record<string, T[]>;
    page?: {
        size: number;
        totalElements: number;
        totalPages: number;
        number: number;
    };
}

/** Flattens a `HalPage<T>` into a plain content/totalElements shape. */
export function toPagedResult<T>(res: HalPage<T> | null | undefined): { content: T[]; totalElements: number } {
    const embedded = res?._embedded ?? {};
    const [content] = Object.values(embedded);
    return {
        content: content ?? [],
        totalElements: res?.page?.totalElements ?? 0,
    };
}
