import { Component, inject, OnInit, TemplateRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { DepartmentService } from './department.service';
import { NotificationService } from '@/app/services/notification.service';

@Component({
    selector: 'app-department',
    imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterLink],
    templateUrl: './department.component.html'
})
export class DepartmentComponent implements OnInit {
    private departmentService = inject(DepartmentService);
    private modal = inject(NgbModal);
    private fb = inject(FormBuilder);
    private notify = inject(NotificationService);

    departments: any[] = [];
    filtered: any[] = [];
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

    initForm(dept?: any): void {
        this.form = this.fb.group({
            id:           [dept?.id || null],
            abbreviation: [dept?.abbreviation || '', Validators.required],
            name:         [dept?.name || '', Validators.required],
        });
    }

    load(): void {
        this.loading = true;
        this.departmentService.getList().subscribe({
            next: (data) => {
                this.departments = data;
                this.applyFilter();
                this.loading = false;
            },
            error: () => {
                this.notify.showError('Failed to load departments.', 'Error');
                this.loading = false;
            }
        });
    }

    applyFilter(): void {
        const q = this.searchQuery.toLowerCase();
        this.filtered = q
            ? this.departments.filter(d =>
                d.name?.toLowerCase().includes(q) ||
                d.abbreviation?.toLowerCase().includes(q))
            : [...this.departments];
    }

    openCreate(tpl: TemplateRef<any>): void {
        this.isEditMode = false;
        this.initForm();
        this.activeModal = this.modal.open(tpl, { backdrop: 'static', size: 'md' });
    }

    openEdit(dept: any, tpl: TemplateRef<any>): void {
        this.isEditMode = true;
        this.initForm(dept);
        this.activeModal = this.modal.open(tpl, { backdrop: 'static', size: 'md' });
    }

    save(): void {
        if (this.form.invalid) { this.form.markAllAsTouched(); return; }
        this.saving = true;
        const req = this.isEditMode
            ? this.departmentService.update(this.form.value)
            : this.departmentService.create(this.form.value);

        req.subscribe({
            next: (res) => {
                this.saving = false;
                if (res.success) {
                    this.notify.showSuccess(res.successMessage, 'Success');
                    this.activeModal?.close();
                    this.load();
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

    confirmDelete(dept: any): void {
        if (!confirm(`Delete "${dept.name}"?`)) return;
        this.departmentService.delete(dept.id).subscribe({
            next: (res) => {
                if (res.success) {
                    this.notify.showSuccess(res.successMessage, 'Success');
                    this.load();
                } else {
                    this.notify.showError(res.failureMessage, 'Error');
                }
            }
        });
    }
}
