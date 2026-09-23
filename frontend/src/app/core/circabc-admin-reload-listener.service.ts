import { Service } from '@angular/core';
import { Observable, Subject } from 'rxjs';

/**
 * Application-wide event bus that lets CIRCABC administration components
 * signal and observe "reload" requests without being directly coupled to
 * one another.
 *
 * Provided in the root injector as a singleton, it acts as a lightweight
 * mediator: one part of the admin UI can announce that a refresh is needed
 * (for example after a configuration change), while any number of other
 * components subscribe to {@link refreshAnnounced$} to react and reload
 * their own data.
 *
 * Key collaborator: RxJS {@link Subject}, used as the underlying multicast
 * source that fans notifications out to all current subscribers.
 */
@Service()
export class CiracbcAdminReloadListenerService {
  /**
   * Internal multicast source that emits a value each time a CIRCABC admin
   * refresh is requested. Kept private so that only this service can push
   * new events, guaranteeing all notifications go through
   * {@link propagateCircabcAdminRefresh}.
   */
  private readonly refreshSource: Subject<void> = new Subject<void>();

  /**
   * Public, read-only stream that emits whenever a CIRCABC admin refresh is
   * announced. Consumers subscribe to this observable to be notified that
   * they should reload their data.
   */
  public refreshAnnounced$: Observable<void> =
    this.refreshSource.asObservable();

  /**
   * Announces that a CIRCABC admin refresh is required, causing
   * {@link refreshAnnounced$} to emit to all current subscribers.
   *
   * @returns Nothing; the notification is delivered synchronously to
   * subscribers.
   */
  public propagateCircabcAdminRefresh(): void {
    this.refreshSource.next();
  }
}
