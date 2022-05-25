/* eslint-disable @typescript-eslint/no-explicit-any */
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpResponse,
} from '@angular/common/http';
import { Injectable, Injector } from '@angular/core';
import { TranslocoService } from '@ngneat/transloco';
import { ActionUrl } from 'app/action-result';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getErrorTranslation, getSuccessTranslation } from 'app/core/util';
import { environment } from 'environments/environment';

/**
 * HTTP Interceptor that show success or error dialog when
 * POST PUT or DELETE HTTP requests are executed
 *
 * @export
 */
@Injectable()
export class MessageInterceptor implements HttpInterceptor {
  constructor(private injector: Injector) {}
  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    if (this.isAresBridgeRequest(req)) {
      return next.handle(req).pipe(
        catchError((response) => {
          if (response instanceof HttpErrorResponse) {
            const uiMessageService: UiMessageService =
              this.injector.get<UiMessageService>(UiMessageService);
            const translateService: TranslocoService =
              this.injector.get<TranslocoService>(TranslocoService);
            // eslint-disable-next-line no-console
            console.log('Processing AresBridge http error', response);
            if (response.status === 504) {
              const errorMessage = translateService.translate(
                'error.ares.server.not.accessible'
              );
              uiMessageService.addErrorMessage(errorMessage, false);
            } else if (response.error.id && response.error.message) {
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
      const actionType = ActionUrl.getActionType(req.urlWithParams, req.method);
      if (actionType === undefined) {
        return next.handle(req);
      }
      // eslint-disable-next-line no-console
      console.log('processing request', req);
      const translateService: TranslocoService =
        this.injector.get<TranslocoService>(TranslocoService);
      const uiMessageService: UiMessageService =
        this.injector.get<UiMessageService>(UiMessageService);

      return next
        .handle(req)
        .pipe(
          tap((ev: HttpEvent<any>) => {
            if (ev instanceof HttpResponse) {
              // eslint-disable-next-line no-console
              console.log('processing response', ev);
              const text = translateService.translate(
                getSuccessTranslation(actionType)
              );
              uiMessageService.addSuccessMessage(text, true);
            }
          })
        )
        .pipe(
          catchError((response) => {
            if (response instanceof HttpErrorResponse) {
              //  in order to get error message from server use response.error.message)
              // eslint-disable-next-line no-console
              console.log('Processing http error', response);
              const errorLabel = getErrorTranslation(actionType);
              const errorLabelStatus = `${errorLabel}.${response.status}`;
              const text = translateService.translate(errorLabelStatus);

              if (errorLabelStatus !== text) {
                uiMessageService.addErrorMessage(text, true);
              } else {
                const defaultText = translateService.translate(errorLabel);
                uiMessageService.addErrorMessage(defaultText, true);
              }
            }
            return throwError(() => response);
          })
        );
    } else {
      return next.handle(req);
    }
  }

  private isAresBridgeRequest(req: HttpRequest<{}>): boolean {
    return (
      environment.aresBridgeEnabled &&
      req.url.startsWith(environment.aresBridgeServer)
    );
  }
}
