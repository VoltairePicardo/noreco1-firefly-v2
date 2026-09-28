/**
 * Maps a document status label to a bootstrap "-subtle" badge variant
 * (mirrors the legacy documentStatusUtil.classType() color coding).
 */
export function documentStatusBadgeVariant(status: string | null | undefined): string {
    const normalized = (status ?? '').toLowerCase();

    if (normalized.includes('approved') || normalized.includes('certified') || normalized.includes('released')) {
        return 'success';
    }
    if (normalized.includes('denied') || normalized.includes('cancelled')) {
        return 'danger';
    }
    if (normalized.includes('returned')) {
        return 'warning';
    }
    if (normalized.includes('created')) {
        return 'info';
    }
    return 'primary';
}

export function documentStatusBadgeClass(status: string | null | undefined): string {
    const variant = documentStatusBadgeVariant(status);
    return `badge badge-label bg-${variant}-subtle text-${variant}`;
}
