import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { TranslocoService } from '@jsverse/transloco';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { environment } from 'environments/environment';
import { tap } from 'rxjs/operators';

/**
 * Determines whether the given request targets the ARES bridge backend, which
 * is handled by a separate authentication flow and therefore excluded from the
 * 401 handling performed here.
 *
 * @param url The outgoing request URL.
 * @returns `true` when the ARES bridge is enabled and the URL points at the
 * configured ARES bridge server.
 */
const isAresBridgeRequest = (url: string): boolean =>
  environment.aresBridgeEnabled && url.startsWith(environment.aresBridgeServer);

/**
 * Functional HTTP interceptor that handles unauthenticated (HTTP 401)
 * responses across the application.
 *
 * When a request fails with a `401 Unauthorized` status for a non-guest user,
 * the interceptor treats the situation as an expired session: it clears the
 * current authentication state, redirects the user to the welcome page and
 * surfaces a localized "session expired" error message. Requests targeting the
 * ARES bridge backend are passed through untouched.
 *
 * Collaborators ({@link LoginService}, {@link Router}, {@link TranslocoService}
 * and {@link UiMessageService}) are resolved with `inject()` at request time.
 */
export const unauthInterceptor: HttpInterceptorFn = (req, next) => {
  if (isAresBridgeRequest(req.url)) {
    return next(req);
  }

  const loginService = inject(LoginService);
  const router = inject(Router);
  const translateService = inject(TranslocoService);
  const uiMessageService = inject(UiMessageService);

  return next(req).pipe(
    tap({
      error: (err: unknown) => {
        if (
          err instanceof HttpErrorResponse &&
          err.status === 401 &&
          !loginService.isGuest()
        ) {
          console.error(err);
          loginService.cleanAuthentication();

          router.navigate(['welcome']);

          const text = translateService.translate('error.session.expired');
          uiMessageService.addErrorMessage(text, true);
        }
      },
    })
  );
};
