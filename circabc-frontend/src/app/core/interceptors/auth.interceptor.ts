/* eslint-disable @typescript-eslint/no-explicit-any */
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from '@angular/common/http';
import { Injectable, Injector } from '@angular/core';
import { LoginService } from 'app/core/login.service';
import { environment } from 'environments/environment';
import { Observable } from 'rxjs';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private inj: Injector) {}

  intercept(
    req: HttpRequest<{}>,
    next: HttpHandler
  ): Observable<HttpEvent<{}>> {
    if (this.isAresBridgeRequest(req) || this.isCaptcha(req)) {
      return next.handle(req);
    }
    const loginService: LoginService = this.inj.get<LoginService>(LoginService);
    if (!(this.isGuest(loginService) || this.isTranslation(req))) {
      const ticket = loginService.getTicket();
      const clonedRequest = req.clone({
        headers: req.headers.set('Authorization', `Basic ${btoa(ticket)}`),
      });
      return next.handle(clonedRequest);
    }
    if (this.isGuest(loginService)) {
      const guestRequest = req.clone({
        params: req.params.set('guest', 'true'),
      });
      return next.handle(guestRequest);
    }
    return next.handle(req);
  }
  private isGuest(loginService: LoginService): boolean {
    return loginService.isGuest();
  }
  private isTranslation(req: HttpRequest<{}>): boolean {
    return req.url.indexOf('assets') > -1 && req.url.indexOf('/i18n/') > -1;
  }

  private isAresBridgeRequest(req: HttpRequest<{}>): boolean {
    return (
      environment.aresBridgeEnabled &&
      req.url.startsWith(environment.aresBridgeServer)
    );
  }
  private isCaptcha(req: HttpRequest<{}>): boolean {
    return (
      environment.captchaURL !== undefined &&
      req.url.indexOf('api/captchaImg') > -1
    );
  }
}
