import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const roleGuard: CanActivateFn = (route, state) => {
    const auth = inject(AuthService);
    const router = inject(Router);
    const routePath = state.url;
    const subRoutePath = route.data['mainPath'];
    const menuIsAllowed = auth.menuIsAllowed(routePath);
    const subMenuIsAllowed = auth.menuIsAllowed(subRoutePath);

    if (menuIsAllowed || subMenuIsAllowed) {
        return true;
    }

    // If not allowed → redirect (optional)
    router.navigate(['/error/401']);
    return false;
};

