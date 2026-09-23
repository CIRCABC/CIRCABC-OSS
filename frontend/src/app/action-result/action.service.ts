import { Service } from '@angular/core';
import { ActionEmitterResult } from 'app/action-result';
import { Observable, Subject } from 'rxjs';

/**
 * Application-wide, singleton service (`providedIn: 'root'`) that acts as an
 * event bus for user/system actions across the CIRCABC frontend.
 *
 * Feature components that complete an action (for example uploading a
 * document, creating a group or editing metadata) call
 * {@link ActionService.propagateActionFinished} to broadcast an
 * {@link ActionEmitterResult}. Other components subscribe to the
 * {@link ActionService.actionFinished$} stream to react to those events —
 * typically to refresh their view or display a notification — without being
 * directly coupled to the component that triggered the action.
 *
 * Key collaborators:
 * - {@link ActionEmitterResult}: the payload describing the completed action.
 * - RxJS {@link Subject}/{@link Observable}: back the internal event stream.
 */
@Service()
export class ActionService {
  /**
   * Internal RxJS subject used to emit action-finished events. Kept private
   * so that only this service can push values, while consumers observe the
   * exposed read-only {@link ActionService.actionFinished$} stream.
   */
  private readonly actionFinishedSource: Subject<ActionEmitterResult> =
    new Subject<ActionEmitterResult>();
  /**
   * Read-only observable stream of {@link ActionEmitterResult} values.
   * Components subscribe to this to be notified whenever an action has
   * finished elsewhere in the application.
   */
  public actionFinished$: Observable<ActionEmitterResult> =
    this.actionFinishedSource.asObservable();

  /**
   * Broadcasts that an action has finished by emitting the given result on
   * the {@link ActionService.actionFinished$} stream, notifying all current
   * subscribers.
   *
   * @param actionFinished The result describing the completed action to
   * propagate to subscribers.
   * @returns Nothing.
   */
  public propagateActionFinished(actionFinished: ActionEmitterResult): void {
    this.actionFinishedSource.next(actionFinished);
  }
}
