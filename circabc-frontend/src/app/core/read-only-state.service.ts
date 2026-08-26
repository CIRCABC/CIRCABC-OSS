import { Injectable, signal } from '@angular/core';

/**
 * Service that holds the read-only state for the current Interest Group.
 * When an IG is locked in read-only mode, write operations (create, edit, delete)
 * should be hidden/disabled in the UI.
 */
@Injectable({ providedIn: 'root' })
export class ReadOnlyStateService {
  private readonly _isReadOnly = signal(false);

  /** Whether the current IG is in read-only mode */
  public readonly isReadOnly = this._isReadOnly.asReadonly();

  /** Set the read-only state based on the IG's lock info */
  public setReadOnly(readOnly: boolean): void {
    this._isReadOnly.set(readOnly);
  }

  /** Reset the read-only state (e.g., when navigating away from the IG) */
  public reset(): void {
    this._isReadOnly.set(false);
  }
}
