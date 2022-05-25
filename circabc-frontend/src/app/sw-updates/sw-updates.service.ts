import { ApplicationRef, Injectable, OnDestroy } from '@angular/core';
import { SwUpdate } from '@angular/service-worker';
import { concat, interval, NEVER, Observable, Subject } from 'rxjs';
import { first, map, takeUntil, tap } from 'rxjs/operators';

/**
 * copy of  https://github.com/angular/angular/blob/master/aio/src/app/sw-updates/sw-updates.service.ts
 * SwUpdatesService
 *
 * @description
 * 1. Checks for available ServiceWorker updates once instantiated.
 * 2. Re-checks every 6 hours.
 * 3. Whenever an update is available, it activates the update.
 *
 */
@Injectable()
export class SwUpdatesService implements OnDestroy {
  private checkInterval = 1000 * 60 * 60 * 6; // 6 hours
  private onDestroy = new Subject<void>();
  // `updateActivated` {Observable<string>} - Emit the version hash whenever an update is activated.
  updateActivated: Observable<string>;

  constructor(appRef: ApplicationRef, private swu: SwUpdate) {
    if (!swu.isEnabled) {
      this.updateActivated = NEVER.pipe(takeUntil(this.onDestroy));
      return;
    }

    // Periodically check for updates (after the app is stabilized).
    const appIsStable = appRef.isStable.pipe(first((v) => v));
    concat(appIsStable, interval(this.checkInterval))
      .pipe(
        tap(() => this.log('Checking for update...')),
        takeUntil(this.onDestroy)
      )
      .subscribe(async () => this.swu.checkForUpdate());

    // Activate available updates.
    this.swu.available
      .pipe(
        tap((evt) => this.log(`Update available: ${JSON.stringify(evt)}`)),
        takeUntil(this.onDestroy)
      )
      .subscribe(async () => this.swu.activateUpdate());

    // Notify about activated updates.
    this.updateActivated = this.swu.activated.pipe(
      tap((evt) => this.log(`Update activated: ${JSON.stringify(evt)}`)),
      map((evt) => evt.current.hash),
      takeUntil(this.onDestroy)
    );
  }

  ngOnDestroy() {
    this.onDestroy.next();
  }

  private log(message: string) {
    const timestamp = new Date().toISOString();
    // eslint-disable-next-line no-console
    console.log(`[SwUpdates - ${timestamp}]: ${message}`);
  }
}
