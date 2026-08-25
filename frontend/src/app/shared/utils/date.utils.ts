export function fmtDate(d: Date): string {
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

export function monthStart(): string {
    const now = new Date();
    return fmtDate(new Date(now.getFullYear(), now.getMonth(), 1));
}

export function monthEnd(): string {
    const now = new Date();
    return fmtDate(new Date(now.getFullYear(), now.getMonth() + 1, 0));
}

export function today(): string {
    return fmtDate(new Date());
}
