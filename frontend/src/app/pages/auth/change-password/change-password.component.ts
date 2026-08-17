import {
    AbstractControl,
    FormBuilder,
    ReactiveFormsModule,
    ValidationErrors,
    Validators,
} from '@angular/forms';
import {
    ChangeDetectionStrategy,
    Component,
    inject,
    signal,
} from '@angular/core';
import { Router } from '@angular/router';
import { LaddaModule } from 'angular2-ladda';
import { UserService } from '@core/services/user.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { PageTitleComponent } from '@app/components/page-title.component';
import { UiCardComponent } from '@app/components/ui-card.component';

function confirmPasswordValidator(group: AbstractControl): ValidationErrors | null {
    const newPw = group.get('newPassword')?.value;
    const confirm = group.get('confirmPassword')?.value;
    if (confirm && newPw !== confirm) {
        return { passwordMismatch: true };
    }
    return null;
}

@Component({
    selector: 'app-change-password',
    standalone: true,
    imports: [ReactiveFormsModule, LaddaModule, PageTitleComponent, UiCardComponent],
    template: `
        <div class="container-fluid">
            <app-page-title title="Profile" subTitle="Change Password" menuLink="profile/change-password"/>
        </div>

        <div class="container-fluid">
            <div class="row justify-content-center">
                <div class="col-12">
                    <app-ui-card title="Change Password">
                        <div class="p-3" card-body>
                            <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
                                <div class="row mb-3">
                                    <div class="col-12 col-md-3">
                                        <label for="oldPassword" class="form-label">
                                            Current Password <span class="text-danger" aria-hidden="true">*</span>
                                        </label>
                                        <input
                                            id="oldPassword"
                                            type="password"
                                            class="form-control"
                                            formControlName="oldPassword"
                                            [class.is-invalid]="c('oldPassword').invalid && c('oldPassword').touched"
                                            autocomplete="current-password"
                                            aria-required="true"
                                        />
                                        @if (c('oldPassword').errors?.['required'] && c('oldPassword').touched) {
                                            <div class="invalid-feedback">Current password is required.</div>
                                        }
                                    </div>
                                </div>

                                <div class="row mb-3">
                                    <div class="col-12 col-md-3">
                                        <label for="newPassword" class="form-label">
                                            New Password <span class="text-danger" aria-hidden="true">*</span>
                                        </label>
                                        <input
                                            id="newPassword"
                                            type="password"
                                            class="form-control"
                                            formControlName="newPassword"
                                            [class.is-invalid]="c('newPassword').invalid && c('newPassword').touched"
                                            autocomplete="new-password"
                                            aria-required="true"
                                            aria-describedby="newPasswordHint"
                                        />
                                        @if (c('newPassword').errors?.['required'] && c('newPassword').touched) {
                                            <div class="invalid-feedback">New password is required.</div>
                                        } @else if (c('newPassword').errors?.['minlength'] && c('newPassword').touched) {
                                            <div class="invalid-feedback">Must be at least 8 characters.</div>
                                        }
                                        <div id="newPasswordHint" class="form-text">Minimum 8 characters.</div>
                                    </div>
                                </div>

                                <div class="row mb-3">
                                    <div class="col-12 col-md-3">
                                        <label for="confirmPassword" class="form-label">
                                            Confirm New Password <span class="text-danger" aria-hidden="true">*</span>
                                        </label>
                                        <input
                                            id="confirmPassword"
                                            type="password"
                                            class="form-control"
                                            formControlName="confirmPassword"
                                            [class.is-invalid]="(c('confirmPassword').invalid || form.errors?.['passwordMismatch']) && c('confirmPassword').touched"
                                            autocomplete="new-password"
                                            aria-required="true"
                                        />
                                        @if (c('confirmPassword').errors?.['required'] && c('confirmPassword').touched) {
                                            <div class="invalid-feedback">Please confirm your new password.</div>
                                        } @else if (form.errors?.['passwordMismatch'] && c('confirmPassword').touched) {
                                            <div class="invalid-feedback">Passwords do not match.</div>
                                        }
                                    </div>
                                </div>

                                <hr/>
                                <div class="text-end">
                                    <button
                                        type="button"
                                        class="btn btn-light me-2"
                                        (click)="router.navigate(['/home'])"
                                    >Cancel</button>
                                    <button
                                        type="submit"
                                        class="btn btn-primary fw-bold"
                                        [ladda]="saving()"
                                        data-style="expand-left"
                                    >Change Password</button>
                                </div>
                            </form>
                        </div>
                    </app-ui-card>
                </div>
            </div>
        </div>
    `
})
export class ChangePasswordComponent {
    private userService = inject(UserService);
    private alertService = inject(AlertService);
    router = inject(Router);
    private fb = inject(FormBuilder);

    saving = signal(false);

    form = this.fb.group(
        {
            oldPassword: ['', Validators.required],
            newPassword: ['', [Validators.required, Validators.minLength(8)]],
            confirmPassword: ['', Validators.required],
        },
        { validators: confirmPasswordValidator }
    );

    c(name: string) {
        return this.form.get(name)!;
    }

    submit() {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }
        this.saving.set(true);
        const { oldPassword, newPassword } = this.form.value;
        this.userService.changePassword({ oldPassword: oldPassword!, newPassword: newPassword! }).subscribe({
            next: () => {
                this.saving.set(false);
                this.alertService.success('Password', 'Changed', '').then(() => this.form.reset());
            },
            error: (err: any) => {
                this.saving.set(false);
                this.alertService.error(
                    'Password',
                    'Change Failed',
                    err?.error?.message ?? 'Please verify your current password and try again.'
                );
            },
        });
    }
}
