import { Component, inject, OnInit, TemplateRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { BankService } from './bank.service';
import { NotificationService } from '@/app/services/notification.service';

@Component({
    selector: 'app-bank',
    imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterLink],
    templateUrl: './bank.component.html'
})
export class BankComponent implements OnInit {
    private bankService = inject(BankService);
    private modal = inject(NgbModal);
    private fb = inject(FormBuilder);
    private notify = inject(NotificationService);

    banks: any[] = [];
    pageNumber = 0;
    totalPages = 0;
    totalElements = 0;
    searchQuery = '';
    loading = false;
    saving = false;
    isEditMode = false;
    form!: FormGroup;
    activeModal: any;

    ngOnInit(): void {
        this.initForm();
        this.load();
    }

    initForm(bank?: any): void {
        this.form = this.fb.group({
            id:             [bank?.id || null],
            name:           [bank?.name || '', Validators.required],
            address:        [bank?.address || '', Validators.required],
            contactNumbers: [bank?.contactNumbers || ''],
            contactPersons: [bank?.contactPersons || ''],
        });
    }

    load(page = 0): void {
        this.loading = true;
        this.bankService.getList(this.searchQuery, page).subscribe({
            next: (data) => {
                this.banks = data.content;
                this.pageNumber = data.number;
                this.totalPages = data.totalPages;
                this.totalElements = data.totalElements;
                this.loading = false;
            },
            error: () => {
                this.notify.showError('Failed to load banks.', 'Error');
                this.loading = false;
            }
        });
    }

    search(): void {
        this.load(0);
    }

    openCreate(tpl: TemplateRef<any>): void {
        this.isEditMode = false;
        this.initForm();
        this.activeModal = this.modal.open(tpl, { backdrop: 'static', size: 'md' });
    }

    openEdit(bank: any, tpl: TemplateRef<any>): void {
        this.isEditMode = true;
        this.initForm(bank);
        this.activeModal = this.modal.open(tpl, { backdrop: 'static', size: 'md' });
    }

    save(): void {
        if (this.form.invalid) { this.form.markAllAsTouched(); return; }
        this.saving = true;
        const req = this.isEditMode
            ? this.bankService.update(this.form.value)
            : this.bankService.create(this.form.value);

        req.subscribe({
            next: (res) => {
                this.saving = false;
                if (res.success) {
                    this.notify.showSuccess(res.successMessage, 'Success');
                    this.activeModal?.close();
                    this.load(this.pageNumber);
                } else {
                    this.notify.showError(res.failureMessage, 'Error');
                }
            },
            error: () => {
                this.saving = false;
                this.notify.showError('Something went wrong.', 'Error');
            }
        });
    }

    confirmDelete(bank: any): void {
        if (!confirm(`Delete "${bank.name}"?`)) return;
        this.bankService.delete(bank.id).subscribe({
            next: (res) => {
                if (res.success) {
                    this.notify.showSuccess(res.successMessage, 'Success');
                    this.load(this.pageNumber);
                } else {
                    this.notify.showError(res.failureMessage, 'Error');
                }
            }
        });
    }

    goToPage(p: number): void {
        if (p >= 0 && p < this.totalPages) this.load(p);
    }
}
