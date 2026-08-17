
import {HttpRequest} from "@angular/common/http";
import {HttpHandler} from "@angular/common/http";
import {HttpInterceptor} from "@angular/common/http";
import {HTTP_INTERCEPTORS} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {TokenStorageService} from '@core/services/token-storage.service';

const TOKEN_HEADER_KEY = 'Authorization';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private token: TokenStorageService) { }

  intercept(req: HttpRequest<any>, next: HttpHandler) {
    let authReq = req;
    const token = this.token.getToken();
    if (token != null) {
      authReq = req.clone({ headers: req.headers.set(TOKEN_HEADER_KEY, 'Bearer ' + token) });
    }
    return next.handle(authReq);
  }
}

export const authInterceptorProviders = [
  { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
];
