/* eslint-disable @typescript-eslint/no-explicit-any */
import {
  HttpEvent,
  HttpInterceptor,
  HttpHandler,
  HttpRequest,
  HttpErrorResponse,
} from '@angular/common/http';
import { Injectable, Injector } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AnalyticsService } from 'app/core/analytics.service';
/* send error to the analytics service */
@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(private injector: Injector) {}
  intercept(
    request: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        const analyticsService: AnalyticsService =
          this.injector.get<AnalyticsService>(AnalyticsService);
        const url = request.url;
        const method = request.method;
        const statusCode = error.status;
        let errorResponse: string;
        if (error.error instanceof ErrorEvent) {
          errorResponse = `Client Side Error: ${error.error.message}`;
        } else {
          errorResponse = `Server Side Error: ${error.message}`;
        }
        if (!url.includes('/ticket/')) {
          analyticsService.trackHTTPError(
            url,
            method,
            statusCode,
            errorResponse
          );
        }
        return throwError(() => error);
      })
    );
  }
}
