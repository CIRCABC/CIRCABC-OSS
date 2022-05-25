/* eslint-disable @typescript-eslint/no-explicit-any */
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpResponse,
} from '@angular/common/http';
import { Injectable } from '@angular/core';

import { Observable, of } from 'rxjs';
import { startWith, tap } from 'rxjs/operators';

import { RequestCache } from 'app/core/interceptors/request-cache.service';

@Injectable()
export class CacheInterceptor implements HttpInterceptor {
  constructor(private cache: RequestCache) {}

  intercept(req: HttpRequest<any>, next: HttpHandler) {
    if (!isCachable(req)) {
      return next.handle(req);
    }

    const cachedResponse = this.cache.get(req);
    if (req.headers.get('x-refresh')) {
      const results$ = sendRequest(req, next, this.cache);
      return cachedResponse
        ? results$.pipe(startWith(cachedResponse))
        : results$;
    }
    return cachedResponse
      ? of(cachedResponse)
      : sendRequest(req, next, this.cache);
  }
}

function isCachable(req: HttpRequest<any>) {
  const urlRegex = /\/service\/circabc\/users\/([a-z0-9-]{7})/;
  const matches: RegExpMatchArray | null = req.url.match(urlRegex);
  if (matches !== null) {
    return req.method === 'GET' && req.url.endsWith(matches[1]);
  } else {
    return false;
  }
}

function sendRequest(
  req: HttpRequest<any>,
  next: HttpHandler,
  cache: RequestCache
): Observable<HttpEvent<any>> {
  let noHeaderRefresh = req.headers;
  if (noHeaderRefresh.get('x-refresh')) {
    noHeaderRefresh = noHeaderRefresh.delete('x-refresh');
  }

  return next.handle(req.clone({ headers: noHeaderRefresh })).pipe(
    tap((event) => {
      if (event instanceof HttpResponse) {
        cache.put(req, event);
      }
    })
  );
}
