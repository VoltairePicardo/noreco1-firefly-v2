import { ApplicationConfig, importProvidersFrom, provideZoneChangeDetection } from '@angular/core';
import { provideRouter, withHashLocation } from '@angular/router';

import { routes } from './app.routes';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {tokenInterceptor} from '@core/services/token.interceptor';
import { ToastrModule } from 'ngx-toastr';
import {provideIcons} from '@ng-icons/core';
import * as tablerIcons from '@ng-icons/tabler-icons';
import * as tablerIconsFill from '@ng-icons/tabler-icons/fill';

export const appConfig: ApplicationConfig = {
  providers: [
      provideZoneChangeDetection({ eventCoalescing: true }),
      provideIcons({...tablerIcons, ...tablerIconsFill}),
      provideRouter(routes, withHashLocation()),
      provideHttpClient(
          withInterceptors([tokenInterceptor])
      ),
      importProvidersFrom(
      ToastrModule.forRoot({
        timeOut: 3000,
        positionClass: 'toast-top-right',
        preventDuplicates: true,
      })
    ),
  ],
};
