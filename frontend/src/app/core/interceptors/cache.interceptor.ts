/* eslint-disable @typescript-eslint/no-explicit-any */
import {
  HttpEvent,
  HttpHandlerFn,
  HttpInterceptorFn,
  HttpRequest,
  HttpResponse,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { RequestCache } from 'app/core/interceptors/request-cache.service';
import { Observable, of } from 'rxjs';
import { startWith, tap } from 'rxjs/operators';

/**
 * Functional HTTP interceptor that transparently caches responses for cachable
 * requests (currently individual user profile `GET` requests).
 *
 * - On a cachable request, returns a previously stored response from the
 *   {@link RequestCache} when available, avoiding a network round trip.
 * - Supports a cache-refresh mode driven by the `x-refresh` request header:
 *   when present, the network request is always sent and, if a cached value
 *   exists, it is emitted first so consumers get an immediate value while the
 *   fresh response is fetched.
 * - Stores successful responses back into the cache for future reuse.
 *
 * {@link RequestCache} is resolved with `inject()` at request time.
 */
export const cacheInterceptor: HttpInterceptorFn = (req, next) => {
  if (!isCachable(req)) {
    return next(req);
  }

  const cache = inject(RequestCache);

  const cachedResponse = cache.get(req);
  if (req.headers.get('x-refresh')) {
    const results$ = sendRequest(req, next, cache);
    return cachedResponse ? results$.pipe(startWith(cachedResponse)) : results$;
  }
  return cachedResponse ? of(cachedResponse) : sendRequest(req, next, cache);
};

/**
 * Determines whether a request is eligible for caching.
 *
 * Only `GET` requests targeting an individual user resource
 * (`/service/circabc/users/{id}`, where the id is a 7-character
 * alphanumeric/hyphen token) whose URL ends with that id are considered
 * cachable.
 *
 * @param req The HTTP request to evaluate.
 * @returns `true` if the request may be served from / stored in the cache,
 * otherwise `false`.
 */
function isCachable(req: HttpRequest<any>) {
  const urlRegex = /\/service\/circabc\/users\/([a-z0-9-]{7})/;
  const matches: RegExpExecArray | null = urlRegex.exec(req.url);
  if (matches !== null) {
    return req.method === 'GET' && req.url.endsWith(matches[1]);
  }
  return false;
}

/**
 * Forwards a request to the network and caches any successful response.
 *
 * The `x-refresh` header (used only to signal cache-refresh intent) is stripped
 * from the outgoing request so it is not sent to the server. Any emitted
 * {@link HttpResponse} is written to the provided cache.
 *
 * @param req The HTTP request to send.
 * @param next The next handler function in the interceptor chain.
 * @param cache The cache store in which to persist the response.
 * @returns An observable of the HTTP events produced by the network call.
 */
function sendRequest(
  req: HttpRequest<any>,
  next: HttpHandlerFn,
  cache: RequestCache
): Observable<HttpEvent<any>> {
  let noHeaderRefresh = req.headers;
  if (noHeaderRefresh.get('x-refresh')) {
    noHeaderRefresh = noHeaderRefresh.delete('x-refresh');
  }

  return next(req.clone({ headers: noHeaderRefresh })).pipe(
    tap((event) => {
      if (event instanceof HttpResponse) {
        cache.put(req, event);
      }
    })
  );
}
