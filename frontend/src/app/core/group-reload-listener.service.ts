import { Service } from '@angular/core';
import { Observable, Subject } from 'rxjs';

/**
 * Application-wide event bus service that lets any component or service signal
 * that a specific interest group's data should be reloaded, and lets other
 * parts of the application react to those signals.
 *
 * It is provided in the root injector (singleton) so that publishers and
 * subscribers share the same underlying RxJS {@link Subject}. Typically an
 * action that mutates group state (e.g. membership or configuration changes)
 * calls {@link GroupReloadListenerService.propagateGroupRefresh} to broadcast
 * the affected group id, while views observing the group subscribe to
 * {@link GroupReloadListenerService.refreshAnnounced$} to refresh themselves.
 *
 * Key collaborators: RxJS {@link Subject}/{@link Observable} for the underlying
 * multicast stream.
 */
@Service()
export class GroupReloadListenerService {
  /**
   * Internal multicast source used to emit group refresh notifications.
   * Kept private so that only {@link propagateGroupRefresh} can push values,
   * ensuring the stream is write-controlled by this service.
   */
  private readonly refreshSource: Subject<string> = new Subject<string>();

  /**
   * Public read-only stream that emits the id of a group whenever a refresh is
   * announced. Consumers subscribe to this observable to be notified that the
   * corresponding group should be reloaded.
   */
  public refreshAnnounced$: Observable<string> =
    this.refreshSource.asObservable();

  /**
   * Broadcasts a refresh request for the given group to all current
   * subscribers of {@link refreshAnnounced$}.
   *
   * @param groupId - The identifier of the interest group that should be
   * reloaded.
   * @returns Nothing; the value is emitted to subscribers asynchronously via
   * the underlying subject.
   */
  public propagateGroupRefresh(groupId: string): void {
    this.refreshSource.next(groupId);
  }
}
