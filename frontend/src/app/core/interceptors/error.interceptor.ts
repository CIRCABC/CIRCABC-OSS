import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AnalyticsService } from 'app/core/analytics.service';
import { throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

/**
 * Functional HTTP interceptor that observes failed HTTP responses and reports
 * them to the {@link AnalyticsService} for monitoring purposes.
 *
 * The interceptor is non-intrusive: it inspects errors as they pass through the
 * HTTP pipeline, forwards relevant failures to analytics tracking, and then
 * re-throws the original error so downstream handlers can react to it as usual.
 * Errors originating from authentication-related endpoints are skipped:
 * requests whose URL contains `/ticket/`, and `401 Unauthorized` responses from
 * `/users/` endpoints, are not tracked.
 *
 * {@link AnalyticsService} is resolved with `inject()` at request time.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const analyticsService = inject(AnalyticsService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const url = req.url;
      const method = req.method;
      const statusCode = error.status;
      const errorResponse =
        error.error instanceof ErrorEvent
          ? `Client Side Error: ${error.error.message}`
          : `Server Side Error: ${error.message}`;

      if (!(
        url.includes('/ticket/') ||
        (url.includes('/users/') && statusCode === 401)
      )) {
        analyticsService.trackHTTPError(url, method, statusCode, errorResponse);
      }

      return throwError(() => error);
    })
  );
};
