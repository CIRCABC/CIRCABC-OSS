import { Injectable, signal } from '@angular/core';

/**
 * Service that holds the lock state for the current Interest Group.
 * Shared between GroupComponent (which drives the header icon) and
 * GroupAdminComponent (which performs lock/unlock operations).
 */
@Injectable({ providedIn: 'root' })
export class GroupLockStateService {
  private readonly _isLocked = signal(false);

  /** Whether the current IG is locked */
  public readonly isLocked = this._isLocked.asReadonly();

  /** Update the locked state */
  public setLocked(locked: boolean): void {
    this._isLocked.set(locked);
  }

  /** Reset when leaving the IG */
  public reset(): void {
    this._isLocked.set(false);
  }
}
