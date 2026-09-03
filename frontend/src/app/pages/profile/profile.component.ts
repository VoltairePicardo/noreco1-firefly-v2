import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { LaddaModule } from 'angular2-ladda';
import { NgIcon } from '@ng-icons/core';
import { provideIcons } from '@ng-icons/core';
import { tablerArrowLeft, tablerCheck } from '@ng-icons/tabler-icons';
import { UserService } from '@core/services/user.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { PageTitleComponent } from '@app/components/page-title.component';
import { UiCardComponent } from '@app/components/ui-card.component';

@Component({
    selector: 'app-profile',
    standalone: true,
    imports: [ReactiveFormsModule, CommonModule, RouterLink, LaddaModule, NgIcon, PageTitleComponent, UiCardComponent],
    providers: [provideIcons({ tablerArrowLeft, tablerCheck })],
    template: `
        <div class="container-fluid">
            <app-page-title title="My Profile" subTitle="Update Profile" menuLink="profile"/>
        </div>

        <div class="container-fluid">
            <div class="row">
                <div class="col-12">
                    <app-ui-card title="My Profile">
                        <div class="p-3" card-body>

                            @if (isLoading()) {
                                <div class="d-flex align-items-center gap-2 text-muted py-3">
                                    <span class="spinner-border spinner-border-sm" role="status"></span> Loading...
                                </div>
                            } @else {

                            <form [formGroup]="form" (ngSubmit)="submit()" novalidate>

                                <!-- Row 1: Full Name -->
                                <div class="row mb-3">
                                    <div class="col-md-6">
                                        <label class="form-label fw-bold">Full Name <span class="text-danger">*</span></label>
                                        <input type="text" class="form-control" formControlName="fullName"
                                               [class.is-invalid]="c('fullName').invalid && c('fullName').touched"/>
                                        @if (c('fullName').errors?.['required'] && c('fullName').touched) {
                                            <div class="invalid-feedback">Full name is required.</div>
                                        }
                                    </div>
                                </div>

                                <!-- Row 2: Username, Email -->
                                <div class="row mb-3">
                                    <div class="col-md-6">
                                        <label class="form-label fw-bold">Username <span class="text-danger">*</span></label>
                                        <input type="text" class="form-control" formControlName="username"
                                               [class.is-invalid]="c('username').invalid && c('username').touched"/>
                                        @if (c('username').errors?.['required'] && c('username').touched) {
                                            <div class="invalid-feedback">Username is required.</div>
                                        } @else if (c('username').errors?.['minlength'] && c('username').touched) {
                                            <div class="invalid-feedback">Minimum 3 characters.</div>
                                        }
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label fw-bold">Email <span class="text-danger">*</span></label>
                                        <input type="email" class="form-control" formControlName="email"
                                               [class.is-invalid]="c('email').invalid && c('email').touched"/>
                                        @if (c('email').errors?.['required'] && c('email').touched) {
                                            <div class="invalid-feedback">Email is required.</div>
                                        } @else if (c('email').errors?.['email'] && c('email').touched) {
                                            <div class="invalid-feedback">Please enter a valid email.</div>
                                        }
                                    </div>
                                </div>

                                <hr/>
                                <p class="fs-xs text-uppercase fw-semibold text-muted mb-3">
                                    Change Password <span class="text-muted fw-normal text-lowercase">(leave blank to keep current)</span>
                                </p>

                                <!-- Row 3: Current Password -->
                                <div class="row mb-3">
                                    <div class="col-md-6">
                                        <label class="form-label fw-bold">Password</label>
                                        <input type="password" class="form-control" formControlName="currentPassword"
                                               placeholder="Enter current password" autocomplete="current-password"/>
                                    </div>
                                </div>

                                <!-- Row 4: New Password, Retype -->
                                <div class="row mb-3">
                                    <div class="col-md-6">
                                        <label class="form-label fw-bold">New Password</label>
                                        <input type="password" class="form-control" formControlName="newPassword"
                                               placeholder="Enter new password" autocomplete="new-password"/>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label fw-bold">Retype</label>
                                        <input type="password" class="form-control" formControlName="retypePassword"
                                               placeholder="Retype new password" autocomplete="new-password"/>
                                    </div>
                                </div>

                                <!-- Footer -->
                                <div class="d-flex gap-2 justify-content-end align-items-center pt-3 border-top mt-3">
                                    <a routerLink="/home" class="btn btn-light fw-bold">
                                        <ng-icon name="tablerArrowLeft" class="ps-0 pe-3 fw-bold"></ng-icon>Back
                                    </a>
                                    <button type="submit" class="btn btn-primary fw-bold"
                                            [ladda]="saving()" data-style="expand-left">
                                        <ng-icon name="tablerCheck" class="ps-0 pe-3 fw-bold"></ng-icon>Save Profile
                                    </button>
                                </div>

                            </form>

                            }
                        </div>
                    </app-ui-card>
                </div>
            </div>
        </div>
    `
})
export class ProfileComponent implements OnInit {
    private userService = inject(UserService);
    private alertService = inject(AlertService);
    private fb = inject(FormBuilder);

    isLoading = signal(false);
    saving = signal(false);

    form = this.fb.group({
        fullName:        ['', Validators.required],
        username:        ['', [Validators.required, Validators.minLength(3), Validators.maxLength(64)]],
        email:           ['', [Validators.required, Validators.email]],
        currentPassword: [''],
        newPassword:     [''],
        retypePassword:  [''],
    });

    ngOnInit(): void {
        this.loadProfile();
    }

    loadProfile(): void {
        this.isLoading.set(true);
        this.userService.getSelfProfile().subscribe({
            next: (data) => {
                this.isLoading.set(false);
                this.form.patchValue({
                    fullName: data.fullName ?? '',
                    username: data.username ?? '',
                    email:    data.email ?? '',
                });
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error('Profile', 'Load Failed', 'Could not load profile data.');
            }
        });
    }

    submit(): void {
        const { fullName, username, email, currentPassword, newPassword, retypePassword } = this.form.value;

        if (this.form.get('fullName')!.invalid || this.form.get('username')!.invalid || this.form.get('email')!.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        const changingPw = !!(newPassword?.trim());
        if (changingPw) {
            if (!currentPassword?.trim()) {
                this.alertService.warning('Profile', 'Validation', 'Please enter your current password.');
                return;
            }
            if (newPassword !== retypePassword) {
                this.alertService.warning('Profile', 'Validation', 'New passwords do not match.');
                return;
            }
        }

        const payload: any = { fullName, username, email };
        if (changingPw) {
            payload.currentPassword = currentPassword;
            payload.newPassword     = newPassword;
            payload.retypePassword  = retypePassword;
        }

        this.saving.set(true);
        this.userService.updateSelfProfile(payload).subscribe({
            next: (res) => {
                this.saving.set(false);
                if (res.success) {
                    this.alertService.success('Profile', 'Saved', 'Profile updated successfully.');
                    this.form.patchValue({ currentPassword: '', newPassword: '', retypePassword: '' });
                } else {
                    this.alertService.error('Profile', 'Save Failed', res.failureMessage);
                }
            },
            error: (err: any) => {
                this.saving.set(false);
                this.alertService.error('Profile', 'Save Failed', err?.error?.failureMessage ?? 'Something went wrong.');
            }
        });
    }

    c(name: string) { return this.form.get(name)!; }
}
