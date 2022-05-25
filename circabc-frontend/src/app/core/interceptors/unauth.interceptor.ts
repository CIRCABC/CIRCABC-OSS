/* eslint-disable @typescript-eslint/no-explicit-any */
import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from '@angular/common/http';
import { Injectable, Injector } from '@angular/core';
import { Router } from '@angular/router';
import { TranslocoService } from '@ngneat/transloco';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { environment } from 'environments/environment';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable()
export class UnauthInterceptor implements HttpInterceptor {
  constructor(private injector: Injector) {}

  private isAresBridgeRequest(req: HttpRequest<{}>): boolean {
    return (
      environment.aresBridgeEnabled &&
      req.url.startsWith(environment.aresBridgeServer)
    );
  }

  intercept(
    req: HttpRequest<{}>,
    next: HttpHandler
  ): Observable<HttpEvent<{}>> {
    if (this.isAresBridgeRequest(req)) {
      return next.handle(req);
    }
    const loginService: LoginService =
      this.injector.get<LoginService>(LoginService);
    const router: Router = this.injector.get<Router>(Router);
    const translateService: TranslocoService =
      this.injector.get<TranslocoService>(TranslocoService);
    const uiMessageService: UiMessageService =
      this.injector.get<UiMessageService>(UiMessageService);
    return next.handle(req).pipe(
      tap(
        // eslint-disable-next-line no-empty, @typescript-eslint/no-empty-function
        (_event: HttpEvent<any>) => {},
        (err: any) => {
          if (err instanceof HttpErrorResponse) {
            if (err.status === 401 && !loginService.isGuest()) {
              loginService.cleanAuthentication();
              // eslint-disable-next-line @typescript-eslint/no-floating-promises
              router.navigate(['welcome']);
              // eslint-disable-next-line @typescript-eslint/no-floating-promises
              const text = translateService.translate('error.session.expired');
              uiMessageService.addErrorMessage(text, true);
            }
          }
        }
      )
    );
  }
}
