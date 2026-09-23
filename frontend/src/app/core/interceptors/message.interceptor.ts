import {
  HttpErrorResponse,
  HttpInterceptorFn,
  HttpResponse,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';
import { getActionType } from 'app/action-result';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getErrorTranslation, getSuccessTranslation } from 'app/core/util';
import { environment } from 'environments/environment';
import { throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

/**
 * Determines whether a request targets the ARES bridge server.
 *
 * @param url The outgoing request URL.
 * @returns `true` when the ARES bridge integration is enabled and the URL
 * points at the configured ARES bridge server; otherwise `false`.
 */
const isAresBridgeRequest = (url: string): boolean =>
  environment.aresBridgeEnabled && url.startsWith(environment.aresBridgeServer);

/**
 * Functional HTTP interceptor that surfaces user-facing success and error
 * notifications for mutating HTTP requests.
 *
 * For `POST`, `PUT` and `DELETE` requests it resolves an action type from the
 * request URL and method and, upon completion, pushes a localized success or
 * error message to the {@link UiMessageService}. Requests targeting the ARES
 * bridge server receive dedicated error handling (gateway timeouts and
 * server-provided error payloads).
 *
 * {@link TranslocoService} and {@link UiMessageService} are resolved with
 * `inject()` at request time. Injection happens synchronously inside the
 * relevant branch (never inside an RxJS callback), so services are only
 * resolved for the requests that actually need them.
 */
export const messageInterceptor: HttpInterceptorFn = (req, next) => {
  if (isAresBridgeRequest(req.url)) {
    const translateService = inject(TranslocoService);
    const uiMessageService = inject(UiMessageService);

    return next(req).pipe(
      catchError((response: unknown) => {
        if (response instanceof HttpErrorResponse) {
          if (response.status === 504) {
            const errorMessage = translateService.translate(
              'error.ares.server.not.accessible'
            );
            uiMessageService.addErrorMessage(errorMessage, false);
          } else if (response.error?.id && response.error?.message) {
            const errorMessage = translateService.translate(
              'error.ares.original.error',
              {
                errorId: response.error.id,
                errorMessage: response.error.message,
              }
            );
            uiMessageService.addErrorMessage(errorMessage, false);
          }
        }
        return throwError(() => response);
      })
    );
  }

  if (
    req.method === 'POST' ||
    req.method === 'PUT' ||
    req.method === 'DELETE'
  ) {
    const actionType = getActionType(req.urlWithParams, req.method);
    if (actionType === undefined) {
      return next(req);
    }

    const translateService = inject(TranslocoService);
    const uiMessageService = inject(UiMessageService);

    return next(req).pipe(
      tap((ev) => {
        if (ev instanceof HttpResponse) {
          const text = translateService.translate(
            getSuccessTranslation(actionType)
          );
          uiMessageService.addSuccessMessage(text, true);
        }
      }),
      catchError((response: unknown) => {
        if (response instanceof HttpErrorResponse) {
          const errorLabel = getErrorTranslation(actionType);
          const errorLabelStatus = `${errorLabel}.${response.status}`;
          const text = translateService.translate(errorLabelStatus);

          if (errorLabelStatus === text) {
            const defaultText = translateService.translate(errorLabel);
            uiMessageService.addErrorMessage(defaultText, true);
          } else {
            uiMessageService.addErrorMessage(text, true);
          }
        }
        return throwError(() => response);
      })
    );
  }

  return next(req);
};
