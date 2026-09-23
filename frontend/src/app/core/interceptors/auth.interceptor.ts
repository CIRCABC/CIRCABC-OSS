import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { LoginService } from 'app/core/login.service';
import { environment } from 'environments/environment';

/**
 * Determines whether the request targets the ARES bridge server.
 *
 * @param url The outgoing request URL.
 * @returns `true` when the ARES bridge is enabled and the URL starts with the
 * configured ARES bridge server address.
 */
const isAresBridgeRequest = (url: string): boolean =>
  environment.aresBridgeEnabled && url.startsWith(environment.aresBridgeServer);

/**
 * Determines whether the request targets the CAPTCHA image endpoint.
 *
 * @param url The outgoing request URL.
 * @returns `true` when a CAPTCHA URL is configured and the URL refers to the
 * CAPTCHA image endpoint.
 */
const isCaptcha = (url: string): boolean =>
  environment.captchaURL !== undefined && url.includes('api/captchaImg');

/**
 * Determines whether the request targets a translation (i18n) asset, which
 * must be served without authentication.
 *
 * @param url The outgoing request URL.
 * @returns `true` if the URL points to an i18n asset file.
 */
const isTranslation = (url: string): boolean =>
  url.includes('assets') && url.includes('/i18n/');

/**
 * Functional HTTP interceptor that attaches authentication information to
 * outgoing CIRCABC API requests.
 *
 * Per request it decides whether and how to authenticate:
 * - Requests targeting the ARES bridge or the CAPTCHA image endpoint are
 *   passed through untouched (they are handled by their own flows).
 * - For authenticated users, it adds a `Basic` `Authorization` header built
 *   from the ECAS ticket obtained from {@link LoginService}.
 * - For guest users, it appends a `guest=true` query parameter instead.
 * - Translation asset requests (i18n JSON files) are left unmodified.
 *
 * {@link LoginService} is resolved with `inject()` at request time, which
 * avoids the construction-time circular dependency that the previous
 * class-based interceptor worked around with a manual {@link Injector}.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (isAresBridgeRequest(req.url) || isCaptcha(req.url)) {
    return next(req);
  }

  const loginService = inject(LoginService);

  if (!(loginService.isGuest() || isTranslation(req.url))) {
    const ticket = loginService.getTicket();
    return next(
      req.clone({
        headers: req.headers.set('Authorization', `Basic ${btoa(ticket)}`),
      })
    );
  }

  if (loginService.isGuest()) {
    return next(req.clone({ params: req.params.set('guest', 'true') }));
  }

  return next(req);
};
