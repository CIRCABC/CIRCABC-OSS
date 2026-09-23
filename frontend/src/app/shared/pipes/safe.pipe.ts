import { inject, Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

/**
 * Determines whether the given value is a safe absolute resource URL, i.e. one
 * using the `http:` or `https:` scheme.
 *
 * Values using any other scheme (such as `javascript:`, `data:` or `file:`),
 * relative URLs, or malformed values are rejected so they can never be marked
 * as trusted resource URLs and reach the DOM.
 *
 * @param url The URL to validate; may be `undefined`.
 * @returns `true` when `url` is a well-formed `http`/`https` URL, `false` otherwise.
 */
export function isAllowedResourceUrl(url: string | undefined): url is string {
  if (!url) {
    return false;
  }

  try {
    const parsedUrl = new URL(url);
    return parsedUrl.protocol === 'http:' || parsedUrl.protocol === 'https:';
  } catch {
    return false;
  }
}

/**
 * Angular pipe (`cbcSafe`) that marks a URL as a trusted resource URL,
 * bypassing Angular's built-in DOM sanitization.
 *
 * It is intended for binding trusted URLs coming from the backend to
 * resource contexts such as `<iframe>` `src` attributes (e.g. news iframes
 * and other embedded content). Only absolute `http`/`https` URLs are accepted;
 * any other value is rejected before it can reach the sanitizer, preventing
 * unsafe schemes (e.g. `javascript:`) from introducing cross-site scripting
 * (XSS) vulnerabilities.
 *
 * @example
 * ```html
 * <iframe [src]="trustedUrl | cbcSafe"></iframe>
 * ```
 */
@Pipe({
  name: 'cbcSafe',
})
export class SafePipe implements PipeTransform {
  /** Angular sanitizer used to produce the trusted {@link SafeResourceUrl}. */
  private readonly sanitizer = inject(DomSanitizer);
  /**
   * Transforms a raw URL string into a {@link SafeResourceUrl} that Angular
   * will use without sanitizing it.
   *
   * @param url The URL to mark as safe for resource binding. Must be an
   *     absolute `http`/`https` URL.
   * @returns A {@link SafeResourceUrl} wrapping the provided URL.
   * @throws Error If `url` is not an absolute `http`/`https` URL.
   */
  transform(url: string | undefined): SafeResourceUrl {
    if (isAllowedResourceUrl(url)) {
      // NOSONAR: Safe - only absolute http/https URLs reach the sanitizer
      return this.sanitizer.bypassSecurityTrustResourceUrl(url); // NOSONAR
    }
    throw new Error('Only http and https resource URLs are allowed');
  }
}
