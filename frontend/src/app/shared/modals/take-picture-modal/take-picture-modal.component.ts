import {AfterViewInit, Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {
    COMMON_ALL_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';

@Component({
  selector: 'app-take-picture-modal',
    imports: [...COMMON_ALL_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
  templateUrl: './take-picture-modal.component.html',
  styleUrl: './take-picture-modal.component.scss'
})
export class TakePictureModalComponent implements OnInit, AfterViewInit {

    @Input() title: string = '';
    @Input() message: string = '';

    @ViewChild('video') video!: ElementRef<HTMLVideoElement>;
    @ViewChild('canvas') canvas!: ElementRef<HTMLCanvasElement>;
    capturedImage: string | null = null;
    stream: MediaStream | null = null;
    cameraError: string | null = null;

    capturing = false;

    constructor(public activeModal: NgbActiveModal) {}

    ngOnInit(): void {}

    ngAfterViewInit(): void {
        this.startCamera();
    }

    async startCamera() {
        this.capturedImage = null;
        this.capturing = true;
        this.cameraError = null;

        if (!navigator.mediaDevices?.getUserMedia) {
            this.capturing = false;
            this.cameraError = 'Camera access is not available. This feature requires a secure connection (HTTPS).';
            return;
        }

        try {
            this.stream = await navigator.mediaDevices.getUserMedia({
                video: {
                    width: { ideal: 1080 },
                    height: { ideal: 1080 },
                    facingMode: 'user',
                }
            });
            this.video.nativeElement.srcObject = this.stream;
            this.video.nativeElement.play();
        } catch (err) {
            this.capturing = false;
            if (err instanceof DOMException) {
                if (err.name === 'NotAllowedError') {
                    this.cameraError = 'Camera permission was denied. Please allow camera access in your browser settings and try again.';
                } else if (err.name === 'NotFoundError') {
                    this.cameraError = 'No camera device was found. Please connect a camera and try again.';
                } else if (err.name === 'NotReadableError') {
                    this.cameraError = 'Camera is already in use by another application.';
                } else {
                    this.cameraError = `Camera error: ${err.message}`;
                }
            } else {
                this.cameraError = 'An unexpected error occurred while accessing the camera.';
            }
        }
    }

    capture() {
        const videoEl = this.video.nativeElement;
        const canvasEl = this.canvas.nativeElement;

        canvasEl.width = videoEl.videoWidth;
        canvasEl.height = videoEl.videoHeight;

        const ctx = canvasEl.getContext('2d');
        if (ctx) {
            ctx.drawImage(videoEl, 0, 0, canvasEl.width, canvasEl.height);
            this.capturedImage = canvasEl.toDataURL('image/jpeg'); // Base64 image
        }

        this.capturing = false;
        this.stopCamera();
    }

    stopCamera () {
        this.capturing = false;
        this.stream?.getTracks().forEach(track => track.stop());
    }

    close() {
        this.stopCamera ();
        this.activeModal.dismiss('closed');
    }

    confirmImage() {
        this.stopCamera ();
        this.activeModal.close({
            action: 'select',
            data: this.capturedImage
        });
    }

}
