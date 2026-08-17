import {Component, ElementRef, Input, ViewChild} from '@angular/core';
import {
    COMMON_ALL_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {LaddaModule} from 'angular2-ladda';
import {TagifyModule} from 'ngx-tagify';
import {provideIcons} from '@ng-icons/core';
import {
    tablerRefresh,
    tablerRotate,
    tablerRotateClockwise,
    tablerZoomIn,
    tablerZoomOut
} from '@ng-icons/tabler-icons';

@Component({
  selector: 'app-rab-image-view-modal',
    imports: [...COMMON_ALL_PAGE_IMPORTS, LaddaModule, TagifyModule],
    providers: [...SHARED_PROVIDERS, provideIcons({
        tablerRotate,
        tablerRotateClockwise,
        tablerZoomIn,
        tablerZoomOut,
        tablerRefresh
    })],
  templateUrl: './rab-image-view-modal.component.html',
  styleUrl: './rab-image-view-modal.component.scss',
  host: {
      '(window:keydown)': 'onKeyDown($event)'
  }
})
export class RabImageViewModalComponent {

    @Input() data: any;
    @ViewChild('rabImage', { static: true }) rabImage!: ElementRef<HTMLImageElement>;

    imageUrl: string = '';

    rotation: number = 0;
    zoom: number = 1;
    initialZoom: number = 1;
    maxZoom: number = 5;
    zoomStep: number = 0.5;

    isDragging: boolean = false;
    startX: number = 0;
    startY: number = 0;
    translateX: number = 0;
    translateY: number = 0;
    currentX: number = 0;
    currentY: number = 0;

    oldAcctNo: string = '';
    acctNo: string = '';
    fullName: string = '';
    meterSN: string = '';
    prevRdng: string = '';
    presRdng: string = '';
    kWhUsed: string = '';

    constructor(public activeModal: NgbActiveModal) {}

    ngOnInit(): void {
        if (!this.data) return;

        console.log(this.data);

        this.imageUrl = this.data.imageUrl;

        this.oldAcctNo = this.data.oldAcctNo;
        this.acctNo = this.data.accountNo;
        this.fullName = this.data.fullName;
        this.meterSN = this.data.serialNo;
        this.prevRdng = this.data.previousReading;
        this.presRdng = this.data.presentReading;
        this.kWhUsed = this.data.kWhUsed;

        const img = new Image();
        img.src = this.imageUrl;
        img.onload = () => {
            this.zoom = this.initialZoom;
            this.applyTransform();
        };
    }

    close(): void {
        this.activeModal.dismiss('closed');
    }

    rotate(deg: number): void {
        this.rotation += deg;
        this.applyTransform();
    }

    zoomIn(): void {
        if (this.zoom < this.maxZoom) {
            this.zoom += this.zoomStep;
            this.applyTransform();
        }
    }

    zoomOut(): void {
        if (this.zoom > this.initialZoom) {
            this.zoom -= this.zoomStep;
            if (this.zoom < this.initialZoom) {
                this.zoom = this.initialZoom;
                this.translateX = 0;
                this.translateY = 0;
            }
            this.applyTransform();
        }
    }

    reset(): void {
        this.rotation = 0;
        this.zoom = this.initialZoom;
        this.translateX = 0;
        this.translateY = 0;
        this.applyTransform();
    }

    private applyTransform(): void {
        if (this.rabImage) {
            this.rabImage.nativeElement.style.transform =
                `translate(${this.translateX}px, ${this.translateY}px) rotate(${this.rotation}deg) scale(${this.zoom})`;
        }
    }

    onKeyDown(event: KeyboardEvent): void {
        switch (event.key) {
            case 'ArrowLeft': this.rotate(-90); break;
            case 'ArrowRight': this.rotate(90); break;
            case '+': this.zoomIn(); break;
            case '-': this.zoomOut(); break;
            case 'Escape': this.close(); break;
        }
    }

    onImageMouseDown(event: MouseEvent): void {
        if (this.zoom > 1) {
            this.isDragging = true;
            this.startX = event.clientX - this.translateX;
            this.startY = event.clientY - this.translateY;
            event.preventDefault();
        }
    }

    onMouseMove(event: MouseEvent): void {
        if (!this.isDragging) return;
        this.currentX = event.clientX - this.startX;
        this.currentY = event.clientY - this.startY;
        this.translateX = this.currentX;
        this.translateY = this.currentY;
        this.applyTransform();
    }

    onMouseUp(): void {
        this.isDragging = false;
    }

    onDoubleClick(event: MouseEvent): void {
        if (!this.rabImage || this.zoom >= this.maxZoom) return;

        const rect = this.rabImage.nativeElement.getBoundingClientRect();
        const offsetX = event.clientX - rect.left;
        const offsetY = event.clientY - rect.top;

        this.zoom += this.zoomStep;

        const centerX = rect.width / 2;
        const centerY = rect.height / 2;

        this.translateX -= (offsetX - centerX) * (this.zoomStep / this.zoom);
        this.translateY -= (offsetY - centerY) * (this.zoomStep / this.zoom);
        this.applyTransform();
    }
}
