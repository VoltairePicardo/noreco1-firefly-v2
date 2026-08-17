import { appName, appNameExtended, credits, currentYear } from '@/app/constants';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '@/app/pages/auth/auth.service';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgOptimizedImage } from '@angular/common';
import {NG_ICON_DIRECTIVES} from '@ng-icons/core';

@Component({
    selector: 'app-sign-in',
    imports: [ReactiveFormsModule, NgOptimizedImage, NG_ICON_DIRECTIVES],
    templateUrl: './sign-in.component.html',
    styles: ``
})
export class SignInComponent {
    currentYear = currentYear;
    startYear = currentYear;
    credits = credits;
    appName = appName;
    appNameExtended = appNameExtended;

    private auth = inject(AuthService);
    private router = inject(Router);
    private route = inject(ActivatedRoute);
    private fb = inject(FormBuilder);

    form = this.fb.group({
        username: ['', Validators.required],
        password: ['', Validators.required],
    });

    error = signal('');
    showPassword = signal(false);

    togglePassword() {
        this.showPassword.update(v => !v);
    }

    onLogin() {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }
        this.error.set('');
        const { username, password } = this.form.value;
        this.auth.login({ username: username!, password: password! }).subscribe({
            next: () => {
                this.auth.fetchAndStoreMenus().subscribe(() => {
                    const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/home';
                    void this.router.navigateByUrl(returnUrl);
                });
            },
            error: (err: Error) => {
                this.error.set(err.message);
            }
        });
    }
}
