import {
  ChangeDetectorRef,
  inject,
  OnDestroy,
  Pipe,
  PipeTransform,
} from '@angular/core';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { UrlHelperService } from 'app/core/url-helper.service';
import { BehaviorSubject, Observable, Subscription } from 'rxjs';

// Using similarity from AsyncPipe to avoid having to pipe |secure|async in HTML.
/**
 * Impure pipe (`cbcSecure`) that resolves a protected resource URL into a
 * sanitized {@link SafeUrl} suitable for binding in templates without the
 * need to chain the `async` pipe (e.g. `url | cbcSecure` instead of
 * `url | secure | async`).
 *
 * The pipe delegates the authenticated fetch to {@link UrlHelperService},
 * which retrieves the resource and returns a blob URL. That value is then
 * marked as trusted via {@link DomSanitizer.bypassSecurityTrustUrl} and
 * emitted through an internal {@link BehaviorSubject}. Internally it mirrors
 * the subscription/change-detection bookkeeping done by Angular's `AsyncPipe`
 * so the latest emitted value is returned synchronously on subsequent
 * template evaluations.
 *
 * Key collaborators:
 * - {@link UrlHelperService} — performs the authenticated request and returns the blob URL.
 * - {@link DomSanitizer} — marks the resolved URL as trusted.
 * - {@link ChangeDetectorRef} — triggers change detection when a new value arrives.
 *
 * @remarks Declared as `pure: false` because the emitted value changes
 * asynchronously after the input URL is resolved.
 */
@Pipe({
  name: 'cbcSecure',

  pure: false,
})
export class SecurePipe implements PipeTransform, OnDestroy {
  private readonly _ref = inject(ChangeDetectorRef);
  private readonly urlHelperService = inject(UrlHelperService);
  private readonly sanitizer = inject(DomSanitizer);

  /** Most recent value emitted by the resolved observable. */
  private _latestValue: {} | null = null;
  /** Last value actually returned from {@link transform}, used to detect changes. */
  private _latestReturnedValue: {} | null = null;
  /** Subscription to the outer (result) observable driving change detection. */
  private _subscription: Subscription | null = null;
  /** The observable currently being tracked, guarding against duplicate subscriptions. */
  private _obj: Observable<SafeUrl> | null = null;

  /** Previously requested URL, used to avoid re-fetching an unchanged input. */
  private previousUrl!: string;
  /** Source subject holding the latest sanitized {@link SafeUrl}. */
  private readonly _result: BehaviorSubject<SafeUrl | null> =
    new BehaviorSubject<SafeUrl | null>(null);
  /** Read-only stream of the resolved sanitized URL, exposed to the async logic. */
  private readonly result: Observable<SafeUrl | null> =
    this._result.asObservable();
  /** Subscription to the {@link UrlHelperService} fetch, disposed on cleanup. */
  private _internalSubscription: Subscription | null = null;

  /**
   * Angular lifecycle hook. Disposes the active subscriptions and internal
   * state when the pipe is destroyed to prevent memory leaks.
   */
  ngOnDestroy(): void {
    if (this._subscription) {
      this._dispose();
    }
  }

  /**
   * Transforms a protected resource URL into a sanitized {@link SafeUrl}.
   *
   * @param url The (authenticated) resource URL to resolve. When empty or
   * falsy, an empty object is returned.
   * @returns The latest resolved {@link SafeUrl} value, or an empty object
   * while the value is still being fetched or when no URL is provided.
   */
  transform(url: string): {} {
    if (url) {
      const obj = this.internalTransform(url);
      return this.asyncTransform(obj);
    }
    return {};
  }

  /**
   * Kicks off (or reuses) the authenticated fetch for the given URL and
   * returns the observable stream of the sanitized result.
   *
   * When the URL differs from the previously requested one, a new request is
   * issued via {@link UrlHelperService}; the response is sanitized with
   * {@link DomSanitizer.bypassSecurityTrustUrl} and pushed onto the internal
   * result subject.
   *
   * @param url The resource URL to fetch and sanitize.
   * @returns The observable emitting the sanitized {@link SafeUrl}.
   */
  private internalTransform(url: string): Observable<SafeUrl> {
    if (!url) {
      return this.result as Observable<SafeUrl>;
    }

    if (this.previousUrl !== url) {
      this.previousUrl = url;
      this._internalSubscription = this.urlHelperService
        .get(url)
        .subscribe((m: string) => {
          // NOSONAR: Safe - URL is fetched via authenticated API and converted to blob URL
          const sanitized = this.sanitizer.bypassSecurityTrustUrl(m); // NOSONAR
          this._result.next(sanitized);
        });
    }
    return this.result as Observable<SafeUrl>;
  }

  /**
   * Mirrors Angular's `AsyncPipe` behavior: subscribes to the provided
   * observable, tracks the latest emitted value, and returns it
   * synchronously on subsequent evaluations. Re-subscribes when the
   * observable instance changes.
   *
   * @param obj The observable emitting the sanitized {@link SafeUrl}.
   * @returns The latest emitted value, or the previously returned value when
   * nothing has changed.
   */
  private asyncTransform(obj: Observable<SafeUrl>): {} {
    if (!this._obj) {
      if (obj) {
        this._subscribe(obj);
      }
      this._latestReturnedValue = this._latestValue;
      return this._latestValue as {};
    }
    if (obj !== this._obj) {
      this._dispose();
      return this.asyncTransform(obj);
    }
    if (this._latestValue === this._latestReturnedValue) {
      return this._latestReturnedValue as {};
    }
    this._latestReturnedValue = this._latestValue;
    return this._latestValue as {};
  }

  /**
   * Subscribes to the given observable and updates the latest value on each
   * emission, propagating errors to the caller.
   *
   * @param obj The observable to subscribe to.
   * @throws Re-throws any error emitted by the observable.
   */
  private _subscribe(obj: Observable<SafeUrl>) {
    this._obj = obj;

    this._subscription = obj.subscribe({
      next: (value: object) => {
        return this._updateLatestValue(obj, value);
      },
      error: (e: {}) => {
        throw e;
      },
    });
  }

  /**
   * Unsubscribes from all active subscriptions and resets the internal state
   * so the pipe can be re-used or safely garbage-collected.
   */
  private _dispose() {
    if (this._subscription) {
      this._subscription.unsubscribe();
    }
    if (this._internalSubscription) {
      this._internalSubscription.unsubscribe();
    }

    this._internalSubscription = null;
    this._latestValue = null;
    this._latestReturnedValue = null;
    this._subscription = null;
    this._obj = null;
  }

  /**
   * Stores the latest emitted value and marks the view for check, but only
   * when the emission originates from the currently tracked observable.
   *
   * @param async The observable that produced the value.
   * @param value The newly emitted value.
   */
  private _updateLatestValue(async: {}, value: object) {
    if (async === this._obj) {
      this._latestValue = value;
      this._ref.markForCheck();
    }
  }
}
