import { ChangeDetectionStrategy, Component, DestroyRef, effect, inject, input, output, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { tablerFile, tablerTrash } from '@ng-icons/tabler-icons';
import { AlertService } from '@/app/shared/services/alert.service';

export interface AttachmentFile {
    id: number;
    originalFilename?: string;
    mimeType?: string;
    /** A file picked locally and not uploaded yet (add/edit forms). */
    local?: File;
}

@Component({
    selector: 'app-attachments',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [NgIcon],
    providers: [provideIcons({ tablerFile, tablerTrash })],
    template: `
        @if (files().length > 0) {
            <div class="row row-cols-4 g-3">
                @for (file of files(); track file.id) {
                    <div class="col">
                        <div class="position-relative">
                            <a href="javascript:void(0)"
                               class="d-flex align-items-center justify-content-center border rounded bg-light overflow-hidden"
                               style="height:140px" [title]="file.originalFilename" (click)="view(file)">
                                @if (isImage(file)) {
                                    @if (thumbs()[file.id]; as thumb) {
                                        <img [src]="thumb" [alt]="file.originalFilename"
                                             class="w-100 h-100" style="object-fit:cover"/>
                                    } @else {
                                        <span class="spinner-border spinner-border-sm text-muted"></span>
                                    }
                                } @else if (isPdf(file)) {
                                    <img src="assets/images/svg/pdf-svgrepo-com.svg" alt="PDF" style="height:80px"/>
                                } @else {
                                    <ng-icon name="tablerFile" class="text-muted" style="font-size:48px"></ng-icon>
                                }
                            </a>
                            @if (editable()) {
                                <button type="button"
                                        class="btn btn-light btn-icon rounded-circle position-absolute top-0 end-0 m-1 text-danger"
                                        (click)="remove.emit(file)">
                                    <ng-icon name="tablerTrash" class="fs-lg"></ng-icon>
                                </button>
                            }
                        </div>
                        <div class="fs-xs text-nowrap text-truncate mt-1" [title]="file.originalFilename">{{ file.originalFilename }}</div>
                    </div>
                }
            </div>
        } @else {
            <p class="text-muted mb-0 fs-sm">{{ emptyText() }}</p>
        }
    `,
})
export class AttachmentsComponent {
    private http = inject(HttpClient);
    private alertService = inject(AlertService);

    files = input<AttachmentFile[]>([]);
    fileUrl = input.required<(fileId: number) => string>();
    editable = input(false);
    emptyText = input('No attachments.');

    remove = output<AttachmentFile>();

    thumbs = signal<Record<number, string>>({});
    private loadedThumbs = new Map<number, string>();

    constructor() {
        effect(() => {
            const files = this.files();
            const ids = new Set(files.map((f) => f.id));

            // drop thumbnails of files that are gone
            this.loadedThumbs.forEach((url, id) => {
                if (!ids.has(id)) {
                    URL.revokeObjectURL(url);
                    this.loadedThumbs.delete(id);
                }
            });

            files.filter((f) => this.isImage(f) && !this.loadedThumbs.has(f.id)).forEach((f) => this.loadThumb(f));
            this.thumbs.set(Object.fromEntries(this.loadedThumbs));
        });

        inject(DestroyRef).onDestroy(() => this.loadedThumbs.forEach((url) => URL.revokeObjectURL(url)));
    }

    private loadThumb(file: AttachmentFile): void {
        if (file.local) {
            this.loadedThumbs.set(file.id, URL.createObjectURL(file.local));
            return;
        }

        // reserve the slot so the effect does not fetch the same file twice
        this.loadedThumbs.set(file.id, '');
        this.http.get(this.fileUrl()(file.id), { responseType: 'blob' }).subscribe({
            next: (blob) => {
                const typed = blob.type ? blob : new Blob([blob], { type: this.guessType(file) });
                this.loadedThumbs.set(file.id, URL.createObjectURL(typed));
                this.thumbs.set(Object.fromEntries(this.loadedThumbs));
            },
            error: () => this.loadedThumbs.delete(file.id),
        });
    }

    isImage(file: AttachmentFile): boolean {
        return /\.(jpe?g|png|gif|webp)$/i.test(file.originalFilename || '') || (file.mimeType || '').startsWith('image/');
    }

    isPdf(file: AttachmentFile): boolean {
        return (file.mimeType || '') === 'application/pdf' || /\.pdf$/i.test(file.originalFilename || '');
    }

    /** Opens the file in a new browser tab. The tab is opened synchronously on click so popup blockers allow it. */
    view(file: AttachmentFile): void {
        if (file.local) {
            const localUrl = URL.createObjectURL(file.local);
            window.open(localUrl, '_blank');
            setTimeout(() => URL.revokeObjectURL(localUrl), 60_000);
            return;
        }

        const tab = window.open('', '_blank');

        this.http.get(this.fileUrl()(file.id), { responseType: 'blob' }).subscribe({
            next: (blob) => {
                const typed = blob.type ? blob : new Blob([blob], { type: this.guessType(file) });
                const url = URL.createObjectURL(typed);
                if (tab) {
                    tab.location.href = url;
                } else {
                    window.open(url, '_blank');
                }
                setTimeout(() => URL.revokeObjectURL(url), 60_000);
            },
            error: () => {
                tab?.close();
                this.alertService.error('Attachment', 'Unable to load this file.', '');
            },
        });
    }

    private guessType(file: AttachmentFile): string {
        if (file.mimeType) return file.mimeType;
        return /\.pdf$/i.test(file.originalFilename || '') ? 'application/pdf' : 'application/octet-stream';
    }
}
