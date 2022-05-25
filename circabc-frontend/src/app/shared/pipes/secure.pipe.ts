import {
  ChangeDetectorRef,
  OnDestroy,
  Pipe,
  PipeTransform,
} from '@angular/core';

import { BehaviorSubject, Observable, Subscription } from 'rxjs';

import { DomSanitizer, SafeUrl } from '@angular/platform-browser';

import { UrlHelperService } from 'app/core/url-helper.service';

// Using similarity from AsyncPipe to avoid having to pipe |secure|async in HTML.
@Pipe({
  name: 'cbcSecure',
  // eslint-disable-next-line @angular-eslint/no-pipe-impure
  pure: false,
})
export class SecurePipe implements PipeTransform, OnDestroy {
  /* eslint-disable @typescript-eslint/naming-convention, no-underscore-dangle, id-blacklist, id-match */
  private _latestValue: {} | null = null;
  private _latestReturnedValue: {} | null = null;
  private _subscription: Subscription | null = null;
  private _obj: Observable<SafeUrl> | null = null;

  private previousUrl!: string;
  private _result: BehaviorSubject<SafeUrl | null> =
    new BehaviorSubject<SafeUrl | null>(null);
  private result: Observable<SafeUrl | null> = this._result.asObservable();
  private _internalSubscription: Subscription | null = null;

  constructor(
    private _ref: ChangeDetectorRef,
    private urlHelperService: UrlHelperService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnDestroy(): void {
    if (this._subscription) {
      this._dispose();
    }
  }

  transform(url: string): {} {
    if (url) {
      const obj = this.internalTransform(url);
      return this.asyncTransform(obj);
    } else {
      return {};
    }
  }

  private internalTransform(url: string): Observable<SafeUrl> {
    if (!url) {
      return this.result as Observable<SafeUrl>;
    }

    if (this.previousUrl !== url) {
      this.previousUrl = url;
      this._internalSubscription = this.urlHelperService
        .get(url)
        .subscribe((m: string) => {
          const sanitized = this.sanitizer.bypassSecurityTrustUrl(m);
          this._result.next(sanitized);
        });
    }
    return this.result as Observable<SafeUrl>;
  }

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

  private _subscribe(obj: Observable<SafeUrl>) {
    // eslint-disable-next-line @typescript-eslint/no-this-alias
    const _this = this;
    this._obj = obj;

    this._subscription = obj.subscribe({
      // eslint-disable-next-line @typescript-eslint/ban-types
      next(value: Object) {
        return _this._updateLatestValue(obj, value);
      },
      error: (e: {}) => {
        throw e;
      },
    });
  }

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

  // eslint-disable-next-line @typescript-eslint/ban-types
  private _updateLatestValue(async: {}, value: Object) {
    if (async === this._obj) {
      this._latestValue = value;
      this._ref.markForCheck();
    }
  }
}
