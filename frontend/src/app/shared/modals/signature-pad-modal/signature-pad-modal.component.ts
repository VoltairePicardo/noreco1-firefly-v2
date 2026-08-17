import { AfterViewInit, ChangeDetectionStrategy, Component, ElementRef, Input, OnDestroy, OnInit, ViewChild, computed, inject, signal } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import SignaturePad from 'signature_pad';
import { AlertService } from '@/app/shared/services/alert.service';

@Component({
    selector: 'app-signature-pad-modal',
    providers: [...SHARED_PROVIDERS],
    templateUrl: './signature-pad-modal.component.html',
    styleUrl: './signature-pad-modal.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    host: {
        '(window:resize)': 'resizeCanvas()',
        '(window:keydown)': 'handleKeyDown($event)'
    }
})
export class SignaturePadModalComponent implements OnInit, AfterViewInit, OnDestroy {

    @Input() title: string = '';
    @Input() message: string = '';
    @ViewChild('canvas', { static: true }) canvas!: ElementRef<HTMLCanvasElement>;
    private signaturePad!: SignaturePad;

    readonly pointerType = signal<'touch' | 'pen' | 'mouse' | null>(null);
    readonly isTouchCapable = computed(() => typeof navigator !== 'undefined' && navigator.maxTouchPoints > 0);

    readonly deviceStatus = computed(() => {
        const type = this.pointerType();
        if (type === 'pen') return { label: 'Signature Pad / Stylus', icon: 'ti ti-pencil', cssClass: 'text-success' };
        if (type === 'touch') return { label: 'Touch / Tablet', icon: 'ti ti-hand-finger', cssClass: 'text-primary' };
        if (type === 'mouse') return { label: 'Mouse / Touchpad', icon: 'ti ti-mouse', cssClass: 'text-danger' };
        if (this.isTouchCapable()) return { label: 'Touch Device Ready', icon: 'ti ti-device-tablet', cssClass: 'text-info' };
        return { label: 'Mouse Only', icon: 'ti ti-mouse', cssClass: 'text-danger' };
    });

    private readonly onPointerDown = (e: PointerEvent) => {
        const type = e.pointerType as 'touch' | 'pen' | 'mouse';
        if (this.pointerType() !== type) {
            this.pointerType.set(type);
        }
    };

    private alertService = inject(AlertService);

    constructor(public activeModal: NgbActiveModal) {}

    ngOnInit(): void {}

    ngAfterViewInit(): void {
        this.resizeCanvas();
        this.signaturePad = new SignaturePad(this.canvas.nativeElement, {
            minWidth: 1,
            maxWidth: 3,
            penColor: 'black',
            backgroundColor: 'rgba(255,255,255,0)',
        });
        this.canvas.nativeElement.addEventListener('pointerdown', this.onPointerDown);
    }

    ngOnDestroy(): void {
        this.canvas.nativeElement.removeEventListener('pointerdown', this.onPointerDown);
    }

    resizeCanvas(): void {
        const canvas = this.canvas.nativeElement;
        const ratio = Math.max(window.devicePixelRatio || 1, 1);
        canvas.width = canvas.offsetWidth * ratio;
        canvas.height = canvas.offsetHeight * ratio;
        canvas.getContext('2d')?.scale(ratio, ratio);
    }

    clear(): void {
        this.signaturePad.clear();
    }

    isEmpty(): boolean {
        return this.signaturePad.isEmpty();
    }

    save(): string | null {
        if (this.signaturePad.isEmpty()) {
            this.alertService.warning('Signature', 'Required', 'Please provide a signature first.');
            return null;
        }
        return this.signaturePad.toDataURL('image/png');
    }

    close() {
        this.activeModal.dismiss('closed');
    }

    confirmSignature() {
        if (this.signaturePad.isEmpty()) {
            this.alertService.warning('Signature', 'Required', 'Please provide a signature first.');
        } else {
            this.activeModal.close({
                action: 'select',
                data: this.signaturePad.toDataURL('image/png')
            });
        }
    }

    handleKeyDown(event: KeyboardEvent): void {
        if (event.key === 'Backspace') {
            event.preventDefault();
            this.clear();
        }
    }
}
