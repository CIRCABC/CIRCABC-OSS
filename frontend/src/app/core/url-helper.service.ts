import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable, Subscriber } from 'rxjs';

/**
 * Root-provided service that fetches remote resources as blobs and exposes them
 * through cached object URLs suitable for direct use in the DOM (e.g. `img[src]`).
 *
 * When a URL is requested it is downloaded once via the Angular {@link HttpClient}
 * as a `blob`, converted into an object URL with `URL.createObjectURL`, and cached
 * so subsequent requests for the same URL resolve immediately without a new HTTP
 * call. A simple size-bounded cache guards against unbounded memory growth by
 * revoking and evicting object URLs when the cache grows beyond its limit.
 *
 * Key collaborators:
 * - {@link HttpClient} — performs the blob download of the requested resource.
 */
@Service()
export class UrlHelperService {
  /** Angular HTTP client used to download the requested resources as blobs. */
  private readonly http = inject(HttpClient);

  /**
   * In-memory cache mapping the originally requested URL to the object URL
   * created from its downloaded blob, avoiding repeated network requests.
   */
  private readonly cacheUrls: Map<string, string> = new Map();

  /**
   * Resolves the given resource URL to a browser object URL, emitting it through
   * the returned observable.
   *
   * If the URL has already been fetched, the cached object URL is emitted
   * immediately. Otherwise the resource is downloaded as a blob, converted into
   * an object URL, cached and then emitted. When the cache exceeds its fixed
   * size limit, the observable returns a teardown function that revokes and
   * removes the created object URL on unsubscription to release memory.
   *
   * @param url The URL of the resource to fetch and convert into an object URL.
   * @returns An {@link Observable} that emits the object URL for the resource.
   */
  get(url: string): Observable<string> {
    return new Observable((observer: Subscriber<string>) => {
      let objectUrl: string | null = null;
      if (this.cacheUrls.has(url)) {
        objectUrl = this.cacheUrls.get(url) ?? null;
        observer.next(objectUrl ?? '');
      } else {
        this.http.get(url, { responseType: 'blob' }).subscribe((m) => {
          objectUrl = URL.createObjectURL(m);
          this.cacheUrls.set(url, objectUrl);
          observer.next(objectUrl);
        });
      }
      const cacheSize = 64;
      if (this.cacheUrls.size > cacheSize) {
        return () => {
          if (objectUrl) {
            URL.revokeObjectURL(objectUrl);
            this.cacheUrls.delete(url);
            objectUrl = null;
          }
        };
      }
      return undefined;
    });
  }
}
