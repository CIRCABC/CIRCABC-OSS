/* eslint-disable @typescript-eslint/no-explicit-any */
import { HttpEvent, HttpRequest, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'environments/environment';

interface RequestCacheEntry {
  url: string;
  response: HttpResponse<any>;
  lastRead: number;
}
export abstract class RequestCache {
  abstract get(req: HttpRequest<any>): HttpEvent<any> | undefined;
  abstract put(req: HttpRequest<any>, response: HttpResponse<any>): void;
}

const maxAge = 300000; // maximum cache age (ms)

// eslint-disable-next-line max-classes-per-file
@Injectable()
export class RequestCacheWithMap implements RequestCache {
  cache = new Map<string, RequestCacheEntry>();

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
