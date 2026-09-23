/* eslint-disable @typescript-eslint/no-explicit-any */
import { HttpEvent, HttpRequest, HttpResponse } from '@angular/common/http';
import { Service } from '@angular/core';
import { environment } from 'environments/environment';

/**
 * Internal record stored for each cached HTTP response.
 */
interface RequestCacheEntry {
  /** The request URL (including query parameters) used as the cache key. */
  url: string;
  /** The cached HTTP response associated with the URL. */
  response: HttpResponse<any>;
  /** Timestamp (epoch milliseconds) of when the entry was last written/read. */
  lastRead: number;
}
/**
 * Abstract contract for an HTTP request cache.
 *
 * Defines the operations used by HTTP interceptors to retrieve and store
 * cached responses, allowing the concrete cache implementation to be swapped
 * via Angular dependency injection.
 */
export abstract class RequestCache {
  /**
   * Retrieves a cached response for the given request, if present and valid.
   *
   * @param req The HTTP request to look up in the cache.
   * @returns The cached HTTP event, or `undefined` when there is no valid entry.
   */
  abstract get(req: HttpRequest<any>): HttpEvent<any> | undefined;
  /**
   * Stores a response in the cache for the given request.
   *
   * @param req The HTTP request that produced the response.
   * @param response The HTTP response to cache.
   */
  abstract put(req: HttpRequest<any>, response: HttpResponse<any>): void;
}

/** Maximum time (in milliseconds) a cached entry is considered valid. */
const maxAge = 300000; // maximum cache age (ms)

/**
 * In-memory implementation of {@link RequestCache} backed by a `Map`.
 *
 * Caches HTTP responses keyed by their full URL (including query parameters).
 * Entries expire after {@link maxAge} milliseconds; expired entries are treated
 * as cache misses and are pruned on subsequent writes. Diagnostic logging is
 * emitted only in non-production environments.
 *
 * This service is typically provided against the {@link RequestCache} token and
 * consumed by HTTP caching interceptors.
 */
@Service({ autoProvided: false })
export class RequestCacheWithMap implements RequestCache {
  /** Backing store mapping request URLs to their cache entries. */
  cache = new Map<string, RequestCacheEntry>();

  /**
   * Returns the cached response for a request when a non-expired entry exists.
   *
   * The full request URL (`urlWithParams`) is used as the cache key. If the
   * matching entry is older than {@link maxAge}, it is considered expired and
   * `undefined` is returned.
   *
   * @param req The HTTP request to look up.
   * @returns The cached {@link HttpResponse}, or `undefined` on a miss or expiry.
   */
  get(req: HttpRequest<any>): HttpResponse<any> | undefined {
    const url = req.urlWithParams;
    const cached = this.cache.get(url);

    if (!cached) {
      return undefined;
    }

    const isExpired = cached.lastRead < Date.now() - maxAge;
    const expired = isExpired ? 'expired ' : '';
    if (!environment.production) {
      // eslint-disable-next-line no-console
      console.log(`Found ${expired}cached response for "${url}".`);
    }

    return isExpired ? undefined : cached.response;
  }

  /**
   * Stores a response for the given request and prunes expired entries.
   *
   * The full request URL (`urlWithParams`) is used as the cache key, replacing
   * any previous entry for that URL. After writing, all entries older than
   * {@link maxAge} are removed from the cache.
   *
   * @param req The HTTP request that produced the response.
   * @param response The HTTP response to cache.
   */
  put(req: HttpRequest<any>, response: HttpResponse<any>): void {
    const url = req.urlWithParams;
    if (!environment.production) {
      // eslint-disable-next-line no-console
      console.log(`Caching response from "${url}".`);
    }
    const entry = { url, response, lastRead: Date.now() };
    this.cache.set(url, entry);

    // remove expired cache entries
    const expired = Date.now() - maxAge;
    this.cache.forEach((cacheEntry) => {
      if (cacheEntry.lastRead < expired) {
        this.cache.delete(cacheEntry.url);
      }
    });

    if (!environment.production) {
      // eslint-disable-next-line no-console
      console.log(`Request cache size: ${this.cache.size}.`);
    }
  }
}
