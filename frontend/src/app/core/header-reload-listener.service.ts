import { Service } from '@angular/core';
import { Observable, Subject } from 'rxjs';

/**
 * Application-wide event bus that coordinates reloading of the header UI.
 *
 * This root-provided service decouples the components that trigger a header
 * refresh from the header component(s) that need to react to it. Producers
 * call {@link HeaderReloadListenerService.propagateHeaderRefresh} to announce
 * that the header should be reloaded, while consumers subscribe to
 * {@link HeaderReloadListenerService.refreshAnnounced$} to be notified.
 *
 * It is backed by an RxJS {@link Subject}, so emissions are hot and only
 * delivered to subscribers that are active at the moment the event is fired.
 */
@Service()
export class HeaderReloadListenerService {
  /**
   * Internal multicast source used to emit header-refresh notifications.
   * Kept private so that only this service can trigger emissions, while
   * consumers observe through {@link refreshAnnounced$}.
   */
  private readonly refreshSource: Subject<void> = new Subject<void>();

  /**
   * Stream that emits (with no payload) every time a header refresh is
   * announced. Consumers subscribe to this observable to reload the header.
   */
  public refreshAnnounced$: Observable<void> =
    this.refreshSource.asObservable();

  /**
   * Announces that the header should be reloaded by emitting on
   * {@link refreshAnnounced$}. All currently active subscribers are notified.
   *
   * @returns Nothing.
   */
  public propagateHeaderRefresh(): void {
    this.refreshSource.next();
  }
}
