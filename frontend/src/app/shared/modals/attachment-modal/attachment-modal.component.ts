import {Component, ElementRef, HostListener, ViewChild} from '@angular/core';
import {COMMON_ALL_PAGE_IMPORTS, SHARED_PROVIDERS} from '@/app/shared/providers/shared-providers';
import {LaddaModule} from 'angular2-ladda';
import {TagifyModule} from 'ngx-tagify';
import {provideIcons} from '@ng-icons/core';
import {tablerRefresh, tablerRotate, tablerRotateClockwise, tablerZoomIn, tablerZoomOut, tablerX} from '@ng-icons/tabler-icons';
import {NgbActiveModal, NgbTooltip} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-attachment-modal',
    imports: [...COMMON_ALL_PAGE_IMPORTS, LaddaModule, TagifyModule, NgbTooltip],
    providers: [...SHARED_PROVIDERS, provideIcons({
        tablerRotate,
        tablerRotateClockwise,
        tablerZoomIn,
        tablerZoomOut,
        tablerRefresh,
        tablerX
    })],
  templateUrl: './attachment-modal.component.html',
  styleUrl: './attachment-modal.component.scss'
})
export class AttachmentModalComponent {

    image: string | null = null;

    zoomLevel = 1;
    zoomStep = 0.1;
    maxZoom = 3;
    minZoom = 0.5;
    rotation = 0;

    translateX = 0;
    translateY = 0;




    isDragging = false;
    private startX = 0;
    private startY = 0;
    private lastX = 0;
    private lastY = 0;

    @ViewChild('container', { static: true }) containerRef!: ElementRef;
    @ViewChild('img', { static: true }) imgRef!: ElementRef<HTMLImageElement>;

    constructor(public activeModal: NgbActiveModal) {}

    closeModal(): void {
        this.activeModal.close();
    }

    zoomIn(): void {
        if (this.zoomLevel < this.maxZoom) {
            this.zoomLevel += this.zoomStep;
            this.applyTransform();
        }
    }

    zoomOut(): void {
        if (this.zoomLevel > this.minZoom) {
            this.zoomLevel -= this.zoomStep;
            this.applyTransform();
        }
    }

    resetZoom(): void {
        this.zoomLevel = 1;
        this.rotation = 0;
        this.translateX = 0;
        this.translateY = 0;
        this.applyTransform();
    }

    rotate(deg: number) {
        this.rotation += deg;
        this.applyTransform();
    }

    onMouseDown(event: MouseEvent): void {
        if (this.zoomLevel <= 1) return;

        event.preventDefault();
        this.isDragging = true;
        this.startX = event.clientX;
        this.startY = event.clientY;
        this.lastX = this.translateX;
        this.lastY = this.translateY;

        document.addEventListener('mousemove', this.onMouseMove);
        document.addEventListener('mouseup', this.onMouseUp);
    }

    onMouseMove = (event: MouseEvent): void => {
        if (!this.isDragging) return;

        const dx = event.clientX - this.startX;
        const dy = event.clientY - this.startY;

        this.translateX = this.lastX + dx;
        this.translateY = this.lastY + dy;
        this.applyTransform();
    };

    onMouseUp = (): void => {
        this.isDragging = false;
        document.removeEventListener('mousemove', this.onMouseMove);
        document.removeEventListener('mouseup', this.onMouseUp);
    };

    onDoubleClick(event: MouseEvent): void {
        if (!this.imgRef || this.zoomLevel >= this.maxZoom) return;

        const rect = this.imgRef.nativeElement.getBoundingClientRect();
        const offsetX = event.clientX - rect.left;
        const offsetY = event.clientY - rect.top;

        this.zoomLevel += this.zoomStep;

        const centerX = rect.width / 2;
        const centerY = rect.height / 2;

        this.translateX -= (offsetX - centerX) * (this.zoomStep / this.zoomLevel);
        this.translateY -= (offsetY - centerY) * (this.zoomStep / this.zoomLevel);

        this.applyTransform();
    }

    private applyTransform(): void {
        if (this.imgRef) {
            this.imgRef.nativeElement.style.transform =
                `translate(${this.translateX}px, ${this.translateY}px) rotate(${this.rotation}deg) scale(${this.zoomLevel})`;
        }
    }

    @HostListener('window:keydown', ['$event'])
    onKeyDown(event: KeyboardEvent): void {
        switch (event.key) {
            case 'ArrowLeft': this.rotate(-90); break;
            case 'ArrowRight': this.rotate(90); break;
            case '+': this.zoomIn(); break;
            case '-': this.zoomOut(); break;
            case 'Escape': this.closeModal(); break;
        }
    }

    reset(): void {
        this.zoomLevel = 1;
        this.rotation = 0;
        this.translateX = 0;
        this.translateY = 0;
        this.applyTransform();
    }

}
