import {
  ChangeDetectionStrategy,
  Component,
  input,
  linkedSignal,
  output,
} from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { type User } from 'app/core/generated/circabc';

/**
 * Presentational component that renders a single user entry within the user
 * management search/results list.
 *
 * It displays the given {@link User} and exposes contextual actions (select and
 * remove) that are surfaced when the row is focused. Focus is coordinated
 * externally through the `focusedUserId` input/output pair so that only one
 * result box in a list is expanded at a time. The component holds no business
 * logic of its own: it merely emits events for the parent to act upon.
 *
 * Selector: `cbc-user-result-box`.
 */
@Component({
  selector: 'cbc-user-result-box',
  templateUrl: './user-result-box.component.html',
  styleUrl: './user-result-box.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslocoModule],
})
export class UserResultBoxComponent {
  /** Required input holding the user rendered by this result box. */
  readonly user = input.required<User>();

  /**
   * Aliased input (`focusedUserId`) carrying the id of the currently focused
   * user in the enclosing list. When it matches this box's user id, the box is
   * considered focused/expanded.
   */
  // eslint-disable-next-line @angular-eslint/no-input-rename
  focusedUserIdInput = input<string>('', { alias: 'focusedUserId' });

  /**
   * Writable local mirror of {@link focusedUserIdInput}. It stays in sync with
   * the input but can be updated locally (e.g. via {@link toggleFocus}) before
   * the change is propagated back to the parent.
   */
  focusedUserId = linkedSignal(this.focusedUserIdInput);

  /** Whether the select action should be offered for this user. Defaults to `true`. */
  readonly showSelect = input(true);

  /** Whether the remove action should be offered for this user. Defaults to `true`. */
  readonly showRemove = input(true);

  /** Emits the updated focused user id whenever the focus is toggled. */
  readonly focusedUserIdChange = output<string>();

  /** Emits the current {@link User} when the user is selected. */
  readonly selectionTriggered = output<User>();

  /** Emits the current {@link User} when a removal is requested. */
  readonly removeTriggered = output<User>();

  /**
   * Toggles the focused state of this result box.
   *
   * If this box's user is already focused, focus is cleared; otherwise this
   * user's id becomes the focused id. The resulting focused id is emitted
   * through {@link focusedUserIdChange} so the parent list can keep other boxes
   * collapsed.
   */
  public toggleFocus() {
    const user = this.user();
    if (this.focusedUserId() === user.userId) {
      this.focusedUserId.set('');
    } else if (user.userId) {
      this.focusedUserId.set(user.userId);
    }
    this.focusedUserIdChange.emit(this.focusedUserId());
  }

  /**
   * Requests selection of this box's user.
   *
   * Emits {@link selectionTriggered} with the current {@link User} and stops the
   * event from bubbling further.
   *
   * @param event - The originating DOM event; its propagation is stopped.
   */
  public triggerSelect(event: Event) {
    this.selectionTriggered.emit(this.user());
    event.stopPropagation();
  }

  /**
   * Requests removal of this box's user.
   *
   * Emits {@link removeTriggered} with the current {@link User} and stops the
   * event from bubbling further.
   *
   * @param event - The originating DOM event; its propagation is stopped.
   */
  public triggerRemove(event: Event) {
    this.removeTriggered.emit(this.user());
    event.stopPropagation();
  }
}
